package io.nuls.crosschain.base.model.bo.txdata;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.crosschain.base.message.base.BaseMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 已注册链信息
 * @author tag
 * @date 2019/5/17
 */
public class RegisteredChainMessage extends BaseMessage {
    private List<ChainInfo> chainInfoList;

    public RegisteredChainMessage(){}

    public RegisteredChainMessage(List<ChainInfo> chainInfoList){
        this.chainInfoList = chainInfoList;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        if(chainInfoList != null && chainInfoList.size() > 0){
            for (ChainInfo chainInfo:chainInfoList) {
                stream.writeNulsData(chainInfo);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        List<ChainInfo> chainInfoList = new ArrayList<>();
        int course;
        while (!byteBuffer.isFinished()) {
            course = byteBuffer.getCursor();
            byteBuffer.setCursor(course);
            chainInfoList.add(byteBuffer.readNulsData(new ChainInfo()));
        }
        this.chainInfoList = chainInfoList;
    }

    @Override
    public int size() {
        int size = 0;
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

    public void setChainInfoList(List<ChainInfo> chainInfoList) {
        this.chainInfoList = chainInfoList;
    }

    public void addChainInfo(ChainInfo chainInfo){
        chainInfoList.remove(chainInfo);
        chainInfoList.add(chainInfo);
    }

    public boolean haveOtherChain(int registerChainId, int mainChainId){
        for (ChainInfo chainInfo : chainInfoList){
            if(chainInfo.getChainId() != registerChainId && chainInfo.getChainId() != mainChainId){
                return true;
            }
        }
        return false;
    }
}
