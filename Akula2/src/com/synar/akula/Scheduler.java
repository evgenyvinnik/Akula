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
package com.synar.akula;

import com.synar.akula.hardware.Chip;
import com.synar.akula.hardware.Core;
import com.synar.akula.hardware.Datacenter;
import com.synar.akula.hardware.Machine;
import com.synar.akula.software.Thread;

/*
 * This is the user written scheduler which is invoked by the framework.
 */
public class Scheduler {

	/*
	 *  This is the naive_cluster implementation. Specifically for a 2-memory domain system.
	 */

	public void launchThread(Thread newThread, Datacenter datacenter)
	{
		//Put the thread on the busiest chip unless all cores are full in which case
		// put it on the other one.
		Chip target_chip = datacenter.get_max_load_chip();
		if(target_chip.load >= target_chip.number_cores)
		{
			target_chip = datacenter.get_min_load_chip();
		}
		target_chip.get_min_load_core().add_thread(newThread);
		target_chip.update_load();
	}
	
	public void updateSchedule(Datacenter datacenter)
	{
		//Check if the max_load_chip has room.
		Chip max_load_chip = datacenter.get_max_load_chip();
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

	/*
	 *  This is the naive_spread implementation. 
	 */
/*	public void launchThread(Thread new_thread, Datacenter datacenter)
	{
		//Put the thread on the busiest chip unless all cores are full in which case
		// put it on the other one.
		Chip target_chip = datacenter.get_min_load_chip();
		target_chip.getMinLoadCore().addThread(new_thread);
		target_chip.updateLoad();
	}
	
	public void updateSchedule(Datacenter datacenter)
	{
		//Check if there is a large enough difference to move threads.
		Chip min_load_chip = M.get_min_load_chip();
		Chip max_load_chip = M.getMaxLoadChip();
		
		if(min_load_chip.mLoad < (max_load_chip.mLoad-1))
		{
			Core remover_core = max_load_chip.getMaxLoadCore();
			Core adder_core = min_load_chip.getMinLoadCore();
			Thread migrant = remover_core.mRunQueue[0];
			remover_core.migrateThread(migrant, adder_core);
			min_load_chip.updateLoad();
			max_load_chip.updateLoad();
		}
	}*/
}
