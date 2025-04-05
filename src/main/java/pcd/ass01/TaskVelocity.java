package pcd.ass01;

public class TaskVelocity implements Runnable {

    private Boid boid;
    private BoidsModel model;
    private Latch latch;

    public TaskVelocity(Boid boid, BoidsModel model, Latch latch) {
        this.boid = boid;
        this.model = model;
        this.latch = latch;
    }

    @Override
    public void run() {
        boid.updateVelocity(model);
        latch.countDown();
    }
}