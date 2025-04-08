package pcd.ass01.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConcurrentBoidsSimulator {

    private final BoidsModel model;
    private List<BoidsWorker> workers;
    private Coordinator syncMonitor;
    private WorkerBarrier workerBarrier;

    private final int maxCycles;
    private final List<List<Number>> performanceData = new ArrayList<>();

    private static final int DEFAULT_FRAMERATE = Integer.MAX_VALUE;
    private final int numThreads = Runtime.getRuntime().availableProcessors() + 1;

    public ConcurrentBoidsSimulator(BoidsModel model, int maxCycles) {
        this.model = model;
        this.maxCycles = maxCycles;
    }

    public List<List<Number>> runSimulation() {
        for(int currentCycle = 0; currentCycle <= maxCycles; currentCycle++) {

            long t0 = System.nanoTime();

            syncMonitor = new Coordinator(numThreads);
            workerBarrier = new WorkerBarrier(numThreads);
            workers = new ArrayList<>();
            createAndStartWorkers();

            syncMonitor.waitWorkers();

            long t1 = System.nanoTime();
            double elapsedTimeMs = (t1 - t0) / 1_000_000.0; // To milliseconds

            // If value too low
            if (elapsedTimeMs < 0.001) {
                elapsedTimeMs = 0.001;
            }

            // Calculate the actual framerate
            int framerate = (int) (1000.0 / elapsedTimeMs);

            // Store both framerate and execution time in one list
            // index 0: framerate, index 1: execution time
            performanceData.add(Arrays.asList(framerate, elapsedTimeMs));

            var frameratePeriod = 1000.0 / DEFAULT_FRAMERATE;
            if (elapsedTimeMs < frameratePeriod) {
                try {
                    Thread.sleep((long)(frameratePeriod - elapsedTimeMs));
                } catch (Exception ex) {
                    Thread.currentThread().interrupt();
                }
            }

            syncMonitor.coordinatorDone();
        }

        return performanceData;
    }

    private void createAndStartWorkers() {
        int totalBoids = model.getBoids().size();
        int boidsPerThread = totalBoids / numThreads;
        int remainingBoids = totalBoids % numThreads;

        int startIndex = 0;
        for (int i = 0; i < numThreads; i++) {
            int boidsForThisThread = boidsPerThread + (i < remainingBoids ? 1 : 0);
            int endIndex = startIndex + boidsForThisThread;

            BoidsWorker worker = new BoidsWorker(model, startIndex, endIndex, syncMonitor, workerBarrier);
            workers.add(worker);
            worker.start();

            startIndex = endIndex;
        }
    }


}