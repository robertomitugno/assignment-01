package pcd.ass01.jpf;

import java.util.ArrayList;
import java.util.List;

public class ConcurrentBoidsSimulator {

    private final BoidsModel model;
    private final int numThreads;
    private volatile boolean running;
    private List<BoidsWorker> workers;
    private Coordinator syncMonitor;
    private WorkerBarrier workerBarrier;

    public ConcurrentBoidsSimulator(BoidsModel model, int numThreads) {
        this.model = model;
        this.numThreads = numThreads;
        this.running = false;
    }

    public void runSimulation() {
        if (running) return;  // Already running

        running = true;
        syncMonitor = new Coordinator(numThreads);
        workerBarrier = new WorkerBarrier(numThreads);
        workers = new ArrayList<>();

        createAndStartWorkers();
        runMainSimulationLoop();

        // After stopping the simulation, ensure all workers are terminated
        syncMonitor.coordinatorDone();

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

    private void runMainSimulationLoop() {
        for(int k = 0; k<5; k++) {
            //long frameStartTime = System.currentTimeMillis();

            syncMonitor.waitWorkers();

            //updateViewAndManageFramerate(frameStartTime);

            syncMonitor.coordinatorDone();
        }
    }

}