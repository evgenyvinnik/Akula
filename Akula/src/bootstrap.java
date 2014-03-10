/*
 * This is the bootstrap evaluation module which uses bootstrap data to run an
 * evaluation of a scheduler.
 */
public class bootstrap extends helper{

	private machine 	M;
	private Thread 	threads_not_started[];
	public Thread 	threads_completed[];
	private int		total_threads;
	private int		next_launch;
	private int		number_done;
	private scheduler sched;
	private double	wall_clock;
	private double  tick;
	private	bootstrap_DB degrad_matrix;
	private bootstrap_DB solo_exec;
	
	public bootstrap(machine M, Thread threads_not_started[], double tick, 
			bootstrap_DB degrad_matrix, bootstrap_DB solo_exec)
	{
		this.M = M;
		this.threads_not_started = threads_not_started;
		this.tick = tick;
		this.degrad_matrix = degrad_matrix;
		this.solo_exec = solo_exec;
		total_threads = threads_not_started.length;
		threads_completed = new Thread[total_threads];
		next_launch = 0;
		number_done = 0;
		sched = new scheduler();
		wall_clock = 0;
	}
	
	/*
	 * This is the function that is called externally and runs
	 * the actual evaluation.
	 */
	public void run_experiment()
	{
		//Until all threads have finished.
		while(number_done < total_threads)
		{
			//Update time.
			wall_clock += tick;
			//Calculate the progress of each thread during the last tick.
			calculate_thread_progress();
			//Check for completed threads.
			thread_completion();
			//Check for ready threads.
			thread_launch();
			//Call scheduler for update.
			sched.update_schedule(M);
		}
		
	}
	
	/*
	 * Looks at every thread on the machine and calculates its progress based on the 
	 * its position and bootstrap data.
	 */
	private void calculate_thread_progress()
	{
		/*
		 * Please note that bootstrap assumes that a core can have a maximum of one
		 * thread mapped to it.
		 */
		for(int mem_dom = 0; mem_dom < M.number_mem_domains; mem_dom++)
		{
			chip target_domain = M.mem_domains[mem_dom];
			int number_cores = target_domain.number_cores;
			Thread target_thread;
			int	target_id;
			int neighbor_threads[] = new int[(number_cores-1)];
			
			for(int c = 0; c < number_cores; c++)
			{
				core target_core = target_domain.core_array[c];
				ASSERT("Number threads bound to core exceeds 1", target_core.load > 1);
				if(target_core.load == 1)
				{
					target_thread = target_core.runQ[0];
					target_id = target_thread.benchmark_id;
					for(int i = 0; i < number_cores-1; i++)
					{
						int neighbor_id = (c + i + 1) % number_cores;
						if(target_domain.core_array[neighbor_id].load ==  0)
						{
							neighbor_threads[i] = 0;
						}
						else
						{
							neighbor_threads[i] = target_domain.core_array[neighbor_id].runQ[0].benchmark_id;
						}
					}
					double thread_perf = degrad_matrix.table_lookup(target_id, neighbor_threads);
					double solo_time = solo_exec.table_lookup(target_id, null);
					
					double thread_progress = tick / solo_time;
					thread_progress *= thread_perf;
					thread_progress *= 100;
					
					target_thread.progress_bar += thread_progress;
					target_thread.record_tick(wall_clock, mem_dom, c, thread_perf);
				}
			}	
		}
	}
	
	private void thread_completion()
	{
		for(int mem_dom = 0; mem_dom < M.number_mem_domains; mem_dom++)
		{
			chip target_domain = M.mem_domains[mem_dom];
			int number_cores = target_domain.number_cores;
			Thread target_thread;
			
			for(int c = 0; c < number_cores; c++)
			{
				if(target_domain.core_array[c].load == 1)
				{
					target_thread = target_domain.core_array[c].runQ[0];
					if(target_thread.progress_bar >= 100.00)
					{
						target_thread.end_time = wall_clock;
						target_domain.core_array[c].remove_thread(target_thread);
						threads_completed[number_done] = target_thread;
						number_done++;
					}
				}
			}
		}
	}
	
	private void thread_launch()
	{
		while((next_launch < total_threads) && 
				(threads_not_started[next_launch].start_time <= wall_clock ))
		{
			Thread new_thread = threads_not_started[next_launch];
			new_thread.start_time = wall_clock;
			next_launch++;
			sched.launch_thread(new_thread, M);
		}
	}
	
}
