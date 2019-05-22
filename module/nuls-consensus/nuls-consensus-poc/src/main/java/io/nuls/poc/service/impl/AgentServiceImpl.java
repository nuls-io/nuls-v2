package io.nuls.poc.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.CoinData;
import io.nuls.core.basic.Page;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.core.core.annotation.Component;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.round.MeetingMember;
import io.nuls.poc.model.bo.round.MeetingRound;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.StopAgent;
import io.nuls.poc.model.dto.input.CreateAgentDTO;
import io.nuls.poc.model.dto.input.SearchAgentDTO;
import io.nuls.poc.model.dto.input.SearchAllAgentDTO;
import io.nuls.poc.model.dto.input.StopAgentDTO;
import io.nuls.poc.model.dto.output.AgentDTO;
import io.nuls.poc.model.po.AgentPo;
import io.nuls.poc.rpc.call.CallMethodUtils;
import io.nuls.poc.service.AgentService;
import io.nuls.poc.storage.AgentStorageService;
import io.nuls.poc.utils.enumeration.ConsensusStatus;
import io.nuls.poc.utils.manager.AgentManager;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.poc.utils.manager.CoinDataManager;
import io.nuls.poc.utils.manager.RoundManager;
import io.nuls.poc.utils.validator.TxValidator;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.rpc.util.TimeUtils;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.Log;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.ObjectUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;

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
@Component
public class AgentServiceImpl implements AgentService {

    @Autowired
    private AgentStorageService agentService;

    @Autowired
    private ChainManager chainManager;

    @Autowired
    private CoinDataManager coinDataManager;

    @Autowired
    private TxValidator validatorManager;

    @Autowired
    private AgentManager agentManager;

    @Autowired
    private RoundManager roundManager;

