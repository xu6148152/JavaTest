package com.zepp.www.rxjavaexample;

//  Created by xubinggui on 13/01/2017.
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicLongArray;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
import java.util.concurrent.atomic.AtomicMarkableReference;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;
import java.util.concurrent.atomic.AtomicStampedReference;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

@SuppressLint("NewApi") public class AtomicTest extends TestBase {
    static long addLong17(long x) {
        return x + 17;
    }

    static int addInt17(int x) {
        return x + 17;
    }

    static Integer addInteger17(Integer x) {
        return new Integer(x.intValue() + 17);
    }

    static Integer sumInteger(Integer x, Integer y) {
        return new Integer(x.intValue() + y.intValue());
    }

    volatile long aLongField;
    volatile int anIntField;
    volatile Integer anIntegerField;

    AtomicLongFieldUpdater aLongFieldUpdater() {
        return AtomicLongFieldUpdater.newUpdater(AtomicTest.class, "aLongField");
    }

    AtomicIntegerFieldUpdater anIntFieldUpdater() {
        return AtomicIntegerFieldUpdater.newUpdater(AtomicTest.class, "anIntField");
    }

    AtomicReferenceFieldUpdater<AtomicTest, Integer> anIntegerFieldUpdater() {
        return AtomicReferenceFieldUpdater.newUpdater(AtomicTest.class, Integer.class, "anIntegerField");
    }

    /**
     * AtomicLong getAndUpdate returns previous value and updates
     * result of supplied function
     */
    @Test public void testLongGetAndUpdate() {
        AtomicLong a = new AtomicLong(1L);
        //返回之前值，并更新之前值
        assertEquals(1L, a.getAndUpdate(AtomicTest::addLong17));
        assertEquals(18L, a.getAndUpdate(AtomicTest::addLong17));
        assertEquals(35L, a.get());
    }

    /**
     * AtomicLong updateAndGet updates with supplied function and
     * returns result.
     */
    @Test public void testLongUpdateAndGet() {
        AtomicLong a = new AtomicLong(1L);
        //更新之前值，并返回新的值
        assertEquals(18L, a.updateAndGet(AtomicTest::addLong17));
        assertEquals(35L, a.updateAndGet(AtomicTest::addLong17));
    }

    /**
     * AtomicLong getAndAccumulate returns previous value and updates
     * with supplied function.
     */
    @Test public void testLongGetAndAccumulate() {
        AtomicLong a = new AtomicLong(1L);
        assertEquals(1L, a.getAndAccumulate(2L, Long::sum));
        assertEquals(3L, a.getAndAccumulate(3L, Long::sum));
        assertEquals(6L, a.get());
    }

    /**
     * AtomicLong accumulateAndGet updates with supplied function and
     * returns result.
     */
    @Test public void testLongAccumulateAndGet() {
        AtomicLong a = new AtomicLong(1L);
        assertEquals(7L, a.accumulateAndGet(6L, Long::sum));
        assertEquals(10L, a.accumulateAndGet(3L, Long::sum));
        assertEquals(10L, a.get());
    }

    /**
     * AtomicInteger getAndUpdate returns previous value and updates
     * result of supplied function
     */
    @Test public void testIntGetAndUpdate() {
        AtomicInteger a = new AtomicInteger(1);
        assertEquals(1, a.getAndUpdate(AtomicTest::addInt17));
        assertEquals(18, a.getAndUpdate(AtomicTest::addInt17));
        assertEquals(35, a.get());
    }

    /**
     * AtomicReference getAndUpdate returns previous value and updates
     * result of supplied function
     */
    @Test public void testReferenceGetAndUpdate() {
        AtomicReference<Integer> a = new AtomicReference<Integer>(one);
        assertEquals(Integer.valueOf(1), a.getAndUpdate(AtomicTest::addInteger17));
        assertEquals(Integer.valueOf(18), a.getAndUpdate(AtomicTest::addInteger17));
        assertEquals(Integer.valueOf(35), a.get());
    }

    /**
     * AtomicReference updateAndGet updates with supplied function and
     * returns result.
     */
    @Test public void testReferenceUpdateAndGet() {
        AtomicReference<Integer> a = new AtomicReference<Integer>(one);
        assertEquals(Integer.valueOf(18), a.updateAndGet(AtomicTest::addInteger17));
        assertEquals(Integer.valueOf(35), a.updateAndGet(AtomicTest::addInteger17));
        assertEquals(Integer.valueOf(35), a.get());
    }

    /**
     * AtomicLongArray getAndUpdate returns previous value and updates
     * result of supplied function
     */
    @Test public void testLongArrayGetAndUpdate() {
        AtomicLongArray a = new AtomicLongArray(1);
        a.set(0, 1);
        assertEquals(1L, a.getAndUpdate(0, AtomicTest::addLong17));
        assertEquals(18L, a.getAndUpdate(0, AtomicTest::addLong17));
        assertEquals(35L, a.get(0));
    }

    /**
     * AtomicLongFieldUpdater getAndUpdate returns previous value and updates
     * result of supplied function
     */
    @Test public void testLongFieldUpdaterGetAndUpdate() {
        AtomicLongFieldUpdater a = aLongFieldUpdater();
        a.set(this, 1);
        assertEquals(1L, a.getAndUpdate(this, AtomicTest::addLong17));
        assertEquals(18L, a.getAndUpdate(this, AtomicTest::addLong17));
        assertEquals(35L, a.get(this));
        assertEquals(35L, aLongField);
    }

