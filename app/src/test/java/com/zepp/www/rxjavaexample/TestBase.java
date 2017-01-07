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

public class TestBase {

    static final int SHORT_DELAY_MS = 1000;

    void awaitTermination(Thread t, long timeoutMillis) {
        try {
            t.join(timeoutMillis);
        } catch (InterruptedException fail) {
            //threadUnexpectedException(fail);
        } finally {
            if (t.getState() != Thread.State.TERMINATED) {
                t.interrupt();
                //threadFail("timed out waiting for thread to terminate");
            }
        }
    }

    void delay(long millis) throws InterruptedException {
        long nanos = millis * (1000 * 1000);
        final long wakeupTime = System.nanoTime() + nanos;
        do {
            if (millis > 0L) {
                Thread.sleep(millis);
            } else // too short to sleep
            {
                Thread.yield();
            }
            nanos = wakeupTime - System.nanoTime();
            millis = nanos / (1000 * 1000);
        } while (nanos >= 0L);
    }

    long timeoutMillis() {
        return SHORT_DELAY_MS / 4;
    }
}
