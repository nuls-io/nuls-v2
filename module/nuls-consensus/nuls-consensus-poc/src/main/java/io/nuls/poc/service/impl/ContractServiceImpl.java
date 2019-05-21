package io.nuls.poc.service.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.HashUtil;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.util.RPCUtil;
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
import io.nuls.poc.model.po.AgentPo;
import io.nuls.poc.model.po.DepositPo;
import io.nuls.poc.rpc.call.CallMethodUtils;
import io.nuls.poc.service.ContractService;
import io.nuls.poc.storage.AgentStorageService;
import io.nuls.poc.storage.DepositStorageService;
import io.nuls.poc.utils.manager.AgentManager;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.poc.utils.manager.CoinDataManager;
import io.nuls.poc.utils.manager.RoundManager;
import io.nuls.poc.utils.validator.TxValidator;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * 智能合约与共识交互接口实现类
 *
 * @author tag
 * 2019/5/5
 */
@Component
public class ContractServiceImpl implements ContractService {
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private CoinDataManager coinDataManager;
    @Autowired
    private TxValidator validatorManager;
    @Autowired
    private RoundManager roundManager;
    @Autowired
    private AgentManager agentManager;
    @Autowired
    private AgentStorageService agentStorageService;
    @Autowired
    private DepositStorageService depositStorageService;

