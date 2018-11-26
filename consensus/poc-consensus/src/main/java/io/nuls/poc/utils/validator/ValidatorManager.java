package io.nuls.poc.utils.validator;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
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
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;

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
        long deposit = agent.getDeposit().getValue();
        if(deposit<ConfigManager.config_map.get(chain_id).getDeposit_min() && deposit > ConfigManager.config_map.get(chain_id).getDeposit_max()){
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
        for (Coin coin : coinData.getTo()) {
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

    public boolean validateStopAgent(int chain_id,Transaction tx)throws NulsException{
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
        if(!stopAgentCoinDataValid(chain_id,tx,agentPo,stopAgent,coinData)){
            return false;
        }
        return true;
    }

    /**
     * 停止节点交易CoinData验证
     * */
    private boolean stopAgentCoinDataValid(int chain_id,Transaction tx,AgentPo agentPo,StopAgent stopAgent,CoinData coinData)throws NulsException{
        List<Deposit> depositList = ConsensusManager.getInstance().getAllDepositMap().get(chain_id);
        Map<NulsDigestData, Deposit> depositMap = new HashMap<>();
        //节点总委托金额（创建节点保证金+委托共识金额）
        Na totalNa = agentPo.getDeposit();
        //创建节点的保证金
        Deposit ownDeposit = new Deposit();
        ownDeposit.setDeposit(agentPo.getDeposit());
        ownDeposit.setAddress(agentPo.getAgentAddress());
        depositMap.put(stopAgent.getCreateTxHash(), ownDeposit);
        //遍历所有委托信息，找出委托本节点的委托信息
        for (Deposit deposit:depositList) {
            if (deposit.getDelHeight() > -1L && (tx.getBlockHeight() == -1L || deposit.getDelHeight() < tx.getBlockHeight())) {
                continue;
            }
            if (!deposit.getAgentHash().equals(agentPo.getHash())) {
                continue;
            }
            depositMap.put(deposit.getTxHash(), deposit);
            totalNa = totalNa.add(deposit.getDeposit());
        }
        //注销节点coinData输入（from）的总金额
        Na fromTotal = Na.ZERO;
        //存放每个账户的委托金额
        Map<String, Na> verifyToMap = new HashMap<>();
        //验证注销节点交易的输入
        for (Coin coin : coinData.getFrom()) {
            //如果该笔UTXO有锁定时间则不能使用
            if (coin.getLockTime() != -1L) {
               throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            //获取该笔UTXO的交易HASH
            NulsDigestData txHash = new NulsDigestData();
            txHash.parse(coin.getOwner(), 0);
            //找到该笔UTXO对应的委托信息
            Deposit deposit = depositMap.remove(txHash);
            //如果委托信息不存在，则表示CoinData组装有错
            if (deposit == null) {
                throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            //如果委托金额与UTXO金额不一样，则表示数据错误
            if (deposit.getAgentHash() == null && !coin.getNa().equals(agentPo.getDeposit())) {
                throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            } else if (!deposit.getDeposit().equals(coin.getNa())) {
                throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            fromTotal = fromTotal.add(coin.getNa());
            //agentHash为null表示该委托信息是创建节点时的保证金信息
            if (deposit.getAgentHash() == null) {
                continue;
            }
            String address = AddressTool.getStringAddressByBytes(deposit.getAddress());
            Na na = verifyToMap.get(address);
            if (null == na) {
                na = deposit.getDeposit();
            } else {
                na = na.add(deposit.getDeposit());
            }
            verifyToMap.put(address, na);
        }
        //如果节点的委托信息与注销节点使用的UTXO不是一一对应的则表示数据错误
        if (!depositMap.isEmpty()) {
            throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        //如果委托总金额与注销节点使用的总金额不相等则表示数据错误
        if (!totalNa.equals(fromTotal)) {
            throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        //创建节点账户注销节点之后返回的金额（保证金-手续费）
        Na ownToCoin = ownDeposit.getDeposit().subtract(coinData.getFee());
        //注销节点后保证金锁定时间
        long ownLockTime = 0L;
        boolean isDeposit = true;
        //验证注销节点交易的输出
        for (Coin coin : coinData.getTo()) {
            String address = AddressTool.getStringAddressByBytes(coin.getAddress());
            Na na = verifyToMap.get(address);
            //如果委托金额与返回金额相等则验证通过
            if (null != na && na.equals(coin.getNa())) {
                verifyToMap.remove(address);
                continue;
            }
            //创建节点保证金验证（创建节点只有一个保证金）
            if(isDeposit && Arrays.equals(coin.getAddress(), ownDeposit.getAddress())  && coin.getNa().equals(ownToCoin)){
                ownLockTime = coin.getLockTime();
                isDeposit = false;
            }else{
                throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
        }
        //todo ???
        if(ownLockTime < (tx.getTime()+ConfigManager.config_map.get(chain_id).getStopAgent_lockTime())){
            throw new NulsException(ConsensusErrorCode.MARGIN_LOCK_TIME_ERROR);
        }else if (tx.getBlockHeight() <= 0 && ownLockTime < (TimeService.currentTimeMillis() + ConfigManager.config_map.get(chain_id).getStopAgent_lockTime() - 300000L)) {
            throw new NulsException(ConsensusErrorCode.MARGIN_LOCK_TIME_ERROR);
        }
        if (!verifyToMap.isEmpty()) {
            throw new NulsException(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
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
        for (Coin coin : coinData.getTo()) {
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
        Na total = Na.ZERO;
        for (DepositPo cd : poList) {
            total = total.add(cd.getDeposit());
        }
        if(total.getValue()+deposit.getDeposit().getValue() > ConfigManager.config_map.get(chain_id).getDeposit_max()){
            throw new NulsException(ConsensusErrorCode.DEPOSIT_OVER_AMOUNT);
        }
        if(total.getValue()+deposit.getDeposit().getValue() < ConfigManager.config_map.get(chain_id).getDeposit_min()){
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
    private boolean isDepositOk(Na deposit, CoinData coinData) {
        if(coinData == null || coinData.getTo().size() == 0) {
            return false;
        }
        Coin coin = coinData.getTo().get(0);
        if(!deposit.equals(coin.getNa())) {
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
