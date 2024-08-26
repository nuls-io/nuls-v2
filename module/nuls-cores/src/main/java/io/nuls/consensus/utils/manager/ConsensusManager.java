package io.nuls.consensus.utils.manager;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.common.NulsCoresConfig;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.economic.base.service.EconomicService;
import io.nuls.consensus.economic.nuls.constant.ParamConstant;
import io.nuls.consensus.economic.nuls.model.bo.AgentInfo;
import io.nuls.consensus.economic.nuls.model.bo.DepositInfo;
import io.nuls.consensus.economic.nuls.model.bo.RoundInfo;
import io.nuls.consensus.model.bo.BlockData;
import io.nuls.consensus.model.bo.Chain;
import io.nuls.consensus.model.bo.ChargeResultData;
import io.nuls.consensus.model.bo.round.MeetingMember;
import io.nuls.consensus.model.bo.round.MeetingRound;
import io.nuls.consensus.model.bo.tx.txdata.Agent;
import io.nuls.consensus.model.bo.tx.txdata.Deposit;
import io.nuls.consensus.rpc.call.CallMethodUtils;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author tag
 * 2018/11/19
 */
@Component
public class ConsensusManager {
    @Autowired
    private PunishManager punishManager;
    @Autowired
    private NulsCoresConfig config;
    @Autowired
    private CoinDataManager coinDataManager;
    @Autowired
    private EconomicService economicService;

    /**
     * CoinBase transaction & Punish transaction
     *
     * @param chain     chain info
     * @param bestBlock local highest block/Latest local blocks
     * @param txList    all tx of block/List of transactions that need to be packaged
     * @param self      agent meeting entity/Node packaging information
     * @param round     latest local round/Latest local round information
     */
    public void addConsensusTx(Chain chain, BlockHeader bestBlock, List<Transaction> txList, MeetingMember self, MeetingRound round, BlockExtendsData extendsData) throws Exception {
        String stateRoot;
        Transaction coinBaseTransaction = createCoinBaseTx(chain, self, txList, round, 0);
        if (AddressTool.validContractAddress(self.getAgent().getRewardAddress(), chain.getConfig().getChainId())) {
            stateRoot = CallMethodUtils.triggerContract(chain.getConfig().getChainId(), RPCUtil.encode(extendsData.getStateRoot()), bestBlock.getHeight(), AddressTool.getStringAddressByBytes(self.getAgent().getRewardAddress()), RPCUtil.encode(coinBaseTransaction.serialize()));
            extendsData.setStateRoot(RPCUtil.decode(stateRoot));
        } else {
            if (coinDataManager.hasContractAddress(coinBaseTransaction.getCoinDataInstance(), chain.getConfig().getChainId())) {
                stateRoot = CallMethodUtils.triggerContract(chain.getConfig().getChainId(), RPCUtil.encode(extendsData.getStateRoot()), bestBlock.getHeight(), null, RPCUtil.encode(coinBaseTransaction.serialize()));
                extendsData.setStateRoot(RPCUtil.decode(stateRoot));
            }
        }
        txList.add(0, coinBaseTransaction);
        punishManager.punishTx(chain, bestBlock, txList, self, round);
    }

    /**
     * assembleCoinBasetransaction
     * Assembling CoinBase transactions
     *
     * @param chain        chain info
     * @param member       packaging information/packing info
     * @param txList       Transaction List/transaction list
     * @param localRound   Latest local round/local newest round info
     * @param unlockHeight Unlocking height/unlock height
     * @return Transaction
     */
    public Transaction createCoinBaseTx(Chain chain, MeetingMember member, List<Transaction> txList, MeetingRound localRound, long unlockHeight) throws IOException, NulsException {
        Transaction tx = new Transaction(TxType.COIN_BASE);
        CoinData coinData = new CoinData();
        /*
        Calculate consensus rewards
        Calculating consensus Awards
        */
        List<CoinTo> rewardList = calcReward(chain, txList, member, localRound, unlockHeight);
        for (CoinTo coin : rewardList) {
            coinData.addTo(coin);
        }
        try {
            tx.setCoinData(coinData.serialize());
        } catch (Exception e) {
            chain.getLogger().error(e);
            coinData = new CoinData();
            rewardList = calcReward(chain, new ArrayList<>(), member, localRound, unlockHeight);
            for (CoinTo coin : rewardList) {
                coinData.addTo(coin);
            }
            tx.setCoinData(coinData.serialize());
        }
        tx.setTime(member.getPackEndTime());
        tx.setHash(NulsHash.calcHash(tx.serializeForHash()));
        return tx;
    }

