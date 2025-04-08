package pcd.ass01.performance;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BoidsSimulation {

	final static double SEPARATION_WEIGHT = 1.0;
	final static double ALIGNMENT_WEIGHT = 1.0;
	final static double COHESION_WEIGHT = 1.0;

	final static int ENVIRONMENT_WIDTH = 1000;
	final static int ENVIRONMENT_HEIGHT = 1000;
	static final double MAX_SPEED = 4.0;
	static final double PERCEPTION_RADIUS = 50.0;
	static final double AVOID_RADIUS = 20.0;

	final static String multithread_performance = "./src/main/java/pcd/ass01/performance/multithread_performance.csv";
	final static String multithread_avg_performance = "./src/main/java/pcd/ass01/performance/multithread_avg_performance.csv";

	public static void main(String[] args) {

		final List<Integer> N_BOIDS = List.of(500, 1500, 2500, 5000, 7500, 10000);

		// Number of cycles for each test
		final int N_CYCLE = 50;

		// Data for measurement - Map<numBoids, List<List<Number>>> where inner List is [framerate, execTime]
		Map<Integer, List<List<Number>>> boidToPerformanceData = new HashMap<>();

		// Data for average measurement - Map<numBoids, List<Number>> where List is [avgFramerate, avgExecTime]
		Map<Integer, List<Number>> boidToAvgPerformance = new HashMap<>();

		System.out.println("Starting test performance:");

		for (Integer nBoid : N_BOIDS) {
			System.out.println("\nTest with " + nBoid + " boids...");

			var model = new BoidsModel(
					nBoid,
					SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
					ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
					MAX_SPEED,
					PERCEPTION_RADIUS,
					AVOID_RADIUS);

			model.createBoids(nBoid);

			var sim = new ConcurrentBoidsSimulator(model, N_CYCLE);

			List<List<Number>> performanceData = sim.runSimulation();

			System.out.println("Test completed with " + nBoid + " boids.");
			boidToPerformanceData.put(nBoid, performanceData);

			// Calculate averages
			double avgFramerate = performanceData.stream()
					.mapToInt(list -> list.get(0).intValue())
					.average()
					.orElse(0);

			double avgExecutionTime = performanceData.stream()
					.mapToDouble(list -> list.get(1).doubleValue())
					.average()
					.orElse(0);

			// Store averages as [avgFramerate, avgExecTime]
			boidToAvgPerformance.put(nBoid, Arrays.asList(avgFramerate, avgExecutionTime));

			System.out.printf("Completed: average fps %.4f, average execution time %.4f ms\n",
					avgFramerate, avgExecutionTime);
		}

		try {
			saveDetailedResultsToCSV(boidToPerformanceData, N_BOIDS);
		} catch (IOException e) {
			System.err.println("Error on saving data: " + e.getMessage());
		}

		try {
			saveAverageResultsToCSV(boidToAvgPerformance, N_BOIDS);
		} catch (IOException e) {
			System.err.println("Error on saving average: " + e.getMessage());
		}

		System.exit(0);
	}

	private static void saveDetailedResultsToCSV(
			Map<Integer, List<List<Number>>> performanceData,
			List<Integer> boidOrder) throws IOException {

		try (FileWriter writer = new FileWriter(BoidsSimulation.multithread_performance)) {
			writer.write("numBoids;framerate;executionTime(ms)\n");

			// For each configuration of boids...
			for (Integer numBoids : boidOrder) {
				List<List<Number>> data = performanceData.get(numBoids);

				for (List<Number> entry : data) {
					writer.write(String.format("%d;%d;%.4f\n",
							numBoids, entry.get(0).intValue(), entry.get(1).doubleValue()));
				}
			}
		}
	}

	private static void saveAverageResultsToCSV(
			Map<Integer, List<Number>> avgPerformance,
			List<Integer> boidOrder) throws IOException {

		try (FileWriter writer = new FileWriter(BoidsSimulation.multithread_avg_performance)) {
			writer.write("numBoids;avgFramerate;avgExecutionTime(ms)\n");

			// For each configuration of boids...
			for (Integer boids : boidOrder) {
				List<Number> data = avgPerformance.get(boids);
				writer.write(String.format("%d;%.4f;%.4f\n",
						boids, data.get(0).doubleValue(), data.get(1).doubleValue()));
			}
		}
	}
}