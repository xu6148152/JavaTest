package com.zepp.www.rxjavaexample;

//  Created by xubinggui on 27/12/2016.
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

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.runners.MockitoJUnitRunner;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.Subscription;
import rx.observables.BlockingObservable;
import rx.observables.ConnectableObservable;
import rx.observables.MathObservable;
import rx.observers.TestSubscriber;
import rx.schedulers.Schedulers;
import rx.schedulers.TestScheduler;
import rx.subjects.AsyncSubject;
import rx.subjects.BehaviorSubject;
import rx.subjects.PublishSubject;
import rx.subjects.ReplaySubject;
import rx.subjects.TestSubject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class) public class RxJava1UnitTest {
    static Integer[] integers = new Integer[9];
    static Integer[] integers1 = new Integer[9];

    static {
        for (int i = 0; i < integers.length; i++) {
            integers[i] = i;
            integers1[i] = -i;
        }
    }

    @Mock Context mContext;

    @Before public void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test public void testCreateObservable() {
        //Observable.create(new SyncOnSubscribe<Object, Object>() {
        //    @Override protected Object generateState() {
        //        return null;
        //    }
        //
        //    @Override protected Object next(Object state, Observer<? super Object> observer) {
        //        observer.onNext(state);
        //        observer.onCompleted();
        //        return state;
        //    }
        //}).subscribe(mSubscriber);
        Observable.create(subscriber -> {
            System.out.println("subscriber");
            if (!subscriber.isUnsubscribed()) {
                subscriber.onNext(subscriber);
                subscriber.onCompleted();
            }
        }).subscribe(mSubscriber);
    }

    @Test public void testDeferObservable() {
        //SomeType someType  = new SomeType();
        //someType.setValue("Some Value");
        //someType.valueObservable().subscribe(System.out::println);
        Observable.defer(Observable::empty).subscribe(new Subscriber<Object>() {
            @Override public void onCompleted() {
                System.out.println("onCompleted");
            }

            @Override public void onError(Throwable e) {
                System.out.println("onError");
            }

            @Override public void onNext(Object o) {
                System.out.println("onNext");
            }
        });
    }

    @Test public void testFromFuture() {
        Observable.from(Executors.newSingleThreadExecutor().submit(() -> {
            System.out.println("before sleep");
            Thread.sleep(5000);
            return true;
        })).subscribe(mSubscriber);
        mSubscriber.unsubscribe();
    }

    @Test public void testTimeInterval() {
        Observable.from(integers).timeInterval().subscribe(mSubscriber);
    }

    @Test public void testTimeOut() {
        Observable.from(integers).timeout(integer -> Observable.from(integers1)).subscribe(mSubscriber);
    }

    @Test public void testTimeStamp() {
        Observable.from(integers).timestamp().subscribe(mSubscriber);
    }

    @Test public void testAll() {
        Observable.from(integers).all(integer -> integer < 5).subscribe(mSubscriber);
    }

    @Test public void testContain() {
        Observable.from(integers).contains(1).subscribe(mSubscriber);
    }

    @Test public void testDefaultEmpty() {
        Observable.empty().defaultIfEmpty(11).subscribe(mSubscriber);
    }

    @Test public void testSequenceEqual() {
        Observable.sequenceEqual(Observable.from(integers), Observable.from(integers)).subscribe(mSubscriber);
    }

    @Test public void testSkipUtil() {
        Observable.from(integers).skipUntil(Observable.from(integers1).delay(1, TimeUnit.SECONDS)).subscribe(mSubscriber);
    }

    @Test public void testSkipWhile() {
        Observable.from(integers).skipWhile(integer -> integer >= 0).subscribe(mSubscriber);
    }

    @Test public void testAverage() {
        MathObservable.from(Observable.from(integers)).averageInteger(integer -> integer).forEach(System.out::println);
    }

    @Test public void testConcat() {
        Observable.concat(Observable.from(integers), Observable.from(integers1)).forEach(System.out::println);
    }

    @Test public void testCount() {
        assertEquals(Integer.valueOf(integers.length), Observable.from(integers).count().toBlocking().single());
    }

    @Test public void testMax() {
        assertEquals(integers[integers.length - 1], MathObservable.from(Observable.from(integers)).max((o1, o2) -> o1 - o2).toBlocking().single());
    }

    @Test public void testReduce() {
        Observable.from(integers).reduce((integer, integer2) -> integer + integer2).forEach(System.out::println);
    }

    @Test public void testConnect() {
        final AtomicInteger run = new AtomicInteger();

        final ConnectableObservable<Integer> co = Observable.defer(() -> Observable.just(run.incrementAndGet())).publish();

        final Observable<Integer> source = co.autoConnect();

        assertEquals(0, run.get());

        TestSubscriber<Integer> ts1 = TestSubscriber.create();
        source.subscribe(ts1);

        ts1.assertCompleted();
        ts1.assertNoErrors();
        ts1.assertValue(1);

        assertEquals(1, run.get());

        TestSubscriber<Integer> ts2 = TestSubscriber.create();
        source.subscribe(ts2);

        ts2.assertNotCompleted();
        ts2.assertNoErrors();
        ts2.assertNoValues();

        assertEquals(1, run.get());
    }

    @Test public void testPublish() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        ConnectableObservable<String> o = Observable.create(new Observable.OnSubscribe<String>() {

            @Override public void call(final Subscriber<? super String> observer) {
                new Thread(() -> {
                    counter.incrementAndGet();
                    observer.onNext("one");
                    observer.onCompleted();
                }).start();
            }
        }).publish();

        final CountDownLatch countDownLatch = new CountDownLatch(2);
        o.subscribe(v -> {
            assertEquals("one", v);
            countDownLatch.countDown();
        });

        // subscribe again
        o.subscribe(v -> {
            assertEquals("one", v);
            countDownLatch.countDown();
        });

        final Subscription subscription = o.connect();
        try {
            if (!countDownLatch.await(1000, TimeUnit.MILLISECONDS)) {
                fail("subscriptions did not receive values");
            }
            assertEquals(1, counter.get());
        } finally {
            subscription.unsubscribe();
        }
    }

    @Test public void testRefCount() {
        final AtomicInteger subscribeCount = new AtomicInteger();
        final AtomicInteger nextCount = new AtomicInteger();
        final Observable<Integer> observable = Observable.from(integers)
                                                         .doOnSubscribe(subscribeCount::incrementAndGet)
                                                         .doOnNext(l -> nextCount.incrementAndGet())
                                                         .publish()
                                                         .refCount();
        final AtomicInteger receivedCount = new AtomicInteger();
        final Subscription s1 = observable.subscribe(l -> receivedCount.incrementAndGet());
        final Subscription s2 = observable.subscribe();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
        }
        s2.unsubscribe();
        s1.unsubscribe();

        System.out.println("onNext Count: " + nextCount.get());

        assertEquals(nextCount.get(), receivedCount.get() * 2);

        assertEquals(2, subscribeCount.get());
    }

    @Test public void testReplay() throws InterruptedException {
        final AtomicInteger counter = new AtomicInteger();
        Observable<String> o = Observable.create(new Observable.OnSubscribe<String>() {

            @Override public void call(final Subscriber<? super String> observer) {
                new Thread(() -> {
                    counter.incrementAndGet();
                    System.out.println("published observable being executed");
                    observer.onNext("one");
                    observer.onCompleted();
                }).start();
            }
        }).replay().autoConnect();

        // we then expect the following 2 subscriptions to get that same value
        final CountDownLatch latch = new CountDownLatch(2);

        // subscribe once
        o.subscribe(v -> {
            assertEquals("one", v);
            System.out.println("v: " + v);
            latch.countDown();
        });

        // subscribe again
        o.subscribe(v -> {
            assertEquals("one", v);
            System.out.println("v: " + v);
            latch.countDown();
        });

        if (!latch.await(1000, TimeUnit.MILLISECONDS)) {
            fail("subscriptions did not receive values");
        }
        assertEquals(1, counter.get());
    }

    @Test public void testAmb() {
        Observable.amb(Observable.from(integers).delay(2, TimeUnit.SECONDS), Observable.from(integers1).delay(1, TimeUnit.SECONDS))
                  .toBlocking()
                  .forEach(System.out::println);
    }

    @Test public void testUsing() {
        Observable.using(() -> null, o -> null, o -> {

        }).subscribe(mSubscriber);
    }

    @Test public void testRepeat() {
        Observable.range(0, 3).repeat(3).subscribe(mSubscriber);
    }

    @Test public void testBuffer() {
        //Observable.from(integers).buffer(2).subscribe(mSubscriber);
        //System.out.println("buffer count");
        //Observable.from(integers).buffer(() -> null).subscribe(mSubscriber);
        //System.out.println("buffer Func0");
        //Observable.from(integers).buffer(Observable.from(integers)).subscribe(mSubscriber);
        //System.out.println("buffer Observable");
        Observable.from(integers).buffer(1, 3, TimeUnit.SECONDS).subscribe(mSubscriber);
        System.out.println("buffer timespan");
    }

    @Test public void testWindow() {
        Observable.from(integers).window(2).subscribe(mSubscriber);
    }

    @Test public void testDebounce() {
        Observable.from(integers).debounce(10, TimeUnit.SECONDS).subscribe(mSubscriber);
        //Observable.from(integers).debounce(Observable::just).subscribe(mSubscriber);
        //Observable.from(integers).map(integer -> {
        //    try {
        //        Thread.sleep(1000);
        //    } catch (InterruptedException e) {
        //        e.printStackTrace();
        //    }
        //    return integer;
        //}).debounce(1, TimeUnit.SECONDS).subscribe(mSubscriber);
    }

    @Test public void testElementAt() {
        Observable.from(integers).elementAt(5).subscribe(mSubscriber);
    }

    @Test public void testFirst() {
        Observable.from(integers).first(integer -> false).subscribe(mSubscriber);
    }

    @Test public void testIgnoreElements() {
        Observable.from(integers).ignoreElements().subscribe(mSubscriber);
    }

    @Test public void testLast() {
        Observable.from(integers).last().subscribe(mSubscriber);
    }

    @Test public void testSample() {
        Observable.from(integers).sample(1, TimeUnit.SECONDS).subscribe(mSubscriber);
    }

    @Test public void testSkip() {
        Observable.from(integers).skip(5).subscribe(mSubscriber);
    }

    @Test public void testSkipLast() {
        Observable.from(integers).skipLast(5).subscribe(mSubscriber);
    }

    @Test public void testAndThenWhen() {
        Observable one = Observable.interval(1, TimeUnit.SECONDS).take(5);
        Observable two = Observable.interval(250, TimeUnit.MILLISECONDS).take(10);
        Observable three = Observable.interval(150, TimeUnit.MILLISECONDS).take(14);
        //final Pattern3 pattern3 = JoinObservable.from(one).and(two).and(three);
        //JoinObservable.when(pattern3.then((first, second, third) -> first)).toObservable().subscribe(mSubscriber);
    }

    @Test public void testCombineLatest() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        MySubscriber subscriber = new MySubscriber();
        subscriber.setCountDownLatch(countDownLatch);
        Observable.combineLatest(Observable.interval(1, TimeUnit.SECONDS).take(5), Observable.from(integers), (first, second) -> first + second)
                  .subscribe(subscriber);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test public void testJoin() {
        List<String[]> leftList = new ArrayList<>();
        leftList.add(new String[] { "2013-01-01 02:00:00", "Batch1" });
        leftList.add(new String[] { "2013-01-01 03:00:00", "Batch2" });
        leftList.add(new String[] { "2013-01-01 04:00:00", "Batch3" });

        List<String[]> rightList = new ArrayList<>();
        rightList.add(new String[] { "2013-01-01 01:00:00", "Production=2" });
        rightList.add(new String[] { "2013-01-01 02:00:00", "Production=0" });
        rightList.add(new String[] { "2013-01-01 03:00:00", "Production=3" });

        Observable.from(leftList)
                  .groupJoin(Observable.from(rightList), a -> Observable.never(), b -> Observable.never(),
                             (left, obsOfRight) -> Observable.from(left))
                  .subscribe(mSubscriber);
    }

    @Test public void testMerge() {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        MySubscriber subscriber = new MySubscriber();
        subscriber.setCountDownLatch(countDownLatch);
        Observable.merge(Observable.from(integers), Observable.interval(1, TimeUnit.SECONDS).take(5)).subscribe(subscriber);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test public void testStartWith() {
        Observable.from(integers).startWith(100).subscribe(mSubscriber);
    }

    @Test public void testSwitch() {
        //Observable.from(integers).switchIfEmpty(Observable.from(integers1)).subscribe(mSubscriber);
        Observable.from(integers).switchMap(Observable::just).subscribe(mSubscriber);
    }

    @Test public void testLoadThreeDocumentsInParallel() {
        Observable.just(1, 2, 3).subscribe(new Subscriber<Integer>() {

            @Override public void onError(Throwable e) {
                System.err.println("Whoops: " + e.getMessage());
            }

            @Override public void onCompleted() {
                System.out.println("Completed Observable." + ", current thread: " + Thread.currentThread());
            }

            @Override public void onNext(Integer integer) {
                System.out.println("Got: " + integer + ", current thread: " + Thread.currentThread());
            }
        });
    }

    @Test public void testDoOnext() {
        Observable.just(1, 2, 3).doOnNext(integer -> {
            //if (integer.equals(2)) {
            //    throw new RuntimeException("I don't like 2");
            //}
            System.out.println("doOnNext: " + integer);
        }).subscribe(new Subscriber<Integer>() {
            @Override public void onCompleted() {
                System.out.println("Completed Observable.");
            }

            @Override public void onError(Throwable throwable) {
                System.err.println("Whoops: " + throwable.getMessage());
            }

            @Override public void onNext(Integer integer) {
                System.out.println("Got: " + integer);
            }
        });
    }

    @Test public void testTake() {
        Observable.just("The", "Dave", "Brubeck", "Quartet", "Time", "Out").take(1).subscribe(new Subscriber<String>() {
            @Override public void onCompleted() {
                System.out.println("Completed Observable.");
            }

            @Override public void onError(Throwable throwable) {
                System.err.println("Whoops: " + throwable.getMessage());
            }

            @Override public void onNext(String name) {
                System.out.println("Got: " + name);
            }
        });
    }

    @Test public void testAction1() {
        Observable.just(1, 2, 3).subscribe(integer -> System.out.println("Got: " + integer));
    }

    @Test public void testCountDownLatch() {
        final CountDownLatch latch = new CountDownLatch(5);
        long startTime = System.currentTimeMillis();
        Observable.interval(1, TimeUnit.SECONDS).subscribe(counter -> {
            latch.countDown();
            System.out.println("Got: " + counter + " time: " + (System.currentTimeMillis() - startTime));
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test public void testBlocking() {
        BlockingObservable<Long> observable = Observable.interval(1, TimeUnit.SECONDS).take(5).toBlocking();
        observable.forEach(counter -> System.out.println("Got: " + counter));
    }

    @Test public void testScan() {
        Observable.just(1, 2, 3, 4, 5).observeOn(Schedulers.io()).map(integer -> {
            System.out.println("current thread: " + Thread.currentThread().getName());
            return integer;
        }).observeOn(Schedulers.computation()).flatMap(integer -> {
            System.out.println("current thread: " + Thread.currentThread().getName());
            return Observable.just(integer);
        }).observeOn(Schedulers.io()).scan((sum, value) -> {
            System.out.println("current thread: " + Thread.currentThread().getName());
            return sum + value;
        }).toBlocking().subscribe(integer -> System.out.println("Sum: " + integer + " current thread: " + Thread.currentThread().getName()));
    }

    @Test public void testGroupBy() {
        Observable.just(1, 2, 3, 4, 5)
                  .groupBy(integer -> integer % 2 == 0)
                  .subscribe(grouped -> grouped.toList().subscribe(integers -> System.out.println(integers + " (Even: " + grouped.getKey() + ")")));
    }

    @Test public void testFilter() {
        Observable.just(1, 2, 3, 4).filter(integer -> integer > 2).subscribe(integer -> System.out.println("integer: " + integer));
    }

    @Test public void testDistinct() {
        Observable.just(1, 2, 3, 4, 1, 2, 3, 4).distinct().subscribe(integer -> System.out.println("integer: " + integer));
    }

    @Test public void testCombine() {
        Observable<Integer> evens = Observable.just(2, 4, 6, 8, 10);
        Observable<Integer> odds = Observable.just(1, 3, 5, 7, 9);
        Observable.merge(evens, odds).subscribe(integer -> System.out.println("integer: " + integer));
    }

    @Test public void testZip() {
        Observable<Integer> evens = Observable.just(2, 4, 6, 8, 10);
        Observable<Integer> odds = Observable.just(1, 3, 5, 7, 9);
        Observable.zip(evens, odds, (v1, v2) -> v1 + " + " + v2 + " is: " + (v1 + v2)).subscribe(System.out::println);
    }

    @Test public void testCatch() {
        Observable.from(integers).map(integer -> integer / 0).onErrorReturn(throwable -> 0).subscribe(mSubscriber);
        Observable.from(integers).map(integer -> integer / 0).onErrorResumeNext(throwable -> Observable.from(integers1)).subscribe(mSubscriber);
        Observable.from(integers).map(integer -> integer / 0).onExceptionResumeNext(Observable.from(integers1)).subscribe(mSubscriber);
    }

    @Test public void testRetry2() {
        Observable.from(integers).map(integer -> {
            int a = 1;
            if (integer == a) {
                throw new RuntimeException("aa");
            }
            return integer;
        }).retry((integer, throwable) -> false).subscribe(mSubscriber);
    }

    @Test public void testRetryWhen() {
        Observable.from(integers).map(integer -> {
            int a = 5;
            if (integer == a) {
                throw new RuntimeException("aa");
            }
            return integer;
        }).retryWhen(observable -> observable.zipWith(Observable.range(1, 3), (n, i) -> i).flatMap(i -> {
            System.out.println("delay retry by " + i + " second(s)");
            return Observable.timer(i, TimeUnit.SECONDS);
        })).toBlocking().forEach(System.out::println);

        //Observable.from(integers).map(integer -> {
        //    int a = 5;
        //    if (integer == a) {
        //        throw new RuntimeException("aa");
        //    }
        //    return integer;
        //}).retryWhen(observable -> observable.flatMap((Func1<Throwable, Observable<?>>) throwable -> Observable.from(integers1))).subscribe(mSubscriber);
    }

    @Test public void testDelay() {
        //Observable.from(integers).delay(5, TimeUnit.SECONDS).toBlocking().forEach(System.out::println);
        Observable.from(integers).delaySubscription(5, TimeUnit.SECONDS).toBlocking().forEach(System.out::println);
    }

    @Test public void testDo() {
        //Observable.from(integers).doOnEach(notification -> {
        //    System.out.println(notification.getKind().name());
        //}).subscribe(mSubscriber);

        Observable.from(integers).doOnRequest(System.out::print).subscribe(mSubscriber);
    }

    @Test public void testMaterialize() {
        Observable.from(integers).materialize().dematerialize().subscribe(mSubscriber);
    }

    @Test public void testError() {
        Observable.just("Apples", "Bananas").doOnNext(s -> {
            throw new RuntimeException("I don't like: " + s);
        }).onErrorReturn(throwable -> {
            System.err.println("Oops: " + throwable.getMessage());
            return "Default";
        }).subscribe(System.out::println);
    }

    @Test public void testRetry() {
        Observable.range(1, 10).doOnNext(integer -> {
            if (new Random().nextInt(10) + 1 == 5) {
                throw new RuntimeException("Boo!");
            }
        }).retryWhen(attempts -> attempts.zipWith(Observable.range(1, 3), (n, i) -> i).flatMap(i -> {
            System.out.println("delay retry by " + i + " second(s)");
            return Observable.timer(i, TimeUnit.SECONDS);
        })).distinct().subscribe(System.out::println);
    }

    @Test public void testSchedulers() {
        //Observable
        //        .range(1, 5)
        //        .map(integer -> {
        //            System.out.println("Map: (" + Thread.currentThread().getName() + ")");
        //            return integer + 2;
        //        })
        //        .subscribe(integer ->
        //                           System.out.println("Got: " + integer + " (" + Thread.currentThread().getName() + ")")
        //        );

        //Observable
        //        .range(1, 5)
        //        .map(integer -> {
        //            System.out.println("Map: (" + Thread.currentThread().getName() + ")");
        //            return integer + 2;
        //        })
        //        .subscribeOn(Schedulers.computation())
        //        .subscribe(integer ->
        //                           System.out.println("Got: " + integer + " (" + Thread.currentThread().getName() + ")")
        //        );

        Observable.range(1, 5)
                  .map(integer -> {
                      System.out.println("Map: (" + Thread.currentThread().getName() + ")");
                      return integer + 2;
                  })
                  .observeOn(Schedulers.computation())
                  .subscribe(integer -> System.out.println("Got: " + integer + " (" + Thread.currentThread().getName() + ")"));
    }

    @Test public void testSubject() {
        final TestScheduler scheduler = new TestScheduler();

        scheduler.advanceTimeTo(100, TimeUnit.SECONDS);
        final TestSubject<Object> subject = TestSubject.create(scheduler);
        final Observer observer = mock(Observer.class);
        subject.subscribe(observer);
        subject.onNext(1);
        scheduler.triggerActions();

        verify(observer, times(1)).onNext(1);
    }

    @Test public void testAsyncSubject() {
        final AsyncSubject<Object> subject = AsyncSubject.create();
        final Observer observer = mock(Observer.class);
        subject.subscribe(observer);

        subject.onNext("first");
        subject.onNext("second");
        subject.onNext("third");
        subject.onCompleted();

        verify(observer, times(1)).onNext(anyString());
        verify(observer, never()).onError(new Throwable());
        verify(observer, times(1)).onCompleted();
    }

    @Test public void testBehaviorSubject() {
        final BehaviorSubject<Object> subject = BehaviorSubject.create("default");
        final Observer observerA = mock(Observer.class);
        final Observer observerB = mock(Observer.class);
        final Observer observerC = mock(Observer.class);

        //final Observer observerA = new Observer() {
        //    @Override public void onCompleted() {
        //
        //    }
        //
        //    @Override public void onError(Throwable e) {
        //
        //    }
        //
        //    @Override public void onNext(Object o) {
        //        System.out.println("observerA " + o);
        //    }
        //};
        //
        //final Observer observerB = new Observer() {
        //    @Override public void onCompleted() {
        //
        //    }
        //
        //    @Override public void onError(Throwable e) {
        //
        //    }
        //
        //    @Override public void onNext(Object o) {
        //        System.out.println("observerB " + o);
        //    }
        //};

        final InOrder inOrder = inOrder(observerA, observerB, observerC);

        //subject.onNext("one");
        //subject.onNext("two");
        //final InOrder inOrderB = inOrder(observerB);
        //final InOrder inOrderC = inOrder(observerC);

        final Subscription subscribeA = subject.subscribe(observerA);
        final Subscription subscribeB = subject.subscribe(observerB);
        //final Subscription subscribeC = subject.subscribe(observerC);


        inOrder.verify(observerA).onNext("default");
        inOrder.verify(observerB).onNext("default");

        subject.onNext("one");

        inOrder.verify(observerA).onNext("one");

        //inOrder.verify(observerA).onNext("one");
        //inOrder.verify(observerA).onNext("two");
        //inOrderB.verify(observerB).onNext("default");
        //inOrderA.verify(observerA).onNext("default");

        //subject.onNext("one");
        //subject.onNext("two");
        //subject.onNext("three");

        //inOrder.verify(observer, times(1)).onNext("two");
        //verify(observer, times(1)).onNext("default");
        //verify(observer, times(1)).onNext("one");
        //verify(observer, times(1)).onNext("two");
        //verify(observer, times(1)).onNext("three");


        //verifyNoMoreInteractions(observer);
    }

    @Test public void testPublishSubject() {
        final PublishSubject<Object> subject = PublishSubject.create();
        final Observer observer = mock(Observer.class);

        subject.onNext("one");
        subject.onNext("two");

        subject.subscribe(observer);

        subject.onNext("three");

        verify(observer, never()).onNext("one");
        verify(observer, never()).onNext("two");
        verify(observer, times(1)).onNext("three");
    }

    @Test public void testReplaySubject() {
        final ReplaySubject<Object> subject = ReplaySubject.create();
        final Observer observer = mock(Observer.class);

        subject.onNext("one");
        subject.onNext("two");

        subject.subscribe(observer);

        subject.onNext("three");

        verify(observer, times(1)).onNext("one");
        verify(observer, times(1)).onNext("two");
        verify(observer, times(1)).onNext("three");
    }



    class MySubscriber extends Subscriber<Object> {
        private CountDownLatch mCountDownLatch;

        public void setCountDownLatch(CountDownLatch countDownLatch) {
            mCountDownLatch = countDownLatch;
        }

        @Override public void onCompleted() {
            if (mCountDownLatch != null) {
                mCountDownLatch.countDown();
            }
            System.out.println("onCompleted");
        }

        @Override public void onError(Throwable e) {
            System.out.println("onError");
        }

        @Override public void onNext(Object o) {
            System.out.println("onNext " + "value: " + o);
        }
    }

    private Subscriber<Object> mSubscriber = new Subscriber<Object>() {

        @Override public void onCompleted() {

            System.out.println("onCompleted");
        }

        @Override public void onError(Throwable e) {
            System.out.println("onError");
        }

        @Override public void onNext(Object o) {
            System.out.println("onNext " + "value: " + o);
        }
    };
}
