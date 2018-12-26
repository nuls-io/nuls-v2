package io.nuls.poc.utils.manager;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.BlockData;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.ChargeResultData;
import io.nuls.poc.model.bo.round.MeetingMember;
import io.nuls.poc.model.bo.round.MeetingRound;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.utils.CallMethodUtils;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.DoubleUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

/**
 * @author tag
 * 2018/11/19
 */
@Component
public class ConsensusManager {
    @Autowired
    private PunishManager punishManager;
    /**
     * CoinBase transaction & Punish transaction
     *
     * @param chain     chain info
     * @param bestBlock local highest block/本地最新区块
     * @param txList    all tx of block/需打包的交易列表
     * @param self      agent meeting data/节点打包信息
     * @param round     latest local round/本地最新轮次信息
     */
    public void addConsensusTx(Chain chain, BlockHeader bestBlock, List<Transaction> txList, MeetingMember self, MeetingRound round) throws NulsException, IOException {
        int assetsId =chain.getConfig().getAssetsId();
        Transaction coinBaseTransaction = createCoinBaseTx(chain,self, txList, round, bestBlock.getHeight() + 1 + chain.getConfig().getCoinbaseUnlockHeight());
        txList.add(0, coinBaseTransaction);
        punishManager.punishTx(chain, bestBlock, txList, self, round);
    }