    @Override
    @SuppressWarnings("unchecked")
    public Result createAgent(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        ContractAgentDTO dto = JSONUtils.map2pojo(params, ContractAgentDTO.class);
        int chainId = dto.getChainId();
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            Log.error(ConsensusErrorCode.CHAIN_NOT_EXIST.getMsg());
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        if (!AddressTool.validContractAddress(AddressTool.getAddress(dto.getContractAddress()), dto.getChainId())
                || !AddressTool.validAddress(dto.getChainId(), dto.getContractSender())) {
            return Result.getFailed(ConsensusErrorCode.ADDRESS_ERROR);
        }

        Transaction tx = new Transaction(TxType.CONTRACT_CREATE_AGENT);
        tx.setTime(dto.getBlockTime());
        Agent agent = new Agent();
        agent.setAgentAddress(AddressTool.getAddress(dto.getContractAddress()));
        agent.setPackingAddress(AddressTool.getAddress(dto.getPackingAddress()));
        agent.setRewardAddress(agent.getAgentAddress());
        agent.setDeposit(BigIntegerUtils.stringToBigInteger(dto.getDeposit()));
        agent.setCommissionRate(Byte.valueOf(dto.getCommissionRate()));
        try {
            tx.setTxData(agent.serialize());
            CoinData coinData = coinDataManager.getContractCoinData(agent.getAgentAddress(), chain, new BigInteger(dto.getDeposit()), ConsensusConstant.CONSENSUS_LOCK_TIME, RPCUtil.decode(dto.getContractNonce()), new BigInteger(dto.getContractBalance()));
            tx.setCoinData(coinData.serialize());
            boolean validResult = validatorManager.validateTx(chain, tx);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            Map<String, Object> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            List<String> value = new ArrayList<>();
            value.add(HashUtil.toHex(tx.getHash()));
            value.add(RPCUtil.encode(tx.serialize()));
            result.put(ConsensusConstant.PARAM_RESULT_VALUE, value);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            return Result.getFailed(e.getErrorCode());
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result stopAgent(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        ContractStopAgentDTO dto = JSONUtils.map2pojo(params, ContractStopAgentDTO.class);
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            Log.error(ConsensusErrorCode.CHAIN_NOT_EXIST.getMsg());
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        if (!AddressTool.validContractAddress(AddressTool.getAddress(dto.getContractAddress()), dto.getChainId())
                || !AddressTool.validAddress(dto.getChainId(), dto.getContractSender())) {
            return Result.getFailed(ConsensusErrorCode.ADDRESS_ERROR);
        }
        Transaction tx = new Transaction(TxType.CONTRACT_STOP_AGENT);
        StopAgent stopAgent = new StopAgent();
        stopAgent.setAddress(AddressTool.getAddress(dto.getContractAddress()));
        List<Agent> agentList = chain.getAgentList();
        Agent agent = null;
        for (Agent a : agentList) {
            if (a.getDelHeight() > 0) {
                continue;
            }
            if (Arrays.equals(a.getAgentAddress(), AddressTool.getAddress(dto.getContractAddress()))) {
                agent = a;
                break;
            }
        }
        if (agent == null || agent.getDelHeight() > 0) {
            return Result.getFailed(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        try {
            stopAgent.setCreateTxHash(agent.getTxHash());
            tx.setTxData(stopAgent.serialize());
            tx.setTime(dto.getBlockTime());
            CoinData coinData = coinDataManager.getStopAgentCoinData(chain, agent, tx.getTime() + chain.getConfig().getStopAgentLockTime());
            tx.setCoinData(coinData.serialize());
            boolean validResult = validatorManager.validateTx(chain, tx);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            Map<String, Object> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            List<String> value = new ArrayList<>();
            value.add(HashUtil.toHex(tx.getHash()));
            value.add(RPCUtil.encode(tx.serialize()));
            result.put(ConsensusConstant.PARAM_RESULT_VALUE, value);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result depositToAgent(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        ContractDepositDTO dto = JSONUtils.map2pojo(params, ContractDepositDTO.class);
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            Log.error(ConsensusErrorCode.CHAIN_NOT_EXIST.getMsg());
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        if (!HashUtil.validHash(dto.getAgentHash())) {
            return Result.getFailed(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        if (!AddressTool.validContractAddress(AddressTool.getAddress(dto.getContractAddress()), dto.getChainId())
                || !AddressTool.validAddress(dto.getChainId(), dto.getContractSender())) {
            return Result.getFailed(ConsensusErrorCode.ADDRESS_ERROR);
        }
        Transaction tx = new Transaction(TxType.CONTRACT_DEPOSIT);
        Deposit deposit = new Deposit();
        deposit.setAddress(AddressTool.getAddress(dto.getContractAddress()));
        deposit.setDeposit(BigIntegerUtils.stringToBigInteger(dto.getDeposit()));
        try {
            deposit.setAgentHash(HashUtil.toBytes(dto.getAgentHash()));
            tx.setTxData(deposit.serialize());
            tx.setTime(dto.getBlockTime());
            CoinData coinData = coinDataManager.getContractCoinData(deposit.getAddress(), chain, new BigInteger(dto.getDeposit()), ConsensusConstant.CONSENSUS_LOCK_TIME, RPCUtil.decode(dto.getContractNonce()), new BigInteger(dto.getContractBalance()));
            tx.setCoinData(coinData.serialize());
            boolean validResult = validatorManager.validateTx(chain, tx);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            Map<String, Object> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            List<String> value = new ArrayList<>();
            value.add(HashUtil.toHex(tx.getHash()));
            value.add(RPCUtil.encode(tx.serialize()));
            result.put(ConsensusConstant.PARAM_RESULT_VALUE, value);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result withdraw(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        ContractWithdrawDTO dto = JSONUtils.map2pojo(params, ContractWithdrawDTO.class);
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            Log.error(ConsensusErrorCode.CHAIN_NOT_EXIST.getMsg());
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        if (!HashUtil.validHash(dto.getJoinAgentHash())) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        if (!AddressTool.validContractAddress(AddressTool.getAddress(dto.getContractAddress()), dto.getChainId())
                || !AddressTool.validAddress(dto.getChainId(), dto.getContractSender())) {
            return Result.getFailed(ConsensusErrorCode.ADDRESS_ERROR);
        }
        try {
            byte[] hash = HashUtil.toBytes(dto.getJoinAgentHash());
            Transaction depositTransaction = CallMethodUtils.getTransaction(chain, dto.getJoinAgentHash());
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
            Transaction cancelDepositTransaction = new Transaction(TxType.CONTRACT_CANCEL_DEPOSIT);
            CancelDeposit cancelDeposit = new CancelDeposit();
            cancelDeposit.setAddress(AddressTool.getAddress(dto.getContractAddress()));
            cancelDeposit.setJoinTxHash(hash);
            cancelDepositTransaction.setTxData(cancelDeposit.serialize());
            CoinData coinData = coinDataManager.getContractUnlockCoinData(cancelDeposit.getAddress(), chain, deposit.getDeposit(), 0);
            coinData.getFrom().get(0).setNonce(CallMethodUtils.getNonce(hash));
            cancelDepositTransaction.setCoinData(coinData.serialize());
            cancelDepositTransaction.setTime(dto.getBlockTime());
            boolean validResult = validatorManager.validateTx(chain, cancelDepositTransaction);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            Map<String, Object> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            List<String> value = new ArrayList<>();
            value.add(HashUtil.toHex(cancelDepositTransaction.getHash()));
            value.add(RPCUtil.encode(cancelDepositTransaction.serialize()));
            result.put(ConsensusConstant.PARAM_RESULT_VALUE, value);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            return Result.getFailed(e.getErrorCode());
        } catch (IOException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result getAgentInfo(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        SearchContractAgentDTO dto = JSONUtils.map2pojo(params, SearchContractAgentDTO.class);
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            Log.error(ConsensusErrorCode.CHAIN_NOT_EXIST.getMsg());
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        if (!HashUtil.validHash(dto.getAgentHash())) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        String contractSender = dto.getContractSender();
        if (StringUtils.isNotBlank(contractSender) && !AddressTool.validAddress(dto.getChainId(), contractSender)) {
            return Result.getFailed(ConsensusErrorCode.ADDRESS_ERROR);
        }
        if (!AddressTool.validContractAddress(AddressTool.getAddress(dto.getContractAddress()), dto.getChainId())) {
            return Result.getFailed(ConsensusErrorCode.ADDRESS_ERROR);
        }


        byte[] agentHashData = HashUtil.toBytes(dto.getAgentHash());
        List<Agent> agentList = chain.getAgentList();
        for (Agent agent : agentList) {
            if (agent.getTxHash().equals(agentHashData)) {
                Map<String, Object> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
                List<String> value = new ArrayList<>();
                value.add(AddressTool.getStringAddressByBytes(agent.getAgentAddress()));
                value.add(AddressTool.getStringAddressByBytes(agent.getPackingAddress()));
                value.add(AddressTool.getStringAddressByBytes(agent.getRewardAddress()));
                value.add(agent.getDeposit().toString());
                value.add(String.valueOf(agent.getCommissionRate()));
                value.add(String.valueOf(agent.getTime()));
                value.add(String.valueOf(agent.getBlockHeight()));
                value.add(String.valueOf(agent.getDelHeight()));
                MeetingRound round = roundManager.getCurrentRound(chain);
                if (round != null && round.getOnlyMember(agent.getPackingAddress(), chain) != null) {
                    value.add(String.valueOf(1));
                } else {
                    value.add(String.valueOf(agent.getStatus()));
                }
                result.put(ConsensusConstant.PARAM_RESULT_VALUE, value);
                return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
            }
        }

        return Result.getFailed(ConsensusErrorCode.AGENT_NOT_EXIST);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result getDepositInfo(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        SearchContractDepositDTO dto = JSONUtils.map2pojo(params, SearchContractDepositDTO.class);
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            Log.error(ConsensusErrorCode.CHAIN_NOT_EXIST.getMsg());
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        if (!HashUtil.validHash(dto.getJoinAgentHash())) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        String contractSender = dto.getContractSender();
        if (StringUtils.isNotBlank(contractSender) && !AddressTool.validAddress(dto.getChainId(), contractSender)) {
            return Result.getFailed(ConsensusErrorCode.ADDRESS_ERROR);
        }
        if (!AddressTool.validContractAddress(AddressTool.getAddress(dto.getContractAddress()), dto.getChainId())) {
            return Result.getFailed(ConsensusErrorCode.ADDRESS_ERROR);
        }
        byte[] hash = HashUtil.toBytes(dto.getJoinAgentHash());
        DepositPo deposit = depositStorageService.get(hash, chain.getConfig().getChainId());
        Map<String, Object> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
        List<String> value = new ArrayList<>();
        value.add(HashUtil.toHex(deposit.getAgentHash()));
        AgentPo agentPo = agentStorageService.get(deposit.getAgentHash(), chain.getConfig().getChainId());
        value.add(AddressTool.getStringAddressByBytes(agentPo.getAgentAddress()));
        value.add(AddressTool.getStringAddressByBytes(deposit.getAddress()));
        value.add(deposit.getDeposit().toString());
        value.add(String.valueOf(deposit.getTime()));
        value.add(String.valueOf(deposit.getBlockHeight()));
        value.add(String.valueOf(deposit.getDelHeight()));
        MeetingRound round = roundManager.getCurrentRound(chain);
        if (round != null && round.getOnlyMember(agentPo.getPackingAddress(), chain) != null) {
            value.add(String.valueOf(1));
        } else {
            value.add(String.valueOf(0));
        }
        result.put(ConsensusConstant.PARAM_RESULT_VALUE, value);
        return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result triggerCoinBaseContract(Map<String, Object> params) {
        if (params == null || params.get(ConsensusConstant.PARAM_CHAIN_ID) == null || params.get(ConsensusConstant.PARAM_TX) == null || params.get(ConsensusConstant.PARAM_BLOCK_HEADER_HEX) == null || params.get(ConsensusConstant.STATE_ROOT) == null) {
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
        Map<String, Object> result = new HashMap<>(2);
        String stateRoot = null;
        try {
            Transaction coinBaseTransaction = new Transaction();
            coinBaseTransaction.parse(RPCUtil.decode((String) params.get(ConsensusConstant.PARAM_TX)), 0);
            BlockHeader blockHeader = new BlockHeader();
            String originalStateRoot = (String) params.get(ConsensusConstant.STATE_ROOT);
            blockHeader.parse(RPCUtil.decode((String) params.get(ConsensusConstant.PARAM_BLOCK_HEADER_HEX)), 0);
            BlockExtendsData extendsData = new BlockExtendsData(blockHeader.getExtend());
            MeetingRound round = roundManager.getRoundByIndex(chain, extendsData.getRoundIndex());
            if (round == null) {
                round = roundManager.getRound(chain, extendsData, false);
            }
            MeetingMember member = round.getMember(extendsData.getPackingIndexOfRound());
            if (AddressTool.validContractAddress(member.getAgent().getRewardAddress(), chain.getConfig().getChainId())) {
                stateRoot = CallMethodUtils.triggerContract(chain.getConfig().getChainId(), originalStateRoot, blockHeader.getHeight() - 1, AddressTool.getStringAddressByBytes(member.getAgent().getRewardAddress()), RPCUtil.encode(coinBaseTransaction.serialize()));
                extendsData.setStateRoot(RPCUtil.decode(stateRoot));
            } else {
                if (coinDataManager.hasContractAddress(coinBaseTransaction.getCoinDataInstance(), chain.getConfig().getChainId())) {
                    stateRoot = CallMethodUtils.triggerContract(chain.getConfig().getChainId(), originalStateRoot, blockHeader.getHeight() - 1, null, RPCUtil.encode(coinBaseTransaction.serialize()));
                    extendsData.setStateRoot(RPCUtil.decode(stateRoot));
                } else {
                    stateRoot = originalStateRoot;
                }
            }
            result.put(ConsensusConstant.PARAM_RESULT_VALUE, stateRoot);
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        } catch (NulsException e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            result.put(ConsensusConstant.PARAM_RESULT_VALUE, null);
            return Result.getFailed(e.getErrorCode()).setData(result);
        } catch (Exception e) {
            chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).error(e);
            result.put(ConsensusConstant.PARAM_RESULT_VALUE, null);
            return Result.getFailed(ConsensusErrorCode.FAILED).setData(result);
        }
    }
}
