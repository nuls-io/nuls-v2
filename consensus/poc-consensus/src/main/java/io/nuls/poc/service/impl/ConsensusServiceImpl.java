package io.nuls.poc.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.round.MeetingMember;
import io.nuls.poc.model.bo.round.MeetingRound;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.CancelDeposit;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.model.bo.tx.txdata.StopAgent;
import io.nuls.poc.model.dto.input.*;
import io.nuls.poc.model.dto.output.*;
import io.nuls.poc.model.po.AgentPo;
import io.nuls.poc.model.po.DepositPo;
import io.nuls.poc.model.po.PunishLogPo;
import io.nuls.poc.service.ConsensusService;
import io.nuls.poc.storage.AgentStorageService;
import io.nuls.poc.storage.DepositStorageService;
import io.nuls.poc.storage.PunishStorageService;
import io.nuls.poc.utils.enumeration.ConsensusStatus;
import io.nuls.poc.utils.manager.*;
import io.nuls.poc.utils.validator.BatchValidator;
import io.nuls.poc.utils.validator.BlockValidator;
import io.nuls.poc.utils.validator.TxValidator;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.data.ObjectUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * 共识模块RPC接口实现类
 * Consensus Module RPC Interface Implementation Class
 *
 * @author tag
 * 2018/11/7
 */
@Service
public class ConsensusServiceImpl implements ConsensusService {
    @Autowired
    private AgentStorageService agentService;
    @Autowired
    private DepositStorageService depositService;
    @Autowired
    private PunishStorageService publishService;
    @Autowired
    private TxValidator validatorManager;
    @Autowired
    private CoinDataManager coinDataManager;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private RoundManager roundManager;
    @Autowired
    private DepositManager depositManager;
    @Autowired
    private BlockValidator blockValidator;
    @Autowired
    private BatchValidator batchValidator;
    @Autowired
    private BlockManager blockManager;

