package com.synar.akula;

import com.synar.akula.hardware.Chip;
import com.synar.akula.hardware.Core;
import com.synar.akula.hardware.Datacenter;
import com.synar.akula.hardware.Machine;
import com.synar.akula.software.Thread;

/*
 * This is the wrapper module which uses a daemon to run the
 * scheduler on a real machine.
 */
public class Wrapper {

	private Datacenter 	mDatacenter;
	private Thread 	mThreadsNotStarted[];
	public Thread 	mThreadsCompleted[];
	private int		mTotalThreads;
	private int		mNextLaunch;
	private int		mNumberDone;
	private Scheduler mScheduler;
	//private double	clock_offset; //TODO: implement clock_offset
	private double  mWallClock;
	private double  mTick;
	//private Daemon D;//TODO: implement daemon
	
	public Wrapper(Datacenter datacenter, Thread threadsNotStarted[], double tick)
	{
		mDatacenter = datacenter;
		this.mThreadsNotStarted = threadsNotStarted;
		this.mTick = tick;
		mTotalThreads = threadsNotStarted.length;
		mThreadsCompleted = new Thread[mTotalThreads];
		mNextLaunch = 0;
		mNumberDone = 0;
		mScheduler = new Scheduler();
		
		//D = new Daemon();//TODO: implement daemon
		//clock_offset = D.get_current_time();//TODO: implement daemon
	}
	
	/*
	 * This is the function that is called externally and runs
	 * the actual evaluation.
	 */
	public void runExperiment()
	{
		//Until all threads have finished.
		while(mNumberDone < mTotalThreads)
		{
			//Update time.
			//wall_clock = D.get_current_time() - clock_offset;//TODO: implement daemon
			//Check if any threads finished.
			//thread_completion();//TODO: implement daemon
			//Update the data structures.
			update_data_structures();
			//Check for ready threads.
			thread_launch();
			//Call scheduler for update.
			mScheduler.updateSchedule(mDatacenter);
			//enforce all mappings.
			enforce_all_settings();
			//Sleep for one tick.
			//D.sched_sleep(tick);//TODO: implement daemon
		}
		
	}
	
	//TODO: implement daemon
//	private void thread_completion()
//	{
//		/*
//		 * We go through every thread that the java framework thinks is on the machine.
//		 */
//		M.update_load();
//		
//		for(int i = 0; i < M.mNumberMemDomains; i++)
//		{
//			Chip target_domain = M.mem_domains[i];
//			for(int j = 0; j < target_domain.mNumberCores; j++)
//			{
//				Core target_core = target_domain.mCoreArray[j];
//				for(int k = 0; k < target_core.mLoad; k++)
//				{
//					Thread target_thread = target_core.mRunQueue[k];
//					//Check if the thread is still alive in the machine.
//					if(!D.thread_alive(target_thread))
//					{
//						target_thread.mEndTime = wall_clock;
//						target_core.removeThread(target_thread);
//						threads_completed[number_done] = target_thread;
//						number_done++;
//					}
//				}
//			}
//		}
//	}
	
	private void update_data_structures()
	{
		
	}
	
	private void thread_launch()
	{
		while((mNextLaunch < mTotalThreads) && 
				(mThreadsNotStarted[mNextLaunch].mStartTime <= mWallClock ))
		{
			Thread new_thread = mThreadsNotStarted[mNextLaunch];
			new_thread.mStartTime = mWallClock;
			mNextLaunch++;
			mScheduler.launchThread(new_thread, mDatacenter);
			//D.launch_thread(new_thread);//TODO: implement daemon
		}
	}
	
	private void enforce_all_settings()
	{
		for(int i = 0; i < mDatacenter.mNumberMemDomains; i++)
		{
			Chip target_domain = mDatacenter.mem_domains[i];
			for(int j = 0; j < target_domain.mNumberCores; j++)
			{
				Core target_core = target_domain.mCoreArray[j];
				for(int k = 0; k < target_core.mLoad; k++)
				{
					Thread target_thread = target_core.mRunQueue[k];
					//D.enforce_mappings(target_thread, i, j);//TODO: implement daemon
					//D.enforce_counters(target_thread);//TODO: implement daemon
				}
			}
		}
	}
}
