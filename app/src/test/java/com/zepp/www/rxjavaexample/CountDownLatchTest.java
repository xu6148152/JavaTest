package com.zepp.www.rxjavaexample;

//  Created by xubinggui on 05/01/2017.
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

import java.util.BitSet;
import java.util.concurrent.CountDownLatch;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@RunWith(MockitoJUnitRunner.class) public class CountDownLatchTest extends TestBase{

    @Test public void testCountDownLatch() throws InterruptedException {
        BitSet bitSet = new BitSet();
        CountDownLatch countDownLatch = new CountDownLatch(2);

        Thread t1 = new Thread(() -> {
            //try {
            //    countDownLatch.await();
            //}catch (Exception ex) {
            //
            //}
            bitSet.set(1);
            countDownLatch.countDown();
        });

        Thread t2 = new Thread(() -> {
            try {
                //countDownLatch.await();
                Thread.sleep(1000);
            } catch (Exception e) {

            }
            bitSet.set(2);
            countDownLatch.countDown();
        });

        t1.start();
        t2.start();
        //countDownLatch.countDown();
        //t1.join();
        //t2.join();
        countDownLatch.await();

        System.out.println(bitSet.get(1));
        System.out.println(bitSet.get(2));
    }

    @Test public void testAwait() throws InterruptedException {
        final CountDownLatch l = new CountDownLatch(2);
        final CountDownLatch pleaseCountDown = new CountDownLatch(1);

        Thread t = new Thread(() -> {
            assertEquals(2, l.getCount());
            pleaseCountDown.countDown();
            try {
                l.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            assertEquals(0, l.getCount());
        });

        t.start();
        pleaseCountDown.await();
        assertEquals(2, l.getCount());
        l.countDown();
        assertEquals(1, l.getCount());
        assertThreadStaysAlive(t);
        l.countDown();
        assertEquals(0, l.getCount());
        awaitTermination(t, 1000);
    }

    @Test
    public void testAwait_Interruptible() throws InterruptedException {
        final CountDownLatch l = new CountDownLatch(1);
        final CountDownLatch pleaseInterrupt = new CountDownLatch(1);
        Thread t = new Thread(() -> {
            Thread.currentThread().interrupt();
            try {
                l.await();
            } catch (InterruptedException success) {
                success.printStackTrace();
            }
            assertFalse(Thread.interrupted());

            pleaseInterrupt.countDown();
            try {
                l.await();
            } catch (InterruptedException success) {
            }
            assertFalse(Thread.interrupted());

            assertEquals(1, l.getCount());
        });

        t.start();

        pleaseInterrupt.await();
        assertThreadStaysAlive(t);
        t.interrupt();
        awaitTermination(t, 1000);
    }
}
