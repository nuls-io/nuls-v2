package io.nuls.api.model.po.db;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@NoArgsConstructor
public class AccountLedgerInfo {

    private String key;

    private String address;

    private int chainId;

    private int assetId;

    private BigInteger totalBalance;

    private boolean isNew;

    public AccountLedgerInfo(String address, int chainId, int assetId) {
        this.key = address + chainId + assetId;
        this.address = address;
        this.chainId = chainId;
        this.assetId = assetId;
        this.totalBalance = BigInteger.ZERO;
        isNew = true;
    }

}
