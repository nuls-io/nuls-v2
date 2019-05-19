package io.nuls.base.api.provider;

import io.nuls.core.constant.CommonCodeConstanst;

import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 15:45
 * @Description: 功能描述
 */
public class Result<T> {

    static final String SUCCESS = CommonCodeConstanst.SUCCESS.getCode();

    String status;

    String message;

    T data;

    List<T> list;

    public Result() {
        this.status = SUCCESS;
    }

    public Result(T data) {
        this.status = SUCCESS;
        this.data = data;
    }

    public Result(List<T> list) {
        this.list = list;
        this.status = SUCCESS;
    }

    public Result(String status, String message) {
        this.status = status;
        this.message = message;
    }

    public static Result fail(String status, String message) {
        return new Result(status, message);
    }

    public boolean isSuccess() {
        return status == SUCCESS;
    }

    public boolean isFailed() {
        return !isSuccess();
    }

    public static String getSUCCESS() {
        return SUCCESS;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public List<T> getList() {
        return list;
    }

    public void setList(List<T> list) {
        this.list = list;
    }
}
