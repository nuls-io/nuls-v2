package io.nuls.poc.utils.util;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.signture.BlockSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.BlockData;
import io.nuls.poc.model.bo.ChargeResultData;
import io.nuls.poc.model.bo.consensus.PunishReasonEnum;
import io.nuls.poc.model.bo.round.MeetingMember;
import io.nuls.poc.model.bo.round.MeetingRound;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.model.bo.tx.txdata.RedPunishData;
import io.nuls.poc.model.bo.tx.txdata.YellowPunishData;
import io.nuls.poc.utils.manager.ConfigManager;
import io.nuls.poc.utils.manager.ConsensusManager;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.DoubleUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * @author tag
 * 2018/11/19
 */
public class ConsensusUtil {
    /**
     * 根据节点地址组装停止节点的coinData
     * Assemble coinData of stop node according to node address
     *
     * @param chainId   chain id/链ID
     * @param assetsId   assets id/资产ID
     * @param address    agent address/节点地址
     * @param lockTime   The end point of the lock (lock start time + lock time) is the length of the lock before./锁定的结束时间点(锁定开始时间点+锁定时长)，之前为锁定的时长
     * @return  CoinData
     */
    public static CoinData getStopAgentCoinData(int chainId, int assetsId, byte[] address, long lockTime) throws IOException,NulsException {
        List<Agent> agentList = ConsensusManager.getInstance().getAllAgentMap().get(chainId);
        for (Agent agent : agentList) {
            if (agent.getDelHeight() > 0) {
                continue;
            }
            if (Arrays.equals(address, agent.getAgentAddress())) {
                return getStopAgentCoinData(chainId,assetsId, agent, lockTime);
            }
        }
        return null;
    }

    /**
     * 根据节点组装停止节点的coinData
     * Assemble the coinData of the stop node according to the node
     *
     * @param chainId   chain id/链ID
     * @param assetsId   assets id/资产ID
     * @param agent      agent info/节点对象
     * @param lockTime   The end point of the lock (lock start time + lock time) is the length of the lock before./锁定的结束时间点(锁定开始时间点+锁定时长)，之前为锁定的时长
     * @return CoinData
     */
    public static CoinData getStopAgentCoinData(int chainId, int assetsId, Agent agent, long lockTime) throws NulsException {
        return getStopAgentCoinData(chainId,assetsId, agent, lockTime, null);
    }

