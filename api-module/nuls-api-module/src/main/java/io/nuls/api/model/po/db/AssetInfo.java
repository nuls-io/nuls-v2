package io.nuls.api.model.po.db;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AssetInfo {

    private String key;

    private int chainId;

    private int assetId;

    private String symbol;


    public AssetInfo(int chainId, int assetId, String symbol) {
        this.key = chainId + "-" + assetId;
        this.chainId = chainId;
        this.assetId = assetId;
        this.symbol = symbol;
    }

}
