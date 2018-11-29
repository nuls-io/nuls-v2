package io.nuls.poc.utils.util;

import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.exception.NulsRuntimeException;
/**
 * @author tag
 * 2018//11/28
 * */
public class CoinDataUtil {
    /**
     * 组装CoinData
     * @param address  商户地址
     * @param chainId  链ID
     * @param assetsId 资产ID
     * @param amount   金额
     * @param lockTime 锁定时间
     * @param txSize   交易大小
     * @return         组装的CoinData
     * */
    public static CoinData getCoinData(byte[] address,int chainId,int assetsId, String amount, long lockTime, int txSize)throws NulsRuntimeException{
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo(address,chainId,assetsId,amount, lockTime);
        coinData.addTo(to);
        txSize += to.size();
        //todo 账本模块获取nonce 可用余额
        byte[] nonce = null;
        String available = "10000";
        //手续费
        CoinFrom from = new CoinFrom(address,chainId,assetsId,amount,nonce, 0);
        txSize += from.size();
        String fee = TransactionFeeCalculator.getMaxFee(txSize);
        String fromAmount = BigIntegerUtils.addToString(amount ,fee);
        if(BigIntegerUtils.isLessThan(available,fromAmount)){
            throw new NulsRuntimeException(ConsensusErrorCode.BANANCE_NOT_ENNOUGH);
        }
        from.setAmount(fromAmount);
        coinData.addFrom(from);
        return  coinData;
    }

    /**
     * 组装解锁金额的CoinData（from中nonce为空）
     * @param address  商户地址
     * @param chainId  链ID
     * @param assetsId 资产ID
     * @param amount   金额
     * @param lockTime 锁定时间
     * @param txSize   交易大小
     * @return         组装的CoinData
     * */
    public static CoinData getUnlockCoinData(byte[] address,int chainId,int assetsId, String amount, long lockTime, int txSize)throws NulsRuntimeException{
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo(address,chainId,assetsId,amount, lockTime);
        coinData.addTo(to);
        txSize += to.size();
        //todo 账本模块获取该账户锁定金额
        String available = "10000";
        //手续费
        CoinFrom from = new CoinFrom(address,chainId,assetsId,amount,-1);
        txSize += from.size();
        String fee = TransactionFeeCalculator.getMaxFee(txSize);
        String fromAmount = BigIntegerUtils.addToString(amount ,fee);
        if(BigIntegerUtils.isLessThan(available,fromAmount)){
            throw new NulsRuntimeException(ConsensusErrorCode.BANANCE_NOT_ENNOUGH);
        }
        from.setAmount(fromAmount);
        coinData.addFrom(from);
        return  coinData;
    }
}
