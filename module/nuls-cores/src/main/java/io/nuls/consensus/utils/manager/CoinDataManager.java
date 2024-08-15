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
 * CoinDataOperational tools
 * CoinData operation tool class
 *
 * @author tag
 * 2018//11/28
 */
@Component
public class CoinDataManager {
    /**
     * assembleCoinData
     * Assemble CoinData
     *
     * @param address      Account address/Account address
     * @param chain        chain info
     * @param amount       money/amount
     * @param lockTime     Lock time/lock time
     * @param txSize       Transaction size/transaction size
     * @param assetChainId Mortgage asset ownershipChainId
     * @param assetId      Mortgage assetsID
     * @return AssembledCoinData/Assembled CoinData
     */
    public CoinData getCoinData(byte[] address, Chain chain, BigInteger amount, long lockTime, int txSize, int assetChainId, int assetId) throws NulsException {
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo(address, assetChainId, assetId, amount, lockTime);
        coinData.addTo(to);
        txSize += to.size();
        //Mortgage asset amount
        Map<String, Object> result = CallMethodUtils.getBalanceAndNonce(chain, AddressTool.getStringAddressByBytes(address), assetChainId, assetId);
        byte[] nonce = RPCUtil.decode((String) result.get("nonce"));
        BigInteger available = new BigInteger(result.get("available").toString());
        //Verify if the account balance is sufficient
        CoinFrom from = new CoinFrom(address, assetChainId, assetId, amount, nonce, (byte) 0);
        txSize += from.size();
        BigInteger fee = TransactionFeeCalculator.getConsensusTxFee(txSize, chain.getConfig().getFeeUnit(assetChainId, assetId));
        BigInteger fromAmount = amount.add(fee);
        if (BigIntegerUtils.isLessThan(available, fromAmount)) {
            throw new NulsException(ConsensusErrorCode.BANANCE_NOT_ENNOUGH);
        }
        from.setAmount(fromAmount);
        coinData.addFrom(from);
        return coinData;
    }

    /**
     * Assembling smart contractsCoinData
     * Assemble Contract CoinData
     *
     * @param address   Account address/Account address
     * @param chain     chain info
     * @param amount    money/amount
     * @param lockTime  Lock time/lock time
     * @param nonce     noncevalue
     * @param available Account balance
     * @return AssembledCoinData/Assembled CoinData
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
     * Assembly unlocking amountCoinData（frominnonceEmpty）
     * Assemble Coin Data for the amount of unlock (from non CE is empty)
     *
     * @param address  Account address/Account address
     * @param chain    chain info
     * @param amount   money/amount
     * @param lockTime Lock time/lock time
     * @param txSize   Transaction size/transaction size
     * @return AssembledCoinData/Assembled CoinData
     */
    public CoinData getUnlockCoinData(byte[] address, Chain chain, BigInteger amount, long lockTime, int txSize) throws NulsException {
        int agentChainId = chain.getConfig().getAgentChainId();
        int agentAssetId = chain.getConfig().getAssetId();
        Map<String, Object> balanceMap = CallMethodUtils.getBalance(chain, AddressTool.getStringAddressByBytes(address), agentChainId, agentAssetId);
        BigInteger freeze = new BigInteger(balanceMap.get("freeze").toString());
        if (BigIntegerUtils.isLessThan(freeze, amount)) {
            throw new NulsException(ConsensusErrorCode.BANANCE_NOT_ENNOUGH);
        }
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo(address, agentChainId, agentAssetId, amount, lockTime);
        coinData.addTo(to);
        txSize += to.size();
        //Handling fees
        CoinFrom from = new CoinFrom(address, agentChainId, agentAssetId, amount, (byte) -1);
        coinData.addFrom(from);
        txSize += from.size();
        BigInteger fee = TransactionFeeCalculator.getConsensusTxFee(txSize, chain.getConfig().getFeeUnit(agentChainId, agentAssetId));
        BigInteger realToAmount = amount.subtract(fee);
        to.setAmount(realToAmount);
        return coinData;
    }

    /**
     * Assembly unlocking amountCoinData（frominnonceEmpty）
     * Assemble Coin Data for the amount of unlock (from non CE is empty)
     *
     * @param address  Account address/Account address
     * @param chain    chain info
     * @param amount   money/amount
     * @param lockTime Lock time/lock time
     * @return AssembledCoinData/Assembled CoinData
     */
    public CoinData getContractUnlockCoinData(byte[] address, Chain chain, BigInteger amount, long lockTime) throws NulsException {
        Map<String, Object> balanceMap = CallMethodUtils.getBalance(chain, AddressTool.getStringAddressByBytes(address), chain.getConfig().getChainId(), chain.getConfig().getAssetId());
        BigInteger freeze = new BigInteger(balanceMap.get("freeze").toString());
        if (BigIntegerUtils.isLessThan(freeze, amount)) {
            throw new NulsException(ConsensusErrorCode.BANANCE_NOT_ENNOUGH);
        }
        CoinData coinData = new CoinData();
        CoinTo to = new CoinTo(address, chain.getConfig().getChainId(), chain.getConfig().getAssetId(), amount, lockTime);
        coinData.addTo(to);
        //Handling fees
        CoinFrom from = new CoinFrom(address, chain.getConfig().getChainId(), chain.getConfig().getAssetId(), amount, (byte) -1);
        coinData.addFrom(from);
        to.setAmount(amount);
        return coinData;
    }

    /**
     * Assemble stop nodes based on their addressescoinData
     * Assemble coinData of stop node according to node address
     *
     * @param chain    chain info
     * @param address  agent address/Node address
     * @param lockTime The end point of the lock (lock start time + lock time) is the length of the lock before./Locked end time point(Lock start time point+Lock duration), previously locked for a certain duration
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
     * Stop node assembly based on node assemblycoinData
     * Assemble the coinData of the stop node according to the node
     *
     * @param chain    chain info
     * @param agent    agent info/Node Object
     * @param lockTime The end point of the lock (lock start time + lock time) is the length of the lock before./Locked end time point(Lock start time point+Lock duration), previously locked for a certain duration
     * @return CoinData
     */
    public CoinData getStopAgentCoinData(Chain chain, Agent agent, long lockTime) throws NulsException {
        return getStopAgentCoinData(chain, agent, lockTime, null);
    }

    /**
     * Assembly nodesCoinDataLock type is time or block height
     * Assembly node CoinData lock type is time or block height
     *
     * @param chain    chain info
     * @param agent    agent info/node
     * @param lockTime lock time/Lock time
     * @param height   lock block height/Lock block
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
            Based on the creation of node transactionsCoinDataOutput in Input for assembling exit node transactions
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
            Obtain the delegation information of the node and return the delegation amount to the principal
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
     * checkCoinBaseIs there a smart contract account in the transaction
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