    /**
     * Calculate consensus rewards
     * Calculating consensus Awards
     *
     * @param chain        chain info
     * @param txList       Transaction List/transaction list
     * @param self         Local packaging information/local agent packing info
     * @param localRound   Latest local round/local newest round info
     * @param unlockHeight Unlocking height/unlock height
     * @return List<CoinTo>
     */
    private List<CoinTo> calcReward(Chain chain, List<Transaction> txList, MeetingMember self, MeetingRound localRound, long unlockHeight) throws NulsException {
        int chainId = chain.getConfig().getChainId();
        int assetsId = chain.getConfig().getAssetId();
        String chainKey = chainId + ConsensusConstant.SEPARATOR + assetsId;
        /*
        Asset and consensus reward key value pairs
        Assets and Consensus Award Key Value Pairs
        Key：assetChainId_assetId
        Value: Consensus reward amount
        */
        Map<String, BigInteger> awardAssetMap = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
        /*
        Calculate intra chain and cross chain transaction fees generated by transactions in blocks
        Calculating intra-chain and cross-chain handling fees for transactions in blocks
        */
        BigInteger returnGas = BigInteger.ZERO;
        for (Transaction tx : txList) {
            int txType = tx.getType();
            if (txType != TxType.COIN_BASE && txType != TxType.CONTRACT_TRANSFER && txType != TxType.CONTRACT_RETURN_GAS && txType != TxType.CONTRACT_CREATE_AGENT
                    && txType != TxType.CONTRACT_STOP_AGENT && txType != TxType.CONTRACT_DEPOSIT && txType != TxType.CONTRACT_CANCEL_DEPOSIT) {
                List<ChargeResultData> resultData = null;

                if (ProtocolGroupManager.getCurrentVersion(chainId) < 20) {
                    resultData = getFeeV1(tx, chain);
                } else {
                    resultData = getFeeV20(tx, chain);
                }

                for (ChargeResultData data : resultData) {
                    String key = data.getKey();
                    if (awardAssetMap.keySet().contains(key)) {
                        awardAssetMap.put(key, awardAssetMap.get(key).add(data.getFee()));
                    } else {
                        awardAssetMap.put(key, data.getFee());
                    }
                }
            }
            if (txType == TxType.CONTRACT_RETURN_GAS) {
                CoinData coinData = new CoinData();
                coinData.parse(tx.getCoinData(), 0);
                for (CoinTo to : coinData.getTo()) {
                    returnGas = returnGas.add(to.getAmount());
                }
            }
        }
        BigInteger chainFee = awardAssetMap.get(chainKey);
        if (returnGas.compareTo(BigInteger.ZERO) > 0) {
            chainFee = awardAssetMap.get(chainKey).subtract(returnGas);
        }
        if (chainFee == null || chainFee.compareTo(BigInteger.ZERO) <= 0) {
            awardAssetMap.remove(chainKey);
        } else {
            awardAssetMap.put(chainKey, chainFee);
        }

        /*
        Chain reward list
        Chain reward list
        */
        return getRewardCoin(self, localRound, unlockHeight, awardAssetMap, chain);
    }


    /**
     * Create blocks
     * create block
     *
     * @param chain                chain info
     * @param blockData            block entity/Block data
     * @param packingAddress       packing address/Packaging address
     * @param packingAddressString packing address/Packaging address
     * @return Block
     */
    public Block createBlock(Chain chain, BlockData blockData, byte[] packingAddress, String packingAddressString) {
        try {
            String password = chain.getConfig().getPassword();
            CallMethodUtils.accountValid(chain.getConfig().getChainId(), packingAddressString, password);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return null;
        }
        Block block = new Block();
        block.setTxs(blockData.getTxList());
        BlockHeader header = new BlockHeader();
        block.setHeader(header);
        try {
            header.setExtend(blockData.getExtendsData().serialize());
        } catch (IOException e) {
            chain.getLogger().error(e.getMessage());
            throw new NulsRuntimeException(e);
        }
        header.setHeight(blockData.getHeight());
        header.setTime(blockData.getTime());
        header.setPreHash(blockData.getPreHash());
        header.setTxCount(blockData.getTxList().size());
        header.setPackingAddress(packingAddress);
        List<NulsHash> txHashList = new ArrayList<>();
        for (int i = 0; i < blockData.getTxList().size(); i++) {
            Transaction tx = blockData.getTxList().get(i);
            tx.setBlockHeight(header.getHeight());
            txHashList.add(tx.getHash());
        }
        header.setMerkleHash(NulsHash.calcMerkleHash(txHashList));
        try {
            CallMethodUtils.blockSignature(chain, packingAddressString, header);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return null;
        }
        return block;
    }

