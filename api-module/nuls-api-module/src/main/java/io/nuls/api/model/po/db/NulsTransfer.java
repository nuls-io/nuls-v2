package io.nuls.api.model.po.db;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class NulsTransfer {

    private String txHash;

    private String from;

    private String value;

    private List<Map<String, Object>> outputs;
}
