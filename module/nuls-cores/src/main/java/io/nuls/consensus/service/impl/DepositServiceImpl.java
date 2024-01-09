package io.nuls.consensus.service.impl;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinTo;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.core.basic.Page;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.ObjectUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.consensus.constant.ConsensusConstant;
import io.nuls.consensus.constant.ConsensusErrorCode;
import io.nuls.consensus.model.bo.Chain;
import io.nuls.consensus.model.bo.tx.txdata.Agent;
import io.nuls.consensus.model.bo.tx.txdata.CancelDeposit;
import io.nuls.consensus.model.bo.tx.txdata.Deposit;
import io.nuls.consensus.model.dto.input.CreateDepositDTO;
import io.nuls.consensus.model.dto.input.SearchDepositDTO;
import io.nuls.consensus.model.dto.input.WithdrawDTO;
import io.nuls.consensus.model.dto.output.DepositDTO;
import io.nuls.consensus.rpc.call.CallMethodUtils;
import io.nuls.consensus.service.DepositService;
import io.nuls.consensus.utils.TxUtil;
import io.nuls.consensus.utils.manager.ChainManager;
import io.nuls.consensus.utils.manager.CoinDataManager;
import io.nuls.consensus.utils.validator.TxValidator;

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
public class DepositServiceImpl implements DepositService {

    @Autowired
    private ChainManager chainManager;
    @Autowired
    private CoinDataManager coinDataManager;
    @Autowired
    private TxValidator validatorManager;

    /**
     * 委托共识
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result depositToAgent(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        CreateDepositDTO dto = JSONUtils.map2pojo(params, CreateDepositDTO.class);
        try {
            ObjectUtils.canNotEmpty(dto);
            ObjectUtils.canNotEmpty(dto.getAddress());
            ObjectUtils.canNotEmpty(dto.getAgentHash());
            ObjectUtils.canNotEmpty(dto.getDeposit());
        }catch (RuntimeException e){
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        if (!NulsHash.validHash(dto.getAgentHash())) {
            return Result.getFailed(ConsensusErrorCode.AGENT_NOT_EXIST);
        }
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            if (!AddressTool.validAddress(dto.getChainId(), dto.getAddress())) {
                throw new NulsException(ConsensusErrorCode.ADDRESS_ERROR);
            }
            //账户验证
            HashMap callResult = CallMethodUtils.accountValid(dto.getChainId(), dto.getAddress(), dto.getPassword());
            Transaction tx = new Transaction(TxType.DEPOSIT);
            Deposit deposit = TxUtil.createDeposit(dto);
            tx.setTxData(deposit.serialize());
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            CoinData coinData = coinDataManager.getCoinData(deposit.getAddress(), chain, new BigInteger(dto.getDeposit()), ConsensusConstant.CONSENSUS_LOCK_TIME, tx.size() + P2PHKSignature.SERIALIZE_LENGTH,chain.getConfig().getAgentChainId(),chain.getConfig().getAgentAssetId());
            tx.setCoinData(coinData.serialize());
            //交易签名
            String priKey = (String) callResult.get("priKey");
            CallMethodUtils.transactionSignature(dto.getChainId(), dto.getAddress(), dto.getPassword(), priKey, tx);
            String txStr = RPCUtil.encode(tx.serialize());
            boolean validResult = validatorManager.validateTx(chain, tx);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            CallMethodUtils.sendTx(chain,txStr);
            Map<String, Object> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            result.put("txHash", tx.getHash().toHex());
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
     * 委托共识交易验证
     */
    @Override
    @SuppressWarnings("unchecked")
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
            Transaction transaction = new Transaction(TxType.DEPOSIT);
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
     * 退出共识
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result withdraw(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        WithdrawDTO dto = JSONUtils.map2pojo(params, WithdrawDTO.class);
        if (!NulsHash.validHash(dto.getTxHash())) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            if (!AddressTool.validAddress( dto.getChainId(), dto.getAddress())) {
                return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
            }
            //账户验证
            HashMap callResult = CallMethodUtils.accountValid(dto.getChainId(), dto.getAddress(), dto.getPassword());
            NulsHash hash = NulsHash.fromHex(dto.getTxHash());
            Transaction depositTransaction = CallMethodUtils.getTransaction(chain,dto.getTxHash());
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
            Transaction cancelDepositTransaction = new Transaction(TxType.CANCEL_DEPOSIT);
            CancelDeposit cancelDeposit = new CancelDeposit();
            cancelDeposit.setAddress(AddressTool.getAddress(dto.getAddress()));
            cancelDeposit.setJoinTxHash(hash);
            cancelDepositTransaction.setTime(NulsDateUtils.getCurrentTimeSeconds());
            cancelDepositTransaction.setTxData(cancelDeposit.serialize());
            CoinData coinData = coinDataManager.getUnlockCoinData(cancelDeposit.getAddress(), chain, deposit.getDeposit(), 0, cancelDepositTransaction.size() + P2PHKSignature.SERIALIZE_LENGTH);
            coinData.getFrom().get(0).setNonce(CallMethodUtils.getNonce(hash.getBytes()));
            cancelDepositTransaction.setCoinData(coinData.serialize());
            cancelDepositTransaction.setTime(NulsDateUtils.getCurrentTimeSeconds());
            //交易签名
            String priKey = (String) callResult.get("priKey");
            CallMethodUtils.transactionSignature(dto.getChainId(), dto.getAddress(), dto.getPassword(), priKey, cancelDepositTransaction);
            String txStr = RPCUtil.encode(cancelDepositTransaction.serialize());
            boolean validResult = validatorManager.validateTx(chain, cancelDepositTransaction);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }
            CallMethodUtils.sendTx(chain,txStr);
            Map<String, Object> result = new HashMap<>(ConsensusConstant.INIT_CAPACITY);
            result.put("txHash", cancelDepositTransaction.getHash().toHex());
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
     * 退出共识交易验证
     */
    @Override
    @SuppressWarnings("unchecked")
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
            Transaction transaction = new Transaction(TxType.CANCEL_DEPOSIT);
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
     * 获取委托列表信息
     */
    @Override
    @SuppressWarnings("unchecked")
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
            if (agentHash != null && !deposit.getAgentHash().toHex().equals(agentHash)) {
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

}
