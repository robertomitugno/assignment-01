package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConcurrentBoidsSimulator {

    private final BoidsModel model;
    private final int numThreads;
    private volatile boolean running;
    private volatile boolean paused;
    private final Object pauseLock = new Object();
    private Optional<BoidsView> view;
    private List<BoidsWorker> workers;
    private Coordinator syncMonitor;
    private WorkerBarrier workerBarrier;

    private static final int FRAMERATE = 25;
    private int framerate;

    public ConcurrentBoidsSimulator(BoidsModel model, int numThreads) {
        this.model = model;
        this.numThreads = numThreads;
        this.running = false;
        this.paused = false;
        this.view = Optional.empty();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
        view.setSimulator(this);
    }

    public void runSimulation() {
        if (running) return;  // Already running

        running = true;
        paused = false;
        syncMonitor = new Coordinator(numThreads);
        workerBarrier = new WorkerBarrier(numThreads);
        workers = new ArrayList<>();

        createAndStartWorkers();
        runMainSimulationLoop();

        // After stopping the simulation, ensure all workers are terminated
        syncMonitor.coordinatorDone();

    }

    public void stopSimulation() {
        running = false;

        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }

        if (workers != null) {
            for (BoidsWorker worker : workers) {
                worker.terminate();
                try {
                    worker.join();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            workers = null;
        }

        // Reset monitors and barriers
        if (syncMonitor != null) {
            syncMonitor.reset();
        }

        if (workerBarrier != null) {
            workerBarrier.reset();
        }

        model.setCohesionWeight(1.0);
        model.setSeparationWeight(1.0);
        model.setAlignmentWeight(1.0);

        model.resetBoids();
    }

    private void createAndStartWorkers() {
        int totalBoids = model.getBoids().size();
        int boidsPerThread = totalBoids / numThreads;
        int remainingBoids = totalBoids % numThreads;

        int startIndex = 0;
        for (int i = 0; i < numThreads; i++) {
            int boidsForThisThread = boidsPerThread + (i < remainingBoids ? 1 : 0);
            int endIndex = startIndex + boidsForThisThread;

            BoidsWorker worker = new BoidsWorker(model, startIndex, endIndex, syncMonitor, workerBarrier);
            workers.add(worker);
            worker.start();

            startIndex = endIndex;
        }
    }

    private void runMainSimulationLoop() {
        while (running) {
            synchronized (pauseLock) {
                while (paused && running) {
                    try {
                        pauseLock.wait();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                if (!running) {
                    break; // Exit if stopped during pause
                }
            }

            long frameStartTime = System.currentTimeMillis();

            syncMonitor.waitWorkers();

            updateViewAndManageFramerate(frameStartTime);

            syncMonitor.coordinatorDone();
        }
    }

    private void updateViewAndManageFramerate(long t0) {
        // Update view now that all position updates are complete
        if (view.isPresent()) {
            view.get().update(framerate);
            var t1 = System.currentTimeMillis();
            var dtElapsed = t1 - t0;
            var frameratePeriod = 1000/FRAMERATE;

            if (dtElapsed < frameratePeriod) {
                try {
                    Thread.sleep(frameratePeriod - dtElapsed);
                } catch (Exception ex) {
                    Thread.currentThread().interrupt();
                }
                framerate = FRAMERATE;
            } else {
                framerate = (int) (1000/dtElapsed);
            }
        }
    }


    public void pauseSimulation() {
        synchronized (pauseLock) {
            paused = true;
        }
    }

    public void resumeSimulation() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }
}