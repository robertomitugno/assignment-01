package pcd.ass01.performance;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public final class WorkerBarrier {

    private final int totalWorkers;
    private int count;
    private int phase = 0;

    private final Lock lock;
    private final Condition barrierCondition;

    public WorkerBarrier(final int totalWorkers) {
        this.totalWorkers = totalWorkers;
        this.count = totalWorkers;
        this.lock = new ReentrantLock();
        this.barrierCondition = lock.newCondition();
    }

    public void await() {
        this.lock.lock();
        try {
            final var workerPhase = this.phase;
            this.count--;

            if (this.count == 0) {
                // Last thread to arrive, reset the barrier
                this.count = this.totalWorkers;
                this.phase++;
                // Signal all waiting threads
                this.barrierCondition.signalAll();
            } else {
                // Not the last thread, wait for others
                while (this.phase == workerPhase) {
                    try {
                        this.barrierCondition.await();
                    } catch (final InterruptedException ignored) {
                        Thread.currentThread().interrupt();
                        // Make sure we don't leave the barrier in an inconsistent state
                        if (this.count == 0) {
                            // If we were the last thread, need to reset
                            this.count = this.totalWorkers;
                            this.phase++;
                            this.barrierCondition.signalAll();
                        } else {
                            // If not the last thread, increment count since we're leaving
                            this.count++;
                        }
                        return;
                    }
                }
            }
        } finally {
            this.lock.unlock();
        }
    }

    public void reset() {
        this.lock.lock();
        try {
            this.count = this.totalWorkers;
            this.phase++;
            this.barrierCondition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }
}