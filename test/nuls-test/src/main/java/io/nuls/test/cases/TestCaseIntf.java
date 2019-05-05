package io.nuls.test.cases;

import io.nuls.base.api.provider.Result;
import io.nuls.test.utils.Utils;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-19 20:48
 * @Description: 测试用例接口
 *
 * @param <T>  测试完后返回类型
 * @param <P>  进入测试的参数类型
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
            Utils.success(depthSpace(depth)+"开始测试【"+title()+"】");
            T res = doTest(param,depth+1);
            Utils.success(depthSpace(depth) + title() + "测试通过");
            return res;
        }else{
            Utils.msg(depthSpace(depth)+"执行:【"+this.title()+"】");
            return doTest(param,depth+1);
        }
    }

    default void checkResultStatus(Result result) throws TestFailException {
        if(!result.isSuccess()){
            throw new TestFailException(result.getStatus() + " : " + result.getMessage());
        }
        if(result.getList() == null && result.getData() == null){
            throw new TestFailException(title() + "测试返回值不符合预期，返回数据为空");
        }

    }

    default void check(boolean condition,String msg) throws TestFailException {
        if(!condition){
            throw new TestFailException(title() + "测试结果不符合预期，" + msg);
        }
    }

    String title();

    T doTest(P param,int depth) throws TestFailException;

    CaseType caseType();

    P initParam();
}
