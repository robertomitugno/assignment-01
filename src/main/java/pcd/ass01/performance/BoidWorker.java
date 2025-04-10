package pcd.ass01.performance;

import pcd.ass01.Coordinator;
import pcd.ass01.WorkerBarrier;

public final class BoidWorker implements Runnable {

    private final BoidsModel model;
    private final Boid boid;
    private final Coordinator coordinator;
    private final WorkerBarrier barrier;

    public BoidWorker(final BoidsModel model,
                      final Boid boid,
                      final Coordinator coordinator,
                      final WorkerBarrier barrier) {
        this.model = model;
        this.boid = boid;
        this.coordinator = coordinator;
        this.barrier = barrier;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            this.boid.updateVelocity(this.model);
            this.barrier.await();
            this.boid.updatePos(this.model);
            this.coordinator.workDoneWaitCoordinator();
        }
    }
}
