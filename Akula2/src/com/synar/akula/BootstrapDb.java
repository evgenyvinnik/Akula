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

/*
 * An instance of the bootstrap data base keeps and serves one
 * info matrix.
 */
public class BootstrapDb
{
	private double[][] data_matrix;
	private int nb_entries;
	private int pointer_array[];
	
	public BootstrapDb(int nb_benchmarks, int nb_neighbors)
	{
		nb_entries = (int)Math.pow((double)nb_benchmarks, (double)nb_neighbors);
		data_matrix = new double[nb_benchmarks][nb_entries];
		
		if(nb_neighbors > 0)
		{
			pointer_array = new int[nb_neighbors];
			pointer_array[0] = 1;
			for(int i = 1; i < nb_neighbors; i++)
			{
				pointer_array[i] = pointer_array[i-1]*nb_benchmarks;
			}
		}
	}
	
	/*
	 * This is the function which is called by the bootstrap module
	 * requesting a lookup. 
	 */
	public double table_lookup(int target_id, int[] neighbor_ids)
	{
		//Translate the benchmark IDs into table IDS;
		int pointer = get_pointer(neighbor_ids);
		return data_matrix[target_id][pointer];
	}
	
	private int get_pointer(int[] neighbor_ids)
	{
		//Needed for one-dimension tables.
		if(neighbor_ids == null)
		{
			return 0;
		}
		
		//Calculates a pointer into a 1-d array based on
		int pointer = 0;
		for(int i = 0; i < neighbor_ids.length; i++)
		{
			pointer += neighbor_ids[i]*pointer_array[i];
		}
		return pointer;
	}
	
	//Read in a file to fill the table.
	public void input_data(int benchmark_id, int[] neighbors, double data)
	{
		int pointer = get_pointer(neighbors);
		data_matrix[benchmark_id][pointer] = data;
	}
}
