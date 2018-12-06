package io.nuls.poc.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
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
import io.nuls.poc.utils.manager.*;
import io.nuls.poc.utils.validator.TxValidator;
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
    /**
     * 创建节点
     */
    @Override
    public Result createAgent(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        try {
            CreateAgentDTO dto = JSONUtils.map2pojo(params, CreateAgentDTO.class);
            ObjectUtils.canNotEmpty(dto);
            ObjectUtils.canNotEmpty(dto.getChainId(), "chainId can not be null");
            ObjectUtils.canNotEmpty(dto.getAgentAddress(), "agent address can not be null");
            ObjectUtils.canNotEmpty(dto.getCommissionRate(), "commission rate can not be null");
            ObjectUtils.canNotEmpty(dto.getDeposit(), "deposit can not be null");
            ObjectUtils.canNotEmpty(dto.getPackingAddress(), "packing address can not be null");
            //1.参数验证
            if (!AddressTool.isPackingAddress(dto.getPackingAddress(), (short) dto.getChainId()) || !AddressTool.validAddress((short) dto.getChainId(), dto.getAgentAddress())) {
                throw new NulsRuntimeException(ConsensusErrorCode.ADDRESS_ERROR);
            }
            //todo 调用账户模块接口  验证账户是否正确

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
            CoinData coinData = coinDataManager.getCoinData(agent.getAgentAddress(), dto.getChainId(), dto.getAssetId(), new BigInteger(dto.getDeposit()), ConsensusConstant.CONSENSUS_LOCK_TIME, tx.size() + P2PHKSignature.SERIALIZE_LENGTH);
            tx.setCoinData(coinData.serialize());
            //todo 4.交易签名

            //todo 5.将交易发送给交易管理模块

            Map<String, Object> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            result.put("txHex", HexUtil.encode(tx.serialize()));
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        } catch (IOException io) {
            Log.error(io);
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        } catch (NulsRuntimeException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
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
        try {
            StopAgentDTO dto = JSONUtils.map2pojo(params, StopAgentDTO.class);
            ObjectUtils.canNotEmpty(dto);
            ObjectUtils.canNotEmpty(dto.getChainId(), "chainId can not be null");
            ObjectUtils.canNotEmpty(dto.getAddress(), "address can not be null");
            if (!AddressTool.validAddress((short) dto.getChainId(), dto.getAddress())) {
                throw new NulsRuntimeException(ConsensusErrorCode.ADDRESS_ERROR);
            }
            Chain chain = chainManager.getChainMap().get(dto.getChainId());
            if (chain == null) {
                throw new NulsException(ConsensusErrorCode.CHAIN_NOT_EXIST);
            }
            //todo  验证账户正确性（账户模块）
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
            CoinData coinData = coinDataManager.getStopAgentCoinData(dto.getChainId(), dto.getAssetId(), agent, TimeService.currentTimeMillis() + chain.getConfig().getStopAgentLockTime());
            tx.setCoinData(coinData.serialize());
            BigInteger fee = TransactionFeeCalculator.getMaxFee(tx.size());
            coinData.getTo().get(0).setAmount(coinData.getTo().get(0).getAmount().subtract(fee));
            //todo 交易签名
            //todo 将交易传递给交易管理模块
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException io) {
            Log.error(io);
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
        try {
            CreateDepositDTO dto = JSONUtils.map2pojo(params, CreateDepositDTO.class);
            ObjectUtils.canNotEmpty(dto);
            ObjectUtils.canNotEmpty(dto.getAddress());
            ObjectUtils.canNotEmpty(dto.getAgentHash());
            ObjectUtils.canNotEmpty(dto.getDeposit());
            if (!NulsDigestData.validHash(dto.getAgentHash())) {
                throw new NulsException(ConsensusErrorCode.AGENT_NOT_EXIST);
            }
            if (!AddressTool.validAddress((short) dto.getChainId(), dto.getAddress())) {
                throw new NulsException(ConsensusErrorCode.ADDRESS_ERROR);
            }
            //todo 账户验证（账户模块）
            Transaction tx = new Transaction(ConsensusConstant.TX_TYPE_JOIN_CONSENSUS);
            Deposit deposit = new Deposit();
            deposit.setAddress(AddressTool.getAddress(dto.getAddress()));
            deposit.setAgentHash(NulsDigestData.fromDigestHex(dto.getAgentHash()));
            deposit.setDeposit(BigIntegerUtils.stringToBigInteger(dto.getDeposit()));
            tx.setTxData(deposit.serialize());
            CoinData coinData = coinDataManager.getCoinData(deposit.getAddress(), dto.getChainId(), dto.getAssetId(), new BigInteger(dto.getDeposit()), ConsensusConstant.CONSENSUS_LOCK_TIME, tx.size() + P2PHKSignature.SERIALIZE_LENGTH);
            tx.setCoinData(coinData.serialize());
            //todo 交易签名
            //todo 将交易传递给交易管理模块
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException io) {
            Log.error(io);
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
        try {
            WithdrawDTO dto = JSONUtils.map2pojo(params, WithdrawDTO.class);
            if (!NulsDigestData.validHash(dto.getTxHash())) {
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            if (!AddressTool.validAddress((short) dto.getChainId(), dto.getAddress())) {
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            //todo 账户验证（账户模块）
            NulsDigestData hash = NulsDigestData.fromDigestHex(dto.getTxHash());
            //todo 从交易模块获取委托交易（交易模块）+ 返回数据处理
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
            CoinData coinData = coinDataManager.getUnlockCoinData(cancelDeposit.getAddress(), dto.getChainId(), dto.getAssetId(), deposit.getDeposit(), 0, cancelDepositTransaction.size() + P2PHKSignature.SERIALIZE_LENGTH);
            coinData.getFrom().get(0).setNonce(hash.getDigestBytes());
            cancelDepositTransaction.setCoinData(coinData.serialize());
            //todo 交易签名
            //todo 将交易传递给交易管理模块
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException io) {
            Log.error(io);
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
        try {
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
            if (pageNumber < ConsensusConstant.MIN_VALUE|| pageSize < ConsensusConstant.MIN_VALUE || pageSize > ConsensusConstant.PAGE_SIZE_MAX_VALUE || chainId <= ConsensusConstant.MIN_VALUE) {
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            Chain chain = chainManager.getChainMap().get(chainId);
            if (chain == null) {
                throw new NulsException(ConsensusErrorCode.CHAIN_NOT_EXIST);
            }
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
            fillAgentList(chainId, handleList, null);
            //todo 是否要添加排序功能
            List<AgentDTO> resultList = new ArrayList<>();
            for (int i = start; i < handleList.size() && i < (start + pageSize); i++) {
                AgentDTO agentDTO = new AgentDTO(handleList.get(i));
                resultList.add(agentDTO);
            }
            page.setList(resultList);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(page);
        } catch (NulsException e) {
            Log.error(e);
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
        try {
            NulsDigestData agentHashData = NulsDigestData.fromDigestHex(agentHash);
            int chainId = dto.getChainId();
            Chain chain = chainManager.getChainMap().get(chainId);
            if (chain == null) {
                throw new NulsException(ConsensusErrorCode.CHAIN_NOT_EXIST);
            }
            List<Agent> agentList = chain.getAgentList();
            for (Agent agent : agentList) {
                if (agent.getTxHash().equals(agentHashData)) {
                    MeetingRound round = roundManager.getCurrentRound(chainId);
                    this.fillAgent(chainId, agent, round, null);
                    AgentDTO result = new AgentDTO(agent);
                    return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
                }
            }
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
        return Result.getFailed(ConsensusErrorCode.AGENT_NOT_EXIST);
    }

    /**
     * 获取惩罚信息
     */
    @Override
    public Result getPublishList(Map<String, Object> params) {
        try {
            if (params == null) {
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            SearchPunishDTO dto = JSONUtils.map2pojo(params, SearchPunishDTO.class);
            int chainId = dto.getChainId();
            String address = dto.getAddress();
            int type = dto.getType();
            if(chainId == 0 || StringUtils.isBlank(address)){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            Chain chain = chainManager.getChainMap().get(chainId);
            if (chain == null) {
                throw new NulsException(ConsensusErrorCode.CHAIN_NOT_EXIST);
            }
            List<PunishLogDTO> yellowPunishList = null;
            List<PunishLogDTO> redPunishList = null;
            //查询红牌交易
            if (type != 1) {
                for (PunishLogPo po : chain.getRedPunishList()) {
                    if (StringUtils.isNotBlank(address) && !ByteUtils.arrayEquals(po.getAddress(), AddressTool.getAddress(address))) {
                        continue;
                    }
                    redPunishList.add(new PunishLogDTO(po));
                }
            } else if (type != 2) {
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
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

    /**
     * 获取委托列表信息
     */
    @Override
    public Result getDepositList(Map<String, Object> params) {
        try {
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
            if (pageNumber < ConsensusConstant.MIN_VALUE|| pageSize < ConsensusConstant.MIN_VALUE || pageSize > ConsensusConstant.PAGE_SIZE_MAX_VALUE || chainId <= ConsensusConstant.MIN_VALUE) {
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            String address = dto.getAddress();
            String agentHash = dto.getAgentHash();
            Chain chain = chainManager.getChainMap().get(chainId);
            if (chain == null) {
                throw new NulsException(ConsensusErrorCode.CHAIN_NOT_EXIST);
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
            Page<DepositDTO> page = new Page<>(pageNumber, pageSize, handleList.size());
            if (start >= handleList.size()) {
                return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(page);
            }
            List<DepositDTO> resultList = new ArrayList<>();
            for (int i = start; i < depositList.size() && i < (start + pageSize); i++) {
                Deposit deposit = depositList.get(i);
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
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

    /**
     * 获取全网信息
     */
    @Override
    public Result getWholeInfo(Map<String, Object> params) {
        try {
            if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null) {
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            Chain chain = chainManager.getChainMap().get(chainId);
            if (chain == null) {
                throw new NulsException(ConsensusErrorCode.CHAIN_NOT_EXIST);
            }
            WholeNetConsensusInfoDTO dto = new WholeNetConsensusInfoDTO();
            List<Agent> agentList = chain.getAgentList();
            if (agentList == null) {
                return Result.getFailed(ConsensusErrorCode.DATA_NOT_EXIST);
            }
            List<Agent> handleList = new ArrayList<>();
            //todo 从区块管理模块获取本地最新高度
            long startBlockHeight = 100;
            for (Agent agent : agentList) {
                if (agent.getDelHeight() != -1L && agent.getDelHeight() <= startBlockHeight) {
                    continue;
                } else if (agent.getBlockHeight() > startBlockHeight || agent.getBlockHeight() < 0L) {
                    continue;
                }
                handleList.add(agent);
            }
            MeetingRound round = roundManager.getCurrentRound(chainId);
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
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

    /**
     * 获取指定账户信息
     */
    @Override
    public Result getInfo(Map<String, Object> params) {
        try {
            if (params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_ADDRESS) == null) {
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            String address = (String) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            AccountConsensusInfoDTO dto = new AccountConsensusInfoDTO();
            Chain chain = chainManager.getChainMap().get(chainId);
            if (chain == null) {
                throw new NulsException(ConsensusErrorCode.CHAIN_NOT_EXIST);
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
                Log.error(e);
                dto.setUsableBalance(BigIntegerUtils.ZERO);
            }
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(dto);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

    /**
     * 验证区块正确性
     */
    @Override
    public Result validBlock(Map<String, Object> params) {
        try {
            if (params == null) {
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            ValidBlockDTO dto = JSONUtils.map2pojo(params, ValidBlockDTO.class);
            if(dto.getChainId() <= ConsensusConstant.MIN_VALUE || dto.getBlock() == null){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            int chainId = dto.getChainId();
            boolean isDownload = dto.isDownload();
            String blockHex = dto.getBlock();
            Chain chain = chainManager.getChainMap().get(chainId);
            if (chain == null) {
                throw new NulsException(ConsensusErrorCode.CHAIN_NOT_EXIST);
            }
            Block block = new Block();
            block.parse(new NulsByteBuffer(HexUtil.decode(blockHex)));
            BlockHeader blockHeader = block.getHeader();
            //验证梅克尔哈希
            if (!blockHeader.getMerkleHash().equals(NulsDigestData.calcMerkleDigestData(block.getTxHashList()))) {
                return Result.getFailed(ConsensusErrorCode.MERKEL_HASH_ERROR);
            }
            BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
            //todo 获取本地最新区块头,区块管理模块获取
            BlockHeader bestBlockHeader = new BlockHeader();
            BlockExtendsData bestExtendsData = new BlockExtendsData(bestBlockHeader.getExtend());
            //该区块为本地最新区块之前的区块
            if (extendsData.getRoundIndex() < bestExtendsData.getRoundIndex() || (extendsData.getRoundIndex() == bestExtendsData.getRoundIndex() && extendsData.getPackingIndexOfRound() <= bestExtendsData.getPackingIndexOfRound())) {
                Log.error("new block rounddata error, block height : " + blockHeader.getHeight() + " , hash :" + blockHeader.getHash());
                return Result.getFailed(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
            }
            MeetingRound currentRound = roundManager.getCurrentRound(chainId);
            //1.当前区块轮次 < 本地最新轮次 && 区块同步已完成
            if (isDownload && extendsData.getRoundIndex() < currentRound.getIndex()) {
                MeetingRound round = roundManager.getRoundByIndex(chainId, extendsData.getRoundIndex());
                if (round != null) {
                    currentRound = round;
                }
            }
            //标志是否有轮次信息变化
            boolean hasChangeRound = false;
            //2.当前区块轮次 > 本地最新轮次
            if (extendsData.getRoundIndex() > currentRound.getIndex()) {
                //未来区块
                if (extendsData.getRoundStartTime() > TimeService.currentTimeMillis() + chain.getConfig().getPackingInterval()) {
                    Log.error("block height " + blockHeader.getHeight() + " round startTime is error, greater than current time! hash :" + blockHeader.getHash());
                    return Result.getFailed(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
                }
                if (!isDownload && (extendsData.getRoundStartTime() + (extendsData.getPackingIndexOfRound() - 1) * chain.getConfig().getPackingInterval()) > TimeService.currentTimeMillis() + chain.getConfig().getPackingInterval()) {
                    Log.error("block height " + blockHeader.getHeight() + " is the block of the future and received in advance! hash :" + blockHeader.getHash());
                    return Result.getFailed(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
                }
                if (extendsData.getRoundStartTime() < currentRound.getEndTime()) {
                    Log.error("block height " + blockHeader.getHeight() + " round index and start time not match! hash :" + blockHeader.getHash());
                    return Result.getFailed(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
                }
                MeetingRound tempRound = roundManager.getRound(chainId, extendsData, !isDownload);
                if (tempRound.getIndex() > currentRound.getIndex()) {
                    tempRound.setPreRound(currentRound);
                    hasChangeRound = true;
                }
                currentRound = tempRound;
            } else if (extendsData.getRoundIndex() < currentRound.getIndex()) {
                MeetingRound preRound = currentRound.getPreRound();
                while (preRound != null) {
                    if (extendsData.getRoundIndex() == preRound.getIndex()) {
                        currentRound = preRound;
                        break;
                    }
                    preRound = preRound.getPreRound();
                }
            }
            if (extendsData.getRoundIndex() != currentRound.getIndex() || extendsData.getRoundStartTime() != currentRound.getStartTime()) {
                Log.error("block height " + blockHeader.getHeight() + " round startTime is error! hash :" + blockHeader.getHash());
                return Result.getFailed(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
            }
            if (extendsData.getConsensusMemberCount() != currentRound.getMemberCount()) {
                Log.error("block height " + blockHeader.getHeight() + " packager count is error! hash :" + blockHeader.getHash());
                return Result.getFailed(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
            }
            Log.debug(currentRound.toString());
            // 验证打包人是否正确
            MeetingMember member = currentRound.getMember(extendsData.getPackingIndexOfRound());
            if (!Arrays.equals(member.getAgent().getPackingAddress(), blockHeader.getPackingAddress())) {
                Log.error("block height " + blockHeader.getHeight() + " packager error! hash :" + blockHeader.getHash());
                return Result.getFailed(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
            }
            if (member.getPackEndTime() != block.getHeader().getTime()) {
                Log.error("block height " + blockHeader.getHeight() + " time error! hash :" + blockHeader.getHash());
                return Result.getFailed(ConsensusErrorCode.BLOCK_ROUND_VALIDATE_ERROR);
            }
            if (hasChangeRound) {
                roundManager.addRound(chainId, currentRound);
            }
            //系统交易验证
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

    /**
     * 批量验证共识模块交易
     */
    @Override
    public Result batchValid(Map<String, Object> params) {
        return null;
    }

    /**
     * 获取当前轮次信息
     */
    @Override
    public Result getCurrentRoundInfo(Map<String, Object> params) {
        try {
            if (params == null || params.get(ConsensusConstant.PARAM_CHAIN_ID) == null) {
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            if(chainId <= ConsensusConstant.MIN_VALUE){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            MeetingRound round = roundManager.getOrResetCurrentRound(chainId, true);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(round);
        }catch (NulsException e){
            Log.error(e);
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
        if (dto.getChainId() <= ConsensusConstant.MIN_VALUE || dto.getAgentHash() == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Map<String, Integer> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
        try {
            NulsDigestData agentHash = new NulsDigestData();
            agentHash.parse(new NulsByteBuffer(HexUtil.decode(dto.getAgentHash())));
            AgentPo agent = agentService.get(agentHash, dto.getChainId());
            if (agent.getDelHeight() > ConsensusConstant.MIN_VALUE) {
                result.put("status", 0);
            } else {
                result.put("status", 1);
            }
        } catch (Exception e) {
            Log.error(e);
        }
        return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
    }

    /**
     * 修改节点状态
     */
    @Override
    public Result updateAgentStatus(Map<String, Object> params) {
        try {
            if (params == null || params.get(ConsensusConstant.PARAM_CHAIN_ID) == null) {
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            if(chainId<=ConsensusConstant.MIN_VALUE){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            Chain chain = chainManager.getChainMap().get(chainId);
            if (chain == null) {
                throw new NulsException(ConsensusErrorCode.CHAIN_NOT_EXIST);
            }
            chain.setCanPacking(true);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        }catch (NulsException e){
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
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
        try {
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            if(chainId <= 0){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_REGISTER_AGENT);
            transaction.parse(HexUtil.decode(txHex), 0);
            boolean result = validatorManager.validateTx(chainId, transaction);
            if (!result) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException et) {
            Log.error(et);
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
        try {
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            if(chainId <= ConsensusConstant.MIN_VALUE){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
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
            agentManager.addAgent(chainId,agent);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
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
        try {
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            if(chainId <= ConsensusConstant.MIN_VALUE){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_REGISTER_AGENT);
            transaction.parse(HexUtil.decode(txHex), 0);
            if (!agentService.delete(transaction.getHash(), chainId)) {
                return Result.getFailed(ConsensusErrorCode.ROLLBACK_FAILED);
            }
            agentManager.removeAgent(chainId,transaction.getHash());
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
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
        try {
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            if(chainId <= ConsensusConstant.MIN_VALUE){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_STOP_AGENT);
            transaction.parse(HexUtil.decode(txHex), 0);
            boolean result = validatorManager.validateTx(chainId, transaction);
            if (!result) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException et) {
            Log.error(et);
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
        try {
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            if(chainId <= ConsensusConstant.MIN_VALUE){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
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
                if(!depositService.save(depositPo, chainId)){
                    return Result.getFailed(ConsensusErrorCode.SAVE_FAILED);
                }
                depositManager.updateDeposit(chainId,depositManager.poToDeposit(depositPo));
            }
            agentPo.setDelHeight(transaction.getBlockHeight());
            //保存数据库和缓存
            if (!agentService.save(agentPo, chainId)) {
                return Result.getFailed(ConsensusErrorCode.SAVE_FAILED);
            }
            agentManager.updateAgent(chainId,agentManager.poToAgent(agentPo));
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (Exception et) {
            Log.error(et);
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
        try {
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            if(chainId <= ConsensusConstant.MIN_VALUE){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
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
                if(!depositService.save(depositPo, chainId)){
                    return Result.getFailed(ConsensusErrorCode.ROLLBACK_FAILED);
                }
                depositManager.updateDeposit(chainId,depositManager.poToDeposit(depositPo));
            }
            //保存数据库和缓存
            if (!agentService.save(agentPo, chainId)) {
                return Result.getFailed(ConsensusErrorCode.ROLLBACK_FAILED);
            }
            agentManager.updateAgent(chainId,agentManager.poToAgent(agentPo));
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (Exception et) {
            Log.error(et);
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
        try {
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            if(chainId <= ConsensusConstant.MIN_VALUE){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_JOIN_CONSENSUS);
            transaction.parse(HexUtil.decode(txHex), 0);
            boolean result = validatorManager.validateTx(chainId, transaction);
            if (!result) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException et) {
            Log.error(et);
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
        try {
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            if(chainId <= ConsensusConstant.MIN_VALUE){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
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
            depositManager.addDeposit(chainId,deposit);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
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
        try {
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            if(chainId <= ConsensusConstant.MIN_VALUE){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_JOIN_CONSENSUS);
            transaction.parse(HexUtil.decode(txHex), 0);
            if (!depositService.delete(transaction.getHash(), chainId)) {
                return Result.getFailed(ConsensusErrorCode.ROLLBACK_FAILED);
            }
            depositManager.removeDeposit(chainId,transaction.getHash());
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }catch (Exception ep){
            Log.error(ep);
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
        try {
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            if(chainId <= ConsensusConstant.MIN_VALUE){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            String txHex = (String) params.get(ConsensusConstant.PARAM_TX);
            Transaction transaction = new Transaction(ConsensusConstant.TX_TYPE_CANCEL_DEPOSIT);
            transaction.parse(HexUtil.decode(txHex), 0);
            boolean result = validatorManager.validateTx(chainId, transaction);
            if (!result) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException et) {
            Log.error(et);
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
        try {
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            if(chainId <= ConsensusConstant.MIN_VALUE){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
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
            depositManager.updateDeposit(chainId,depositManager.poToDeposit(po));
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
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
        try {
            int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
            if(chainId <= ConsensusConstant.MIN_VALUE){
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
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
            depositManager.updateDeposit(chainId,depositManager.poToDeposit(po));
            return Result.getSuccess(ConsensusErrorCode.SUCCESS);
        } catch (NulsException e) {
            Log.error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

    private void fillAgentList(int chainId, List<Agent> agentList, List<Deposit> depositList) throws NulsException{
        MeetingRound round = roundManager.getCurrentRound(chainId);
        for (Agent agent : agentList) {
            fillAgent(chainId, agent, round, depositList);
        }
    }

    private void fillAgent(int chainId, Agent agent, MeetingRound round, List<Deposit> depositList){
        Chain chain = chainManager.getChainMap().get(chainId);
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
}
