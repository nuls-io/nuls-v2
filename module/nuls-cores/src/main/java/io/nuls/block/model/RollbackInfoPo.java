package io.nuls.block.model;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

public class RollbackInfoPo extends BaseNulsData {
    private long height;

    public RollbackInfoPo(){
    }

    public RollbackInfoPo(long height){
        this.height = height;
    }

    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeVarInt(height);
    }

    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.height = byteBuffer.readVarInt();
    }

    public int size() {
        return  SerializeUtils.sizeOfVarInt(height);
    }

    public long getHeight() {
        return height;
    }

    public void setHeight(long height) {
        this.height = height;
    }
}
