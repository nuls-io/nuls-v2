package io.nuls.crosschain.base.message;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.core.constant.ToolsConstant;
import io.nuls.crosschain.base.message.base.BaseMessage;

import java.io.IOException;

/**
 * 查询已注册跨链交易信息
 * @author tag
 * @date 2019/5/17
 */
public class GetRegisteredChainMessage extends BaseMessage {

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeNulsData(null);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer){

    }

    @Override
    public int size() {
        return ToolsConstant.PLACE_HOLDER.length;
    }
}
