package io.nuls.poc.model.bo;

import java.math.BigInteger;

/**
 * 交易手续费返回结果类
 * Transaction Fee Return Result Class
 *
 * @author tag
 * */
public class ChargeResultData {
    private BigInteger fee;
    private int chainId;

    public ChargeResultData(BigInteger fee, int chainId) {
        this.fee = fee;
        this.chainId = chainId;
    }

    public BigInteger getFee() {
        return fee;
    }

    public void setFee(BigInteger fee) {
        this.fee = fee;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }
}
