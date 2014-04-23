package com.synar.akula.hardware;

import com.synar.akula.software.Thread;

/*
 *  This class abstracts the individual cores within the memory domains.
 *  Thread objects are added to cores. 
 *  Core objects are added to machine objects.
 */
public class Core{

	/*
	 * The main data structure of the core object is the run queue which 
	 * contains the threads bound to it.
	 */
	public static int MAX_THREADS_PER_CORE = 10;
	public int mLoad = 0;
	public int mCoreId;
	public Thread[] mRunQueue = new Thread[MAX_THREADS_PER_CORE];

	/*
	 * Operations on the run_queue.
	 */
	public boolean addThread(Thread new_thread)
	{
		//Ensure there is room.
		if ( mLoad < MAX_THREADS_PER_CORE )
		{
			System.out.println("No room in run_queue");
			return false;
		}

		mRunQueue[mLoad] = new_thread;
		mLoad++;
		return true;
	}
	
	public boolean removeThread(Thread removed_thread)
	{
		int location = getThreadLocation(removed_thread);
		if ( location < 0 )
		{
			System.out.println("Thread not found");
			return false;
		}

		mRunQueue[location] = null;
		consolidateQ(location);
		mLoad--;
		return true;
	}

	public void migrateThread(Thread migrant_thread, Core destination_core)
	{
		removeThread(migrant_thread);
		destination_core.addThread(migrant_thread);
	}
	
	public void migrateThreadMachine(Thread migrant_thread, Core destination_core)
	{
		removeThread(migrant_thread);
		//increase migrant thread time
		destination_core.addThread(migrant_thread);
	}
	public void migrateThreadRack(Thread migrant_thread, Core destination_core)
	{
		removeThread(migrant_thread);
		//increase migrant thread time
		destination_core.addThread(migrant_thread);
	}
		
	private int getThreadLocation(Thread search_thread)
	{
		for(int i = 0; i < mLoad; i++)
		{
			if(mRunQueue[i].mThreadName.compareTo(search_thread.mThreadName) == 0)
			{
				return i;
			}
		}
		//Could not find the thread on the run_queue.
		return -1;
	}
	
	private void consolidateQ(int removed_location)
	{
		int last_thread = mLoad-1;
		if(removed_location != last_thread)
		{
			mRunQueue[removed_location] = mRunQueue[last_thread];
			mRunQueue[last_thread] = null;
		}
	
	}
}
