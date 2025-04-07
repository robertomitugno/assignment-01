package pcd.ass01;

public final class BoidsSimulation {

    private static final double SEPARATION_WEIGHT = 1.0;
    private static final double ALIGNMENT_WEIGHT = 1.0;
    private static final double COHESION_WEIGHT = 1.0;

    private static final int ENVIRONMENT_WIDTH = 1000;
    private static final int ENVIRONMENT_HEIGHT = 1000;
    private static final double MAX_SPEED = 4.0;
    private static final double PERCEPTION_RADIUS = 50.0;
    private static final double AVOID_RADIUS = 20.0;

    private static final int SCREEN_WIDTH = 800;
    private static final int SCREEN_HEIGHT = 800;

    private static final int NUM_THREADS = Runtime.getRuntime().availableProcessors() + 1;

    public static void main(final String... args) {
        final var model = new BoidsModel(
			SEPARATION_WEIGHT,
			ALIGNMENT_WEIGHT,
			COHESION_WEIGHT,
			ENVIRONMENT_WIDTH,
			ENVIRONMENT_HEIGHT,
			MAX_SPEED,
			PERCEPTION_RADIUS,
			AVOID_RADIUS
        );
        final var sim = new BoidsSimulator(model, NUM_THREADS);
        final var view = new BoidsView(model, SCREEN_WIDTH, SCREEN_HEIGHT);
        sim.attachView(view);
        sim.runSimulation();
    }
}
