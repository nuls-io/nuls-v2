package io.nuls.rpc.model.message;

import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
public class Request {
    private String requestAck;
    private String subscriptionEventCounter;
    private String subscriptionPeriod;
    private String subscriptionRange;
    private String responseMaxSize;
    private Map<String,Object> requestMethods;

    public String getRequestAck() {
        return requestAck;
    }

    public void setRequestAck(String requestAck) {
        this.requestAck = requestAck;
    }

    public String getSubscriptionEventCounter() {
        return subscriptionEventCounter;
    }

    public void setSubscriptionEventCounter(String subscriptionEventCounter) {
        this.subscriptionEventCounter = subscriptionEventCounter;
    }

    public String getSubscriptionPeriod() {
        return subscriptionPeriod;
    }

    public void setSubscriptionPeriod(String subscriptionPeriod) {
        this.subscriptionPeriod = subscriptionPeriod;
    }

    public String getSubscriptionRange() {
        return subscriptionRange;
    }

    public void setSubscriptionRange(String subscriptionRange) {
        this.subscriptionRange = subscriptionRange;
    }

    public String getResponseMaxSize() {
        return responseMaxSize;
    }

    public void setResponseMaxSize(String responseMaxSize) {
        this.responseMaxSize = responseMaxSize;
    }

    public Map<String, Object> getRequestMethods() {
        return requestMethods;
    }

    public void setRequestMethods(Map<String, Object> requestMethods) {
        this.requestMethods = requestMethods;
    }
}
