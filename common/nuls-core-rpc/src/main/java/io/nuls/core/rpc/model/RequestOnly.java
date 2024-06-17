package io.nuls.core.rpc.model;

import io.nuls.core.rpc.model.message.Request;

/**
 * Request to call remote method without returning
 * Request calls to remote methods without requiring return
 *
 * @author tag
 * @date 2019/09/09
 */

public class RequestOnly {
    private Request request;

    private int messageSize;

    public  RequestOnly(Request request, int messageSize){
        this.request = request;
        this.messageSize = messageSize;
    }

    public Request getRequest() {
        return request;
    }

    public void setRequest(Request request) {
        this.request = request;
    }

    public int getMessageSize() {
        return messageSize;
    }

    public void setMessageSize(int messageSize) {
        this.messageSize = messageSize;
    }
}
