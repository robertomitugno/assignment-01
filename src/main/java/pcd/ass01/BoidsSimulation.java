package pcd.ass01;

public class BoidsSimulation {

	final static double SEPARATION_WEIGHT = 1.0;
    final static double ALIGNMENT_WEIGHT = 1.0;
    final static double COHESION_WEIGHT = 1.0;

    final static int ENVIRONMENT_WIDTH = 1000; 
	final static int ENVIRONMENT_HEIGHT = 1000;
    static final double MAX_SPEED = 4.0;
    static final double PERCEPTION_RADIUS = 50.0;
    static final double AVOID_RADIUS = 20.0;

	final static int SCREEN_WIDTH = 1200;
	final static int SCREEN_HEIGHT = 1200;

	final static int NUM_THREADS = Runtime.getRuntime().availableProcessors();

	public static void main(String[] args) {
		var model = new BoidsModel(
				SEPARATION_WEIGHT, ALIGNMENT_WEIGHT, COHESION_WEIGHT,
				ENVIRONMENT_WIDTH, ENVIRONMENT_HEIGHT,
				MAX_SPEED,
				PERCEPTION_RADIUS,
				AVOID_RADIUS);
		var sim = new ConcurrentBoidsSimulator(model, NUM_THREADS);
		var view = new BoidsView(model, SCREEN_WIDTH, SCREEN_HEIGHT);
		sim.attachView(view);
		sim.runSimulation();
	}
}
