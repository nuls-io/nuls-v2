package io.nuls.crosschain.nuls.model.bo.message;
import io.nuls.base.data.NulsHash;
import io.nuls.crosschain.base.message.base.BaseMessage;
/**
 * 未处理消息类
 * Unprocessed message class
 *
 * @author tag
 * 2019/5/22
 */
public class UntreatedMessage {
    private int chainId;
    private String nodeId;
    private BaseMessage message;
    private NulsHash cacheHash;

    public UntreatedMessage(){

    }

    public UntreatedMessage(int chainId,String nodeId,BaseMessage baseMessage,NulsHash cacheHash){
        this.chainId = chainId;
        this.nodeId = nodeId;
        this.message = baseMessage;
        this.cacheHash = cacheHash;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public BaseMessage getMessage() {
        return message;
    }

    public void setMessage(BaseMessage message) {
        this.message = message;
    }

    public NulsHash getCacheHash() {
        return cacheHash;
    }

    public void setCacheHash(NulsHash cacheHash) {
        this.cacheHash = cacheHash;
    }
}
