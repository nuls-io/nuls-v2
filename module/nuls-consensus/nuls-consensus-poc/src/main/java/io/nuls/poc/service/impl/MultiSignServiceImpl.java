package io.nuls.poc.service.impl;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.signture.MultiSignTxSignature;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.constant.ConsensusErrorCode;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.model.bo.tx.txdata.Agent;
import io.nuls.poc.model.bo.tx.txdata.CancelDeposit;
import io.nuls.poc.model.bo.tx.txdata.Deposit;
import io.nuls.poc.model.bo.tx.txdata.StopAgent;
import io.nuls.poc.model.dto.input.*;
import io.nuls.poc.rpc.call.CallMethodUtils;
import io.nuls.poc.service.MultiSignService;
import io.nuls.poc.utils.TxUtil;
import io.nuls.poc.utils.manager.ChainManager;
import io.nuls.poc.utils.manager.CoinDataManager;
import io.nuls.poc.utils.validator.TxValidator;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

/**
 * 多签账户相关交易接口实现类
 * Implementation Class of Multi-Sign Account Related Transaction Interface
 *
 * @author tag
 * 2019/07/25
 * */
@Component
public class MultiSignServiceImpl implements MultiSignService {
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private CoinDataManager coinDataManager;
    @Autowired
    private TxValidator txValidator;

    @Override
    @SuppressWarnings("unchecked")
    public Result createMultiAgent(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        CreateMultiAgentDTO dto = JSONUtils.map2pojo(params, CreateMultiAgentDTO.class);
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            Log.error(ConsensusErrorCode.CHAIN_NOT_EXIST.getMsg());
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            MultiSigAccount multiSigAccount = CallMethodUtils.getMultiSignAccount(dto.getChainId(), dto.getAgentAddress());
            HashMap callResult = null;
            if(StringUtils.isNotBlank(dto.getSignAddress()) && StringUtils.isNotBlank(dto.getPassword())){
                callResult = CallMethodUtils.accountValid(dto.getChainId(), dto.getSignAddress(), dto.getPassword());
            }

            Transaction tx = new Transaction(TxType.REGISTER_AGENT);
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            Agent agent = TxUtil.createAgent(dto);
            tx.setTxData(agent.serialize());

            int txSignSize = multiSigAccount.getM() * P2PHKSignature.SERIALIZE_LENGTH;
            CoinData coinData = coinDataManager.getCoinData(agent.getAgentAddress(), chain, new BigInteger(dto.getDeposit()), ConsensusConstant.CONSENSUS_LOCK_TIME, tx.size() + txSignSize,chain.getConfig().getAgentChainId(),chain.getConfig().getAgentAssetId());
            tx.setCoinData(coinData.serialize());

            String priKey = null;
            if(callResult != null && AddressTool.validSignAddress(multiSigAccount.getPubKeyList(), HexUtil.decode((String)callResult.get(ConsensusConstant.PUB_KEY)))){
                priKey = (String) callResult.get("priKey");
            }
            buildMultiSignTransactionSignature(tx, multiSigAccount,priKey);

            boolean validResult = txValidator.validateTx(chain, tx);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }

            String txStr = RPCUtil.encode(tx.serialize());
            Map<String, Object> result = new HashMap<>(4);
            result.put("txHash", tx.getHash().toHex());
            result.put("tx", txStr);
            result.put("completed", false);

