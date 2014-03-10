import java.util.*;
import java.io.*;

/*
 * This needs to be called to set up the experimental parameters.
 */
public class initializer {

	/*
	 * This is only an EXAMPLE of how to set up the bootstrap_data bases.
	 * The real implementation will depend on the machine specifics as well
	 * as the benchmarks used.
	 */
	
	/*
	 * Other bootstrap data.
	 */
	public String[] benchmark_names = {"blank","gcc","lbm","mcf","milc","povray","gamess","namd","gobmk","libquantum"};
	public int[] length = {0, 100, 1313, 1626, 1129, 453, 481, 1526, 355, 1522};
	public double[] IPC = {0, 0.74, 0.52, 0.11, 0.55, 1.12, 1.12, 0.78, 0.87, 0.74};
	public int[] MPI = {0, 30, 287, 605, 220, 0, 0, 1, 5, 240};
	public int[] MPC = {0, 23, 149, 69, 121, 0, 0, 1, 4, 178};
	public String[] invocations = {"blank", 
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
	
	public bootstrap_DB get_degrad_matrix()
	{
		/*
		 * Put the bootstrap data into a 4D matrix by parsing several files.
		 */
		readLines();
		//Convert into database format.
		bootstrap_DB DB = new bootstrap_DB(10, 3);
		for(int benchmark = 0; benchmark < 10; benchmark++)
		{
			for(int i = 0; i < 10; i++)
			{
				for(int j = 0; j < 10; j++)
				{
					for(int k = 0; k < 10; k++)
					{
						int[] neighbors = {i, j, k};
						DB.input_data(benchmark, neighbors, array[benchmark][i][j][k]);
					}
				}
			}
		}
		return DB;
	}
	
	private String[] names = {"blank","gcc","lbm","mcf","milc","povray","gamess","namd","gobmk","libquantum","sphinx"};
	private double[][][][] array = new double[10][10][10][10]; 
	
	private void readLines(){
		clearMatrix();
		try{
			Scanner scan = new Scanner(new File("results.txt"));
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
			scan = new Scanner(new File("resultsII.txt"));
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
								System.out.println("Bad entry for: " + names[ID]);
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
			array[IDs[0]][0][0][0] = ratios[0];
		}
		else if(step == 2){
			//System.out.println("double");
			array[IDs[0]][IDs[1]][0][0] = ratios[0];
			array[IDs[1]][IDs[0]][0][0] = ratios[1];
		}
		else if(step == 3){
			//System.out.println("triple");
			array[IDs[0]][IDs[1]][IDs[2]][0] = ratios[0];
			array[IDs[1]][IDs[0]][IDs[2]][0] = ratios[1];
			array[IDs[2]][IDs[0]][IDs[1]][0] = ratios[2];
		}
		else if(step == 4){
			//System.out.println("quad");
			array[IDs[0]][IDs[1]][IDs[2]][IDs[3]] = ratios[0];
			array[IDs[1]][IDs[0]][IDs[2]][IDs[3]] = ratios[1];
			array[IDs[2]][IDs[0]][IDs[1]][IDs[3]] = ratios[2];
			array[IDs[3]][IDs[0]][IDs[1]][IDs[2]] = ratios[3];
		}
		else{
			//System.out.println("confused number of steps " + step);
		}
	}
	
	private int getBenchID(String line){
		for(int i = 1; i < names.length; i++){
			for(int j = 0; j < 4; j++){
				String match = "/" + names[i] + j + "/";
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
						array[i][j][k][w] = 0;
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
						if(array[i][j][k][w] == 0){
							double repeats[] = new double[5];
							repeats[0] = array[i][j][w][k];
							repeats[1] = array[i][k][j][w];
							repeats[2] = array[i][k][w][j];
							repeats[3] = array[i][w][j][k];
							repeats[4] = array[i][w][k][j];
							for(int t = 0; t < 5; t++){
								if(repeats[t] > 0){
									array[i][j][k][w] = repeats[t];
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
			double solo = array[i][0][0][0];
			for(int j = 0; j < 10; j++){
				for(int k = 0; k < 10; k++){
					for(int w = 0; w < 10; w++){
						array[i][j][k][w] = array[i][j][k][w] / solo;
						if(array[i][j][k][w] > 1.00){
							array[i][j][k][w] = 1.00;
						}
					}
				}
			}
		}
	}


}

