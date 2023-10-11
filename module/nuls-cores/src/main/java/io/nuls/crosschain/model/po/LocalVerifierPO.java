package io.nuls.crosschain.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LocalVerifierPO extends BaseNulsData {
    List<String> verifierList;

    public  LocalVerifierPO(){}

    public LocalVerifierPO(List<String> verifierList){
        this.verifierList = verifierList;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        int verifierCount = verifierList == null ? 0 : verifierList.size();
        stream.writeVarInt(verifierCount);
        if (null != verifierList) {
            for (String verifier : verifierList) {
                stream.writeString(verifier);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int verifierCount = (int) byteBuffer.readVarInt();

        if (0 < verifierCount) {
            List<String> verifierList = new ArrayList<>();
            for (int i = 0; i < verifierCount; i++) {
                verifierList.add(byteBuffer.readString());
            }
            this.verifierList = verifierList;
        }
    }

    @Override
    public int size() {
        int size = SerializeUtils.sizeOfVarInt(verifierList == null ? 0 : verifierList.size());
        if (null != verifierList) {
            for (String verifier : verifierList) {
                size += SerializeUtils.sizeOfString(verifier);
            }
        }
        return size;
    }

    public List<String> getVerifierList() {
        return verifierList;
    }

    public void setVerifierList(List<String> verifierList) {
        this.verifierList = verifierList;
    }
}
