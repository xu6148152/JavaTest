package com.binea.www.rxjavaexample;

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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.junit.Test;

public class ArrayBlockingQueueTest extends BlockingQueueTest {
    @Override protected BlockingQueue emptyCollection() {
        boolean fair = true;//control fair lock or unfair lock
        if(fair) {
            return new ArrayBlockingQueue(SIZE, true);
        }
        return new ArrayBlockingQueue(SIZE, false);
    }

    @Override @Test public void testOfferNull() {
        super.testOfferNull();
    }

    @Override @Test public void testAddNull() {
        super.testAddNull();
    }

    @Override @Test public void testTimedOfferNull() throws InterruptedException {
        super.testTimedOfferNull();
    }

    @Override @Test public void testPutNull() throws InterruptedException {
        super.testPutNull();
    }

    @Override @Test public void testDrainToSelf() {
        super.testDrainToSelf();
    }

    @Override @Test public void testDrainToNonPositiveMaxElements() {
        super.testDrainToNonPositiveMaxElements();
    }

    @Override @Test public void testTimedPollWithOffer() throws InterruptedException {
        super.testTimedPollWithOffer();
    }

    @Override @Test public void testTakeFromEmptyBlocksInterruptibly() throws InterruptedException {
        super.testTakeFromEmptyBlocksInterruptibly();
    }

    @Override @Test public void testRemoveElement() {
        super.testRemoveElement();
    }

    @Test public void testArrayBlocking() {
        BlockingQueue queue = emptyCollection();
        //for(int i = 0; i < SIZE; i++) {
        //    queue.add(i);
        //}
        try {
            Thread t = new Thread(() -> {
                try {
                    System.out.println("take: " + queue.take());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            });
            t.start();
            queue.put(-1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        printQueueRemainCapacity(queue);
        System.out.println("peek: " + queue.peek());
        printQueueRemainCapacity(queue);
        System.out.println("poll: " + queue.poll());
        printQueueRemainCapacity(queue);

    }

    private void printQueueRemainCapacity(BlockingQueue queue) {
        System.out.println("remain capacity: " + queue.remainingCapacity());
    }
}
