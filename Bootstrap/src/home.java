
public class home {

	public static void main(String[] args) {
		
		System.out.println("hello world");
		
		/*
		 * 	Initialize everything needed for a bootstrap simulation.
		 */
		
		// Read in the the bootstrap data from files and create the lookup tables. 
		initializer init = new initializer();
		bootstrap_DB degrad_matrix = init.get_degrad_matrix();
		bootstrap_DB solo_matrix = init.get_solo_matrix();
		
		// Initialize a machine with 2 memory-domains of 4-cores each.
		machine M = new machine(2, 4);
		
		// Create the initial workload. (4 devil threads MCF and 4 turtle threads gamess).
		// To make this more versatile it is best to create workload files with thread data 
		// and a parser which creates workloads by reading the files. 
		Thread workload[] = new Thread[8];
		for(int i = 0; i < 4; i++)
		{
			Thread new_thread = new Thread();
			new_thread.thread_name = "MCF" + i;
			new_thread.benchmark_id = 3; //The benchmark id of MCF see initializer.java
			new_thread.start_time = i;
			workload[i] = new_thread;
		}
		for(int i = 4; i < 8; i++)
		{
			Thread new_thread = new Thread();
			new_thread.thread_name = "GAMESS" + (i-4);
			new_thread.benchmark_id = 8; //The benchmark id of GAMESS see initializer.java
			new_thread.start_time = i;
			workload[i] = new_thread;
		}
		
		/*
		 * Launch the bootstrap module.
		 */
		double tick = 1.00;
		bootstrap mod = new bootstrap(M, workload, tick, degrad_matrix, solo_matrix);
		mod.run_experiment();
		
		/*
		 * Collect the completed threads and obtain their statistics.
		 */
		stats s = new stats();
		s.solos = solo_matrix;
		s.dumpStats("my_results.txt", workload);
		
		System.out.println("done");
	}

}
