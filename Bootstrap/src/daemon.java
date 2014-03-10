import java.util.*;
import java.io.*;
/*
 * This class interacts with the daemon that attaches all affinity masks,
 * performance counters, etc on a real machine.
 */
public class daemon {

	public double get_current_time()
	{
		return 0;
	}
	
	public boolean thread_alive(Thread target_thread)
	{
		return true;
	}
	
	public void launch_thread(Thread new_thread)
	{
		
	}
	
	public void sched_sleep(double tick)
	{

	}
	
	public void enforce_mappings(Thread target_thread, int domain_id, int core_id)
	{
		
	}
	
	public void enforce_counters(Thread target_thread)
	{
		
	}
	
	public int convert_to_core_id(int mem_domain, int core_number)
	{
		/*
		 * This is a machine specific function that needs to be manually
		 * added to translate the mem_domain/core numbers into core numbers
		 * as understood by the actual OS.
		 */
		return 0;
	}
}
