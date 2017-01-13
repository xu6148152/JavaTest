package com.zepp.www.rxjavaexample;

//  Created by xubinggui on 11/01/2017.
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public abstract class BlockingQueueTest extends TestBase {
    /** Returns an empty instance of the implementation class. */
    protected abstract BlockingQueue emptyCollection();

    /**
     * Returns an element suitable for insertion in the collection.
     * Override for collections with unusual element types.
     */
    protected Object makeElement(int i) {
        return i;
    }

    public void testOfferNull() {
        final Queue q = emptyCollection();
        try {
            q.offer(null);
            shouldThrow();
        } catch (NullPointerException success) {
            success.printStackTrace();
        }
    }

    /**
     * add(null) throws NullPointerException
     */
    public void testAddNull() {
        final Collection q = emptyCollection();
        try {
            q.add(null);
            shouldThrow();
        } catch (NullPointerException success) {
            success.printStackTrace();
        }
    }

    /**
     * timed offer(null) throws NullPointerException
     */
    public void testTimedOfferNull() throws InterruptedException {
        final BlockingQueue q = emptyCollection();
        long startTime = System.nanoTime();
        try {
            q.offer(null, LONG_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (NullPointerException success) {
            success.printStackTrace();
        }
        assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS);
    }

    /**
     * put(null) throws NullPointerException
     */
    public void testPutNull() throws InterruptedException {
        final BlockingQueue q = emptyCollection();
        try {

            q.put(null);
            shouldThrow();
        } catch (NullPointerException success) {
            success.printStackTrace();
        }
    }

    /**
     * drainTo(this) throws IllegalArgumentException
     */
    public void testDrainToSelf() {
        final BlockingQueue q = emptyCollection();
        try {
            q.drainTo(q);
            shouldThrow();
        } catch (IllegalArgumentException success) {
            success.printStackTrace();
        }
    }

    /**
     * drainTo(c, n) returns 0 and does nothing when n <= 0
     */
    public void testDrainToNonPositiveMaxElements() {
        final BlockingQueue q = emptyCollection();
        final int[] ns = { 0, -1, -42, Integer.MIN_VALUE };
        for (int n : ns)
            assertEquals(0, q.drainTo(new ArrayList(), n));
        if (q.remainingCapacity() > 0) {
            // Not SynchronousQueue, that is
            Object one = makeElement(1);
            q.add(one);
            ArrayList c = new ArrayList();
            for (int n : ns)
                assertEquals(0, q.drainTo(new ArrayList(), n));
            assertEquals(1, q.size());
            assertSame(one, q.poll());
            assertTrue(c.isEmpty());
        }
    }

    public void testTimedPollWithOffer() throws InterruptedException {
        final BlockingQueue q = emptyCollection();
        final CheckedBarrier barrier = new CheckedBarrier(2);
        final Object zero = makeElement(0);

        Thread t = newStartedThread(new CheckedRunnable() {
            public void realRun() throws InterruptedException {
                long startTime = System.nanoTime();
                assertNull(q.poll(timeoutMillis(), MILLISECONDS));
                assertTrue(millisElapsedSince(startTime) >= timeoutMillis());

                barrier.await();

                assertSame(zero, q.poll(LONG_DELAY_MS, MILLISECONDS));

                Thread.currentThread().interrupt();
                try {
                    q.poll(LONG_DELAY_MS, MILLISECONDS);
                    shouldThrow();
                } catch (InterruptedException success) {
                }
                assertFalse(Thread.interrupted());

                barrier.await();
                try {
                    q.poll(LONG_DELAY_MS, MILLISECONDS);
                    shouldThrow();
                } catch (InterruptedException success) {
                }
                assertFalse(Thread.interrupted());

                assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS);
            }
        });

        barrier.await();
        long startTime = System.nanoTime();
        assertTrue(q.offer(zero, LONG_DELAY_MS, MILLISECONDS));
        assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS);

        barrier.await();
        assertThreadStaysAlive(t);
        t.interrupt();
        awaitTermination(t);
    }

    /**
     * take() blocks interruptibly when empty
     */
    public void testTakeFromEmptyBlocksInterruptibly() throws InterruptedException {
        final BlockingQueue q = emptyCollection();
        final CountDownLatch threadStarted = new CountDownLatch(1);
        Thread t = newStartedThread(new CheckedRunnable() {
            public void realRun() {
                threadStarted.countDown();
                try {
                    q.take();
                    shouldThrow();
                } catch (InterruptedException success) {
                    success.printStackTrace();
                }
                assertFalse(Thread.interrupted());
            }
        });

        threadStarted.await();
        assertThreadStaysAlive(t);
        t.interrupt();
        awaitTermination(t);
    }

    public void testRemoveElement() {
        final BlockingQueue q = emptyCollection();
        final int size = Math.min(q.remainingCapacity(), SIZE);
        final Object[] elts = new Object[size];
        assertFalse(q.contains(makeElement(99)));
        assertFalse(q.remove(makeElement(99)));
        checkEmpty(q);
        for (int i = 0; i < size; i++)
            q.add(elts[i] = makeElement(i));
        for (int i = 1; i < size; i += 2) {
            for (int pass = 0; pass < 2; pass++) {
                assertEquals((pass == 0), q.contains(elts[i]));
                assertEquals((pass == 0), q.remove(elts[i]));
                assertFalse(q.contains(elts[i]));
                assertTrue(q.contains(elts[i - 1]));
                if (i < size - 1)
                    assertTrue(q.contains(elts[i + 1]));
            }
        }
        if (size > 0)
            assertTrue(q.contains(elts[0]));
        for (int i = size - 2; i >= 0; i -= 2) {
            assertTrue(q.contains(elts[i]));
            assertFalse(q.contains(elts[i + 1]));
            assertTrue(q.remove(elts[i]));
            assertFalse(q.contains(elts[i]));
            assertFalse(q.remove(elts[i + 1]));
            assertFalse(q.contains(elts[i + 1]));
        }
        checkEmpty(q);
    }
}
