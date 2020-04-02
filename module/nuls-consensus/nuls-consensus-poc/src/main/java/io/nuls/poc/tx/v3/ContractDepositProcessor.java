package io.nuls.poc.tx.v3;

import io.nuls.base.data.*;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.parse.JSONUtils;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.model.dto.transaction.TransactionDto;
import io.nuls.poc.model.po.DepositPo;
import io.nuls.poc.utils.LoggerUtil;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.poc.utils.manager.DepositManager;
import io.nuls.poc.utils.validator.TxValidator;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * 智能合约委托交易处理器
 *
 * @author tag
 * @date 2019/12/2
 */

@Component("ContractDepositProcessorV3")
public class ContractDepositProcessor implements TransactionProcessor {
    @Autowired
    private DepositManager depositManager;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxValidator txValidator;

    @Override
    public int getType() {
        return TxType.CONTRACT_DEPOSIT;
    }

    @Override
    public int getPriority() {
        return 4;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Map<String, Object> result = new HashMap<>(2);
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            LoggerUtil.commonLog.error("Chains do not exist.");
            result.put("txList", txs);
            result.put("errorCode", ConsensusErrorCode.CHAIN_NOT_EXIST.getCode());
            return result;
        }
        //chain.getLogger().info("进入版本三验证器" );
        List<Transaction> invalidTxList = new ArrayList<>();
        String errorCode = null;
        Set<NulsHash> invalidHashSet = txValidator.getInvalidAgentHash(txMap.get(TxType.RED_PUNISH), txMap.get(TxType.CONTRACT_STOP_AGENT), txMap.get(TxType.STOP_AGENT), chain);
        //个节点总委托金额
        Map<NulsHash, BigInteger> agentDepositTotalMap = new HashMap<>(16);
        for (Transaction contractDepositTx : txs) {
            try {
                Deposit deposit = new Deposit();
                deposit.parse(contractDepositTx.getTxData(), 0);
                //coinData地址
                if (!verifyV3(chain, contractDepositTx.getCoinDataInstance(), deposit.getAddress())) {
                    invalidTxList.add(contractDepositTx);
                    continue;
                }
                if (!txValidator.validateTx(chain, contractDepositTx)) {
                    invalidTxList.add(contractDepositTx);
                    chain.getLogger().info("Intelligent Contract Delegation Transaction Failed to Verify");
                    continue;
                }
                if (invalidHashSet.contains(deposit.getAgentHash())) {
                    invalidTxList.add(contractDepositTx);
                    chain.getLogger().info("Conflict between Intelligent Contract Delegation Transaction and Red Card Transaction or Stop Node Transaction");
                    errorCode = ConsensusErrorCode.CONFLICT_ERROR.getCode();
                }
                NulsHash agentHash = deposit.getAgentHash();
                BigInteger totalDeposit = BigInteger.ZERO;
                //验证委托金额是否超出节点最大委托量（冲突检测）
                if (agentDepositTotalMap.containsKey(agentHash)) {
                    totalDeposit = agentDepositTotalMap.get(agentHash).add(deposit.getDeposit());
                    if (totalDeposit.compareTo(chain.getConfig().getCommissionMax()) > 0) {
                        invalidTxList.add(contractDepositTx);
                        chain.getLogger().info("Node delegation amount exceeds maximum delegation amount");
                        throw new NulsException(ConsensusErrorCode.DEPOSIT_OVER_AMOUNT);
                    } else {
                        agentDepositTotalMap.put(agentHash, totalDeposit);
                    }
                } else {
                    List<DepositPo> poList = txValidator.getDepositListByAgent(chain, deposit.getAgentHash());
                    for (DepositPo cd : poList) {
                        totalDeposit = totalDeposit.add(cd.getDeposit());
                    }
                    totalDeposit = totalDeposit.add(deposit.getDeposit());
                    agentDepositTotalMap.put(agentHash, totalDeposit);
                }
            } catch (NulsException e) {
                invalidTxList.add(contractDepositTx);
                chain.getLogger().error("Intelligent Contract Creation Node Transaction Verification Failed");
                chain.getLogger().error(e);
                errorCode = e.getErrorCode().getCode();
            } catch (IOException io) {
                invalidTxList.add(contractDepositTx);
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
                if (depositManager.depositCommit(tx, blockHeader, chain)) {
                    commitSuccessList.add(tx);
                }
            } catch (NulsException e) {
                chain.getLogger().error("Failure to deposit transaction submission");
                chain.getLogger().error(e);
                commitResult = false;
            }
        }
        //回滚已提交成功的交易
        if (!commitResult) {
            for (Transaction rollbackTx : commitSuccessList) {
                try {
                    depositManager.depositRollBack(rollbackTx, chain);
                } catch (NulsException e) {
                    chain.getLogger().error("Failure to deposit transaction rollback");
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
                try {
                    chain.getLogger().info("contract deposit transaction rollback, hash is {}, tx is {}", tx.getHash().toHex(), JSONUtils.obj2json(new TransactionDto(tx)));
                } catch (Exception e) {
                    chain.getLogger().warn(e.getMessage());
                }
                if (depositManager.depositRollBack(tx, chain)) {
                    rollbackSuccessList.add(tx);
                }
            } catch (NulsException e) {
                chain.getLogger().error("Failure to deposit transaction rollback");
                chain.getLogger().error(e);
                rollbackResult = false;
            }
        }
        //保存已回滚成功的交易
        if (!rollbackResult) {
            for (Transaction commitTx : rollbackSuccessList) {
                try {
                    depositManager.depositCommit(commitTx, blockHeader, chain);
                } catch (NulsException e) {
                    chain.getLogger().error("Failure to deposit transaction submission");
                    chain.getLogger().error(e);
                }
            }
        }
        return rollbackResult;
    }

    /**
     * 版本三新增的验证
     * */
    private boolean verifyV3(Chain chain, CoinData coinData, byte[] creator)throws NulsException{
        //验证from中地址与to中地址是否相同且为委托者
        if (!Arrays.equals(creator, coinData.getFrom().get(0).getAddress()) || !Arrays.equals(creator, coinData.getTo().get(0).getAddress())) {
            chain.getLogger().error("From address or to address in coinData is not the principal address");
            throw new NulsException(ConsensusErrorCode.COIN_DATA_VALID_ERROR);
        }
        return true;
    }
}
