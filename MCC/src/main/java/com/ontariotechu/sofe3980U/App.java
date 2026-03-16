package com.ontariotechu.sofe3980U;


import java.io.FileReader; 
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * Evaluate Single Variable Continuous Regression
 *
 */
public class App 
{
	private static final float EPSILON = 1e-15f;

	private static float clipProbability(float probability) {
		if (probability < EPSILON) {
			return EPSILON;
		}
		if (probability > 1.0f - EPSILON) {
			return 1.0f - EPSILON;
		}
		return probability;
	}

	private static int argmax(float[] values) {
		int bestIndex = 0;
		float bestValue = values[0];
		for (int i = 1; i < values.length; i++) {
			if (values[i] > bestValue) {
				bestValue = values[i];
				bestIndex = i;
			}
		}
		return bestIndex;
	}

	private static void printConfusionMatrix(int[][] matrix) {
		int classes = matrix.length;
		System.out.println("Confusion matrix");
		System.out.print("\t\t");
		for (int col = 0; col < classes; col++) {
			System.out.print("y=" + (col + 1) + "\t");
		}
		System.out.println();

		for (int predicted = 0; predicted < classes; predicted++) {
			System.out.print("\ty^=" + (predicted + 1) + "\t");
			for (int trueClass = 0; trueClass < classes; trueClass++) {
				System.out.print(matrix[trueClass][predicted] + "\t");
			}
			System.out.println();
		}
	}

    public static void main( String[] args )
    {
		String filePath="model.csv";
		FileReader filereader;
		List<String[]> allData;
		try{
			filereader = new FileReader(filePath); 
			CSVReader csvReader = new CSVReaderBuilder(filereader).withSkipLines(1).build(); 
			allData = csvReader.readAll();
		}
		catch(Exception e){
			System.out.println( "Error reading the CSV file" );
			return;
		}

		if (allData.isEmpty()) {
			System.out.println("No data found in model.csv");
			return;
		}

		int numberOfClasses = allData.get(0).length - 1;
		int[][] confusionMatrix = new int[numberOfClasses][numberOfClasses];
		float ceSum = 0.0f;

		for (String[] row : allData) {
			int yTrue = Integer.parseInt(row[0]);
			float[] yPredicted = new float[numberOfClasses];
			for (int i = 0; i < numberOfClasses; i++) {
				yPredicted[i] = Float.parseFloat(row[i + 1]);
			}

			float probabilityOfTrueClass = clipProbability(yPredicted[yTrue - 1]);
			ceSum += -(float) Math.log(probabilityOfTrueClass);

			int predictedClass = argmax(yPredicted) + 1;
			confusionMatrix[yTrue - 1][predictedClass - 1]++;
		}

		float ce = ceSum / allData.size();
		System.out.println("CE =" + ce);
		printConfusionMatrix(confusionMatrix);

	}
	
}
