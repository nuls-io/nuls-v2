package io.nuls.test.cases;


/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 13:53
 * @Description: 功能描述
 */
public class RemoteTestParam<T> {

    Class<? extends TestCaseIntf> caseCls;

    T source;

    Object param;

    boolean isEquals = true;

    public RemoteTestParam(Class<? extends TestCaseIntf> caseCls,T source,Object param){
        this(caseCls,source,param,true);
    }

    public RemoteTestParam(Class<? extends TestCaseIntf> caseCls, T source, Object param, boolean isEquals) {
        this.caseCls = caseCls;
        this.source = source;
        this.param = param;
        this.isEquals = isEquals;
    }

    public Class<? extends TestCaseIntf> getCaseCls() {
        return caseCls;
    }

    public void setCaseCls(Class<? extends TestCaseIntf> caseCls) {
        this.caseCls = caseCls;
    }

    public T getSource() {
        return source;
    }

    public void setSource(T source) {
        this.source = source;
    }

    public Object getParam() {
        return param;
    }

    public void setParam(Object param) {
        this.param = param;
    }

    public boolean isEquals() {
        return isEquals;
    }

    public void setEquals(boolean equals) {
        isEquals = equals;
    }

    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("\"caseCls\":")
                .append(caseCls)
                .append(",\"source\":")
                .append(source)
                .append(",\"param\":")
                .append(param)
                .append(",\"isEquals\":")
                .append(isEquals)
                .append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RemoteTestParam)) return false;

        RemoteTestParam<?> that = (RemoteTestParam<?>) o;

        if (isEquals != that.isEquals) return false;
        if (caseCls != null ? !caseCls.equals(that.caseCls) : that.caseCls != null) return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        return param != null ? param.equals(that.param) : that.param == null;
    }

    @Override
    public int hashCode() {
        int result = caseCls != null ? caseCls.hashCode() : 0;
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (param != null ? param.hashCode() : 0);
        result = 31 * result + (isEquals ? 1 : 0);
        return result;
    }
}
