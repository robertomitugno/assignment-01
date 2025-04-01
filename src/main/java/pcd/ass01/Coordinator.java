package pcd.ass01;

/**
* Class that manages synchronization between worker threads and between worker and main thread.
* Combines barrier functionality for workers and coordination with the main thread.
 */
public class Coordinator {
    private final int totalThreads;
    private int count = 0;
    private int phaseCount = 0;
    private boolean mainWaiting = false;

    public Coordinator(int totalThreads) {
        this.totalThreads = totalThreads;
    }

    public synchronized void workerBarrier() {
        int myPhase = phaseCount;
        count++;

        if (count == totalThreads) {
            // Last worker arrived at the barrier
            count = 0;
            phaseCount++;
            notifyAll();
        } else {
            // Not the last worker, wait for others
            while (phaseCount == myPhase && !Thread.currentThread().isInterrupted()) {
                try {
                    wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    public synchronized void workerDoneWaitCoordinator() {
        try {
            count++;

            if (count == totalThreads) {
                // All workers are done
                if (mainWaiting) {
                    mainWaiting = false;
                    notifyAll();
                }
            }

            // Wait for the coordinator to signal the next cycle
            while (count != 0 && !Thread.currentThread().isInterrupted()) {
                wait();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void waitAllWorkers() {
        try {
            mainWaiting = true;

            // Wait for all workers to finish their updates
            while (count < totalThreads && !Thread.currentThread().isInterrupted()) {
                wait();
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    public synchronized void coordinatorDone() {
        count = 0;
        notifyAll();  // Notify all workers to start the next iteration
    }
}