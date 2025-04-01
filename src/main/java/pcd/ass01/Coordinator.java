package pcd.ass01;

public class Coordinator {
    private final int totalThreads;
    private int finishedCount = 0;

    public Coordinator(int totalThreads) {
        this.totalThreads = totalThreads;
    }

    // Called by the worker threads when they finish their last work (updatePosition)
    public synchronized void workDoneWaitCoordinator() {
        try {
            finishedCount++;
            if (finishedCount == totalThreads) {
                notifyAll();
            }

            while (finishedCount != 0) {
                wait();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Before update the view, the coordinator must wait for all threads to finish
    public synchronized void waitWorkers() {
        try {
            while (finishedCount < totalThreads) {
                wait();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // Called when view is updated
    public synchronized void coordinatorDone() {
        finishedCount = 0;
        notifyAll();
    }

    public synchronized void reset() {
        finishedCount = 0;
        notifyAll();
    }
}