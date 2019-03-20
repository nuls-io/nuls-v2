package io.nuls.test.cases;

import io.nuls.api.provider.Result;
import io.nuls.test.utils.Utils;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-19 20:48
 * @Description: 功能描述
 */
public interface TestCaseIntf<T,P> {

    default String depthSpace(int depth){
        return "===".repeat(depth) + ">";
    }

    default T check(P param,int depth) throws TestFailException{
        T res = doTest(param,depth+1);
        Utils.success(depthSpace(depth) + title() + "测试通过");
        return res;
    }

    default void checkResultStatus(Result result) throws TestFailException {
        if(!result.isSuccess()){
            throw new TestFailException(result.getMessage());
        }
    }

    String title();

    T doTest(P param,int depth) throws TestFailException;

}
