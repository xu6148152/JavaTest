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

import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class ArrayDequeTest extends TestBase {
    /**
     * Returns a new deque of given size containing consecutive
     * Integers 0 ... n.
     */
    private ArrayDeque<Integer> populatedDeque(int n) {
        ArrayDeque<Integer> q = new ArrayDeque<Integer>();
        assertTrue(q.isEmpty());
        for (int i = 0; i < n; ++i)
            assertTrue(q.offerLast(new Integer(i)));
        assertFalse(q.isEmpty());
        assertEquals(n, q.size());
        return q;
    }

    @Test public void testConstructor1() {
        assertEquals(0, new ArrayDeque().size());
    }

    /**
     * Initializing from null Collection throws NPE
     */
    @Test public void testConstructor3() {
        try {
            new ArrayDeque((Collection) null);
            shouldThrow();
        } catch (NullPointerException success) {
            success.printStackTrace();
        }
    }

    /**
     * Deque contains all elements of collection used to initialize
     */
    @Test public void testConstructor6() {
        Integer[] ints = new Integer[SIZE];
        for (int i = 0; i < SIZE; ++i)
            ints[i] = new Integer(i);
        ArrayDeque q = new ArrayDeque(Arrays.asList(ints));
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(ints[i], q.pollFirst());
            System.out.println(q.size());
        }
    }

    /**
     * isEmpty is true before add, false after
     */
    @Test public void testEmpty() {
        ArrayDeque q = new ArrayDeque();
        assertTrue(q.isEmpty());
        q.add(new Integer(1));
        assertFalse(q.isEmpty());
        q.add(new Integer(2));
        q.removeFirst();
        q.removeFirst();
        assertTrue(q.isEmpty());
    }

    /**
     * size changes when elements added and removed
     */
    @Test public void testSize() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(SIZE - i, q.size());
            q.removeFirst();
        }
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.size());
            q.add(new Integer(i));
        }
    }

    /**
     * push(null) throws NPE
     */
    @Test public void testPushNull() {
        ArrayDeque q = new ArrayDeque(1);
        try {
            q.push(null);
            shouldThrow();
        } catch (NullPointerException success) {
            success.printStackTrace();
        }
    }

    /**
     * peekFirst() returns element inserted with push
     */
    @Test public void testPush() {
        ArrayDeque q = populatedDeque(3);
        q.pollLast();
        q.push(four);
        assertSame(four, q.peekFirst());
        System.out.println(q.peekFirst());
    }

    /**
     * pop() removes next element, or throws NSEE if empty
     */
    @Test public void testPop() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.pop());
        }
        try {
            q.pop();
            shouldThrow();
        } catch (NoSuchElementException success) {
            success.printStackTrace();
        }
    }

    /**
     * offer(x) succeeds
     */
    @Test public void testOffer() {
        ArrayDeque q = new ArrayDeque();
        assertTrue(q.offer(zero));
        assertTrue(q.offer(one));
        assertSame(zero, q.peekFirst());
        assertSame(one, q.peekLast());
        System.out.println(q.peek());
        System.out.println(q.peekFirst());
        System.out.println(q.peekLast());
        System.out.println(q.getFirst());
        System.out.println(q.getLast());
    }

    /**
     * add(x) succeeds
     */
    @Test public void testAdd() {
        ArrayDeque q = new ArrayDeque();
        assertTrue(q.add(zero));
        assertTrue(q.add(one));
        assertSame(zero, q.peekFirst());
        assertSame(one, q.peekLast());
    }

    /**
     * pollFirst() succeeds unless empty
     */
    @Test public void testPollFirst() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.pollFirst());
        }
        assertNull(q.pollFirst());
    }

    /**
     * pollLast() succeeds unless empty
     */
    @Test public void testPollLast() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = SIZE - 1; i >= 0; --i) {
            assertEquals(i, q.pollLast());
        }
        assertNull(q.pollLast());
    }

    /**
     * poll() succeeds unless empty
     */
    @Test public void testPoll() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.poll());
        }
        assertNull(q.poll());
    }

    /**
     * remove() removes next element, or throws NSEE if empty
     */
    @Test public void testRemove() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.remove());
        }
        try {
            q.remove();
            shouldThrow();
        } catch (NoSuchElementException success) {
            success.printStackTrace();
        }
    }

    /**
     * peek() returns next element, or null if empty
     */
    @Test public void testPeek() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.peek());
            assertEquals(i, q.poll());
            assertTrue(q.peek() == null || !q.peek().equals(i));
        }
        assertNull(q.peek());
    }

    /**
     * element() returns first element, or throws NSEE if empty
     */
    @Test public void testElement() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertEquals(i, q.element());
            assertEquals(i, q.poll());
        }
        try {
            q.element();
            shouldThrow();
        } catch (NoSuchElementException success) {
        }
    }

    /**
     * removeFirstOccurrence(x) removes x and returns true if present
     */
    @Test public void testRemoveFirstOccurrence() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 1; i < SIZE; i += 2) {
            assertTrue(q.removeFirstOccurrence(new Integer(i)));
        }
        for (int i = 0; i < SIZE; i += 2) {
            assertTrue(q.removeFirstOccurrence(new Integer(i)));
            assertFalse(q.removeFirstOccurrence(new Integer(i + 1)));
        }
        assertTrue(q.isEmpty());
    }

    /**
     * contains(x) reports true when elements added but not yet removed
     */
    @Test public void testContains() {
        ArrayDeque q = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            assertTrue(q.contains(new Integer(i)));
            assertEquals(i, q.pollFirst());
            assertFalse(q.contains(new Integer(i)));
        }
    }

    /**
     * retainAll(c) retains only those elements of c and reports true if changed
     */
    @Test public void testRetainAll() {
        ArrayDeque q = populatedDeque(SIZE);
        ArrayDeque p = populatedDeque(SIZE);
        for (int i = 0; i < SIZE; ++i) {
            boolean changed = q.retainAll(p);
            assertEquals(changed, (i > 0));
            assertTrue(q.containsAll(p));
            assertEquals(SIZE - i, q.size());
            p.removeFirst();
        }
    }

    void checkToArray(ArrayDeque q) {
        int size = q.size();
        Object[] o = q.toArray();
        assertEquals(size, o.length);
        Iterator it = q.iterator();
        for (int i = 0; i < size; i++) {
            Integer x = (Integer) it.next();
            assertEquals((Integer)o[0] + i, (int) x);
            assertSame(o[i], x);
        }
    }
}
