/***********************************************************************************************************************
 *
 * Akula v2: A Toolset for Experimenting and Developing Thread Placement Algorithms on Multicore Systems
 * ==========================================
 *
 * Copyright (C) 2014 by Evgeny Vinnik and Sergey Blagodurov
 *
 ***********************************************************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 *
 **********************************************************************************************************************/
package com.synar.akula.hardware;

import com.synar.akula.software.Thread;

/*
 * This is the the object that contains machines
 */
public class Rack
{
	public int mNumberMachines;
	public int mNumberMemDomains;
	public Machine Machines[];
	public int load;
	
	public Rack(int numberMachines, int numberMemDomains, int numberCores )
	{
		load = 0;
		mNumberMachines = numberMachines;
		mNumberMemDomains = numberMemDomains;
		Machines = new Machine[numberMachines];
		for(int i = 0; i < numberMachines; i++)
		{
			Machines[i] = new Machine(numberMemDomains, numberCores);
		}
	}
	
	public void updateLoad()
	{
		load = 0;
		for(int i = 0; i < mNumberMachines; i++)
		{
			Machines[i].update_load();
			load += Machines[i].load;
		}
	}
	
	public Machine getMinLoadMachine()
	{
		int min_load = 1 + mNumberMemDomains*mNumberMemDomains*Core.MAX_THREADS_PER_CORE;
		Machine target = null;
		for(int i = 0; i < mNumberMachines; i++)
		{
			if(Machines[i].load < min_load)
			{
				min_load = Machines[i].load;
				target = Machines[i];
			}
		}
		return target;
	}
	
	public Machine getMaxLoadMachine()
	{
		int max_load = -1;
		Machine target = null;
		for(int i = 0; i < mNumberMachines; i++)
		{
			if(Machines[i].load > max_load)
			{
				max_load = Machines[i].load;
				target = Machines[i];
			}
		}
		return target;
	}
	
	public void balanceLoad()
	{
		Machine min_machine = getMinLoadMachine();
		Machine max_machine = getMaxLoadMachine();
		
		while(min_machine.load < max_machine.load+1)
		{
			Core max_core = max_machine.getMaxLoadChip().getMaxLoadCore();
			Core min_core = min_machine.get_min_load_chip().getMinLoadCore();
			Thread migrant = max_core.mRunQueue[0];
			
			max_core.migrateThreadMachine(migrant, min_core);
			max_machine.balanceLoad();
			max_machine.update_load();
			min_machine.balanceLoad();
			min_machine.update_load();
			
			min_machine = getMinLoadMachine();
			max_machine = getMaxLoadMachine();
		}
	} 
}