    /**
     * get returns the last value lazySet in same thread
     */
    @Test public void testGetLazySet() {
        AtomicBoolean ai = new AtomicBoolean(true);
        assertTrue(ai.get());
        ai.lazySet(false);
        assertFalse(ai.get());
        ai.lazySet(true);
        assertTrue(ai.get());
    }

    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    @Test public void testAtomicBooleanCompareAndSetInMultipleThreads() throws Exception {
        final AtomicBoolean ai = new AtomicBoolean(true);
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                while (!ai.compareAndSet(false, true)) {
                    Thread.yield();
                }
            }
        });

        t.start();
        assertTrue(ai.compareAndSet(true, false));
        t.join(LONG_DELAY_MS);
        assertFalse(t.isAlive());
    }

    /**
     * a deserialized serialized atomic holds same value
     */
    @Test public void testSerialization() throws Exception {
        AtomicBoolean x = new AtomicBoolean();
        AtomicBoolean y = serialClone(x);
        x.set(true);
        AtomicBoolean z = serialClone(x);
        assertTrue(x.get());
        assertFalse(y.get());
        assertTrue(z.get());
    }

    /**
     * compareAndSet in one thread enables another waiting for value
     * to succeed
     */
    @Test public void testAtomicIntegerArrayCompareAndSetInMultipleThreads() throws Exception {
        final AtomicIntegerArray a = new AtomicIntegerArray(1);
        a.set(0, 1);
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                while (!a.compareAndSet(0, 2, 3)) {
                    Thread.yield();
                }
            }
        });

        t.start();
        assertTrue(a.compareAndSet(0, 1, 2));
        t.join(LONG_DELAY_MS);
        assertFalse(t.isAlive());
        assertEquals(3, a.get(0));
    }

    class AtomicIntegerArrayCounter extends CheckedRunnable {
        final AtomicIntegerArray aa;
        volatile int counts;

        AtomicIntegerArrayCounter(AtomicIntegerArray a) {
            aa = a;
        }

        public void realRun() {
            for (; ; ) {
                boolean done = true;
                for (int i = 0; i < aa.length(); i++) {
                    int v = aa.get(i);
                    assertTrue(v >= 0);
                    if (v != 0) {
                        done = false;
                        if (aa.compareAndSet(i, v, v - 1)) {
                            ++counts;
                        }
                    }
                }
                if (done) {
                    break;
                }
            }
        }
    }

    /**
     * Multiple threads using same array of counters successfully
     * update a number of times equal to total count
     */
    @Test public void testAtomicIntegerArrayCountingInMultipleThreads() throws InterruptedException {
        final AtomicIntegerArray aa = new AtomicIntegerArray(SIZE);
        int countdown = 10000;
        for (int i = 0; i < SIZE; i++)
            aa.set(i, countdown);
        AtomicIntegerArrayCounter c1 = new AtomicIntegerArrayCounter(aa);
        AtomicIntegerArrayCounter c2 = new AtomicIntegerArrayCounter(aa);
        Thread t1 = new Thread(c1);
        Thread t2 = new Thread(c2);
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        assertEquals(c1.counts + c2.counts, SIZE * countdown);
    }

    /**
     * constructor initializes to given reference and mark
     */
    @Test public void testAtomicMarkableReferenceConstructor() {
        AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        assertSame(one, ai.getReference());
        assertFalse(ai.isMarked());
        AtomicMarkableReference a2 = new AtomicMarkableReference(null, true);
        assertNull(a2.getReference());
        assertTrue(a2.isMarked());
    }

    /**
     * compareAndSet in one thread enables another waiting for reference value
     * to succeed
     */
    @Test public void testAtomicMarkableReferenceCompareAndSetInMultipleThreads() throws Exception {
        final AtomicMarkableReference ai = new AtomicMarkableReference(one, false);
        Thread t = new Thread(new CheckedRunnable() {
            public void realRun() {
                while (!ai.compareAndSet(two, three, false, false)) {
                    Thread.yield();
                }
            }
        });

        t.start();
        assertTrue(ai.compareAndSet(one, two, false, false));
        t.join(LONG_DELAY_MS);
        assertFalse(t.isAlive());
        assertSame(three, ai.getReference());
        assertFalse(ai.isMarked());
    }

    /**
     * compareAndSet succeeds in changing values if equal to expected reference
     * and stamp else fails
     */
    @Test public void testCompareAndSet() {
        int[] mark = new int[1];
        AtomicStampedReference ai = new AtomicStampedReference(one, 0);
        assertSame(one, ai.get(mark));
        assertEquals(0, ai.getStamp());
        assertEquals(0, mark[0]);

        assertTrue(ai.compareAndSet(one, two, 0, 0));
        assertSame(two, ai.get(mark));
        assertEquals(0, mark[0]);

        assertTrue(ai.compareAndSet(two, m3, 0, 1));
        assertSame(m3, ai.get(mark));
        assertEquals(1, mark[0]);

        assertFalse(ai.compareAndSet(two, m3, 1, 1));
        assertSame(m3, ai.get(mark));
        assertEquals(1, mark[0]);
    }
}