    /**
     * Calculate transaction fees
     * Calculating transaction fees
     *
     * @param tx    transaction/transaction
     * @param chain chain info
     * @return ChargeResultData
     */
    private List<ChargeResultData> getFeeV20(Transaction tx, Chain chain) throws NulsException {
        List<ChargeResultData> list = new ArrayList<>();
        CoinData coinData = new CoinData();
        coinData.parse(tx.getCoinData(), 0);
        /*
        Cross chain transaction calculation fees
        Cross-Chain Transactions Calculate Processing Fees
        */
        boolean isCrossTx = tx.getType() == TxType.CROSS_CHAIN;
        if (config.getMainChainId() == chain.getConfig().getChainId()) {
            isCrossTx = isCrossTx || tx.getType() == TxType.CONTRACT_TOKEN_CROSS_TRANSFER;
        }
        if (isCrossTx) {
            int feeChainId = chain.getConfig().getChainId();
            int feeAssetId = chain.getConfig().getAssetId();
            /*
            Calculate in chain transaction fees,fromMain assets within the medium chain - toSum of main assets within the mid chain
            Calculate in-chain handling fees, from in-chain main assets - to in-chain main assets and
            */
            if (AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress()) == feeChainId && feeChainId != config.getMainChainId()) {
                list.add(new ChargeResultData(getFee(coinData, feeChainId, feeAssetId), feeChainId, feeAssetId));
                return list;
            }
            /*
            Calculate main chain and friend chain transaction fees,First, calculateCoinDataTotal cross chain handling fees in the middle, and then divide the cross chain handling fees according to the proportion
            Calculate the main chain and friendship chain handling fees, first calculate the total cross-chain handling fees in CoinData,
            and then divide the cross-chain handling fees according to the proportion.
            */
            BigInteger fee = getFee(coinData, config.getMainChainId(), config.getMainAssetId());
            /*
            If the current chain is the main chain,If the cross chain transaction target is the main chain, the main chain will charge all cross chain transaction fees. If the target is other chains, the main chain will charge a certain proportion of cross chain transaction fees
            If the current chain is the main chain and the target of cross-chain transaction is connected to the main chain, the main chain charges all cross-chain handling fees,
            and if the target is connected to other chains, the main chain charges a certain proportion of cross-chain handling fees.
            */
            int mainCommissionRatio = config.getMainChainCommissionRatio();
            if (feeChainId == config.getMainChainId()) {
                int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());
                if (toChainId == config.getMainChainId()) {
                    list.add(new ChargeResultData(fee, config.getMainChainId(), config.getMainAssetId()));
                    return list;
                }
                list.add(new ChargeResultData(fee.multiply(new BigInteger(String.valueOf(mainCommissionRatio))).divide(new BigInteger(String.valueOf(ConsensusConstant.VALUE_OF_ONE_HUNDRED))), config.getMainChainId(), config.getMainAssetId()));
                return list;
            }
            list.add(new ChargeResultData(fee.multiply(new BigInteger(String.valueOf(ConsensusConstant.VALUE_OF_ONE_HUNDRED - mainCommissionRatio))).divide(new BigInteger(String.valueOf(ConsensusConstant.VALUE_OF_ONE_HUNDRED))), config.getMainChainId(), config.getMainAssetId()));
            return list;
        } else if (tx.getType() == TxType.REGISTER_AGENT || tx.getType() == TxType.STOP_AGENT || tx.getType() == TxType.DEPOSIT || tx.getType() == TxType.CANCEL_DEPOSIT) {
            int feeChainId = chain.getConfig().getChainId();
            int feeAssetId = chain.getConfig().getAssetId();
            list.add(new ChargeResultData(getFee(coinData, feeChainId, feeAssetId), feeChainId, feeAssetId));
        } else {
            for (CoinFrom from : coinData.getFrom()) {
                BigInteger amount = getFee(coinData, from.getAssetsChainId(), from.getAssetsId());
                if (amount != null && amount.compareTo(BigInteger.ZERO) > 0) {
                    list.add(new ChargeResultData(amount, from.getAssetsChainId(), from.getAssetsId()));
                }
            }
        }
        if (list.isEmpty()) {
            int feeChainId = chain.getConfig().getChainId();
            int feeAssetId = chain.getConfig().getAssetId();
            list.add(new ChargeResultData(BigInteger.ZERO, feeChainId, feeAssetId));
        }
        return list;
    }

    private List<ChargeResultData> getFeeV1(Transaction tx, Chain chain) throws NulsException {
        List<ChargeResultData> list = new ArrayList<>();
        list.add(getRealFeeV1(tx, chain));
        return list;
    }

    private ChargeResultData getRealFeeV1(Transaction tx, Chain chain) throws NulsException {
        CoinData coinData = new CoinData();
        int feeChainId = chain.getConfig().getChainId();
        int feeAssetId = chain.getConfig().getAssetId();
        coinData.parse(tx.getCoinData(), 0);
        /*
        Cross chain transaction calculation fees
        Cross-Chain Transactions Calculate Processing Fees
        */
        boolean isCrossTx = tx.getType() == TxType.CROSS_CHAIN;
        if (config.getMainChainId() == chain.getConfig().getChainId()) {
            isCrossTx = isCrossTx || tx.getType() == TxType.CONTRACT_TOKEN_CROSS_TRANSFER;
        }
        if (isCrossTx) {
            /*
            Calculate in chain transaction fees,fromMain assets within the medium chain - toSum of main assets within the mid chain
            Calculate in-chain handling fees, from in-chain main assets - to in-chain main assets and
            */
            if (AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress()) == feeChainId && feeChainId != config.getMainChainId()) {
                return new ChargeResultData(getFee(coinData, feeChainId, feeAssetId), feeChainId, feeAssetId);
            }
            /*
            Calculate main chain and friend chain transaction fees,First, calculateCoinDataTotal cross chain handling fees in the middle, and then divide the cross chain handling fees according to the proportion
            Calculate the main chain and friendship chain handling fees, first calculate the total cross-chain handling fees in CoinData,
            and then divide the cross-chain handling fees according to the proportion.
            */
            BigInteger fee = getFee(coinData, config.getMainChainId(), config.getMainAssetId());
            /*
            If the current chain is the main chain,If the cross chain transaction target is the main chain, the main chain will charge all cross chain transaction fees. If the target is other chains, the main chain will charge a certain proportion of cross chain transaction fees
            If the current chain is the main chain and the target of cross-chain transaction is connected to the main chain, the main chain charges all cross-chain handling fees,
            and if the target is connected to other chains, the main chain charges a certain proportion of cross-chain handling fees.
            */
            int mainCommissionRatio = config.getMainChainCommissionRatio();
            if (feeChainId == config.getMainChainId()) {
                int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());
                if (toChainId == config.getMainChainId()) {
                    return new ChargeResultData(fee, config.getMainChainId(), config.getMainAssetId());
                }
                return new ChargeResultData(fee.multiply(new BigInteger(String.valueOf(mainCommissionRatio))).divide(new BigInteger(String.valueOf(ConsensusConstant.VALUE_OF_ONE_HUNDRED))), config.getMainChainId(), config.getMainAssetId());
            }
            return new ChargeResultData(fee.multiply(new BigInteger(String.valueOf(ConsensusConstant.VALUE_OF_ONE_HUNDRED - mainCommissionRatio))).divide(new BigInteger(String.valueOf(ConsensusConstant.VALUE_OF_ONE_HUNDRED))), config.getMainChainId(), config.getMainAssetId());
        } else if (tx.getType() == TxType.REGISTER_AGENT || tx.getType() == TxType.STOP_AGENT || tx.getType() == TxType.DEPOSIT || tx.getType() == TxType.CANCEL_DEPOSIT) {
            feeChainId = chain.getConfig().getAgentChainId();
            feeAssetId = chain.getConfig().getAgentAssetId();
        }
        return new ChargeResultData(getFee(coinData, feeChainId, feeAssetId), feeChainId, feeAssetId);
    }

    /**
     * Calculate designated handling fees
     *
     * @param coinData     coinData
     * @param assetChainId Designated asset chainID
     * @param assetId      Designated assetsID
     * @return Handling fee size
     */
    public BigInteger getFee(CoinData coinData, int assetChainId, int assetId) {
        BigInteger fromAmount = BigInteger.ZERO;
        BigInteger toAmount = BigInteger.ZERO;
        for (CoinFrom from : coinData.getFrom()) {
            if (from.getAssetsChainId() == assetChainId && from.getAssetsId() == assetId) {
                fromAmount = fromAmount.add(from.getAmount());
            }
        }
        for (CoinTo to : coinData.getTo()) {
            if (to.getAssetsChainId() == assetChainId && to.getAssetsId() == assetId) {
                toAmount = toAmount.add(to.getAmount());
            }
        }
        return fromAmount.subtract(toAmount);
    }

    /**
     * Distribute consensus rewards
     *
     * @param self          Local packaging information/local agent packing info
     * @param localRound    Latest local round/local newest round info
     * @param unlockHeight  Unlocking height/unlock height
     * @param awardAssetMap Collection of handling fees
     * @param chain         chain info
     * @return Cross chain transaction distribution set
     */
    @SuppressWarnings("unchecked")
    private List<CoinTo> getRewardCoin(MeetingMember self, MeetingRound localRound, long unlockHeight, Map<String, BigInteger> awardAssetMap, Chain chain) throws NulsException {
        Map<String, Object> param = new HashMap<>(4);

        RoundInfo roundInfo = new RoundInfo(localRound.getTotalWeight(), localRound.getStartTime(), localRound.getEndTime(), localRound.getMemberCount());

        List<DepositInfo> depositList = new ArrayList<>();
        for (Deposit deposit : self.getDepositList()) {
            DepositInfo depositInfo = new DepositInfo(deposit.getDeposit(), deposit.getAddress());
            depositList.add(depositInfo);
        }

        Agent agent = self.getAgent();
        AgentInfo agentInfo = new AgentInfo(agent.getCommissionRate(), agent.getDeposit(), agent.getRewardAddress(), agent.getTotalDeposit(), agent.getCreditVal(), depositList);

        param.put(ParamConstant.CHAIN_ID, chain.getConfig().getChainId());
        param.put(ParamConstant.ROUND_INFO, roundInfo);
        param.put(ParamConstant.AGENT_INFO, agentInfo);
        param.put(ParamConstant.AWARD_ASSERT_MAP, awardAssetMap);

        Result result = economicService.calcReward(param);
        if (result.isFailed()) {
            chain.getLogger().error("Miscalculation of Consensus Reward");
            throw new NulsException(result.getErrorCode());
        }
        return (List<CoinTo>) ((Map<String, Object>) result.getData()).get("coinToList");
    }

    /**
     * Distribute consensus rewards
     * @param self             Local packaging information/local agent packing info
     * @param localRound       Latest local round/local newest round info
     * @param unlockHeight     Unlocking height/unlock height
     * @param awardAssetMap    Collection of handling fees
     * @param chain            chain info
     * @return Cross chain transaction distribution set
     * */
    /*private List<CoinTo> getRewardCoin(MeetingMember self, MeetingRound localRound, long unlockHeight,Map<String, BigInteger> awardAssetMap, Chain chain){
        List<CoinTo> rewardList = new ArrayList<>();
        *//*
        If it is a seed node, only receive transaction fees without calculating consensus rewards（The seed node deposit is0）
        If it is a seed node, it only receives transaction fee without calculating consensus award (seed node margin is 0)
        *//*
        if (BigIntegerUtils.isEqual(self.getAgent().getDeposit(), BigInteger.ZERO)) {
            if(awardAssetMap == null || awardAssetMap.isEmpty()){
                return rewardList;
            }
            for (Map.Entry<String, BigInteger> rewardEntry:awardAssetMap.entrySet()) {
                String[] assetInfo = rewardEntry.getKey().split(ConsensusConstant.SEPARATOR);
                CoinTo agentReword = new CoinTo(self.getAgent().getRewardAddress(), Integer.valueOf(assetInfo[0]), Integer.valueOf(assetInfo[1]), rewardEntry.getValue(), unlockHeight);
                rewardList.add(agentReword);
            }
            return rewardList;
        }
        *//*
        The total reward for this round's block out bonus(The number of block nodes in this round*Consensus based rewards )
        Total reward in this round
        *//*
        BigDecimal totalAll = DoubleUtils.mul(new BigDecimal(localRound.getMemberCount()), new BigDecimal(chain.getConfig().getBlockReward()));
        BigInteger selfAllDeposit = self.getAgent().getDeposit().add(self.getAgent().getTotalDeposit());
        BigDecimal agentWeight = DoubleUtils.mul(new BigDecimal(selfAllDeposit), self.getAgent().getCreditVal());
        if (localRound.getTotalWeight() > 0 && agentWeight.doubleValue() > 0) {
            *//*
            Consensus reward for this node = Node weight/Weight for this round*Consensus based rewards
            Node Consensus Award = Node Weight/Round Weight*Consensus Foundation Award
            *//*
            BigInteger consensusReword = DoubleUtils.mul(totalAll, DoubleUtils.div(agentWeight, localRound.getTotalWeight())).toBigInteger();
            String assetKey = chain.getConfig().getChainId() + ConsensusConstant.SEPARATOR + chain.getConfig().getAwardAssetId();
            if(awardAssetMap.keySet().contains(assetKey)){
                awardAssetMap.put(assetKey, awardAssetMap.get(assetKey).add(consensusReword));
            }else{
                awardAssetMap.put(assetKey, consensusReword);
            }
        }
        if(awardAssetMap == null || awardAssetMap.isEmpty()){
            return rewardList;
        }
        //Calculate the weight of participating consensus accounts
        Map<String,BigDecimal> depositWeightMap = getDepositWeight(self, selfAllDeposit);
        for (Map.Entry<String, BigInteger> rewardEntry:awardAssetMap.entrySet()) {
            String[] assetInfo = rewardEntry.getKey().split(ConsensusConstant.SEPARATOR);
            BigDecimal totalReward = new BigDecimal(rewardEntry.getValue());
            rewardList.addAll(assembleCoinTo(depositWeightMap, Integer.valueOf(assetInfo[0]),Integer.valueOf(assetInfo[1]) ,totalReward ,unlockHeight ));
        }
        return rewardList;
    }*/


    /**
     * Calculate the weight of accounts participating in consensus
     * Calculating Account Weights for Participating in Consensus
     * @param self           Current node information
     * @param totalDeposit   The total weight of the current node in this round
     * @return Details of weight allocation for accounts participating in consensus
     * */
    /*private Map<String,BigDecimal> getDepositWeight(MeetingMember self,BigInteger totalDeposit){
        Map<String,BigDecimal> depositWeightMap = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
        BigDecimal commissionRate = new BigDecimal(DoubleUtils.div(self.getAgent().getCommissionRate(), 100, 2));
        BigDecimal depositRate = new BigDecimal(1).subtract(commissionRate);
        //Node creator weight
        BigDecimal creatorWeight = new BigDecimal(self.getAgent().getDeposit()).divide(new BigDecimal(totalDeposit), 4, RoundingMode.HALF_DOWN);
        BigDecimal creatorCommissionWeight = new BigDecimal(1).subtract(creatorWeight).multiply(commissionRate);
        creatorWeight = creatorWeight.add(creatorCommissionWeight);
        depositWeightMap.put(AddressTool.getStringAddressByBytes(self.getAgent().getRewardAddress()), creatorWeight);
        *//*
        Calculate the reward received by each entrusted account
        Calculate the rewards for each entrusted account
        *//*
        for (Deposit deposit : self.getDepositList()) {
            *//*
            Calculate the weight of each entrusted account（Entrusted amount/Total commission)
            Calculate the weight of each entrusted account (amount of entrusted account/total entrusted fee)
            *//*
            String depositAddress = AddressTool.getStringAddressByBytes(deposit.getAddress());
            BigDecimal depositWeight = new BigDecimal(deposit.getDeposit()).divide(new BigDecimal(totalDeposit), 4, RoundingMode.HALF_DOWN).multiply(depositRate);
            if(depositWeightMap.keySet().contains(depositAddress)){
                depositWeightMap.put(depositAddress, depositWeightMap.get(depositAddress).add(depositWeight));
            }else{
                depositWeightMap.put(depositAddress, depositWeight);
            }
        }
        return depositWeightMap;
    }*/

    /**
     * assembleCoinTo
     * @param depositWeightMap   Weight allocation for participating consensus accounts
     * @param assetChainId       Asset ChainID
     * @param assetId            assetID
     * @param totalReward        Total reward
     * @param unlockHeight       Lock height
     * @return CoinTo
     * */
    /*private List<CoinTo> assembleCoinTo(Map<String,BigDecimal> depositWeightMap,int assetChainId,int assetId,BigDecimal totalReward, long unlockHeight){
        List<CoinTo> coinToList = new ArrayList<>();
        for (Map.Entry<String,BigDecimal> entry:depositWeightMap.entrySet()) {
            String address = entry.getKey();
            BigDecimal depositWeight = entry.getValue();
            BigInteger amount = totalReward.multiply(depositWeight).toBigInteger();
            CoinTo coinTo = new CoinTo(AddressTool.getAddress(address),assetChainId,assetId,amount,unlockHeight);
            coinToList.add(coinTo);
        }
        return  coinToList;
    }*/
}
