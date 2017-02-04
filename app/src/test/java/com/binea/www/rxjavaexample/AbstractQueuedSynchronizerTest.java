package com.binea.www.rxjavaexample;

//  Created by xubinggui on 08/01/2017.
//                            _ooOoo_  
//                           o8888888o  
//                           88" . "88  
//                           (| -_- |)  
//                            O\ = /O  
//                        ____/`---'\____  
//                      .   ' \\| |// `.  
//                       / \\||| : |||// \  
//                     / _||||| -:- |||||- \  
//                       | | \\\ - /// | |  
//                     | \_| ''\---/'' | |  
//                      \ .-\__ `-` ___/-. /  
//                   ___`. .' /--.--\ `. . __  
//                ."" '< `.___\_<|>_/___.' >'"".  
//               | | : `- \`.;`\ _ /`;.`/ - ` : | |  
//                 \ \ `-. \_ __\ /__ _/ .-` / /  
//         ======`-.____`-.___\_____/___.-`____.-'======  
//                            `=---='  
//  
//         .............................................  
//                  佛祖镇楼                  BUG辟易 

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import junit.framework.AssertionFailedError;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class AbstractQueuedSynchronizerTest extends BaseTest {
    static class Mutex extends AbstractQueuedSynchronizer {
        /** An eccentric value for locked synchronizer state. */
        static final int LOCKED = (1 << 31) | (1 << 15);

        static final int UNLOCKED = 0;

        @Override protected boolean isHeldExclusively() {
            int state = getState();
            assertTrue(state == UNLOCKED || state == LOCKED);
            return state == LOCKED;
        }

        @Override protected boolean tryAcquire(int acquires) {
            assertEquals(LOCKED, acquires);
            boolean result = compareAndSetState(UNLOCKED, LOCKED);
            System.out.println("tryAcquire result " + result + ": " + Thread.currentThread());
            return result;
        }

        @Override protected boolean tryRelease(int releases) {
            if (getState() != LOCKED) {
                throw new IllegalMonitorStateException();
            }
            assertEquals(LOCKED, releases);
            setState(UNLOCKED);
            return true;
        }

        public boolean tryAcquireNanos(long nanos) throws InterruptedException {
            return tryAcquireNanos(LOCKED, nanos);
        }

        public boolean tryAcquire() {
            return tryAcquire(LOCKED);
        }

        public boolean tryRelease() {
            return tryRelease(LOCKED);
        }

        public void acquire() {
            acquire(LOCKED);
        }

        public void acquireInterruptibly() throws InterruptedException {
            acquireInterruptibly(LOCKED);
        }

        public void release() {
            release(LOCKED);
        }

        public ConditionObject newCondition() {
            return new ConditionObject();
        }
    }

    /**
     * A simple latch class, to test shared mode.
     */
    static class BooleanLatch extends AbstractQueuedSynchronizer {
        public boolean isSignalled() {
            return getState() != 0;
        }

        public int tryAcquireShared(int ignore) {
            return isSignalled() ? 1 : -1;
        }

        public boolean tryReleaseShared(int ignore) {
            setState(1);
            return true;
        }
    }

    /**
     * A runnable calling acquireInterruptibly that does not expect to
     * be interrupted.
     */
    class InterruptibleSyncRunnable extends CheckedRunnable {
        final Mutex sync;

        InterruptibleSyncRunnable(Mutex sync) {
            this.sync = sync;
        }

        public void realRun() throws InterruptedException {
            sync.acquireInterruptibly();
        }
    }

    class InterruptedSyncRunnable extends CheckedRunnable {
        final Mutex sync;

        InterruptedSyncRunnable(Mutex sync) {
            this.sync = sync;
        }

        public void realRun() throws InterruptedException {
            sync.acquireInterruptibly();
        }
    }

    /** A constant to clarify calls to checking methods below. */
    static final Thread[] NO_THREADS = new Thread[0];

    /**
     * Spin-waits until sync.isQueued(t) becomes true.
     */
    void waitForQueuedThread(AbstractQueuedSynchronizer sync, Thread t) {
        long startTime = System.nanoTime();
        while (!sync.isQueued(t)) {
            if (millisElapsedSince(startTime) > LONG_DELAY_MS) {
                throw new AssertionFailedError("timed out");
            }
            Thread.yield();
        }
        assertTrue(t.isAlive());
    }

    /**
     * Checks that sync has exactly the given queued threads.
     */
    void assertHasQueuedThreads(
            AbstractQueuedSynchronizer sync, Thread... expected) {
        Collection<Thread> actual = sync.getQueuedThreads();
        assertEquals(expected.length > 0, sync.hasQueuedThreads());
        assertEquals(expected.length, sync.getQueueLength());
        assertEquals(expected.length, actual.size());
        assertEquals(expected.length == 0, actual.isEmpty());
        assertEquals(new HashSet<Thread>(actual), new HashSet<Thread>(Arrays.asList(expected)));
    }

    /**
     * Checks that sync has exactly the given (exclusive) queued threads.
     */
    void assertHasExclusiveQueuedThreads(
            AbstractQueuedSynchronizer sync, Thread... expected) {
        assertHasQueuedThreads(sync, expected);
        assertEquals(new HashSet<Thread>(sync.getExclusiveQueuedThreads()), new HashSet<Thread>(sync.getQueuedThreads()));
        assertEquals(0, sync.getSharedQueuedThreads().size());
        assertTrue(sync.getSharedQueuedThreads().isEmpty());
    }

    /**
     * Checks that sync has exactly the given (shared) queued threads.
     */
    void assertHasSharedQueuedThreads(
            AbstractQueuedSynchronizer sync, Thread... expected) {
        assertHasQueuedThreads(sync, expected);
        assertEquals(new HashSet<Thread>(sync.getSharedQueuedThreads()), new HashSet<Thread>(sync.getQueuedThreads()));
        assertEquals(0, sync.getExclusiveQueuedThreads().size());
        assertTrue(sync.getExclusiveQueuedThreads().isEmpty());
    }

    /**
     * Checks that condition c has exactly the given waiter threads,
     * after acquiring mutex.
     */
    void assertHasWaitersUnlocked(
            Mutex sync, AbstractQueuedSynchronizer.ConditionObject c, Thread... threads) {
        sync.acquire();
        assertHasWaitersLocked(sync, c, threads);
        sync.release();
    }

    /**
     * Checks that condition c has exactly the given waiter threads.
     */
    void assertHasWaitersLocked(
            Mutex sync, AbstractQueuedSynchronizer.ConditionObject c, Thread... threads) {
        assertEquals(threads.length > 0, sync.hasWaiters(c));
        assertEquals(threads.length, sync.getWaitQueueLength(c));
        assertEquals(threads.length == 0, sync.getWaitingThreads(c).isEmpty());
        assertEquals(threads.length, sync.getWaitingThreads(c).size());
        assertEquals(new HashSet<Thread>(sync.getWaitingThreads(c)), new HashSet<Thread>(Arrays.asList(threads)));
    }

    enum AwaitMethod {await, awaitTimed, awaitNanos, awaitUntil}

    /**
     * Awaits condition using the specified AwaitMethod.
     */
    void await(AbstractQueuedSynchronizer.ConditionObject c, AwaitMethod awaitMethod) throws InterruptedException {
        long timeoutMillis = 2 * LONG_DELAY_MS;
        switch (awaitMethod) {
            case await:
                c.await();
                break;
            case awaitTimed:
                assertTrue(c.await(timeoutMillis, MILLISECONDS));
                break;
            case awaitNanos:
                long nanosTimeout = MILLISECONDS.toNanos(timeoutMillis);
                long nanosRemaining = c.awaitNanos(nanosTimeout);
                assertTrue(nanosRemaining > 0);
                break;
            case awaitUntil:
                assertTrue(c.awaitUntil(delayedDate(timeoutMillis)));
                break;
            default:
                throw new AssertionError();
        }
    }

    /**
     * Checks that awaiting the given condition times out (using the
     * default timeout duration).
     */
    void assertAwaitTimesOut(AbstractQueuedSynchronizer.ConditionObject c, AwaitMethod awaitMethod) {
        long timeoutMillis = timeoutMillis();
        long startTime;
        try {
            switch (awaitMethod) {
                case awaitTimed:
                    startTime = System.nanoTime();
                    assertFalse(c.await(timeoutMillis, MILLISECONDS));
                    assertTrue(millisElapsedSince(startTime) >= timeoutMillis);
                    break;
                case awaitNanos:
                    startTime = System.nanoTime();
                    long nanosTimeout = MILLISECONDS.toNanos(timeoutMillis);
                    long nanosRemaining = c.awaitNanos(nanosTimeout);
                    assertTrue(nanosRemaining <= 0);
                    assertTrue(nanosRemaining > -MILLISECONDS.toNanos(LONG_DELAY_MS));
                    assertTrue(millisElapsedSince(startTime) >= timeoutMillis);
                    break;
                case awaitUntil:
                    // We shouldn't assume that nanoTime and currentTimeMillis
                    // use the same time source, so don't use nanoTime here.
                    java.util.Date delayedDate = delayedDate(timeoutMillis());
                    assertFalse(c.awaitUntil(delayedDate(timeoutMillis)));
                    assertTrue(new java.util.Date().getTime() >= delayedDate.getTime());
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        } catch (InterruptedException ie) {
            threadUnexpectedException(ie);
        }
    }

    @Test public void testIsHeldExclusively() {
        Mutex mutex = new Mutex();
        assertFalse(mutex.isHeldExclusively());
    }

    @Test public void testAcquire() {
        Mutex mutex = new Mutex();
        mutex.tryAcquire();
        assertTrue(mutex.isHeldExclusively());
        mutex.tryRelease();
        assertFalse(mutex.isHeldExclusively());
    }

    @Test public void testHasQueuedThreads() {
        final Mutex sync = new Mutex();
        assertFalse(sync.hasQueuedThreads());
        sync.acquire();
        Thread t1 = newStartedThread(new InterruptedSyncRunnable(sync));
        waitForQueuedThread(sync, t1);
        assertTrue(sync.hasQueuedThreads());
        Thread t2 = newStartedThread(new InterruptibleSyncRunnable(sync));
        waitForQueuedThread(sync, t2);
        assertTrue(sync.hasQueuedThreads());
        t1.interrupt();
        //awaitTermination(t1, 1000);
        //assertTrue(sync.hasQueuedThreads());
        sync.release();
        //awaitTermination(t2, 1000);
        System.out.println(sync.getQueueLength());
        //assertFalse(sync.hasQueuedThreads());
    }

    /**
     * isQueued(null) throws NullPointerException
     */
    @Test public void testIsQueuedNPE() {
        final Mutex sync = new Mutex();
        try {
            sync.isQueued(null);
            shouldThrow();
        } catch (NullPointerException success) {
            success.printStackTrace();
        }
    }

    /**
     * isQueued reports whether a thread is queued
     */
    @Test public void testIsQueued() {
        final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        assertFalse(sync.isQueued(t1));
        assertFalse(sync.isQueued(t2));
        sync.acquire();
        t1.start();
        waitForQueuedThread(sync, t1);
        assertTrue(sync.isQueued(t1));
        assertFalse(sync.isQueued(t2));
        t2.start();
        waitForQueuedThread(sync, t2);
        sync.release();
        //assertTrue(sync.isQueued(t1));
        //assertTrue(sync.isQueued(t2));
        //t1.interrupt();
        //awaitTermination(t1);
        //assertFalse(sync.isQueued(t1));
        //assertTrue(sync.isQueued(t2));
        //sync.release();
        //awaitTermination(t2);
        //assertFalse(sync.isQueued(t1));
        //assertFalse(sync.isQueued(t2));
    }

    @Test public void testGetFirstQueuedThread() {
        final Mutex sync = new Mutex();
        assertNull(sync.getFirstQueuedThread());
        sync.acquire();
        Thread t1 = newStartedThread(new InterruptedSyncRunnable(sync));
        waitForQueuedThread(sync, t1);
        assertEquals(t1, sync.getFirstQueuedThread());
        Thread t2 = newStartedThread(new InterruptibleSyncRunnable(sync));
        waitForQueuedThread(sync, t2);
        assertEquals(t1, sync.getFirstQueuedThread());
        t1.interrupt();
        awaitTermination(t1);
        assertEquals(t2, sync.getFirstQueuedThread());
        sync.release();
        awaitTermination(t2);
        assertNull(sync.getFirstQueuedThread());
    }

    /**
     * hasContended reports false if no thread has ever blocked, else true
     */
    @Test public void testHasContended() {
        final Mutex sync = new Mutex();
        assertFalse(sync.hasContended());
        sync.acquire();
        assertFalse(sync.hasContended());
        Thread t1 = newStartedThread(new InterruptedSyncRunnable(sync));
        waitForQueuedThread(sync, t1);
        assertTrue(sync.hasContended());
        Thread t2 = newStartedThread(new InterruptibleSyncRunnable(sync));
        waitForQueuedThread(sync, t2);
        assertTrue(sync.hasContended());
        t1.interrupt();
        awaitTermination(t1);
        assertTrue(sync.hasContended());
        sync.release();
        awaitTermination(t2);
        assertTrue(sync.hasContended());
    }

    @Test
    /**
     * getQueuedThreads returns all waiting threads
     */ public void testGetQueuedThreads() {
        final Mutex sync = new Mutex();
        Thread t1 = new Thread(new InterruptedSyncRunnable(sync));
        Thread t2 = new Thread(new InterruptibleSyncRunnable(sync));
        assertHasExclusiveQueuedThreads(sync, NO_THREADS);
        sync.acquire();
        assertHasExclusiveQueuedThreads(sync, NO_THREADS);
        t1.start();
        waitForQueuedThread(sync, t1);
        assertHasExclusiveQueuedThreads(sync, t1);
        assertTrue(sync.getQueuedThreads().contains(t1));
        assertFalse(sync.getQueuedThreads().contains(t2));
        t2.start();
        waitForQueuedThread(sync, t2);
        assertHasExclusiveQueuedThreads(sync, t1, t2);
        assertTrue(sync.getQueuedThreads().contains(t1));
        assertTrue(sync.getQueuedThreads().contains(t2));
        t1.interrupt();
        awaitTermination(t1);
        assertHasExclusiveQueuedThreads(sync, t2);
        sync.release();
        awaitTermination(t2);
        assertHasExclusiveQueuedThreads(sync, NO_THREADS);
    }

    @Test
    /**
     * getSharedQueuedThreads does not include exclusively waiting threads
     */ public void testGetSharedQueuedThreads_Exclusive() {
        final Mutex sync = new Mutex();
        assertTrue(sync.getSharedQueuedThreads().isEmpty());
        sync.acquire();
        assertTrue(sync.getSharedQueuedThreads().isEmpty());
        Thread t1 = newStartedThread(new InterruptedSyncRunnable(sync));
        waitForQueuedThread(sync, t1);
        assertTrue(sync.getSharedQueuedThreads().isEmpty());
        Thread t2 = newStartedThread(new InterruptibleSyncRunnable(sync));
        waitForQueuedThread(sync, t2);
        assertTrue(sync.getSharedQueuedThreads().isEmpty());
        t1.interrupt();
        awaitTermination(t1);
        assertTrue(sync.getSharedQueuedThreads().isEmpty());
        sync.release();
        awaitTermination(t2);
        assertTrue(sync.getSharedQueuedThreads().isEmpty());
    }

    /**
     * getSharedQueuedThreads returns all shared waiting threads
     */
    @Test public void testGetSharedQueuedThreads_Shared() {
        final BooleanLatch l = new BooleanLatch();
        assertHasSharedQueuedThreads(l, NO_THREADS);
        Thread t1 = newStartedThread(() -> {
            try {
                l.acquireSharedInterruptibly(0);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        waitForQueuedThread(l, t1);
        assertHasSharedQueuedThreads(l, t1);
        Thread t2 = newStartedThread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                l.acquireSharedInterruptibly(0);
            }
        });
        waitForQueuedThread(l, t2);
        assertHasSharedQueuedThreads(l, t1, t2);
        t1.interrupt();
        awaitTermination(t1);
        assertHasSharedQueuedThreads(l, t2);
        assertTrue(l.releaseShared(0));
        awaitTermination(t2);
        assertHasSharedQueuedThreads(l, NO_THREADS);
    }

    /**
     * owns is true for a condition created by sync else false
     */
    @Test public void testOwns() {
        final Mutex sync = new Mutex();
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        final Mutex sync2 = new Mutex();
        assertTrue(sync.owns(c));
        assertFalse(sync2.owns(c));
    }

    /**
     * Calling signalAll without holding sync throws IllegalMonitorStateException
     */
    @Test
    public void testSignalAll_IMSE() {
        final Mutex sync = new Mutex();
        final AbstractQueuedSynchronizer.ConditionObject c = sync.newCondition();
        try {
            c.signalAll();
            shouldThrow();
        } catch (IllegalMonitorStateException success) {
            success.printStackTrace();
        }
    }
}
