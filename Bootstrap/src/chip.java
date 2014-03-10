/*
 *  This class abstracts the memory domains within the machine.
 *  It contains cores. 
 */
public class chip extends helper{

	public int 	number_cores;
	public core core_array[];
	public int load;
	public int mem_domain_id;
	
	public chip(int number_cores, int id)
	{
		this.mem_domain_id = id;
		this.number_cores = number_cores;
		core_array = new core[number_cores];
		for(int i = 0; i < number_cores; i++)
		{
			core_array[i] = new core();
			core_array[i].core_id = i;
		}
		load = 0;
	}
	
	public void update_load()
	{
		load = 0;
		for(int i = 0; i < number_cores; i++)
		{
			load += core_array[i].load;
		}
	}
	
	public core get_min_load_core()
	{
		int min_load = core_array[0].MAX_THREADS_PER_CORE+1;
		core target = null;
		
		for(int i = 0; i < number_cores; i++)
		{
			if(core_array[i].load < min_load)
			{
				min_load = core_array[i].load;
				target = core_array[i];
			}	
		}
		
		return target;
	}
	
	public core get_max_load_core()
	{
		int max_load = -1;
		core target = null;
		
		for(int i = 0; i < number_cores; i++)
		{
			if(core_array[i].load > max_load)
			{
				max_load = core_array[i].load;
				target = core_array[i];
			}	
		}
		
		return target;
	}
	
	public void balance_load()
	{
		core min_core = get_min_load_core();
		core max_core = get_max_load_core();
		
		while(min_core.load < max_core.load+1)
		{
			Thread migrant = max_core.runQ[0];
			max_core.migrate_thread(migrant, min_core);
			min_core = get_min_load_core();
			max_core = get_max_load_core();
		}
	}
}
