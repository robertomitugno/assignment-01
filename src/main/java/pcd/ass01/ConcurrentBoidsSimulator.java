package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ConcurrentBoidsSimulator {

    private final BoidsModel model;
    private final int numThreads;
    private volatile boolean running;
    private Optional<BoidsView> view;
    private List<BoidsWorker> workers;
    private Coordinator coordinator;

    private static final int FRAMERATE = 25;
    private int framerate;

    public ConcurrentBoidsSimulator(BoidsModel model, int numThreads) {
        this.model = model;
        this.numThreads = numThreads;
        this.running = false;
        this.view = Optional.empty();
    }

    public void attachView(BoidsView view) {
        this.view = Optional.of(view);
    }

    public void runSimulation() {
        running = true;
        coordinator = new Coordinator(numThreads);
        workers = new ArrayList<>();

        int boidsForThread = model.getBoids().size() / numThreads;
        int remainingBoids = model.getBoids().size() % numThreads;

        int startIndex = 0;
        for (int i = 0; i < numThreads; i++) {
            int count = boidsForThread + (i < remainingBoids ? 1 : 0);
            int endIndex = startIndex + count;

            BoidsWorker worker = new BoidsWorker(model, startIndex, endIndex, coordinator);
            workers.add(worker);
            worker.start();

            startIndex = endIndex;
        }

        // Main simulation loop
        while (running) {
            // Wait for all worker threads to complete their updates
            coordinator.waitAllWorkers();

            var t0 = System.currentTimeMillis();

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

            // Signal worker threads to start the next iteration
            coordinator.coordinatorDone();
        }
    }

    public void stopSimulation() {
        running = false;
        for (BoidsWorker worker : workers) {
            worker.terminate();
            try {
                worker.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }
}