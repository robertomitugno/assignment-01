package pcd.ass01.jpf;

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
        for(int k = 0; k < 3; k++){
            for (int i = 0; i < boids.size(); i++) {
                boids.get(i).updateVelocity(model);
            }
            workerBarrier.await();
            for (int i = 0; i < boids.size(); i++) {
                boids.get(i).updatePos(model);
            }
            syncMonitor.workDoneWaitCoordinator();

        }
    }
}