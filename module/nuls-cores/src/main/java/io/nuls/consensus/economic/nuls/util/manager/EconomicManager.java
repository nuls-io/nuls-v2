package io.nuls.consensus.economic.nuls.util.manager;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinTo;
import io.nuls.consensus.economic.nuls.constant.NulsEconomicConstant;
import io.nuls.consensus.economic.nuls.model.bo.*;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.ArraysTool;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.DoubleUtils;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Economic Model Management
 * @author tag
 * @date 2019/7/23
 */
@Component
public class EconomicManager {
    public static Map<Integer, ConsensusConfigInfo> configMap = new HashMap<>();

    private static InflationInfo lastVisitInflationInfo = null;
    
    /**
     * Distribute consensus rewards
     * @param agentInfo        Local packaging information/local agent packing info
     * @param roundInfo        Latest local round/local newest round info
     * @param consensusConfig  Chain configuration information/chain config
     * @param unlockHeight     Unlocking height/unlock height
     * @param awardAssetMap    Collection of handling fees
     * @return                 Cross chain transaction distribution set
     * */
    public static List<CoinTo> getRewardCoin(AgentInfo agentInfo, RoundInfo roundInfo, ConsensusConfigInfo consensusConfig, long unlockHeight, Map<String, BigInteger> awardAssetMap)throws NulsException{
        List<CoinTo> rewardList = new ArrayList<>();
        /*
        If it is a seed node, only receive transaction fees without calculating consensus rewards（The seed node deposit is0）
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

        if(consensusConfig.getTotalInflationAmount().equals(BigInteger.ZERO) || consensusConfig.getInflationAmount().equals(BigInteger.ZERO)){
            return rewardList;
        }

        /*
        The total reward for this round's block out bonus(The number of block nodes in this round*Consensus based rewards )
        Total reward in this round
        */
        BigDecimal totalAll =calcRoundConsensusReward(roundInfo,consensusConfig).setScale(2, 1);
        Log.info("Number of blocks produced in this round{}The total amount of rewards in this round：{}",roundInfo.getMemberCount(),totalAll );
        BigInteger selfAllDeposit = agentInfo.getDeposit().add(agentInfo.getTotalDeposit());
        BigDecimal agentWeight = DoubleUtils.mul(new BigDecimal(selfAllDeposit), agentInfo.getCreditVal());
        if (roundInfo.getTotalWeight() > 0 && agentWeight.doubleValue() > 0) {
            /*
            Consensus reward for this node = Node weight/Weight for this round*Consensus based rewards
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
        //Calculate the weight of participating consensus accounts
        Map<String,BigDecimal> depositWeightMap = getDepositWeight(agentInfo, selfAllDeposit);
        for (Map.Entry<String, BigInteger> rewardEntry:awardAssetMap.entrySet()) {
            String[] assetInfo = rewardEntry.getKey().split(NulsEconomicConstant.SEPARATOR);
            BigDecimal totalReward = new BigDecimal(rewardEntry.getValue());
            rewardList.addAll(assembleCoinTo(depositWeightMap, Integer.valueOf(assetInfo[0]),Integer.valueOf(assetInfo[1]) ,totalReward ,unlockHeight ));
        }
        return rewardList;
    }


    /**
     * Calculate the weight of accounts participating in consensus
     * Calculating Account Weights for Participating in Consensus
     * @param agentInfo      Current node information
     * @param totalDeposit   The total weight of the current node in this round
     * @return               Details of weight allocation for accounts participating in consensus
     * */
    private static Map<String,BigDecimal> getDepositWeight(AgentInfo agentInfo,BigInteger totalDeposit){
        Map<String,BigDecimal> depositWeightMap = new HashMap<>(NulsEconomicConstant.VALUE_OF_16);
        BigDecimal commissionRate = new BigDecimal(DoubleUtils.div(agentInfo.getCommissionRate(), 100, 2));
        if(commissionRate.compareTo(BigDecimal.ONE) >= 0){
            depositWeightMap.put(AddressTool.getStringAddressByBytes(agentInfo.getRewardAddress()), BigDecimal.ONE);
            return depositWeightMap;
        }
        BigDecimal depositRate = new BigDecimal(1).subtract(commissionRate);
        BigInteger creatorDeposit = agentInfo.getDeposit();
        /*
        Calculate the reward received by each entrusted account
        Calculate the rewards for each entrusted account
        */
        for (DepositInfo deposit : agentInfo.getDepositList()) {
            if(ArraysTool.arrayEquals(agentInfo.getRewardAddress(), deposit.getAddress())){
                creatorDeposit = creatorDeposit.add(deposit.getDeposit());
                continue;
            }
            /*
            Calculate the weight of each entrusted account（Entrusted amount/Total commission)
            Calculate the weight of each entrusted account (amount of entrusted account/total entrusted fee)
            */
            String depositAddress = AddressTool.getStringAddressByBytes(deposit.getAddress());
            BigDecimal depositWeight = new BigDecimal(deposit.getDeposit()).divide(new BigDecimal(totalDeposit), 8, RoundingMode.HALF_DOWN).multiply(depositRate);
            if(depositWeightMap.keySet().contains(depositAddress)){
                depositWeightMap.put(depositAddress, depositWeightMap.get(depositAddress).add(depositWeight));
            }else{
                depositWeightMap.put(depositAddress, depositWeight);
            }
        }

        //Node creator weight
        BigDecimal creatorWeight = new BigDecimal(creatorDeposit).divide(new BigDecimal(totalDeposit), 8, RoundingMode.HALF_DOWN);
        BigDecimal creatorCommissionWeight = BigDecimal.ONE.subtract(creatorWeight).multiply(commissionRate);
        creatorWeight = creatorWeight.add(creatorCommissionWeight);
        depositWeightMap.put(AddressTool.getStringAddressByBytes(agentInfo.getRewardAddress()), creatorWeight);
        Log.debug("Block weight allocation：{}",depositWeightMap.toString());
        return depositWeightMap;
    }

    /**
     * assembleCoinTo
     * @param depositWeightMap   Weight allocation for participating consensus accounts
     * @param assetChainId       Asset ChainID
     * @param assetId            assetID
     * @param totalReward        Total reward
     * @param unlockHeight       Lock height
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
     * Calculate the total consensus reward for this round
     * Computing the general consensus awards for this round
     *
     * @param roundInfo             Round information
     * @param consensusConfig       Consensus configuration
     * @return                      The overall consensus reward for this round
     * */
    private static BigDecimal calcRoundConsensusReward(RoundInfo roundInfo, ConsensusConfigInfo consensusConfig)throws NulsException{
        BigDecimal totalAll = BigDecimal.ZERO;
        //The block time is the end time of block generation, so the first block time produced in a round is the start time of the round+Block output interval time
        long roundStartTime = roundInfo.getRoundStartTime() + consensusConfig.getPackingInterval();
        long roundEndTime = roundInfo.getRoundEndTime();

        //Deflation caused by block rollback also rolls back
        boolean changeInflationInfo = lastVisitInflationInfo == null || roundStartTime >= lastVisitInflationInfo.getEndTime() || roundEndTime <= lastVisitInflationInfo.getStartTime();

        InflationInfo inflationInfo = getInflationInfo(consensusConfig, roundStartTime);
        if( roundStartTime >= inflationInfo.getStartTime() && roundEndTime <= inflationInfo.getEndTime()){
            return DoubleUtils.mul(new BigDecimal(roundInfo.getMemberCount()), new BigDecimal(inflationInfo.getAwardUnit()));
        }

        long currentCount;
        while(roundEndTime > inflationInfo.getEndTime()){
            currentCount = (inflationInfo.getEndTime() - roundStartTime)/consensusConfig.getPackingInterval() + 1;
            totalAll = totalAll.add(DoubleUtils.mul(new BigDecimal(currentCount), new BigDecimal(inflationInfo.getAwardUnit())));
            Log.info("The consensus reward for this round is{}The quantity is{}", inflationInfo.getAwardUnit(),currentCount);
            roundStartTime += currentCount * consensusConfig.getPackingInterval();
            inflationInfo = getInflationInfo(consensusConfig, roundStartTime);
        }
        currentCount = (roundEndTime - roundStartTime)/consensusConfig.getPackingInterval() + 1;
        Log.info("The consensus reward for this round is{}The quantity is{}", inflationInfo.getAwardUnit(),currentCount);
        totalAll = totalAll.add(DoubleUtils.mul(new BigDecimal(currentCount), new BigDecimal(inflationInfo.getAwardUnit())));
        if(changeInflationInfo){
            lastVisitInflationInfo = inflationInfo;
        }
        return totalAll;
    }

    /**
     * Calculate the inflation details at the specified time point
     * Calculate inflation details at specified time points
     *
     * @param consensusConfig   Consensus configuration
     * @param time              time point
     * @return                  Inflation information at this time point
     * */
    private static InflationInfo getInflationInfo(ConsensusConfigInfo consensusConfig , long time) throws NulsException {
        if(lastVisitInflationInfo != null && time >= lastVisitInflationInfo.getStartTime() && time <= lastVisitInflationInfo.getEndTime()){
            return lastVisitInflationInfo;
        }
        InflationInfo inflationInfo = new InflationInfo();
        long startTime = consensusConfig.getInitTime();
        long endTime = consensusConfig.getInitTime() + consensusConfig.getDeflationTimeInterval();

        if(time < startTime){
            time = startTime;
            Log.info("The current time is less than the start time of deflation！" );
        }

        if(time <= endTime){
            inflationInfo.setStartTime(startTime);
            inflationInfo.setEndTime(endTime);
            inflationInfo.setInflationAmount(new BigDecimal(consensusConfig.getInflationAmount()));
            inflationInfo.setAwardUnit(calcAwardUnit(consensusConfig, new BigDecimal(consensusConfig.getInflationAmount())));
        }else{
            long differentCount = (time - endTime)/consensusConfig.getDeflationTimeInterval();
            if((time - endTime)%consensusConfig.getDeflationTimeInterval() != 0){
                differentCount++;
            }
            long differentTime = consensusConfig.getDeflationTimeInterval()* differentCount;
            inflationInfo.setStartTime(startTime + differentTime);
            inflationInfo.setEndTime(endTime + differentTime);
            double ratio = DoubleUtils.div(consensusConfig.getDeflationRatio(), NulsEconomicConstant.VALUE_OF_100, 4);
            BigDecimal inflationAmount = DoubleUtils.mul(new BigDecimal(consensusConfig.getInflationAmount()),BigDecimal.valueOf(Math.pow(ratio, differentCount)));
            inflationInfo.setInflationAmount(inflationAmount);
            inflationInfo.setAwardUnit(calcAwardUnit(consensusConfig,inflationAmount));
        }
        Log.info("Inflation changes, current inflation start time{}：The end time of inflation at the current stage：{},Total inflation at the current stage：{}At the current stage, unit rewards for block production：{}", inflationInfo.getStartTime(),inflationInfo.getEndTime(),inflationInfo.getInflationAmount(),inflationInfo.getAwardUnit());
        return inflationInfo;
    }

    private static double calcAwardUnit(ConsensusConfigInfo consensusConfig, BigDecimal inflationAmount){
        long blockCount = consensusConfig.getDeflationTimeInterval()/consensusConfig.getPackingInterval();
        return DoubleUtils.div(inflationAmount, BigDecimal.valueOf(blockCount)).setScale(4, BigDecimal.ROUND_HALF_UP).doubleValue();
    }
}
