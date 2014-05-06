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

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import au.com.bytecode.opencsv.CSVReader;
import com.synar.akula.hardware.Datacenter;
import com.synar.akula.software.Thread;
import com.synar.akula.utils.Initializer;

public class Akula
{

	static FileAlterationMonitor	monitor;

	public static void main(String[] args)
	{

		// create the command line parser
		CommandLineParser parser = new BasicParser();

		Options options = createOptions();

		HelpFormatter formatter = new HelpFormatter();
		String header = "\nAkula simulator\n----------------\n\nCommand line parameters:";
		String footer = "\nSFU Synar Lab (c) 2014";

		if ((args.length < 1) || (args[0].equals("-h")) || (args[0].equals("--help")))
		{
			formatter.printHelp("akula", header, options, footer, true);
			System.exit(0);
		}
		else
		{
			File jobsFile;
			File outputFile;
			File scheduleFile = null;
			File intermediateFile;

			CSVReader jobsFileReader;
			List<String[]> jobs = null;


			int racksNumber = 40;
			int machinesNumber = 10;
			int domainsNumber = 2;
			int coresNumber = 4;

			try
			{
				// parse the command line arguments
				CommandLine line = parser.parse(options, args);

				{
					jobsFile = new File(line.getOptionValue('j'));
					try
					{
						jobsFile.getCanonicalPath();
					}
					catch (IOException e)
					{
						System.out.println("Path to jobs file isn't valid");
						System.exit(-1);
					}
					if (jobsFile.isDirectory())
					{
						System.out.println("Path to jobs file shouldn't be a directory");

						System.exit(-1);
					}
					if (!jobsFile.exists())
					{
						System.out.println("Jobs file doesn't exist");

						System.exit(-1);
					}
					else
					{
						jobsFileReader = new CSVReader(new FileReader(jobsFile), ',', '\"', 0);
						jobs = jobsFileReader.readAll();
					}

				}

				{
					outputFile = new File(line.getOptionValue('o'));
					try
					{
						outputFile.getCanonicalPath();
					}
					catch (IOException e)
					{
						System.out.println("Path to the output file isn't valid");
						System.exit(-1);
					}
					if (outputFile.isDirectory())
					{
						System.out.println("Path for output file shouldn't be a directory");

						System.exit(-1);
					}
				}

				{
					scheduleFile = new File(line.getOptionValue('s'));
					try
					{
						scheduleFile.getCanonicalPath();
					}
					catch (IOException e)
					{
						System.out.println("Path to the schedule file isn't valid");
						System.exit(-1);
					}
					if (scheduleFile.isDirectory())
					{
						System.out.println("Path for schedule file shouldn't be a directory");

						System.exit(-1);
					}
				}

				{
					intermediateFile = new File(line.getOptionValue('i'));
					try
					{
						intermediateFile.getCanonicalPath();
					}
					catch (IOException e)
					{
						System.out.println("Path to the intermediate file isn't valid");
						System.exit(-1);
					}
					if (outputFile.isDirectory())
					{
						System.out.println("Path for intermediate file shouldn't be a directory");

						System.exit(-1);
					}
				}

				String s = "";
				// validate that block-size has been set
				if (line.hasOption('h'))
				{
					// automatically generate the help statement
					formatter.printHelp("akula", header, options, footer, true);
					System.exit(0);
				}


				if (line.hasOption('r'))
				{
					try
					{
						s = line.getOptionValue('r');
						racksNumber = Integer.parseInt(s);
					}
					catch (NumberFormatException e)
					{
						throw new ParseException("Not an integer value " + s);
					}
				}

				if (line.hasOption('m'))
				{
					try
					{
						s = line.getOptionValue('m');
						machinesNumber = Integer.parseInt(s);
					}
					catch (NumberFormatException e)
					{
						throw new ParseException("Not an integer value " + s);
					}
				}


				if (line.hasOption('d'))
				{
					try
					{
						s = line.getOptionValue('d');
						domainsNumber = Integer.parseInt(s);
					}
					catch (NumberFormatException e)
					{
						throw new ParseException("Not an integer value " + s);
					}
				}

				if (line.hasOption('c'))
				{
					try
					{
						s = line.getOptionValue('c');
						coresNumber = Integer.parseInt(s);
					}
					catch (NumberFormatException e)
					{
						throw new ParseException("Not an integer value " + s);
					}
				}
			}
			catch (ParseException exp)
			{
				System.out.println("Wrong parameters:" + exp.getMessage());
				formatter.printHelp("akula", header, options, footer, true);
				System.exit(-1);
			}
			catch (FileNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (IOException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}


			System.out.println("Starting simulation");

			/*
			 * 	Initialize everything needed for a bootstrap simulation.
			 */

			// Try writing it back out as CSV to the console

			ArrayList<String> jobsNames = new ArrayList<String>();

			ArrayList<Long> mapInputBytes = new ArrayList<Long>();
			ArrayList<Long> shuffleBytes = new ArrayList<Long>();
			ArrayList<Long> reduceOutputBytes = new ArrayList<Long>();

			ArrayList<Integer> mappersNumber = new ArrayList<Integer>();
			ArrayList<Integer> containerMappersNumber = new ArrayList<Integer>();
			ArrayList<Integer> reducersNumber = new ArrayList<Integer>();
			ArrayList<Integer> containerReducersNumber = new ArrayList<Integer>();
			ArrayList<Integer> containersPerJobNumber = new ArrayList<Integer>();

			ArrayList<Float> shuffleMegabytes = new ArrayList<Float>();
			ArrayList<Integer> commClass = new ArrayList<Integer>();

			ArrayList<Float> commWiseDegradation = new ArrayList<Float>();
			ArrayList<Integer> volumesNumber = new ArrayList<Integer>();
			ArrayList<Integer> contentionClass = new ArrayList<Integer>();

			ArrayList<Float> contentionDegradation = new ArrayList<Float>();
			ArrayList<Float> containerPowerConsumption = new ArrayList<Float>();

			for (int i = 0; i < jobs.size(); i++)
			{
				String[] job = jobs.get(i);

				jobsNames.add(job[0]);

				mapInputBytes.add(Long.parseLong(job[1]));
				shuffleBytes.add(Long.parseLong(job[2]));
				reduceOutputBytes.add(Long.parseLong(job[3]));

				mappersNumber.add(Integer.parseInt(job[4]));
				containerMappersNumber.add(Integer.parseInt(job[5]));
				reducersNumber.add(Integer.parseInt(job[6]));
				containerReducersNumber.add(Integer.parseInt(job[7]));
				containersPerJobNumber.add(Integer.parseInt(job[8]));

				shuffleMegabytes.add(Float.parseFloat(job[9]));
				commClass.add(Integer.parseInt(job[10]));

				commWiseDegradation.add(Float.parseFloat(job[11]));
				volumesNumber.add(Integer.parseInt(job[12]));
				contentionClass.add(Integer.parseInt(job[13]));

				contentionDegradation.add(Float.parseFloat(job[14]));
				containerPowerConsumption.add(Float.parseFloat(job[15]));

			}

			/*StringWriter sw = new StringWriter();
			CSVWriter writer = new CSVWriter(sw);
			writer.writeAll(jobs);
			
			System.out.println("\n\nGenerated CSV File:\n\n");
			System.out.println(sw.toString());*/

			// Read in the the bootstrap data from files and create the lookup tables. 
			Initializer init = new Initializer();
			BootstrapDb degrad_matrix = init.getDegradMatrix();
			BootstrapDb solo_matrix = init.getSoloMatrix();

			// Initialize a data center with 40 racks with 10 machines each, with 2 memory-domains of 4-cores each.
			Datacenter datacenter = new Datacenter(racksNumber, machinesNumber, domainsNumber, coresNumber);

			// Create the initial workload. (4 devil threads MCF and 4 turtle threads gamess).
			// To make this more versatile it is best to create workload files with thread data 
			// and a parser which creates workloads by reading the files. 

			Thread workload[] = new Thread[8];
			for (int i = 0; i < 4; i++)
			{
				Thread new_thread = new Thread();
				new_thread.mThreadName = "MCF" + i;
				new_thread.mBenchmarkId = 3; //The benchmark id of MCF see initializer.java
				new_thread.mStartTime = i;
				workload[i] = new_thread;
			}
			for (int i = 4; i < 8; i++)
			{
				Thread new_thread = new Thread();
				new_thread.mThreadName = "GAMESS" + (i - 4);
				new_thread.mBenchmarkId = 8; //The benchmark id of GAMESS see initializer.java
				new_thread.mStartTime = i;
				workload[i] = new_thread;
			}

			// The monitor will perform polling on the folder every 5 seconds
			final long pollingInterval = 1 * 1000;

			File folder = new File(scheduleFile.getParent());

			if (!folder.exists())
			{
				// Test to see if monitored folder exists
				throw new RuntimeException("Directory not found: " + scheduleFile.getParent());
			}

			FileAlterationObserver observer = new FileAlterationObserver(scheduleFile.getParent());

			monitor = new FileAlterationMonitor(pollingInterval);
			FileAlterationListener listener = new FileAlterationListenerAdaptor()
			{
				// Is triggered when a file is created in the monitored folder
				@Override
				public void onFileCreate(File file)
				{
					try
					{
						// "file" is the reference to the newly created file
						System.out.println("File created: "
								+ file.getCanonicalPath());
					}
					catch (IOException e)
					{
						e.printStackTrace(System.err);
					}
				}

				// Is triggered when a file is deleted from the monitored folder
				@Override
				public void onFileDelete(File file)
				{
					try
					{
						// "file" is the reference to the removed file
						System.out.println("File removed: "
								+ file.getCanonicalPath());
						// "file" does not exists anymore in the location
						System.out.println("File still exists in location: "
								+ file.exists());
						if (file.getName().equals("schedule.txt"))
						{
							try
							{
								System.out.println("Exiting: "
										+ file.getCanonicalPath());
								monitor.stop();
							}
							catch (Exception e)
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					catch (IOException e)
					{
						e.printStackTrace(System.err);
					}
				}

				// Is triggered when a file is deleted from the monitored folder
				@Override
				public void onFileChange(File file)
				{
					try
					{
						// "file" is the reference to the removed file
						System.out.println("File changed: "
								+ file.getCanonicalPath());
					}
					catch (IOException e)
					{
						e.printStackTrace(System.err);
					}
				}
			};

			observer.addListener(listener);
			monitor.addObserver(observer);
			try
			{
				monitor.start();
			}
			catch (Exception e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			/*
			 * Launch the bootstrap module.
			 */
			double tick = 1.00;
			Bootstrap mod = new Bootstrap(datacenter, workload, tick, degrad_matrix, solo_matrix);
			mod.run_experiment();

			/*
			 * Collect the completed threads and obtain their statistics.
			 */
			Stats s = new Stats();
			s.mSolos = solo_matrix;
			s.dumpStats("my_results.txt", workload);

			System.out.println("done");
		}

	}

	private static Options createOptions()
	{
		// create the Options
		Options options = new Options();
		//create jobsFileOption
		Option jobsFileOption = OptionBuilder.create('j');
		jobsFileOption.setLongOpt("jobs");
		jobsFileOption.setDescription("Path to file containing jobs");
		jobsFileOption.setRequired(true);
		jobsFileOption.setOptionalArg(false);
		jobsFileOption.setArgs(1);
		jobsFileOption.setArgName("jobs_file");

		//create outputFileOption
		Option outputFileOption = OptionBuilder.create('o');
		outputFileOption.setLongOpt("output");
		outputFileOption.setDescription("Path for file, where results will be stored");
		outputFileOption.setRequired(true);
		outputFileOption.setOptionalArg(false);
		outputFileOption.setArgs(1);
		outputFileOption.setArgName("output_file");

		//create scheduleFileOption
		Option scheduleFileOption = OptionBuilder.create('s');
		scheduleFileOption.setLongOpt("schedule");
		scheduleFileOption.setDescription("Path to file, from which new schedule will be read");
		scheduleFileOption.setRequired(true);
		scheduleFileOption.setOptionalArg(false);
		scheduleFileOption.setArgs(1);
		scheduleFileOption.setArgName("schedule_file");

		//create intermediateFileOption
		Option intermediateFileOption = OptionBuilder.create('i');
		intermediateFileOption.setLongOpt("intermediate");
		intermediateFileOption.setDescription("Path to file, where intermediate state will be printed");
		intermediateFileOption.setRequired(true);
		intermediateFileOption.setOptionalArg(false);
		intermediateFileOption.setArgs(1);
		intermediateFileOption.setArgName("intermediate_file");

		//create racksOption
		Option racksOption = OptionBuilder.create('r');
		racksOption.setLongOpt("racks");
		racksOption.setDescription("Number of racks in the datacenter. Default is 40");
		racksOption.setRequired(false);
		racksOption.setOptionalArg(false);
		racksOption.setArgs(1);
		racksOption.setArgName("number_racks");
		racksOption.setType(Integer.class);

		//create machinesOption
		Option machinesOption = OptionBuilder.create('m');
		machinesOption.setLongOpt("machines");
		machinesOption.setDescription("Number of machines in each rack. Default is 10");
		machinesOption.setRequired(false);
		machinesOption.setOptionalArg(false);
		machinesOption.setArgs(1);
		machinesOption.setArgName("number_machines");
		machinesOption.setType(Integer.class);

		//create domainsOption
		Option domainsOption = OptionBuilder.create('d');
		domainsOption.setLongOpt("domains");
		domainsOption.setDescription("Number of memory domains (CPUs) in each machine. Default is 2");
		domainsOption.setRequired(false);
		domainsOption.setOptionalArg(false);
		domainsOption.setArgs(1);
		domainsOption.setArgName("number_domains");
		domainsOption.setType(Integer.class);

		//create coresOption
		Option coresOption = OptionBuilder.create('c');
		coresOption.setLongOpt("cores");
		coresOption.setDescription("Number of cores in each memory domain (CPU). Default is 4");
		coresOption.setRequired(false);
		coresOption.setOptionalArg(false);
		coresOption.setArgs(1);
		coresOption.setArgName("number_cores");
		coresOption.setType(Integer.class);
		
		//create pollingOption
		Option pollingOption = OptionBuilder.create('p');
		pollingOption.setLongOpt("polling");
		pollingOption.setDescription("Schedule polling interval in milliseconds. Default is 1000 ms");
		pollingOption.setRequired(false);
		pollingOption.setOptionalArg(false);
		pollingOption.setArgs(1);
		pollingOption.setArgName("ms");
		pollingOption.setType(Integer.class);

		//create helpOption
		Option helpOption = OptionBuilder.create('h');
		helpOption.setLongOpt("help");
		helpOption.setDescription("Print this help");
		helpOption.setRequired(false);

		options.addOption(jobsFileOption);
		options.addOption(outputFileOption);
		options.addOption(scheduleFileOption);
		options.addOption(intermediateFileOption);

		options.addOption(racksOption);
		options.addOption(machinesOption);
		options.addOption(domainsOption);
		options.addOption(coresOption);

		options.addOption(helpOption);
		return options;
	}
}
