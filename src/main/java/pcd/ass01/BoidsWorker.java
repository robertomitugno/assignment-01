package pcd.ass01;

import java.util.List;

public class BoidsWorker extends Thread {
    private final BoidsModel model;
    private final int startIndex;
    private final int endIndex;
    private final SyncWorkersMonitor syncMonitor;
    private final WorkerBarrier workerBarrier;
    private volatile boolean running = true;

    public BoidsWorker(BoidsModel model, int startIndex, int endIndex, SyncWorkersMonitor syncMonitor, WorkerBarrier workerBarrier) {
        this.model = model;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.syncMonitor = syncMonitor;
        this.workerBarrier = workerBarrier;
    }

    @Override
    public void run() {
        List<Boid> boids;

        while (running) {
            // Get synchronized copy of boids list
            boids = model.getBoids();

            // Barriera #1: Attendi che tutti i worker abbiano ottenuto la lista
            workerBarrier.await();

            // Update velocities of assigned boids
            for (int i = startIndex; i < endIndex && i < boids.size(); i++) {
                boids.get(i).updateVelocity(model);
            }

            // Barriera #2: Attendi che tutti i worker abbiano aggiornato le velocità
            workerBarrier.await();

            // Update positions of assigned boids
            for (int i = startIndex; i < endIndex && i < boids.size(); i++) {
                boids.get(i).updatePos(model);
            }

            // Signal that work is done and wait for coordinator to signal next cycle
            syncMonitor.workDoneWaitCoordinator();
        }
    }

    public void terminate() {
        running = false;
        interrupt();
    }
}