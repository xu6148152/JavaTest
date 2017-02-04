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

import java.util.AbstractQueue;
import java.util.Iterator;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AbstractQueueTest extends BaseTest {
    static class Succeed extends AbstractQueue<Integer> {
        Integer mElement = one;

        @Override public Iterator<Integer> iterator() {
            return null;
        }

        @Override public int size() {
            return 0;
        }

        @Override public boolean offer(Integer integer) {
            if (integer == null) {
                throw new NullPointerException();
            }
            mElement = integer;
            return true;
        }

        @Override public Integer poll() {
            return mElement;
        }

        @Override public Integer peek() {
            return mElement;
        }
    }

    static class Fail extends AbstractQueue<Integer> {
        public boolean offer(Integer x) {
            if (x == null) {
                throw new NullPointerException();
            }
            return false;
        }

        public Integer peek() {
            return null;
        }

        public Integer poll() {
            return null;
        }

        public int size() {
            return 0;
        }

        public Iterator iterator() {
            return null;
        } // not needed
    }

    /**
     * add returns true if offer succeeds
     */
    @Test public void testAddS() {
        Succeed q = new Succeed();
        assertTrue(q.add(two));
        assertEquals(two, q.poll());
        assertEquals(two, q.element());
        assertEquals(two, q.peek());
    }

    /**
     * add throws ISE true if offer fails
     */
    @Test public void testAddF() {
        Fail q = new Fail();
        try {
            q.add(one);
        } catch (IllegalStateException success) {
            success.printStackTrace();
        }
    }
}
