package io.nuls.test.cases;

/**
 * @Author: zhoulijun
 * @Time: 2019-04-24 16:16
 * @Description: Function Description
 */
public abstract class BaseAdapter<T,P> implements TestCaseIntf<T,P> {

    @Override
    public P initParam(){return null;}

    @Override
    public CaseType caseType() {
        return CaseType.Adapter;
    }

}