    /**
     * 创建节点
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result createAgent(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        CreateAgentDTO dto = JSONUtils.map2pojo(params, CreateAgentDTO.class);
        try {
            ObjectUtils.canNotEmpty(dto);
            ObjectUtils.canNotEmpty(dto.getChainId(), "chainId can not be null");
            ObjectUtils.canNotEmpty(dto.getAgentAddress(), "agent address can not be null");
            ObjectUtils.canNotEmpty(dto.getCommissionRate(), "commission rate can not be null");
            ObjectUtils.canNotEmpty(dto.getDeposit(), "deposit can not be null");
            ObjectUtils.canNotEmpty(dto.getPackingAddress(), "packing address can not be null");
        } catch (RuntimeException e) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }

        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            Log.error(ConsensusErrorCode.CHAIN_NOT_EXIST.getMsg());
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            //1.参数验证
            if (!AddressTool.isNormalAddress(dto.getPackingAddress(), (short) dto.getChainId())) {
                throw new NulsRuntimeException(ConsensusErrorCode.ADDRESS_ERROR);
            }
            //2.账户验证
            HashMap callResult = CallMethodUtils.accountValid(dto.getChainId(), dto.getAgentAddress(), dto.getPassword());
            //3.组装创建节点交易
            Transaction tx = new Transaction(TxType.REGISTER_AGENT);
            tx.setTime(TimeUtils.getCurrentTimeSeconds());
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
            String priKey = (String) callResult.get("priKey");
            CallMethodUtils.transactionSignature(dto.getChainId(), dto.getAgentAddress(), dto.getPassword(), priKey, tx);
            String txStr = RPCUtil.encode(tx.serialize());
            boolean validResult = validatorManager.validateTx(chain, tx);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            /*boolean validResult = CallMethodUtils.transactionBasicValid(chain,txStr);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            validResult = validatorManager.validateTx(chain, tx);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }*/
            CallMethodUtils.sendTx(chain, txStr);
            Map<String, Object> result = new HashMap<>(2);
            result.put("txHash", HashUtil.toHex(tx.getHash()));
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        } catch (IOException e) {
            chain.getLogger().error(e);
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (Exception e) {
            chain.getLogger().error(e);
            return Result.getFailed(ConsensusErrorCode.INTERFACE_CALL_FAILED);
        }
    }

    /**
     * 创建节点交易验证
     */
    @Override
    @SuppressWarnings("unchecked")
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
            Transaction transaction = new Transaction(TxType.REGISTER_AGENT);
            transaction.parse(RPCUtil.decode(txHex), 0);
            boolean result = validatorManager.validateTx(chain, transaction);
            if (!result) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            Map<String, Object> validResult = new HashMap<>(2);
            validResult.put("value", true);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(validResult);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLogger().error(e);
            return Result.getFailed(ConsensusErrorCode.DATA_ERROR);
        }
    }

    /**
     * 停止节点
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result stopAgent(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        StopAgentDTO dto = JSONUtils.map2pojo(params, StopAgentDTO.class);
        try {
            ObjectUtils.canNotEmpty(dto);
            ObjectUtils.canNotEmpty(dto.getChainId(), "chainId can not be null");
            ObjectUtils.canNotEmpty(dto.getAddress(), "address can not be null");
        } catch (RuntimeException e) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        if (!AddressTool.validAddress((short) dto.getChainId(), dto.getAddress())) {
            throw new NulsRuntimeException(ConsensusErrorCode.ADDRESS_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            HashMap callResult = CallMethodUtils.accountValid(dto.getChainId(), dto.getAddress(), dto.getPassword());
            Transaction tx = new Transaction(TxType.STOP_AGENT);
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
            tx.setTime(TimeUtils.getCurrentTimeSeconds());
            CoinData coinData = coinDataManager.getStopAgentCoinData(chain, agent, TimeUtils.getCurrentTimeSeconds() + chain.getConfig().getStopAgentLockTime());
            BigInteger fee = TransactionFeeCalculator.getNormalTxFee(tx.size() + P2PHKSignature.SERIALIZE_LENGTH + coinData.serialize().length);
            coinData.getTo().get(0).setAmount(coinData.getTo().get(0).getAmount().subtract(fee));
            tx.setCoinData(coinData.serialize());
            //交易签名
            String priKey = (String) callResult.get("priKey");
            CallMethodUtils.transactionSignature(dto.getChainId(), dto.getAddress(), dto.getPassword(), priKey, tx);
            String txStr = RPCUtil.encode(tx.serialize());
            boolean validResult = validatorManager.validateTx(chain, tx);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            /*boolean validResult = CallMethodUtils.transactionBasicValid(chain,txStr);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            validResult = validatorManager.validateTx(chain, tx);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }*/
            CallMethodUtils.sendTx(chain, txStr);
            Map<String, Object> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            result.put("txHash", HashUtil.toHex(tx.getHash()));
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLogger().error(e);
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
    }


    /**
     * 停止节点交易验证
     */
    @Override
    @SuppressWarnings("unchecked")
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
            Transaction transaction = new Transaction(TxType.STOP_AGENT);
            transaction.parse(RPCUtil.decode(txHex), 0);
            boolean result = validatorManager.validateTx(chain, transaction);
            if (!result) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            Map<String, Object> validResult = new HashMap<>(2);
            validResult.put("value", true);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(validResult);
        } catch (NulsException e) {
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLogger().error(e);
            return Result.getFailed(ConsensusErrorCode.DATA_ERROR);
        }
    }

    /**
     * 获取节点列表信息
     */
    @Override
    @SuppressWarnings("unchecked")
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
                //从账户模块获取账户别名
                String agentAlias = CallMethodUtils.getAlias(chain, agentAddress);
                String packingAlias = CallMethodUtils.getAlias(chain, packingAddress);
                boolean b = agentId.contains(keyword);
                b = b || agentAddress.equals(keyword) || packingAddress.equals(keyword);
                if (StringUtils.isNotBlank(agentAlias)) {
                    b = b || agentAlias.toUpperCase().contains(keyword);
                    agent.setAlais(agentAlias);
                }
                if (!b && StringUtils.isNotBlank(packingAlias)) {
                    b = agentAlias.toUpperCase().contains(keyword);
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
        agentManager.fillAgentList(chain, handleList, null);
        List<AgentDTO> resultList = new ArrayList<>();
        for (int i = start; i < handleList.size() && i < (start + pageSize); i++) {
            AgentDTO agentDTO = new AgentDTO(handleList.get(i));
            resultList.add(agentDTO);
        }
        page.setList(resultList);
        return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(page);
    }

    /**
     * 获取指定节点信息
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result getAgentInfo(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        SearchAgentDTO dto = JSONUtils.map2pojo(params, SearchAgentDTO.class);
        String agentHash = dto.getAgentHash();
        if (!HashUtil.validHash(agentHash)) {
            return Result.getFailed(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        int chainId = dto.getChainId();
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        byte[] agentHashData = HashUtil.toBytes(agentHash);
        List<Agent> agentList = chain.getAgentList();
        for (Agent agent : agentList) {
            if (HashUtil.equals(agent.getTxHash(), agentHashData)) {
                MeetingRound round = roundManager.getCurrentRound(chain);
                if (agent.getDelHeight() == -1) {
                    agentManager.fillAgent(chain, agent, round, null);
                } else {
                    agent.setMemberCount(0);
                    agent.setTotalDeposit(BigInteger.ZERO);
                    agent.setStatus(0);
                    agent.setCreditVal(0);
                }
                AgentDTO result = new AgentDTO(agent);
                return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
            }
        }
        return Result.getFailed(ConsensusErrorCode.AGENT_NOT_EXIST);
    }

    /**
     * 获取指定节点状态
     */
    @Override
    @SuppressWarnings("unchecked")
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
            byte[] agentHash = HashUtil.toBytes(dto.getAgentHash());
            AgentPo agent = agentService.get(agentHash, chainId);
            if (agent.getDelHeight() > ConsensusConstant.MIN_VALUE) {
                result.put("status", 0);
            } else {
                result.put("status", 1);
            }
        } catch (Exception e) {
            chain.getLogger().error(e);
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
        chain.getLogger().debug("updateAgentConsensusStatus-修改节点共识状态成功......");
        return Result.getSuccess(ConsensusErrorCode.SUCCESS);
    }

    /**
     * 修改节点打包状态
     */
    @Override
    public Result updateAgentStatus(Map<String, Object> params) {
        if (params == null || params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_STATUS) == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        int chainId = (Integer) params.get(ConsensusConstant.PARAM_CHAIN_ID);
        int status = (Integer) params.get(ConsensusConstant.PARAM_STATUS);
        if (chainId <= ConsensusConstant.MIN_VALUE) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        if (status == 1) {
            chain.setCanPacking(true);
            chain.getLogger().debug("updateAgentStatus--节点打包状态修改成功，修改后状态为：可打包状态");
        } else {
            chain.setCanPacking(false);
            chain.getLogger().debug("updateAgentStatus--节点打包状态修改成功，修改后状态为：不可打包状态");
        }
        return Result.getSuccess(ConsensusErrorCode.SUCCESS);

    }

    /**
     * 获取当前节点出块地址
     *
     * @param params
     * @return Result
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result getNodePackingAddress(Map<String, Object> params) {
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
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.AC.abbr, "ac_getUnencryptedAddressList", params);
            List<String> accountAddressList = (List<String>) ((HashMap) ((HashMap) cmdResp.getResponseData()).get("ac_getUnencryptedAddressList")).get("list");
            List<Agent> workAgentList = chain.getWorkAgentList(chain.getNewestHeader().getHeight());
            String packAddress = null;
            for (Agent agent : workAgentList) {
                String address = AddressTool.getStringAddressByBytes(agent.getPackingAddress());
                if (accountAddressList.contains(address)) {
                    packAddress = address;
                    break;
                }
            }
            Map<String, Object> resultMap = new HashMap<>(2);
            resultMap.put("packAddress", packAddress);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(resultMap);
        } catch (Exception e) {
            chain.getLogger().error(e);
            return Result.getFailed(ConsensusErrorCode.DATA_ERROR);
        }
    }

    /**
     * 获取所有节点出块地址/指定N个区块出块指定
     *
     * @param params
     * @return Result
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result getAgentAddressList(Map<String, Object> params) {
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
        Map<String, Object> resultMap = new HashMap<>(2);
        resultMap.put("packAddress", chain.getWorkAddressList(chain.getNewestHeader().getHeight()));
        return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(resultMap);
    }

    /**
     * 获取当前节点的出块账户信息
     *
     * @param params
     * @return Result
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result getPackerInfo(Map<String, Object> params) {
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
            MeetingRound round = roundManager.resetRound(chain, true);
            MeetingMember member = round.getMyMember();
            Map<String, Object> resultMap = new HashMap<>(4);
            if (member != null) {
                resultMap.put("address", AddressTool.getStringAddressByBytes(member.getAgent().getPackingAddress()));
                resultMap.put("password", chain.getConfig().getPassword());
            }
            List<String> packAddressList = new ArrayList<>();
            for (MeetingMember meetingMember : round.getMemberList()) {
                packAddressList.add(AddressTool.getStringAddressByBytes(meetingMember.getAgent().getPackingAddress()));
            }
            resultMap.put("packAddressList", packAddressList);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(resultMap);
        } catch (Exception e) {
            chain.getLogger().error(e);
            return Result.getFailed(ConsensusErrorCode.DATA_ERROR);
        }
    }
}
