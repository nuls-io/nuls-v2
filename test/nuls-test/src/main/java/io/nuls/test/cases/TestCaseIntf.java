package io.nuls.test.cases;

import io.nuls.base.api.provider.Result;
import io.nuls.test.utils.Utils;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-19 20:48
 * @Description: Test Case Interface
 *
 * @param <T>  After testing, return the type
 * @param <P>  Enter parameter types for testing
 */
public interface TestCaseIntf<T,P> {

    default String depthSpace(int depth){
        return "===".repeat(depth) + (depth > 0 ? ">" : "");
    }

    default T check(P param,int depth) throws TestFailException{
        if(param == null){
            param = initParam();
        }
        if(this.caseType() == CaseType.Test){
            Utils.success(depthSpace(depth)+"Start testing【"+title()+"】");
            T res = doTest(param,depth+1);
            Utils.success(depthSpace(depth) + title() + "Test passed");
            return res;
        }else{
            Utils.msg(depthSpace(depth)+"implement:【"+this.title()+"】");
            return doTest(param,depth+1);
        }
    }

    default void checkResultStatus(Result result) throws TestFailException {
        if(!result.isSuccess()){
            throw new TestFailException(result.getStatus() + " : " + result.getMessage());
        }
        if(result.getList() == null && result.getData() == null){
            throw new TestFailException(title() + "The test return value does not meet expectations, and the return data is empty");
        }

    }

    default void check(boolean condition,String msg) throws TestFailException {
        if(!condition){
            throw new TestFailException(title() + "The test results did not meet expectations," + msg);
        }
    }

    String title();

    T doTest(P param,int depth) throws TestFailException;

    CaseType caseType();

    P initParam();
}
