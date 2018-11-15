package io.nuls.rpc.model.message;

/**
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
public class NegotiateConnectionResponse {
    private int negotiationStatus;
    private String negotiationComment;

    public int getNegotiationStatus() {
        return negotiationStatus;
    }

    public void setNegotiationStatus(int negotiationStatus) {
        this.negotiationStatus = negotiationStatus;
    }

    public String getNegotiationComment() {
        return negotiationComment;
    }

    public void setNegotiationComment(String negotiationComment) {
        this.negotiationComment = negotiationComment;
    }
}
