package io.nuls.test.cases;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-19 21:02
 * @Description: 功能描述
 */
public class TestFailException extends Exception {

    public TestFailException(String message){
        super(message);
    }

    public TestFailException(String message,Throwable e){
        super(message,e);
    }

}
