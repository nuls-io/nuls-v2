package io.nuls.poc.tx.v3;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.RedPunishData;
import io.nuls.poc.model.bo.tx.txdata.StopAgent;
import io.nuls.poc.model.po.AgentPo;
import io.nuls.poc.rpc.call.CallMethodUtils;
import io.nuls.poc.storage.AgentStorageService;
import io.nuls.poc.utils.LoggerUtil;
import io.nuls.poc.utils.manager.AgentManager;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.poc.utils.validator.TxValidator;

import java.io.IOException;
import java.util.*;

/**
 * 停止节点交易处理器
 *
 * @author tag
 * @date 2019/12/2
 */

@Component("StopAgentProcessorV3")
public class StopAgentProcessor implements TransactionProcessor {
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxValidator txValidator;
    @Autowired
    private AgentStorageService agentStorageService;

    @Override
    public int getType() {
        return TxType.STOP_AGENT;
    }

    @Override
    public int getPriority() {
        return 6;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        Map<String, Object> result = new HashMap<>(2);
        if (chain == null) {
            LoggerUtil.commonLog.error("Chains do not exist.");
            result.put("txList", txs);
            result.put("errorCode", ConsensusErrorCode.CHAIN_NOT_EXIST.getCode());
            return result;
        }
        List<Transaction> invalidTxList = new ArrayList<>();
        String errorCode = null;
        Set<String> redPunishAddressSet = new HashSet<>();
        Set<NulsHash> hashSet = new HashSet<>();
        List<Transaction> redPunishTxList = txMap.get(TxType.RED_PUNISH);
        if (redPunishTxList != null && redPunishTxList.size() > 0) {
            for (Transaction redPunishTx : redPunishTxList) {
                RedPunishData redPunishData = new RedPunishData();
                try {
                    redPunishData.parse(redPunishTx.getTxData(), 0);
                    String addressHex = HexUtil.encode(redPunishData.getAddress());
                    redPunishAddressSet.add(addressHex);
                } catch (NulsException e) {
                    chain.getLogger().error(e);
                }
            }
        }
        List<Transaction> contractStopAgentTxList = txMap.get(TxType.CONTRACT_STOP_AGENT);
        if (contractStopAgentTxList != null && contractStopAgentTxList.size() > 0) {
            try {
                for (Transaction contractStopAgentTx : contractStopAgentTxList) {
                    StopAgent stopAgent = new StopAgent();
                    stopAgent.parse(contractStopAgentTx.getTxData(), 0);
                    hashSet.add(stopAgent.getCreateTxHash());
                }
            } catch (Exception e) {
                chain.getLogger().error(e);
            }
        }
        for (Transaction stopAgentTx : txs) {
            try {
                if (!txValidator.validateTx(chain, stopAgentTx)) {
                    invalidTxList.add(stopAgentTx);
                    chain.getLogger().info("Intelligent Contract Exit Node Trading Verification Failed");
                    continue;
                }
                //验证交易签名是否为节点创建者签名
                StopAgent stopAgent = new StopAgent();
                stopAgent.parse(stopAgentTx.getTxData(), 0);
                AgentPo agentPo = agentStorageService.get(stopAgent.getCreateTxHash(), chain.getConfig().getChainId());
                if (!verifyV3(chain, stopAgentTx, agentPo.getAgentAddress())) {
                    chain.getLogger().error("Stop node signature verification failed");
                    continue;
                }
                //验证停止节点交易时间正确性
                long time = NulsDateUtils.getCurrentTimeSeconds();
                if (blockHeader != null) {
                    time = blockHeader.getTime();
                }
                long txTime = stopAgentTx.getTime();
                if (txTime > time + 3600 || txTime < time - 3600) {
                    invalidTxList.add(stopAgentTx);
                    chain.getLogger().error("Trading time error,txTime:{},time:{}", txTime, time);
                    errorCode = ConsensusErrorCode.ERROR_UNLOCK_TIME.getCode();
                    continue;
                }
                CoinData coinData = new CoinData();
                coinData.parse(stopAgentTx.getCoinData(), 0);
                long unlockedTime = stopAgentTx.getTime() + chain.getConfig().getStopAgentLockTime();
                if (coinData.getTo().get(0).getLockTime() != unlockedTime) {
                    invalidTxList.add(stopAgentTx);
                    chain.getLogger().error("Error unlocking time");
                    errorCode = ConsensusErrorCode.ERROR_UNLOCK_TIME.getCode();
                    continue;
                }
                if (!hashSet.add(stopAgent.getCreateTxHash())) {
                    invalidTxList.add(stopAgentTx);
                    chain.getLogger().info("Repeated transactions");
                    errorCode = ConsensusErrorCode.CONFLICT_ERROR.getCode();
                    continue;
                }
                Agent agent = new Agent();

                Transaction createAgentTx = CallMethodUtils.getTransaction(chain, stopAgent.getCreateTxHash().toHex());
                if (createAgentTx == null) {
                    invalidTxList.add(stopAgentTx);
                    chain.getLogger().info("The creation node transaction corresponding to intelligent contract cancellation node transaction does not exist");
                    errorCode = ConsensusErrorCode.CONFLICT_ERROR.getCode();
                    continue;
                }
                agent.parse(createAgentTx.getTxData(), 0);
                stopAgent.setAddress(agent.getAgentAddress());

                if (!redPunishAddressSet.isEmpty()) {
                    if (redPunishAddressSet.contains(HexUtil.encode(stopAgent.getAddress())) || redPunishAddressSet.contains(HexUtil.encode(agent.getPackingAddress()))) {
                        invalidTxList.add(stopAgentTx);
                        chain.getLogger().info("Intelligent contract cancellation node transaction cancellation node does not exist");
                        errorCode = ConsensusErrorCode.CONFLICT_ERROR.getCode();
                    }
                }
            } catch (NulsException e) {
                invalidTxList.add(stopAgentTx);
                chain.getLogger().error("Intelligent Contract Creation Node Transaction Verification Failed");
                chain.getLogger().error(e);
                errorCode = e.getErrorCode().getCode();
            } catch (IOException io) {
                invalidTxList.add(stopAgentTx);
                chain.getLogger().error("Intelligent Contract Creation Node Transaction Verification Failed");
                chain.getLogger().error(io);
                errorCode = ConsensusErrorCode.SERIALIZE_ERROR.getCode();
            }
        }
        result.put("txList", invalidTxList);
        result.put("errorCode", errorCode);
        return result;
    }

