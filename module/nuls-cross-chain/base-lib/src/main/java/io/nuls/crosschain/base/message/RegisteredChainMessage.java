package io.nuls.crosschain.base.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.crosschain.base.message.base.BaseMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 已注册链信息
 * @author tag
 * @date 2019/5/17
 */
public class RegisteredChainMessage extends BaseMessage {
    private List<ChainInfo> chainInfoList;

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

    public List<ChainInfo> getChainInfoList() {
        return chainInfoList;
    }

    public void setChainInfoList(List<ChainInfo> chainInfoList) {
        this.chainInfoList = chainInfoList;
    }
}
