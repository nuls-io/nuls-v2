package io.nuls.consensus.utils.manager;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.constant.ConsensusErrorCode;
import io.nuls.consensus.model.bo.Chain;
import io.nuls.consensus.model.bo.tx.txdata.Agent;
import io.nuls.consensus.model.bo.tx.txdata.Deposit;
import io.nuls.consensus.rpc.call.CallMethodUtils;

import java.math.BigInteger;
import java.util.*;

/**
 * CoinData操作工具类
 * CoinData operation tool class
 *
 * @author tag
 * 2018//11/28
 */
@Component
public class CoinDataManager {
    /**
     * 组装CoinData
     * Assemble CoinData
     *
     * @param address      账户地址/Account address
     * @param chain        chain info
     * @param amount       金额/amount
     * @param lockTime     锁定时间/lock time
     * @param txSize       交易大小/transaction size
     * @param assetChainId 抵押资产所属ChainId
     * @param assetId      抵押资产ID
     * @return 组装的CoinData/Assembled CoinData
     */
    public CoinData getCoinData(byte[] address, Chain chain, BigInteger amount, long lockTime, int txSize, int assetChainId, int assetId) throws NulsException {
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo(address, assetChainId, assetId, amount, lockTime);
        coinData.addTo(to);
        txSize += to.size();
        //抵押资产金额
        Map<String, Object> result = CallMethodUtils.getBalanceAndNonce(chain, AddressTool.getStringAddressByBytes(address),assetChainId,assetId);
        byte[] nonce = RPCUtil.decode((String) result.get("nonce"));
        BigInteger available = new BigInteger(result.get("available").toString());
        //验证账户余额是否足够
        CoinFrom from = new CoinFrom(address, assetChainId, assetId, amount, nonce, (byte) 0);
        txSize += from.size();
        BigInteger fee = TransactionFeeCalculator.getConsensusTxFee(txSize,chain.getConfig().getFeeUnit());
        BigInteger fromAmount = amount.add(fee);
        if (BigIntegerUtils.isLessThan(available, fromAmount)) {
            throw new NulsException(ConsensusErrorCode.BANANCE_NOT_ENNOUGH);
        }
        from.setAmount(fromAmount);
        coinData.addFrom(from);
        return coinData;
    }

