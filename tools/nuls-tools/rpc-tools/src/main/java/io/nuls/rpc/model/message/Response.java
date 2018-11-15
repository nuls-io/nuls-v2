package io.nuls.rpc.model.message;

import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
public class Response {
    private String requestID;
    private long responseProcessingTime;
    private int responseStatus;
    private String responseComment;
    private int responseMaxSize;
    private Map<String, Object> responseData;

    public String getRequestID() {
        return requestID;
    }

    public void setRequestID(String requestID) {
        this.requestID = requestID;
    }

    public long getResponseProcessingTime() {
        return responseProcessingTime;
    }

    public void setResponseProcessingTime(long responseProcessingTime) {
        this.responseProcessingTime = responseProcessingTime;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseComment() {
        return responseComment;
    }

    public void setResponseComment(String responseComment) {
        this.responseComment = responseComment;
    }

    public int getResponseMaxSize() {
        return responseMaxSize;
    }

    public void setResponseMaxSize(int responseMaxSize) {
        this.responseMaxSize = responseMaxSize;
    }

    public Map<String, Object> getResponseData() {
        return responseData;
    }

    public void setResponseData(Map<String, Object> responseData) {
        this.responseData = responseData;
    }
}
