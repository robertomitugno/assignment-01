package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConcurrentBoidsSimulator {

    private final BoidsModel model;
    private final int numThreads;
    private StateManager stateManager;
    private Optional<BoidsView> view;
    private List<BoidsWorker> workers;
    private Coordinator syncMonitor;
    private WorkerBarrier workerBarrier;

    private static final int FRAMERATE = 25;
    private int framerate;

    public ConcurrentBoidsSimulator(BoidsModel model, int numThreads) {
        this.model = model;
        this.numThreads = numThreads;
        this.view = Optional.empty();
        this.stateManager = new StateManager();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
        view.setSimulator(this);
    }

    public void runSimulation() {
        while (true) {
            waitUntilRunning();

            syncMonitor = new Coordinator(numThreads);
            workerBarrier = new WorkerBarrier(numThreads);
            workers = new ArrayList<>();

            createAndStartWorkers();

            while (stateManager.isRunning()) {
                if (!stateManager.waitIfPaused()) {
                    break;
                }

                long frameStartTime = System.currentTimeMillis();

                // Attendi che tutti i worker completino i loro calcoli
                syncMonitor.waitWorkers();

                // Aggiorna la view e gestisci il framerate
                updateViewAndManageFramerate(frameStartTime);

                // Notifica ai worker che possono procedere con il prossimo frame
                syncMonitor.coordinatorDone();
            }

            resetSimulation();
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

    private void updateViewAndManageFramerate(long t0) {
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