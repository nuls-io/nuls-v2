package io.nuls.network.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.network.model.dto.Dto;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class NodesContainerPo extends BasePo {

    private List<NodePo> disConnectNodes;

    private List<NodePo> canConnectNodes;

    private List<NodePo> failNodes;

    private List<NodePo> uncheckNodes;

    public NodesContainerPo() {
        disConnectNodes = new ArrayList<>();
        canConnectNodes = new ArrayList<>();
        failNodes = new ArrayList<>();
        uncheckNodes = new ArrayList<>();
    }

    public List<NodePo> getDisConnectNodes() {
        return disConnectNodes;
    }

    public void setDisConnectNodes(List<NodePo> disConnectNodes) {
        this.disConnectNodes = disConnectNodes;
    }

    public List<NodePo> getCanConnectNodes() {
        return canConnectNodes;
    }

    public void setCanConnectNodes(List<NodePo> canConnectNodes) {
        this.canConnectNodes = canConnectNodes;
    }

    public List<NodePo> getFailNodes() {
        return failNodes;
    }

    public void setFailNodes(List<NodePo> failNodes) {
        this.failNodes = failNodes;
    }

    public List<NodePo> getUncheckNodes() {
        return uncheckNodes;
    }

    public void setUncheckNodes(List<NodePo> uncheckNodes) {
        this.uncheckNodes = uncheckNodes;
    }

    protected void parseNodePo(NulsByteBuffer byteBuffer, List<NodePo> nodes) throws NulsException {
        int size = (int) byteBuffer.readVarInt();
        if (0 < size) {
            for (int i = 0; i < size; i++) {
                NodePo nodePo = new NodePo();
                nodePo.parse(byteBuffer);
                nodes.add(nodePo);
            }
        }
    }

    @Override
    public Dto parseDto() {
        return null;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        int disConnectNodesSize = (disConnectNodes == null ? 0 : disConnectNodes.size());
        stream.writeVarInt(disConnectNodesSize);
        if (null != disConnectNodes) {
            for (NodePo nodePo : disConnectNodes) {
                nodePo.serializeToStream(stream);
            }
        }
        int canConnectNodesSize = (canConnectNodes == null ? 0 : canConnectNodes.size());
        stream.writeVarInt(canConnectNodesSize);
        if (null != canConnectNodes) {
            for (NodePo nodePo : canConnectNodes) {
                nodePo.serializeToStream(stream);
            }
        }

        int failNodesSize = (failNodes == null ? 0 : failNodes.size());
        stream.writeVarInt(failNodesSize);
        if (null != failNodes) {
            for (NodePo nodePo : failNodes) {
                nodePo.serializeToStream(stream);
            }
        }

        int uncheckNodesSize = (uncheckNodes == null ? 0 : uncheckNodes.size());
        stream.writeVarInt(uncheckNodesSize);
        if (null != uncheckNodes) {
            for (NodePo nodePo : uncheckNodes) {
                nodePo.serializeToStream(stream);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        parseNodePo(byteBuffer, disConnectNodes);
        parseNodePo(byteBuffer, canConnectNodes);
        parseNodePo(byteBuffer, failNodes);
        parseNodePo(byteBuffer, uncheckNodes);
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfVarInt(disConnectNodes == null ? 0 : disConnectNodes.size());
        if (null != disConnectNodes) {
            for (NodePo nodePo : disConnectNodes) {
                size += nodePo.size();
            }
        }
        size += SerializeUtils.sizeOfVarInt(canConnectNodes == null ? 0 : canConnectNodes.size());
        if (null != canConnectNodes) {
            for (NodePo nodePo : canConnectNodes) {
                size += nodePo.size();
            }
        }
        size += SerializeUtils.sizeOfVarInt(failNodes == null ? 0 : failNodes.size());
        if (null != failNodes) {
            for (NodePo nodePo : failNodes) {
                size += nodePo.size();
            }
        }
        size += SerializeUtils.sizeOfVarInt(uncheckNodes == null ? 0 : uncheckNodes.size());
        if (null != uncheckNodes) {
            for (NodePo nodePo : uncheckNodes) {
                size += nodePo.size();
            }
        }
        return size;
    }
}
