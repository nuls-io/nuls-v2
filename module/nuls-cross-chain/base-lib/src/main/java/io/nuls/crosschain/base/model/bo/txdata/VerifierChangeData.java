package io.nuls.crosschain.base.model.bo.txdata;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 验证者变更交易txdata
 * Verifier changes transaction txdata
 *
 * @author tag
 * 2019/6/18
 */
public class VerifierChangeData extends BaseNulsData {
    private int chainId;
    private List<String> registerAgentList;
    private List<String> cancelAgentList;


    public VerifierChangeData(){}

    public VerifierChangeData(List<String> registerAgentList,List<String> cancelAgentList,int chainId){
        this.chainId = chainId;
        this.registerAgentList = registerAgentList;
        this.cancelAgentList = cancelAgentList;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(chainId);
        int registerCount = registerAgentList == null ? 0 : registerAgentList.size();
        stream.writeVarInt(registerCount);
        if(registerAgentList != null){
            for (String registerAgent:registerAgentList) {
                stream.writeString(registerAgent);
            }
        }
        int cancelCount = cancelAgentList == null ? 0 : cancelAgentList.size();
        stream.writeVarInt(cancelCount);
        if(cancelAgentList != null){
            for (String cancelAgent:cancelAgentList) {
                stream.writeString(cancelAgent);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chainId = byteBuffer.readUint16();
        int registerCount = (int) byteBuffer.readVarInt();
        if(registerCount > 0){
            List<String> registerAgentList = new ArrayList<>();
            for (int i = 0; i < registerCount; i++) {
                registerAgentList.add(byteBuffer.readString());
            }
            this.registerAgentList = registerAgentList;
        }

        int cancelCount = (int) byteBuffer.readVarInt();
        if (cancelCount > 0) {
            List<String> cancelAgentList = new ArrayList<>();
            for (int i = 0; i < cancelCount; i++) {
                cancelAgentList.add(byteBuffer.readString());
            }
            this.cancelAgentList = cancelAgentList;
        }
    }

    @Override
    public int size() {
        int size = SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfVarInt(registerAgentList == null ? 0 : registerAgentList.size());
        if(registerAgentList != null){
            for (String registerAgent:registerAgentList) {
                size += SerializeUtils.sizeOfString(registerAgent);
            }
        }

        size += SerializeUtils.sizeOfVarInt(cancelAgentList == null ? 0 : cancelAgentList.size());
        if(cancelAgentList != null){
            for (String cancelAgent:cancelAgentList) {
                size += SerializeUtils.sizeOfString(cancelAgent);
            }
        }
        return size;
    }

    public List<String> getRegisterAgentList() {
        return registerAgentList;
    }

    public void setRegisterAgentList(List<String> registerAgentList) {
        this.registerAgentList = registerAgentList;
    }

    public List<String> getCancelAgentList() {
        return cancelAgentList;
    }

    public void setCancelAgentList(List<String> cancelAgentList) {
        this.cancelAgentList = cancelAgentList;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }
}
