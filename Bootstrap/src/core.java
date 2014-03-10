/*
 *  This class abstracts the individual cores within the memory domains.
 *  Thread objects are added to cores. 
 *  Core objects are added to machine objects.
 */
public class core extends helper{

	/*
	 * The main data structure of the core object is the run queue which 
	 * contains the threads bound to it.
	 */
	public static int MAX_THREADS_PER_CORE = 10;
	public int load = 0;
	public int core_id;
	public Thread[] runQ = new Thread[MAX_THREADS_PER_CORE];

	/*
	 * Operations on the run_queue.
	 */
	public void add_thread(Thread new_thread)
	{
		//Ensure there is room.
		ASSERT("No room in run_queue", (load < MAX_THREADS_PER_CORE) );
		runQ[load] = new_thread;
		load++;
	}
	
	public void remove_thread(Thread removed_thread)
	{
		int location = get_thread_location(removed_thread);
		ASSERT("Thread not found", (location > -1));
		runQ[location] = null;
		consolidateQ(location);
		load--;
	}

	public void migrate_thread(Thread migrant_thread, core destination_core)
	{
		remove_thread(migrant_thread);
		destination_core.add_thread(migrant_thread);
	}
	
	private int get_thread_location(Thread search_thread)
	{
		for(int i = 0; i < load; i++)
		{
			if(runQ[i].thread_name.compareTo(search_thread.thread_name) == 0)
			{
				return i;
			}
		}
		//Could not find the thread on the run_queue.
		return -1;
	}
	
	private void consolidateQ(int removed_location)
	{
		int last_thread = load-1;
		if(removed_location != last_thread)
		{
			runQ[removed_location] = runQ[last_thread];
			runQ[last_thread] = null;
		}
	
	}
}