    /**
     * 组装智能合约CoinData
     * Assemble Contract CoinData
     *
     * @param address   账户地址/Account address
     * @param chain     chain info
     * @param amount    金额/amount
     * @param lockTime  锁定时间/lock time
     * @param nonce     nonce值
     * @param available 账户余额
     * @return 组装的CoinData/Assembled CoinData
     */
    public CoinData getContractCoinData(byte[] address, Chain chain, BigInteger amount, long lockTime, byte[] nonce, BigInteger available) throws NulsException {
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo(address, chain.getConfig().getChainId(), chain.getConfig().getAssetId(), amount, lockTime);
        coinData.addTo(to);
        CoinFrom from = new CoinFrom(address, chain.getConfig().getChainId(), chain.getConfig().getAssetId(), amount, nonce, (byte) 0);
        if (BigIntegerUtils.isLessThan(available, amount)) {
            throw new NulsException(ConsensusErrorCode.BANANCE_NOT_ENNOUGH);
        }
        coinData.addFrom(from);
        return coinData;
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
     * @return 组装的CoinData/Assembled CoinData
     */
    public CoinData getUnlockCoinData(byte[] address, Chain chain, BigInteger amount, long lockTime, int txSize) throws NulsException {
        int agentChainId = chain.getConfig().getAgentChainId();
        int agentAssetId = chain.getConfig().getAssetId();
        Map<String, Object> balanceMap = CallMethodUtils.getBalance(chain, AddressTool.getStringAddressByBytes(address),agentChainId,agentAssetId);
        BigInteger freeze = new BigInteger(balanceMap.get("freeze").toString());
        if (BigIntegerUtils.isLessThan(freeze, amount)) {
            throw new NulsException(ConsensusErrorCode.BANANCE_NOT_ENNOUGH);
        }
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo(address, agentChainId, agentAssetId, amount, lockTime);
        coinData.addTo(to);
        txSize += to.size();
        //手续费
        CoinFrom from = new CoinFrom(address, agentChainId, agentAssetId, amount, (byte) -1);
        coinData.addFrom(from);
        txSize += from.size();
        BigInteger fee = TransactionFeeCalculator.getConsensusTxFee(txSize,chain.getConfig().getFeeUnit());
        BigInteger realToAmount = amount.subtract(fee);
        to.setAmount(realToAmount);
        return coinData;
    }

    /**
     * 组装解锁金额的CoinData（from中nonce为空）
     * Assemble Coin Data for the amount of unlock (from non CE is empty)
     *
     * @param address  账户地址/Account address
     * @param chain    chain info
     * @param amount   金额/amount
     * @param lockTime 锁定时间/lock time
     * @return 组装的CoinData/Assembled CoinData
     */
    public CoinData getContractUnlockCoinData(byte[] address, Chain chain, BigInteger amount, long lockTime) throws NulsException {
        Map<String, Object> balanceMap = CallMethodUtils.getBalance(chain, AddressTool.getStringAddressByBytes(address),chain.getConfig().getChainId(),chain.getConfig().getAssetId());
        BigInteger freeze = new BigInteger(balanceMap.get("freeze").toString());
        if (BigIntegerUtils.isLessThan(freeze, amount)) {
            throw new NulsException(ConsensusErrorCode.BANANCE_NOT_ENNOUGH);
        }
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo(address, chain.getConfig().getChainId(), chain.getConfig().getAssetId(), amount, lockTime);
        coinData.addTo(to);
        //手续费
        CoinFrom from = new CoinFrom(address, chain.getConfig().getChainId(), chain.getConfig().getAssetId(), amount, (byte) -1);
        coinData.addFrom(from);
        to.setAmount(amount);
        return coinData;
    }

    /**
     * 根据节点地址组装停止节点的coinData
     * Assemble coinData of stop node according to node address
     *
     * @param chain    chain info
     * @param address  agent address/节点地址
     * @param lockTime The end point of the lock (lock start time + lock time) is the length of the lock before./锁定的结束时间点(锁定开始时间点+锁定时长)，之前为锁定的时长
     * @return CoinData
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
     * @param chain    chain info
     * @param agent    agent info/节点对象
     * @param lockTime The end point of the lock (lock start time + lock time) is the length of the lock before./锁定的结束时间点(锁定开始时间点+锁定时长)，之前为锁定的时长
     * @return CoinData
     */
    public CoinData getStopAgentCoinData(Chain chain, Agent agent, long lockTime) throws NulsException {
        return getStopAgentCoinData(chain, agent, lockTime, null);
    }

    /**
     * 组装节点CoinData锁定类型为时间或区块高度
     * Assembly node CoinData lock type is time or block height
     *
     * @param chain    chain info
     * @param agent    agent info/节点
     * @param lockTime lock time/锁定时间
     * @param height   lock block height/锁定区块
     * @return CoinData
     */
    private CoinData getStopAgentCoinData(Chain chain, Agent agent, long lockTime, Long height) throws NulsException {
        if (null == agent) {
            return null;
        }
        try {
            int agentChainId = chain.getConfig().getAgentChainId();
            int agentAssetId = chain.getConfig().getAgentAssetId();
            NulsHash createTxHash = agent.getTxHash();
            Transaction createAgentTransaction = CallMethodUtils.getTransaction(chain, createTxHash.toHex());
            if (null == createAgentTransaction) {
                throw new NulsRuntimeException(ConsensusErrorCode.TX_NOT_EXIST);
            }
            CoinData coinData = new CoinData();
            List<CoinTo> toList = new ArrayList<>();
            List<CoinFrom> fromList = new ArrayList<>();
            toList.add(new CoinTo(agent.getAgentAddress(), agentChainId, agentAssetId, agent.getDeposit(), lockTime));
            /*
            根据创建节点交易的CoinData中的输出 组装退出节点交易的输入
            Assemble the input to exit the node transaction based on the output in CoinData that creates the node transaction
            */
            CoinData createCoinData = new CoinData();
            createCoinData.parse(createAgentTransaction.getCoinData(), 0);
            for (CoinTo to : createCoinData.getTo()) {
                CoinFrom from = new CoinFrom(agent.getAgentAddress(), agentChainId, agentAssetId);
                if (to.getAmount().compareTo(agent.getDeposit()) == 0 && to.getLockTime() == -1L) {
                    from.setAmount(to.getAmount());
                    from.setLocked((byte) -1);
                    from.setNonce(CallMethodUtils.getNonce(createTxHash.getBytes()));
                    fromList.add(from);
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
            Map<String, CoinTo> toMap = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            long blockHeight = null == height ? -1 : height;
            for (Deposit deposit : deposits) {
                if (deposit.getDelHeight() > 0 && (blockHeight <= 0 || deposit.getDelHeight() < blockHeight)) {
                    continue;
                }
                if (!deposit.getAgentHash().equals(agent.getTxHash())) {
                    continue;
                }
                Transaction depositTransaction = CallMethodUtils.getTransaction(chain, deposit.getTxHash().toHex());
                CoinData depositCoinData = new CoinData();
                depositCoinData.parse(depositTransaction.getCoinData(), 0);
                CoinFrom from;
                for (CoinTo to : depositCoinData.getTo()) {
                    if (!BigIntegerUtils.isEqual(to.getAmount(), deposit.getDeposit()) || to.getLockTime() != -1L) {
                        continue;
                    }
                    byte[] nonce = CallMethodUtils.getNonce(deposit.getTxHash().getBytes());
                    from = new CoinFrom(deposit.getAddress(), agentChainId, agentAssetId, to.getAmount(), nonce, (byte) -1);
                    fromList.add(from);
                    break;
                }
                String address = AddressTool.getStringAddressByBytes(deposit.getAddress());
                CoinTo coinTo = toMap.get(address);
                if (coinTo == null) {
                    coinTo = new CoinTo(deposit.getAddress(), agentChainId, agentAssetId, deposit.getDeposit(), 0);
                    toMap.put(address, coinTo);
                } else {
                    coinTo.setAmount(coinTo.getAmount().add(deposit.getDeposit()));
                }
            }
            toList.addAll(toMap.values());
            coinData.setTo(toList);
            return coinData;
        } catch (NulsException e) {
            chain.getLogger().error(e.getMessage());
            throw e;
        }
    }

    /**
     * 查看CoinBase交易中是否存在智能合约账户
     *
     * @param coinData coinData
     * @param chainId  chainId
     */
    public boolean hasContractAddress(CoinData coinData, int chainId) {
        for (CoinTo coinTo : coinData.getTo()) {
            if (AddressTool.validContractAddress(coinTo.getAddress(), chainId)) {
                return true;
            }
        }
        return false;
    }
}
