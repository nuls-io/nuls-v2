package io.nuls.poc.utils.util;

import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.exception.NulsRuntimeException;

import java.math.BigInteger;

/**
 * CoinData操作工具类
 * CoinData operation tool class
 * @author tag
 * 2018//11/28
 * */
public class CoinDataUtil {
    /**
     * 组装CoinData
     * Assemble CoinData
     *
     * @param address  账户地址/Account address
     * @param chainId  链ID/chain id
     * @param assetsId 资产ID/assets id
     * @param amount   金额/amount
     * @param lockTime 锁定时间/lock time
     * @param txSize   交易大小/transaction size
     * @return         组装的CoinData/Assembled CoinData
     * */
    public static CoinData getCoinData(byte[] address,int chainId,int assetsId, BigInteger amount, long lockTime, int txSize)throws NulsRuntimeException{
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo(address,chainId,assetsId,amount, lockTime);
        coinData.addTo(to);
        txSize += to.size();
        //todo 账本模块获取nonce 可用余额
        byte[] nonce = null;
        BigInteger available = new BigInteger("50000000000");
        //手续费
        CoinFrom from = new CoinFrom(address,chainId,assetsId,amount,nonce, (byte)0);
        txSize += from.size();
        BigInteger fee = TransactionFeeCalculator.getMaxFee(txSize);
        BigInteger fromAmount = amount.add(fee);
        if(BigIntegerUtils.isLessThan(available,fromAmount)){
            throw new NulsRuntimeException(ConsensusErrorCode.BANANCE_NOT_ENNOUGH);
        }
        from.setAmount(fromAmount);
        coinData.addFrom(from);
        return  coinData;
    }

    /**
     * 组装解锁金额的CoinData（from中nonce为空）
     * Assemble Coin Data for the amount of unlock (from non CE is empty)
     *
     * @param address  账户地址/Account address
     * @param chainId  链ID/chain id
     * @param assetsId 资产ID/assets id
     * @param amount   金额/amount
     * @param lockTime 锁定时间/lock time
     * @param txSize   交易大小/transaction size
     * @return         组装的CoinData/Assembled CoinData
     * */
    public static CoinData getUnlockCoinData(byte[] address, int chainId, int assetsId, BigInteger amount, long lockTime, int txSize)throws NulsRuntimeException{
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo(address,chainId,assetsId,amount, lockTime);
        coinData.addTo(to);
        txSize += to.size();
        //todo 账本模块获取该账户锁定金额
        BigInteger available = new BigInteger("10000");
        //手续费
        CoinFrom from = new CoinFrom(address,chainId,assetsId,amount,(byte)-1);
        txSize += from.size();
        BigInteger fee = TransactionFeeCalculator.getMaxFee(txSize);
        BigInteger fromAmount = amount.add(fee);
        if(BigIntegerUtils.isLessThan(available,fromAmount)){
            throw new NulsRuntimeException(ConsensusErrorCode.BANANCE_NOT_ENNOUGH);
        }
        from.setAmount(fromAmount);
        coinData.addFrom(from);
        return  coinData;
    }
}
