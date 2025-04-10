package pcd.ass01.jpf;

import java.util.ArrayList;
import java.util.List;
import pcd.ass01.Coordinator;
import pcd.ass01.WorkerBarrier;

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
        for(int k = 0; k < 3; k++) {
            syncMonitor.waitWorkers();

            syncMonitor.coordinatorDone();
        }
    }

    private void createAndStartWorkers() {
        List<Boid> boids = model.getBoids();
        int totalBoids = boids.size();
        int boidsPerThread = totalBoids / numThreads;
        int remainingBoids = totalBoids % numThreads;

        int startIndex = 0;
        for (int i = 0; i < numThreads; i++) {
            int boidsForThisThread = boidsPerThread + (i < remainingBoids ? 1 : 0);
            int endIndex = startIndex + boidsForThisThread;

            BoidsWorker worker = new BoidsWorker(model, boids.subList(startIndex, endIndex) , syncMonitor, workerBarrier);
            workers.add(worker);
            worker.start();

            startIndex = endIndex;
        }
    }
}