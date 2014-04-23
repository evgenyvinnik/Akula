package com.synar.akula;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Akula
{
	/**
	 * @param args
	 */
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
			File scheduleFile;
			File intermediateFile;

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
					if(jobsFile.isDirectory())
					{
						System.out.println("Path to jobs file shouldn't be a directory");
						
						System.exit(-1);
					}
					if(!jobsFile.exists())
					{
						System.out.println("Jobs file doesn't exist");
						
						System.exit(-1);						
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
					if(outputFile.isDirectory())
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
					if(scheduleFile.isDirectory())
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
					if(outputFile.isDirectory())
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
				else if (line.hasOption('r'))
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
				else if (line.hasOption('m'))
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
				else if (line.hasOption('d'))
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
				else if (line.hasOption('c'))
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
		}

		System.out.println("Starting simulation");
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
