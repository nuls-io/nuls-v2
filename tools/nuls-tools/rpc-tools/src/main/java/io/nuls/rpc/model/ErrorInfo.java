package io.nuls.rpc.model;

/**
 * @author tangyi
 * @date 2018/11/6
 * @description
 */
public class ErrorInfo {
    private int code;
    private String message;

    public ErrorInfo() {
    }

    public ErrorInfo(int code, String message) {
        this.code = code;
        this.message = message;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
