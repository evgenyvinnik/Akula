/*
 * The thread class captures individual threads.
 * These are added to a core object to facilitate thread-to-core binding.
 */
public class Thread {

	/*
	 * The set of data that defines a thread. Both real and simulated threads. 
	 */
	public String 	thread_name = null;
	public int 		benchmark_id = -1; // Needed for stats and bootstrap modules.
	public double 	start_time;
	public double	end_time;
	
	/*
	 * The executable and run directory are only used for the wrapper module.
	 */
	public String 	executable = null;
	public String 	run_dir = null;

	/*
	 * The thread progress value is only used during bootstrap evaluations.
	 */
	public double	progress_bar = 0;
	
	/*
	 * Here we keep a tick by tick history of the thread's execution.
	 * These are stored as a linked list.
	 */
	public int			number_ticks = 0;
	public history_node	head = null;
	
	
	/*
	 * Method to add a history node.
	 */
	public void record_tick(double time, int mem_domain_id, int core_id, double perf_degrad)
	{
		history_node new_node = new history_node();
		new_node.time = time;
		new_node.mem_domain_id = mem_domain_id;
		new_node.core_id = core_id;
		new_node.perf_degrad = perf_degrad;
		
		if(number_ticks == 0)
		{
			head = new_node;
			new_node.next = null;
		}
		else
		{
			new_node.next = head;
			head = new_node;
		}
		
		number_ticks++;
	}
}
