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
 * 验证人初始化交易txdata
 * Verifier initializes transaction txdata
 *
 * @author tag
 * 2019/8/6
 */
public class VerifierInitData extends BaseNulsData {
    private int registerChainId;
    private List<String> verifierList;

    public VerifierInitData(){}


    public VerifierInitData(int registerChainId,List<String> verifierList){
        this.registerChainId = registerChainId;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(registerChainId);
        int registerCount = verifierList == null ? 0 : verifierList.size();
        stream.writeVarInt(registerCount);
        if(verifierList != null){
            for (String registerAgent:verifierList) {
                stream.writeString(registerAgent);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.registerChainId = byteBuffer.readUint16();
        int verifierCount = (int) byteBuffer.readVarInt();
        if(verifierCount > 0){
            List<String> verifierList = new ArrayList<>();
            for (int i = 0; i < verifierCount; i++) {
                verifierList.add(byteBuffer.readString());
            }
            this.verifierList = verifierList;
        } }

    @Override
    public int size() {
        int size = SerializeUtils.sizeOfUint16();
        size += SerializeUtils.sizeOfVarInt(verifierList == null ? 0 : verifierList.size());
        if(verifierList != null){
            for (String verifier:verifierList) {
                size += SerializeUtils.sizeOfString(verifier);
            }
        }
        return size;
    }


    public int getRegisterChainId() {
        return registerChainId;
    }

    public void setRegisterChainId(int registerChainId) {
        this.registerChainId = registerChainId;
    }

    public List<String> getVerifierList() {
        return verifierList;
    }

    public void setVerifierList(List<String> verifierList) {
        this.verifierList = verifierList;
    }
}
