package pcd.ass01.performance;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class BoidsSimulation {

	final static double SEPARATION_WEIGHT = 1.0;
	final static double ALIGNMENT_WEIGHT = 1.0;
	final static double COHESION_WEIGHT = 1.0;

	final static int ENVIRONMENT_WIDTH = 1000;
	final static int ENVIRONMENT_HEIGHT = 1000;
	static final double MAX_SPEED = 4.0;
	static final double PERCEPTION_RADIUS = 50.0;
	static final double AVOID_RADIUS = 20.0;

	final static int SCREEN_WIDTH = 800;
	final static int SCREEN_HEIGHT = 800;

	final static String multithread_performance = "./src/main/java/pcd/ass01/performance/multithread_performance.csv";
	final static String multithread_avg_performance = "./src/main/java/pcd/ass01/performance/multithread_avg_performance.csv";

	public static void main(String[] args) {

		int availableCore = Runtime.getRuntime().availableProcessors() + 1;

		// Valori di thread da testare
		final List<Integer> nThreads = new ArrayList<>();
		Stream.of(2, 4, 8, 12, availableCore).forEach(n -> {
			if (n <= availableCore) {
				nThreads.add(n);
			}
		});

		// Definizione dei numeri di boid da testare
		final List<Integer> N_BOIDS = List.of(500, 1500, 2500, 5000, 7500, 10000);

		// Numero di cicli per ogni simulazione
		final int N_CYCLE = 50;

		// Variabile per raccogliere i risultati
		Map<Integer, Map<Integer, List<Integer>>> threadToBoidToFramerates = new HashMap<>();
		// Variabile per raccogliere le medie
		Map<Integer, Map<Integer, Double>> threadToBoidToAvgFramerates = new HashMap<>();

		System.out.println("Avvio test di performance:");

		for (Integer nThread : nThreads) {
			Map<Integer, List<Integer>> threadResults = new HashMap<>();
			threadToBoidToFramerates.put(nThread, threadResults);

			Map<Integer, Double> threadAvgResults = new HashMap<>();
			threadToBoidToAvgFramerates.put(nThread, threadAvgResults);

			for (Integer nBoid : N_BOIDS) {
				System.out.println("\nTest con " + nThread + " thread e " + nBoid + " boid in corso...");

				var model = new BoidsModel(
						nBoid,
						SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
						ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
						MAX_SPEED,
						PERCEPTION_RADIUS,
						AVOID_RADIUS);

				model.createBoids(nBoid);

				var sim = new ConcurrentBoidsSimulator(model, nThread, N_CYCLE);

				List<Integer> framerates = sim.runSimulation();
				System.out.println("Test completato con " + nBoid + " boid e " + nThread + " thread.");
				threadResults.put(nBoid, framerates);

				double avg = framerates.stream().mapToInt(Integer::intValue).average().orElse(0);
				threadAvgResults.put(nBoid, avg);

				System.out.printf("Completato: media fps %.2f\n", avg);

			}
		}

		try {
			saveDetailedResultsToCSV(threadToBoidToFramerates, multithread_performance);
		} catch (IOException e) {
			System.err.println("Errore nel salvare i dati dettagliati: " + e.getMessage());
		}

		try {
			saveAverageResultsToCSV(threadToBoidToAvgFramerates, N_BOIDS, nThreads, multithread_avg_performance);
		} catch (IOException e) {
			System.err.println("Errore nel salvare le medie: " + e.getMessage());
		}

		System.exit(0);
	}

	private static void saveDetailedResultsToCSV(Map<Integer, Map<Integer, List<Integer>>> results, String filePath) throws IOException {
		try (FileWriter writer = new FileWriter(filePath)) {
			writer.write("numThreads,numBoids,framerates...\n");

			// Per ogni configurazione di thread e boid...
			for (Map.Entry<Integer, Map<Integer, List<Integer>>> threadEntry : results.entrySet()) {
				int numThreads = threadEntry.getKey();
				Map<Integer, List<Integer>> boidResults = threadEntry.getValue();

				for (Map.Entry<Integer, List<Integer>> boidEntry : boidResults.entrySet()) {
					int numBoids = boidEntry.getKey();
					List<Integer> frameRates = boidEntry.getValue();

					writer.write(numThreads + "," + numBoids);

					for (int frameRate : frameRates) {
						writer.write("," + frameRate);
					}
					writer.write("\n");
				}
			}
		}
	}

	private static void saveAverageResultsToCSV(Map<Integer, Map<Integer, Double>> averages, List<Integer> nBoids, List<Integer> nThreads, String filePath) throws IOException {
		try (FileWriter writer = new FileWriter(filePath)) {
			writer.write("numBoids");

			for (Integer threads : nThreads) {
				writer.write(";" + threads + " threads");
			}
			writer.write("\n");

			// Per ogni configurazione di boids...
			for (Integer boids : nBoids) {
				writer.write(String.valueOf(boids));

				// Per ogni thread, scrivi il valore medio del framerate
				for (Integer threads : nThreads) {
					Double avg = averages.get(threads).get(boids);
					writer.write(String.format(";%.2f", avg));
				}
				writer.write("\n");
			}
		}
	}
}