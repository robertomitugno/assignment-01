package pcd.ass01.performance;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class Coordinator {

    private final int totalThreads;
    private int finishedCount;

    private final Lock lock;
    private final Condition workersCondition;    // Workers wait on this condition
    private final Condition coordinatorCondition; // Coordinator waits on this condition

    public Coordinator(final int totalThreads) {
        this.totalThreads = totalThreads;
        this.finishedCount = 0;
        this.lock = new ReentrantLock();
        this.workersCondition = this.lock.newCondition();
        this.coordinatorCondition = this.lock.newCondition();
    }

    // Called by the worker threads when they finish their last work (updatePosition)
    public void workDoneWaitCoordinator() {
        this.lock.lock();
        try {
            this.finishedCount++;
            // If all threads have finished, signal the coordinator
            if (this.finishedCount == this.totalThreads) {
                this.coordinatorCondition.signalAll();
            }
            // Wait until the coordinator has processed the results and reset finishedCount
            while (this.finishedCount != 0) {
                try {
                    this.workersCondition.await();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        } finally {
            this.lock.unlock();
        }
    }

    // Before update the view, the coordinator must wait for all threads to finish
    public void waitWorkers() {
        this.lock.lock();
        try {
            // Wait until all worker threads have finished
            while (this.finishedCount < this.totalThreads) {
                try {
                    this.coordinatorCondition.await();
                } catch (final InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        } finally {
            this.lock.unlock();
        }
    }

    // Called when view is updated
    public void coordinatorDone() {
        this.lock.lock();
        try {
            this.finishedCount = 0;
            // Signal all waiting worker threads that they can continue
            this.workersCondition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    public void reset() {
        this.lock.lock();
        try {
            this.finishedCount = 0;
            // Notify all waiting threads to continue
            this.workersCondition.signalAll();
            this.coordinatorCondition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }
}