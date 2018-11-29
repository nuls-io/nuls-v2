package io.nuls.poc.utils.validator;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.CancelDeposit;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.model.bo.tx.txdata.StopAgent;
import io.nuls.poc.model.po.AgentPo;
import io.nuls.poc.model.po.DepositPo;
import io.nuls.poc.model.po.PunishLogPo;
import io.nuls.poc.storage.AgentStorageService;
import io.nuls.poc.storage.DepositStorageService;
import io.nuls.poc.utils.manager.ConfigManager;
import io.nuls.poc.utils.manager.ConsensusManager;
import io.nuls.poc.utils.util.ConsensusUtil;
import io.nuls.poc.utils.util.PoConvertUtil;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.util.*;

@Component
public class ValidatorManager {
    @Autowired
    private AgentStorageService agentStorageService;
    @Autowired
    private DepositStorageService depositStorageService;

    public boolean validateCreateAgent(int chain_id,Transaction tx)throws NulsException{
        if(tx.getTxData() == null){
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        Agent agent = new Agent();
        agent.parse(tx.getTxData(),0);
        if(!createAgentBasicValid(chain_id,tx,agent)){
            return false;
        }
        if(!createAgentAddrValide(chain_id,tx,agent)){
            return false;
        }
        return  true;
    }

    /**
     * 创建节点交易基础验证
     * */
    private boolean createAgentBasicValid(int chain_id,Transaction tx,Agent agent)throws NulsException{
        if (!AddressTool.validNormalAddress(agent.getPackingAddress(),(short)chain_id)) {
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
        double commissionRate = agent.getCommissionRate();
        if (commissionRate < ConfigManager.config_map.get(chain_id).getCommissionRate_min() || commissionRate > ConfigManager.config_map.get(chain_id).getCommissionRate_max()) {
            throw new NulsException(ConsensusErrorCode.COMMISSION_RATE_OUT_OF_RANGE);
        }
        String deposit = agent.getDeposit();
        if(BigIntegerUtils.compare(deposit,ConfigManager.config_map.get(chain_id).getDeposit_min())<0 && BigIntegerUtils.compare(deposit,ConfigManager.config_map.get(chain_id).getDeposit_max())>0){
            throw new NulsException(ConsensusErrorCode.DEPOSIT_OUT_OF_RANGE);
        }
        CoinData coinData = new CoinData();
        coinData.parse(tx.getCoinData(),0);
        if (!isDepositOk(agent.getDeposit(), coinData)) {
            throw new NulsException(ConsensusErrorCode.DEPOSIT_ERROR);
        }
        if(!validSignature(tx,agent.getAgentAddress())){
            return false;
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
     * 创建节点交易节点地址及出块地址验证
     * */
    private boolean createAgentAddrValide(int chain_id,Transaction tx,Agent agent)throws NulsException{
        String seedNodesStr = ConfigManager.config_map.get(chain_id).getSeedNodes();
        if (StringUtils.isBlank(seedNodesStr)){
            return true;
        }
        byte[] nodeAddressBytes = null;
        //节点地址及出块地址不能是种子节点
        for (String nodeAddress:seedNodesStr.split("")) {
            nodeAddressBytes = AddressTool.getAddress(nodeAddress);
            if(Arrays.equals(nodeAddressBytes, agent.getAgentAddress())){
                throw new NulsException(ConsensusErrorCode.AGENT_EXIST);
            }
            if(Arrays.equals(nodeAddressBytes, agent.getPackingAddress())){
                throw new NulsException(ConsensusErrorCode.AGENT_PACKING_EXIST);
            }
        }
        //节点地址及出块地址不能重复
        List<Agent> agentList = ConsensusManager.getInstance().getAllAgentMap().get(chain_id);
        if(agentList != null && agentList.size()>0){
            Set<String> set = new HashSet<>();
            for (Agent agentTemp:agentList) {
                if(agentTemp.getTxHash().equals(tx.getHash())){
                    throw new NulsException(ConsensusErrorCode.TRANSACTION_REPEATED);
                }
                set.add(HexUtil.encode(agentTemp.getAgentAddress()));
                set.add(HexUtil.encode(agentTemp.getPackingAddress()));
            }
            boolean b = set.contains(HexUtil.encode(agent.getAgentAddress()));
            if(b){
                throw new NulsException(ConsensusErrorCode.AGENT_EXIST);
            }
            b = set.contains(HexUtil.encode(agent.getPackingAddress()));
            if(b){
                throw new NulsException(ConsensusErrorCode.AGENT_PACKING_EXIST);
            }
        }
        long count = this.getRedPunishCount(chain_id,agent.getAgentAddress());
        if(count > 0){
            throw new NulsException(ConsensusErrorCode.LACK_OF_CREDIT);
        }
        return true;
    }

    public boolean validateStopAgent(int chain_id,int assetsId,Transaction tx)throws NulsException,IOException{
        if(tx.getTxData() == null){
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        StopAgent stopAgent = new StopAgent();
        stopAgent.parse(tx.getTxData(),0);
        AgentPo agentPo = agentStorageService.get(stopAgent.getCreateTxHash(),chain_id);
        if(agentPo == null || agentPo.getDelHeight() > 0){
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        if(!validSignature(tx,agentPo.getAgentAddress())){
            return false;
        }
        CoinData coinData = new CoinData();
        coinData.parse(tx.getCoinData(),0);
        if (coinData.getTo() == null || coinData.getTo().isEmpty()) {
            throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        if(!stopAgentCoinDataValid(chain_id,assetsId,tx,agentPo,stopAgent,coinData)){
            return false;
        }
        return true;
    }

    /**
     * 停止节点交易CoinData验证
     * */
    private boolean stopAgentCoinDataValid(int chainId, int assetsId,Transaction tx,AgentPo agentPo,StopAgent stopAgent,CoinData coinData)throws NulsException,IOException {
        Agent agent = PoConvertUtil.poToAgent(agentPo);
        CoinData localCoinData = ConsensusUtil.getStopAgentCoinData(chainId, assetsId, agent, TimeService.currentTimeMillis() + ConfigManager.config_map.get(chainId).getStopAgent_lockTime());
        String fee = TransactionFeeCalculator.getMaxFee(tx.size());
        localCoinData.getTo().get(0).setAmount(BigIntegerUtils.subToString(coinData.getTo().get(0).getAmount(),fee));
        if(!Arrays.equals(coinData.serialize(),localCoinData.serialize())){
            return false;
        }
        return true;
    }

    public boolean validateDeposit(int chain_id, Transaction tx)throws NulsException{
        if (null == tx || null == tx.getTxData() || tx.getCoinData() == null) {
            throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        Deposit deposit = new Deposit();
        deposit.parse(tx.getTxData(),0);
        if(deposit.getAddress() == null || deposit.getAgentHash()==null || deposit.getDeposit() == null){
            throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        if(!createDepositInfoValid(chain_id,deposit)){
            return false;
        }
        CoinData coinData = new CoinData();
        coinData.parse(tx.getCoinData(),0);
        if (!isDepositOk(deposit.getDeposit(), coinData)) {
            throw new NulsException(ConsensusErrorCode.DEPOSIT_ERROR);
        }
        if(!validSignature(tx,deposit.getAddress())){
            return false;
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

    private boolean createDepositInfoValid(int chain_id,Deposit deposit) throws NulsException{
        AgentPo agentPo = agentStorageService.get(deposit.getAgentHash(),chain_id);
        if(agentPo == null || agentPo.getDelHeight() >0){
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        List<DepositPo> poList = this.getDepositListByAgent(chain_id,deposit.getAgentHash());
        if(poList != null && poList.size()>ConfigManager.config_map.get(chain_id).getDeposit_number_max()){
            throw new NulsException(ConsensusErrorCode.DEPOSIT_OVER_COUNT);
        }
        //节点当前委托总金额
        String total = deposit.getDeposit();
        for (DepositPo cd : poList) {
            total = BigIntegerUtils.addToString(total,cd.getDeposit());
        }
        if(BigIntegerUtils.compare(total,ConfigManager.config_map.get(chain_id).getDeposit_max())>0){
            throw new NulsException(ConsensusErrorCode.DEPOSIT_OVER_AMOUNT);
        }
        if(BigIntegerUtils.compare(total,ConfigManager.config_map.get(chain_id).getDeposit_min())<0){
            throw new NulsException(ConsensusErrorCode.DEPOSIT_NOT_ENOUGH);
        }
        return true;
    }

    public boolean validateWithdraw(int chain_id, Transaction tx)throws NulsException{
        CancelDeposit cancelDeposit = new CancelDeposit();
        cancelDeposit.parse(tx.getTxData(),0);
        DepositPo depositPo = depositStorageService.get(cancelDeposit.getJoinTxHash(),chain_id);
        if(depositPo == null || depositPo.getDelHeight() > 0){
            throw new NulsException(ConsensusErrorCode.DATA_NOT_EXIST);
        }
        if(!validSignature(tx,depositPo.getAddress())){
            return false;
        }
        return true;
    }

    /**
     * 交易签名验证
     * */
    private boolean validSignature(Transaction tx ,byte[] address) throws NulsException{
        TransactionSignature sig = new TransactionSignature();
        try {
            sig.parse(tx.getTransactionSignature(), 0);
            if (!SignatureUtil.containsAddress(tx, address)){
                return false;
            }
        }catch (NulsException e){
            Log.error(e);
            throw e;
        }
        return true;
    }

    /**
     * 委托信息验证
     * */
    private boolean isDepositOk(String deposit, CoinData coinData) {
        if(coinData == null || coinData.getTo().size() == 0) {
            return false;
        }
        CoinTo coin = coinData.getTo().get(0);
        if(!BigIntegerUtils.isEqual(deposit,coin.getAmount())) {
            return false;
        }
        if(coin.getLockTime() != ConsensusConstant.LOCK_OF_LOCK_TIME) {
            return false;
        }
        return true;
    }

    /**
     * 节点是否获得过红牌
     * */
    private long getRedPunishCount(int chain_id,byte[] address ) {
        List<PunishLogPo> list = ConsensusManager.getInstance().getRedPunishMap().get(chain_id);
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

    private List<DepositPo> getDepositListByAgent(int chain_id,NulsDigestData agentHash) throws NulsException{
        List<DepositPo> depositList = null;
        try {
            depositList = depositStorageService.getList(chain_id);
        }catch (Exception e){
            Log.error(e);
            throw new NulsException(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
        //todo  获取本地最新高度
        long startBlockHeight = 100;
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

    public static void main(String[] args){
        System.out.println(TimeService.currentTimeMillis() );
    }
}
