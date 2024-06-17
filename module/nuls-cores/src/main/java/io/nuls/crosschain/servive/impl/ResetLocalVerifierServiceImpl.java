package io.nuls.crosschain.servive.impl;

import com.google.common.collect.Lists;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.basic.Result;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.service.ResetLocalVerifierService;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.constant.ParamConstant;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.rpc.call.*;
import io.nuls.crosschain.srorage.LocalVerifierService;
import io.nuls.crosschain.utils.TxUtil;
import io.nuls.crosschain.utils.manager.ChainManager;
import io.nuls.crosschain.utils.manager.CoinDataManager;
import io.nuls.crosschain.utils.manager.LocalVerifierManager;
import io.nuls.crosschain.utils.thread.ResetOtherChainVerifierListHandler;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.stream.Collectors;

import static io.nuls.base.basic.TransactionFeeCalculator.NORMAL_PRICE_PRE_1024_BYTES;
import static io.nuls.core.constant.CommonCodeConstanst.PARAMETER_ERROR;

/**
 * @Author: zhoulijun
 * @Time: 2020/11/23 11:17
 * @Description: Function Description
 */
@Component
public class ResetLocalVerifierServiceImpl implements ResetLocalVerifierService {

    @Autowired
    private ChainManager chainManager;

    @Autowired
    private CoinDataManager coinDataManager;

    @Autowired
    NulsCoresConfig nulsCrossChainConfig;

    @Autowired
    LocalVerifierService localVerifierService;

    @Autowired
    LocalVerifierManager localVerifierManager;



    /**
     * Cache reset for heterogeneous chain storage, initialization of main chain validators, validator transactionshash
     * Used to distinguish from regular initialization validator transactions when processing Byzantine signatures
     * After processing this transaction at this node, proceed with the transactionhashRemove from this list
     */
    private Set<String> resetOtherVerifierTxList = new HashSet<>();

    private CoinData assemblyCoinFrom(Chain chain, String addressStr) throws NulsException {
        byte[] address = AddressTool.getAddress(addressStr);
        if (!AddressTool.validAddress(chain.getChainId(), addressStr)) {
            //The transfer transaction transfer address must be a local chain address
            chain.getLogger().error("Cross chain transaction transfer out account is not a local chain account");
            throw new NulsException(NulsCrossChainErrorCode.ADDRESS_IS_NOT_THE_CURRENT_CHAIN);
        }
        int assetChainId = chain.getChainId();
        int assetId = nulsCrossChainConfig.getAssetId();
        //Check the corresponding asset balance Is it sufficient
        Map<String, Object> result = LedgerCall.getBalanceAndNonce(chain, addressStr, assetChainId, assetId);
        byte[] nonce = RPCUtil.decode((String) result.get("nonce"));
        BigInteger balance = new BigInteger(result.get("available").toString());
        if (BigIntegerUtils.isLessThan(balance, NORMAL_PRICE_PRE_1024_BYTES)) {
            chain.getLogger().error("Insufficient account balance");
            throw new NulsException(NulsCrossChainErrorCode.INSUFFICIENT_BALANCE);
        }
        CoinData coinData = new CoinData();
        coinData.setFrom(List.of(new CoinFrom(address, assetChainId, assetId, NORMAL_PRICE_PRE_1024_BYTES, nonce, NulsCrossChainConstant.UNLOCKED_TX)));
        coinData.setTo(List.of(new CoinTo(address,assetChainId,assetId,BigInteger.ZERO)));
        return coinData;
    }

