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
 *  This class abstracts the memory domains within the machine.
 *  It contains cores. 
 */
public class Chip{

	public int mNumberCores;
	public Core mCoreArray[];
	public int mLoad;
	public int memDomainId;
	
	public Chip(int number_cores, int id)
	{
		this.memDomainId = id;
		this.mNumberCores = number_cores;
		mCoreArray = new Core[number_cores];
		for(int i = 0; i < number_cores; i++)
		{
			mCoreArray[i] = new Core();
			mCoreArray[i].mCoreId = i;
		}
		mLoad = 0;
	}
	
	public void updateLoad()
	{
		mLoad = 0;
		for(int i = 0; i < mNumberCores; i++)
		{
			mLoad += mCoreArray[i].mLoad;
		}
	}
	
	public Core getMinLoadCore()
	{
		int min_load = Core.MAX_THREADS_PER_CORE+1;
		Core target = null;
		
		for(int i = 0; i < mNumberCores; i++)
		{
			if(mCoreArray[i].mLoad < min_load)
			{
				min_load = mCoreArray[i].mLoad;
				target = mCoreArray[i];
			}	
		}
		
		return target;
	}
	
	public Core getMaxLoadCore()
	{
		int max_load = -1;
		Core target = null;
		
		for(int i = 0; i < mNumberCores; i++)
		{
			if(mCoreArray[i].mLoad > max_load)
			{
				max_load = mCoreArray[i].mLoad;
				target = mCoreArray[i];
			}	
		}
		
		return target;
	}
	
	public void balanceLoad()
	{
		Core min_core = getMinLoadCore();
		Core max_core = getMaxLoadCore();
		
		while(min_core.mLoad < max_core.mLoad+1)
		{
			Thread migrant = max_core.mRunQueue[0];
			max_core.migrateThread(migrant, min_core);
			min_core = getMinLoadCore();
			max_core = getMaxLoadCore();
		}
	}
}
