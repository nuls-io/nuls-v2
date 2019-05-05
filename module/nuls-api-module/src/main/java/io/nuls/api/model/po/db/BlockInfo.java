package io.nuls.api.model.po.db;

import java.util.List;

public class BlockInfo {

    private BlockHeaderInfo header;

    private List<TransactionInfo> txList;

    public BlockHeaderInfo getHeader() {
        return header;
    }

    public void setHeader(BlockHeaderInfo header) {
        this.header = header;
    }

    public List<TransactionInfo> getTxList() {
        return txList;
    }

    public void setTxList(List<TransactionInfo> txList) {
        this.txList = txList;
    }
}
