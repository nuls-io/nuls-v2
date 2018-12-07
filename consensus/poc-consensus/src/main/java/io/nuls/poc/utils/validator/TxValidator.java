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
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.CancelDeposit;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.model.bo.tx.txdata.StopAgent;
import io.nuls.poc.model.po.AgentPo;
import io.nuls.poc.model.po.DepositPo;
import io.nuls.poc.model.po.PunishLogPo;
import io.nuls.poc.storage.AgentStorageService;
import io.nuls.poc.storage.DepositStorageService;
import io.nuls.poc.utils.manager.*;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * 交易验证工具类
 * Transaction Verification Tool Class
 *
 * @author tag
 * 2018/11/30
 * */
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
    /**
     * 验证交易
     * Verifying transactions
     *
     * @param chainId 链ID/chain id
     * @param tx       交易/transaction info
     * @return boolean
     * */
    public boolean validateTx(int chainId, Transaction tx) throws  NulsException,IOException{
        switch (tx.getType()){
            case(ConsensusConstant.TX_TYPE_REGISTER_AGENT) : return validateCreateAgent(chainId,tx);
            case(ConsensusConstant.TX_TYPE_STOP_AGENT): return validateStopAgent(chainId,tx);
            case(ConsensusConstant.TX_TYPE_JOIN_CONSENSUS): return validateDeposit(chainId,tx);
            case(ConsensusConstant.TX_TYPE_CANCEL_DEPOSIT): return validateWithdraw(chainId,tx);
            default: return false;
        }
    }

    /**
     * 创建节点交易验证
     * Create node transaction validation
     *
     * @param chainId    链ID/chain id
     * @param tx          创建节点交易/create agent transaction
     * @return boolean
     * */
    private boolean validateCreateAgent(int chainId,Transaction tx)throws NulsException{
        if(tx.getTxData() == null){
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        Agent agent = new Agent();
        agent.parse(tx.getTxData(),0);
        if(!createAgentBasicValid(chainId,tx,agent)){
            return false;
        }
        if(!createAgentAddrValide(chainId,tx,agent)){
            return false;
        }
        return  true;
    }

    /**
     * 停止节点交易验证
     * Stop Node Transaction Verification
     *      *
     * @param chainId   链ID/chain id
     * @param tx         停止节点交易/stop agent transaction
     * @return boolean
     * */
    private boolean validateStopAgent(int chainId,Transaction tx)throws NulsException,IOException{
        if(tx.getTxData() == null){
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        StopAgent stopAgent = new StopAgent();
        stopAgent.parse(tx.getTxData(),0);
        AgentPo agentPo = agentStorageService.get(stopAgent.getCreateTxHash(),chainId);
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
        if(!stopAgentCoinDataValid(chainId,tx,agentPo,stopAgent,coinData)){
            return false;
        }
        return true;
    }

    /**
     * 委托共识交易验证
     * Deposit Transaction Verification
     *
     * @param chainId 链ID/Chain Id
     * @param tx       委托共识交易/Deposit Transaction
     * @return boolean
     * */
    private boolean validateDeposit(int chainId, Transaction tx)throws NulsException{
        if (null == tx || null == tx.getTxData() || tx.getCoinData() == null) {
            throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        Deposit deposit = new Deposit();
        deposit.parse(tx.getTxData(),0);
        if(deposit.getAddress() == null || deposit.getAgentHash()==null || deposit.getDeposit() == null){
            throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        if(!createDepositInfoValid(chainId,deposit)){
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

    /**
     * 退出节点交易验证
     * Withdraw Transaction Verification
     *
     * @param chainId  链ID/chain id
     * @param tx        退出节点交易/Withdraw Transaction
     * @return  boolean
     * */
    private boolean validateWithdraw(int chainId, Transaction tx)throws NulsException{
        CancelDeposit cancelDeposit = new CancelDeposit();
        cancelDeposit.parse(tx.getTxData(),0);
        DepositPo depositPo = depositStorageService.get(cancelDeposit.getJoinTxHash(),chainId);
        if(depositPo == null || depositPo.getDelHeight() > 0){
            throw new NulsException(ConsensusErrorCode.DATA_NOT_EXIST);
        }
        if(!validSignature(tx,depositPo.getAddress())){
            return false;
        }
        return true;
    }

    /**
     * 创建节点交易基础验证
     * Create agent transaction base validation
     *
     * @param chainId 链ID/chain id
     * @param tx       创建节点交易/create transaction
     * @param agent    节点/agent
     * @return boolean
     * */
    private boolean createAgentBasicValid(int chainId,Transaction tx,Agent agent)throws NulsException{
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null ){
            throw new NulsException(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        if (!AddressTool.validNormalAddress(agent.getPackingAddress(),(short)chainId)) {
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
        if (commissionRate < chain.getConfig().getCommissionRateMin() || commissionRate > chain.getConfig().getCommissionRateMax()) {
            throw new NulsException(ConsensusErrorCode.COMMISSION_RATE_OUT_OF_RANGE);
        }
        BigInteger deposit = agent.getDeposit();
        if(deposit.compareTo(chain.getConfig().getDepositMin())<0 && deposit.compareTo(chain.getConfig().getDepositMax())>0){
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
     * address validate
     *
     * @param chainId  链ID/chain id
     * @param tx        创建节点交易/create transaction
     * @param agent     节点/agent
     * @return boolean
     * */
    private boolean createAgentAddrValide(int chainId,Transaction tx,Agent agent)throws NulsException{
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null ){
            throw new NulsException(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        String seedNodesStr = chain.getConfig().getSeedNodes();
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
        List<Agent> agentList = chain.getAgentList();
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
        long count = this.getRedPunishCount(chain,agent.getAgentAddress());
        if(count > 0){
            throw new NulsException(ConsensusErrorCode.LACK_OF_CREDIT);
        }
        return true;
    }


    /**
     * 停止节点交易CoinData验证
     * Stop agent transaction CoinData validate
     *
     * @param chainId    链ID/chain id
     * @param tx         退出节点交易/stop agent transaction
     * @param agentPo    退出的节点信息/agent
     * @param coinData   交易的CoinData/coinData
     * @return  boolean
     * */
    private boolean stopAgentCoinDataValid(int chainId,Transaction tx,AgentPo agentPo,StopAgent stopAgent,CoinData coinData)throws NulsException,IOException {
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null ){
            throw new NulsException(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        Agent agent = agentManager.poToAgent(agentPo);
        CoinData localCoinData = coinDataManager.getStopAgentCoinData(chain, agent, TimeService.currentTimeMillis() + chain.getConfig().getStopAgentLockTime());
        BigInteger fee = TransactionFeeCalculator.getMaxFee(tx.size());
        localCoinData.getTo().get(0).setAmount(coinData.getTo().get(0).getAmount().subtract(fee));
        if(!Arrays.equals(coinData.serialize(),localCoinData.serialize())){
            return false;
        }
        return true;
    }

    /**
     * 委托交易基础信息验证
     * deposit transaction base validate
     *
     * @param chainId   链ID/chain id
     * @param deposit    委托信息/deposit
     * @return boolean
     * */
    private boolean createDepositInfoValid(int chainId,Deposit deposit) throws NulsException{
        Chain chain = chainManager.getChainMap().get(chainId);
        if(chain == null ){
            throw new NulsException(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        AgentPo agentPo = agentStorageService.get(deposit.getAgentHash(),chainId);
        if(agentPo == null || agentPo.getDelHeight() >0){
            throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        List<DepositPo> poList = this.getDepositListByAgent(chainId,deposit.getAgentHash());
        if(poList != null && poList.size()>chain.getConfig().getDepositNumberMax()){
            throw new NulsException(ConsensusErrorCode.DEPOSIT_OVER_COUNT);
        }
        //节点当前委托总金额
        BigInteger total = deposit.getDeposit();
        for (DepositPo cd : poList) {
            total = total.add(cd.getDeposit());
        }
        if(total.compareTo(chain.getConfig().getDepositMax())>0){
            throw new NulsException(ConsensusErrorCode.DEPOSIT_OVER_AMOUNT);
        }
        if(total.compareTo(chain.getConfig().getDepositMin())<0){
            throw new NulsException(ConsensusErrorCode.DEPOSIT_NOT_ENOUGH);
        }
        return true;
    }


    /**
     * 交易签名验证
     * Transaction signature verification
     *
     * @param tx       交易/transaction
     * @param address  签名账户/signature
     * @return boolean
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
     * deposit validate
     *
     * @param deposit    委托信息/deposit
     * @param coinData   交易的CoinData/CoinData
     * @return boolean
     * */
    private boolean isDepositOk(BigInteger deposit, CoinData coinData) {
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
     * Does the node get a red card?
     *
     * @param chain      chain info
     * @param address    出块地址/packing address
     * @return long
     * */
    private long getRedPunishCount(Chain chain,byte[] address ) {
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
     * 获取节点的委托列表
     * Get the delegate list for the node
     *
     * @param chainId   链ID/chain id
     * @param agentHash  节点HASH/agent hash
     * @return  List<DepositPo>
     * */
    private List<DepositPo> getDepositListByAgent(int chainId,NulsDigestData agentHash) throws NulsException{
        List<DepositPo> depositList = null;
        try {
            depositList = depositStorageService.getList(chainId);
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
}
