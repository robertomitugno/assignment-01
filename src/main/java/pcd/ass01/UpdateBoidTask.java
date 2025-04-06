package pcd.ass01;

public final class UpdateBoidTask implements Runnable {

    public enum Mode {
        VELOCITY,
        POSITION
    }

    private final Boid boid;
    private final BoidsModel model;
    private final Latch latch;
    private final Mode mode;

    public UpdateBoidTask(final Boid boid,
                          final BoidsModel model,
                          final Latch latch,
                          final Mode mode) {
        this.boid = boid;
        this.model = model;
        this.latch = latch;
        this.mode = mode;
    }

    @Override
    public void run() {
        if (this.mode == Mode.VELOCITY) {
            this.boid.updateVelocity(this.model);
        } else {
            this.boid.updatePos(this.model);
        }
        latch.countDown();
    }
}