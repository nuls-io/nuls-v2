package io.nuls.consensus.utils.validator;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.MultiSignTxSignature;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ArraysTool;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.constant.ConsensusErrorCode;
import io.nuls.consensus.model.bo.Chain;
import io.nuls.consensus.model.bo.tx.txdata.*;
import io.nuls.consensus.model.po.AgentPo;
import io.nuls.consensus.model.po.DepositPo;
import io.nuls.consensus.model.po.PunishLogPo;
import io.nuls.consensus.rpc.call.CallMethodUtils;
import io.nuls.consensus.storage.AgentStorageService;
import io.nuls.consensus.storage.DepositStorageService;
import io.nuls.consensus.utils.compare.CoinFromComparator;
import io.nuls.consensus.utils.compare.CoinToComparator;
import io.nuls.consensus.utils.manager.AgentManager;
import io.nuls.consensus.utils.manager.ChainManager;
import io.nuls.consensus.utils.manager.CoinDataManager;
import io.nuls.consensus.utils.manager.ConsensusManager;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolContext;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * Transaction verification tools
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
     * Verify transactions
     * Verifying transactions
     *
     * @param chain chainID/chain id
     * @param tx    transaction/transaction info
     * @return boolean
     */
    public boolean validateTx(Chain chain, Transaction tx) throws NulsException, IOException {
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
            case (TxType.DELAY_STOP_AGENT):
                return validateDelayStopAgent(chain, tx);
            default:
                return false;
        }
    }

    private boolean validateDelayStopAgent(Chain chain, Transaction tx) throws NulsException, IOException {
        DelayStopAgent txData = new DelayStopAgent();
        txData.parse(tx.getTxData(), 0);
        AgentPo agentPo = this.agentStorageService.get(txData.getAgentHash(), chain.getConfig().getChainId());
        if (null == agentPo || agentPo.getDelHeight() <= 1) {
            chain.getLogger().warn("agent hash not right,{}", txData.getAgentHash().toHex());
            return false;
        }
        if (txData.getHeight() != agentPo.getDelHeight()) {
            chain.getLogger().warn("agent delHeight not right,{}", txData.getAgentHash().toHex());
            return false;
        }
        Agent agent = null;
        for (Agent a : chain.getAgentList()) {
            if (a.getTxHash().equals(agentPo.getHash())) {
                agent = a;
                break;
            }
        }

        if (null == agent || agent.getDelHeight() <= 1) {
            chain.getLogger().warn("Cache agent deleteHeight not right,{}", txData.getAgentHash().toHex());
            return false;
        }

        //validatecoindataConsensus relevance
        CoinData csCoinData = coinDataManager.getStopAgentCoinData(chain, agent, 1);
        if (!ArraysTool.arrayEquals(csCoinData.serialize(), tx.getCoinData())) {
            chain.getLogger().warn("Delay stop agent coindata not right,{}", txData.getAgentHash().toHex());
            return false;
        }
        //Verify signature
        TransactionSignature signature = new TransactionSignature();
        signature.parse(tx.getTransactionSignature(), 0);
        if (signature.getSignersCount() > 1) {
            chain.getLogger().warn("Delay stop agent signature count not right,{}", txData.getAgentHash().toHex());
            return false;
        }
        P2PHKSignature sig = signature.getP2PHKSignatures().get(0);
        if (!ECKey.verify(tx.getHash().getBytes(), sig.getSignData().getSignBytes(), sig.getPublicKey())) {
            chain.getLogger().warn("Delay stop agent signature not right,{}", txData.getAgentHash().toHex());
            return false;
        }
        byte[] address = AddressTool.getAddress(sig.getPublicKey(), chain.getConfig().getChainId());
        String addr = AddressTool.getStringAddressByBytes(address);
        List<String> seedList = new ArrayList<>(Arrays.asList(chain.getConfig().getSeedNodes().split(",")));
        if (!seedList.contains(addr)) {
            chain.getLogger().warn("Delay stop agent tx must sended by a seed address,{}", txData.getAgentHash().toHex());
            return false;
        }
        return true;
    }

    /**
     * Create node transaction verification
     * Create node transaction validation
     *
     * @param chain chainID/chain id
     * @param tx    Create node transactions/create agent transaction
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
     * Stop node transaction verification
     * Stop Node Transaction Verification
     *
     * @param chain chainID/chain id
     * @param tx    Stop node transactions/stop agent transaction
     * @return boolean
     */
    private boolean validateStopAgent(Chain chain, Transaction tx) throws NulsException, IOException {
        if (tx.getTxData() == null) {
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        StopAgent stopAgent = new StopAgent();
        stopAgent.parse(tx.getTxData(), 0);
        AgentPo agentPo = agentStorageService.get(stopAgent.getCreateTxHash(), chain.getConfig().getChainId());
        if (agentPo == null || agentPo.getDelHeight() > 0) {
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
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
     * Entrusted consensus transaction verification
     * Deposit Transaction Verification
     *
     * @param chain chainID/Chain Id
     * @param tx    Entrusted consensus trading/Deposit Transaction
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
            //Verify if the handling fee is sufficient
            try {
                int size = tx.serialize().length;
                if (AddressTool.isMultiSignAddress(coinData.getFrom().get(0).getAddress())) {
                    MultiSignTxSignature transactionSignature = new MultiSignTxSignature();
                    transactionSignature.parse(tx.getTransactionSignature(), 0);
                    size += transactionSignature.getM() * P2PHKSignature.SERIALIZE_LENGTH;
                    size -= tx.getTransactionSignature().length;
                }

                BigInteger fee = TransactionFeeCalculator.getConsensusTxFee(size, chain.getConfig().getFeeUnit(chain.getConfig().getAgentChainId(), chain.getConfig().getAgentAssetId()));
                if (fee.compareTo(consensusManager.getFee(coinData, chain.getConfig().getAgentChainId(), chain.getConfig().getAgentAssetId())) > 0) {
                    chain.getLogger().error("Insufficient handling fees！");
                    throw new NulsException(ConsensusErrorCode.FEE_NOT_ENOUGH);
                }
            } catch (IOException e) {
                chain.getLogger().error("Data serialization error！");
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
     * Exit node transaction verification
     * Withdraw Transaction Verification
     *
     * @param chain chainID/chain id
     * @param tx    Exit node transaction/Withdraw Transaction
     * @return boolean
     */
    private boolean validateWithdraw(Chain chain, Transaction tx) throws NulsException {
        CancelDeposit cancelDeposit = new CancelDeposit();
        cancelDeposit.parse(tx.getTxData(), 0);
        DepositPo depositPo = depositStorageService.get(cancelDeposit.getJoinTxHash(), chain.getConfig().getChainId());
        if (depositPo == null || depositPo.getDelHeight() > 0) {
            throw new NulsException(ConsensusErrorCode.DATA_NOT_EXIST);
        }
        //checkfromandtoIs the middle address the same
        CoinData coinData = new CoinData();
        coinData.parse(tx.getCoinData(), 0);
        if (coinData.getFrom().size() == 0 || coinData.getTo().size() == 0) {
            throw new NulsException(ConsensusErrorCode.DATA_ERROR);
        }
        //Exit entrusted account and assetsIDIs it correct
        if (!Arrays.equals(coinData.getFrom().get(0).getAddress(), coinData.getTo().get(0).getAddress())
                || coinData.getFrom().get(0).getAssetsId() != coinData.getTo().get(0).getAssetsId()) {
            throw new NulsException(ConsensusErrorCode.DATA_ERROR);
        }
        //validatenonceIs the value correct
        if (!ArraysTool.arrayEquals(CallMethodUtils.getNonce(cancelDeposit.getJoinTxHash().getBytes()), coinData.getFrom().get(0).getNonce())) {
            throw new NulsException(ConsensusErrorCode.COIN_DATA_VALID_ERROR);
        }
        //Is the withdrawal commission amount correct
        if (depositPo.getDeposit().compareTo(coinData.getFrom().get(0).getAmount()) != 0 || coinData.getTo().get(0).getAmount().compareTo(BigInteger.ZERO) <= 0) {
            throw new NulsException(ConsensusErrorCode.DATA_ERROR);
        }
        if (tx.getType() == TxType.CONTRACT_CANCEL_DEPOSIT && coinData.getTo().get(0).getAmount().compareTo(depositPo.getDeposit()) > 0) {
            throw new NulsException(ConsensusErrorCode.DATA_ERROR);
        } else if (tx.getType() == TxType.CANCEL_DEPOSIT && coinData.getTo().get(0).getAmount().compareTo(depositPo.getDeposit()) >= 0) {
            throw new NulsException(ConsensusErrorCode.DATA_ERROR);
        }
        return true;
    }

    /**
     * Create node transaction basic verification
     * Create agent transaction base validation
     *
     * @param chain chainID/chain id
     * @param tx    Create node transactions/create transaction
     * @param agent node/agent
     * @return boolean
     */
    private boolean createAgentBasicValid(Chain chain, Transaction tx, Agent agent) throws NulsException {
        if (!AddressTool.validNormalAddress(agent.getPackingAddress(), chain.getConfig().getChainId())) {
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
        //The transaction created by the smart contract is not signed
        if (tx.getType() == TxType.REGISTER_AGENT) {
            //Verify if the handling fee is sufficient
            try {
                int size = tx.serialize().length;
                if (AddressTool.isMultiSignAddress(coinData.getFrom().get(0).getAddress())) {
                    MultiSignTxSignature transactionSignature = new MultiSignTxSignature();
                    transactionSignature.parse(tx.getTransactionSignature(), 0);
                    size += transactionSignature.getM() * P2PHKSignature.SERIALIZE_LENGTH;
                    size -= tx.getTransactionSignature().length;
                }
                BigInteger fee = TransactionFeeCalculator.getConsensusTxFee(size, chain.getConfig().getFeeUnit(chain.getConfig().getAgentChainId(), chain.getConfig().getAgentAssetId()));
                if (fee.compareTo(consensusManager.getFee(coinData, chain.getConfig().getAgentChainId(), chain.getConfig().getAgentAssetId())) > 0) {
                    chain.getLogger().error("Insufficient handling fees！");
                    throw new NulsException(ConsensusErrorCode.FEE_NOT_ENOUGH);
                }
            } catch (IOException e) {
                chain.getLogger().error("Data serialization error！");
                throw new NulsException(ConsensusErrorCode.SERIALIZE_ERROR);
            }
        }
        Set<String> addressSet = new HashSet<>();
        int lockCount = 0;
        for (CoinTo coin : coinData.getTo()) {
            if (coin.getAssetsChainId() != chain.getConfig().getAgentChainId() || coin.getAssetsId() != chain.getConfig().getAgentAssetId()) {
                chain.getLogger().error("Illegal locking of assets");
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
     * Create node transaction node address and block out address verification
     * address validate
     *
     * @param chain chainID/chain id
     * @param tx    Create node transactions/create transaction
     * @param agent node/agent
     * @return boolean
     */
    private boolean createAgentAddrValid(Chain chain, Transaction tx, Agent agent) throws NulsException {
        String seedNodesStr = chain.getConfig().getSeedNodes();
        if (StringUtils.isBlank(seedNodesStr)) {
            return true;
        }
        byte[] nodeAddressBytes;
        //Node address and block address cannot be seed nodes
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
        //Node address and block address cannot be duplicated
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
     * Stop node transactionsCoinDatavalidate
     * Stop agent transaction CoinData validate
     *
     * @param chain    chainID/chain id
     * @param tx       Exit node transaction/stop agent transaction
     * @param agentPo  Exit node information/agent
     * @param coinData TransactionalCoinData/coinData
     * @return boolean
     */
    private boolean stopAgentCoinDataValid(Chain chain, Transaction tx, AgentPo agentPo, StopAgent stopAgent, CoinData coinData) throws NulsException, IOException {
        Agent agent = agentManager.poToAgent(agentPo);
        CoinData localCoinData = coinDataManager.getStopAgentCoinData(chain, agent, coinData.getTo().get(0).getLockTime());
        //coinDataandlocalCoinDatasort
        CoinFromComparator fromComparator = new CoinFromComparator();
        CoinToComparator toComparator = new CoinToComparator();
        coinData.getFrom().sort(fromComparator);
        coinData.getTo().sort(toComparator);
        localCoinData.getFrom().sort(fromComparator);
        localCoinData.getTo().sort(toComparator);
        CoinTo last = localCoinData.getTo().get(localCoinData.getTo().size() - 1);
        if (tx.getType() == TxType.STOP_AGENT) {
            int size = tx.size();
            if (!AddressTool.isMultiSignAddress(agentPo.getAgentAddress())) {
                size += P2PHKSignature.SERIALIZE_LENGTH;
            } else {
                MultiSignTxSignature transactionSignature = new MultiSignTxSignature();
                transactionSignature.parse(tx.getTransactionSignature(), 0);
                size += transactionSignature.getM() * P2PHKSignature.SERIALIZE_LENGTH;
            }
            size -= tx.getTransactionSignature().length;
            BigInteger fee = TransactionFeeCalculator.getNormalTxFee(size, chain.getConfig().getFeeUnit(chain.getConfig().getChainId(), 1));
            last.setAmount(last.getAmount().subtract(fee));
        }
        return Arrays.equals(coinData.serialize(), localCoinData.serialize());
    }

    /**
     * Verification of Basic Information for Entrusted Transactions
     * deposit transaction base validate
     *
     * @param chain   chainID/chain id
     * @param deposit Entrustment information/deposit
     * @return boolean
     */
    private boolean createDepositInfoValid(Chain chain, Deposit deposit) throws NulsException {
        AgentPo agentPo = agentStorageService.get(deposit.getAgentHash(), chain.getConfig().getChainId());
        if (agentPo == null || agentPo.getDelHeight() > 0) {
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        List<DepositPo> poList = this.getDepositListByAgent(chain, deposit.getAgentHash());
        //The current total entrusted amount of the node
        BigInteger total = deposit.getDeposit();
        if (total.compareTo(chain.getConfig().getEntrusterDepositMin()) < 0) {
            throw new NulsException(ConsensusErrorCode.DEPOSIT_NOT_ENOUGH);
        }
        ProtocolContext context = ContextManager.getContext(chain.getConfig().getChainId());
        BigInteger commissionMax = chain.getConfig().getCommissionMax();
        if (context != null && context.getCurrentProtocolVersion() != null && context.getCurrentProtocolVersion().getVersion() >= 23) {
            commissionMax = chain.getConfig().getCommissionMaxV23();
        }
        if (total.compareTo(commissionMax) > 0) {
            throw new NulsException(ConsensusErrorCode.DEPOSIT_OVER_AMOUNT);
        }
        for (DepositPo cd : poList) {
            total = total.add(cd.getDeposit());
        }
        if (total.compareTo(commissionMax) > 0) {
            throw new NulsException(ConsensusErrorCode.DEPOSIT_OVER_AMOUNT);
        }
        return true;
    }

    /**
     * Commission information verification
     * deposit validate
     *
     * @param deposit  Entrustment information/deposit
     * @param coinData TransactionalCoinData/CoinData
     * @return boolean
     */
    private boolean isDepositOk(BigInteger deposit, CoinData coinData) throws NulsException {
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
     * Has the node ever received a red card
     * Does the node get a red card?
     *
     * @param chain   chain info
     * @param address Block address/packing address
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
     * Get the delegation list of nodes
     * Get the delegate list for the node
     *
     * @param chain     chainID/chain id
     * @param agentHash nodeHASH/agent hash
     * @return List<DepositPo>
     */
    public List<DepositPo> getDepositListByAgent(Chain chain, NulsHash agentHash) throws NulsException {
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
     * Obtain the nodes corresponding to red card transactions or stopped node transactions in the block transaction listHashlist
     * Get the node Hash list corresponding to the block transaction list, the red card transaction or the stop node transaction
     *
     * @param redPunishTxs Red card trading/Red card penalty node address
     * @param stopAgentTxs Stop node transaction list/Stop Node Trading List
     * @param chain        chain info
     */
    public Set<NulsHash> getInvalidAgentHash(List<Transaction> redPunishTxs, List<Transaction> contractStopAgentTxs, List<Transaction> stopAgentTxs, Chain chain) {
        Set<String> redPunishAddressSet = new HashSet<>();
        if (redPunishTxs != null && redPunishTxs.size() > 0) {
            for (Transaction redPunishTx : redPunishTxs) {
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
        } catch (Exception e) {
            chain.getLogger().error(e);
        }
        return agentHashSet;
    }
}
