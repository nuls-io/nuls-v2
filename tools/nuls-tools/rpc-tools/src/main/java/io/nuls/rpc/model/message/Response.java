package io.nuls.rpc.model.message;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
public class Response {
    private int requestId;
    private long responseProcessingTime;
    private int responseStatus;
    private String responseComment;
    private int responseMaxSize;
    private Object responseData;

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
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

    public Object getResponseData() {
        return responseData;
    }

    public void setResponseData(Object responseData) {
        this.responseData = responseData;
    }
}
