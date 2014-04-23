package com.synar.akula.hardware;

import com.synar.akula.software.Thread;

/*
 * This is the top level object that contains memory domains.
 */
public class Machine {

	public int mNumberMemDomains;
	public Chip mem_domains[];
	public int load;
	
	public Machine(int number_mem_domains, int number_cores)
	{
		load = 0;
		this.mNumberMemDomains = number_mem_domains;
		mem_domains = new Chip[number_mem_domains];
		for(int i = 0; i < number_mem_domains; i++)
		{
			mem_domains[i] = new Chip(number_cores, i);
		}
	}
	
	public void update_load()
	{
		load = 0;
		for(int i = 0; i < mNumberMemDomains; i++)
		{
			mem_domains[i].updateLoad();
			load += mem_domains[i].mLoad;
		}
	}
	
	public Chip get_min_load_chip()
	{
		int min_load = 1 + mNumberMemDomains*Core.MAX_THREADS_PER_CORE;
		Chip target = null;
		for(int i = 0; i < mNumberMemDomains; i++)
		{
			if(mem_domains[i].mLoad < min_load)
			{
				min_load = mem_domains[i].mLoad;
				target = mem_domains[i];
			}
		}
		return target;
	}
	
	public Chip getMaxLoadChip()
	{
		int max_load = -1;
		Chip target = null;
		for(int i = 0; i < mNumberMemDomains; i++)
		{
			if(mem_domains[i].mLoad > max_load)
			{
				max_load = mem_domains[i].mLoad;
				target = mem_domains[i];
			}
		}
		return target;
	}
	
	public void balanceLoad()
	{
		Chip min_chip = get_min_load_chip();
		Chip max_chip = getMaxLoadChip();
		
		while(min_chip.mLoad < max_chip.mLoad+1)
		{
			Core max_core = max_chip.getMaxLoadCore();
			Core min_core = min_chip.getMinLoadCore();
			Thread migrant = max_core.mRunQueue[0];
			
			max_core.migrateThread(migrant, min_core);
			max_chip.balanceLoad();
			max_chip.updateLoad();
			min_chip.balanceLoad();
			min_chip.updateLoad();
			
			min_chip = get_min_load_chip();
			max_chip = getMaxLoadChip();
		}
	} 
}
