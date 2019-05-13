package io.nuls.test.controller;

import io.nuls.test.cases.TestCaseIntf;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 11:09
 * @Description: 功能描述
 */
public class RemoteCaseReq {

    Class<? extends TestCaseIntf> caseClass;

    String param;

    public Class<? extends TestCaseIntf> getCaseClass() {
        return caseClass;
    }

    public void setCaseClass(Class<? extends TestCaseIntf> caseClass) {
        this.caseClass = caseClass;
    }

    public String getParam() {
        return param;
    }

    public void setParam(String param) {
        this.param = param;
    }

    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("\"caseClass\":")
                .append(caseClass)
                .append(",\"param\":\"")
                .append(param).append('\"')
                .append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RemoteCaseReq)) return false;

        RemoteCaseReq that = (RemoteCaseReq) o;

        if (caseClass != null ? !caseClass.equals(that.caseClass) : that.caseClass != null) return false;
        return param != null ? param.equals(that.param) : that.param == null;
    }

    @Override
    public int hashCode() {
        int result = caseClass != null ? caseClass.hashCode() : 0;
        result = 31 * result + (param != null ? param.hashCode() : 0);
        return result;
    }
}
