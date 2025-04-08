package pcd.ass01.performance;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConcurrentBoidsSimulator {

    private final BoidsModel model;
    private final int numThreads;
    private List<BoidsWorker> workers;
    private Coordinator syncMonitor;
    private WorkerBarrier workerBarrier;

    private int maxCycles;
    private int currentCycle = 0;
    private List<Integer> framerates = new ArrayList<>();

    private static final int DEFAULT_FRAMERATE = Integer.MAX_VALUE;
    private int framerate;

    public ConcurrentBoidsSimulator(BoidsModel model, int numThreads, int maxCycles) {
        this.model = model;
        this.numThreads = numThreads;
        this.maxCycles = maxCycles;
    }

    public List<Integer> runSimulation() {
        framerates.clear();

        // Inizializza la simulazione
        syncMonitor = new Coordinator(numThreads);
        workerBarrier = new WorkerBarrier(numThreads);
        workers = new ArrayList<>();
        createAndStartWorkers();

        for(currentCycle = 0; currentCycle <= maxCycles; currentCycle++) {

            long t0 = System.nanoTime();

            syncMonitor.waitWorkers();

            long t1 = System.nanoTime();
            double elapsedTimeMs = (t1 - t0) / 1_000_000.0; // Conversione a millisecondi

            // Se valore troppo basso
            if (elapsedTimeMs < 0.001) {
                elapsedTimeMs = 0.001;
            }

            // Calcola il framerate effettivo
            framerate = (int)(1000.0 / elapsedTimeMs);

            var frameratePeriod = 1000.0 / DEFAULT_FRAMERATE;
            if (elapsedTimeMs < frameratePeriod) {
                try {
                    Thread.sleep((long)(frameratePeriod - elapsedTimeMs));
                } catch (Exception ex) {
                    Thread.currentThread().interrupt();
                }
            }

            framerates.add(framerate);


            syncMonitor.coordinatorDone();
        }

        return framerates;
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