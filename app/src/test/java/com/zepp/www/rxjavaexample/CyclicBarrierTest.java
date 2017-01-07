package com.zepp.www.rxjavaexample;

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

import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class CyclicBarrierTest extends TestBase {

    private volatile int countAction;

    private class MyAction implements Runnable {
        public void run() {
            ++countAction;
        }
    }

    @Test public void testCyclicBarrier1() {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(1, new MyAction());
        assertEquals(0, cyclicBarrier.getNumberWaiting());
    }

    @Test public void testTwoParties() throws Exception {
        CyclicBarrier cyclicBarrier = new CyclicBarrier(2);
        Thread t = new Thread(() -> {
            try {
                //cyclicBarrier.await();
                //cyclicBarrier.await();
                //cyclicBarrier.await();
                //cyclicBarrier.await();
                System.out.println(cyclicBarrier.getNumberWaiting());
                cyclicBarrier.await();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        t.start();
        cyclicBarrier.await();
        //cyclicBarrier.await();
        //cyclicBarrier.await();
        //cyclicBarrier.await();

        awaitTermination(t, 1000);
    }

    @Test public void testAwait1_Interrupted_BrokenBarrier() throws Exception {
        final CyclicBarrier c = new CyclicBarrier(3);
        final CountDownLatch pleaseInterrupt = new CountDownLatch(2);
        Thread t1 = new Thread(() -> {
            pleaseInterrupt.countDown();
            try {
                c.await();
            } catch (Exception e) {

            }
        });
        Thread t2 = new Thread(() -> {
            pleaseInterrupt.countDown();
            try {
                c.await();
            } catch (Exception e) {

            }
        });

        t1.start();
        t2.start();
        pleaseInterrupt.await();
        t1.interrupt();
        awaitTermination(t1, 1000);
        awaitTermination(t2, 1000);
    }

    @Test public void testReset_BrokenBarrier() throws InterruptedException {
        final CyclicBarrier c = new CyclicBarrier(3);
        final CountDownLatch pleaseReset = new CountDownLatch(2);
        Thread t1 = new Thread(() -> {
            pleaseReset.countDown();
            try {
                c.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        });
        Thread t2 = new Thread(() -> {
            pleaseReset.countDown();
            try {
                c.await();
            } catch (InterruptedException | BrokenBarrierException e) {
                e.printStackTrace();
            }
        });

        t1.start();
        t2.start();
        pleaseReset.await();

        awaitNumberWaiting(c, 2);
        c.reset();
        awaitTermination(t1, 1000);
        awaitTermination(t2, 1000);
    }

    @Test public void testReset_Leakage() throws InterruptedException {
        final CyclicBarrier c = new CyclicBarrier(2);
        final AtomicBoolean done = new AtomicBoolean();
        Thread t = new Thread(() -> {
            while (!done.get()) {
                try {
                    while (c.isBroken()) {
                        c.reset();
                    }
                    c.await();
                } catch (BrokenBarrierException | InterruptedException ok) {
                    ok.printStackTrace();
                }
            }
        });

        t.start();

        for (int i = 0; i < 4; i++) {
            delay(timeoutMillis());
            t.interrupt();
        }
        done.set(true);
        t.interrupt();
        awaitTermination(t, SHORT_DELAY_MS);
    }

    private void awaitNumberWaiting(CyclicBarrier barrier, int numberOfWaiters) {
        while (barrier.getNumberWaiting() != numberOfWaiters) {
            Thread.yield();
        }
    }
}
