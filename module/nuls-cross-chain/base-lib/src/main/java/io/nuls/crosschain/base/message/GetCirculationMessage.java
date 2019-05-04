package io.nuls.crosschain.base.message;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.crosschain.base.message.base.BaseMessage;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

/**
 * 主网向友链获取链资产发行量
 * @author tag
 * @date 2019/4/4
 */
public class GetCirculationMessage extends BaseMessage {
    private String assertIds;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream){
        stream.writeString(assertIds);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.assertIds = byteBuffer.readString();
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfString(assertIds);
        return size;
    }

    public String getAssertIds() {
        return assertIds;
    }

    public void setAssertIds(String assertIds) {
        this.assertIds = assertIds;
    }
}
