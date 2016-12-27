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
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import rx.Observable;
import rx.Observer;
import rx.Subscriber;
import rx.observables.BlockingObservable;
import rx.observables.SyncOnSubscribe;
import rx.schedulers.Schedulers;

@RunWith(MockitoJUnitRunner.class) public class RxJava1UnitTest {
    @Mock Context mContext;

    @Test public void testCreateObservable() {
        Observable.create(new SyncOnSubscribe<Object, Object>() {
            @Override protected Object generateState() {
                return null;
            }

            @Override protected Object next(Object state, Observer<? super Object> observer) {
                observer.onNext(state);
                observer.onCompleted();
                return state;
            }
        }).subscribe(new Subscriber<Object>() {
            @Override public void onCompleted() {

            }

            @Override public void onError(Throwable e) {

            }

            @Override public void onNext(Object o) {

            }
        });
        //Observable.create(subscriber -> {
        //    System.out.println("subscriber");
        //    if (!subscriber.isUnsubscribed()) {
        //        subscriber.onNext(subscriber);
        //        subscriber.onCompleted();
        //    }
        //}).subscribe(new Subscriber<Object>() {
        //    @Override public void onCompleted() {
        //        System.out.println("onCompleted");
        //    }
        //
        //    @Override public void onError(Throwable e) {
        //        System.out.println("onError");
        //    }
        //
        //    @Override public void onNext(Object o) {
        //        System.out.println("onNext");
        //    }
        //});
    }

    @Test public void testDeferObservable() {
        Observable.defer(Observable::empty).subscribe(new Subscriber<Object>() {
            @Override public void onCompleted() {

            }

            @Override public void onError(Throwable e) {

            }

            @Override public void onNext(Object o) {

            }
        });
    }

    @Test public void testLoadThreeDocumentsInParallel() {
        Observable.just(1, 2, 3).subscribeOn(Schedulers.io()).observeOn(Schedulers.io()).subscribe(new Subscriber<Integer>() {

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
        Observable.interval(1, TimeUnit.SECONDS).subscribe(counter -> {
            latch.countDown();
            System.out.println("Got: " + counter);
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
        Observable.just(1, 2, 3, 4, 5).scan((sum, value) -> sum + value).subscribe(integer -> System.out.println("Sum: " + integer));
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

    @Test public void testSubjects() {

    }
}
