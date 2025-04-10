package pcd.ass01.performance;

import java.util.List;
import pcd.ass01.Coordinator;
import pcd.ass01.WorkerBarrier;

public class BoidsWorker extends Thread {
    private final BoidsModel model;
    private final List<Boid> boids;
    private final Coordinator syncMonitor;
    private final WorkerBarrier workerBarrier;

    public BoidsWorker(BoidsModel model, List<Boid> boids, Coordinator syncMonitor, WorkerBarrier workerBarrier) {
        this.model = model;
        this.boids = boids;
        this.syncMonitor = syncMonitor;
        this.workerBarrier = workerBarrier;
    }

    @Override
    public void run() {
        while (!isInterrupted()) {
            // Update velocities of assigned boids
            for (int i = 0; i < boids.size(); i++) {
                boids.get(i).updateVelocity(model);
            }
            // Barrier #2: Wait for all workers to update velocities
            workerBarrier.await();
            // Update positions of assigned boids
            for (int i = 0; i < boids.size(); i++) {
                boids.get(i).updatePos(model);
            }
            // Signal that work is done and wait for coordinator to signal next cycle
            syncMonitor.workDoneWaitCoordinator();
        }
    }
}