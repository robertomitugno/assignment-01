package pcd.ass01;

public class TaskPosition implements Runnable {

    private Boid boid;
    private BoidsModel model;
    private Latch latch;

    public TaskPosition(Boid boid, BoidsModel model, Latch latch) {
        this.boid = boid;
        this.model = model;
        this.latch = latch;
    }

    @Override
    public void run() {
        boid.updatePos(model);
        latch.countDown();
    }
}