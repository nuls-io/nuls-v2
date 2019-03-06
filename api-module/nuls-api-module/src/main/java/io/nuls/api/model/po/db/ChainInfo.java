package io.nuls.api.model.po.db;

import lombok.Data;

import java.util.Set;

@Data
public class ChainInfo {

    private int chainId;

    private AssetInfo defaultAsset;

    private Set<AssetInfo> assets;

}
