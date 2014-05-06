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
package com.synar.akula.utils;
import java.util.*;
import java.io.*;

import com.synar.akula.BootstrapDb;

/*
 * This needs to be called to set up the experimental parameters.
 */
public class Initializer {

	/*
	 * This is only an EXAMPLE of how to set up the bootstrap_data bases.
	 * The real implementation will depend on the machine specifics as well
	 * as the benchmarks used.
	 */
	
	/*
	 * Other bootstrap data.
	 */
	public String[] mBenchmarkNames = {"blank","gcc","lbm","mcf","milc","povray","gamess","namd","gobmk","libquantum"};
	public int[] mLength = {0, 100, 1313, 1626, 1129, 453, 481, 1526, 355, 1522};
	
	//instructions per second
	public double[] mIpc = {0, 0.74, 0.52, 0.11, 0.55, 1.12, 1.12, 0.78, 0.87, 0.74};
	
	//misses per instruction
	public int[] mMpi = {0, 30, 287, 605, 220, 0, 0, 1, 5, 240};
	
	//misses per cycle
	public int[] mMpc = {0, 23, 149, 69, 121, 0, 0, 1, 4, 178};
	public String[] invocations = { "blank", 
									"./gcc_base.amd64-m64-gcc43-nn 200.i -o 200.s > 200.out 2>> 200.err",
									"./lbm_base.amd64-m64-gcc43-nn 3000 reference.dat 0 0 100_100_130_ldc.of > lbm.out 2>> lbm.err",
									"./mcf_base.amd64-m64-gcc43-nn inp.in > inp.out 2>> inp.err",
									"./milc_base.amd64-m64-gcc43-nn < su3imp.in > su3imp.out 2>> su3imp.err",
									"./povray_base.amd64-m64-gcc43-nn SPEC-benchmark-ref.ini > SPEC-benchmark-ref.stdout 2>> SPEC-benchmark-ref.stderr",
									"./gamess_base.amd64-m64-gcc43-nn < cytosine.2.config > cytosine.2.out 2>> cytosine.2.err",
									"./namd_base.amd64-m64-gcc43-nn --input namd.input --iterations 38 --output namd.out > namd.stdout 2>> namd.err",
									"./gobmk_base.amd64-m64-gcc43-nn --quiet --mode gtp < nngs.tst > nngs.out 2>> nngs.err",
									"./libquantum_base.amd64-m64-gcc43-nn 1397 8 > ref.out 2>> ref.err"
									};
	
	public BootstrapDb getSoloMatrix()
	{
		BootstrapDb DB = new BootstrapDb(10, 0);
		for(int i = 1; i < 10; i++)
		{
			DB.input_data(i, null, (double)mLength[i]);
		}
		return DB;
	}
	
	public BootstrapDb getIpcMatrix()
	{
		BootstrapDb DB = new BootstrapDb(10, 0);
		for(int i = 1; i < 10; i++)
		{
			DB.input_data(i, null, mIpc[i]);
		}
		return DB;
	}
	
	public BootstrapDb getMpiMatrix()
	{
		BootstrapDb DB = new BootstrapDb(10, 0);
		for(int i = 1; i < 10; i++)
		{
			DB.input_data(i, null, mMpi[i]);
		}
		return DB;
	}
	
	public BootstrapDb getDegradMatrix()
	{
		/*
		 * Put the bootstrap data into a 4D matrix by parsing several files.
		 */
		readLines();
		//Convert into database format.
		BootstrapDb DB = new BootstrapDb(10, 3);
		for(int benchmark = 0; benchmark < 10; benchmark++)
		{
			for(int i = 0; i < 10; i++)
			{
				for(int j = 0; j < 10; j++)
				{
					for(int k = 0; k < 10; k++)
					{
						int[] neighbors = {i, j, k};
						DB.input_data(benchmark, neighbors, mArray[benchmark][i][j][k]);
					}
				}
			}
		}
		return DB;
	}
	
	private String[] mNames = {"blank","gcc","lbm","mcf","milc","povray","gamess","namd","gobmk","libquantum","sphinx"};
	private double[][][][] mArray = new double[10][10][10][10]; 
	
