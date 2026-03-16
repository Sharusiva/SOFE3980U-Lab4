package com.ontariotechu.sofe3980U;


import java.io.FileReader; 
import java.util.List;
import com.opencsv.*;

/**
 * Evaluate Single Variable Continuous Regression
 *
 */
public class App 
{
	private static final double EPSILON = 1e-15;

	private static double clipProbability(double probability) {
		if (probability < EPSILON) {
			return EPSILON;
		}
		if (probability > 1.0 - EPSILON) {
			return 1.0 - EPSILON;
		}
		return probability;
	}

	private static int argmax(double[] values) {
		int bestIndex = 0;
		double bestValue = values[0];
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
		System.out.println("Confusion Matrix (rows=true class, cols=predicted class):");
		System.out.print("\t");
		for (int col = 0; col < classes; col++) {
			System.out.print((col + 1) + "\t");
		}
		System.out.println();

		for (int row = 0; row < classes; row++) {
			System.out.print((row + 1) + "\t");
			for (int col = 0; col < classes; col++) {
				System.out.print(matrix[row][col] + "\t");
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
		double ceSum = 0.0;

		for (String[] row : allData) {
			int yTrue = Integer.parseInt(row[0]);
			double[] yPredicted = new double[numberOfClasses];
			for (int i = 0; i < numberOfClasses; i++) {
				yPredicted[i] = Double.parseDouble(row[i + 1]);
			}

			double probabilityOfTrueClass = clipProbability(yPredicted[yTrue - 1]);
			ceSum += -Math.log(probabilityOfTrueClass);

			int predictedClass = argmax(yPredicted) + 1;
			confusionMatrix[yTrue - 1][predictedClass - 1]++;
		}

		double ce = ceSum / allData.size();
		System.out.printf("Cross-Entropy (CE): %.6f%n", ce);
		printConfusionMatrix(confusionMatrix);

	}
	
}
