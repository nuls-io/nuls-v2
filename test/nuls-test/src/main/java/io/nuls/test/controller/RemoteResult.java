package io.nuls.test.controller;


/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 11:43
 * @Description: 功能描述
 */

public class RemoteResult<T> {

    boolean success = true;

    T data;

    String msg;

    public RemoteResult() {
    }

    public RemoteResult(T data){
        this.data = data;
    }

    public RemoteResult(boolean success,String msg){
        this.success = success;
        this.msg = msg;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("\"success\":")
                .append(success)
                .append(",\"data\":")
                .append(data)
                .append(",\"msg\":\"")
                .append(msg).append('\"')
                .append('}').toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RemoteResult)) return false;

        RemoteResult<?> that = (RemoteResult<?>) o;

        if (success != that.success) return false;
        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        return msg != null ? msg.equals(that.msg) : that.msg == null;
    }

    @Override
    public int hashCode() {
        int result = (success ? 1 : 0);
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (msg != null ? msg.hashCode() : 0);
        return result;
    }
}
