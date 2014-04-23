package com.synar.akula;

import java.io.*;
import java.util.Arrays;

import com.synar.akula.software.Thread;

/*
 * This module calculates statistics about threads sent to it.
 */
public class Stats
{

	/*
	 * Make sure to initialize this properly.
	 */
	public BootstrapDb	mSolos;

	public void dumpStats(String fileName, Thread[] list)
	{
		try
		{

			double[] degrads = new double[list.length];
			PrintWriter output = new PrintWriter(new FileWriter(fileName, true));
			output.println("Thread Name \t On Processor Time (s) \t % Degrad");
			double totalCompletion = -1;

			for (int i = 0; i < list.length; i++)
			{
				double run_time = list[i].mEndTime - list[i].mStartTime;
				double solo_time = mSolos.table_lookup(list[i].mBenchmarkId, null);
				double degrad = 100.00 * ((run_time - solo_time) / solo_time);
				String line = list[i].mThreadName + "\t" + run_time + "\t" + degrad;
				output.println(line);
				degrads[i] = degrad;
				if (run_time > totalCompletion)
				{
					totalCompletion = run_time;
				}
			}

			output.println("Total Length: " + totalCompletion);
			calculateStatistic(output, degrads);
			output.println(" ");
			output.println(" ");
			output.println(" ");
			output.close();
		}
		catch (Exception E)
		{
			System.out.println(E);
			System.exit(0);
		}
	}

	private void calculateStatistic(PrintWriter output, double[] degrads)
	{
		try
		{
			//Sort the degradations from largest to smallest
			Arrays.sort(degrads);
			double N = (double) degrads.length;
			//retrieve stats
			output.println("Max Degradation: " + degrads[0]);
			double sum = 0;
			for (int i = 0; i < degrads.length; i++)
			{
				sum += degrads[i];
			}
			sum /= N;
			output.println("Average Degradation: " + sum);

			double sumI = 0;
			for (int i = 0; i < (degrads.length) / 2; i++)
			{
				sumI += degrads[i];
			}
			sumI /= (N / 2.00);
			output.println("Top Half Degradation: " + sumI);

			//calculate unfairness
			double sumII = 0;
			for (int i = 0; i < degrads.length; i++)
			{
				sumII += (degrads[i] * degrads[i]);
			}
			sumII /= N;
			double sigma = Math.sqrt(sumII - sum);
			output.println("Unfairness: " + 100 * (sigma / sum));
		}
		catch (Exception E)
		{
			System.out.println(E);
			System.exit(0);
		}
	}

}
