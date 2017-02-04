package com.binea.www.rxjavaexample;

import org.junit.Test;

import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.*;

/**
 * Created by binea on 4/2/2017.
 */

public class CompletableFutureTest extends BaseTest {
    static class CFException extends RuntimeException {
    }

    void checkIncomplete(CompletableFuture<?> f) {
        assertFalse(f.isDone());
        assertFalse(f.isCancelled());
        assertTrue(f.toString().contains("Not completed"));
        try {
            assertNull(f.getNow(null));
        } catch (Throwable fail) {
            threadUnexpectedException(fail);
        }
        try {
            f.get(0L, SECONDS);
            shouldThrow();
        } catch (TimeoutException success) {
        } catch (Throwable fail) {
            threadUnexpectedException(fail);
        }
    }

    <T> void checkCompletedNormally(CompletableFuture<T> f, T value) {
        checkTimedGet(f, value);

        try {
            assertEquals(value, f.join());
        } catch (Throwable fail) {
            threadUnexpectedException(fail);
        }
        try {
            assertEquals(value, f.getNow(null));
        } catch (Throwable fail) {
            threadUnexpectedException(fail);
        }
        try {
            assertEquals(value, f.get());
        } catch (Throwable fail) {
            threadUnexpectedException(fail);
        }
        assertTrue(f.isDone());
        assertFalse(f.isCancelled());
        assertFalse(f.isCompletedExceptionally());
        assertTrue(f.toString().contains("[Completed normally]"));
    }

    /**
     * Returns the "raw" internal exceptional completion of f,
     * without any additional wrapping with CompletionException.
     */
    <U> Throwable exceptionalCompletion(CompletableFuture<U> f) {
        // handle (and whenComplete) can distinguish between "direct"
        // and "wrapped" exceptional completion
        return f.handle((U u, Throwable t) -> t).join();
    }

