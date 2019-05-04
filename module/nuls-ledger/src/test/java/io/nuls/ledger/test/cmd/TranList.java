package io.nuls.ledger.test.cmd;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.NulsOutputStreamBuffer;
import io.nuls.base.data.BaseNulsData;
import io.nuls.base.data.Transaction;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.SerializeUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class TranList extends BaseNulsData {
    private List<Transaction> txs = new ArrayList<>();

    public List<Transaction> getTxs() {
        return txs;
    }

    public void setTxs(List<Transaction> txs) {
        this.txs = txs;
    }

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
