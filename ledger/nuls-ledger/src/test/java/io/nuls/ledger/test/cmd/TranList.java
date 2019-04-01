package io.nuls.ledger.test.cmd;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.Transaction;
import io.nuls.ledger.model.po.AccountState;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.SerializeUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TranList extends BaseNulsData {
    @Setter
    @Getter
    private List<Transaction> txs = new ArrayList<>();



    @Override
    protected void serializeToStream(NulsOutputStreamBuffer stream) throws IOException {
        stream.writeUint16(txs.size());
        for (Transaction tr : txs) {
            stream.writeNulsData(tr);
        }

    }

    @Override
    public void parse(NulsByteBuffer byteBuffer) throws NulsException {
        int c = byteBuffer.readUint16();
        for (int i = 0; i < c; i++) {
            this.txs.add(byteBuffer.readNulsData(new Transaction()));
        }
    }

    @Override
    public int size() {
        int size = 0;
        size += SerializeUtils.sizeOfUint16();
        for (Transaction tx : txs) {
            size += SerializeUtils.sizeOfNulsData(tx);
        }
        return size;
    }
}
