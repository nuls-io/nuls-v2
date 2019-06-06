package io.nuls.poc.utils.manager;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.DoubleUtils;
import io.nuls.poc.constant.ConsensusConfig;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.BlockData;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.ChargeResultData;
import io.nuls.poc.model.bo.round.MeetingMember;
import io.nuls.poc.model.bo.round.MeetingRound;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.rpc.call.CallMethodUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

/**
 * @author tag
 * 2018/11/19
 */
@Component
public class ConsensusManager {
    @Autowired
    private PunishManager punishManager;
    @Autowired
    private ConsensusConfig config;
    @Autowired
    private CoinDataManager coinDataManager;

    /**
     * CoinBase transaction & Punish transaction
     *
     * @param chain     chain info
     * @param bestBlock local highest block/本地最新区块
     * @param txList    all tx of block/需打包的交易列表
     * @param self      agent meeting entity/节点打包信息
     * @param round     latest local round/本地最新轮次信息
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
     * 组装CoinBase交易
     * Assembling CoinBase transactions
     *
     * @param chain        chain info
     * @param member       打包信息/packing info
     * @param txList       交易列表/transaction list
     * @param localRound   本地最新轮次/local newest round info
     * @param unlockHeight 解锁高度/unlock height
     * @return Transaction
     */
    public Transaction createCoinBaseTx(Chain chain, MeetingMember member, List<Transaction> txList, MeetingRound localRound, long unlockHeight) throws IOException, NulsException {
        Transaction tx = new Transaction(TxType.COIN_BASE);
        CoinData coinData = new CoinData();
        /*
        计算共识奖励
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
     * 计算共识奖励
     * Calculating consensus Awards
     *
     * @param chain        chain info
     * @param txList       交易列表/transaction list
     * @param self         本地打包信息/local agent packing info
     * @param localRound   本地最新轮次/local newest round info
     * @param unlockHeight 解锁高度/unlock height
     * @return List<CoinTo>
     */
    private List<CoinTo> calcReward(Chain chain, List<Transaction> txList, MeetingMember self, MeetingRound localRound, long unlockHeight) throws NulsException {
        int chainId = chain.getConfig().getChainId();
        int assetsId = chain.getConfig().getAssetId();
        String chainKey = chainId + ConsensusConstant.SEPARATOR + assetsId;
        /*
        资产与共识奖励键值对
        Assets and Consensus Award Key Value Pairs
        Key：assetChainId_assetId
        Value: 共识奖励金额
        */
        Map<String, BigInteger> awardAssetMap = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
        /*
        计算区块中交易产生的链内和跨链手续费
        Calculating intra-chain and cross-chain handling fees for transactions in blocks
        */
        BigInteger returnGas = BigInteger.ZERO;
        for (Transaction tx : txList) {
            int txType = tx.getType();
            if (txType != TxType.COIN_BASE && txType != TxType.CONTRACT_TRANSFER && txType != TxType.CONTRACT_RETURN_GAS && txType != TxType.CONTRACT_CREATE_AGENT
                    && txType != TxType.CONTRACT_STOP_AGENT && txType != TxType.CONTRACT_DEPOSIT && txType != TxType.CONTRACT_CANCEL_DEPOSIT) {
                ChargeResultData resultData = getFee(tx, chain);
                String key = resultData.getKey();
                if(awardAssetMap.keySet().contains(key)){
                    awardAssetMap.put(key, awardAssetMap.get(key).add(resultData.getFee()));
                }else{
                    awardAssetMap.put(key, resultData.getFee());
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
        if(returnGas.compareTo(BigInteger.ZERO) > 0){
            chainFee = awardAssetMap.get(chainKey).subtract(returnGas);
        }
        if(chainFee == null || chainFee.compareTo(BigInteger.ZERO) <= 0){
            awardAssetMap.remove(chainKey);
        }else{
            awardAssetMap.put(chainKey, chainFee);
        }

        /*
        链内奖励列表
        Chain reward list
        */
        return getRewardCoin(self, localRound, unlockHeight, awardAssetMap, chain);
    }


    /**
     * 创建区块
     * create block
     *
     * @param chain          chain info
     * @param blockData      block entity/区块数据
     * @param packingAddress packing address/打包地址
     * @return Block
     */
    public Block createBlock(Chain chain, BlockData blockData, byte[] packingAddress) {
        try {
            String password = chain.getConfig().getPassword();
            CallMethodUtils.accountValid(chain.getConfig().getChainId(), AddressTool.getStringAddressByBytes(packingAddress), password);
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
            CallMethodUtils.blockSignature(chain, AddressTool.getStringAddressByBytes(packingAddress), header);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return null;
        }
        return block;
    }

    /**
     * 计算交易手续费
     * Calculating transaction fees
     *
     * @param tx    transaction/交易
     * @param chain chain info
     * @return ChargeResultData
     */
    private ChargeResultData getFee(Transaction tx, Chain chain) throws NulsException {
        CoinData coinData = new CoinData();
        int feeChainId = chain.getConfig().getChainId();
        int feeAssetId = chain.getConfig().getAssetId();
        coinData.parse(tx.getCoinData(), 0);
        /*
        跨链交易计算手续费
        Cross-Chain Transactions Calculate Processing Fees
        */
        if (tx.getType() == TxType.CROSS_CHAIN) {
            /*
            计算链内手续费，from中链内主资产 - to中链内主资产的和
            Calculate in-chain handling fees, from in-chain main assets - to in-chain main assets and
            */
            if (AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress()) == feeChainId) {
                return new ChargeResultData(getFee(coinData, feeChainId, feeAssetId), feeChainId, feeAssetId);
            }
            /*
            计算主链和友链手续费,首先计算CoinData中总的跨链手续费，然后根据比例分跨链手续费
            Calculate the main chain and friendship chain handling fees, first calculate the total cross-chain handling fees in CoinData,
            and then divide the cross-chain handling fees according to the proportion.
            */
            BigInteger fee = getFee(coinData, config.getMainChainId(), config.getMainAssetId());
            /*
            如果当前链为主链,且跨链交易目标连为主链则主链收取全部跨链手续费，如果目标连为其他链则主链收取一定比例的跨链手续费
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
     * 计算指定手续费
     * @param coinData         coinData
     * @param assetChainId     指定资产链ID
     * @param assetId          指定资产ID
     * @return                 手续费大小
     * */
    private BigInteger getFee(CoinData coinData , int assetChainId, int assetId){
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
     * 分发共识奖励
     * @param self             本地打包信息/local agent packing info
     * @param localRound       本地最新轮次/local newest round info
     * @param unlockHeight     解锁高度/unlock height
     * @param awardAssetMap    手续费集合
     * @param chain            chain info
     * @return                 跨链交易分发集合
     * */
    private List<CoinTo> getRewardCoin(MeetingMember self, MeetingRound localRound, long unlockHeight,Map<String, BigInteger> awardAssetMap, Chain chain){
        List<CoinTo> rewardList = new ArrayList<>();
        /*
        如果为种子节点，只领取交易手续费不计算共识奖励（种子节点保证金为0）
        If it is a seed node, it only receives transaction fee without calculating consensus award (seed node margin is 0)
        */
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
        /*
        本轮次总的出块奖励金(本轮次出块节点数*共识基础奖励 )
        Total reward in this round
        */
        BigDecimal totalAll = DoubleUtils.mul(new BigDecimal(localRound.getMemberCount()), new BigDecimal(chain.getConfig().getBlockReward()));
        BigInteger selfAllDeposit = self.getAgent().getDeposit().add(self.getAgent().getTotalDeposit());
        BigDecimal agentWeight = DoubleUtils.mul(new BigDecimal(selfAllDeposit), self.getAgent().getCreditVal());
        if (localRound.getTotalWeight() > 0 && agentWeight.doubleValue() > 0) {
            /*
            本节点共识奖励 = 节点权重/本轮次权重*共识基础奖励
            Node Consensus Award = Node Weight/Round Weight*Consensus Foundation Award
            */
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
        //计算参与共识账户的权重
        Map<String,BigDecimal> depositWeightMap = getDepositWeight(self, selfAllDeposit);
        for (Map.Entry<String, BigInteger> rewardEntry:awardAssetMap.entrySet()) {
            String[] assetInfo = rewardEntry.getKey().split(ConsensusConstant.SEPARATOR);
            BigDecimal totalReward = new BigDecimal(rewardEntry.getValue());
            rewardList.addAll(assembleCoinTo(depositWeightMap, Integer.valueOf(assetInfo[0]),Integer.valueOf(assetInfo[1]) ,totalReward ,unlockHeight ));
        }
        return rewardList;
    }


    /**
     * 计算参与共识的账户权重
     * Calculating Account Weights for Participating in Consensus
     * @param self           当前节点信息
     * @param totalDeposit   当前节点本轮次总权重
     * @return               参与共识的账户权重分配详情
     * */
    private Map<String,BigDecimal> getDepositWeight(MeetingMember self,BigInteger totalDeposit){
        Map<String,BigDecimal> depositWeightMap = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
        BigDecimal commissionRate = new BigDecimal(DoubleUtils.div(self.getAgent().getCommissionRate(), 100, 2));
        BigDecimal depositRate = new BigDecimal(1).subtract(commissionRate);
        //节点创建者权重
        BigDecimal creatorWeight = new BigDecimal(self.getAgent().getDeposit()).divide(new BigDecimal(totalDeposit), 4, RoundingMode.HALF_DOWN);
        BigDecimal creatorCommissionWeight = new BigDecimal(1).subtract(creatorWeight).multiply(commissionRate);
        creatorWeight = creatorWeight.add(creatorCommissionWeight);
        depositWeightMap.put(AddressTool.getStringAddressByBytes(self.getAgent().getRewardAddress()), creatorWeight);
        /*
        计算各委托账户获得的奖励金
        Calculate the rewards for each entrusted account
        */
        for (Deposit deposit : self.getDepositList()) {
            /*
            计算各委托账户权重（委托金额/总的委托金)
            Calculate the weight of each entrusted account (amount of entrusted account/total entrusted fee)
            */
            String depositAddress = AddressTool.getStringAddressByBytes(deposit.getAddress());
            BigDecimal depositWeight = new BigDecimal(deposit.getDeposit()).divide(new BigDecimal(totalDeposit), 4, RoundingMode.HALF_DOWN).multiply(depositRate);
            if(depositWeightMap.keySet().contains(depositAddress)){
                depositWeightMap.put(depositAddress, depositWeightMap.get(depositAddress).add(depositWeight));
            }else{
                depositWeightMap.put(depositAddress, depositWeight);
            }
        }
        return depositWeightMap;
    }

    /**
     * 组装CoinTo
     * @param depositWeightMap   参与共识账户的权重分配
     * @param assetChainId       资产链ID
     * @param assetId            资产ID
     * @param totalReward        总的奖励金
     * @param unlockHeight       锁定高度
     * @return                   CoinTo
     * */
    private List<CoinTo> assembleCoinTo(Map<String,BigDecimal> depositWeightMap,int assetChainId,int assetId,BigDecimal totalReward, long unlockHeight){
        List<CoinTo> coinToList = new ArrayList<>();
        for (Map.Entry<String,BigDecimal> entry:depositWeightMap.entrySet()) {
            String address = entry.getKey();
            BigDecimal depositWeight = entry.getValue();
            BigInteger amount = totalReward.multiply(depositWeight).toBigInteger();
            CoinTo coinTo = new CoinTo(AddressTool.getAddress(address),assetChainId,assetId,amount,unlockHeight);
            coinToList.add(coinTo);
        }
        return  coinToList;
    }
}
