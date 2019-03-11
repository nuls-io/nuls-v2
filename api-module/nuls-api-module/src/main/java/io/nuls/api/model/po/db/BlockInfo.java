package io.nuls.api.model.po.db;

import lombok.Data;

import java.util.List;

@Data
public class BlockInfo {

    private BlockHeaderInfo header;

    private List<TransactionInfo> txList;

}
