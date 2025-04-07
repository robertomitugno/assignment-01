package pcd.ass01;

public final class StateManager {

    private final Object pauseLock = new Object();
    private boolean running;
    private boolean paused;

    public void start() {
        if (this.running) {
            return;
        }
        this.running = true;
        this.paused = false;
    }

    public void stop() {
        synchronized (this.pauseLock) {
            this.running = false;
            this.paused = false;
            this.pauseLock.notifyAll();
        }
    }

    public void pause() {
        synchronized (this.pauseLock) {
            this.paused = true;
        }
    }

    public void resume() {
        synchronized (this.pauseLock) {
            this.paused = false;
            this.pauseLock.notifyAll();
        }
    }

    public boolean isRunning() {
        return this.running;
    }

    public boolean waitIfPaused() {
        synchronized (this.pauseLock) {
            while (this.paused && this.running) {
                try {
                    this.pauseLock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return false;
                }
            }
            return this.running;
        }
    }
}