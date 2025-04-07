package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class BoidsSimulator {

    private static final int FRAMERATE = 25;

    private final BoidsModel model;
    private final StateManager stateManager = new StateManager();
    private final List<Thread> workers = new ArrayList<>();
    private Optional<BoidsView> view = Optional.empty();
    private int framerate;
    private Coordinator coordinator;
    private WorkerBarrier workerBarrier;

    public BoidsSimulator(final BoidsModel model) {
        this.model = model;
    }
      
    public void runSimulation() {
    	while (true) {
            this.waitUntilRunning();
            final var numWorkers = model.getBoids().size();
            this.coordinator = new Coordinator(numWorkers);
            this.workerBarrier = new WorkerBarrier(numWorkers);
            this.createAndStartWorkers();
            while (this.stateManager.isRunning()) {
                if (!this.stateManager.waitIfPaused()) {
                    break;
                }
                final var frameStartTime = System.currentTimeMillis();
                this.coordinator.waitWorkers();
                this.renderBoids(frameStartTime);
                this.coordinator.coordinatorDone();
            }
            this.resetSimulation();
    	}
    }

    public void startSimulation() {
        synchronized (this.stateManager) {
            this.stateManager.start();
            this.stateManager.notifyAll();
        }
    }

    public void stopSimulation() {
        this.stateManager.stop();
    }

    public void pauseSimulation() {
        this.stateManager.pause();
    }

    public void resumeSimulation() {
        this.stateManager.resume();
    }

    public void attachView(final BoidsView view) {
        this.view = Optional.of(view);
        view.setSimulator(this);
    }

    private void createAndStartWorkers() {
        final var boids = this.model.getBoids();
        for (final var boid : boids) {
            this.workers.add(
                Thread.startVirtualThread(new BoidWorker(this.model, boid, this.coordinator, this.workerBarrier))
            );
        }
    }

    private void waitUntilRunning() {
        synchronized (this.stateManager) {
            while (!this.stateManager.isRunning()) {
                try {
                    this.stateManager.wait();
                } catch (final InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    private void renderBoids(final long t0) {
        if (this.view.isPresent()) {
            this.view.get().update(this.framerate);
            final var t1 = System.currentTimeMillis();
            final var dtElapsed = t1 - t0;
            final var frameratePeriod = 1000 / FRAMERATE;
            if (dtElapsed < frameratePeriod) {
                try {
                    Thread.sleep(frameratePeriod - dtElapsed);
                } catch (final Exception ignored) {}
                this.framerate = FRAMERATE;
            } else {
                this.framerate = (int)(1000 / dtElapsed);
            }
        }
    }

    private void resetSimulation() {
        if (!this.workers.isEmpty()) {
            for (final var worker : this.workers) {
                worker.interrupt();
                try {
                    worker.join();
                } catch (final InterruptedException ignored) {
                    Thread.currentThread().interrupt();
                }
            }
            this.workers.clear();
        }
        this.coordinator.reset();
        this.workerBarrier.reset();
        this.model.setSeparationWeight(1.0);
        this.model.setCohesionWeight(1.0);
        this.model.setAlignmentWeight(1.0);
        this.model.resetBoids();
    }
}