    /**
     * 组装节点CoinData锁定类型为时间或区块高度
     * Assembly node CoinData lock type is time or block height
     *
     * @param chainId    chain id/链ID
     * @param assetsId    assets id/资产ID
     * @param agent       agent info/节点
     * @param lockTime    lock time/锁定时间
     * @param height      lock block height/锁定区块
     * @return CoinData
     */
    public static CoinData getStopAgentCoinData(int chainId, int assetsId, Agent agent, long lockTime, Long height) throws NulsException{
        if (null == agent) {
            return null;
        }
        try {
            //todo 充交易模块获取创建该节点时的交易
            NulsDigestData createTxHash = agent.getTxHash();
            Transaction createAgentTransaction = null;
            if (null == createAgentTransaction) {
                throw new NulsRuntimeException(ConsensusErrorCode.TX_NOT_EXIST);
            }
            CoinData coinData = new CoinData();
            List<CoinTo> toList = new ArrayList<>();
            List<CoinFrom> fromList = new ArrayList<>();
            toList.add(new CoinTo(agent.getAgentAddress(),chainId,assetsId,agent.getDeposit(), lockTime));
            coinData.setTo(toList);

            /*
            根据创建节点交易的CoinData中的输出 组装退出节点交易的输入
            Assemble the input to exit the node transaction based on the output in CoinData that creates the node transaction
            */
            CoinData createCoinData = new CoinData();
            createCoinData.parse(createAgentTransaction.getCoinData(),0);
            for (CoinTo to:createCoinData.getTo()) {
                CoinFrom from = new CoinFrom(agent.getAgentAddress(),chainId,assetsId);
                if(to.getAmount().compareTo(agent.getTotalDeposit()) == 0 && to.getLockTime() == -1L){
                    from.setAmount(to.getAmount());
                    from.setLocked((byte)-1);
                    from.setNonce(createTxHash.getDigestBytes());
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
            List<Deposit> deposits = ConsensusManager.getInstance().getAllDepositMap().get(chainId);
            List<String> addressList = new ArrayList<>();
            Map<String, CoinTo> toMap = new HashMap<>();
            long blockHeight = null == height ? -1 : height;
            for (Deposit deposit : deposits) {
                if (deposit.getDelHeight() > 0 && (blockHeight <= 0 || deposit.getDelHeight() < blockHeight)) {
                    continue;
                }
                if (!deposit.getAgentHash().equals(agent.getTxHash())) {
                    continue;
                }
                //todo  从交易管理模块获取委托交易 deposit.getTxHash()
                Transaction depositTransaction = null;
                CoinData depositCoinData = new CoinData();
                depositCoinData.parse(depositTransaction.getCoinData(), 0);
                CoinFrom from = null;
                for (CoinTo to:depositCoinData.getTo()) {
                    if (!BigIntegerUtils.isEqual(to.getAmount(),deposit.getDeposit()) || to.getLockTime() != -1L) {
                        continue;
                    }
                    byte[] nonce = deposit.getTxHash().getDigestBytes();
                    from = new CoinFrom(deposit.getAddress(),chainId,assetsId,to.getAmount(),nonce,(byte)-1);
                    fromList.add(from);
                    break;
                }
                String address = AddressTool.getStringAddressByBytes(deposit.getAddress());
                CoinTo coinTo = toMap.get(address);
                if(coinTo == null){
                    coinTo = new CoinTo(deposit.getAddress(),chainId,assetsId,deposit.getDeposit(),0);
                    toMap.put(address,coinTo);
                    addressList.add(address);
                }else{
                    coinTo.setAmount(coinTo.getAmount().add(deposit.getDeposit()));
                }
            }
            for (String address : addressList) {
                coinData.getTo().add(toMap.get(address));
            }
            return coinData;
        } catch (NulsException e) {
            Log.error(e);
            throw e;
        }
    }

    /**
     * CoinBase transaction & Punish transaction
     *
     * @param bestBlock local highest block/本地最新区块
     * @param txList    all tx of block/需打包的交易列表
     * @param self      agent meeting data/节点打包信息
     * @param round     latest local round/本地最新轮次信息
     */
    public static void addConsensusTx(int chainId, Block bestBlock, List<Transaction> txList, MeetingMember self, MeetingRound round) throws NulsException, IOException {
        int assetsId = ConfigManager.config_map.get(chainId).getAssetsId();
        Transaction coinBaseTransaction = createCoinBaseTx(chainId,assetsId,self, txList, round, bestBlock.getHeader().getHeight() + 1 + ConfigManager.config_map.get(chainId).getCoinbaseUnlockHeight());
        txList.add(0, coinBaseTransaction);
        punishTx(chainId,assetsId, bestBlock, txList, self, round);
    }

    /**
     * 组装CoinBase交易
     * Assembling CoinBase transactions
     *
     * @param chainId      链ID/chain id
     * @param assetsId      资产ID/assets id
     * @param member        打包信息/packing info
     * @param txList        交易列表/transaction list
     * @param localRound    本地最新轮次/local newest round info
     * @param unlockHeight  解锁高度/unlock height
     * @return Transaction
     */
    public static Transaction createCoinBaseTx(int chainId,int assetsId,MeetingMember member, List<Transaction> txList, MeetingRound localRound, long unlockHeight) throws IOException, NulsException {
        Transaction tx = new Transaction(ConsensusConstant.TX_TYPE_COINBASE);
        try {
            CoinData coinData = new CoinData();
            /*
            计算共识奖励
            Calculating consensus Awards
            */
            List<CoinTo> rewardList = calcReward(chainId,assetsId,txList, member, localRound, unlockHeight);
            for (CoinTo coin : rewardList) {
                coinData.addTo(coin);
            }
            tx.setTime(member.getPackEndTime());
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
            tx.setCoinData(coinData.serialize());
        } catch (IOException e) {
            Log.error(e);
        }
        return tx;
    }

    /**
     * 计算共识奖励
     * Calculating consensus Awards
     *
     * @param chainId     链ID/chain id
     * @param txList       交易列表/transaction list
     * @param self         本地打包信息/local agent packing info
     * @param localRound   本地最新轮次/local newest round info
     * @param unlockHeight 解锁高度/unlock height
     * @return List<CoinTo>
     */
    private static List<CoinTo> calcReward(int chainId,int assetsId,List<Transaction> txList, MeetingMember self, MeetingRound localRound, long unlockHeight) throws NulsException, IOException {
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
            ChargeResultData resultData = getFee(tx,chainId);
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
        if (localRound.getTotalWeight() > 0d && agentWeight.doubleValue() > 0d) {
            /*
            本节点共识奖励 = 节点权重/本轮次权重*共识基础奖励
            Node Consensus Award = Node Weight/Round Weight*Consensus Foundation Award
            */
            inBlockReword = DoubleUtils.sum(inBlockReword, DoubleUtils.mul(totalAll, DoubleUtils.div(agentWeight, localRound.getTotalWeight())).doubleValue());
            outBlockReword = DoubleUtils.sum(outBlockReword, DoubleUtils.mul(totalAll, DoubleUtils.div(agentWeight, localRound.getTotalWeight())).doubleValue());
        }
        if (inBlockReword == 0d && outBlockReword == 0d) {
            return inRewardList;
        }
        /*
        创建节点账户所得共识奖励金，总的奖励金*（保证金/（保证金+委托金额））+ 佣金
        Incentives for creating node accounts, total incentives * (margin /(margin + commission amount)+commissions
        */
        double agentOwnWeight = new BigDecimal(self.getAgent().getDeposit().divide(selfAllDeposit)).doubleValue();
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
            double weight = new BigDecimal(deposit.getDeposit().divide(selfAllDeposit)).doubleValue();

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
     * 组装红/黄牌交易
     * Assemble Red/Yellow Transaction
     *
     * @param bestBlock  Latest local block/本地最新区块
     * @param txList     A list of transactions to be packaged/需打包的交易列表
     * @param self       Local Node Packing Information/本地节点打包信息
     * @param round      Local latest rounds information/本地最新轮次信息
     */
    public static void punishTx(int chainId,int assetsId, Block bestBlock, List<Transaction> txList, MeetingMember self, MeetingRound round) throws NulsException, IOException {
        Transaction yellowPunishTransaction = createYellowPunishTx(bestBlock, self, round);
        if (null == yellowPunishTransaction) {
            return;
        }
        txList.add(yellowPunishTransaction);
        /*
        当连续100个黄牌时，给出一个红牌
        When 100 yellow CARDS in a row, give a red card.
        */
        YellowPunishData yellowPunishData = new YellowPunishData();
        yellowPunishData.parse(yellowPunishTransaction.getTxData(),0);
        List<byte[]> addressList = yellowPunishData.getAddressList();
        Set<Integer> punishedSet = new HashSet<>();
        for (byte[] address : addressList) {
            MeetingMember member = round.getMemberByAgentAddress(address);
            if (null == member) {
                member = round.getPreRound().getMemberByAgentAddress(address);
            }
            if (DoubleUtils.compare(member.getAgent().getCreditVal(), ConsensusConstant.RED_PUNISH_CREDIT_VAL) == -1) {
                if (!punishedSet.add(member.getPackingIndexOfRound())) {
                    continue;
                }
                if (member.getAgent().getDelHeight() > 0L) {
                    continue;
                }
                Transaction redPunishTransaction = new Transaction(ConsensusConstant.TX_TYPE_RED_PUNISH);
                RedPunishData redPunishData = new RedPunishData();
                redPunishData.setAddress(address);
                redPunishData.setReasonCode(PunishReasonEnum.TOO_MUCH_YELLOW_PUNISH.getCode());
                redPunishTransaction.setTxData(redPunishData.serialize());
                redPunishTransaction.setTime(self.getPackEndTime());
                CoinData coinData = getStopAgentCoinData(chainId,assetsId, redPunishData.getAddress(), redPunishTransaction.getTime() + ConfigManager.config_map.get(chainId).getRedPublishLockTime());
                redPunishTransaction.setCoinData(coinData.serialize());
                redPunishTransaction.setHash(NulsDigestData.calcDigestData(redPunishTransaction.serializeForHash()));
                txList.add(redPunishTransaction);
            }
        }
    }

    /**
     * 组装黄牌
     * Assemble Yellow Transaction
     *
     * @param preBlock  Latest local block/本地最新区块
     * @param self      A list of transactions to be packaged/需打包的交易列表
     * @param round     Local latest rounds information/本地最新轮次信息
     * @return  Transaction
     */
    public static Transaction createYellowPunishTx(Block preBlock, MeetingMember self, MeetingRound round) throws NulsException, IOException {
        BlockExtendsData preBlockRoundData = new BlockExtendsData(preBlock.getHeader().getExtend());
        /*
        如果本节点当前打包轮次比本地最新区块的轮次大一轮以上则返回不生成黄牌交易
        If the current packing rounds of this node are more than one round larger than the rounds of the latest local block,
        no yellow card transaction will be generated.
        */
        if (self.getRoundIndex() - preBlockRoundData.getRoundIndex() > 1) {
            return null;
        }
        /*
        计算需要生成的黄牌数量，即当前出的块与本地最新区块之间相差的区块数
        Calculate the number of yellow cards that need to be generated, that is, the number of blocks that differ from the latest local block
        */
        int yellowCount = 0;

        /*
        如果当前轮次与本地最新区块是同一轮次，则当前节点在本轮次中的出块下标与最新区块之间的差值减一即为需要生成的光拍交易数
        If the current round is the same as the latest local block, then the difference between the block subscript of the current node and the latest block in this round is reduced by one,
        that is, the number of optical beat transactions that need to be generated.
        */
        if (self.getRoundIndex() == preBlockRoundData.getRoundIndex() && self.getPackingIndexOfRound() != preBlockRoundData.getPackingIndexOfRound() + 1) {
            yellowCount = self.getPackingIndexOfRound() - preBlockRoundData.getPackingIndexOfRound() - 1;
        }

        /*
        如果当前轮次与本地最新区块不是同一轮次，且当前节点不是本轮次中第一个出块的或则本地最新区块不为它所在轮次中最后一个出块的
        则黄牌数为：上一轮次出块数-本地最新区块出块下标+当前节点出块下标-1
        If the current round is not the same as the latest local block, and the current node is not the first block in this round, or the latest local block is not the last block in its round.
        The yellow card number is: the number of blocks out in the last round - local latest block out subscript + current node out block subscript - 1
        */
        if (self.getRoundIndex() != preBlockRoundData.getRoundIndex() && (self.getPackingIndexOfRound() != 1 || preBlockRoundData.getPackingIndexOfRound() != preBlockRoundData.getConsensusMemberCount())) {
            yellowCount = self.getPackingIndexOfRound() + preBlockRoundData.getConsensusMemberCount() - preBlockRoundData.getPackingIndexOfRound() - 1;
        }
        if (yellowCount == 0) {
            return null;
        }
        List<byte[]> addressList = new ArrayList<>();
        MeetingMember member = null;
        MeetingRound preRound = null;
        for (int i = 1; i <= yellowCount; i++) {
            int index = self.getPackingIndexOfRound() - i;
            /*
            本轮次需生成的黄牌
            Yellow cards to be generated in this round
            */
            if (index > 0) {
                member = round.getMember(index);
                if (member.getAgent() == null || member.getAgent().getDelHeight() > 0) {
                    continue;
                }
                addressList.add(member.getAgent().getAgentAddress());
            }
            /*
            上一轮需要生成的黄牌
            Yellow cards needed to be generated in the last round
            */
            else {
                preRound = round.getPreRound();
                member = preRound.getMember(index + preRound.getMemberCount());
                if (member.getAgent() == null || member.getAgent().getDelHeight() > 0) {
                    continue;
                }
                addressList.add(member.getAgent().getAgentAddress());
            }
        }
        if (addressList.isEmpty()) {
            return null;
        }
        Transaction punishTx = new Transaction(ConsensusConstant.TX_TYPE_YELLOW_PUNISH);
        YellowPunishData data = new YellowPunishData();
        data.setAddressList(addressList);
        punishTx.setTxData(data.serialize());
        punishTx.setTime(self.getPackEndTime());
        punishTx.setHash(NulsDigestData.calcDigestData(punishTx.serializeForHash()));
        return punishTx;
    }


    /**
     * 创建区块
     * create block
     *
     * @param blockData       block data/区块数据
     * @param packingAddress  packing address/打包地址
     * @return Block
     */
    public static Block createBlock(BlockData blockData, byte[] packingAddress){
        //todo 调账户模块接口验证账户正确性+获取账户EcKey
        ECKey eckey = new ECKey();
        Block block = new Block();
        block.setTxs(blockData.getTxList());
        BlockHeader header = new BlockHeader();
        block.setHeader(header);
        try {
            block.getHeader().setExtend(blockData.getExtendsData().serialize());
        } catch (IOException e) {
            Log.error(e);
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
        BlockSignature scriptSig = new BlockSignature();
        NulsSignData signData = SignatureUtil.signDigest(header.getHash().getDigestBytes(), eckey);
        scriptSig.setSignData(signData);
        scriptSig.setPublicKey(eckey.getPubKey());
        header.setBlockSignature(scriptSig);
        return block;
    }

    /**
     * 计算交易手续费
     * Calculating transaction fees
     *
     * @param tx         transaction/交易
     * @param chainId    chain id/链ID
     * @return  ChargeResultData
     * */
    public static ChargeResultData getFee(Transaction tx,int chainId)throws NulsException{
        CoinData coinData = new CoinData();
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
                    if(from.getAssetsChainId() == chainId && from.getAssetsId() == ConfigManager.config_map.get(chainId).getAssetsId()){
                        fromAmount = fromAmount.add(from.getAmount());
                    }
                }
                for (CoinTo to:coinData.getTo()) {
                    if(to.getAssetsChainId() == chainId && to.getAssetsId() == ConfigManager.config_map.get(chainId).getAssetsId()){
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
