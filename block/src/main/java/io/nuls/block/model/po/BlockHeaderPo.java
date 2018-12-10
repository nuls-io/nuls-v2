package io.nuls.block.model.po;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.Block;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.NulsDigestData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.List;

/**
 * 区块头存储对象
 *
 * @author captain
 * @date 18-12-10 下午3:50
 * @version 1.0
 */
public class BlockHeaderPo extends BlockHeader {

    @Getter @Setter
    private List<NulsDigestData> txHashList;

    @Override
    public int size() {
        int size = super.size();
        for (NulsDigestData hash : txHashList) {
            size += SerializeUtils.sizeOfNulsData(hash);
        }
        return size;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        super.serializeToStream(stream);
        for (NulsDigestData hash : txHashList) {
            stream.writeNulsData(hash);
        }
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        super.parse(byteBuffer);
        for (int i = 0; i < getTxCount(); i++) {
            this.txHashList.add(byteBuffer.readHash());
        }
    }

}