    /**
     * 组装CoinBase交易
     * Assembling CoinBase transactions
     *
     * @param chain         chain info
     * @param member        打包信息/packing info
     * @param txList        交易列表/transaction list
     * @param localRound    本地最新轮次/local newest round info
     * @param unlockHeight  解锁高度/unlock height
     * @return Transaction
     */
    public Transaction createCoinBaseTx(Chain chain,MeetingMember member, List<Transaction> txList, MeetingRound localRound, long unlockHeight) throws IOException,NulsException {
        Transaction tx = new Transaction(ConsensusConstant.TX_TYPE_COINBASE);
        try {
            CoinData coinData = new CoinData();
            /*
            计算共识奖励
            Calculating consensus Awards
            */
            List<CoinTo> rewardList = calcReward(chain,txList, member, localRound, unlockHeight);
            for (CoinTo coin : rewardList) {
                coinData.addTo(coin);
            }
            tx.setTime(member.getPackEndTime());
            tx.setCoinData(coinData.serialize());
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e.getMessage());
            throw e;
        }catch (NulsException e){
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e.getMessage());
            throw e;
        }
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
    private List<CoinTo> calcReward(Chain chain,List<Transaction> txList, MeetingMember self, MeetingRound localRound, long unlockHeight) throws NulsException{
        int chainId = chain.getConfig().getChainId();
        int assetsId = chain.getConfig().getAssetsId();
        /*
        链内交易手续费(资产为链内主资产)
        Intra-chain transaction fees (assets are the main assets in the chain)
        */
        BigInteger totalFee = BigInteger.ZERO;

        /*
        跨链交易手续费(资产为主链主资产)
        Cross-Chain Transaction Fees (Assets as Main Chain Assets)
        */
        BigInteger crossFee = BigInteger.ZERO;

        /*
        计算区块中交易产生的链内和跨链手续费
        Calculating intra-chain and cross-chain handling fees for transactions in blocks
        */
        for (Transaction tx : txList) {
            CoinData coinData = new CoinData();
            coinData.parse(tx.getCoinData(), 0);
            ChargeResultData resultData = getFee(tx,chain);
            if(resultData.getChainId() == chainId){
                totalFee = totalFee.add(resultData.getFee());
            }else{
                crossFee = crossFee.add(resultData.getFee());
            }
        }

        /*
        链内奖励列表
        Chain reward list
        */
        List<CoinTo> inRewardList = new ArrayList<>();
        /*
        跨链交易奖励
        Cross link trading incentives
        */
        List<CoinTo> outRewardList = new ArrayList<>();


        /*
        如果为种子节点，只领取交易手续费不计算共识奖励（种子节点保证金为0）
        If it is a seed node, it only receives transaction fee without calculating consensus award (seed node margin is 0)
        */
        if (BigIntegerUtils.isEqual(self.getAgent().getDeposit(), BigInteger.ZERO)) {
            if (!BigIntegerUtils.isEqual(totalFee,BigInteger.ZERO)) {
                CoinTo agentReword = new CoinTo(self.getAgent().getRewardAddress(),chainId,assetsId,totalFee,unlockHeight);
                inRewardList.add(agentReword);
            }
            if(!BigIntegerUtils.isEqual(crossFee,BigInteger.ZERO)){
                CoinTo agentReword = new CoinTo(self.getAgent().getRewardAddress(),ConsensusConstant.MAIN_CHAIN_ID,ConsensusConstant.MAIN_ASSETS_ID,crossFee,unlockHeight);
                outRewardList.add(agentReword);
            }
            inRewardList.addAll(outRewardList);
            return inRewardList;
        }

        /*
        本轮次总的出块奖励金(本轮次出块节点数*共识基础奖励 )
        Total reward in this round
        */
        BigDecimal totalAll = DoubleUtils.mul(new BigDecimal(localRound.getMemberCount()), new BigDecimal(ConsensusConstant.BLOCK_REWARD));
        double commissionRate = DoubleUtils.div(self.getAgent().getCommissionRate(), 100, 2);
        BigInteger selfAllDeposit = self.getAgent().getDeposit().add(self.getAgent().getTotalDeposit());
        BigDecimal agentWeight = DoubleUtils.mul(new BigDecimal(selfAllDeposit), self.getAgent().getCreditVal());

        double inBlockReword = totalFee.doubleValue();
        double outBlockReword = crossFee.doubleValue();
        if (localRound.getTotalWeight() > 0 && agentWeight.doubleValue() > 0) {
            /*
            本节点共识奖励 = 节点权重/本轮次权重*共识基础奖励
            Node Consensus Award = Node Weight/Round Weight*Consensus Foundation Award
            */
            inBlockReword = DoubleUtils.sum(inBlockReword, DoubleUtils.mul(totalAll, DoubleUtils.div(agentWeight, localRound.getTotalWeight())).doubleValue());
        }
        if (inBlockReword == 0 && outBlockReword == 0) {
            return inRewardList;
        }
        /*
        创建节点账户所得共识奖励金，总的奖励金*（保证金/（保证金+委托金额））+ 佣金
        Incentives for creating node accounts, total incentives * (margin /(margin + commission amount)+commissions
        */
        double agentOwnWeight = new BigDecimal(self.getAgent().getDeposit()).divide(new BigDecimal(selfAllDeposit)).doubleValue();
        double inCaReward = DoubleUtils.mul(inBlockReword, agentOwnWeight);
        double outCaReward = DoubleUtils.mul(outBlockReword, agentOwnWeight);
        /*
        计算各委托账户获得的奖励金
        Calculate the rewards for each entrusted account
        */
        for (Deposit deposit : self.getDepositList()) {
            /*
            计算各委托账户权重（委托金额/总的委托金)
            Calculate the weight of each entrusted account (amount of entrusted account/total entrusted fee)
            */
            double weight = new BigDecimal(deposit.getDeposit()).divide (new BigDecimal(selfAllDeposit)).doubleValue();

            /*
            如果委托账户为创建该节点账户自己,则将节点账户奖励金加上该共识奖励金
            If the delegated account creates the node account itself, the node account reward is added to the consensus reward.
            */
            if (Arrays.equals(deposit.getAddress(), self.getAgent().getAgentAddress())) {
                inCaReward = inCaReward + DoubleUtils.mul(inBlockReword, weight);
                outCaReward = outCaReward + DoubleUtils.mul(outBlockReword, weight);
            }
            /*
            如果委托账户不是创建节点账户，则该账户获得实际奖励金 = 奖励金 - 佣金，节点账户奖励金需加上佣金
            If the entrusted account is not the creation of a node account,
            the account receives an actual bonus = bonus - commission, which is added to the nodal account bonus.
            */
            else {
                /*
                委托账户获得的奖励金
                Reward for entrusted account
                */
                double inReward = DoubleUtils.mul(inBlockReword, weight);
                double outReward = DoubleUtils.mul(outBlockReword, weight);

                /*
                佣金计算
                Commission Calculation
                */
                double inFee = DoubleUtils.mul(inReward, commissionRate);
                double outFee = DoubleUtils.mul(outReward, commissionRate);
                inCaReward = inCaReward + inFee;
                outCaReward = outCaReward + outFee;

                /*
                委托账户实际获得的奖励金 = 奖励金 - 佣金
                Actual bonus for entrusted account = bonus - Commission
                */
                double inHisReward = DoubleUtils.sub(inReward, inFee);
                double outHisReward = DoubleUtils.sub(outReward, outFee);
                if (inHisReward == 0D && outHisReward == 0D) {
                    continue;
                }
                long inDepositReward = DoubleUtils.longValue(inHisReward);
                long outDepositReward = DoubleUtils.longValue(outHisReward);
                if(inDepositReward != 0){
                    CoinTo inRewardCoin = null;
                    for (CoinTo coin : inRewardList) {
                        if (Arrays.equals(coin.getAddress(), deposit.getAddress())) {
                            inRewardCoin = coin;
                            break;
                        }
                    }
                    if(inRewardCoin == null){
                        inRewardCoin = new CoinTo(deposit.getAddress(),chainId,assetsId, BigInteger.valueOf(inDepositReward), unlockHeight);
                        inRewardList.add(inRewardCoin);
                    }else{
                        inRewardCoin.setAmount(inRewardCoin.getAmount().add(BigInteger.valueOf(inDepositReward)));
                    }
                }
                if(outDepositReward != 0){
                    CoinTo outRewardCoin = null;
                    for (CoinTo coin : outRewardList) {
                        if (Arrays.equals(coin.getAddress(), deposit.getAddress())) {
                            outRewardCoin = coin;
                            break;
                        }
                    }
                    if(outRewardCoin == null){
                        outRewardCoin = new CoinTo(deposit.getAddress(),ConsensusConstant.MAIN_CHAIN_ID,ConsensusConstant.MAIN_ASSETS_ID, BigInteger.valueOf(outDepositReward), unlockHeight);
                        outRewardList.add(outRewardCoin);
                    }else{
                        outRewardCoin.setAmount(outRewardCoin.getAmount().add(BigInteger.valueOf(outDepositReward)));
                    }
                }
            }
        }
        inRewardList.addAll(outRewardList);
        inRewardList.sort(new Comparator<CoinTo>() {
            @Override
            public int compare(CoinTo o1, CoinTo o2) {
                return Arrays.hashCode(o1.getAddress()) > Arrays.hashCode(o2.getAddress()) ? 1 : -1;
            }
        });
        if(DoubleUtils.compare(inCaReward,BigDecimal.ZERO.doubleValue())>0){
            CoinTo inAgentReward = new CoinTo(self.getAgent().getRewardAddress(),chainId,assetsId, BigInteger.valueOf(DoubleUtils.longValue(inCaReward)), unlockHeight);
            inRewardList.add(0,inAgentReward);
        }
        if(DoubleUtils.compare(outCaReward,BigDecimal.ZERO.doubleValue())>0){
            CoinTo outAgentReward = new CoinTo(self.getAgent().getRewardAddress(),ConsensusConstant.MAIN_CHAIN_ID,ConsensusConstant.MAIN_ASSETS_ID, BigInteger.valueOf(DoubleUtils.longValue(outCaReward)), unlockHeight);
            inRewardList.add(0,outAgentReward);
        }
        return inRewardList;
    }


    /**
     * 创建区块
     * create block
     *
     * @param chain        chain info
     * @param blockData       block data/区块数据
     * @param packingAddress  packing address/打包地址
     * @return Block
     */
    public Block createBlock(Chain chain,BlockData blockData, byte[] packingAddress){
        try {
            CallMethodUtils.accountValid(chain.getConfig().getChainId(),AddressTool.getStringAddressByBytes(packingAddress),null);
        }catch (NulsException e){
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e);
            return null;
        }
        Block block = new Block();
        block.setTxs(blockData.getTxList());
        BlockHeader header = new BlockHeader();
        block.setHeader(header);
        try {
            header.setExtend(blockData.getExtendsData().serialize());
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e.getMessage());
            throw new NulsRuntimeException(e);
        }
        header.setHeight(blockData.getHeight());
        header.setTime(blockData.getTime());
        header.setPreHash(blockData.getPreHash());
        header.setTxCount(blockData.getTxList().size());
        List<NulsDigestData> txHashList = new ArrayList<>();
        for (int i = 0; i < blockData.getTxList().size(); i++) {
            Transaction tx = blockData.getTxList().get(i);
            tx.setBlockHeight(header.getHeight());
            txHashList.add(tx.getHash());
        }
        header.setMerkleHash(NulsDigestData.calcMerkleDigestData(txHashList));
        header.setHash(NulsDigestData.calcDigestData(block.getHeader()));
        try {
            CallMethodUtils.blockSignature(chain.getConfig().getChainId(),AddressTool.getStringAddressByBytes(packingAddress),header);
        }catch (NulsException e){
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e);
            return null;
        }
        return block;
    }

    /**
     * 计算交易手续费
     * Calculating transaction fees
     *
     * @param tx         transaction/交易
     * @param chain      chain info
     * @return  ChargeResultData
     * */
    private ChargeResultData getFee(Transaction tx,Chain chain)throws NulsException{
        CoinData coinData = new CoinData();
        int chainId = chain.getConfig().getChainId();
        coinData.parse(tx.getCoinData(),0);
        /*
        跨链交易计算手续费
        Cross-Chain Transactions Calculate Processing Fees
        */
        if(tx.getType() == ConsensusConstant.TX_TYPE_CROSS_CHAIN){
            BigInteger fromAmount = BigInteger.ZERO;
            BigInteger toAmount = BigInteger.ZERO;
            /*
            计算链内手续费，from中链内主资产 - to中链内主资产的和
            Calculate in-chain handling fees, from in-chain main assets - to in-chain main assets and
            */
            if(AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress()) == chainId){
                for (CoinFrom from:coinData.getFrom()) {
                    if(from.getAssetsChainId() == chainId && from.getAssetsId() == chain.getConfig().getAssetsId()){
                        fromAmount = fromAmount.add(from.getAmount());
                    }
                }
                for (CoinTo to:coinData.getTo()) {
                    if(to.getAssetsChainId() == chainId && to.getAssetsId() == chain.getConfig().getAssetsId()){
                        toAmount = toAmount.add(to.getAmount());
                    }
                }
                return new ChargeResultData(fromAmount.subtract(toAmount),chainId);
            }
            /*
            计算主链和友链手续费,首先计算CoinData中总的跨链手续费，然后根据比例分跨链手续费
            Calculate the main chain and friendship chain handling fees, first calculate the total cross-chain handling fees in CoinData,
            and then divide the cross-chain handling fees according to the proportion.
            */
            for (CoinFrom from:coinData.getFrom()) {
                if(from.getAssetsChainId() == ConsensusConstant.MAIN_CHAIN_ID && from.getAssetsId() == ConsensusConstant.MAIN_ASSETS_ID){
                    fromAmount = fromAmount.add(from.getAmount());
                }
            }
            for (CoinTo to:coinData.getTo()) {
                if(to.getAssetsChainId() == ConsensusConstant.MAIN_CHAIN_ID  && to.getAssetsId() == ConsensusConstant.MAIN_ASSETS_ID){
                    toAmount = toAmount.add(to.getAmount());
                }
            }
            /*
            总的跨链手续费
            Total cross-chain handling fee
            */
            BigInteger fee = fromAmount.subtract(toAmount);

            /*
            如果当前链为主链,且跨链交易目标连为主链则主链收取全部跨链手续费，如果目标连为其他链则主链收取一定比例的跨链手续费
            If the current chain is the main chain and the target of cross-chain transaction is connected to the main chain, the main chain charges all cross-chain handling fees,
            and if the target is connected to other chains, the main chain charges a certain proportion of cross-chain handling fees.
            */
            if(chainId == ConsensusConstant.MAIN_CHAIN_ID){
                int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());
                if(toChainId == ConsensusConstant.MAIN_CHAIN_ID){
                    return new ChargeResultData(fee,ConsensusConstant.MAIN_CHAIN_ID);
                }
                return new ChargeResultData(fee.multiply(new BigInteger(String.valueOf(ConsensusConstant.MAIN_COMMISSION_RATIO))).divide(new BigInteger(String.valueOf(ConsensusConstant.MAIN_COMMISSION_RATIO))),ConsensusConstant.MAIN_CHAIN_ID);
            }
            return new ChargeResultData(fee.multiply(new BigInteger(String.valueOf(ConsensusConstant.MAIN_COMMISSION_RATIO))).divide(new BigInteger(String.valueOf(ConsensusConstant.MAIN_COMMISSION_RATIO-ConsensusConstant.MAIN_COMMISSION_RATIO))),ConsensusConstant.MAIN_CHAIN_ID);
        }
        /*
        链内交易手续费
        Processing fees for intra-chain transactions
        */
        return new ChargeResultData(coinData.getFee(),chainId);
    }
}