    /**
     * 创建节点
     */
    @Override
    public Result createAgent(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        CreateAgentDTO dto = JSONUtils.map2pojo(params, CreateAgentDTO.class);
        ObjectUtils.canNotEmpty(dto);
        ObjectUtils.canNotEmpty(dto.getChainId(), "chainId can not be null");
        ObjectUtils.canNotEmpty(dto.getAgentAddress(), "agent address can not be null");
        ObjectUtils.canNotEmpty(dto.getCommissionRate(), "commission rate can not be null");
        ObjectUtils.canNotEmpty(dto.getDeposit(), "deposit can not be null");
        ObjectUtils.canNotEmpty(dto.getPackingAddress(), "packing address can not be null");
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            Log.error(ConsensusErrorCode.CHAIN_NOT_EXIST.getMsg());
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            //1.参数验证
            if (!AddressTool.isPackingAddress(dto.getPackingAddress(), (short) dto.getChainId()) || !AddressTool.validAddress((short) dto.getChainId(), dto.getAgentAddress())) {
                throw new NulsRuntimeException(ConsensusErrorCode.ADDRESS_ERROR);
            }
            //2.账户验证
            HashMap callResult = accountValid(dto.getChainId(),dto.getAgentAddress(),dto.getPassword());
            //3.组装创建节点交易
            Transaction tx = new Transaction(ConsensusConstant.TX_TYPE_REGISTER_AGENT);
            tx.setTime(TimeService.currentTimeMillis());
            //3.1.组装共识节点信息
            Agent agent = new Agent();
            agent.setAgentAddress(AddressTool.getAddress(dto.getAgentAddress()));
            agent.setPackingAddress(AddressTool.getAddress(dto.getPackingAddress()));
            if (StringUtils.isBlank(dto.getRewardAddress())) {
                agent.setRewardAddress(agent.getAgentAddress());
            } else {
                agent.setRewardAddress(AddressTool.getAddress(dto.getRewardAddress()));
            }
            agent.setDeposit(BigIntegerUtils.stringToBigInteger(dto.getDeposit()));
            agent.setCommissionRate(dto.getCommissionRate());
            tx.setTxData(agent.serialize());
            //3.2.组装coinData
            CoinData coinData = coinDataManager.getCoinData(agent.getAgentAddress(), chain, new BigInteger(dto.getDeposit()), ConsensusConstant.CONSENSUS_LOCK_TIME, tx.size() + P2PHKSignature.SERIALIZE_LENGTH);
            tx.setCoinData(coinData.serialize());
            //4.交易签名
            String priKey =(String)callResult.get("priKey");
            transactionSignture(dto.getChainId(),dto.getAgentAddress(),dto.getPassword(),priKey,tx);
            //todo 5.将交易发送给交易管理模块

            Map<String, Object> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            result.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        } catch (NulsRuntimeException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        }catch (Exception e){
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(ConsensusErrorCode.INTERFACE_CALL_FAILED);
        }
    }

    /**
     * 停止节点
     */
    @Override
    public Result stopAgent(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        StopAgentDTO dto = JSONUtils.map2pojo(params, StopAgentDTO.class);
        ObjectUtils.canNotEmpty(dto);
        ObjectUtils.canNotEmpty(dto.getChainId(), "chainId can not be null");
        ObjectUtils.canNotEmpty(dto.getAddress(), "address can not be null");
        if (!AddressTool.validAddress((short) dto.getChainId(), dto.getAddress())) {
            throw new NulsRuntimeException(ConsensusErrorCode.ADDRESS_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            HashMap callResult = accountValid(dto.getChainId(),dto.getAddress(),dto.getPassword());
            Transaction tx = new Transaction(ConsensusConstant.TX_TYPE_STOP_AGENT);
            StopAgent stopAgent = new StopAgent();
            stopAgent.setAddress(AddressTool.getAddress(dto.getAddress()));
            List<Agent> agentList = chain.getAgentList();
            Agent agent = null;
            for (Agent a : agentList) {
                if (a.getDelHeight() > 0) {
                    continue;
                }
                if (Arrays.equals(a.getAgentAddress(), AddressTool.getAddress(dto.getAddress()))) {
                    agent = a;
                    break;
                }
            }
            if (agent == null || agent.getDelHeight() > 0) {
                return Result.getFailed(ConsensusErrorCode.AGENT_NOT_EXIST);
            }
            stopAgent.setCreateTxHash(agent.getTxHash());
            tx.setTxData(stopAgent.serialize());
            CoinData coinData = coinDataManager.getStopAgentCoinData(chain, agent, TimeService.currentTimeMillis() + chain.getConfig().getStopAgentLockTime());
            tx.setCoinData(coinData.serialize());
            BigInteger fee = TransactionFeeCalculator.getNormalTxFee(tx.size());
            coinData.getTo().get(0).setAmount(coinData.getTo().get(0).getAmount().subtract(fee));
            //交易签名
            String priKey =(String)callResult.get("priKey");
            transactionSignture(dto.getChainId(),dto.getAddress(),dto.getPassword(),priKey,tx);
            //todo 将交易传递给交易管理模块

            Map<String, Object> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            result.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
    }

    /**
     * 委托共识
     */
    @Override
    public Result depositToAgent(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        CreateDepositDTO dto = JSONUtils.map2pojo(params, CreateDepositDTO.class);
        ObjectUtils.canNotEmpty(dto);
        ObjectUtils.canNotEmpty(dto.getAddress());
        ObjectUtils.canNotEmpty(dto.getAgentHash());
        ObjectUtils.canNotEmpty(dto.getDeposit());
        if (!NulsDigestData.validHash(dto.getAgentHash())) {
            return Result.getFailed(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            if (!AddressTool.validAddress((short) dto.getChainId(), dto.getAddress())) {
                throw new NulsException(ConsensusErrorCode.ADDRESS_ERROR);
            }
            //账户验证
            HashMap callResult = accountValid(dto.getChainId(),dto.getAddress(),dto.getPassword());
            Transaction tx = new Transaction(ConsensusConstant.TX_TYPE_JOIN_CONSENSUS);
            Deposit deposit = new Deposit();
            deposit.setAddress(AddressTool.getAddress(dto.getAddress()));
            deposit.setAgentHash(NulsDigestData.fromDigestHex(dto.getAgentHash()));
            deposit.setDeposit(BigIntegerUtils.stringToBigInteger(dto.getDeposit()));
            tx.setTxData(deposit.serialize());
            CoinData coinData = coinDataManager.getCoinData(deposit.getAddress(), chain, new BigInteger(dto.getDeposit()), ConsensusConstant.CONSENSUS_LOCK_TIME, tx.size() + P2PHKSignature.SERIALIZE_LENGTH);
            tx.setCoinData(coinData.serialize());
            //交易签名
            String priKey =(String)callResult.get("priKey");
            transactionSignture(dto.getChainId(),dto.getAddress(),dto.getPassword(),priKey,tx);

            //todo 将交易传递给交易管理模块
            Map<String, Object> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            result.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
    }

    /**
     * 退出共识
     */
    @Override
    public Result withdraw(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        WithdrawDTO dto = JSONUtils.map2pojo(params, WithdrawDTO.class);
        if (!NulsDigestData.validHash(dto.getTxHash())) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            if (!AddressTool.validAddress((short) dto.getChainId(), dto.getAddress())) {
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            //账户验证
            HashMap callResult = accountValid(dto.getChainId(),dto.getAddress(),dto.getPassword());

            //todo 从交易模块获取委托交易（交易模块）+ 返回数据处理
            NulsDigestData hash = NulsDigestData.fromDigestHex(dto.getTxHash());
            Transaction depositTransaction = new Transaction(ConsensusConstant.TX_TYPE_JOIN_CONSENSUS);
            if (depositTransaction == null) {
                return Result.getFailed(ConsensusErrorCode.TX_NOT_EXIST);
            }
            CoinData depositCoinData = new CoinData();
            depositCoinData.parse(depositTransaction.getCoinData(), 0);
            Deposit deposit = new Deposit();
            deposit.parse(depositTransaction.getTxData(), 0);
            boolean flag = false;
            for (CoinTo to : depositCoinData.getTo()) {
                if (to.getLockTime() == -1L && to.getAmount().compareTo(deposit.getDeposit()) == 0) {
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                return Result.getFailed(ConsensusErrorCode.DATA_ERROR);
            }
            Transaction cancelDepositTransaction = new Transaction(ConsensusConstant.TX_TYPE_CANCEL_DEPOSIT);
            CancelDeposit cancelDeposit = new CancelDeposit();
            cancelDeposit.setAddress(AddressTool.getAddress(dto.getAddress()));
            cancelDeposit.setJoinTxHash(hash);
            cancelDepositTransaction.setTxData(cancelDeposit.serialize());
            CoinData coinData = coinDataManager.getUnlockCoinData(cancelDeposit.getAddress(), chain, deposit.getDeposit(), 0, cancelDepositTransaction.size() + P2PHKSignature.SERIALIZE_LENGTH);
            coinData.getFrom().get(0).setNonce(hash.getDigestBytes());
            cancelDepositTransaction.setCoinData(coinData.serialize());
            //交易签名
            String priKey =(String)callResult.get("priKey");
            transactionSignture(dto.getChainId(),dto.getAddress(),dto.getPassword(),priKey,cancelDepositTransaction);
            //todo 将交易传递给交易管理模块
            Map<String, Object> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            result.put("txHex", HexUtil.encode(cancelDepositTransaction.serialize()));
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
    }

    /**
     * 获取节点列表信息
     */
    @Override
    public Result getAgentList(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        SearchAllAgentDTO dto = JSONUtils.map2pojo(params, SearchAllAgentDTO.class);
        int pageNumber = dto.getPageNumber();
        int pageSize = dto.getPageSize();
        int chainId = dto.getChainId();
        if (pageNumber == ConsensusConstant.MIN_VALUE) {
            pageNumber = ConsensusConstant.PAGE_NUMBER_INIT_VALUE;
        }
        if (pageSize == ConsensusConstant.MIN_VALUE) {
            pageSize = ConsensusConstant.PAGE_SIZE_INIT_VALUE;
        }
        if (pageNumber < ConsensusConstant.MIN_VALUE || pageSize < ConsensusConstant.MIN_VALUE || pageSize > ConsensusConstant.PAGE_SIZE_MAX_VALUE || chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            List<Agent> agentList = chain.getAgentList();
            List<Agent> handleList = new ArrayList<>();
            String keyword = dto.getKeyWord();
            long startBlockHeight = chain.getNewestHeader().getHeight();
            for (Agent agent : agentList) {
                if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                    continue;
                }
                if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                    continue;
                }
                if (StringUtils.isNotBlank(keyword)) {
                    keyword = keyword.toUpperCase();
                    String agentAddress = AddressTool.getStringAddressByBytes(agent.getAgentAddress()).toUpperCase();
                    String packingAddress = AddressTool.getStringAddressByBytes(agent.getPackingAddress()).toUpperCase();
                    String agentId = agentManager.getAgentId(agent.getTxHash()).toUpperCase();
                    //todo
                    //从账户模块获取账户别名
                    String alias = "";
                    boolean b = agentId.indexOf(keyword) >= 0;
                    b = b || agentAddress.equals(keyword) || packingAddress.equals(keyword);
                    if (StringUtils.isNotBlank(alias)) {
                        b = b || alias.toUpperCase().indexOf(keyword) >= 0;
                        agent.setAlais(alias);
                    }
                    if (!b) {
                        continue;
                    }
                }
                handleList.add(agent);
            }
            int start = pageNumber * pageSize - pageSize;
            Page<AgentDTO> page = new Page<>(pageNumber, pageSize, handleList.size());
            //表示查询的起始位置大于数据总数即查询的该页不存在数据
            if (start >= page.getTotal()) {
                return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(page);
            }
            fillAgentList(chain, handleList, null);
            //todo 是否要添加排序功能
            List<AgentDTO> resultList = new ArrayList<>();
            for (int i = start; i < handleList.size() && i < (start + pageSize); i++) {
                AgentDTO agentDTO = new AgentDTO(handleList.get(i));
                resultList.add(agentDTO);
            }
            page.setList(resultList);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(page);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        }
    }

    /**
     * 获取指定节点信息
     */
    @Override
    public Result getAgentInfo(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        SearchAgentDTO dto = JSONUtils.map2pojo(params, SearchAgentDTO.class);
        String agentHash = dto.getAgentHash();
        if (!NulsDigestData.validHash(agentHash)) {
            return Result.getFailed(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        int chainId = dto.getChainId();
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            NulsDigestData agentHashData = NulsDigestData.fromDigestHex(agentHash);
            List<Agent> agentList = chain.getAgentList();
            for (Agent agent : agentList) {
                if (agent.getTxHash().equals(agentHashData)) {
                    MeetingRound round = roundManager.getCurrentRound(chain);
                    this.fillAgent(chain, agent, round, null);
                    AgentDTO result = new AgentDTO(agent);
                    return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
                }
            }
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        }
        return Result.getFailed(ConsensusErrorCode.AGENT_NOT_EXIST);
    }

    /**
     * 获取惩罚信息
     */
    @Override
    public Result getPublishList(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        SearchPunishDTO dto = JSONUtils.map2pojo(params, SearchPunishDTO.class);
        int chainId = dto.getChainId();
        String address = dto.getAddress();
        int type = dto.getType();
        if (chainId == 0 || StringUtils.isBlank(address)) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        List<PunishLogDTO> yellowPunishList = null;
        List<PunishLogDTO> redPunishList = null;
        //查询红牌交易
        if (type != 1) {
            redPunishList = new ArrayList<>();
            for (PunishLogPo po : chain.getRedPunishList()) {
                if (StringUtils.isNotBlank(address) && !ByteUtils.arrayEquals(po.getAddress(), AddressTool.getAddress(address))) {
                    continue;
                }
                redPunishList.add(new PunishLogDTO(po));
            }
        } else if (type != 2) {
            yellowPunishList = new ArrayList<>();
            for (PunishLogPo po : chain.getYellowPunishList()) {
                if (StringUtils.isNotBlank(address) && !ByteUtils.arrayEquals(po.getAddress(), AddressTool.getAddress(address))) {
                    continue;
                }
                yellowPunishList.add(new PunishLogDTO(po));
            }
        }
        Map<String, List<PunishLogDTO>> resultMap = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
        resultMap.put("redPunish", redPunishList);
        resultMap.put("yellowPunish", yellowPunishList);
        return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(resultMap);
    }

    /**
     * 获取委托列表信息
     */
    @Override
    public Result getDepositList(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        SearchDepositDTO dto = JSONUtils.map2pojo(params, SearchDepositDTO.class);
        int pageNumber = dto.getPageNumber();
        int pageSize = dto.getPageSize();
        int chainId = dto.getChainId();
        if (pageNumber == ConsensusConstant.MIN_VALUE) {
            pageNumber = ConsensusConstant.PAGE_NUMBER_INIT_VALUE;
        }
        if (pageSize == ConsensusConstant.MIN_VALUE) {
            pageSize = ConsensusConstant.PAGE_SIZE_INIT_VALUE;
        }
        if (pageNumber < ConsensusConstant.MIN_VALUE || pageSize < ConsensusConstant.MIN_VALUE || pageSize > ConsensusConstant.PAGE_SIZE_MAX_VALUE || chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        String address = dto.getAddress();
        String agentHash = dto.getAgentHash();
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        List<Deposit> depositList = chain.getDepositList();
        List<Deposit> handleList = new ArrayList<>();
        long startBlockHeight = chain.getNewestHeader().getHeight();
        byte[] addressBytes = null;
        if (StringUtils.isNotBlank(address)) {
            addressBytes = AddressTool.getAddress(address);
        }
        for (Deposit deposit : depositList) {
            if (deposit.getDelHeight() != -1L && deposit.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (deposit.getBlockHeight() > startBlockHeight || deposit.getBlockHeight() < 0L) {
                continue;
            }
            if (addressBytes != null && !Arrays.equals(deposit.getAddress(), addressBytes)) {
                continue;
            }
            if (agentHash != null && !deposit.getAgentHash().getDigestHex().equals(agentHash)) {
                continue;
            }
            handleList.add(deposit);
        }
        int start = pageNumber * pageSize - pageSize;
        int handleSize = handleList.size();
        Page<DepositDTO> page = new Page<>(pageNumber, pageSize, handleSize);
        if (start >= handleSize) {
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(page);
        }
        List<DepositDTO> resultList = new ArrayList<>();
        for (int i = start; i < handleSize && i < (start + pageSize); i++) {
            Deposit deposit = handleList.get(i);
            List<Agent> agentList = chain.getAgentList();
            Agent agent = null;
            for (Agent a : agentList) {
                if (a.getTxHash().equals(deposit.getAgentHash())) {
                    agent = a;
                    break;
                }
            }
            deposit.setStatus(agent == null ? 0 : agent.getStatus());
            resultList.add(new DepositDTO(deposit, agent));
        }
        page.setList(resultList);
        return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(page);
    }

    /**
     * 获取全网信息
     */
    @Override
    public Result getWholeInfo(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            WholeNetConsensusInfoDTO dto = new WholeNetConsensusInfoDTO();
            List<Agent> agentList = chain.getAgentList();
            if (agentList == null) {
                return Result.getFailed(ConsensusErrorCode.DATA_NOT_EXIST);
            }
            List<Agent> handleList = new ArrayList<>();
            //获取本地最新高度
            long startBlockHeight = chain.getNewestHeader().getHeight();
            for (Agent agent : agentList) {
                if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                    continue;
                } else if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                    continue;
                }
                handleList.add(agent);
            }
            MeetingRound round = roundManager.getCurrentRound(chain);
            BigInteger totalDeposit = BigInteger.ZERO;
            int packingAgentCount = 0;
            if (null != round) {
                for (MeetingMember member : round.getMemberList()) {
                    totalDeposit = totalDeposit.add(member.getAgent().getDeposit().add(member.getAgent().getTotalDeposit()));
                    if (member.getAgent() != null) {
                        packingAgentCount++;
                    }
                }
            }
            dto.setAgentCount(handleList.size());
            dto.setTotalDeposit(String.valueOf(totalDeposit));
            dto.setConsensusAccountNumber(handleList.size());
            dto.setPackingAgentCount(packingAgentCount);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(dto);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        }
    }

    /**
     * 获取指定账户信息
     */
    @Override
    public Result getInfo(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_ADDRESS) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        String address = (String) params.get(ConsensusConstant.PARAM_ADDRESS);
        AccountConsensusInfoDTO dto = new AccountConsensusInfoDTO();
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        long startBlockHeight = chain.getNewestHeader().getHeight();
        int agentCount = 0;
        String agentHash = null;
        byte[] addressBytes = AddressTool.getAddress(address);
        List<Agent> agentList = chain.getAgentList();
        for (Agent agent : agentList) {
            if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                continue;
            } else if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                continue;
            }
            if (Arrays.equals(agent.getAgentAddress(), addressBytes)) {
                //一个账户最多只能创建一个共识节点
                agentCount = 1;
                agentHash = agent.getTxHash().getDigestHex();
                break;
            }
        }
        List<Deposit> depositList = chain.getDepositList();
        Set<NulsDigestData> agentSet = new HashSet<>();
        BigInteger totalDeposit = BigInteger.ZERO;
        for (Deposit deposit : depositList) {
            if (deposit.getDelHeight() != -1L && deposit.getDelHeight() <= startBlockHeight) {
                continue;
            }
            if (deposit.getBlockHeight() > startBlockHeight || deposit.getBlockHeight() < 0L) {
                continue;
            }
            if (!Arrays.equals(deposit.getAddress(), addressBytes)) {
                continue;
            }
            agentSet.add(deposit.getAgentHash());
            totalDeposit = totalDeposit.add(deposit.getDeposit());
        }
        dto.setAgentCount(agentCount);
        dto.setAgentHash(agentHash);
        dto.setJoinAgentCount(agentSet.size());
        //todo 统计账户奖励金

        dto.setTotalDeposit(String.valueOf(totalDeposit));
        try {
            //todo 从账本模块获取账户可用余额
        } catch (Exception e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            dto.setUsableBalance(BigIntegerUtils.ZERO);
        }
        return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(dto);
    }

    /**
     * 验证区块正确性
     */
    @Override
    public Result validBlock(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        ValidBlockDTO dto = JSONUtils.map2pojo(params, ValidBlockDTO.class);
        if (dto.getChainId() <= ConsensusConstant.MIN_VALUE || dto.getBlock() == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = dto.getChainId();
        /*
        * 0区块下载中，1接收到最新区块
        * */
        boolean isDownload = (dto.getDownload()==0);
        String blockHex = dto.getBlock();
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            Block block = new Block();
            block.parse(new NulsByteBuffer(HexUtil.decode(blockHex)));
            blockValidator.validate(isDownload, chain, block);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
    }

    /**
     * 批量验证共识模块交易
     */
    @Override
    public Result batchValid(Map<String, Object> params) {
        if (params == null || params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            List<String> txHexs = JSONUtils.json2list((String) params.get(ConsensusConstant.PARAM_TX), String.class);
            List<Transaction> txList = new ArrayList<>();
            for (String txHex : txHexs) {
                Transaction tx = new Transaction();
                tx.parse(HexUtil.decode(txHex), 0);
            }
            batchValidator.batchValid(txList, chain);
            List<String> resultTxHexs = new ArrayList<>();
            for (Transaction tx : txList) {
                resultTxHexs.add(HexUtil.encode(tx.serialize()));
            }
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(resultTxHexs);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
    }

    /**
     * 获取当前轮次信息
     */
    @Override
    public Result getCurrentRoundInfo(Map<String, Object> params) {
        if (params == null || params.get(ConsensusConstant.PARAM_CHAIN_ID) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            MeetingRound round = roundManager.getOrResetCurrentRound(chain, true);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(round);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        }
    }

    /**
     * 获取指定节点状态
     */
    @Override
    public Result getAgentStatus(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        SearchAgentDTO dto = JSONUtils.map2pojo(params, SearchAgentDTO.class);
        int chainId = dto.getChainId();
        if (dto.getChainId() <= ConsensusConstant.MIN_VALUE || dto.getAgentHash() == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        Map<String, Integer> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
        try {
            NulsDigestData agentHash = new NulsDigestData();
            agentHash.parse(new NulsByteBuffer(HexUtil.decode(dto.getAgentHash())));
            AgentPo agent = agentService.get(agentHash, chainId);
            if (agent.getDelHeight() > ConsensusConstant.MIN_VALUE) {
                result.put("status", 0);
            } else {
                result.put("status", 1);
            }
        } catch (Exception e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
        }
        return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
    }

    /**
     * 修改节点共识状态
     */
    @Override
    public Result updateAgentConsensusStatus(Map<String, Object> params) {
        if (params == null || params.get(ConsensusConstant.PARAM_CHAIN_ID) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        chain.setConsensusStatus(ConsensusStatus.RUNNING);
        return Result.getSuccess(ConsensusErrorCode.SUCCESS);
    }

    /**
     * 修改节点打包状态
     */
    @Override
    public Result updateAgentStatus(Map<String, Object> params) {
            if (params == null || params.get(ConsensusConstant.PARAM_CHAIN_ID) == null) {
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            if (chainId <= ConsensusConstant.MIN_VALUE) {
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            Chain chain = chainManager.getChainMap().get(chainId);
            if (chain == null) {
                return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
            }
            chain.setCanPacking(true);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);

    }

    /**
     * 停止一条链
     */
    @Override
    public Result stopChain(Map<String, Object> params) {
        return null;
    }

    /**
     * 启动一条新链
     */
    @Override
    public Result runChain(Map<String, Object> params) {
        return null;
    }

    @Override
    public Result runMainChain(Map<String, Object> params) {
        return null;
    }

    /**
     * 创建节点交易验证
     */
    @Override
    public Result createAgentValid(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
        if (chainId <= 0) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_REGISTER_AGENT);
            transaction.parse(HexUtil.decode(txHex), 0);
            boolean result = validatorManager.validateTx(chainId, transaction);
            if (!result) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(ConsensusErrorCode.DATA_ERROR);
        }
    }

    /**
     * 创建节点交易提交
     */
    @Override
    public Result createAgentCommit(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX) == null || params.get(ConsensusConstant.PARAM_BLOCK_HEADER) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_REGISTER_AGENT);
            transaction.parse(HexUtil.decode(txHex), 0);
            String headerHex = (String) params.get(ConsensusConstant.PARAM_BLOCK_HEADER);
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.parse(HexUtil.decode(headerHex), 0);
            Agent agent = new Agent();
            agent.parse(transaction.getTxData(), 0);
            agent.setTxHash(transaction.getHash());
            agent.setBlockHeight(blockHeader.getHeight());
            agent.setTime(transaction.getTime());
            AgentPo agentPo = agentManager.agentToPo(agent);
            if (!agentService.save(agentPo, chainId)) {
                return Result.getFailed(ConsensusErrorCode.SAVE_FAILED);
            }
            agentManager.addAgent(chain, agent);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        }
    }

    /**
     * 创建节点交易回滚
     */
    @Override
    public Result createAgentRollBack(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_REGISTER_AGENT);
            transaction.parse(HexUtil.decode(txHex), 0);
            if (!agentService.delete(transaction.getHash(), chainId)) {
                return Result.getFailed(ConsensusErrorCode.ROLLBACK_FAILED);
            }
            agentManager.removeAgent(chain, transaction.getHash());
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        }
    }

    /**
     * 停止节点交易验证
     */
    @Override
    public Result stopAgentValid(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_STOP_AGENT);
            transaction.parse(HexUtil.decode(txHex), 0);
            boolean result = validatorManager.validateTx(chainId, transaction);
            if (!result) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(ConsensusErrorCode.DATA_ERROR);
        }
    }

    /**
     * 停止节点交易提交
     */
    @Override
    public Result stopAgentCommit(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX) == null || params.get(ConsensusConstant.PARAM_BLOCK_HEADER) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_STOP_AGENT);
            transaction.parse(HexUtil.decode(txHex), 0);
            String headerHex = (String) params.get(ConsensusConstant.PARAM_BLOCK_HEADER);
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.parse(HexUtil.decode(headerHex), 0);
            if (transaction.getTime() < (blockHeader.getTime() - 300000L)) {
                return Result.getFailed(ConsensusErrorCode.LOCK_TIME_NOT_REACHED);
            }
            //找到需要注销的节点信息
            StopAgent stopAgent = new StopAgent();
            stopAgent.parse(transaction.getTxData(), 0);
            AgentPo agentPo = agentService.get(stopAgent.getCreateTxHash(), chainId);
            if (agentPo == null || agentPo.getDelHeight() > 0) {
                return Result.getFailed(ConsensusErrorCode.AGENT_NOT_EXIST);
            }
            //找到该节点的委托信息,并设置委托状态为退出
            List<DepositPo> depositPoList = depositService.getList(chainId);
            for (DepositPo depositPo : depositPoList) {
                if (depositPo.getDelHeight() > -1L) {
                    continue;
                }
                if (!depositPo.getAgentHash().equals(agentPo.getHash())) {
                    continue;
                }
                depositPo.setDelHeight(transaction.getBlockHeight());
                if (!depositService.save(depositPo, chainId)) {
                    return Result.getFailed(ConsensusErrorCode.SAVE_FAILED);
                }
                depositManager.updateDeposit(chain, depositManager.poToDeposit(depositPo));
            }
            agentPo.setDelHeight(transaction.getBlockHeight());
            //保存数据库和缓存
            if (!agentService.save(agentPo, chainId)) {
                return Result.getFailed(ConsensusErrorCode.SAVE_FAILED);
            }
            agentManager.updateAgent(chain, agentManager.poToAgent(agentPo));
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        } catch (Exception e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(ConsensusErrorCode.DATA_ERROR);
        }
    }

    /**
     * 停止节点交易回滚
     */
    @Override
    public Result stopAgentRollBack(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_STOP_AGENT);
            transaction.parse(HexUtil.decode(txHex), 0);
            StopAgent stopAgent = new StopAgent();
            stopAgent.parse(transaction.getTxData(), 0);
            AgentPo agentPo = agentService.get(stopAgent.getCreateTxHash(), chainId);
            agentPo.setDelHeight(-1);
            //找到该节点的委托信息,并设置委托状态为退出
            List<DepositPo> depositPoList = depositService.getList(chainId);
            for (DepositPo depositPo : depositPoList) {
                if (depositPo.getDelHeight() != transaction.getBlockHeight()) {
                    continue;
                }
                if (!depositPo.getAgentHash().equals(agentPo.getHash())) {
                    continue;
                }
                depositPo.setDelHeight(-1);
                if (!depositService.save(depositPo, chainId)) {
                    return Result.getFailed(ConsensusErrorCode.ROLLBACK_FAILED);
                }
                depositManager.updateDeposit(chain, depositManager.poToDeposit(depositPo));
            }
            //保存数据库和缓存
            if (!agentService.save(agentPo, chainId)) {
                return Result.getFailed(ConsensusErrorCode.ROLLBACK_FAILED);
            }
            agentManager.updateAgent(chain, agentManager.poToAgent(agentPo));
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        } catch (Exception e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(ConsensusErrorCode.DATA_ERROR);
        }
    }

    /**
     * 委托共识交易验证
     */
    @Override
    public Result depositValid(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_JOIN_CONSENSUS);
            transaction.parse(HexUtil.decode(txHex), 0);
            boolean result = validatorManager.validateTx(chainId, transaction);
            if (!result) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(ConsensusErrorCode.DATA_ERROR);
        }
    }

    /**
     * 委托共识交易提交
     */
    @Override
    public Result depositCommit(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX) == null || params.get(ConsensusConstant.PARAM_BLOCK_HEADER) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_JOIN_CONSENSUS);
            transaction.parse(HexUtil.decode(txHex), 0);
            String headerHex = (String) params.get(ConsensusConstant.PARAM_BLOCK_HEADER);
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.parse(HexUtil.decode(headerHex), 0);
            Deposit deposit = new Deposit();
            deposit.parse(transaction.getTxData(), 0);
            deposit.setTxHash(transaction.getHash());
            deposit.setTime(transaction.getTime());
            deposit.setBlockHeight(blockHeader.getHeight());
            DepositPo depositPo = depositManager.depositToPo(deposit);
            if (!depositService.save(depositPo, chainId)) {
                return Result.getFailed(ConsensusErrorCode.SAVE_FAILED);
            }
            depositManager.addDeposit(chain, deposit);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        }
    }

    /**
     * 委托共识交易回滚
     */
    @Override
    public Result depositRollBack(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_JOIN_CONSENSUS);
            transaction.parse(HexUtil.decode(txHex), 0);
            if (!depositService.delete(transaction.getHash(), chainId)) {
                return Result.getFailed(ConsensusErrorCode.ROLLBACK_FAILED);
            }
            depositManager.removeDeposit(chain, transaction.getHash());
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        } catch (Exception e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
        }
        return Result.getFailed(ConsensusErrorCode.ROLLBACK_FAILED);
    }

    /**
     * 退出共识交易验证
     */
    @Override
    public Result withdrawValid(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_CANCEL_DEPOSIT);
            transaction.parse(HexUtil.decode(txHex), 0);
            boolean result = validatorManager.validateTx(chainId, transaction);
            if (!result) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(ConsensusErrorCode.DATA_ERROR);
        }
    }

    /**
     * 退出共识交易提交
     */
    @Override
    public Result withdrawCommit(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_CANCEL_DEPOSIT);
            transaction.parse(HexUtil.decode(txHex), 0);
            CancelDeposit cancelDeposit = new CancelDeposit();
            cancelDeposit.parse(transaction.getTxData(), 0);
            //获取该笔交易对应的加入共识委托交易
            DepositPo po = depositService.get(cancelDeposit.getJoinTxHash(), chainId);
            //委托交易不存在
            if (po == null) {
                return Result.getFailed(ConsensusErrorCode.DATA_NOT_EXIST);
            }
            //委托交易已退出
            if (po.getDelHeight() > 0) {
                return Result.getFailed(ConsensusErrorCode.DEPOSIT_WAS_CANCELED);
            }
            //设置退出共识高度
            po.setDelHeight(transaction.getBlockHeight());
            if (!depositService.save(po, chainId)) {
                return Result.getFailed(ConsensusErrorCode.SAVE_FAILED);
            }
            depositManager.updateDeposit(chain, depositManager.poToDeposit(po));
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        }
    }

    /**
     * 退出共识交易回滚
     */
    @Override
    public Result withdrawRollBack(Map<String, Object> params) {
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_CANCEL_DEPOSIT);
            transaction.parse(HexUtil.decode(txHex), 0);
            CancelDeposit cancelDeposit = new CancelDeposit();
            cancelDeposit.parse(transaction.getTxData(), 0);
            //获取该笔交易对应的加入共识委托交易
            DepositPo po = depositService.get(cancelDeposit.getJoinTxHash(), chainId);
            //委托交易不存在
            if (po == null) {
                return Result.getFailed(ConsensusErrorCode.DATA_NOT_EXIST);
            }
            if (po.getDelHeight() != transaction.getBlockHeight()) {
                return Result.getFailed(ConsensusErrorCode.DEPOSIT_NEVER_CANCELED);
            }
            po.setDelHeight(-1L);
            if (!depositService.save(po, chainId)) {
                return Result.getFailed(ConsensusErrorCode.ROLLBACK_FAILED);
            }
            depositManager.updateDeposit(chain, depositManager.poToDeposit(po));
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        }
    }

    /**
     * 缓存最新区块
     */
    @Override
    public Result addBlock(Map<String,Object> params){
        if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_BLOCK_HEADER) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            String headerHex = (String) params.get(ConsensusConstant.PARAM_BLOCK_HEADER);
            BlockHeader header = new BlockHeader();
            header.parse(HexUtil.decode(headerHex),0);
            blockManager.addNewBlock(chain,header);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        }catch (NulsException e){
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e.getMessage());
            return Result.getFailed(e.getErrorCode());
        }
    }

    private void fillAgentList(Chain chain, List<Agent> agentList, List<Deposit> depositList) throws NulsException {
        MeetingRound round = roundManager.getCurrentRound(chain);
        for (Agent agent : agentList) {
            fillAgent(chain, agent, round, depositList);
        }
    }

    private void fillAgent(Chain chain, Agent agent, MeetingRound round, List<Deposit> depositList) {
        if (null == depositList || depositList.isEmpty()) {
            depositList = chain.getDepositList();
        }
        if (depositList == null || depositList.isEmpty()) {
            agent.setMemberCount(0);
            agent.setTotalDeposit(BigInteger.ZERO);
        } else {
            Set<String> memberSet = new HashSet<>();
            BigInteger total = BigInteger.ZERO;
            for (int i = 0; i < depositList.size(); i++) {
                Deposit deposit = depositList.get(i);
                if (!agent.getTxHash().equals(deposit.getAgentHash())) {
                    continue;
                }
                if (deposit.getDelHeight() >= 0) {
                    continue;
                }
                total = total.add(deposit.getDeposit());
                memberSet.add(AddressTool.getStringAddressByBytes(deposit.getAddress()));
            }
            agent.setMemberCount(memberSet.size());
            agent.setTotalDeposit(total);
        }
        if (round == null) {
            return;
        }
        MeetingMember member = round.getMember(agent.getPackingAddress());
        if (null == member) {
            agent.setStatus(0);
            return;
        }
        agent.setStatus(1);
        agent.setCreditVal(member.getAgent().getCreditVal());
    }

    private HashMap accountValid(int chainId,String address,String password)throws NulsRuntimeException{
        try {
            Map<String,Object> callParams = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            callParams.put("chainId",chainId);
            callParams.put("address",address);
            callParams.put("password",password);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr,"ac_getPriKeyByAddress", callParams);
            if(!cmdResp.isSuccess()){
                throw new NulsRuntimeException(ConsensusErrorCode.ACCOUNT_NOT_EXIST);
            }
            HashMap callResult = (HashMap)((HashMap) cmdResp.getResponseData()).get("ac_getPriKeyByAddress");
            if(callResult == null || callResult.size() == 0 || !(boolean)callResult.get(ConsensusConstant.VALID_RESULT)){
                throw new NulsRuntimeException(ConsensusErrorCode.ACCOUNT_VALID_ERROR);
            }
            return callResult;
        }catch (Exception e){
            throw new NulsRuntimeException(ConsensusErrorCode.INTERFACE_CALL_FAILED);
        }
    }

    private void transactionSignture(int chainId,String address,String password,String priKey,Transaction tx){
        try {
            P2PHKSignature p2PHKSignature = new P2PHKSignature();
            if(!StringUtils.isBlank(priKey)){
                p2PHKSignature = SignatureUtil.createSignatureByPriKey(tx,priKey);
            }
            else{
                Map<String,Object> callParams = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
                callParams.put("chainId",chainId);
                callParams.put("address",address);
                callParams.put("password",password);
                callParams.put("dataHex",HexUtil.encode(tx.getHash().getDigestBytes()));
                Response signResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr,"ac_signDigest", callParams);
                if(!signResp.isSuccess()){
                    throw new NulsRuntimeException(ConsensusErrorCode.TX_SIGNTURE_ERROR);
                }
                HashMap signResult = (HashMap)((HashMap) signResp.getResponseData()).get("ac_signDigest");
                p2PHKSignature.parse(HexUtil.decode((String)signResult.get("signatureHex")),0);
            }
            TransactionSignature signature = new TransactionSignature();
            List<P2PHKSignature>p2PHKSignatures = new ArrayList<>();
            p2PHKSignatures.add(p2PHKSignature);
            signature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(signature.serialize());
        }catch (Exception e){
            throw new NulsRuntimeException(ConsensusErrorCode.INTERFACE_CALL_FAILED);
        }
    }
}
