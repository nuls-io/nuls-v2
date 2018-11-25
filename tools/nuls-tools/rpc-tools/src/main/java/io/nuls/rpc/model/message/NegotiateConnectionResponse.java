package io.nuls.rpc.model.message;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
public class NegotiateConnectionResponse {
    private String negotiationStatus;
    private String negotiationComment;

    public String getNegotiationStatus() {
        return negotiationStatus;
    }

    public void setNegotiationStatus(String negotiationStatus) {
        this.negotiationStatus = negotiationStatus;
    }

    public String getNegotiationComment() {
        return negotiationComment;
    }

    public void setNegotiationComment(String negotiationComment) {
        this.negotiationComment = negotiationComment;
    }
}