    /**
     * Create and broadcast a reset chain validator transaction
     *
     * @return
     */
    @Override
    public Result createResetLocalVerifierTx(int chainId, String address, String password) {
        if (chainId <= NulsCrossChainConstant.CHAIN_ID_MIN) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        if (StringUtils.isBlank(address) && StringUtils.isBlank(password)) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(NulsCrossChainErrorCode.CHAIN_NOT_EXIST);
        }
        if (!nulsCrossChainConfig.getSeedNodeList().contains(address)) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        try {
            Transaction tx = new Transaction(TxType.RESET_LOCAL_VERIFIER_LIST);
            tx.setTime(NulsDateUtils.getCurrentTimeSeconds());
            tx.setCoinData(assemblyCoinFrom(chain,address).serialize());
            TransactionSignature transactionSignature = new TransactionSignature();
            List<P2PHKSignature> p2PHKSignatures = new ArrayList<>();
            P2PHKSignature p2PHKSignature = AccountCall.signDigest(address, password, tx.getHash().getBytes());
            p2PHKSignatures.add(p2PHKSignature);
            transactionSignature.setP2PHKSignatures(p2PHKSignatures);
            tx.setTransactionSignature(transactionSignature.serialize());
            if (!TransactionCall.sendTx(chain, RPCUtil.encode(tx.serialize()))) {
                chain.getLogger().error("Failed to reset the transaction sending module of this chain's validator list transaction\n\n");
                throw new NulsException(NulsCrossChainErrorCode.INTERFACE_CALL_FAILED);
            }
            Map<String, Object> result = new HashMap<>(2);
            result.put(ParamConstant.TX_HASH, tx.getHash().toHex());
            return Result.getSuccess(CommonCodeConstanst.SUCCESS).setData(result);
        }catch (NulsException e){
            chain.getLogger().error("Exception caught while creating and resetting the validator list transaction in this chain",e);
            return Result.getFailed(e.getErrorCode());
        }catch (Throwable e){
            chain.getLogger().error("Unknown exception caught during the creation and reset of this chain's validator list transaction,{}",e.getMessage(),e);
            return Result.getFailed(CommonCodeConstanst.SYS_UNKOWN_EXCEPTION);
        }

    }

    /**
     * Verify this transaction'scoin dataMiddlefromOnly in the middle can there be1Transactions signed by seed nodes
     *
     * @param chainId     chain ID
     * @param txs         cross chain transaction list
     * @param blockHeader block header
     * @return
     */
    @Override
    public Map<String, Object> validate(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        Map<String, Object> result = new HashMap<>(2);
        if (chain == null) {
            result.put("txList", txs);
            result.put("errorCode", NulsCrossChainErrorCode.CHAIN_NOT_EXIST.getCode());
            return result;
        }
        //One block only handles one reset transaction, discard the rest
        List<Transaction> invalidCtxList =  txs.stream().skip(1).collect(Collectors.toList());
        String errorCode = null;
        Transaction tx = txs.get(0);
        try {
            CoinData coinData = tx.getCoinDataInstance();
            //There can only be onefrom
            if (coinData.getFrom().size() != 1) {
                result.put("txList", txs);
                result.put("errorCode", NulsCrossChainErrorCode.COINDATA_VERIFY_FAIL.getCode());
                return result;
            }
            //Must be a transaction sent by a seed node
            if (coinData.getFromAddressList().stream().noneMatch(d -> nulsCrossChainConfig.getSeedNodeList().contains(d))) {
                result.put("txList", txs);
                result.put("errorCode", NulsCrossChainErrorCode.MUST_SEED_ADDRESS_SIGN.getCode());
                return result;
            }
            TransactionSignature transactionSignature = new TransactionSignature();
            transactionSignature.parse(tx.getTransactionSignature(), 0);
            byte[] txHashByte = tx.getHash().getBytes();
            //Only one signature is allowed
            if (transactionSignature.getP2PHKSignatures().size() != 1) {
                chain.getLogger().error("signatures can not be null");
                throw new NulsException(NulsCrossChainErrorCode.SIGNATURE_ERROR);
            }

            //Verify signature
            P2PHKSignature signature = transactionSignature.getP2PHKSignatures().get(0);
            if (!ECKey.verify(txHashByte, signature.getSignData().getSignBytes(), signature.getPublicKey())) {
                chain.getLogger().error("Signature verification failed");
                throw new NulsException(NulsCrossChainErrorCode.SIGNATURE_ERROR);
            }
            //The signature must be a seed node
            String signAddress = AddressTool.getStringAddressByBytes(AddressTool.getAddress(signature.getPublicKey(), chain.getChainId()));
            if (!nulsCrossChainConfig.getSeedNodeList().contains(signAddress)) {
                chain.getLogger().error("Signature verification failed");
                throw new NulsException(NulsCrossChainErrorCode.SIGNATURE_ERROR);
            }
        } catch (NulsException e) {
            invalidCtxList.add(tx);
            chain.getLogger().error("reset local verifier Transaction Verification Failure");
            chain.getLogger().error(e);
            errorCode = e.getErrorCode().getCode();
        }
        result.put("txList", invalidCtxList);
        result.put("errorCode", errorCode);
        return result;
    }

    /**
     * 1.Store the current list of validators in this chainold_local_verifierIn the table keyFor height
     * 2.Retrieve the latest node list from the consensus module and refresh the block address to the list of validators in this chain.
     * 3.Assemble a parallel chain validator to initialize the transaction broadcast to the parallel chain,All registered parallel chains must be broadcasted.
     *
     * @param chainId     chain ID
     * @param txs         cross chain transaction list
     * @param blockHeader block header
     * @return
     */
    @Override
    public boolean commitTx(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return false;
        }
        Transaction tx = txs.get(0);
        Set<String> allAgentPackingAddress = new HashSet<>(ConsensusCall.getWorkAgentList(chain));
        allAgentPackingAddress.addAll(nulsCrossChainConfig.getSeedNodeList());
        chain.getLogger().info("Obtain the latest block address list for the current network（Including seed nodes）:{}",allAgentPackingAddress);
        //Back up the current list of validators in this chain
        localVerifierService.backup(chainId,blockHeader.getHeight());
        chain.getSwitchVerifierLock().writeLock().lock();
        try{
            boolean res = LocalVerifierManager.initLocalVerifier(chain,new ArrayList<>(allAgentPackingAddress));
            if(!res){
                chain.getLogger().error("Failed to reset the list of validators on this chain");
                return false;
            }
        } catch (Exception e) {
            chain.getLogger().error(e.getMessage(), e);
        } finally {
            chain.getSwitchVerifierLock().writeLock().unlock();
        }
        chain.getLogger().info("Reset the list of validators in this chain completed:{}",chain.getVerifierList());
        int syncStatus = BlockCall.getBlockStatus(chain);
        List<ChainInfo> otherChainInfoList = chainManager.getRegisteredCrossChainList().stream().filter(d->d.getChainId() != chainId).collect(Collectors.toList());
        List<Transaction> newTxList = Lists.newArrayList();
        otherChainInfoList.forEach(chainInfo -> {
            try {
                    //Assemble a transaction to reset the main network validator list for parallel chain storage
                    newTxList.add(TxUtil.createVerifierInitTx(chain.getVerifierList(), tx.getTime(), chainInfo.getChainId()));
            } catch (IOException e) {
                chain.getLogger().error("Transaction failure in assembling and resetting the main network validator list for parallel chain storage",e);
            }
        });
        if(otherChainInfoList.size() != newTxList.size()){
            return false;
        }
        newTxList.forEach(initOtherVerifierTx->{
            chain.getCrossTxThreadPool().execute(
                    new ResetOtherChainVerifierListHandler(chain, initOtherVerifierTx,syncStatus));
            String txHash = initOtherVerifierTx.getHash().toHex();
            resetOtherVerifierTxList.add(txHash);
            chain.getLogger().info("Initiate a transaction to reset the main chain verifier list stored in parallel chains,txHash:{}",txHash);
        });
        return true;
    }



    @Override
    public boolean rollbackTx(int chainId, List<Transaction> txs, BlockHeader blockHeader) {
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return false;
        }
        return localVerifierService.rollback(chainId,blockHeader.getHeight());
    }

    @Override
    public boolean isResetOtherVerifierTx(String txHash) {
        return resetOtherVerifierTxList.contains(txHash);
    }

    @Override
    public void finishResetOtherVerifierTx(String txHash) {
        resetOtherVerifierTxList.remove(txHash);
    }

}
