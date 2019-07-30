package io.nuls.economic.nuls.util.manager;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinTo;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.DoubleUtils;
import io.nuls.economic.nuls.constant.NulsEconomicConstant;
import io.nuls.economic.nuls.model.bo.*;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.*;

/**
 * 经济模型管理类
 * @author tag
 * @date 2019/7/23
 */
@Component
public class EconomicManager {
    public static Map<Integer, ConsensusConfigInfo> configMap = new HashMap<>();

    private static InflationInfo lastVisitInflationInfo = null;
    
    /**
     * 分发共识奖励
     * @param agentInfo        本地打包信息/local agent packing info
     * @param roundInfo        本地最新轮次/local newest round info
     * @param consensusConfig  链配置信息/chain config
     * @param unlockHeight     解锁高度/unlock height
     * @param awardAssetMap    手续费集合
     * @return                 跨链交易分发集合
     * */
    public static List<CoinTo> getRewardCoin(AgentInfo agentInfo, RoundInfo roundInfo, ConsensusConfigInfo consensusConfig, long unlockHeight, Map<String, BigInteger> awardAssetMap)throws NulsException{
        List<CoinTo> rewardList = new ArrayList<>();
        /*
        如果为种子节点，只领取交易手续费不计算共识奖励（种子节点保证金为0）
        If it is a seed node, it only receives transaction fee without calculating consensus award (seed node margin is 0)
        */
        if (BigIntegerUtils.isEqual(agentInfo.getDeposit(), BigInteger.ZERO)) {
            if(awardAssetMap == null || awardAssetMap.isEmpty()){
                return rewardList;
            }
            for (Map.Entry<String, BigInteger> rewardEntry:awardAssetMap.entrySet()) {
                String[] assetInfo = rewardEntry.getKey().split(NulsEconomicConstant.SEPARATOR);
                CoinTo agentReword = new CoinTo(agentInfo.getRewardAddress(), Integer.valueOf(assetInfo[0]), Integer.valueOf(assetInfo[1]), rewardEntry.getValue(), unlockHeight);
                rewardList.add(agentReword);
            }
            return rewardList;
        }
        /*
        本轮次总的出块奖励金(本轮次出块节点数*共识基础奖励 )
        Total reward in this round
        */
        BigDecimal totalAll =calcRoundConsensusReward(roundInfo,consensusConfig).setScale(2, 1);
        Log.info("本轮次出块数量{}，本轮奖励总额：{}",roundInfo.getMemberCount(),totalAll );
        BigInteger selfAllDeposit = agentInfo.getDeposit().add(agentInfo.getTotalDeposit());
        BigDecimal agentWeight = DoubleUtils.mul(new BigDecimal(selfAllDeposit), agentInfo.getCreditVal());
        if (roundInfo.getTotalWeight() > 0 && agentWeight.doubleValue() > 0) {
            /*
            本节点共识奖励 = 节点权重/本轮次权重*共识基础奖励
            Node Consensus Award = Node Weight/Round Weight*Consensus Foundation Award
            */
            BigInteger consensusReword = DoubleUtils.mul(totalAll, DoubleUtils.div(agentWeight, roundInfo.getTotalWeight())).toBigInteger();
            String assetKey = consensusConfig.getChainId() + NulsEconomicConstant.SEPARATOR + consensusConfig.getAwardAssetId();
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
        Map<String,BigDecimal> depositWeightMap = getDepositWeight(agentInfo, selfAllDeposit);
        for (Map.Entry<String, BigInteger> rewardEntry:awardAssetMap.entrySet()) {
            String[] assetInfo = rewardEntry.getKey().split(NulsEconomicConstant.SEPARATOR);
            BigDecimal totalReward = new BigDecimal(rewardEntry.getValue());
            rewardList.addAll(assembleCoinTo(depositWeightMap, Integer.valueOf(assetInfo[0]),Integer.valueOf(assetInfo[1]) ,totalReward ,unlockHeight ));
        }
        return rewardList;
    }


    /**
     * 计算参与共识的账户权重
     * Calculating Account Weights for Participating in Consensus
     * @param agentInfo      当前节点信息
     * @param totalDeposit   当前节点本轮次总权重
     * @return               参与共识的账户权重分配详情
     * */
    private static Map<String,BigDecimal> getDepositWeight(AgentInfo agentInfo,BigInteger totalDeposit){
        Map<String,BigDecimal> depositWeightMap = new HashMap<>(NulsEconomicConstant.VALUE_OF_16);
        BigDecimal commissionRate = new BigDecimal(DoubleUtils.div(agentInfo.getCommissionRate(), 100, 2));
        BigDecimal depositRate = new BigDecimal(1).subtract(commissionRate);
        //节点创建者权重
        BigDecimal creatorWeight = new BigDecimal(agentInfo.getDeposit()).divide(new BigDecimal(totalDeposit), 4, RoundingMode.HALF_DOWN);
        BigDecimal creatorCommissionWeight = new BigDecimal(1).subtract(creatorWeight).multiply(commissionRate);
        creatorWeight = creatorWeight.add(creatorCommissionWeight);
        depositWeightMap.put(AddressTool.getStringAddressByBytes(agentInfo.getRewardAddress()), creatorWeight);
        /*
        计算各委托账户获得的奖励金
        Calculate the rewards for each entrusted account
        */
        for (DepositInfo deposit : agentInfo.getDepositList()) {
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
    private static List<CoinTo> assembleCoinTo(Map<String,BigDecimal> depositWeightMap, int assetChainId, int assetId, BigDecimal totalReward, long unlockHeight){
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


    /**
     * 计算本轮次总的共识奖励
     * Computing the general consensus awards for this round
     *
     * @param roundInfo             轮次信息
     * @param consensusConfig       共识配置
     * @return                      本轮总共识奖励
     * */
    private static BigDecimal calcRoundConsensusReward(RoundInfo roundInfo, ConsensusConfigInfo consensusConfig)throws NulsException{
        BigDecimal totalAll = BigDecimal.ZERO;
        //区块时间是出块结束时间，所以轮次出的第一个块时间是轮次开始时间+出块间隔时间
        long roundStartTime = roundInfo.getRoundStartTime() + consensusConfig.getPackingInterval();
        long roundEndTime = roundInfo.getRoundEndTime();

        InflationInfo inflationInfo = getInflationInfo(consensusConfig, roundStartTime);
        if(roundEndTime <= inflationInfo.getEndTime()){
            return DoubleUtils.mul(new BigDecimal(roundInfo.getMemberCount()), new BigDecimal(inflationInfo.getAwardUnit()));
        }

        long currentCount;
        while(roundEndTime > inflationInfo.getEndTime()){
            currentCount = (inflationInfo.getEndTime() - roundStartTime)/consensusConfig.getPackingInterval() + 1;
            totalAll = totalAll.add(DoubleUtils.mul(new BigDecimal(currentCount), new BigDecimal(inflationInfo.getAwardUnit())));
            Log.info("本轮共识奖励为{}的数量为{}", inflationInfo.getAwardUnit(),currentCount);
            roundStartTime += currentCount * consensusConfig.getPackingInterval();
            inflationInfo = getInflationInfo(consensusConfig, roundStartTime);
        }
        currentCount = (roundEndTime - roundStartTime)/consensusConfig.getPackingInterval() + 1;
        Log.info("本轮共识奖励为{}的数量为{}", inflationInfo.getAwardUnit(),currentCount);
        totalAll = totalAll.add(DoubleUtils.mul(new BigDecimal(currentCount), new BigDecimal(inflationInfo.getAwardUnit())));

        return totalAll;
    }

    /**
     * 计算指定时间点所在的通胀详情
     * Calculate inflation details at specified time points
     *
     * @param consensusConfig   共识配置
     * @param time              时间点
     * @return                  该时间点通胀信息
     * */
    private static InflationInfo getInflationInfo(ConsensusConfigInfo consensusConfig , long time) throws NulsException {
        InflationInfo inflationInfo = new InflationInfo();
        long startTime = consensusConfig.getInitTime();
        long endTime = consensusConfig.getInitTime() + consensusConfig.getDeflationTimeInterval();

        if(time < startTime){
            Log.error("The current time is less than the initial time of inflation");
            throw new NulsException(CommonCodeConstanst.PARAMETER_ERROR);
        }

        if(lastVisitInflationInfo != null && time >= lastVisitInflationInfo.getStartTime() && time <= lastVisitInflationInfo.getEndTime()){
            return lastVisitInflationInfo;
        }

        if(time <= endTime){
            inflationInfo.setStartTime(startTime);
            inflationInfo.setEndTime(endTime);
            inflationInfo.setInflationAmount(consensusConfig.getInflationAmount());
            inflationInfo.setAwardUnit(calcAwardUnit(consensusConfig, consensusConfig.getInflationAmount()));
        }else{
            long differentCount = (time - endTime)/consensusConfig.getDeflationTimeInterval();
            if((time - endTime)%consensusConfig.getDeflationTimeInterval() != 0){
                differentCount++;
            }
            long differentTime = consensusConfig.getDeflationTimeInterval()* differentCount;
            inflationInfo.setStartTime(startTime + differentTime);
            inflationInfo.setEndTime(endTime + differentTime);
            double ratio = DoubleUtils.div(consensusConfig.getDeflationRatio(), NulsEconomicConstant.VALUE_OF_100, 2);
            BigInteger inflationAmount = DoubleUtils.mul(new BigDecimal(consensusConfig.getInflationAmount()),BigDecimal.valueOf(Math.pow(ratio, differentCount))).toBigInteger();
            inflationInfo.setInflationAmount(inflationAmount);
            inflationInfo.setAwardUnit(calcAwardUnit(consensusConfig,inflationAmount));
        }
        lastVisitInflationInfo = inflationInfo;
        Log.info("通胀发生改变，当前通胀通胀开始时间{}：，当前阶段通胀结束时间：{},当前阶段通胀总数：{}，当前阶段出块单位奖励：{}", inflationInfo.getStartTime(),inflationInfo.getEndTime(),inflationInfo.getInflationAmount(),inflationInfo.getAwardUnit());
        return inflationInfo;
    }

    private static double calcAwardUnit(ConsensusConfigInfo consensusConfig, BigInteger inflationAmount){
        long blockCount = consensusConfig.getDeflationTimeInterval()/consensusConfig.getPackingInterval();
        return DoubleUtils.div(inflationAmount, BigInteger.valueOf(blockCount), 2);
    }
}
