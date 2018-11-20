package io.nuls.ledger.model;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;

import java.io.IOException;
/**
 * Created by wangkun23 on 2018/11/19.
 */
public class AccountState extends BaseNulsData {

    private short chaiId;

    private long nonce;

    private long balance;

    public AccountState(short chaiId, long nonce, long balance) {
        this.chaiId = chaiId;
        this.nonce = nonce;
        this.balance = balance;
    }

    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeShort(chaiId);
        stream.writeUint32(nonce);
        stream.writeUint32(balance);
    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        this.chaiId = byteBuffer.readShort();
        this.nonce = byteBuffer.readUint32();
        this.balance = byteBuffer.readUint32();
    }

    @Override
    public int size() {
        int size = 0;
        //chainId
        size += 2;
        size += SerializeUtils.sizeOfInt32();
        size += SerializeUtils.sizeOfInt32();
        return size;
    }
}
