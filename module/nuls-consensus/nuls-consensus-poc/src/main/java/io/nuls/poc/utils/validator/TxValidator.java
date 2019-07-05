package io.nuls.poc.utils.validator;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.tx.txdata.*;
import io.nuls.poc.model.po.AgentPo;
import io.nuls.poc.model.po.DepositPo;
import io.nuls.poc.model.po.PunishLogPo;
import io.nuls.poc.storage.AgentStorageService;
import io.nuls.poc.storage.DepositStorageService;
import io.nuls.poc.utils.compare.CoinFromComparator;
import io.nuls.poc.utils.compare.CoinToComparator;
import io.nuls.poc.utils.manager.AgentManager;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.poc.utils.manager.CoinDataManager;
import io.nuls.poc.utils.manager.ConsensusManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * 交易验证工具类
 * Transaction Verification Tool Class
 *
 * @author tag
 * 2018/11/30
 */
@Component
public class TxValidator {
    @Autowired
    private AgentStorageService agentStorageService;
    @Autowired
    private DepositStorageService depositStorageService;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private CoinDataManager coinDataManager;
    @Autowired
    private ConsensusManager consensusManager;

    /**
     * 验证交易
     * Verifying transactions
     *
     * @param chain 链ID/chain id
     * @param tx    交易/transaction info
     * @return boolean
     */
    public boolean validateTx(Chain chain, Transaction tx) throws NulsException,IOException{
        switch (tx.getType()) {
            case (TxType.REGISTER_AGENT):
            case (TxType.CONTRACT_CREATE_AGENT):
                return validateCreateAgent(chain, tx);
            case (TxType.STOP_AGENT):
            case (TxType.CONTRACT_STOP_AGENT):
                return validateStopAgent(chain, tx);
            case (TxType.DEPOSIT):
            case (TxType.CONTRACT_DEPOSIT):
                return validateDeposit(chain, tx);
            case (TxType.CANCEL_DEPOSIT):
            case (TxType.CONTRACT_CANCEL_DEPOSIT):
                return validateWithdraw(chain, tx);
            default:
                return false;
        }
    }

