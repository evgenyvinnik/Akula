/*
 * This is the top level object that contains memory domains.
 */
public class machine {

	public int number_mem_domains;
	public chip mem_domains[];
	public int load;
	
	public machine(int number_mem_domains, int number_cores)
	{
		load = 0;
		this.number_mem_domains = number_mem_domains;
		mem_domains = new chip[number_mem_domains];
		for(int i = 0; i < number_mem_domains; i++)
		{
			mem_domains[i] = new chip(number_cores, i);
		}
	}
	
	public void update_load()
	{
		load = 0;
		for(int i = 0; i < number_mem_domains; i++)
		{
			mem_domains[i].update_load();
			load += mem_domains[i].load;
		}
	}
	
	public chip get_min_load_chip()
	{
		int min_load = 1 + number_mem_domains*mem_domains[0].core_array[0].MAX_THREADS_PER_CORE;
		chip target = null;
		for(int i = 0; i < number_mem_domains; i++)
		{
			if(mem_domains[i].load < min_load)
			{
				min_load = mem_domains[i].load;
				target = mem_domains[i];
			}
		}
		return target;
	}
	
	public chip get_max_load_chip()
	{
		int max_load = -1;
		chip target = null;
		for(int i = 0; i < number_mem_domains; i++)
		{
			if(mem_domains[i].load > max_load)
			{
				max_load = mem_domains[i].load;
				target = mem_domains[i];
			}
		}
		return target;
	}
	
	public void balance_load()
	{
		chip min_chip = get_min_load_chip();
		chip max_chip = get_max_load_chip();
		
		while(min_chip.load < max_chip.load+1)
		{
			core max_core = max_chip.get_max_load_core();
			core min_core = min_chip.get_min_load_core();
			Thread migrant = max_core.runQ[0];
			
			max_core.migrate_thread(migrant, min_core);
			max_chip.balance_load();
			max_chip.update_load();
			min_chip.balance_load();
			min_chip.update_load();
			
			min_chip = get_min_load_chip();
			max_chip = get_max_load_chip();
		}
	} 
}
