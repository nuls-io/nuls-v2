package io.nuls.poc.utils.util;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.consensus.PunishReasonEnum;
import io.nuls.poc.model.bo.round.MeetingMember;
import io.nuls.poc.model.bo.round.MeetingRound;
import io.nuls.poc.model.bo.tx.*;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.model.bo.tx.txdata.RedPunishData;
import io.nuls.poc.model.bo.tx.txdata.YellowPunishData;
import io.nuls.poc.utils.manager.ConfigManager;
import io.nuls.poc.utils.manager.ConsensusManager;
import io.nuls.tools.basic.VarInt;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.data.DoubleUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

import java.io.IOException;
import java.util.*;
/**
 * @author  tag
 * 2018/11/19
 * */
public class ConsensusUtil {
    /**
     * 根据节点地址组装停止节点的coinData
     * @param chain_id 链ID
     * @param address  节点地址
     * @param lockTime 锁定的结束时间点(锁定开始时间点+锁定时长)，之前为锁定的时长
     */
    public static CoinData getStopAgentCoinData(int chain_id, byte[] address, long lockTime) throws IOException {
        List<Agent> agentList = ConsensusManager.getInstance().getAllAgentMap().get(chain_id);
        for (Agent agent : agentList) {
            if (agent.getDelHeight() > 0) {
                continue;
            }
            if (Arrays.equals(address, agent.getAgentAddress())) {
                return getStopAgentCoinData(chain_id, agent, lockTime);
            }
        }
        return null;
    }

    /**
     * 根据节点组装停止节点的coinData
     * @param chain_id 链ID
     * @param agent    节点对象
     * @param lockTime 锁定的结束时间点(锁定开始时间点+锁定时长)，之前为锁定的时长
     */
    public static CoinData getStopAgentCoinData(int chain_id, Agent agent, long lockTime) throws IOException {
        return getStopAgentCoinData(chain_id, agent, lockTime, null);
    }

    /**
     * 组装节点CoinData锁定类型为时间或区块高度
     * @param chain_id  链ID
     * @param agent     节点
     * @param lockTime  锁定时间
     * @param hight     锁定区块
     * */
    public static CoinData getStopAgentCoinData(int chain_id, Agent agent, long lockTime, Long hight) throws IOException {
        if (null == agent) {
            return null;
        }
        NulsDigestData createTxHash = agent.getTxHash();
        CoinData coinData = new CoinData();
        List<Coin> toList = new ArrayList<>();
        toList.add(new Coin(agent.getAgentAddress(), agent.getDeposit(), lockTime));
        coinData.setTo(toList);
        //todo
        //充交易模块获取创建该节点时的交易
        CreateAgentTransaction transaction = null;
        if (null == transaction) {
            throw new NulsRuntimeException(ConsensusErrorCode.TX_NOT_EXIST);
        }
        List<Coin> fromList = new ArrayList<>();
        for (int index = 0; index < transaction.getCoinData().getTo().size(); index++) {
            Coin coin = transaction.getCoinData().getTo().get(index);
            if (coin.getNa().equals(agent.getDeposit()) && coin.getLockTime() == -1L) {
                coin.setOwner(ByteUtils.concatenate(transaction.getHash().serialize(), new VarInt(index).encode()));
                fromList.add(coin);
                break;
            }
        }
        if (fromList.isEmpty()) {
            throw new NulsRuntimeException(ConsensusErrorCode.DATA_ERROR);
        }
        coinData.setFrom(fromList);
        List<Deposit> deposits = ConsensusManager.getInstance().getAllDepositMap().get(chain_id);
        List<String> addressList = new ArrayList<>();
        Map<String, Coin> toMap = new HashMap<>();
        long blockHeight = null == hight ? -1 : hight;
        for (Deposit deposit : deposits) {
            if (deposit.getDelHeight() > 0 && (blockHeight <= 0 || deposit.getDelHeight() < blockHeight)) {
                continue;
            }
            if (!deposit.getAgentHash().equals(agent.getTxHash())) {
                continue;
            }
            //todo
            //从交易模块获取获取指定的委托交易
            DepositTransaction dtx = null;
            Coin fromCoin = null;
            for (Coin coin : dtx.getCoinData().getTo()) {
                if (!coin.getNa().equals(deposit.getDeposit()) || coin.getLockTime() != -1L) {
                    continue;
                }
                fromCoin = new Coin(ByteUtils.concatenate(dtx.getHash().serialize(), new VarInt(0).encode()), coin.getNa(), coin.getLockTime());
                fromCoin.setLockTime(-1L);
                fromList.add(fromCoin);
                break;
            }
            String address = AddressTool.getStringAddressByBytes(deposit.getAddress());
            Coin coin = toMap.get(address);
            if (null == coin) {
                coin = new Coin(deposit.getAddress(), deposit.getDeposit(), 0);
                addressList.add(address);
                toMap.put(address, coin);
            } else {
                coin.setNa(coin.getNa().add(fromCoin.getNa()));
            }
        }
        for (String address : addressList) {
            coinData.getTo().add(toMap.get(address));
        }
        return coinData;
    }

