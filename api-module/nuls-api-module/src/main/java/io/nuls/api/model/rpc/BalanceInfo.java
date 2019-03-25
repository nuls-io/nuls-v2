package io.nuls.api.model.rpc;

import lombok.Data;

import java.math.BigInteger;

@Data
public class BalanceInfo {

    private BigInteger totalBalance;

    private BigInteger balance;

    private BigInteger timeLock;

    private BigInteger consensusLock;


}
