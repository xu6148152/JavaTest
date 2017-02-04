package com.binea.www.rxjavaexample;

//  Created by xubinggui on 06/01/2017.
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

import android.annotation.SuppressLint;

import junit.framework.AssertionFailedError;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.security.Permission;
import java.util.Arrays;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class BaseTest {

    public static final int LONG_DELAY_MS = 1000;
    public static final long MEDIUM_DELAY_MS = 1000;
    /**
     * The first exception encountered if any threadAssertXXX method fails.
     */
    private final AtomicReference<Throwable> threadFailure = new AtomicReference<Throwable>(null);

    static final int SHORT_DELAY_MS = 1000;

    /**
     * The number of elements to place in collections, arrays, etc.
     */
    public static final int SIZE = 20;

    public static final Integer zero = 0;
    public static final Integer one = 1;
    public static final Integer two = 2;
    public static final Integer three = 3;
    public static final Integer four = 4;
    public static final Integer five = 5;
    public static final Integer six = 6;
    public static final Integer seven = 7;
    public static final Integer eight = 8;
    public static final Integer nine = 9;
    public static final Integer m1 = new Integer(-1);
    public static final Integer m2 = new Integer(-2);
    public static final Integer m3 = new Integer(-3);
    public static final Integer m4 = new Integer(-4);
    public static final Integer m5 = new Integer(-5);
    public static final Integer m6 = new Integer(-6);
    public static final Integer m10 = new Integer(-10);

    void awaitTermination(Thread t, long timeoutMillis) {
        try {
            t.join(timeoutMillis);
        } catch (InterruptedException fail) {
            //threadUnexpectedException(fail);
        } finally {
            if (t.getState() != Thread.State.TERMINATED) {
                t.interrupt();
                //threadFail("timed out waiting for thread to terminate");
            }
        }
    }

    void awaitTermination(Thread t) {
        awaitTermination(t, 1000);
    }

    void delay(long millis) throws InterruptedException {
        long nanos = millis * (1000 * 1000);
        final long wakeupTime = System.nanoTime() + nanos;
        do {
            if (millis > 0L) {
                Thread.sleep(millis);
            } else // too short to sleep
            {
                Thread.yield();
            }
            nanos = wakeupTime - System.nanoTime();
            millis = nanos / (1000 * 1000);
        } while (nanos >= 0L);
    }

    long timeoutMillis() {
        return SHORT_DELAY_MS / 4;
    }

    public static final String TEST_STRING = "a test string";

    public static class StringTask implements Callable<String> {
        final String value;

        public StringTask() {
            this(TEST_STRING);
        }

        public StringTask(String value) {
            this.value = value;
        }

        public String call() {
            return value;
        }
    }

    public void runWithPermissions(Runnable r, Permission... permissions) {
        // Android-changed - no SecurityManager
        // SecurityManager sm = System.getSecurityManager();
        // if (sm == null) {
        //     r.run();
        // }
        // runWithSecurityManagerWithPermissions(r, permissions);
        r.run();
    }

    /**
     * Allows use of try-with-resources with per-test thread pools.
     */
    @SuppressLint("NewApi")
    class PoolCleaner implements AutoCloseable {
        private final ExecutorService pool;

        public PoolCleaner(ExecutorService pool) {
            this.pool = pool;
        }

        public void close() {
            joinPool(pool);
        }
    }

    /**
     * Waits out termination of a thread pool or fails doing so.
     */
    void joinPool(ExecutorService pool) {
        try {
            pool.shutdown();
            if (!pool.awaitTermination(2 * LONG_DELAY_MS, MILLISECONDS)) {
                try {
                    threadFail("ExecutorService " + pool +
                            " did not terminate in a timely manner");
                } finally {
                    // last resort, for the benefit of subsequent tests
                    pool.shutdownNow();
                    pool.awaitTermination(MEDIUM_DELAY_MS, MILLISECONDS);
                }
            }
        } catch (SecurityException ok) {
            // Allowed in case test doesn't have privs
        } catch (InterruptedException fail) {
            threadFail("Unexpected InterruptedException");
        }
    }

    /**
     * Just like fail(reason), but additionally recording (using
     * threadRecordFailure) any AssertionFailedError thrown, so that
     * the current testcase will fail.
     */
    public void threadFail(String reason) {
        try {
            fail(reason);
        } catch (AssertionFailedError t) {
            threadRecordFailure(t);
            throw t;
        }
    }

    /**
     * Records an exception so that it can be rethrown later in the test
     * harness thread, triggering a test case failure.  Only the first
     * failure is recorded; subsequent calls to this method from within
     * the same test have no effect.
     */
    public void threadRecordFailure(Throwable t) {
        System.err.println(t);
        dumpTestThreads();
        threadFailure.compareAndSet(null, t);
    }

    /**
     * A debugging tool to print stack traces of most threads, as jstack does.
     * Uninteresting threads are filtered out.
     */
    static void dumpTestThreads() {
        // Android-change no ThreadMXBean
        // ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        // System.err.println("------ stacktrace dump start ------");
        // for (ThreadInfo info : threadMXBean.dumpAllThreads(true, true)) {
        //     String name = info.getThreadName();
        //     if ("Signal Dispatcher".equals(name))
        //         continue;
        //     if ("Reference Handler".equals(name)
        //         && info.getLockName().startsWith("java.lang.ref.Reference$Lock"))
        //         continue;
        //     if ("Finalizer".equals(name)
        //         && info.getLockName().startsWith("java.lang.ref.ReferenceQueue$Lock"))
        //         continue;
        //     if ("checkForWedgedTest".equals(name))
        //         continue;
        //     System.err.print(info);
        // }
        // System.err.println("------ stacktrace dump end ------");
    }

    /**
     * An extension of PoolCleaner that has an action to release the pool.
     */
    class PoolCleanerWithReleaser extends PoolCleaner {
        private final Runnable releaser;

        public PoolCleanerWithReleaser(ExecutorService pool, Runnable releaser) {
            super(pool);
            this.releaser = releaser;
        }

        public void close() {
            try {
                releaser.run();
            } finally {
                super.close();
            }
        }
    }

    PoolCleaner cleaner(ExecutorService pool, CountDownLatch latch) {
        return new PoolCleanerWithReleaser(pool, releaser(latch));
    }

    Runnable releaser(final CountDownLatch latch) {
        return () -> {
            do {
                latch.countDown();
            } while (latch.getCount() > 0);
        };
    }

    public abstract class CheckedRunnable implements Runnable {
        protected abstract void realRun() throws Throwable;

        public final void run() {
            try {
                realRun();
            } catch (Throwable fail) {
                threadUnexpectedException(fail);
            }
        }
    }

    /**
     * Records the given exception using {@link #threadRecordFailure},
     * then rethrows the exception, wrapping it in an
     * AssertionFailedError if necessary.
     */
    public void threadUnexpectedException(Throwable t) {
        threadRecordFailure(t);
        t.printStackTrace();
        if (t instanceof RuntimeException) {
            throw (RuntimeException) t;
        } else if (t instanceof Error) {
            throw (Error) t;
        } else {
            AssertionFailedError afe = new AssertionFailedError("unexpected exception: " + t);
            afe.initCause(t);
            throw afe;
        }
    }

    /**
     * Returns the number of milliseconds since time given by
     * startNanoTime, which must have been previously returned from a
     * call to {@link System#nanoTime()}.
     */
    static long millisElapsedSince(long startNanoTime) {
        return NANOSECONDS.toMillis(System.nanoTime() - startNanoTime);
    }

    /**
     * Returns a new Date instance representing a time at least
     * delayMillis milliseconds in the future.
     */
    Date delayedDate(long delayMillis) {
        // Add 1 because currentTimeMillis is known to round into the past.
        return new Date(System.currentTimeMillis() + delayMillis + 1);
    }

    /**
     * Returns a new started daemon Thread running the given runnable.
     */
    Thread newStartedThread(Runnable runnable) {
        Thread t = new Thread(runnable);
        t.setDaemon(true);
        t.start();
        return t;
    }

    /**
     * Fails with message "should throw " + exceptionName.
     */
    public void shouldThrow(String exceptionName) {
        fail("Should throw " + exceptionName);
    }

    /**
     * Fails with message "should throw exception".
     */
    public void shouldThrow() {
        fail("Should throw exception");
    }

    /**
     * A CyclicBarrier that uses timed await and fails with
     * AssertionFailedErrors instead of throwing checked exceptions.
     */
    public class CheckedBarrier extends CyclicBarrier {
        public CheckedBarrier(int parties) {
            super(parties);
        }

        public int await() {
            try {
                return super.await(2 * LONG_DELAY_MS, MILLISECONDS);
            } catch (TimeoutException timedOut) {
                throw new AssertionFailedError("timed out");
            } catch (Exception fail) {
                AssertionFailedError afe = new AssertionFailedError("Unexpected exception: " + fail);
                afe.initCause(fail);
                throw afe;
            }
        }
    }

    /**
     * Checks that thread does not terminate within the default
     * millisecond delay of {@code timeoutMillis()}.
     */
    void assertThreadStaysAlive(Thread thread) {
        assertThreadStaysAlive(thread, timeoutMillis());
    }

    /**
     * Checks that thread does not terminate within the given millisecond delay.
     */
    void assertThreadStaysAlive(Thread thread, long millis) {
        try {
            // No need to optimize the failing case via Thread.join.
            delay(millis);
            assertTrue(thread.isAlive());
        } catch (InterruptedException fail) {
            threadFail("Unexpected InterruptedException");
        }
    }

    void checkEmpty(BlockingQueue q) {
        try {
            assertTrue(q.isEmpty());
            assertEquals(0, q.size());
            assertNull(q.peek());
            assertNull(q.poll());
            assertNull(q.poll(0, MILLISECONDS));
            assertEquals(q.toString(), "[]");
            assertTrue(Arrays.equals(q.toArray(), new Object[0]));
            assertFalse(q.iterator().hasNext());
            try {
                q.element();
                shouldThrow();
            } catch (NoSuchElementException success) {
            }
            try {
                q.iterator().next();
                shouldThrow();
            } catch (NoSuchElementException success) {
            }
            try {
                q.remove();
                shouldThrow();
            } catch (NoSuchElementException success) {
            }
        } catch (InterruptedException fail) {
            threadUnexpectedException(fail);
        }
    }

    <T> T serialClone(T o) {
        try {
            ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(serialBytes(o)));
            T clone = (T) ois.readObject();
            assertSame(o.getClass(), clone.getClass());
            return clone;
        } catch (Throwable fail) {
            threadUnexpectedException(fail);
            return null;
        }
    }

    byte[] serialBytes(Object o) {
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream oos = new ObjectOutputStream(bos);
            oos.writeObject(o);
            oos.flush();
            oos.close();
            return bos.toByteArray();
        } catch (Throwable fail) {
            threadUnexpectedException(fail);
            return new byte[0];
        }
    }

    /**
     * Checks that timed f.get() returns the expected value, and does not
     * wait for the timeout to elapse before returning.
     */
    <T> void checkTimedGet(Future<T> f, T expectedValue, long timeoutMillis) {
        long startTime = System.nanoTime();
        try {
            assertEquals(expectedValue, f.get(timeoutMillis, MILLISECONDS));
        } catch (Throwable fail) {
            threadUnexpectedException(fail);
        }
        if (millisElapsedSince(startTime) > timeoutMillis / 2)
            throw new AssertionFailedError("timed get did not return promptly");
    }

    <T> void checkTimedGet(Future<T> f, T expectedValue) {
        checkTimedGet(f, expectedValue, LONG_DELAY_MS);
    }

    /**
     * Just like assertSame(x, y), but additionally recording (using
     * threadRecordFailure) any AssertionFailedError thrown, so that
     * the current testcase will fail.
     */
    public void threadAssertSame(Object x, Object y) {
        try {
            assertSame(x, y);
        } catch (AssertionFailedError fail) {
            threadRecordFailure(fail);
            throw fail;
        }
    }

    /**
     * Just like assertNull(x), but additionally recording (using
     * threadRecordFailure) any AssertionFailedError thrown, so that
     * the current testcase will fail.
     */
    public void threadAssertNull(Object x) {
        try {
            assertNull(x);
        } catch (AssertionFailedError t) {
            threadRecordFailure(t);
            throw t;
        }
    }

    /**
     * Just like assertTrue(b), but additionally recording (using
     * threadRecordFailure) any AssertionFailedError thrown, so that
     * the current testcase will fail.
     */
    public void threadAssertTrue(boolean b) {
        try {
            assertTrue(b);
        } catch (AssertionFailedError t) {
            threadRecordFailure(t);
            throw t;
        }
    }
}
