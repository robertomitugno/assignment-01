package pcd.ass01;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoidsSimulator {

    private BoidsModel model;
    private final int maxCycles;
    private final List<List<Number>> performanceData = new ArrayList<>();
    private static final int DEFAULT_FRAMERATE = Integer.MAX_VALUE;

    public BoidsSimulator(final BoidsModel model, int maxCycles ) {
        this.model = model;
        this.maxCycles = maxCycles;
    }

    public List<List<Number>> runSimulation() {
        for(int currentCycle = 0; currentCycle <= maxCycles; currentCycle++) {
            long t0 = System.nanoTime();
            var boids = model.getBoids();

            /*
             * Improved correctness: first update velocities...
             */
            for (Boid boid : boids) {
                boid.updateVelocity(model);
            }

            /*
             * ..then update positions
             */
            for (Boid boid : boids) {
                boid.updatePos(model);
            }

            long t1 = System.nanoTime();
            double elapsedTimeMs = (t1 - t0) / 1_000_000.0; // To milliseconds

            // If value too low
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
                    Thread.sleep((long) (frameratePeriod - elapsedTimeMs));
                } catch (Exception ex) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return performanceData;
    }
}