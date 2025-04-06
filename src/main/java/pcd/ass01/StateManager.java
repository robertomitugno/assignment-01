package pcd.ass01;

public class StateManager {

    private boolean running;
    private boolean paused;
    private final Object pauseLock = new Object();

    public StateManager() {
        this.running = false;
        this.paused = false;
    }

    public boolean start() {
        if (running) return false;

        running = true;
        paused = false;
        return true;
    }

    public void stop() {
        synchronized (pauseLock) {
            running = false;
            paused = false;
            pauseLock.notifyAll();
        }
    }

    public void pause() {
        synchronized (pauseLock) {
            paused = true;
        }
    }

    public void resume() {
        synchronized (pauseLock) {
            paused = false;
            pauseLock.notifyAll();
        }
    }

    public boolean isRunning() {
        return running;
    }

    public boolean waitIfPaused() {
        synchronized (pauseLock) {
            while (paused && running) {     //If pause == true, but simulation is still running
                try {
                    pauseLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return running;
        }
    }
}