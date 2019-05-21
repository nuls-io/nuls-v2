package io.nuls.crosschain.nuls.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
/**
 * 存入数据库的交易Hash列表
 * Transaction Hash List in Database
 *
 * @author tag
 * 2019/4/15
 * */
public class SendCtxHashPo extends BaseNulsData {

    private List<NulsDigestData> hashList = new ArrayList<>();

    public  SendCtxHashPo(){

    }

    public SendCtxHashPo(List<NulsDigestData> hashList){
        this.hashList = hashList;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        if (hashList != null && hashList.size() > 0) {
            for (NulsDigestData nulsDigestData : hashList) {
                stream.writeNulsData(nulsDigestData);
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int course;
        List<NulsDigestData> hashList = new ArrayList<>();
        while (!byteBuffer.isFinished()) {
            course = byteBuffer.getCursor();
            byteBuffer.setCursor(course);
            hashList.add(byteBuffer.readNulsData(new NulsDigestData()));
        }
        this.hashList = hashList;
    }

    @Override
    public int size() {
        int size = 0;
        if (hashList != null && hashList.size() > 0) {
            for (NulsDigestData nulsDigestData : hashList) {
                size +=  SerializeUtils.sizeOfNulsData(nulsDigestData);
            }
        }
        return size;
    }

    public List<NulsDigestData> getHashList() {
        return hashList;
    }

    public void setHashList(List<NulsDigestData> hashList) {
        this.hashList = hashList;
    }
}