	private void readLines(){
		clearMatrix();
		try{
			Scanner scan = new Scanner(new File("bootstrap_resultsI.txt"));
			String[] lines = new String[20];
			int count = 0;
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				if(line.startsWith("***")){
					parseBlock(lines, count);
					count = 0;
				}
				else{
					lines[count] = line;
					count++;
				}
			}
			scan.close();
			scan = new Scanner(new File("bootstrap_resultsII.txt"));
			count = 0;
			while(scan.hasNextLine()){
				String line = scan.nextLine();
				if(line.startsWith("***")){
					parseBlock(lines, count);
					count = 0;
				}
				else{
					lines[count] = line;
					count++;
				}
			}
			consolidateMatrix();
			convertMatrix();
			return;
			
		}
		catch(Exception e){
			System.out.println(e);
			System.exit(0);
		}
		return;
	}
	
	private void parseBlock(String[] lines, int counted){
		double[] ratios = new double[4];
		int[] IDs = new int[4];
		int step = 0;
		long cycles = 0;
		String cyclebuffer = "";
		String instbuffer = "";
		long inst = 0;
		int read = 0;
		for(int i = 0; i < counted; i++){
			Scanner lineScan = new Scanner(lines[i]);
			if(lineScan.hasNextLong()){
				long buffer = lineScan.nextLong();
				String event = lineScan.next();
				read++;
				if(event.compareTo("CPU_CLK_UNHALTED")==0){
					cycles = buffer;
					cyclebuffer = lineScan.next();
				}
				else if(event.compareTo("RETIRED_INSTRUCTIONS")==0){
					inst = buffer;
					instbuffer = lineScan.next();
				}
				else{
					System.out.println("Illegal entry: " + event);
				}
				//we have a complete read ready
				if(read == 2){
					read = 0;
					if(cyclebuffer.compareTo(instbuffer)!=0){
						System.out.println("adjacent lines do NOT match!!!" + cyclebuffer  + " " + instbuffer);
					}
					else{
						int ID = getBenchID(cyclebuffer);
						//Do not record anything that has sphinx in it
						if(ID == 10){
							return;
						}
						if(ID != 0){
							IDs[step] = ID;
							double Dinst = (double)inst;
							double Dcycle = (double)cycles;
							ratios[step] = Dinst / Dcycle;
							step++;
							
							if(Dinst < 100000000){
								System.out.println("Bad entry for: " + mNames[ID]);
							}
						}
					}
				}
			}
			else{
				//System.out.println(lines[i]);
			}
		}
		if(step == 1){
			//System.out.println("single");
			mArray[IDs[0]][0][0][0] = ratios[0];
		}
		else if(step == 2){
			//System.out.println("double");
			mArray[IDs[0]][IDs[1]][0][0] = ratios[0];
			mArray[IDs[1]][IDs[0]][0][0] = ratios[1];
		}
		else if(step == 3){
			//System.out.println("triple");
			mArray[IDs[0]][IDs[1]][IDs[2]][0] = ratios[0];
			mArray[IDs[1]][IDs[0]][IDs[2]][0] = ratios[1];
			mArray[IDs[2]][IDs[0]][IDs[1]][0] = ratios[2];
		}
		else if(step == 4){
			//System.out.println("quad");
			mArray[IDs[0]][IDs[1]][IDs[2]][IDs[3]] = ratios[0];
			mArray[IDs[1]][IDs[0]][IDs[2]][IDs[3]] = ratios[1];
			mArray[IDs[2]][IDs[0]][IDs[1]][IDs[3]] = ratios[2];
			mArray[IDs[3]][IDs[0]][IDs[1]][IDs[2]] = ratios[3];
		}
		else{
			//System.out.println("confused number of steps " + step);
		}
	}
	
	private int getBenchID(String line){
		for(int i = 1; i < mNames.length; i++){
			for(int j = 0; j < 4; j++){
				String match = "/" + mNames[i] + j + "/";
				if(line.contains(match)){
					return i;
				}
			}
		}
		return 0;
	}
	
	private void clearMatrix(){
		for(int i = 0; i < 10; i++){
			for(int j = 0; j < 10; j++){
				for(int k = 0; k < 10; k++){
					for(int w = 0; w < 10; w++){
						mArray[i][j][k][w] = 0;
					}
				}
			}
		}
	}
	
	private void consolidateMatrix(){
		for(int i = 1; i < 10; i++){
			for(int j = 0; j < 10; j++){
				for(int k = 0; k < 10; k++){
					for(int w = 0; w < 10; w++){
						if(mArray[i][j][k][w] == 0){
							double repeats[] = new double[5];
							repeats[0] = mArray[i][j][w][k];
							repeats[1] = mArray[i][k][j][w];
							repeats[2] = mArray[i][k][w][j];
							repeats[3] = mArray[i][w][j][k];
							repeats[4] = mArray[i][w][k][j];
							for(int t = 0; t < 5; t++){
								if(repeats[t] > 0){
									mArray[i][j][k][w] = repeats[t];
									break;
								}
							}
						}
					}
				}
			}
		}
	}
	
	private void convertMatrix(){
		for(int i = 1; i < 10; i++){
			double solo = mArray[i][0][0][0];
			for(int j = 0; j < 10; j++){
				for(int k = 0; k < 10; k++){
					for(int w = 0; w < 10; w++){
						mArray[i][j][k][w] = mArray[i][j][k][w] / solo;
						if(mArray[i][j][k][w] > 1.00){
							mArray[i][j][k][w] = 1.00;
						}
					}
				}
			}
		}
	}


}

