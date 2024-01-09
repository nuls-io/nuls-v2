package io.nuls.crosschain.base.model.bo.txdata;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.crosschain.base.message.base.BaseMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;

import java.io.IOException;
import java.util.*;

public class RegisteredChainChangeData extends BaseMessage {
    /**
     * 注册链ID
     * */
    private int registerChainId;
    /**
     * 消息类型 0：将已注册跨链的所有链信息广播给新注册链，1：将新注册链信息广播给已注册跨链的链，2：链资产变更（注销/注册资产）
     */
    private int type;

    private List<ChainInfo> chainInfoList;

    public RegisteredChainChangeData(){}

    public RegisteredChainChangeData(int registerChainId, int type, List<ChainInfo> chainInfoList){
        this.registerChainId = registerChainId;
        this.type = type;
        this.chainInfoList = chainInfoList;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(registerChainId);
        stream.writeUint16(type);
        int count = chainInfoList == null ? 0 : chainInfoList.size();
        stream.writeVarInt(count);
        if(chainInfoList != null){
            for (ChainInfo chainInfo:chainInfoList) {
                stream.writeNulsData(chainInfo);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.registerChainId = byteBuffer.readUint16();
        this.type = byteBuffer.readUint16();
        int count = (int) byteBuffer.readVarInt();
        if(count > 0){
            List<ChainInfo> chainInfoList = new ArrayList<>();
            for (int i = 0; i < count; i++) {
                chainInfoList.add(byteBuffer.readNulsData(new ChainInfo()));
            }
            this.chainInfoList = chainInfoList;
        }
    }

    @Override
    public int size() {
        int size = SerializeUtils.sizeOfUint16() * 2;
        size += SerializeUtils.sizeOfVarInt(chainInfoList == null ? 0 : chainInfoList.size());
        if (chainInfoList != null && chainInfoList.size() > 0) {
            for (ChainInfo chainInfo : chainInfoList) {
                size +=  SerializeUtils.sizeOfNulsData(chainInfo);
            }
        }
        return size;
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof RegisteredChainMessage){
            RegisteredChainMessage message = (RegisteredChainMessage) obj;
            try {
                if(getChainInfoList() == null && message.getChainInfoList() == null){
                    return true;
                }
                if(getChainInfoList() == null && message.getChainInfoList() != null){
                    return  false;
                }
                if(getChainInfoList() != null && message.getChainInfoList() == null){
                    return  false;
                }
                if(getChainInfoList().size() != message.getChainInfoList().size()){
                    return false;
                }
                if(Arrays.equals(serialize(), message.serialize())){
                    return true;
                }
            }catch (Exception e){
                Log.error(e);
            }
        }
        return false;
    }



    public List<ChainInfo> getChainInfoList() {
        return chainInfoList;
    }

    public int getRegisterChainId() {
        return registerChainId;
    }

    public void setRegisterChainId(int registerChainId) {
        this.registerChainId = registerChainId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public void setChainInfoList(List<ChainInfo> chainInfoList) {
        this.chainInfoList = chainInfoList;
    }

    public void addChainInfo(ChainInfo chainInfo){
        chainInfoList.remove(chainInfo);
        chainInfoList.add(chainInfo);
    }
}
