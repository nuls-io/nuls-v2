package io.nuls.rpc.model.message;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
public class Response {
    private String requestId;
    private String responseProcessingTime;
    private String responseStatus;
    private String responseComment;
    private String responseMaxSize;
    private Object responseData;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getResponseProcessingTime() {
        return responseProcessingTime;
    }

    public void setResponseProcessingTime(String responseProcessingTime) {
        this.responseProcessingTime = responseProcessingTime;
    }

    public String getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(String responseStatus) {
        this.responseStatus = responseStatus;
    }

    public String getResponseComment() {
        return responseComment;
    }

    public void setResponseComment(String responseComment) {
        this.responseComment = responseComment;
    }

    public String getResponseMaxSize() {
        return responseMaxSize;
    }

    public void setResponseMaxSize(String responseMaxSize) {
        this.responseMaxSize = responseMaxSize;
    }

    public Object getResponseData() {
        return responseData;
    }

    public void setResponseData(Object responseData) {
        this.responseData = responseData;
    }
}
