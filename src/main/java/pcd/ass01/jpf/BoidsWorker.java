package pcd.ass01.jpf;

import java.util.List;

public class BoidsWorker extends Thread {
    private final BoidsModel model;
    private final int startIndex;
    private final int endIndex;
    private final Coordinator syncMonitor;
    private final WorkerBarrier workerBarrier;
    //private volatile boolean running = true;

    public BoidsWorker(BoidsModel model, int startIndex, int endIndex, Coordinator syncMonitor, WorkerBarrier workerBarrier) {
        this.model = model;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.syncMonitor = syncMonitor;
        this.workerBarrier = workerBarrier;
    }

    @Override
    public void run() {
        List<Boid> boids;

        //while (running) {
        for(int k = 0; k < 5; k++){
            boids = model.getBoids();

            // Barrier #1: Wait for all workers to get the boids list
            workerBarrier.await();

            // Update velocities of assigned boids
            for (int i = startIndex; i < endIndex && i < boids.size(); i++) {
                boids.get(i).updateVelocity(model);
            }

            // Barrier #2: Wait for all workers to update velocities
            workerBarrier.await();

            // Update positions of assigned boids
            for (int i = startIndex; i < endIndex && i < boids.size(); i++) {
                boids.get(i).updatePos(model);
            }

            // Signal that work is done and wait for coordinator to signal next cycle
            syncMonitor.workDoneWaitCoordinator();

        }
    }

}