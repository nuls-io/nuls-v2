package io.nuls.api.model.po.db;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssetInfo {

    private int chainId;

    private int AssetId;

    private String symbol;

}
