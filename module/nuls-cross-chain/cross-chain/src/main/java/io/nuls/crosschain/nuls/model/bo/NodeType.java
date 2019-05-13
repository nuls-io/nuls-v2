package io.nuls.crosschain.nuls.model.bo;

public class NodeType {
    private String nodeId;
    private int nodeType;

    public NodeType(String nodeId, int nodeType) {
        this.nodeId = nodeId;
        this.nodeType = nodeType;
    }

    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public int getNodeType() {
        return nodeType;
    }

    public void setNodeType(int nodeType) {
        this.nodeType = nodeType;
    }
}
