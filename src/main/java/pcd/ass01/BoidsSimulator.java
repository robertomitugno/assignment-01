package pcd.ass01;

import java.util.Optional;
import java.util.concurrent.*;

public final class BoidsSimulator {

    private static final int FRAMERATE = 25;

    private final BoidsModel model;
    private StateManager stateManager;
    private ExecutorService executor;
    private Optional<BoidsView> view = Optional.empty();
    private int framerate;
    private final int numThreads;
    private Latch latch;

    public BoidsSimulator(final BoidsModel model, final int numThreads) {
        this.model = model;
        this.stateManager = new StateManager();
        this.numThreads = numThreads;
        this.executor = Executors.newFixedThreadPool(numThreads);
    }

    public void runSimulation() {
        final var boids = model.getBoids();
        while (true) {
            waitUntilRunning();
            latch = new Latch(boids.size());

            while (stateManager.isRunning()) {
                if (!stateManager.waitIfPaused()) {
                    break;
                }
                final var t0 = System.currentTimeMillis();

                for (final var boid : boids) {
                    final var updateVelocityTask = new UpdateBoidTask(boid, model, latch, UpdateBoidTask.Mode.VELOCITY);
                    this.executor.execute(updateVelocityTask);
                }

                try {
                    latch.await();
                } catch (InterruptedException ignored) {
                } finally {
                    latch.reset(boids.size());
                }

                for (final var boid : boids) {
                    final var updatePositionTask = new UpdateBoidTask(boid, model, latch, UpdateBoidTask.Mode.POSITION);
                    executor.execute(updatePositionTask);
                }

                try {
                    latch.await();
                } catch (InterruptedException ignored) {
                } finally {
                    latch.reset(boids.size());
                }

                this.renderBoids(t0);
            }
            resetSimulation();
        }
    }

    public void attachView(final BoidsView view) {
        this.view = Optional.of(view);
        view.setSimulator(this);
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

    private void waitUntilRunning() {
        synchronized (stateManager) {
            while (!stateManager.isRunning()) {
                try {
                    stateManager.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void resetSimulation() {
        if (this.executor != null && !this.executor.isShutdown()) {
            this.executor.shutdown();
            try {
                this.executor.awaitTermination(5, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        this.executor = Executors.newFixedThreadPool(numThreads);

        latch.cleanUp(latch.getCount());

        this.model.setAlignmentWeight(1.0);
        this.model.setCohesionWeight(1.0);
        this.model.setSeparationWeight(1.0);

        this.model.resetBoids();
    }

    public void startSimulation() {
        synchronized (stateManager) {
            stateManager.start();
            stateManager.notifyAll();
        }
    }

    public void stopSimulation() {
        stateManager.stop();
    }

    public void pauseSimulation() {
        stateManager.pause();
    }

    public void resumeSimulation() {
        stateManager.resume();
    }
}