    void checkCompletedExceptionally(CompletableFuture<?> f,
                                     boolean wrapped,
                                     Consumer<Throwable> checker) {
        Throwable cause = exceptionalCompletion(f);
        if (wrapped) {
            assertTrue(cause instanceof CompletionException);
            cause = cause.getCause();
        }
        checker.accept(cause);

        long startTime = System.nanoTime();
        try {
            f.get(LONG_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (ExecutionException success) {
            assertSame(cause, success.getCause());
        } catch (Throwable fail) {
            threadUnexpectedException(fail);
        }
        assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS / 2);

        try {
            f.join();
            shouldThrow();
        } catch (CompletionException success) {
            assertSame(cause, success.getCause());
        } catch (Throwable fail) {
            threadUnexpectedException(fail);
        }

        try {
            f.getNow(null);
            shouldThrow();
        } catch (CompletionException success) {
            assertSame(cause, success.getCause());
        } catch (Throwable fail) {
            threadUnexpectedException(fail);
        }

        try {
            f.get();
            shouldThrow();
        } catch (ExecutionException success) {
            assertSame(cause, success.getCause());
        } catch (Throwable fail) {
            threadUnexpectedException(fail);
        }

        assertFalse(f.isCancelled());
        assertTrue(f.isDone());
        assertTrue(f.isCompletedExceptionally());
        assertTrue(f.toString().contains("[Completed exceptionally]"));
    }

    void checkCompletedWithWrappedCFException(CompletableFuture<?> f) {
        checkCompletedExceptionally(f, true,
                (t) -> assertTrue(t instanceof CFException));
    }

    void checkCompletedWithWrappedCancellationException(CompletableFuture<?> f) {
        checkCompletedExceptionally(f, true,
                (t) -> assertTrue(t instanceof CancellationException));
    }

    void checkCompletedWithTimeoutException(CompletableFuture<?> f) {
        checkCompletedExceptionally(f, false,
                (t) -> assertTrue(t instanceof TimeoutException));
    }

    void checkCompletedWithWrappedException(CompletableFuture<?> f,
                                            Throwable ex) {
        checkCompletedExceptionally(f, true, (t) -> assertSame(t, ex));
    }

    void checkCompletedExceptionally(CompletableFuture<?> f, Throwable ex) {
        checkCompletedExceptionally(f, false, (t) -> assertSame(t, ex));
    }

    void checkCancelled(CompletableFuture<?> f) {
        long startTime = System.nanoTime();
        try {
            f.get(LONG_DELAY_MS, MILLISECONDS);
            shouldThrow();
        } catch (CancellationException success) {
        } catch (Throwable fail) {
            threadUnexpectedException(fail);
        }
        assertTrue(millisElapsedSince(startTime) < LONG_DELAY_MS / 2);

        try {
            f.join();
            shouldThrow();
        } catch (CancellationException success) {
        }
        try {
            f.getNow(null);
            shouldThrow();
        } catch (CancellationException success) {
        }
        try {
            f.get();
            shouldThrow();
        } catch (CancellationException success) {
        } catch (Throwable fail) {
            threadUnexpectedException(fail);
        }

        assertTrue(exceptionalCompletion(f) instanceof CancellationException);

        assertTrue(f.isDone());
        assertTrue(f.isCompletedExceptionally());
        assertTrue(f.isCancelled());
        assertTrue(f.toString().contains("[Completed exceptionally]"));
    }

    /**
     * Permits the testing of parallel code for the 3 different
     * execution modes without copy/pasting all the test methods.
     */
    enum ExecutionMode {
        SYNC {
            public void checkExecutionMode() {
                assertFalse(ThreadExecutor.startedCurrentThread());
                assertNull(ForkJoinTask.getPool());
            }

            public CompletableFuture<Void> runAsync(Runnable a) {
                throw new UnsupportedOperationException();
            }

            public <U> CompletableFuture<U> supplyAsync(Supplier<U> a) {
                throw new UnsupportedOperationException();
            }

            public <T> CompletableFuture<Void> thenRun
                    (CompletableFuture<T> f, Runnable a) {
                return f.thenRun(a);
            }

            public <T> CompletableFuture<Void> thenAccept
                    (CompletableFuture<T> f, Consumer<? super T> a) {
                return f.thenAccept(a);
            }

            public <T, U> CompletableFuture<U> thenApply
                    (CompletableFuture<T> f, Function<? super T, U> a) {
                return f.thenApply(a);
            }

            public <T, U> CompletableFuture<U> thenCompose
                    (CompletableFuture<T> f,
                     Function<? super T, ? extends CompletionStage<U>> a) {
                return f.thenCompose(a);
            }

            public <T, U> CompletableFuture<U> handle
                    (CompletableFuture<T> f,
                     BiFunction<? super T, Throwable, ? extends U> a) {
                return f.handle(a);
            }

            public <T> CompletableFuture<T> whenComplete
                    (CompletableFuture<T> f,
                     BiConsumer<? super T, ? super Throwable> a) {
                return f.whenComplete(a);
            }

            public <T, U> CompletableFuture<Void> runAfterBoth
                    (CompletableFuture<T> f, CompletableFuture<U> g, Runnable a) {
                return f.runAfterBoth(g, a);
            }

            public <T, U> CompletableFuture<Void> thenAcceptBoth
                    (CompletableFuture<T> f,
                     CompletionStage<? extends U> g,
                     BiConsumer<? super T, ? super U> a) {
                return f.thenAcceptBoth(g, a);
            }

            public <T, U, V> CompletableFuture<V> thenCombine
                    (CompletableFuture<T> f,
                     CompletionStage<? extends U> g,
                     BiFunction<? super T, ? super U, ? extends V> a) {
                return f.thenCombine(g, a);
            }

            public <T> CompletableFuture<Void> runAfterEither
                    (CompletableFuture<T> f,
                     CompletionStage<?> g,
                     java.lang.Runnable a) {
                return f.runAfterEither(g, a);
            }

            public <T> CompletableFuture<Void> acceptEither
                    (CompletableFuture<T> f,
                     CompletionStage<? extends T> g,
                     Consumer<? super T> a) {
                return f.acceptEither(g, a);
            }

            public <T, U> CompletableFuture<U> applyToEither
                    (CompletableFuture<T> f,
                     CompletionStage<? extends T> g,
                     Function<? super T, U> a) {
                return f.applyToEither(g, a);
            }
        },

        ASYNC {
            public void checkExecutionMode() {
                assertEquals(defaultExecutorIsCommonPool,
                        (ForkJoinPool.commonPool() == ForkJoinTask.getPool()));
            }

            public CompletableFuture<Void> runAsync(Runnable a) {
                return CompletableFuture.runAsync(a);
            }

            public <U> CompletableFuture<U> supplyAsync(Supplier<U> a) {
                return CompletableFuture.supplyAsync(a);
            }

            public <T> CompletableFuture<Void> thenRun
                    (CompletableFuture<T> f, Runnable a) {
                return f.thenRunAsync(a);
            }

            public <T> CompletableFuture<Void> thenAccept
                    (CompletableFuture<T> f, Consumer<? super T> a) {
                return f.thenAcceptAsync(a);
            }

            public <T, U> CompletableFuture<U> thenApply
                    (CompletableFuture<T> f, Function<? super T, U> a) {
                return f.thenApplyAsync(a);
            }

            public <T, U> CompletableFuture<U> thenCompose
                    (CompletableFuture<T> f,
                     Function<? super T, ? extends CompletionStage<U>> a) {
                return f.thenComposeAsync(a);
            }

            public <T, U> CompletableFuture<U> handle
                    (CompletableFuture<T> f,
                     BiFunction<? super T, Throwable, ? extends U> a) {
                return f.handleAsync(a);
            }

            public <T> CompletableFuture<T> whenComplete
                    (CompletableFuture<T> f,
                     BiConsumer<? super T, ? super Throwable> a) {
                return f.whenCompleteAsync(a);
            }

            public <T, U> CompletableFuture<Void> runAfterBoth
                    (CompletableFuture<T> f, CompletableFuture<U> g, Runnable a) {
                return f.runAfterBothAsync(g, a);
            }

            public <T, U> CompletableFuture<Void> thenAcceptBoth
                    (CompletableFuture<T> f,
                     CompletionStage<? extends U> g,
                     BiConsumer<? super T, ? super U> a) {
                return f.thenAcceptBothAsync(g, a);
            }

            public <T, U, V> CompletableFuture<V> thenCombine
                    (CompletableFuture<T> f,
                     CompletionStage<? extends U> g,
                     BiFunction<? super T, ? super U, ? extends V> a) {
                return f.thenCombineAsync(g, a);
            }

            public <T> CompletableFuture<Void> runAfterEither
                    (CompletableFuture<T> f,
                     CompletionStage<?> g,
                     java.lang.Runnable a) {
                return f.runAfterEitherAsync(g, a);
            }

            public <T> CompletableFuture<Void> acceptEither
                    (CompletableFuture<T> f,
                     CompletionStage<? extends T> g,
                     Consumer<? super T> a) {
                return f.acceptEitherAsync(g, a);
            }

            public <T, U> CompletableFuture<U> applyToEither
                    (CompletableFuture<T> f,
                     CompletionStage<? extends T> g,
                     Function<? super T, U> a) {
                return f.applyToEitherAsync(g, a);
            }
        },

        EXECUTOR {
            public void checkExecutionMode() {
                assertTrue(ThreadExecutor.startedCurrentThread());
            }

            public CompletableFuture<Void> runAsync(Runnable a) {
                return CompletableFuture.runAsync(a, new ThreadExecutor());
            }

            public <U> CompletableFuture<U> supplyAsync(Supplier<U> a) {
                return CompletableFuture.supplyAsync(a, new ThreadExecutor());
            }

            public <T> CompletableFuture<Void> thenRun
                    (CompletableFuture<T> f, Runnable a) {
                return f.thenRunAsync(a, new ThreadExecutor());
            }

            public <T> CompletableFuture<Void> thenAccept
                    (CompletableFuture<T> f, Consumer<? super T> a) {
                return f.thenAcceptAsync(a, new ThreadExecutor());
            }

            public <T, U> CompletableFuture<U> thenApply
                    (CompletableFuture<T> f, Function<? super T, U> a) {
                return f.thenApplyAsync(a, new ThreadExecutor());
            }

            public <T, U> CompletableFuture<U> thenCompose
                    (CompletableFuture<T> f,
                     Function<? super T, ? extends CompletionStage<U>> a) {
                return f.thenComposeAsync(a, new ThreadExecutor());
            }

            public <T, U> CompletableFuture<U> handle
                    (CompletableFuture<T> f,
                     BiFunction<? super T, Throwable, ? extends U> a) {
                return f.handleAsync(a, new ThreadExecutor());
            }

            public <T> CompletableFuture<T> whenComplete
                    (CompletableFuture<T> f,
                     BiConsumer<? super T, ? super Throwable> a) {
                return f.whenCompleteAsync(a, new ThreadExecutor());
            }

            public <T, U> CompletableFuture<Void> runAfterBoth
                    (CompletableFuture<T> f, CompletableFuture<U> g, Runnable a) {
                return f.runAfterBothAsync(g, a, new ThreadExecutor());
            }

            public <T, U> CompletableFuture<Void> thenAcceptBoth
                    (CompletableFuture<T> f,
                     CompletionStage<? extends U> g,
                     BiConsumer<? super T, ? super U> a) {
                return f.thenAcceptBothAsync(g, a, new ThreadExecutor());
            }

            public <T, U, V> CompletableFuture<V> thenCombine
                    (CompletableFuture<T> f,
                     CompletionStage<? extends U> g,
                     BiFunction<? super T, ? super U, ? extends V> a) {
                return f.thenCombineAsync(g, a, new ThreadExecutor());
            }

            public <T> CompletableFuture<Void> runAfterEither
                    (CompletableFuture<T> f,
                     CompletionStage<?> g,
                     java.lang.Runnable a) {
                return f.runAfterEitherAsync(g, a, new ThreadExecutor());
            }

            public <T> CompletableFuture<Void> acceptEither
                    (CompletableFuture<T> f,
                     CompletionStage<? extends T> g,
                     Consumer<? super T> a) {
                return f.acceptEitherAsync(g, a, new ThreadExecutor());
            }

            public <T, U> CompletableFuture<U> applyToEither
                    (CompletableFuture<T> f,
                     CompletionStage<? extends T> g,
                     Function<? super T, U> a) {
                return f.applyToEitherAsync(g, a, new ThreadExecutor());
            }
        };

        public abstract void checkExecutionMode();

        public abstract CompletableFuture<Void> runAsync(Runnable a);

        public abstract <U> CompletableFuture<U> supplyAsync(Supplier<U> a);

        public abstract <T> CompletableFuture<Void> thenRun
                (CompletableFuture<T> f, Runnable a);

        public abstract <T> CompletableFuture<Void> thenAccept
                (CompletableFuture<T> f, Consumer<? super T> a);

        public abstract <T, U> CompletableFuture<U> thenApply
                (CompletableFuture<T> f, Function<? super T, U> a);

        public abstract <T, U> CompletableFuture<U> thenCompose
                (CompletableFuture<T> f,
                 Function<? super T, ? extends CompletionStage<U>> a);

        public abstract <T, U> CompletableFuture<U> handle
                (CompletableFuture<T> f,
                 BiFunction<? super T, Throwable, ? extends U> a);

        public abstract <T> CompletableFuture<T> whenComplete
                (CompletableFuture<T> f,
                 BiConsumer<? super T, ? super Throwable> a);

        public abstract <T, U> CompletableFuture<Void> runAfterBoth
                (CompletableFuture<T> f, CompletableFuture<U> g, Runnable a);

        public abstract <T, U> CompletableFuture<Void> thenAcceptBoth
                (CompletableFuture<T> f,
                 CompletionStage<? extends U> g,
                 BiConsumer<? super T, ? super U> a);

        public abstract <T, U, V> CompletableFuture<V> thenCombine
                (CompletableFuture<T> f,
                 CompletionStage<? extends U> g,
                 BiFunction<? super T, ? super U, ? extends V> a);

        public abstract <T> CompletableFuture<Void> runAfterEither
                (CompletableFuture<T> f,
                 CompletionStage<?> g,
                 java.lang.Runnable a);

        public abstract <T> CompletableFuture<Void> acceptEither
                (CompletableFuture<T> f,
                 CompletionStage<? extends T> g,
                 Consumer<? super T> a);

        public abstract <T, U> CompletableFuture<U> applyToEither
                (CompletableFuture<T> f,
                 CompletionStage<? extends T> g,
                 Function<? super T, U> a);
    }

    // Used for explicit executor tests
    static final class ThreadExecutor implements Executor {
        final AtomicInteger count = new AtomicInteger(0);
        static final ThreadGroup tg = new ThreadGroup("ThreadExecutor");

        static boolean startedCurrentThread() {
            return Thread.currentThread().getThreadGroup() == tg;
        }

        public void execute(Runnable r) {
            count.getAndIncrement();
            new Thread(tg, r).start();
        }
    }

    abstract class CheckedAction {
        int invocationCount = 0;
        final ExecutionMode m;

        CheckedAction(ExecutionMode m) {
            this.m = m;
        }

        void invoked() {
            m.checkExecutionMode();
            assertEquals(0, invocationCount++);
        }

        void assertNotInvoked() {
            assertEquals(0, invocationCount);
        }

        void assertInvoked() {
            assertEquals(1, invocationCount);
        }
    }

    abstract class CheckedIntegerAction extends CheckedAction {
        Integer value;

        CheckedIntegerAction(ExecutionMode m) {
            super(m);
        }

        void assertValue(Integer expected) {
            assertInvoked();
            assertEquals(expected, value);
        }
    }

    class IntegerSupplier extends CheckedAction
            implements Supplier<Integer> {
        final Integer value;

        IntegerSupplier(ExecutionMode m, Integer value) {
            super(m);
            this.value = value;
        }

        public Integer get() {
            invoked();
            return value;
        }
    }

    // A function that handles and produces null values as well.
    static Integer inc(Integer x) {
        return (x == null) ? null : x + 1;
    }

    class NoopConsumer extends CheckedIntegerAction
            implements Consumer<Integer> {
        NoopConsumer(ExecutionMode m) {
            super(m);
        }

        public void accept(Integer x) {
            invoked();
            value = x;
        }
    }

    class IncFunction extends CheckedIntegerAction
            implements Function<Integer, Integer> {
        IncFunction(ExecutionMode m) {
            super(m);
        }

        public Integer apply(Integer x) {
            invoked();
            return value = inc(x);
        }
    }

    // Choose non-commutative actions for better coverage
    // A non-commutative function that handles and produces null values as well.
    static Integer subtract(Integer x, Integer y) {
        return (x == null && y == null) ? null :
                ((x == null) ? 42 : x.intValue())
                        - ((y == null) ? 99 : y.intValue());
    }

    class SubtractAction extends CheckedIntegerAction
            implements BiConsumer<Integer, Integer> {
        SubtractAction(ExecutionMode m) {
            super(m);
        }

        public void accept(Integer x, Integer y) {
            invoked();
            value = subtract(x, y);
        }
    }

    class SubtractFunction extends CheckedIntegerAction
            implements BiFunction<Integer, Integer, Integer> {
        SubtractFunction(ExecutionMode m) {
            super(m);
        }

        public Integer apply(Integer x, Integer y) {
            invoked();
            return value = subtract(x, y);
        }
    }

    class Noop extends CheckedAction implements Runnable {
        Noop(ExecutionMode m) {
            super(m);
        }

        public void run() {
            invoked();
        }
    }

    class FailingSupplier extends CheckedAction
            implements Supplier<Integer> {
        FailingSupplier(ExecutionMode m) {
            super(m);
        }

        public Integer get() {
            invoked();
            throw new CFException();
        }
    }

    class FailingConsumer extends CheckedIntegerAction
            implements Consumer<Integer> {
        FailingConsumer(ExecutionMode m) {
            super(m);
        }

        public void accept(Integer x) {
            invoked();
            value = x;
            throw new CFException();
        }
    }

    class FailingBiConsumer extends CheckedIntegerAction
            implements BiConsumer<Integer, Integer> {
        FailingBiConsumer(ExecutionMode m) {
            super(m);
        }

        public void accept(Integer x, Integer y) {
            invoked();
            value = subtract(x, y);
            throw new CFException();
        }
    }

    class FailingFunction extends CheckedIntegerAction
            implements Function<Integer, Integer> {
        FailingFunction(ExecutionMode m) {
            super(m);
        }

        public Integer apply(Integer x) {
            invoked();
            value = x;
            throw new CFException();
        }
    }

    class FailingBiFunction extends CheckedIntegerAction
            implements BiFunction<Integer, Integer, Integer> {
        FailingBiFunction(ExecutionMode m) {
            super(m);
        }

        public Integer apply(Integer x, Integer y) {
            invoked();
            value = subtract(x, y);
            throw new CFException();
        }
    }

    class FailingRunnable extends CheckedAction implements Runnable {
        FailingRunnable(ExecutionMode m) {
            super(m);
        }

        public void run() {
            invoked();
            throw new CFException();
        }
    }

    class CompletableFutureInc extends CheckedIntegerAction
            implements Function<Integer, CompletableFuture<Integer>> {
        CompletableFutureInc(ExecutionMode m) {
            super(m);
        }

        public CompletableFuture<Integer> apply(Integer x) {
            invoked();
            value = x;
            CompletableFuture<Integer> f = new CompletableFuture<>();
            assertTrue(f.complete(inc(x)));
            return f;
        }
    }

    class FailingCompletableFutureFunction extends CheckedIntegerAction
            implements Function<Integer, CompletableFuture<Integer>> {
        FailingCompletableFutureFunction(ExecutionMode m) {
            super(m);
        }

        public CompletableFuture<Integer> apply(Integer x) {
            invoked();
            value = x;
            throw new CFException();
        }
    }

    static final boolean defaultExecutorIsCommonPool
            = ForkJoinPool.getCommonPoolParallelism() > 1;

    @Test
    public void testConstructor() {
        CompletableFuture<Integer> f = new CompletableFuture<>();
        checkIncomplete(f);
    }

    /**
     * complete completes normally, as indicated by methods isDone,
     * isCancelled, join, get, and getNow
     */
    @Test
    public void testComplete() {
        for (Integer v1 : new Integer[]{1, null}) {
            CompletableFuture<Integer> f = new CompletableFuture<>();
            checkIncomplete(f);
            assertTrue(f.complete(v1));
            assertFalse(f.complete(v1));
            checkCompletedNormally(f, v1);
        }
    }

    /**
     * completeExceptionally completes exceptionally, as indicated by
     * methods isDone, isCancelled, join, get, and getNow
     */
    @Test
    public void testCompleteExceptionally() {
        CompletableFuture<Integer> f = new CompletableFuture<>();
        CFException ex = new CFException();
        checkIncomplete(f);
        f.completeExceptionally(ex);
        checkCompletedExceptionally(f, ex);
    }

    /**
     * cancel completes exceptionally and reports cancelled, as indicated by
     * methods isDone, isCancelled, join, get, and getNow
     */
    @Test
    public void testCancel() {
        for (boolean mayInterruptIfRunning : new boolean[]{true, false}) {
            CompletableFuture<Integer> f = new CompletableFuture<>();
            checkIncomplete(f);
            assertTrue(f.cancel(mayInterruptIfRunning));
            assertTrue(f.cancel(mayInterruptIfRunning));
            assertTrue(f.cancel(!mayInterruptIfRunning));
            checkCancelled(f);
        }
    }


    /**
     * obtrudeValue forces completion with given value
     */
    @Test
    public void testObtrudeValue() {
        CompletableFuture<Integer> f = new CompletableFuture<>();
        checkIncomplete(f);
        assertTrue(f.complete(one));
        checkCompletedNormally(f, one);
        f.obtrudeValue(three);
        checkCompletedNormally(f, three);
        f.obtrudeValue(two);
        checkCompletedNormally(f, two);
        f = new CompletableFuture<>();
        f.obtrudeValue(three);
        checkCompletedNormally(f, three);
        f.obtrudeValue(null);
        checkCompletedNormally(f, null);
        f = new CompletableFuture<>();
        f.completeExceptionally(new CFException());
        f.obtrudeValue(four);
        checkCompletedNormally(f, four);
    }

    /**
     * obtrudeException forces completion with given exception
     */
    @Test
    public void testObtrudeException() {
        for (Integer v1 : new Integer[]{1, null}) {
            CFException ex;
            CompletableFuture<Integer> f;

            f = new CompletableFuture<>();
            assertTrue(f.complete(v1));
            for (int i = 0; i < 2; i++) {
                f.obtrudeException(ex = new CFException());
                checkCompletedExceptionally(f, ex);
            }

            f = new CompletableFuture<>();
            for (int i = 0; i < 2; i++) {
                f.obtrudeException(ex = new CFException());
                checkCompletedExceptionally(f, ex);
            }

            f = new CompletableFuture<>();
            f.completeExceptionally(ex = new CFException());
            f.obtrudeValue(v1);
            checkCompletedNormally(f, v1);
            f.obtrudeException(ex = new CFException());
            checkCompletedExceptionally(f, ex);
            f.completeExceptionally(new CFException());
            checkCompletedExceptionally(f, ex);
            assertFalse(f.complete(v1));
            checkCompletedExceptionally(f, ex);
        }
    }

    /**
     * getNumberOfDependents returns number of dependent tasks
     */
    @Test
    public void testGetNumberOfDependents() {
        for (ExecutionMode m : ExecutionMode.values())
            for (Integer v1 : new Integer[]{1, null}) {
                CompletableFuture<Integer> f = new CompletableFuture<>();
                assertEquals(0, f.getNumberOfDependents());
                final CompletableFuture<Void> g = m.thenRun(f, new Noop(m));
                assertEquals(1, f.getNumberOfDependents());
                assertEquals(0, g.getNumberOfDependents());
                final CompletableFuture<Void> h = m.thenRun(f, new Noop(m));
                assertEquals(2, f.getNumberOfDependents());
                assertEquals(0, h.getNumberOfDependents());
                assertTrue(f.complete(v1));
                checkCompletedNormally(g, null);
                checkCompletedNormally(h, null);
                assertEquals(0, f.getNumberOfDependents());
                assertEquals(0, g.getNumberOfDependents());
                assertEquals(0, h.getNumberOfDependents());
            }
    }

    /**
     * toString indicates current completion state
     */
    @Test
    public void testToString() {
        CompletableFuture<String> f;

        f = new CompletableFuture<>();
        assertTrue(f.toString().contains("[Not completed]"));

        assertTrue(f.complete("foo"));
        assertTrue(f.toString().contains("[Completed normally]"));

        f = new CompletableFuture<>();
        assertTrue(f.completeExceptionally(new IndexOutOfBoundsException()));
        assertTrue(f.toString().contains("[Completed exceptionally]"));

        for (boolean mayInterruptIfRunning : new boolean[]{true, false}) {
            f = new CompletableFuture<>();
            assertTrue(f.cancel(mayInterruptIfRunning));
            assertTrue(f.toString().contains("[Completed exceptionally]"));
        }
    }

    /**
     * completedFuture returns a completed CompletableFuture with given value
     */
    @Test
    public void testCompletedFuture() {
        CompletableFuture<String> f = CompletableFuture.completedFuture("test");
        checkCompletedNormally(f, "test");
    }

    /**
     * exceptionally action is not invoked when source completes
     * normally, and source result is propagated
     */
    @Test
    public void testExceptionally_normalCompletion() {
        for (boolean createIncomplete : new boolean[]{true, false})
            for (Integer v1 : new Integer[]{1, null}) {
                final AtomicInteger a = new AtomicInteger(0);
                final CompletableFuture<Integer> f = new CompletableFuture<>();
                if (!createIncomplete) assertTrue(f.complete(v1));
                final CompletableFuture<Integer> g = f.exceptionally
                        ((Throwable t) -> {
                            a.getAndIncrement();
                            threadFail("should not be called");
                            return null;            // unreached
                        });
                if (createIncomplete) assertTrue(f.complete(v1));

                checkCompletedNormally(g, v1);
                checkCompletedNormally(f, v1);
                assertEquals(0, a.get());
            }
    }

    /**
     * exceptionally action completes with function value on source
     * exception
     */
    @Test
    public void testExceptionally_exceptionalCompletion() {
        for (boolean createIncomplete : new boolean[]{true, false})
            for (Integer v1 : new Integer[]{1, null}) {
                final AtomicInteger a = new AtomicInteger(0);
                final CFException ex = new CFException();
                final CompletableFuture<Integer> f = new CompletableFuture<>();
                if (!createIncomplete) f.completeExceptionally(ex);
                final CompletableFuture<Integer> g = f.exceptionally
                        ((Throwable t) -> {
                            ExecutionMode.SYNC.checkExecutionMode();
                            threadAssertSame(t, ex);
                            a.getAndIncrement();
                            return v1;
                        });
                if (createIncomplete) f.completeExceptionally(ex);

                checkCompletedNormally(g, v1);
                assertEquals(1, a.get());
            }
    }

    /**
     * If an "exceptionally action" throws an exception, it completes
     * exceptionally with that exception
     */
    @Test
    public void testExceptionally_exceptionalCompletionActionFailed() {
        for (boolean createIncomplete : new boolean[]{true, false}) {
            final AtomicInteger a = new AtomicInteger(0);
            final CFException ex1 = new CFException();
            final CFException ex2 = new CFException();
            final CompletableFuture<Integer> f = new CompletableFuture<>();
            if (!createIncomplete) f.completeExceptionally(ex1);
            final CompletableFuture<Integer> g = f.exceptionally
                    ((Throwable t) -> {
                        ExecutionMode.SYNC.checkExecutionMode();
                        threadAssertSame(t, ex1);
                        a.getAndIncrement();
                        throw ex2;
                    });
            if (createIncomplete) f.completeExceptionally(ex1);

            checkCompletedWithWrappedException(g, ex2);
            checkCompletedExceptionally(f, ex1);
            assertEquals(1, a.get());
        }
    }

    /**
     * whenComplete action executes on normal completion, propagating
     * source result.
     */
    @Test
    public void testWhenComplete_normalCompletion() {
        for (ExecutionMode m : ExecutionMode.values())
            for (boolean createIncomplete : new boolean[]{true, false})
                for (Integer v1 : new Integer[]{1, null}) {
                    final AtomicInteger a = new AtomicInteger(0);
                    final CompletableFuture<Integer> f = new CompletableFuture<>();
                    if (!createIncomplete) assertTrue(f.complete(v1));
                    final CompletableFuture<Integer> g = m.whenComplete
                            (f,
                                    (Integer result, Throwable t) -> {
                                        m.checkExecutionMode();
                                        threadAssertSame(result, v1);
                                        threadAssertNull(t);
                                        a.getAndIncrement();
                                    });
                    if (createIncomplete) assertTrue(f.complete(v1));

                    checkCompletedNormally(g, v1);
                    checkCompletedNormally(f, v1);
                    assertEquals(1, a.get());
                }
    }

    /**
     * whenComplete action executes on exceptional completion, propagating
     * source result.
     */
    @Test
    public void testWhenComplete_exceptionalCompletion() {
        for (ExecutionMode m : ExecutionMode.values())
            for (boolean createIncomplete : new boolean[]{true, false}) {
                final AtomicInteger a = new AtomicInteger(0);
                final CFException ex = new CFException();
                final CompletableFuture<Integer> f = new CompletableFuture<>();
                if (!createIncomplete) f.completeExceptionally(ex);
                final CompletableFuture<Integer> g = m.whenComplete
                        (f,
                                (Integer result, Throwable t) -> {
                                    m.checkExecutionMode();
                                    threadAssertNull(result);
                                    threadAssertSame(t, ex);
                                    a.getAndIncrement();
                                });
                if (createIncomplete) f.completeExceptionally(ex);

                checkCompletedWithWrappedException(g, ex);
                checkCompletedExceptionally(f, ex);
                assertEquals(1, a.get());
            }
    }

    /**
     * whenComplete action executes on cancelled source, propagating
     * CancellationException.
     */
    @Test
    public void testWhenComplete_sourceCancelled() {
        for (ExecutionMode m : ExecutionMode.values())
            for (boolean mayInterruptIfRunning : new boolean[]{true, false})
                for (boolean createIncomplete : new boolean[]{true, false}) {
                    final AtomicInteger a = new AtomicInteger(0);
                    final CompletableFuture<Integer> f = new CompletableFuture<>();
                    if (!createIncomplete) assertTrue(f.cancel(mayInterruptIfRunning));
                    final CompletableFuture<Integer> g = m.whenComplete
                            (f,
                                    (Integer result, Throwable t) -> {
                                        m.checkExecutionMode();
                                        threadAssertNull(result);
                                        threadAssertTrue(t instanceof CancellationException);
                                        a.getAndIncrement();
                                    });
                    if (createIncomplete) assertTrue(f.cancel(mayInterruptIfRunning));

                    checkCompletedWithWrappedCancellationException(g);
                    checkCancelled(f);
                    assertEquals(1, a.get());
                }
    }

    /**
     * If a whenComplete action throws an exception when triggered by
     * a normal completion, it completes exceptionally
     */
    @Test
    public void testWhenComplete_sourceCompletedNormallyActionFailed() {
        for (boolean createIncomplete : new boolean[]{true, false})
            for (ExecutionMode m : ExecutionMode.values())
                for (Integer v1 : new Integer[]{1, null}) {
                    final AtomicInteger a = new AtomicInteger(0);
                    final CFException ex = new CFException();
                    final CompletableFuture<Integer> f = new CompletableFuture<>();
                    if (!createIncomplete) assertTrue(f.complete(v1));
                    final CompletableFuture<Integer> g = m.whenComplete
                            (f,
                                    (Integer result, Throwable t) -> {
                                        m.checkExecutionMode();
                                        threadAssertSame(result, v1);
                                        threadAssertNull(t);
                                        a.getAndIncrement();
                                        throw ex;
                                    });
                    if (createIncomplete) assertTrue(f.complete(v1));

                    checkCompletedWithWrappedException(g, ex);
                    checkCompletedNormally(f, v1);
                    assertEquals(1, a.get());
                }
    }

    /**
     * If a whenComplete action throws an exception when triggered by
     * a source completion that also throws an exception, the source
     * exception takes precedence (unlike handle)
     */
    @Test
    public void testWhenComplete_sourceFailedActionFailed() {
        for (boolean createIncomplete : new boolean[]{true, false})
            for (ExecutionMode m : ExecutionMode.values()) {
                final AtomicInteger a = new AtomicInteger(0);
                final CFException ex1 = new CFException();
                final CFException ex2 = new CFException();
                final CompletableFuture<Integer> f = new CompletableFuture<>();

                if (!createIncomplete) f.completeExceptionally(ex1);
                final CompletableFuture<Integer> g = m.whenComplete
                        (f,
                                (Integer result, Throwable t) -> {
                                    m.checkExecutionMode();
                                    threadAssertSame(t, ex1);
                                    threadAssertNull(result);
                                    a.getAndIncrement();
                                    throw ex2;
                                });
                if (createIncomplete) f.completeExceptionally(ex1);

                checkCompletedWithWrappedException(g, ex1);
                checkCompletedExceptionally(f, ex1);
//                if (testImplementationDetails) {
//                    assertEquals(1, ex1.getSuppressed().length);
//                    assertSame(ex2, ex1.getSuppressed()[0]);
//                }
                assertEquals(1, a.get());
            }
    }

    /**
     * runAsync completes after running Runnable
     */
    @Test
    public void testRunAsync_normalCompletion() {
        ExecutionMode[] executionModes = {
                ExecutionMode.ASYNC,
                ExecutionMode.EXECUTOR,
        };
        for (ExecutionMode m : executionModes) {
            final Noop r = new Noop(m);
            final CompletableFuture<Void> f = m.runAsync(r);
            assertNull(f.join());
            checkCompletedNormally(f, null);
            r.assertInvoked();
        }
    }

    /**
     * thenRun result completes normally after normal completion of source
     */
    @Test
    public void testThenRun_normalCompletion() {
        for (ExecutionMode m : ExecutionMode.values())
            for (Integer v1 : new Integer[]{1, null}) {
                final CompletableFuture<Integer> f = new CompletableFuture<>();
                final Noop[] rs = new Noop[6];
                for (int i = 0; i < rs.length; i++) rs[i] = new Noop(m);

                final CompletableFuture<Void> h0 = m.thenRun(f, rs[0]);
                final CompletableFuture<Void> h1 = m.runAfterBoth(f, f, rs[1]);
                final CompletableFuture<Void> h2 = m.runAfterEither(f, f, rs[2]);
                checkIncomplete(h0);
                checkIncomplete(h1);
                checkIncomplete(h2);
                assertTrue(f.complete(v1));
                final CompletableFuture<Void> h3 = m.thenRun(f, rs[3]);
                final CompletableFuture<Void> h4 = m.runAfterBoth(f, f, rs[4]);
                final CompletableFuture<Void> h5 = m.runAfterEither(f, f, rs[5]);

                checkCompletedNormally(h0, null);
                checkCompletedNormally(h1, null);
                checkCompletedNormally(h2, null);
                checkCompletedNormally(h3, null);
                checkCompletedNormally(h4, null);
                checkCompletedNormally(h5, null);
                checkCompletedNormally(f, v1);
                for (Noop r : rs) r.assertInvoked();
            }
    }

    /**
     * allOf(no component futures) returns a future completed normally
     * with the value null
     */
    @Test
    public void testAllOf_empty() throws Exception {
        CompletableFuture<Void> f = CompletableFuture.allOf();
        checkCompletedNormally(f, null);
    }

    @Test
    public void testAllOf_backwards() throws Exception {
        for (int k = 1; k < 10; k++) {
            CompletableFuture<Integer>[] fs
                    = (CompletableFuture<Integer>[]) new CompletableFuture[k];
            for (int i = 0; i < k; i++)
                fs[i] = new CompletableFuture<>();
            CompletableFuture<Void> f = CompletableFuture.allOf(fs);
            for (int i = k - 1; i >= 0; i--) {
                checkIncomplete(f);
                checkIncomplete(CompletableFuture.allOf(fs));
                fs[i].complete(one);
            }
            checkCompletedNormally(f, null);
            checkCompletedNormally(CompletableFuture.allOf(fs), null);
        }
    }

    @Test
    public void testAllOf_exceptional() throws Exception {
        for (int k = 1; k < 10; k++) {
            CompletableFuture<Integer>[] fs
                    = (CompletableFuture<Integer>[]) new CompletableFuture[k];
            CFException ex = new CFException();
            for (int i = 0; i < k; i++)
                fs[i] = new CompletableFuture<>();
            CompletableFuture<Void> f = CompletableFuture.allOf(fs);
            for (int i = 0; i < k; i++) {
                checkIncomplete(f);
                checkIncomplete(CompletableFuture.allOf(fs));
                if (i != k / 2) {
                    fs[i].complete(i);
                    checkCompletedNormally(fs[i], i);
                } else {
                    fs[i].completeExceptionally(ex);
                    checkCompletedExceptionally(fs[i], ex);
                }
            }
            checkCompletedWithWrappedException(f, ex);
            checkCompletedWithWrappedException(CompletableFuture.allOf(fs), ex);
        }
    }

    /**
     * anyOf(no component futures) returns an incomplete future
     */
    @Test
    public void testAnyOf_empty() throws Exception {
        for (Integer v1 : new Integer[]{1, null}) {
            CompletableFuture<Object> f = CompletableFuture.anyOf();
            checkIncomplete(f);

            f.complete(v1);
            checkCompletedNormally(f, v1);
        }
    }

}
