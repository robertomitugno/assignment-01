package pcd.ass01.performance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import pcd.ass01.Latch;

public final class BoidsSimulator {

    private final BoidsModel model;
    private final ExecutorService executor;

    private final int maxCycles;
    private final List<List<Number>> performanceData = new ArrayList<>();
    private static final int DEFAULT_FRAMERATE = Integer.MAX_VALUE;
    int numThreads = Runtime.getRuntime().availableProcessors() + 1;

    public BoidsSimulator(final BoidsModel model, int maxCycles) {
        this.model = model;
        this.executor = Executors.newFixedThreadPool(numThreads);
        this.maxCycles = maxCycles;
    }

    public List<List<Number>> runSimulation() {
        final var boids = model.getBoids();
        for (int currentCycle = 0; currentCycle <= maxCycles; currentCycle++) {
            var latch = new Latch(boids.size());

            final long t0 = System.nanoTime();

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

            long t1 = System.nanoTime();
            double elapsedTimeMs = (t1 - t0) / 1_000_000.0; // To milliseconds

            // If the value is too low
            if (elapsedTimeMs < 0.001) {
                elapsedTimeMs = 0.001;
            }

            // Calculate the actual framerate
            int framerate = (int) (1000.0 / elapsedTimeMs);

            // Store both framerate and execution time in one list
            // index 0: framerate, index 1: execution time
            performanceData.add(Arrays.asList(framerate, elapsedTimeMs));

            var frameratePeriod = 1000.0 / DEFAULT_FRAMERATE;
            if (elapsedTimeMs < frameratePeriod) {
                try {
                    Thread.sleep((long)(frameratePeriod - elapsedTimeMs));
                } catch (Exception ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return performanceData;
    }

}



