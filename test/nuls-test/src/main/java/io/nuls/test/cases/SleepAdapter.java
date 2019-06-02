package io.nuls.test.cases;

import io.nuls.core.core.annotation.Component;

import java.util.concurrent.TimeUnit;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-21 10:50
 * @Description: 功能描述
 */
public abstract class SleepAdapter extends BaseAdapter<Object,Object> {


    public abstract int sleepSec();


    @Override
    public String title() {
        return "等待"+this.sleepSec()+"秒";
    }

    @Override
    public Object doTest(Object param, int depth) throws TestFailException {
        try {
            for (int j = 1; j <= this.sleepSec(); j++) {
                System.out.print(j + " ");
                TimeUnit.SECONDS.sleep(1L);
            }
            System.out.println();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return param;
    }

    @Override
    public CaseType caseType() {
        return CaseType.Adapter;
    }
//
//    public static Class<? extends SleepAdapter> sleepSec(int sec){
//        return new SleepAdapter(){
//            @Override
//            public int sleepSec() {
//                return sec;
//            }
//        }.getClass();
//    }

    @Component
    public static class $10SEC extends SleepAdapter {

        @Override
        public int sleepSec() {
            return 10;
        }
    }

    @Component
    public static class $15SEC extends SleepAdapter {

        @Override
        public int sleepSec() {
            return 15;
        }
    }

    @Component
    public static class $30SEC extends SleepAdapter {

        @Override
        public int sleepSec() {
            return 30;
        }
    }

    @Component
    public static class $60SEC extends SleepAdapter {

        @Override
        public int sleepSec() {
            return 60;
        }
    }

    @Component
    public static class MAX extends SleepAdapter {

        @Override
        public int sleepSec() {
            return Integer.MAX_VALUE;
        }
    }

}
