package io.nuls.rpc.model.message;

import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
public class Request {
    private int requestAck;
    private int subscriptionEventCounter;
    private int subscriptionPeriod;
    private String subscriptionRange;
    private int responseMaxSize;
    private Map<String,Object> requestMethods;

    public int isRequestAck() {
        return requestAck;
    }

    public void setRequestAck(int requestAck) {
        this.requestAck = requestAck;
    }

    public int getSubscriptionEventCounter() {
        return subscriptionEventCounter;
    }

    public void setSubscriptionEventCounter(int subscriptionEventCounter) {
        this.subscriptionEventCounter = subscriptionEventCounter;
    }

    public int getSubscriptionPeriod() {
        return subscriptionPeriod;
    }

    public void setSubscriptionPeriod(int subscriptionPeriod) {
        this.subscriptionPeriod = subscriptionPeriod;
    }

    public String getSubscriptionRange() {
        return subscriptionRange;
    }

    public void setSubscriptionRange(String subscriptionRange) {
        this.subscriptionRange = subscriptionRange;
    }

    public int getResponseMaxSize() {
        return responseMaxSize;
    }

    public void setResponseMaxSize(int responseMaxSize) {
        this.responseMaxSize = responseMaxSize;
    }

    public Map<String,Object> getRequestMethods() {
        return requestMethods;
    }

    public void setRequestMethods(Map<String,Object> requestMethods) {
        this.requestMethods = requestMethods;
    }
}
