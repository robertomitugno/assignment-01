package pcd.ass01;

import java.util.Optional;
import java.util.concurrent.*;

public final class BoidsSimulator {

    private static final int FRAMERATE = 25;

    private final BoidsModel model;
    private final ExecutorService executor;
    private Optional<BoidsView> view = Optional.empty();
    private int framerate;

    public BoidsSimulator(final BoidsModel model, final int numThreads) {
        this.model = model;
        this.executor = Executors.newFixedThreadPool(numThreads);
    }

    public void runSimulation() {
        final var boids = model.getBoids();
        var latch = new Latch(boids.size());

        while (true) {
            final var t0 = System.currentTimeMillis();

            for (final var boid : boids) {
                final var updateVelocityTask = new UpdateBoidTask(boid, model, latch, UpdateBoidTask.Mode.VELOCITY);
                this.executor.execute(updateVelocityTask);
            }

            try {
                latch.await();
            } catch (InterruptedException ignored) {
            } finally {
                latch = new Latch(boids.size());
            }

            for (final var boid : boids) {
                final var updatePositionTask = new UpdateBoidTask(boid, model, latch, UpdateBoidTask.Mode.POSITION);
                executor.execute(updatePositionTask);
            }

            try {
                latch.await();
            } catch (InterruptedException ignored) {
            } finally {
                latch = new Latch(boids.size());
            }

            this.renderBoids(t0);
        }
    }

    public void attachView(final BoidsView view) {
        this.view = Optional.of(view);
    }

    private void renderBoids(final long t0) {
        if (view.isPresent()) {
            this.view.get().update(this.framerate);
            final var t1 = System.currentTimeMillis();
            final var dtElapsed = t1 - t0;
            final var frameratePeriod = 1000 / FRAMERATE;
            if (dtElapsed < frameratePeriod) {
                try {
                    Thread.sleep(frameratePeriod - dtElapsed);
                } catch (Exception ignored) {
                }
                this.framerate = FRAMERATE;
            } else {
                this.framerate = (int) (1000 / dtElapsed);
            }
        }
    }
}
