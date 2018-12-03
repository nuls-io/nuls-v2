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
     *
     * @param chain_id 链ID
     * @param assetsId 资产ID
     * @param address  节点地址
     * @param lockTime 锁定的结束时间点(锁定开始时间点+锁定时长)，之前为锁定的时长
     */
    public static CoinData getStopAgentCoinData(int chain_id, int assetsId, byte[] address, long lockTime) throws IOException,NulsException {
        List<Agent> agentList = ConsensusManager.getInstance().getAllAgentMap().get(chain_id);
        for (Agent agent : agentList) {
            if (agent.getDelHeight() > 0) {
                continue;
            }
            if (Arrays.equals(address, agent.getAgentAddress())) {
                return getStopAgentCoinData(chain_id,assetsId, agent, lockTime);
            }
        }
        return null;
    }

    /**
     * 根据节点组装停止节点的coinData
     *
     * @param chain_id 链ID
     * @param assetsId 资产ID
     * @param agent    节点对象
     * @param lockTime 锁定的结束时间点(锁定开始时间点+锁定时长)，之前为锁定的时长
     */
    public static CoinData getStopAgentCoinData(int chain_id, int assetsId, Agent agent, long lockTime) throws NulsException, IOException {
        return getStopAgentCoinData(chain_id,assetsId, agent, lockTime, null);
    }

    /**
     * 组装节点CoinData锁定类型为时间或区块高度
     *
     * @param chain_id 链ID
     * @param assetsId 资产ID
     * @param agent    节点
     * @param lockTime 锁定时间
     * @param hight    锁定区块
     */
    public static CoinData getStopAgentCoinData(int chain_id, int assetsId, Agent agent, long lockTime, Long hight) throws NulsException{
        if (null == agent) {
            return null;
        }
        try {
            //创建节点交易
            //todo
            //充交易模块获取创建该节点时的交易
            NulsDigestData createTxHash = agent.getTxHash();
            Transaction createAgentTransaction = null;
            if (null == createAgentTransaction) {
                throw new NulsRuntimeException(ConsensusErrorCode.TX_NOT_EXIST);
            }
            CoinData coinData = new CoinData();
            List<CoinTo> toList = new ArrayList<>();
            List<CoinFrom> fromList = new ArrayList<>();
            toList.add(new CoinTo(agent.getAgentAddress(),chain_id,assetsId,agent.getDeposit(), lockTime));
            coinData.setTo(toList);
            //根据创建节点交易的CoinData中to组装 退出节点交易的from
            CoinData createCoinData = new CoinData();
            createCoinData.parse(createAgentTransaction.getCoinData(),0);
            for (CoinTo to:createCoinData.getTo()) {
                CoinFrom from = new CoinFrom(agent.getAgentAddress(),chain_id,assetsId);
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

            //获取该节点的委托信息
            List<Deposit> deposits = ConsensusManager.getInstance().getAllDepositMap().get(chain_id);
            List<String> addressList = new ArrayList<>();
            Map<String, CoinTo> toMap = new HashMap<>();
            long blockHeight = null == hight ? -1 : hight;
            //将委托该节点的委托金返回给委托账户
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
                    from = new CoinFrom(deposit.getAddress(),chain_id,assetsId,to.getAmount(),nonce,(byte)-1);
                    fromList.add(from);
                    break;
                }
                String address = AddressTool.getStringAddressByBytes(deposit.getAddress());
                CoinTo coinTo = toMap.get(address);
                if(coinTo == null){
                    coinTo = new CoinTo(deposit.getAddress(),chain_id,assetsId,deposit.getDeposit(),0);
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
    public static void addConsensusTx(int chain_id, Block bestBlock, List<Transaction> txList, MeetingMember self, MeetingRound round) throws NulsException, IOException {
        int assetsId = ConfigManager.config_map.get(chain_id).getAssetsId();
        Transaction coinBaseTransaction = createCoinBaseTx(chain_id,assetsId,self, txList, round, bestBlock.getHeader().getHeight() + 1 + ConfigManager.config_map.get(chain_id).getCoinbase_unlock_height());
        txList.add(0, coinBaseTransaction);
        punishTx(chain_id,assetsId, bestBlock, txList, self, round);
    }

    /**
     * 组装CoinBase交易
     *
     * @param member
     * @param txList
     * @param localRound
     * @param unlockHeight
     */
    public static Transaction createCoinBaseTx(int chain_id,int assetsId,MeetingMember member, List<Transaction> txList, MeetingRound localRound, long unlockHeight) throws IOException, NulsException {
        Transaction tx = new Transaction(ConsensusConstant.TX_TYPE_COINBASE);
        try {
            CoinData coinData = new CoinData();
            //计算共识奖励
            List<CoinTo> rewardList = calcReward(chain_id,assetsId,txList, member, localRound, unlockHeight);
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
     */
    private static List<CoinTo> calcReward(int chain_id,int assetsId,List<Transaction> txList, MeetingMember self, MeetingRound localRound, long unlockHeight) throws NulsException, IOException {
        //链内交易手续费(资产为链内主资产)
        BigInteger totalFee = BigInteger.ZERO;
        //跨链交易手续费(资产为主链主资产)
        BigInteger crossFee = BigInteger.ZERO;
        //计算手续费
        for (Transaction tx : txList) {
            CoinData coinData = new CoinData();
            coinData.parse(tx.getCoinData(), 0);
            ChargeResultData resultData = getFee(tx,chain_id);
            if(resultData.getChainId() == chain_id){
                totalFee = totalFee.add(resultData.getFee());
            }else{
                crossFee = crossFee.add(resultData.getFee());
            }
        }
        //链类奖励列表
        List<CoinTo> inRewardList = new ArrayList<>();
        //跨链交易奖励
        List<CoinTo> outRewardList = new ArrayList<>();
        //如果为种子节点，只领取交易手续费不计算共识奖励（种子节点保证金为0）
        if (BigIntegerUtils.isEqual(self.getAgent().getDeposit(), BigInteger.ZERO)) {
            if (!BigIntegerUtils.isEqual(totalFee,BigInteger.ZERO)) {
                CoinTo agentReword = new CoinTo(self.getAgent().getRewardAddress(),chain_id,assetsId,totalFee,unlockHeight);
                inRewardList.add(agentReword);
            }
            if(!BigIntegerUtils.isEqual(crossFee,BigInteger.ZERO)){
                CoinTo agentReword = new CoinTo(self.getAgent().getRewardAddress(),ConsensusConstant.MAIN_CHAIN_ID,ConsensusConstant.MAIN_ASSETS_ID,crossFee,unlockHeight);
                outRewardList.add(agentReword);
            }
            inRewardList.addAll(outRewardList);
            return inRewardList;
        }
        BigDecimal totalAll = DoubleUtils.mul(new BigDecimal(localRound.getMemberCount()), new BigDecimal(ConsensusConstant.BLOCK_REWARD));
        //佣金比例
        double commissionRate = DoubleUtils.div(self.getAgent().getCommissionRate(), 100, 2);
        //节点权重
        BigInteger selfAllDeposit = self.getAgent().getDeposit().add(self.getAgent().getTotalDeposit());
        BigDecimal agentWeight = DoubleUtils.mul(new BigDecimal(selfAllDeposit), self.getAgent().getCreditVal());
        //节点总的奖励金额（交易手续费+共识奖励）
        double inBlockReword = totalFee.doubleValue();
        double outBlockReword = crossFee.doubleValue();
        if (localRound.getTotalWeight() > 0d && agentWeight.doubleValue() > 0d) {
            //本节点共识奖励 = 节点权重/本轮次权重*共识基础奖励
            inBlockReword = DoubleUtils.sum(inBlockReword, DoubleUtils.mul(totalAll, DoubleUtils.div(agentWeight, localRound.getTotalWeight())).doubleValue());
            outBlockReword = DoubleUtils.sum(outBlockReword, DoubleUtils.mul(totalAll, DoubleUtils.div(agentWeight, localRound.getTotalWeight())).doubleValue());
        }
        if (inBlockReword == 0d && outBlockReword == 0d) {
            return inRewardList;
        }
        //创建节点账户所得奖励金，总的奖励金*（保证金/（保证金+委托金额））+ 佣金
        double agentOwnWeight = new BigDecimal(self.getAgent().getDeposit().divide(selfAllDeposit)).doubleValue();
        double inCaReward = DoubleUtils.mul(inBlockReword, agentOwnWeight);
        double outCaReward = DoubleUtils.mul(outBlockReword, agentOwnWeight);
        //计算各委托账户获得的奖励金
        for (Deposit deposit : self.getDepositList()) {
            //计算各委托账户权重（委托金额/总的委托金）
            double weight = new BigDecimal(deposit.getDeposit().divide(selfAllDeposit)).doubleValue();
            if (Arrays.equals(deposit.getAddress(), self.getAgent().getAgentAddress())) {
                inCaReward = inCaReward + DoubleUtils.mul(inBlockReword, weight);
                outCaReward = outCaReward + DoubleUtils.mul(outBlockReword, weight);
            } else {
                //委托账户获得的奖励金
                double inReward = DoubleUtils.mul(inBlockReword, weight);
                double outReward = DoubleUtils.mul(outBlockReword, weight);
                double inFee = DoubleUtils.mul(inReward, commissionRate);
                double outFee = DoubleUtils.mul(outReward, commissionRate);
                inCaReward = inCaReward + inFee;
                outCaReward = outCaReward + outFee;
                //委托账户实际获得的奖励金 = 奖励金 - 佣金
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
                        inRewardCoin = new CoinTo(deposit.getAddress(),chain_id,assetsId, BigInteger.valueOf(inDepositReward), unlockHeight);
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
            CoinTo inAgentReward = new CoinTo(self.getAgent().getRewardAddress(),chain_id,assetsId, BigInteger.valueOf(DoubleUtils.longValue(inCaReward)), unlockHeight);
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
     *
     * @param bestBlock 本地最新区块
     * @param txList    需打包的交易列表
     * @param self      本地节点打包信息
     * @param round     本地最新轮次信息
     */
    public static void punishTx(int chain_id,int assetsId, Block bestBlock, List<Transaction> txList, MeetingMember self, MeetingRound round) throws NulsException, IOException {
        Transaction yellowPunishTransaction = createYellowPunishTx(bestBlock, self, round);
        if (null == yellowPunishTransaction) {
            return;
        }
        txList.add(yellowPunishTransaction);
        //当连续100个黄牌时，给出一个红牌
        //When 100 yellow CARDS in a row, give a red card.
        YellowPunishData yellowPunishData = new YellowPunishData();
        yellowPunishData.parse(yellowPunishTransaction.getTxData(),0);
        List<byte[]> addressList = yellowPunishData.getAddressList();
        Set<Integer> punishedSet = new HashSet<>();
        for (byte[] address : addressList) {
            MeetingMember member = round.getMemberByAgentAddress(address);
            if (null == member) {
                member = round.getPreRound().getMemberByAgentAddress(address);
            }
            //如果节点信誉值小于等于临界值时，生成红牌交易
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
                CoinData coinData = getStopAgentCoinData(chain_id,assetsId, redPunishData.getAddress(), redPunishTransaction.getTime() + ConfigManager.config_map.get(chain_id).getRedPublish_lockTime());
                redPunishTransaction.setCoinData(coinData.serialize());
                redPunishTransaction.setHash(NulsDigestData.calcDigestData(redPunishTransaction.serializeForHash()));
                txList.add(redPunishTransaction);
            }
        }
    }

    /**
     * 组装黄牌
     *
     * @param preBlock 本地最新区块
     * @param self     当前节点的打包信息
     * @param round    本地最新轮次信息
     */
    public static Transaction createYellowPunishTx(Block preBlock, MeetingMember self, MeetingRound round) throws NulsException, IOException {
        BlockExtendsData preBlockRoundData = new BlockExtendsData(preBlock.getHeader().getExtend());
        //如果本节点当前打包轮次比本地最新区块的轮次大一轮以上则返回不生成黄牌交易
        if (self.getRoundIndex() - preBlockRoundData.getRoundIndex() > 1) {
            return null;
        }
        //计算需要生成的黄牌数量，即当前出的块与本地最新区块之间相差的区块数
        int yellowCount = 0;
        //如果当前轮次与本地最新区块是统一轮次，则当前节点在本轮次中的出块下标与最新区块之间的差值减一即为需要生成的光拍交易数
        if (self.getRoundIndex() == preBlockRoundData.getRoundIndex() && self.getPackingIndexOfRound() != preBlockRoundData.getPackingIndexOfRound() + 1) {
            yellowCount = self.getPackingIndexOfRound() - preBlockRoundData.getPackingIndexOfRound() - 1;
        }
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
            //本轮次需生成的黄牌
            if (index > 0) {
                member = round.getMember(index);
                if (member.getAgent() == null || member.getAgent().getDelHeight() > 0) {
                    continue;
                }
                addressList.add(member.getAgent().getAgentAddress());
            }
            //上一轮需要生成的黄牌
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
     * @param blockData       区块数据
     * @param packingAddress  打包地址
     */
    public static Block createBlock(BlockData blockData, byte[] packingAddress) throws NulsException {
        //todo
        //从账户管理模块验证
        //打包地址账户是否存在
        //判断打包账户是否为加密账户
        //获取账户公钥用于签名账户
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
     * @param tx       交易
     * @param chainId  链ID
     * */
    public static ChargeResultData getFee(Transaction tx,int chainId)throws NulsException{
        CoinData coinData = new CoinData();
        coinData.parse(tx.getCoinData(),0);
        //如果为跨链交易
        if(tx.getType() == ConsensusConstant.TX_TYPE_CROSS_CHAIN){
            BigInteger fromAmount = BigInteger.ZERO;
            BigInteger toAmount = BigInteger.ZERO;
            //计算链内手续费，from中链内主资产 - to中链内主资产的和
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
            //计算主链和跨链手续费,首先找到CoinData中总的跨链手续费
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
            //跨链手续费
            BigInteger fee = fromAmount.subtract(toAmount);
            //如果当前链为主链,且跨链交易目标连为主链则主链收取全部跨链手续费，如果目标连为其他链则主链收取一定比例的跨链手续费
            if(chainId == ConsensusConstant.MAIN_CHAIN_ID){
                int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());
                if(toChainId == ConsensusConstant.MAIN_CHAIN_ID){
                    return new ChargeResultData(fee,ConsensusConstant.MAIN_CHAIN_ID);
                }
                return new ChargeResultData(fee.multiply(new BigInteger(String.valueOf(ConsensusConstant.MAIN_COMMISSION_RATIO))).divide(new BigInteger(String.valueOf(ConsensusConstant.MAIN_COMMISSION_RATIO))),ConsensusConstant.MAIN_CHAIN_ID);
            }
            return new ChargeResultData(fee.multiply(new BigInteger(String.valueOf(ConsensusConstant.MAIN_COMMISSION_RATIO))).divide(new BigInteger(String.valueOf(ConsensusConstant.MAIN_COMMISSION_RATIO-ConsensusConstant.MAIN_COMMISSION_RATIO))),ConsensusConstant.MAIN_CHAIN_ID);
        }
        return new ChargeResultData(coinData.getFee(),chainId);
    }
}
