package io.nuls.core.rpc.protocol;

public class MessageProcessor {
    private String handler;
    private String method;

    public String getHandler() {
        return handler;
    }

    public void setHandler(String handler) {
        this.handler = handler;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    @Override
    public String toString() {
        return "MessageProcessor{" +
                "handler='" + handler + '\'' +
                ", method='" + method + '\'' +
                '}';
    }
}