    /**
     * CoinBase transaction & Punish transaction
     *
     * @param bestBlock local highest block/本地最新区块
     * @param txList    all tx of block/需打包的交易列表
     * @param self      agent meeting data/节点打包信息
     * @param round     latest local round/本地最新轮次信息
     */
    private static void addConsensusTx(int chain_id ,Block bestBlock, List<Transaction> txList, MeetingMember self, MeetingRound round) throws NulsException, IOException {
        CoinBaseTransaction coinBaseTransaction = createCoinBaseTx(self, txList, round, bestBlock.getHeader().getHeight() + 1 + ConfigManager.config_map.get(chain_id).getCoinbase_unlock_height());
        txList.add(0, coinBaseTransaction);
        punishTx(chain_id,bestBlock, txList, self, round);
    }

    /**
     * 组装CoinBase交易
     * @param member
     * @param txList
     * @param localRound
     * @param unlockHeight
     * */
    public static CoinBaseTransaction createCoinBaseTx(MeetingMember member, List<Transaction> txList, MeetingRound localRound, long unlockHeight){
        CoinData coinData = new CoinData();
        //计算共识奖励
        List<Coin> rewardList = calcReward(txList, member, localRound, unlockHeight);
        for (Coin coin : rewardList) {
            coinData.addTo(coin);
        }
        CoinBaseTransaction tx = new CoinBaseTransaction();
        tx.setTime(member.getPackEndTime());
        tx.setCoinData(coinData);
        try {
            tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        } catch (IOException e) {
            Log.error(e);
        }
        return tx;
    }

    /**
     * 计算共识奖励
     * */
    private static List<Coin> calcReward(List<Transaction> txList, MeetingMember self, MeetingRound localRound, long unlockHeight){
        List<Coin> rewardList = new ArrayList<>();
        //如果为种子节点，只领取交易手续费不计算共识奖励（种子节点保证金为0）
        if (self.getAgent().getDeposit().getValue() == Na.ZERO.getValue()) {
            long totalFee = 0;
            for (Transaction tx : txList) {
                totalFee += tx.getFee().getValue();
            }
            if (totalFee == 0L) {
                return rewardList;
            }
            double caReward = totalFee;
            Coin agentReword = new Coin(self.getAgent().getRewardAddress(), Na.valueOf((long) caReward), unlockHeight);
            rewardList.add(agentReword);
            return rewardList;
        }
        //交易手续费
        long totalFee = 0;
        //计算手续费
        for (Transaction tx : txList) {
            totalFee += tx.getFee().getValue();
        }
        double totalAll = DoubleUtils.mul(localRound.getMemberCount(), ConsensusConstant.BLOCK_REWARD.getValue());
        //佣金比例
        double commissionRate = DoubleUtils.div(self.getAgent().getCommissionRate(), 100, 2);
        //节点权重
        double agentWeight = DoubleUtils.mul(DoubleUtils.sum(self.getAgent().getDeposit().getValue(), self.getAgent().getTotalDeposit().getValue()), self.getAgent().getCreditVal());
        //节点总的奖励金额（交易手续费+共识奖励）
        double blockReword = totalFee;
        if (localRound.getTotalWeight() > 0d && agentWeight > 0d) {
            //本节点共识奖励 = 节点权重/本轮次权重*共识基础奖励
            blockReword = DoubleUtils.sum(blockReword, DoubleUtils.mul(totalAll, DoubleUtils.div(agentWeight, localRound.getTotalWeight())));
        }
        if (blockReword == 0d) {
            return rewardList;
        }
        //节点总委托金额（创建节点保证金+总的委托金额）
        long realTotalAllDeposit = self.getAgent().getDeposit().getValue() + self.getAgent().getTotalDeposit().getValue();
        //创建节点账户所得奖励金，总的奖励金*（保证金/（保证金+委托金额））+ 佣金
        double caReward = DoubleUtils.mul(blockReword, DoubleUtils.div(self.getAgent().getDeposit().getValue(), realTotalAllDeposit));
        //计算各委托账户获得的奖励金
        for (Deposit deposit : self.getDepositList()) {
            //计算各委托账户权重（委托金额/总的委托金）
            double weight = DoubleUtils.div(deposit.getDeposit().getValue(), realTotalAllDeposit);
            if (Arrays.equals(deposit.getAddress(), self.getAgent().getAgentAddress())) {
                caReward = caReward + DoubleUtils.mul(blockReword, weight);
            } else {
                //委托账户获得的奖励金
                double reward = DoubleUtils.mul(blockReword, weight);
                double fee = DoubleUtils.mul(reward, commissionRate);
                caReward = caReward + fee;
                //委托账户实际获得的奖励金 = 奖励金 - 佣金
                double hisReward = DoubleUtils.sub(reward, fee);
                if (hisReward == 0D) {
                    continue;
                }
                Na depositReward = Na.valueOf(DoubleUtils.longValue(hisReward));
                Coin rewardCoin = null;
                for (Coin coin : rewardList) {
                    if (Arrays.equals(coin.getAddress(), deposit.getAddress())) {
                        rewardCoin = coin;
                        break;
                    }
                }
                if (rewardCoin == null) {
                    rewardCoin = new Coin(deposit.getAddress(), depositReward, unlockHeight);
                    rewardList.add(rewardCoin);
                } else {
                    rewardCoin.setNa(rewardCoin.getNa().add(depositReward));
                }
            }
        }
        rewardList.sort(new Comparator<Coin>() {
            @Override
            public int compare(Coin o1, Coin o2) {
                return Arrays.hashCode(o1.getOwner()) > Arrays.hashCode(o2.getOwner()) ? 1 : -1;
            }
        });
        Coin agentReward = new Coin(self.getAgent().getRewardAddress(), Na.valueOf(DoubleUtils.longValue(caReward)), unlockHeight);
        rewardList.add(0, agentReward);
        return rewardList;
    }

