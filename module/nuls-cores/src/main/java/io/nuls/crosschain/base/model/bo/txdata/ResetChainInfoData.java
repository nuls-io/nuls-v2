package io.nuls.crosschain.base.model.bo.txdata;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;

public class ResetChainInfoData extends BaseNulsData {
    private String json;

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeString(json);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        json = byteBuffer.readString();
    }

    @Override
    public int size() {
        return SerializeUtils.sizeOfString(json);
    }

    public String getJson() {
        return json;
    }

    public void setJson(String json) {
        this.json = json;
    }
}
