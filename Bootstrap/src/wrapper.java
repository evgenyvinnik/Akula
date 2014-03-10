/*
 * This is the wrapper module which uses a daemon to run the
 * scheduler on a real machine.
 */
public class wrapper {

	private machine 	M;
	private Thread 	threads_not_started[];
	public Thread 	threads_completed[];
	private int		total_threads;
	private int		next_launch;
	private int		number_done;
	private scheduler sched;
	private double	clock_offset;
	private double  wall_clock;
	private double  tick;
	private daemon D;
	
	public wrapper(machine M, Thread threads_not_started[], double tick)
	{
		this.M = M;
		this.threads_not_started = threads_not_started;
		this.tick = tick;
		total_threads = threads_not_started.length;
		threads_completed = new Thread[total_threads];
		next_launch = 0;
		number_done = 0;
		sched = new scheduler();
		
		D = new daemon();
		clock_offset = D.get_current_time();
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
			wall_clock = D.get_current_time() - clock_offset;
			//Check if any threads finished.
			thread_completion();
			//Update the data structures.
			update_data_structures();
			//Check for ready threads.
			thread_launch();
			//Call scheduler for update.
			sched.update_schedule(M);
			//enforce all mappings.
			enforce_all_settings();
			//Sleep for one tick.
			D.sched_sleep(tick);
		}
		
	}
	
	private void thread_completion()
	{
		/*
		 * We go through every thread that the java framework thinks is on the machine.
		 */
		M.update_load();
		
		for(int i = 0; i < M.number_mem_domains; i++)
		{
			chip target_domain = M.mem_domains[i];
			for(int j = 0; j < target_domain.number_cores; j++)
			{
				core target_core = target_domain.core_array[j];
				for(int k = 0; k < target_core.load; k++)
				{
					Thread target_thread = target_core.runQ[k];
					//Check if the thread is still alive in the machine.
					if(!D.thread_alive(target_thread))
					{
						target_thread.end_time = wall_clock;
						target_core.remove_thread(target_thread);
						threads_completed[number_done] = target_thread;
						number_done++;
					}
				}
			}
		}
	}
	
	private void update_data_structures()
	{
		
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
			D.launch_thread(new_thread);
		}
	}
	
	private void enforce_all_settings()
	{
		for(int i = 0; i < M.number_mem_domains; i++)
		{
			chip target_domain = M.mem_domains[i];
			for(int j = 0; j < target_domain.number_cores; j++)
			{
				core target_core = target_domain.core_array[j];
				for(int k = 0; k < target_core.load; k++)
				{
					Thread target_thread = target_core.runQ[k];
					D.enforce_mappings(target_thread, i, j);
					D.enforce_counters(target_thread);
				}
			}
		}
	}
}
