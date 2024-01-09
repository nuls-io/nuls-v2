package io.nuls.consensus.tx.v4;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.BlockHeader;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.Transaction;
import io.nuls.base.protocol.TransactionProcessor;
import io.nuls.base.signture.MultiSignTxSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.constant.ConsensusErrorCode;
import io.nuls.consensus.model.bo.Chain;
import io.nuls.consensus.model.bo.tx.txdata.Agent;
import io.nuls.consensus.model.bo.tx.txdata.RedPunishData;
import io.nuls.consensus.rpc.call.CallMethodUtils;
import io.nuls.consensus.utils.LoggerUtil;
import io.nuls.consensus.utils.manager.AgentManager;
import io.nuls.consensus.utils.manager.ChainManager;
import io.nuls.consensus.utils.validator.TxValidator;

import java.io.IOException;
import java.util.*;

/**
 * 创建节点处理器
 * @author tag
 * @date 2019/6/1
 */
@Component("CreateAgentProcessorV4")
public class CreateAgentProcessor implements TransactionProcessor {
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxValidator txValidator;

    @Override
    public int getType() {
        return TxType.REGISTER_AGENT;
    }

    @Override
    public int getPriority() {
        return 8;
    }

    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, Map<Integer, List<Transaction>> txMap, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        Map<String, Object> result = new HashMap<>(2);
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist.");
            result.put("txList", txs);
            result.put("errorCode", ConsensusErrorCode.CHAIN_NOT_EXIST.getCode());
            return result;
        }
        List<Transaction> invalidTxList = new ArrayList<>();
        String errorCode = null;
        Set<String> redPunishAddressSet = new HashSet<>();
        Set<String> createAgentAddressSet = new HashSet<>();
        List<Transaction> redPunishTxList = txMap.get(TxType.RED_PUNISH);
        if(redPunishTxList != null && redPunishTxList.size() >0){
            for (Transaction redPunishTx:redPunishTxList) {
                RedPunishData redPunishData = new RedPunishData();
                try {
                    redPunishData.parse(redPunishTx.getTxData(), 0);
                    String addressHex = HexUtil.encode(redPunishData.getAddress());
                    redPunishAddressSet.add(addressHex);
                }catch (NulsException e){
                    chain.getLogger().error(e);
                }
            }
        }
        List<Transaction> contractCreateAgentTxList = txMap.get(TxType.CONTRACT_CREATE_AGENT);
        if(contractCreateAgentTxList != null && contractCreateAgentTxList.size() >0){
            for (Transaction contractCreateAgentTx:contractCreateAgentTxList) {
                try {
                    Agent agent = new Agent();
                    agent.parse(contractCreateAgentTx.getTxData(), 0);
                    createAgentAddressSet.add(HexUtil.encode(agent.getAgentAddress()));
                    createAgentAddressSet.add(HexUtil.encode(agent.getPackingAddress()));
                }catch (Exception e){
                    chain.getLogger().error(e);
                }
            }
        }
        for (Transaction createAgentTx:txs) {
            try {
                if (!txValidator.validateTx(chain, createAgentTx)) {
                    invalidTxList.add(createAgentTx);
                    chain.getLogger().info("Failure to create node transaction validation");
                    continue;
                }
                Agent agent = new Agent();
                agent.parse(createAgentTx.getTxData(), 0);
                String agentAddressHex = HexUtil.encode(agent.getAgentAddress());
                String packAddressHex = HexUtil.encode(agent.getPackingAddress());
                //验证签名及coinData地址
                if (!verifyV4(chain, createAgentTx, agent.getAgentAddress())) {
                    invalidTxList.add(createAgentTx);
                    continue;
                }
                /*
                 * 获得过红牌交易的地址不能创建节点
                 * */
                if (!redPunishAddressSet.isEmpty()) {
                    if (redPunishAddressSet.contains(agentAddressHex) || redPunishAddressSet.contains(packAddressHex)) {
                        invalidTxList.add(createAgentTx);
                        chain.getLogger().info("Creating Node Trading and Red Card Trading Conflict");
                        errorCode = ConsensusErrorCode.CONFLICT_ERROR.getCode();
                        continue;
                    }
                }
                /*
                 * 重复创建节点
                 * */
                if (!createAgentAddressSet.add(agentAddressHex) || !createAgentAddressSet.add(packAddressHex)) {
                    invalidTxList.add(createAgentTx);
                    chain.getLogger().info("Repeated transactions");
                    errorCode = ConsensusErrorCode.CONFLICT_ERROR.getCode();
                }
            }catch (NulsException e){
                invalidTxList.add(createAgentTx);
                chain.getLogger().error("Intelligent Contract Creation Node Transaction Verification Failed");
                chain.getLogger().error(e);
                errorCode = e.getErrorCode().getCode();
            }catch (IOException io){
                invalidTxList.add(createAgentTx);
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
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> commitSuccessList = new ArrayList<>();
        boolean commitResult = true;
        for (Transaction tx:txs) {
            try {
                if(agentManager.createAgentCommit(tx,blockHeader,chain)){
                    commitSuccessList.add(tx);
                }
            }catch (NulsException e){
                chain.getLogger().error("Failure to create agent transaction submission");
                chain.getLogger().error(e);
                commitResult = false;
            }
        }
        //回滚已提交成功的交易
        if(!commitResult){
            for (Transaction rollbackTx:commitSuccessList) {
                try {
                    agentManager.createAgentRollBack(rollbackTx, chain);
                }catch (NulsException e){
                    chain.getLogger().error("Failure to create agent transaction rollback");
                    chain.getLogger().error(e);
                }
            }
        }
        return commitResult;
    }

    @Override
    public boolean rollback(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null){
            LoggerUtil.commonLog.error("Chains do not exist.");
            return false;
        }
        List<Transaction> rollbackSuccessList = new ArrayList<>();
        boolean rollbackResult = true;
        for (Transaction tx:txs) {
            try {
                if(agentManager.createAgentRollBack(tx,chain)){
                    rollbackSuccessList.add(tx);
                }
            }catch (NulsException e){
                chain.getLogger().error("Failure to create node transaction rollback");
                chain.getLogger().error(e);
                rollbackResult = false;
            }
        }
        //保存已回滚成功的交易
        if(!rollbackResult){
            for (Transaction commitTx:rollbackSuccessList) {
                try {
                    agentManager.createAgentCommit(commitTx, blockHeader, chain);
                }catch (NulsException e){
                    chain.getLogger().error("Failure to create node transaction submission");
                    chain.getLogger().error(e);
                }
            }
        }
        return rollbackResult;
    }

    /**
     * 版本三新增的验证
     *
     * @param chain   链信息
     * @param tx      委托交易
     * @param creator 委托账户地址
     */
    private boolean verifyV4(Chain chain, Transaction tx, byte[] creator) throws NulsException {
        if (tx.getTransactionSignature() == null) {
            chain.getLogger().error("Unsigned Commission transaction");
            throw new NulsException(ConsensusErrorCode.AGENT_CREATOR_NOT_SIGNED);
        }
        byte[] signer;
        if(tx.isMultiSignTx()){
            MultiSignTxSignature signTxSignature = new MultiSignTxSignature();
            signTxSignature.parse(tx.getTransactionSignature(), 0);
            if (signTxSignature.getP2PHKSignatures() == null || signTxSignature.getP2PHKSignatures().isEmpty()
            || signTxSignature.getPubKeyList() == null || signTxSignature.size() < signTxSignature.getM()) {
                throw new NulsException(ConsensusErrorCode.AGENT_CREATOR_NOT_SIGNED);
            }
            signer = AddressTool.getAddress(CallMethodUtils.createMultiSignAccount(chain.getConfig().getChainId(), signTxSignature));
        }else{
            TransactionSignature transactionSignature = new TransactionSignature();
            transactionSignature.parse(tx.getTransactionSignature(), 0);
            if (transactionSignature.getP2PHKSignatures() == null || transactionSignature.getP2PHKSignatures().isEmpty()) {
                throw new NulsException(ConsensusErrorCode.AGENT_CREATOR_NOT_SIGNED);
            }
            signer = AddressTool.getAddress(transactionSignature.getP2PHKSignatures().get(0).getPublicKey(), chain.getConfig().getChainId());
        }
        //验证签名是否为节点创建者签名
        if (!Arrays.equals(creator, signer)) {
            chain.getLogger().error("The signature of the entrusted transaction is not the signature of the entrusting party");
            throw new NulsException(ConsensusErrorCode.TX_CREATOR_NOT_SIGNED);
        }
        //验证from中地址与to中地址是否相同且为委托者
        CoinData coinData = tx.getCoinDataInstance();
        if (!Arrays.equals(creator, coinData.getFrom().get(0).getAddress()) || !Arrays.equals(creator, coinData.getTo().get(0).getAddress())) {
            chain.getLogger().error("From address or to address in coinData is not the principal address");
            throw new NulsException(ConsensusErrorCode.COIN_DATA_VALID_ERROR);
        }
        //锁定资产必须为本链主资产
        for (CoinTo coinTo : coinData.getTo()){
            if(coinTo.getLockTime() == ConsensusConstant.CONSENSUS_LOCK_TIME &&
                    (coinTo.getAssetsChainId() != chain.getConfig().getAgentChainId() || coinTo.getAssetsId() != chain.getConfig().getAgentAssetId())){
                chain.getLogger().info("Entrusted assets are not consensus assets");
                throw new NulsException(ConsensusErrorCode.COIN_DATA_VALID_ERROR);
            }
        }
        return true;
    }
}