    /**
     * 创建节点交易验证
     * Create node transaction validation
     *
     * @param chain 链ID/chain id
     * @param tx    创建节点交易/create agent transaction
     * @return boolean
     */
    private boolean validateCreateAgent(Chain chain, Transaction tx) throws NulsException {
        if (tx.getTxData() == null) {
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        Agent agent = new Agent();
        agent.parse(tx.getTxData(), 0);
        if (!createAgentBasicValid(chain, tx, agent)) {
            return false;
        }
        if (!createAgentAddrValid(chain, tx, agent)) {
            return false;
        }
        return true;
    }

    /**
     * 停止节点交易验证
     * Stop Node Transaction Verification
     *
     * @param chain 链ID/chain id
     * @param tx    停止节点交易/stop agent transaction
     * @return boolean
     */
    private boolean validateStopAgent(Chain chain, Transaction tx) throws NulsException,IOException{
        if (tx.getTxData() == null) {
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        StopAgent stopAgent = new StopAgent();
        stopAgent.parse(tx.getTxData(), 0);
        AgentPo agentPo = agentStorageService.get(stopAgent.getCreateTxHash(), chain.getConfig().getChainId());
        if (agentPo == null || agentPo.getDelHeight() > 0) {
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        if (tx.getType() == TxType.STOP_AGENT) {
            if (!validSignature(tx, agentPo.getAgentAddress(), chain.getConfig().getChainId())) {
                throw new NulsException(ConsensusErrorCode.SIGNATURE_ERROR);
            }
        }
        CoinData coinData = new CoinData();
        coinData.parse(tx.getCoinData(), 0);
        if (coinData.getTo() == null || coinData.getTo().isEmpty()) {
            throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        if (!stopAgentCoinDataValid(chain, tx, agentPo, stopAgent, coinData)) {
            throw new NulsException(ConsensusErrorCode.COIN_DATA_VALID_ERROR);
        }
        return true;
    }

    /**
     * 委托共识交易验证
     * Deposit Transaction Verification
     *
     * @param chain 链ID/Chain Id
     * @param tx    委托共识交易/Deposit Transaction
     * @return boolean
     */
    private boolean validateDeposit(Chain chain, Transaction tx) throws NulsException {
        if (null == tx || null == tx.getTxData() || tx.getCoinData() == null) {
            throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        Deposit deposit = new Deposit();
        deposit.parse(tx.getTxData(), 0);
        if (deposit.getAddress() == null || deposit.getAgentHash() == null || deposit.getDeposit() == null) {
            throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        if (!createDepositInfoValid(chain, deposit)) {
            return false;
        }
        CoinData coinData = new CoinData();
        coinData.parse(tx.getCoinData(), 0);
        if (!isDepositOk(deposit.getDeposit(), coinData)) {
            throw new NulsException(ConsensusErrorCode.DEPOSIT_ERROR);
        }
        if (tx.getType() == TxType.DEPOSIT) {
            if (!validSignature(tx, deposit.getAddress(), chain.getConfig().getChainId())) {
                throw new NulsException(ConsensusErrorCode.SIGNATURE_ERROR);
            }
            //验证手续费是否足够
            try {
                BigInteger fee = TransactionFeeCalculator.getConsensusTxFee(tx.serialize().length, chain.getConfig().getFeeUnit());
                if(fee.compareTo(consensusManager.getFee(coinData, chain.getConfig().getAgentChainId(), chain.getConfig().getAgentAssetId())) > 0){
                    chain.getLogger().error("手续费不足！");
                    throw new NulsException(ConsensusErrorCode.FEE_NOT_ENOUGH);
                }
            }catch (IOException e){
                chain.getLogger().error("数据序列化错误！");
                throw new NulsException(ConsensusErrorCode.SERIALIZE_ERROR);
            }
        }
        Set<String> addressSet = new HashSet<>();
        int lockCount = 0;
        for (CoinTo coin : coinData.getTo()) {
            if (coin.getLockTime() == ConsensusConstant.CONSENSUS_LOCK_TIME) {
                lockCount++;
            }
            addressSet.add(AddressTool.getStringAddressByBytes(coin.getAddress()));

        }
        if (lockCount > 1 || addressSet.size() > 1) {
            throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        return true;
    }

    /**
     * 退出节点交易验证
     * Withdraw Transaction Verification
     *
     * @param chain 链ID/chain id
     * @param tx    退出节点交易/Withdraw Transaction
     * @return boolean
     */
    private boolean validateWithdraw(Chain chain, Transaction tx) throws NulsException {
        CancelDeposit cancelDeposit = new CancelDeposit();
        cancelDeposit.parse(tx.getTxData(), 0);
        DepositPo depositPo = depositStorageService.get(cancelDeposit.getJoinTxHash(), chain.getConfig().getChainId());
        if (depositPo == null || depositPo.getDelHeight() > 0) {
            throw new NulsException(ConsensusErrorCode.DATA_NOT_EXIST);
        }
        //查看对出委托账户是否正确(智能合约创建的交易没有签名)
        if (tx.getType() == TxType.CANCEL_DEPOSIT) {
            if (!validSignature(tx, depositPo.getAddress(), chain.getConfig().getChainId())) {
                throw new NulsException(ConsensusErrorCode.SIGNATURE_ERROR);
            }
        }
        //查看from和to中地址是否一样
        CoinData coinData = new CoinData();
        coinData.parse(tx.getCoinData(), 0);
        if (coinData.getFrom().size() == 0 || coinData.getTo().size() == 0) {
            throw new NulsException(ConsensusErrorCode.DATA_ERROR);
        }
        //退出委托账户及资产ID是否正确
        if (!Arrays.equals(coinData.getFrom().get(0).getAddress(), coinData.getTo().get(0).getAddress())
                || coinData.getFrom().get(0).getAssetsId() != coinData.getTo().get(0).getAssetsId()) {
            throw new NulsException(ConsensusErrorCode.DATA_ERROR);
        }
        //退出委托金额是否正确
        if(depositPo.getDeposit().compareTo(coinData.getFrom().get(0).getAmount()) != 0 || coinData.getTo().get(0).getAmount().compareTo(BigInteger.ZERO) <= 0){
            throw new NulsException(ConsensusErrorCode.DATA_ERROR);
        }
        if(tx.getType() == TxType.CONTRACT_CANCEL_DEPOSIT && coinData.getTo().get(0).getAmount().compareTo(depositPo.getDeposit()) > 0){
            throw new NulsException(ConsensusErrorCode.DATA_ERROR);
        }else if(tx.getType() == TxType.CANCEL_DEPOSIT && coinData.getTo().get(0).getAmount().compareTo(depositPo.getDeposit()) >= 0){
            throw new NulsException(ConsensusErrorCode.DATA_ERROR);
        }
        return true;
    }

    /**
     * 创建节点交易基础验证
     * Create agent transaction base validation
     *
     * @param chain 链ID/chain id
     * @param tx    创建节点交易/create transaction
     * @param agent 节点/agent
     * @return boolean
     */
    private boolean createAgentBasicValid(Chain chain, Transaction tx, Agent agent) throws NulsException {
        if (!AddressTool.validNormalAddress(agent.getPackingAddress(), (short) chain.getConfig().getChainId())) {
            throw new NulsException(ConsensusErrorCode.ADDRESS_ERROR);
        }
        if (Arrays.equals(agent.getAgentAddress(), agent.getPackingAddress())) {
            throw new NulsException(ConsensusErrorCode.AGENTADDR_AND_PACKING_SAME);
        }
        if (Arrays.equals(agent.getRewardAddress(), agent.getPackingAddress())) {
            throw new NulsException(ConsensusErrorCode.REWARDADDR_AND_PACKING_SAME);
        }
        if (tx.getTime() <= 0) {
            throw new NulsException(ConsensusErrorCode.DATA_ERROR);
        }
        byte commissionRate = agent.getCommissionRate();
        if (commissionRate < chain.getConfig().getCommissionRateMin() || commissionRate > chain.getConfig().getCommissionRateMax()) {
            throw new NulsException(ConsensusErrorCode.COMMISSION_RATE_OUT_OF_RANGE);
        }
        BigInteger deposit = agent.getDeposit();
        if (deposit.compareTo(chain.getConfig().getDepositMin()) < 0 || deposit.compareTo(chain.getConfig().getDepositMax()) > 0) {
            throw new NulsException(ConsensusErrorCode.DEPOSIT_OUT_OF_RANGE);
        }
        CoinData coinData = new CoinData();
        coinData.parse(tx.getCoinData(), 0);
        if (!isDepositOk(agent.getDeposit(), coinData)) {
            throw new NulsException(ConsensusErrorCode.DEPOSIT_ERROR);
        }
        //智能合约创建的交易没有签名
        if (tx.getType() == TxType.REGISTER_AGENT) {
            if (!validSignature(tx, agent.getAgentAddress(), chain.getConfig().getChainId())) {
                throw new NulsException(ConsensusErrorCode.SIGNATURE_ERROR);
            }
            //验证手续费是否足够
            try {
                BigInteger fee = TransactionFeeCalculator.getConsensusTxFee(tx.serialize().length, chain.getConfig().getFeeUnit());
                if(fee.compareTo(consensusManager.getFee(coinData, chain.getConfig().getAgentChainId(), chain.getConfig().getAgentAssetId())) > 0){
                    chain.getLogger().error("手续费不足！");
                    throw new NulsException(ConsensusErrorCode.FEE_NOT_ENOUGH);
                }
            }catch (IOException e){
                chain.getLogger().error("数据序列化错误！");
                throw new NulsException(ConsensusErrorCode.SERIALIZE_ERROR);
            }
        }
        Set<String> addressSet = new HashSet<>();
        int lockCount = 0;
        for (CoinTo coin : coinData.getTo()) {
            if(coin.getAssetsChainId() != chain.getConfig().getAgentChainId() || coin.getAssetsId() != chain.getConfig().getAgentAssetId()){
                chain.getLogger().error("锁定资产不合法");
                throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            if (coin.getLockTime() == ConsensusConstant.CONSENSUS_LOCK_TIME) {
                lockCount++;
            }
            addressSet.add(AddressTool.getStringAddressByBytes(coin.getAddress()));
        }
        if (lockCount > 1 || addressSet.size() > 1) {
            throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        return true;
    }

    /**
     * 创建节点交易节点地址及出块地址验证
     * address validate
     *
     * @param chain 链ID/chain id
     * @param tx    创建节点交易/create transaction
     * @param agent 节点/agent
     * @return boolean
     */
    private boolean createAgentAddrValid(Chain chain, Transaction tx, Agent agent) throws NulsException {
        String seedNodesStr = chain.getConfig().getSeedNodes();
        if (StringUtils.isBlank(seedNodesStr)) {
            return true;
        }
        byte[] nodeAddressBytes;
        //节点地址及出块地址不能是种子节点
        String splitSign = ",";
        for (String nodeAddress : seedNodesStr.split(splitSign)) {
            nodeAddressBytes = AddressTool.getAddress(nodeAddress);
            if (Arrays.equals(nodeAddressBytes, agent.getAgentAddress())) {
                throw new NulsException(ConsensusErrorCode.AGENT_EXIST);
            }
            if (Arrays.equals(nodeAddressBytes, agent.getPackingAddress())) {
                throw new NulsException(ConsensusErrorCode.AGENT_PACKING_EXIST);
            }
        }
        //节点地址及出块地址不能重复
        List<Agent> agentList = chain.getNewOrWorkAgentList(chain.getNewestHeader().getHeight());
        if (agentList != null && agentList.size() > 0) {
            Set<String> set = new HashSet<>();
            for (Agent agentTemp : agentList) {
                if (agentTemp.getTxHash().equals(tx.getHash())) {
                    throw new NulsException(ConsensusErrorCode.TRANSACTION_REPEATED);
                }
                set.add(HexUtil.encode(agentTemp.getAgentAddress()));
                set.add(HexUtil.encode(agentTemp.getPackingAddress()));
            }
            boolean b = set.contains(HexUtil.encode(agent.getAgentAddress()));
            if (b) {
                throw new NulsException(ConsensusErrorCode.AGENT_EXIST);
            }
            b = set.contains(HexUtil.encode(agent.getPackingAddress()));
            if (b) {
                throw new NulsException(ConsensusErrorCode.AGENT_PACKING_EXIST);
            }
        }
        long count = this.getRedPunishCount(chain, agent.getAgentAddress());
        if (count > 0) {
            throw new NulsException(ConsensusErrorCode.LACK_OF_CREDIT);
        }
        return true;
    }


    /**
     * 停止节点交易CoinData验证
     * Stop agent transaction CoinData validate
     *
     * @param chain    链ID/chain id
     * @param tx       退出节点交易/stop agent transaction
     * @param agentPo  退出的节点信息/agent
     * @param coinData 交易的CoinData/coinData
     * @return boolean
     */
    private boolean stopAgentCoinDataValid(Chain chain, Transaction tx, AgentPo agentPo, StopAgent stopAgent, CoinData coinData) throws NulsException,IOException{
        Agent agent = agentManager.poToAgent(agentPo);
        CoinData localCoinData = coinDataManager.getStopAgentCoinData(chain, agent, coinData.getTo().get(0).getLockTime());
        //coinData和localCoinData排序
        CoinFromComparator fromComparator = new CoinFromComparator();
        CoinToComparator toComparator = new CoinToComparator();
        coinData.getFrom().sort(fromComparator);
        coinData.getTo().sort(toComparator);
        localCoinData.getFrom().sort(fromComparator);
        localCoinData.getTo().sort(toComparator);
        CoinTo last = localCoinData.getTo().get(localCoinData.getTo().size() - 1);
        if(tx.getType() == TxType.STOP_AGENT){
            int size = tx.size();
            if (TxType.STOP_AGENT == tx.getType()) {
                size -= tx.getTransactionSignature().length + P2PHKSignature.SERIALIZE_LENGTH;
            }
            BigInteger fee = TransactionFeeCalculator.getNormalTxFee(size);
            last.setAmount(last.getAmount().subtract(fee));
        }
        return Arrays.equals(coinData.serialize(), localCoinData.serialize());
    }

    /**
     * 委托交易基础信息验证
     * deposit transaction base validate
     *
     * @param chain   链ID/chain id
     * @param deposit 委托信息/deposit
     * @return boolean
     */
    private boolean createDepositInfoValid(Chain chain, Deposit deposit) throws NulsException {
        AgentPo agentPo = agentStorageService.get(deposit.getAgentHash(), chain.getConfig().getChainId());
        if (agentPo == null || agentPo.getDelHeight() > 0) {
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        List<DepositPo> poList = this.getDepositListByAgent(chain, deposit.getAgentHash());
        //节点当前委托总金额
        BigInteger total = deposit.getDeposit();
        if (total.compareTo(chain.getConfig().getEntrusterDepositMin()) < 0) {
            throw new NulsException(ConsensusErrorCode.DEPOSIT_NOT_ENOUGH);
        }
        if (total.compareTo(chain.getConfig().getCommissionMax()) > 0) {
            throw new NulsException(ConsensusErrorCode.DEPOSIT_OVER_AMOUNT);
        }
        for (DepositPo cd : poList) {
            total = total.add(cd.getDeposit());
        }
        if (total.compareTo(chain.getConfig().getCommissionMax()) > 0) {
            throw new NulsException(ConsensusErrorCode.DEPOSIT_OVER_AMOUNT);
        }
        return true;
    }

    /**
     * 交易签名验证
     * Transaction signature verification
     *
     * @param tx      交易/transaction
     * @param address 签名账户/signature
     * @return boolean
     */
    private boolean validSignature(Transaction tx, byte[] address, int chainId) throws NulsException {
        TransactionSignature sig = new TransactionSignature();
        sig.parse(tx.getTransactionSignature(), 0);
        if (!SignatureUtil.containsAddress(tx, address, chainId)) {
            throw new NulsException(ConsensusErrorCode.SIGNATURE_ERROR);
        }
        return true;
    }

    /**
     * 委托信息验证
     * deposit validate
     *
     * @param deposit  委托信息/deposit
     * @param coinData 交易的CoinData/CoinData
     * @return boolean
     */
    private boolean isDepositOk(BigInteger deposit, CoinData coinData) throws NulsException{
        if (coinData == null || coinData.getTo().size() == 0) {
            throw new NulsException(ConsensusErrorCode.COIN_DATA_VALID_ERROR);
        }
        CoinTo coin = coinData.getTo().get(0);
        if (!BigIntegerUtils.isEqual(deposit, coin.getAmount())) {
            throw new NulsException(ConsensusErrorCode.COIN_DATA_VALID_ERROR);
        }
        if (coin.getLockTime() != ConsensusConstant.LOCK_OF_LOCK_TIME) {
            throw new NulsException(ConsensusErrorCode.COIN_DATA_VALID_ERROR);
        }
        return true;
    }

    /**
     * 节点是否获得过红牌
     * Does the node get a red card?
     *
     * @param chain   chain info
     * @param address 出块地址/packing address
     * @return long
     */
    private long getRedPunishCount(Chain chain, byte[] address) {
        List<PunishLogPo> list = chain.getRedPunishList();
        if (null == list || list.isEmpty()) {
            return 0;
        }
        long count = 0;
        for (PunishLogPo po : list) {
            if (Arrays.equals(address, po.getAddress())) {
                count++;
            }
        }
        return count;
    }

    /**
     * 获取节点的委托列表
     * Get the delegate list for the node
     *
     * @param chain     链ID/chain id
     * @param agentHash 节点HASH/agent hash
     * @return List<DepositPo>
     */
    private List<DepositPo> getDepositListByAgent(Chain chain, NulsHash agentHash) throws NulsException {
        List<DepositPo> depositList;
        try {
            depositList = depositStorageService.getList(chain.getConfig().getChainId());
        } catch (Exception e) {
            throw new NulsException(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
        long startBlockHeight = chain.getNewestHeader().getHeight();
        List<DepositPo> resultList = new ArrayList<>();
        for (DepositPo deposit : depositList) {
            if (deposit.getDelHeight() != -1L && deposit.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (deposit.getBlockHeight() > startBlockHeight || deposit.getBlockHeight() < 0L) {
                continue;
            }
            if (deposit.getAgentHash().equals(agentHash)) {
                resultList.add(deposit);
            }
        }
        return resultList;
    }

    /**
     * 获取区块交易列表中，红牌交易或停止节点交易对应的节点Hash列表
     * Get the node Hash list corresponding to the block transaction list, the red card transaction or the stop node transaction
     *
     * @param redPunishTxs        红牌交易/Red card penalty node address
     * @param stopAgentTxs        停止节点交易列表/Stop Node Trading List
     * @param chain               chain info
     */
    public Set<NulsHash> getInvalidAgentHash(List<Transaction> redPunishTxs, List<Transaction> contractStopAgentTxs, List<Transaction> stopAgentTxs, Chain chain){
        Set<String> redPunishAddressSet = new HashSet<>();
        if(redPunishTxs != null && redPunishTxs.size() >0){
            for (Transaction redPunishTx:redPunishTxs) {
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
        Set<NulsHash> agentHashSet = new HashSet<>();
        List<Agent> agentList = chain.getAgentList();
        long startBlockHeight = chain.getNewestHeader().getHeight();
        if (!redPunishAddressSet.isEmpty()) {
            for (Agent agent : agentList) {
                if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                    continue;
                }
                if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                    continue;
                }
                if (redPunishAddressSet.contains(HexUtil.encode(agent.getAgentAddress())) || redPunishAddressSet.contains(HexUtil.encode(agent.getPackingAddress()))) {
                    agentHashSet.add(agent.getTxHash());
                }
            }
        }
        try {
            if (stopAgentTxs != null) {
                StopAgent stopAgent = new StopAgent();
                for (Transaction tx : stopAgentTxs) {
                    stopAgent.parse(tx.getTxData(), 0);
                    agentHashSet.add(stopAgent.getCreateTxHash());
                }
            }
            if (contractStopAgentTxs != null) {
                StopAgent stopAgent = new StopAgent();
                for (Transaction tx : contractStopAgentTxs) {
                    stopAgent.parse(tx.getTxData(), 0);
                    agentHashSet.add(stopAgent.getCreateTxHash());
                }
            }
        }catch (Exception e){
            chain.getLogger().error(e);
        }
        return agentHashSet;
    }
}
