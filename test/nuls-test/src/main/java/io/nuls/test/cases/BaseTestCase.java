package io.nuls.test.cases;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-22 09:48
 * @Description: 功能描述
 */
public abstract class BaseTestCase<T,P> implements TestCaseIntf<T,P> {

    @Override
    public P initParam(){
        return null;
    }

    @Override
    public CaseType caseType(){
        return CaseType.Test;
    }

}
