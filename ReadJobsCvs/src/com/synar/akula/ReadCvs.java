package com.synar.akula;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;

public class ReadCvs
{

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		
		System.out.println(args[0]);
		System.out.println(args[1]);
		System.out.println(args[2]);
		
		String inputFileName = args[0];
		try
		{
		
			// Try writing it back out as CSV to the console
			CSVReader jobsFileReader = new CSVReader(new FileReader(inputFileName), ',', '\"', 0 );
			List<String[]> jobs = jobsFileReader.readAll();
			
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
			
			for(int i = 0; i< jobs.size(); i++)
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
			
			
			StringWriter sw = new StringWriter();
			CSVWriter writer = new CSVWriter(sw);
			writer.writeAll(jobs);
			
			System.out.println("\n\nGenerated CSV File:\n\n");
			System.out.println(sw.toString());
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
	}

}
