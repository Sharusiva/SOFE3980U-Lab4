package com.ontariotechu.sofe3980U;


import java.io.FileReader; 
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * Evaluate Single Variable Continuous Regression
 *
 */
public class App 
{
	private static final double THRESHOLD = 0.5;
	private static final double EPSILON = 1e-15;

	private static class Prediction {
		int trueLabel;
		double predicted;

		Prediction(int trueLabel, double predicted) {
			this.trueLabel = trueLabel;
			this.predicted = predicted;
		}
	}

	private static class EvaluationResult {
		String modelName;
		int tp;
		int tn;
		int fp;
		int fn;
		double bce;
		double accuracy;
		double precision;
		double recall;
		double f1;
		double aucRoc;

		EvaluationResult(String modelName) {
			this.modelName = modelName;
		}
	}

	private static List<Prediction> readPredictions(String filePath) throws Exception {
		FileReader fileReader = new FileReader(filePath);
		CSVReader csvReader = new CSVReaderBuilder(fileReader).withSkipLines(1).build();
		List<String[]> allData = csvReader.readAll();

		List<Prediction> predictions = new ArrayList<>();
		for (String[] row : allData) {
			int yTrue = Integer.parseInt(row[0]);
			double yPredicted = Double.parseDouble(row[1]);
			predictions.add(new Prediction(yTrue, yPredicted));
		}
		csvReader.close();
		fileReader.close();
		return predictions;
	}

	private static double clipProbability(double probability) {
		if (probability < EPSILON) {
			return EPSILON;
		}
		if (probability > 1.0 - EPSILON) {
			return 1.0 - EPSILON;
		}
		return probability;
	}

	private static double safeDivide(double numerator, double denominator) {
		if (denominator == 0.0) {
			return 0.0;
		}
		return numerator / denominator;
	}

	private static double computeAucRoc(List<Prediction> predictions) {
		int positives = 0;
		int negatives = 0;
		for (Prediction prediction : predictions) {
			if (prediction.trueLabel == 1) {
				positives++;
			} else {
				negatives++;
			}
		}

		if (positives == 0 || negatives == 0) {
			return 0.0;
		}

		List<Prediction> sorted = new ArrayList<>(predictions);
		sorted.sort(Comparator.comparingDouble((Prediction p) -> p.predicted).reversed());

		double tp = 0;
		double fp = 0;
		double prevTpr = 0;
		double prevFpr = 0;
		double auc = 0;

		for (Prediction prediction : sorted) {
			if (prediction.trueLabel == 1) {
				tp++;
			} else {
				fp++;
			}

			double tpr = tp / positives;
			double fpr = fp / negatives;
			auc += (fpr - prevFpr) * (tpr + prevTpr) / 2.0;
			prevTpr = tpr;
			prevFpr = fpr;
		}

		return auc;
	}

	private static EvaluationResult evaluateModel(String filePath, String modelName) throws Exception {
		List<Prediction> predictions = readPredictions(filePath);
		EvaluationResult result = new EvaluationResult(modelName);

		double bceSum = 0;
		for (Prediction prediction : predictions) {
			double p = clipProbability(prediction.predicted);
			bceSum += -(prediction.trueLabel * Math.log(p) + (1 - prediction.trueLabel) * Math.log(1 - p));

			int predictedLabel = prediction.predicted >= THRESHOLD ? 1 : 0;
			if (prediction.trueLabel == 1 && predictedLabel == 1) {
				result.tp++;
			} else if (prediction.trueLabel == 0 && predictedLabel == 0) {
				result.tn++;
			} else if (prediction.trueLabel == 0 && predictedLabel == 1) {
				result.fp++;
			} else {
				result.fn++;
			}
		}

		double total = predictions.size();
		result.bce = safeDivide(bceSum, total);
		result.accuracy = safeDivide(result.tp + result.tn, total);
		result.precision = safeDivide(result.tp, result.tp + result.fp);
		result.recall = safeDivide(result.tp, result.tp + result.fn);
		result.f1 = safeDivide(2.0 * result.precision * result.recall, result.precision + result.recall);
		result.aucRoc = computeAucRoc(predictions);

		return result;
	}

	private static void printResult(EvaluationResult result) {
		System.out.println("==============================");
		System.out.println("Model: " + result.modelName);
		System.out.printf("BCE: %.6f%n", result.bce);
		System.out.printf("Confusion Matrix (threshold=%.1f): TP=%d, TN=%d, FP=%d, FN=%d%n",
				THRESHOLD, result.tp, result.tn, result.fp, result.fn);
		System.out.printf("Accuracy: %.6f%n", result.accuracy);
		System.out.printf("Precision: %.6f%n", result.precision);
		System.out.printf("Recall: %.6f%n", result.recall);
		System.out.printf("F1 Score: %.6f%n", result.f1);
		System.out.printf("AUC-ROC: %.6f%n", result.aucRoc);
	}

	private static EvaluationResult selectBestModel(List<EvaluationResult> results) {
		return results.stream()
				.max(Comparator
						.comparingDouble((EvaluationResult r) -> r.aucRoc)
						.thenComparingDouble(r -> r.f1)
						.thenComparingDouble(r -> r.accuracy)
						.thenComparing(Comparator.comparingDouble((EvaluationResult r) -> r.bce).reversed()))
				.orElse(null);
	}

    public static void main( String[] args )
    {
		List<EvaluationResult> results = new ArrayList<>();
		try {
			results.add(evaluateModel("model_1.csv", "model_1.csv"));
			results.add(evaluateModel("model_2.csv", "model_2.csv"));
			results.add(evaluateModel("model_3.csv", "model_3.csv"));
		} catch (Exception e) {
			System.out.println("Error reading/evaluating CSV files: " + e.getMessage());
			return;
		}

		for (EvaluationResult result : results) {
			printResult(result);
		}

		EvaluationResult bestModel = selectBestModel(results);
		if (bestModel != null) {
			System.out.println("==============================");
			System.out.println("Best performing model: " + bestModel.modelName);
			System.out.printf("(Ranked by AUC-ROC, then F1, then Accuracy, then lower BCE)%n");
		}

	}
	
}
