package com.synar.akula.software;

import com.synar.akula.HistoryNode;

/*
 * The thread class captures individual threads.
 * These are added to a core object to facilitate thread-to-core binding.
 */
public class Thread {

	/*
	 * The set of data that defines a thread. Both real and simulated threads. 
	 */
	public String 	mThreadName = null;
	public int 		mBenchmarkId = -1; // Needed for stats and bootstrap modules.
	public double 	mStartTime;
	public double	mEndTime;
	
	/*
	 * The executable and run directory are only used for the wrapper module.
	 */
	public String 	executable = null;
	public String 	mRunDir = null;

	/*
	 * The thread progress value is only used during bootstrap evaluations.
	 */
	public double	mProgressBar = 0;
	
	/*
	 * Here we keep a tick by tick history of the thread's execution.
	 * These are stored as a linked list.
	 */
	public int			mNumberTicks = 0;
	public HistoryNode	mHead = null;
	
	
	/*
	 * Method to add a history node.
	 */
	public void recordTick(double time, int mem_domain_id, int core_id, double perf_degrad)
	{
		HistoryNode new_node = new HistoryNode();
		new_node.mTime = time;
		new_node.mMemMomainId = mem_domain_id;
		new_node.mMoreId = core_id;
		new_node.mPerfDegrad = perf_degrad;
		
		if(mNumberTicks == 0)
		{
			mHead = new_node;
			new_node.mNext = null;
		}
		else
		{
			new_node.mNext = mHead;
			mHead = new_node;
		}
		
		mNumberTicks++;
	}
}
