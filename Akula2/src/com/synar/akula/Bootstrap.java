package com.synar.akula;

import com.synar.akula.hardware.Chip;
import com.synar.akula.hardware.Core;
import com.synar.akula.hardware.Datacenter;
import com.synar.akula.hardware.Machine;
import com.synar.akula.software.Thread;

/*
 * This is the bootstrap evaluation module which uses bootstrap data to run an
 * evaluation of a scheduler.
 */
public class Bootstrap{

	private Datacenter 	mDatacenter;
	private Thread 	mThreadsNotStarted[];
	public Thread 	mThreadsCompleted[];
	private int		total_threads;
	private int		next_launch;
	private int		number_done;
	private Scheduler sched;
	private double	wall_clock;
	private double  tick;
	private	BootstrapDb degrad_matrix;
	private BootstrapDb solo_exec;
	
	public Bootstrap(Datacenter datacenter, Thread threads_not_started[], double tick, 
					 BootstrapDb degrad_matrix, BootstrapDb solo_exec)
	{
		mDatacenter = datacenter;
		this.mThreadsNotStarted = threads_not_started;
		this.tick = tick;
		this.degrad_matrix = degrad_matrix;
		this.solo_exec = solo_exec;
		total_threads = threads_not_started.length;
		mThreadsCompleted = new Thread[total_threads];
		next_launch = 0;
		number_done = 0;
		sched = new Scheduler();
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
			sched.updateSchedule(mDatacenter);
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
		for(int mem_dom = 0; mem_dom < mDatacenter.number_mem_domains; mem_dom++)
		{
			Chip target_domain = mDatacenter.mem_domains[mem_dom];
			int number_cores = target_domain.mNumberCores;
			Thread target_thread;
			int	target_id;
			int neighbor_threads[] = new int[(number_cores-1)];
			
			for(int c = 0; c < number_cores; c++)
			{
				Core target_core = target_domain.mCoreArray[c];
				if (target_core.mLoad > 1)
				{
					System.out.println("Number threads bound to core exceeds 1");
				}
				else
				{
					target_thread = target_core.mRunQueue[0];
					target_id = target_thread.mBenchmarkId;
					for(int i = 0; i < number_cores-1; i++)
					{
						int neighbor_id = (c + i + 1) % number_cores;
						if(target_domain.mCoreArray[neighbor_id].mLoad ==  0)
						{
							neighbor_threads[i] = 0;
						}
						else
						{
							neighbor_threads[i] = target_domain.mCoreArray[neighbor_id].mRunQueue[0].mBenchmarkId;
						}
					}
					double thread_perf = degrad_matrix.table_lookup(target_id, neighbor_threads);
					double solo_time = solo_exec.table_lookup(target_id, null);
					
					double thread_progress = tick / solo_time;
					thread_progress *= thread_perf;
					thread_progress *= 100;
					
					target_thread.mProgressBar += thread_progress;
					target_thread.recordTick(wall_clock, mem_dom, c, thread_perf);
				}
			}	
		}
	}
	
	private void thread_completion()
	{
		for(int mem_dom = 0; mem_dom < mDatacenter.number_mem_domains; mem_dom++)
		{
			Chip target_domain = mDatacenter.mem_domains[mem_dom];
			int number_cores = target_domain.mNumberCores;
			Thread target_thread;
			
			for(int c = 0; c < number_cores; c++)
			{
				if(target_domain.mCoreArray[c].mLoad == 1)
				{
					target_thread = target_domain.mCoreArray[c].mRunQueue[0];
					if(target_thread.mProgressBar >= 100.00)
					{
						target_thread.mEndTime = wall_clock;
						target_domain.mCoreArray[c].removeThread(target_thread);
						target_domain.updateLoad();
						mThreadsCompleted[number_done] = target_thread;
						number_done++;
					}
				}
			}
		}
	}
	
	private void thread_launch()
	{
		while((next_launch < total_threads) && 
				(mThreadsNotStarted[next_launch].mStartTime <= wall_clock ))
		{
			Thread new_thread = mThreadsNotStarted[next_launch];
			new_thread.mStartTime = wall_clock;
			next_launch++;
			sched.launchThread(new_thread, mDatacenter);
		}
	}
	
}