    /**
     * 组装红/黄牌交易
     * @param bestBlock  本地最新区块
     * @param txList     需打包的交易列表
     * @param self       本地节点打包信息
     * @param round      本地最新轮次信息
     * */
    public static void punishTx(int chain_id, Block bestBlock, List<Transaction> txList, MeetingMember self, MeetingRound round) throws NulsException, IOException {
        YellowPunishTransaction yellowPunishTransaction = createYellowPunishTx(bestBlock, self, round);
        if (null == yellowPunishTransaction) {
            return;
        }
        txList.add(yellowPunishTransaction);
        //当连续100个黄牌时，给出一个红牌
        //When 100 yellow CARDS in a row, give a red card.
        List<byte[]> addressList = yellowPunishTransaction.getTxData().getAddressList();
        Set<Integer> punishedSet = new HashSet<>();
        for (byte[] address : addressList) {
            MeetingMember member = round.getMemberByAgentAddress(address);
            if (null == member) {
                member = round.getPreRound().getMemberByAgentAddress(address);
            }
            //如果节点信誉值小于等于临界值时，生成红牌交易
            if (DoubleUtils.compare(member.getAgent().getCreditVal(),ConsensusConstant.RED_PUNISH_CREDIT_VAL)==-1) {
                if (!punishedSet.add(member.getPackingIndexOfRound())) {
                    continue;
                }
                if (member.getAgent().getDelHeight() > 0L) {
                    continue;
                }
                RedPunishTransaction redPunishTransaction = new RedPunishTransaction();
                RedPunishData redPunishData = new RedPunishData();
                redPunishData.setAddress(address);
                redPunishData.setReasonCode(PunishReasonEnum.TOO_MUCH_YELLOW_PUNISH.getCode());
                redPunishTransaction.setTxData(redPunishData);
                redPunishTransaction.setTime(self.getPackEndTime());
                CoinData coinData = getStopAgentCoinData(chain_id, redPunishData.getAddress(), redPunishTransaction.getTime() + ConfigManager.config_map.get(chain_id).getRedPublish_lockTime());
                redPunishTransaction.setCoinData(coinData);
                redPunishTransaction.setHash(NulsDigestData.calcDigestData(redPunishTransaction.serializeForHash()));
                txList.add(redPunishTransaction);
            }
        }
    }

    /**
     * 组装黄牌
     * @param preBlock  本地最新区块
     * @param self      当前节点的打包信息
     * @param round     本地最新轮次信息
     * */
    public static YellowPunishTransaction createYellowPunishTx(Block preBlock, MeetingMember self, MeetingRound round) throws NulsException, IOException {
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
        YellowPunishTransaction punishTx = new YellowPunishTransaction();
        YellowPunishData data = new YellowPunishData();
        data.setAddressList(addressList);
        punishTx.setTxData(data);
        punishTx.setTime(self.getPackEndTime());
        punishTx.setHash(NulsDigestData.calcDigestData(punishTx.serializeForHash()));
        return punishTx;
    }
}
