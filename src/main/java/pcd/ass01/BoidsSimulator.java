package pcd.ass01;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;

public class BoidsSimulator {

    private BoidsModel model;
    private Optional<BoidsView> view;
    
    private static final int FRAMERATE = 25;
    private int framerate;

    private ExecutorService executor;

    private Latch latch;

    public BoidsSimulator(BoidsModel model, int numThreads) {
        this.model = model;
        view = Optional.empty();
        executor = Executors.newFixedThreadPool(numThreads);
    }

    public void attachView(BoidsView view) {
    	this.view = Optional.of(view);
    }

    public void runSimulation() throws InterruptedException {
        var boids = model.getBoids();
        latch = new Latch(boids.size());

        while (true) {
            var t0 = System.currentTimeMillis();

            for(Boid boid: boids) {
                executor.execute(new TaskVelocity(boid, model, latch));
            }

            latch.await();
            latch.reset(boids.size());

            for(Boid boid: boids) {
                executor.execute(new TaskPosition(boid, model, latch));
            }

            latch.await();
            latch.reset(boids.size());

    		if (view.isPresent()) {
            	view.get().update(framerate);
            	var t1 = System.currentTimeMillis();
                var dtElapsed = t1 - t0;
                var framratePeriod = 1000/FRAMERATE;
                
                if (dtElapsed < framratePeriod) {		
                	try {
                		Thread.sleep(framratePeriod - dtElapsed);
                	} catch (Exception ex) {}
                	framerate = FRAMERATE;
                } else {
                	framerate = (int) (1000/dtElapsed);
                }
    		}

    	}
    }
}
