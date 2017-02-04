package com.binea.www.rxjavaexample;

//  Created by xubinggui on 07/01/2017.
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
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class AbstractExecutorServiceTest extends BaseTest {
    /**
     * A no-frills implementation of AbstractExecutorService, designed
     * to test the submit methods only.
     */
    static class DirectExecutorService extends AbstractExecutorService {
        public void execute(Runnable r) {
            r.run();
        }

        public void shutdown() {
            shutdown = true;
        }

        public List<Runnable> shutdownNow() {
            shutdown = true;
            return Collections.EMPTY_LIST;
        }

        public boolean isShutdown() {
            return shutdown;
        }

        public boolean isTerminated() {
            return isShutdown();
        }

        public boolean awaitTermination(long timeout, TimeUnit unit) {
            return isShutdown();
        }

        private volatile boolean shutdown = false;
    }

    @Test public void testExecuteRunnable() throws Exception {
        ExecutorService executorService = new DirectExecutorService();
        final AtomicBoolean atomicBoolean = new AtomicBoolean(false);
        final Future<?> future = executorService.submit(() -> {
            atomicBoolean.set(true);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        assertNull(future.get());
        assertNull(future.get(0, TimeUnit.SECONDS));
        assertTrue(atomicBoolean.get());
        assertTrue(future.isDone());
        assertFalse(future.isCancelled());
    }

    @Test public void testSubmitCallable() throws Exception {
        ExecutorService executorService = new DirectExecutorService();
        final Future<String> submit = executorService.submit(new StringTask());
        assertNotNull(submit.get());
        assertNotNull(submit.get(0, TimeUnit.SECONDS));
        assertTrue(submit.isDone());
        assertFalse(submit.isCancelled());
    }

    @Test public void testSubmitPrivilegedAction() throws Exception {
        Runnable r = () -> {
            ExecutorService e = new DirectExecutorService();
            Future future = e.submit(Executors.callable(new PrivilegedAction() {
                public Object run() {
                    return TEST_STRING;
                }
            }));

            try {
                assertSame(TEST_STRING, future.get());
            } catch (InterruptedException | ExecutionException e1) {
                e1.printStackTrace();
            }
        };

        runWithPermissions(r, new RuntimePermission("getClassLoader"), new RuntimePermission("setContextClassLoader"),
                           new RuntimePermission("modifyThread"));
    }

    @SuppressLint("NewApi") @Test public void testInterruptedSubmit() throws InterruptedException {
        final CountDownLatch submitted = new CountDownLatch(1);
        final CountDownLatch quittingTime = new CountDownLatch(1);
        final Callable<Void> awaiter = () -> {
            assertTrue(quittingTime.await(2 * LONG_DELAY_MS, MILLISECONDS));
            return null;
        };
        final ExecutorService p = new ThreadPoolExecutor(1, 1, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<Runnable>(10));
        try (PoolCleaner cleaner = cleaner(p, quittingTime)) {
            Thread t = new Thread(() -> {
                Future<Void> future = p.submit(awaiter);
                submitted.countDown();
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            });
            t.start();
            submitted.await();
            t.interrupt();
            awaitTermination(t, 1000);
        }
    }
}
