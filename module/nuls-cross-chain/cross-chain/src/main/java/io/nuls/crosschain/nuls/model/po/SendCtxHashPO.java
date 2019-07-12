package io.nuls.crosschain.nuls.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.NulsHash;
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
 */
public class SendCtxHashPO extends BaseNulsData {

    private List<NulsHash> hashList = new ArrayList<>();

    public SendCtxHashPO() {

    }

    public SendCtxHashPO(List<NulsHash> hashList) {
        this.hashList = hashList;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        if (hashList != null && hashList.size() > 0) {
            for (NulsHash hash : hashList) {
                stream.write(hash.getBytes());
            }
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int course;
        List<NulsHash> hashList = new ArrayList<>();
        while (!byteBuffer.isFinished()) {
            course = byteBuffer.getCursor();
            byteBuffer.setCursor(course);
            hashList.add(byteBuffer.readHash());
        }
        this.hashList = hashList;
    }

    @Override
    public int size() {
        int size = 0;
        if (hashList != null && hashList.size() > 0) {
            for (NulsHash hash : hashList) {
                size += NulsHash.HASH_LENGTH;
            }
        }
        return size;
    }

    public List<NulsHash> getHashList() {
        return hashList;
    }

    public void setHashList(List<NulsHash> hashList) {
        this.hashList = hashList;
    }
}
