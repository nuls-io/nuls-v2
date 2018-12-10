package io.nuls.poc.utils.manager;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

import java.math.BigInteger;
import java.util.*;

/**
 * CoinData操作工具类
 * CoinData operation tool class
 * @author tag
 * 2018//11/28
 * */
@Component
public class CoinDataManager {
    /**
     * 组装CoinData
     * Assemble CoinData
     *
     * @param address  账户地址/Account address
     * @param chain    chain info
     * @param amount   金额/amount
     * @param lockTime 锁定时间/lock time
     * @param txSize   交易大小/transaction size
     * @return         组装的CoinData/Assembled CoinData
     * */
    public CoinData getCoinData(byte[] address,Chain chain, BigInteger amount, long lockTime, int txSize)throws NulsRuntimeException{
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo(address,chain.getConfig().getChainId(),chain.getConfig().getAssetsId(),amount, lockTime);
        coinData.addTo(to);
        txSize += to.size();
        //todo 账本模块获取nonce 可用余额
        byte[] nonce = new byte[8];
        BigInteger available = new BigInteger("50000000000");
        //手续费
        CoinFrom from = new CoinFrom(address,chain.getConfig().getChainId(),chain.getConfig().getAssetsId(),amount,nonce, (byte)0);
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
     * @param chain    chain info
     * @param amount   金额/amount
     * @param lockTime 锁定时间/lock time
     * @param txSize   交易大小/transaction size
     * @return         组装的CoinData/Assembled CoinData
     * */
    public CoinData getUnlockCoinData(byte[] address,Chain chain, BigInteger amount, long lockTime, int txSize)throws NulsRuntimeException{
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo(address,chain.getConfig().getChainId(),chain.getConfig().getAssetsId(),amount, lockTime);
        coinData.addTo(to);
        txSize += to.size();
        //todo 账本模块获取该账户锁定金额
        BigInteger available = new BigInteger("10000");
        //手续费
        CoinFrom from = new CoinFrom(address,chain.getConfig().getChainId(),chain.getConfig().getAssetsId(),amount,(byte)-1);
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
     * 根据节点地址组装停止节点的coinData
     * Assemble coinData of stop node according to node address
     *
     * @param chain      chain info
     * @param address    agent address/节点地址
     * @param lockTime   The end point of the lock (lock start time + lock time) is the length of the lock before./锁定的结束时间点(锁定开始时间点+锁定时长)，之前为锁定的时长
     * @return  CoinData
     */
    public CoinData getStopAgentCoinData(Chain chain, byte[] address, long lockTime) throws NulsException {
        List<Agent> agentList = chain.getAgentList();
        for (Agent agent : agentList) {
            if (agent.getDelHeight() > 0) {
                continue;
            }
            if (Arrays.equals(address, agent.getAgentAddress())) {
                return getStopAgentCoinData(chain, agent, lockTime);
            }
        }
        return null;
    }

    /**
     * 根据节点组装停止节点的coinData
     * Assemble the coinData of the stop node according to the node
     *
     * @param chain      chain info
     * @param agent      agent info/节点对象
     * @param lockTime   The end point of the lock (lock start time + lock time) is the length of the lock before./锁定的结束时间点(锁定开始时间点+锁定时长)，之前为锁定的时长
     * @return CoinData
     */
    public CoinData getStopAgentCoinData(Chain chain, Agent agent, long lockTime) throws NulsException {
        return getStopAgentCoinData(chain, agent, lockTime, null);
    }

    /**
     * 组装节点CoinData锁定类型为时间或区块高度
     * Assembly node CoinData lock type is time or block height
     *
     * @param chain       chain info
     * @param agent       agent info/节点
     * @param lockTime    lock time/锁定时间
     * @param height      lock block height/锁定区块
     * @return CoinData
     */
    public CoinData getStopAgentCoinData(Chain chain, Agent agent, long lockTime, Long height) throws NulsException{
        if (null == agent) {
            return null;
        }
        try {
            int chainId = chain.getConfig().getChainId();
            int assetsId = chain.getConfig().getAssetsId();
            //todo 从交易模块获取创建该节点时的交易
            NulsDigestData createTxHash = agent.getTxHash();
            Transaction createAgentTransaction = null;
            if (null == createAgentTransaction) {
                throw new NulsRuntimeException(ConsensusErrorCode.TX_NOT_EXIST);
            }
            CoinData coinData = new CoinData();
            List<CoinTo> toList = new ArrayList<>();
            List<CoinFrom> fromList = new ArrayList<>();
            toList.add(new CoinTo(agent.getAgentAddress(),chainId,assetsId,agent.getDeposit(), lockTime));
            coinData.setTo(toList);

            /*
            根据创建节点交易的CoinData中的输出 组装退出节点交易的输入
            Assemble the input to exit the node transaction based on the output in CoinData that creates the node transaction
            */
            CoinData createCoinData = new CoinData();
            createCoinData.parse(createAgentTransaction.getCoinData(),0);
            for (CoinTo to:createCoinData.getTo()) {
                CoinFrom from = new CoinFrom(agent.getAgentAddress(),chainId,assetsId);
                if(to.getAmount().compareTo(agent.getTotalDeposit()) == 0 && to.getLockTime() == -1L){
                    from.setAmount(to.getAmount());
                    from.setLocked((byte)-1);
                    from.setNonce(createTxHash.getDigestBytes());
                }
            }
            if (fromList.isEmpty()) {
                throw new NulsRuntimeException(ConsensusErrorCode.DATA_ERROR);
            }
            coinData.setFrom(fromList);

            /*
            获取该节点的委托信息，并将委托金额返回给委托人
            Obtain the delegation information of the node and return the amount of the delegation to the principal
            */
            List<Deposit> deposits = chain.getDepositList();
            List<String> addressList = new ArrayList<>();
            Map<String, CoinTo> toMap = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            long blockHeight = null == height ? -1 : height;
            for (Deposit deposit : deposits) {
                if (deposit.getDelHeight() > 0 && (blockHeight <= 0 || deposit.getDelHeight() < blockHeight)) {
                    continue;
                }
                if (!deposit.getAgentHash().equals(agent.getTxHash())) {
                    continue;
                }
                //todo  从交易管理模块获取委托交易 deposit.getTxHash()
                Transaction depositTransaction = null;
                CoinData depositCoinData = new CoinData();
                depositCoinData.parse(depositTransaction.getCoinData(), 0);
                CoinFrom from = null;
                for (CoinTo to:depositCoinData.getTo()) {
                    if (!BigIntegerUtils.isEqual(to.getAmount(),deposit.getDeposit()) || to.getLockTime() != -1L) {
                        continue;
                    }
                    byte[] nonce = deposit.getTxHash().getDigestBytes();
                    from = new CoinFrom(deposit.getAddress(),chainId,assetsId,to.getAmount(),nonce,(byte)-1);
                    fromList.add(from);
                    break;
                }
                String address = AddressTool.getStringAddressByBytes(deposit.getAddress());
                CoinTo coinTo = toMap.get(address);
                if(coinTo == null){
                    coinTo = new CoinTo(deposit.getAddress(),chainId,assetsId,deposit.getDeposit(),0);
                    toMap.put(address,coinTo);
                    addressList.add(address);
                }else{
                    coinTo.setAmount(coinTo.getAmount().add(deposit.getDeposit()));
                }
            }
            for (String address : addressList) {
                coinData.getTo().add(toMap.get(address));
            }
            return coinData;
        } catch (NulsException e) {
            Log.error(e);
            throw e;
        }
    }
}