            if(callResult != null && multiSigAccount.getM() == 1){
                CallMethodUtils.sendTx(chain, txStr);
                result.put("completed", true);
            }
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        }catch(NulsException e){
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        }catch (IOException e){
            chain.getLogger().error(e);
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result stopMultiAgent(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        StopMultiAgentDTO dto = JSONUtils.map2pojo(params, StopMultiAgentDTO.class);
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            Log.error(ConsensusErrorCode.CHAIN_NOT_EXIST.getMsg());
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            MultiSigAccount multiSigAccount = CallMethodUtils.getMultiSignAccount(dto.getChainId(), dto.getAddress());
            HashMap callResult = null;
            if(StringUtils.isNotBlank(dto.getSignAddress()) && StringUtils.isNotBlank(dto.getPassword())){
                callResult = CallMethodUtils.accountValid(dto.getChainId(), dto.getSignAddress(), dto.getPassword());
            }

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
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            int txSignSize = multiSigAccount.getM() * P2PHKSignature.SERIALIZE_LENGTH;
            CoinData coinData = coinDataManager.getStopAgentCoinData(chain, agent, NulsDateUtils.getCurrentTimeSeconds() + chain.getConfig().getStopAgentLockTime());
            BigInteger fee = TransactionFeeCalculator.getConsensusTxFee(tx.size() + txSignSize + coinData.serialize().length, chain.getConfig().getFeeUnit());
            coinData.getTo().get(0).setAmount(coinData.getTo().get(0).getAmount().subtract(fee));
            tx.setCoinData(coinData.serialize());

            String priKey = null;
            if(callResult != null && AddressTool.validSignAddress(multiSigAccount.getPubKeyList(), HexUtil.decode((String)callResult.get(ConsensusConstant.PUB_KEY)))){
                priKey = (String) callResult.get("priKey");
            }
            buildMultiSignTransactionSignature(tx, multiSigAccount,priKey);

            boolean validResult = txValidator.validateTx(chain, tx);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }

            String txStr = RPCUtil.encode(tx.serialize());
            Map<String, Object> result = new HashMap<>(4);
            result.put("txHash", tx.getHash().toHex());
            result.put("tx", txStr);
            result.put("completed", false);

            if(callResult != null && multiSigAccount.getM() == 1){
                CallMethodUtils.sendTx(chain, txStr);
                result.put("completed", true);
            }
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        }catch(NulsException e){
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        }catch (IOException e){
            chain.getLogger().error(e);
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result multiDeposit(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        CreateMultiDepositDTO dto = JSONUtils.map2pojo(params, CreateMultiDepositDTO.class);
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            Log.error(ConsensusErrorCode.CHAIN_NOT_EXIST.getMsg());
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            MultiSigAccount multiSigAccount = CallMethodUtils.getMultiSignAccount(dto.getChainId(), dto.getAddress());
            HashMap callResult = null;
            if(StringUtils.isNotBlank(dto.getSignAddress()) && StringUtils.isNotBlank(dto.getPassword())){
                callResult = CallMethodUtils.accountValid(dto.getChainId(), dto.getSignAddress(), dto.getPassword());
            }

            Transaction tx = new Transaction(TxType.DEPOSIT);
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            Deposit deposit = TxUtil.createDeposit(dto);
            tx.setTxData(deposit.serialize());

            int txSignSize = multiSigAccount.getM() * P2PHKSignature.SERIALIZE_LENGTH;
            CoinData coinData = coinDataManager.getCoinData(deposit.getAddress(), chain, new BigInteger(dto.getDeposit()), ConsensusConstant.CONSENSUS_LOCK_TIME, tx.size() + txSignSize,chain.getConfig().getAgentChainId(),chain.getConfig().getAgentAssetId());
            tx.setCoinData(coinData.serialize());

            String priKey = null;
            if(callResult != null && AddressTool.validSignAddress(multiSigAccount.getPubKeyList(), HexUtil.decode((String)callResult.get(ConsensusConstant.PUB_KEY)))){
                priKey = (String) callResult.get("priKey");
            }
            buildMultiSignTransactionSignature(tx, multiSigAccount,priKey);

            boolean validResult = txValidator.validateTx(chain, tx);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }

            String txStr = RPCUtil.encode(tx.serialize());
            Map<String, Object> result = new HashMap<>(4);
            result.put("txHash", tx.getHash().toHex());
            result.put("tx", txStr);
            result.put("completed", false);

            if(callResult != null && multiSigAccount.getM() == 1){
                CallMethodUtils.sendTx(chain, txStr);
                result.put("completed", true);
            }
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        }catch(NulsException e){
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        }catch (IOException e){
            chain.getLogger().error(e);
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Result multiWithdraw(Map<String, Object> params) {
        if (params == null) {
            return Result.getFailed(ConsensusErrorCode.PARAM_ERROR);
        }
        MultiWithdrawDTO dto = JSONUtils.map2pojo(params, MultiWithdrawDTO.class);
        Chain chain = chainManager.getChainMap().get(dto.getChainId());
        if (chain == null) {
            Log.error(ConsensusErrorCode.CHAIN_NOT_EXIST.getMsg());
            return Result.getFailed(ConsensusErrorCode.CHAIN_NOT_EXIST);
        }
        try {
            MultiSigAccount multiSigAccount = CallMethodUtils.getMultiSignAccount(dto.getChainId(), dto.getAddress());
            HashMap callResult = null;
            if(StringUtils.isNotBlank(dto.getSignAddress()) && StringUtils.isNotBlank(dto.getPassword())){
                callResult = CallMethodUtils.accountValid(dto.getChainId(), dto.getSignAddress(), dto.getPassword());
            }

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

            int txSignSize = multiSigAccount.getM() * P2PHKSignature.SERIALIZE_LENGTH;
            CoinData coinData = coinDataManager.getUnlockCoinData(cancelDeposit.getAddress(), chain, deposit.getDeposit(), 0, cancelDepositTransaction.size() + txSignSize);
            coinData.getFrom().get(0).setNonce(CallMethodUtils.getNonce(hash.getBytes()));
            cancelDepositTransaction.setCoinData(coinData.serialize());
            cancelDepositTransaction.setTime(NulsDateUtils.getCurrentTimeSeconds());

            String priKey = null;
            if(callResult != null && AddressTool.validSignAddress(multiSigAccount.getPubKeyList(), HexUtil.decode((String)callResult.get(ConsensusConstant.PUB_KEY)))){
                priKey = (String) callResult.get("priKey");
            }
            buildMultiSignTransactionSignature(cancelDepositTransaction, multiSigAccount,priKey);

            boolean validResult = txValidator.validateTx(chain, cancelDepositTransaction);
            if (!validResult) {
                return Result.getFailed(ConsensusErrorCode.TX_DATA_VALIDATION_ERROR);
            }

            String txStr = RPCUtil.encode(cancelDepositTransaction.serialize());
            Map<String, Object> result = new HashMap<>(4);
            result.put("txHash", cancelDepositTransaction.getHash().toHex());
            result.put("tx", txStr);
            result.put("completed", false);

            if(callResult != null && multiSigAccount.getM() == 1){
                CallMethodUtils.sendTx(chain, txStr);
                result.put("completed", true);
            }
            return Result.getSuccess(ConsensusErrorCode.SUCCESS).setData(result);
        }catch(NulsException e){
            chain.getLogger().error(e);
            return Result.getFailed(e.getErrorCode());
        }catch (IOException e){
            chain.getLogger().error(e);
            return Result.getFailed(ConsensusErrorCode.DATA_PARSE_ERROR);
        }
    }

    private void buildMultiSignTransactionSignature(Transaction transaction, MultiSigAccount multiSigAccount, String priKey) throws NulsException {
        MultiSignTxSignature transactionSignature = new MultiSignTxSignature();
        transactionSignature.setM(multiSigAccount.getM());
        transactionSignature.setPubKeyList(multiSigAccount.getPubKeyList());
        try {
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            if(priKey != null && !priKey.isEmpty()){
                P2PHKSignature p2PHKSignature = SignatureUtil.createSignatureByPriKey(transaction, priKey);
                p2PHKSignatures.add(p2PHKSignature);
            }
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
            transaction.setTransactionSignature(transactionSignature.serialize());
        } catch (IOException e) {
            throw new NulsException(ConsensusErrorCode.SERIALIZE_ERROR);
        }
    }
}
