/*
 * This is the user written scheduler which is invoked by the framework.
 */
public class scheduler {

	/*
	 *  This is the naive_cluster implementation. Specifically for a 2-memory domain system.
	 */
/*	
	public void launch_thread(Thread new_thread, machine M)
	{
		//Put the thread on the busiest chip unless all cores are full in which case
		// put it on the other one.
		chip target_chip = M.get_max_load_chip();
		if(target_chip.load >= target_chip.number_cores)
		{
			target_chip = M.get_min_load_chip();
		}
		target_chip.get_min_load_core().add_thread(new_thread);
		target_chip.update_load();
	}
	
	public void update_schedule(machine M)
	{
		//Check if the max_load_chip has room.
		chip max_load_chip = M.get_max_load_chip();
		if(max_load_chip.load < max_load_chip.number_cores)
		{
			//Check if the min_load chip has at least one thread on it.
			chip min_load_chip = M.get_min_load_chip();
			if(min_load_chip.load > 0)
			{
				core remover_core = min_load_chip.get_max_load_core();
				core adder_core = max_load_chip.get_min_load_core();
				Thread migrant = remover_core.runQ[0];
				remover_core.migrate_thread(migrant, adder_core);
				min_load_chip.update_load();
				max_load_chip.update_load();
			}
		}
	}
*/
	/*
	 *  This is the naive_spread implementation. 
	 */
	public void launch_thread(Thread new_thread, machine M)
	{
		//Put the thread on the busiest chip unless all cores are full in which case
		// put it on the other one.
		chip target_chip = M.get_min_load_chip();
		target_chip.get_min_load_core().add_thread(new_thread);
		target_chip.update_load();
	}
	
	public void update_schedule(machine M)
	{
		//Check if there is a large enough difference to move threads.
		chip min_load_chip = M.get_min_load_chip();
		chip max_load_chip = M.get_max_load_chip();
		
		if(min_load_chip.load < (max_load_chip.load-1))
		{
			core remover_core = max_load_chip.get_max_load_core();
			core adder_core = min_load_chip.get_min_load_core();
			Thread migrant = remover_core.runQ[0];
			remover_core.migrate_thread(migrant, adder_core);
			min_load_chip.update_load();
			max_load_chip.update_load();
		}
	}
}
