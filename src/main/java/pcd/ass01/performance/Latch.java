package pcd.ass01.performance;

public final class Latch {

    private int counter;

    public Latch(final int numTasks) {
        this.counter = numTasks;
    }

    public synchronized void countDown() {
        this.counter--;
        if (this.counter == 0) {
            this.notify();
        }
    }

    public synchronized void await() throws InterruptedException {
        while (this.counter > 0) {
            this.wait();
        }
    }

    public synchronized void reset(int numTasks) {
        this.counter = numTasks;
    }

}