    @Override
    public boolean commit(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> commitSuccessList = new ArrayList<>();
        boolean commitResult = true;
        for (Transaction tx : txs) {
            try {
                if (agentManager.stopAgentCommit(tx, blockHeader, chain)) {
                    commitSuccessList.add(tx);
                }
            } catch (NulsException e) {
                chain.getLogger().error("Failure to create node transaction submission");
                chain.getLogger().error(e);
                commitResult = false;
            }
        }
        //回滚已提交成功的交易
        if (!commitResult) {
            for (Transaction rollbackTx : commitSuccessList) {
                try {
                    agentManager.stopAgentRollBack(rollbackTx, chain, blockHeader);
                } catch (NulsException e) {
                    chain.getLogger().error("Failure to create node transaction rollback");
                    chain.getLogger().error(e);
                }
            }
        }
        return commitResult;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> rollbackSuccessList = new ArrayList<>();
        boolean rollbackResult = true;
        for (Transaction tx : txs) {
            try {
                if (agentManager.stopAgentRollBack(tx, chain, blockHeader)) {
                    rollbackSuccessList.add(tx);
                }
            } catch (NulsException e) {
                chain.getLogger().error("Failure to stop agent transaction rollback");
                chain.getLogger().error(e);
                rollbackResult = false;
            }
        }
        //保存已回滚成功的交易
        if (!rollbackResult) {
            for (Transaction commitTx : rollbackSuccessList) {
                try {
                    agentManager.stopAgentCommit(commitTx, blockHeader, chain);
                } catch (NulsException e) {
                    chain.getLogger().error("Failure to stop agent transaction submission");
                    chain.getLogger().error(e);
                }
            }
        }
        return rollbackResult;
    }

    /**
     * 版本三新增的验证
     */
    private boolean verifyV3(Chain chain, Transaction tx, byte[] creator) throws NulsException {
        //验证签名是否为节点创建者签名
        TransactionSignature transactionSignature = new TransactionSignature();
        if (tx.getTransactionSignature() == null) {
            chain.getLogger().error("Unsigned Commission transaction");
            throw new NulsException(ConsensusErrorCode.AGENT_CREATOR_NOT_SIGNED);
        }
        transactionSignature.parse(tx.getTransactionSignature(), 0);
        if (transactionSignature.getP2PHKSignatures() == null || transactionSignature.getP2PHKSignatures().isEmpty()) {
            throw new NulsException(ConsensusErrorCode.AGENT_CREATOR_NOT_SIGNED);
        }
        if (!Arrays.equals(creator, AddressTool.getAddress(transactionSignature.getP2PHKSignatures().get(0).getPublicKey(), chain.getConfig().getChainId()))) {
            chain.getLogger().error("The signature of the entrusted transaction is not the signature of the entrusting party");
            throw new NulsException(ConsensusErrorCode.TX_CREATOR_NOT_SIGNED);
        }
        return true;
    }
}
