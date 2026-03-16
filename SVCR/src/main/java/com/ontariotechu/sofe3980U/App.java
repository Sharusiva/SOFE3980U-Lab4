package com.ontariotechu.sofe3980U;


import java.io.FileReader; 
import java.io.IOException;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * Evaluate Single Variable Continuous Regression
 *
 */
public class App 
{
	public static void main( String[] args )
	{
		String[] filePaths = {"model_1.csv", "model_2.csv", "model_3.csv"};
		double[] mse = new double[filePaths.length];
		double[] mae = new double[filePaths.length];
		double[] mare = new double[filePaths.length];

		for (int modelIndex = 0; modelIndex < filePaths.length; modelIndex++) {
			try {
				FileReader fileReader = new FileReader(filePaths[modelIndex]);
				CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();
				List<String[]> allData = csvReader.readAll();

				double mseSum = 0;
				double maeSum = 0;
				double mareSum = 0;
				int count = 0;

				for (String[] row : allData) {
					double yTrue = Double.parseDouble(row[0]);
					double yPredicted = Double.parseDouble(row[1]);
					double error = yTrue - yPredicted;

					mseSum += Math.pow(error, 2);
					maeSum += Math.abs(error);
					mareSum += (Math.abs(error) / (Math.abs(yTrue) + 1e-10)) * 100;
					count++;
				}

				mse[modelIndex] = mseSum / count;
				mae[modelIndex] = maeSum / count;
				mare[modelIndex] = mareSum / count;

				System.out.println("For Model " + (modelIndex + 1) + ":");
				System.out.println("MSE: " + mse[modelIndex]);
				System.out.println("MAE: " + mae[modelIndex]);
				System.out.println("MARE: " + mare[modelIndex]);
				System.out.println();
			}
			catch(IOException e){
				System.out.println("Error reading the CSV file: " + filePaths[modelIndex]);
				return;
			}
		}

		int bestMseModel = getBestModelIndex(mse);
		int bestMaeModel = getBestModelIndex(mae);
		int bestMareModel = getBestModelIndex(mare);

		System.out.println("according to MSE, the best model is: Model " + (bestMseModel + 1));
		System.out.println("according to MAE, the best model is: Model " + (bestMaeModel + 1));
		System.out.println("according to MARE, the best model is: Model " + (bestMareModel + 1));
    }

	private static int getBestModelIndex(double[] values) {
		int bestIndex = 0;
		for (int i = 1; i < values.length; i++) {
			if (values[i] < values[bestIndex]) {
				bestIndex = i;
			}
		}
		return bestIndex;
	}
}
