package io.nuls.crosschain.nuls.model.bo.message;

import io.nuls.crosschain.base.message.BroadCtxSignMessage;

import java.util.Arrays;

public class WaitBroadSignMessage {
    private String nodeId;
    private BroadCtxSignMessage message;

    public WaitBroadSignMessage(String nodeId,BroadCtxSignMessage message){
        this.nodeId = nodeId;
        this.message = message;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public BroadCtxSignMessage getMessage() {
        return message;
    }

    public void setMessage(BroadCtxSignMessage message) {
        this.message = message;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + Arrays.hashCode(this.message.getSignature());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof WaitBroadSignMessage)) {
            return false;
        }
        return Arrays.equals(this.getMessage().getSignature(), ((WaitBroadSignMessage) obj).getMessage().getSignature());
    }
}
