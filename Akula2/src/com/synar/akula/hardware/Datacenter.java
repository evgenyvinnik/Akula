package com.synar.akula.hardware;

import com.synar.akula.software.Thread;

public class Datacenter
{
	public int number_racks;
	public int number_machines;
	public int number_mem_domains;
	public Rack Racks[];
	public int load;
	
	public Datacenter( int number_racks, int number_machines, int number_mem_domains, int number_cores )
	{
		load = 0;
		this.number_racks = number_racks;
		this.number_machines = number_machines;
		this.number_mem_domains = number_mem_domains;
		Racks = new Rack[number_racks];
		for(int i = 0; i < number_racks; i++)
		{
			Racks[i] = new Rack(number_machines, number_mem_domains, number_cores);
		}
	}
	
	public void update_load()
	{
		load = 0;
		for(int i = 0; i < number_racks; i++)
		{
			Racks[i].updateLoad();
			load += Racks[i].load;
		}
	}
	
	public Rack get_min_load_rack()
	{
		int min_load = 1 + number_racks*number_mem_domains*number_mem_domains*Core.MAX_THREADS_PER_CORE;
		Rack target = null;
		for(int i = 0; i < number_racks; i++)
		{
			if(Racks[i].load < min_load)
			{
				min_load = Racks[i].load;
				target = Racks[i];
			}
		}
		return target;
	}
	
	public Rack get_max_load_rack()
	{
		int max_load = -1;
		Rack target = null;
		for(int i = 0; i < number_racks; i++)
		{
			if(Racks[i].load > max_load)
			{
				max_load = Racks[i].load;
				target = Racks[i];
			}
		}
		return target;
	}
	
	public void balance_load()
	{
		Rack min_rack = get_min_load_rack();
		Rack max_rack = get_max_load_rack();
		
		while(min_rack.load < max_rack.load+1)
		{
			Core max_core = max_rack.getMaxLoadMachine().getMaxLoadChip().getMaxLoadCore();
			Core min_core = min_rack.getMinLoadMachine().get_min_load_chip().getMinLoadCore();
			Thread migrant = max_core.mRunQueue[0];
			
			max_core.migrateThreadRack(migrant, min_core);
			max_rack.balanceLoad();
			max_rack.updateLoad();
			min_rack.balanceLoad();
			min_rack.updateLoad();
			
			min_rack = get_min_load_rack();
			max_rack = get_max_load_rack();
		}
	} 
}
