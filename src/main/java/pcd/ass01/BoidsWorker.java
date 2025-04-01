package pcd.ass01;

import java.util.List;

public class BoidsWorker extends Thread {
    private final BoidsModel model;
    private final int startIndex;
    private final int endIndex;
    private final Coordinator coordinator;
    private volatile boolean running = true;

    public BoidsWorker(BoidsModel model, int startIndex, int endIndex, Coordinator coordinator) {
        this.model = model;
        this.startIndex = startIndex;
        this.endIndex = endIndex;
        this.coordinator = coordinator;
    }

    @Override
    public void run() {
        List<Boid> boids;

        while (running) {
            // Get synchronized copy of boids list
            boids = model.getBoids();

            coordinator.workerBarrier();

            for (int i = startIndex; i < endIndex && i < boids.size(); i++) {
                boids.get(i).updateVelocity(model);
            }

            coordinator.workerBarrier();

            for (int i = startIndex; i < endIndex && i < boids.size(); i++) {
                boids.get(i).updatePos(model);
            }

            // Signal that work is done and wait for coordinator to signal next cycle
            coordinator.workerDoneWaitCoordinator();
        }
    }

    public void terminate() {
        running = false;
        interrupt();
    }
}