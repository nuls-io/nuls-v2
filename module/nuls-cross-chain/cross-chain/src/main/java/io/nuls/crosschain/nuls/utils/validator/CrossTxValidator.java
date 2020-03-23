package io.nuls.crosschain.nuls.utils.validator;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.core.constant.TxType;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxSignMessage;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.nuls.constant.ParamConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.rpc.call.AccountCall;
import io.nuls.crosschain.nuls.rpc.call.ChainManagerCall;
import io.nuls.crosschain.nuls.rpc.call.ConsensusCall;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.srorage.ConvertHashService;
import io.nuls.crosschain.nuls.srorage.ConvertCtxService;
import io.nuls.crosschain.nuls.utils.CommonUtil;
import io.nuls.crosschain.nuls.utils.TxUtil;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

import static io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode.PAYEE_AND_PAYER_IS_THE_SAME_CHAIN;

/**
 * 跨链交易验证工具类
 * Transaction Verification Tool Class
 *
 * @author tag
 * 2019/4/15
 */
@Component
public class CrossTxValidator {
    @Autowired
    private NulsCrossChainConfig config;

    @Autowired
    private ConvertHashService convertHashService;

    @Autowired
    private ConvertCtxService convertCtxService;

    @Autowired
    private ChainManager chainManager;

    /**
     * 验证交易
     * Verifying transactions
     *
     * @param chain           链ID/chain id
     * @param tx              交易/transaction info
     * @param blockHeader     区块头信息/block header info
     * @return boolean
     */
    @SuppressWarnings("unchecked")
    public boolean packageValid(Chain chain, Transaction tx, BlockHeader blockHeader) throws NulsException, IOException{
        CoinData coinData = tx.getCoinDataInstance();
        int fromChainId = AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress());
        int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());
        if(chain.getChainId() == fromChainId){
            if(!SignatureUtil.validateCtxSignture(tx)){
                chain.getLogger().error("Signature verification failed");
                throw new NulsException(NulsCrossChainErrorCode.SIGNATURE_ERROR);
            }
        }else{
            Transaction realCtx = tx;
            List<String> verifierList;
            int minPassCount;
            int verifierChainId = fromChainId;
            ChainInfo chainInfo;
            if(chain.getChainId() == toChainId && !config.isMainNet()){
                verifierChainId = config.getMainChainId();
                realCtx = TxUtil.friendConvertToMain(chain, tx, null, TxType.CROSS_CHAIN);
                realCtx.setTransactionSignature(tx.getTransactionSignature());
            }
            chainInfo = chainManager.getChainInfo(verifierChainId);
            if(chainInfo == null){
                chain.getLogger().error("链未注册,chainId:{}",verifierChainId);
                throw new NulsException(NulsCrossChainErrorCode.CHAIN_UNREGISTERED);
            }
            verifierList = new ArrayList<>(chainInfo.getVerifierList());
            if(verifierList.isEmpty()){
                chain.getLogger().error("链还未注册验证人,chainId:{}",verifierChainId);
                throw new NulsException(NulsCrossChainErrorCode.CHAIN_UNREGISTERED_VERIFIER);
            }
            minPassCount = chainInfo.getMinPassCount();
            if(!signByzantineVerify(chain, realCtx, verifierList, minPassCount, verifierChainId)){
                chain.getLogger().info("签名拜占庭验证失败！");
                throw new NulsException(NulsCrossChainErrorCode.CTX_SIGN_BYZANTINE_FAIL);
            }
            if(config.isMainNet()){
                if(!ChainManagerCall.verifyCtxAsset(fromChainId, tx)){
                    chain.getLogger().info("跨链资产验证失败！");
                    throw new NulsException(NulsCrossChainErrorCode.CROSS_ASSERT_VALID_ERROR);
                }
            }
        }
        return true;
    }


    /**
     * 验证交易
     * Verifying transactions
     *
     * @param chain           链ID/chain id
     * @param tx              交易/transaction info
     * @param blockHeader     区块头信息/block header info
     * @return boolean
     */
    @SuppressWarnings("unchecked")
    public boolean validateTx(Chain chain, Transaction tx, BlockHeader blockHeader) throws NulsException, IOException{
        //判断这笔跨链交易是否属于本链
        CoinData coinData = tx.getCoinDataInstance();
        if (!coinDataValid(chain, coinData, tx.size())) {
            throw new NulsException(NulsCrossChainErrorCode.COINDATA_VERIFY_FAIL);
        }
        //如果本链为发起链且本链不为主链,则需要生成主网协议的跨链交易验证并验证签名
        int fromChainId = AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress());
        int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());

        if(toChainId == 0){
            throw new NulsException(NulsCrossChainErrorCode.TO_ADDRESS_ERROR);
        }
        //本链协议跨链交易不需要签名拜占庭验证，只需验证交易签名
        if(chain.getChainId() == fromChainId){
            if(tx.getType() == TxType.CROSS_CHAIN){
                //验证From中地址是否都签了名
                Set<String> fromAddressSet = tx.getCoinDataInstance().getFromAddressList();
                TransactionSignature transactionSignature = new TransactionSignature();
                transactionSignature.parse(tx.getTransactionSignature(), 0);
                String signAddress;
                boolean verifyResult = false;
                for (P2PHKSignature signature : transactionSignature.getP2PHKSignatures()){
                    signAddress = AddressTool.getStringAddressByBytes(AddressTool.getAddress(signature.getPublicKey(), chain.getChainId()));
                    fromAddressSet.remove(signAddress);
                    if(fromAddressSet.isEmpty()){
                        verifyResult = true;
                        break;
                    }
                }
                if(!verifyResult){
                    throw new NulsException(NulsCrossChainErrorCode.SIGNATURE_ERROR);
                }
                if(!validateCtxSignature(tx.getHash().getBytes(), transactionSignature)){
                    chain.getLogger().error("Signature verification failed");
                    throw new NulsException(NulsCrossChainErrorCode.SIGNATURE_ERROR);
                }
            }
        }else{
            Transaction realCtx = tx;
            List<String> verifierList;
            int minPassCount;
            int verifierChainId = fromChainId;
            ChainInfo chainInfo;

            if(chain.getChainId() == toChainId && !config.isMainNet()){
                verifierChainId = config.getMainChainId();
                int txType = TxType.CROSS_CHAIN;
                if(tx.getTxData() != null){
                    txType = ByteUtils.bytesToInt(tx.getTxData());
                }
                realCtx = TxUtil.friendConvertToMain(chain, tx, null, txType);
            }
            chainInfo = chainManager.getChainInfo(verifierChainId);
            if(chainInfo == null){
                chain.getLogger().error("链未注册,chainId:{}",verifierChainId);
                throw new NulsException(NulsCrossChainErrorCode.CHAIN_UNREGISTERED);
            }
            verifierList = new ArrayList<>(chainInfo.getVerifierList());
            if(verifierList.isEmpty()){
                chain.getLogger().error("链还未注册验证人,chainId:{}",verifierChainId);
                throw new NulsException(NulsCrossChainErrorCode.CHAIN_UNREGISTERED_VERIFIER);
            }
            minPassCount = chainInfo.getMinPassCount();

            if(!SignatureUtil.validateCtxSignture(realCtx)){
                chain.getLogger().info("主网协议跨链交易签名验证失败！");
                throw new NulsException(NulsCrossChainErrorCode.SIGNATURE_ERROR);
            }

            if(!signByzantineVerify(chain, realCtx, verifierList, minPassCount, verifierChainId)){
                chain.getLogger().info("签名拜占庭验证失败！");
                throw new NulsException(NulsCrossChainErrorCode.CTX_SIGN_BYZANTINE_FAIL);
            }
        }

        if(config.isMainNet()){
            if(!ChainManagerCall.verifyCtxAsset(fromChainId, tx)){
                chain.getLogger().info("跨链资产验证失败！");
                throw new NulsException(NulsCrossChainErrorCode.CROSS_ASSERT_VALID_ERROR);
            }
        }
        return true;
    }


    public boolean coinDataValid(Chain chain, CoinData coinData, int txSize)throws NulsException{
        return coinDataValid(chain,coinData,txSize,true);
    }

    /**
     * CoinData基础验证
     * CoinData basic validate
     *
     * @param chain
     * @param coinData
     * @param txSize
     */
    public boolean coinDataValid(Chain chain, CoinData coinData, int txSize, boolean isLocalCtx) throws NulsException {
        List<CoinFrom> coinFromList = coinData.getFrom();
        List<CoinTo> coinToList = coinData.getTo();
        if (coinFromList == null || coinFromList.isEmpty()
                || coinToList == null || coinToList.isEmpty()) {
            chain.getLogger().error("转出方或转入方为空");
            throw new NulsException(NulsCrossChainErrorCode.COINFROM_NOT_FOUND);
        }
        int fromChainId = 0;
        int toChainId = 0;
        //跨链交易的from中地址必须是同一条链的地址，to中的地址必须是一条链地址
        for (CoinFrom coinFrom : coinFromList) {
            if (fromChainId == 0) {
                fromChainId = AddressTool.getChainIdByAddress(coinFrom.getAddress());
            }
            if (AddressTool.getChainIdByAddress(coinFrom.getAddress()) != fromChainId) {
                chain.getLogger().error("跨链交易转出方存在多条链账户");
                throw new NulsException(NulsCrossChainErrorCode.CROSS_TX_PAYER_CHAIN_NOT_SAME);
            }
        }
        for (CoinTo coinTo : coinToList) {
            if (toChainId == 0) {
                toChainId = AddressTool.getChainIdByAddress(coinTo.getAddress());
            }
            if (AddressTool.getChainIdByAddress(coinTo.getAddress()) != toChainId) {
                chain.getLogger().error("跨链交易转入方存在多条链账户");
                throw new NulsException(NulsCrossChainErrorCode.CROSS_TX_PAYEE_CHAIN_NOT_SAME);
            }
        }
        //from和to不能是同一个地址
        if (fromChainId == toChainId) {
            chain.getLogger().error("跨链交易转出方和转入方是同一条链账户");
            throw new NulsException(PAYEE_AND_PAYER_IS_THE_SAME_CHAIN);
        }
        //查询这条跨链交易是否与本链相关
        int chainId = chain.getChainId();
        if (fromChainId != chainId && toChainId != chainId && !config.isMainNet()) {
            chain.getLogger().error("该跨链交易不是本链跨链交易");
            throw new NulsException(NulsCrossChainErrorCode.NOT_BELONG_TO_CURRENT_CHAIN);
        }
        //如果本链不为发起链，验证CoinData中的主网主资产是否足够支付手续费
        if (chain.getChainId() != fromChainId || !isLocalCtx) {
            BigInteger feeTotalFrom = BigInteger.ZERO;
            for (CoinFrom coinFrom : coinFromList) {
                if (CommonUtil.isNulsAsset(coinFrom)) {
                    feeTotalFrom = feeTotalFrom.add(coinFrom.getAmount());
                }
            }
            BigInteger feeTotalTo = BigInteger.ZERO;
            for (CoinTo coinTo : coinToList) {
                if (CommonUtil.isNulsAsset(coinTo)) {
                    feeTotalTo = feeTotalTo.add(coinTo.getAmount());
                }
            }
            //本交易预计收取的手续费
            BigInteger targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
            //交易中已收取的手续费
            BigInteger actualFee = feeTotalFrom.subtract(feeTotalTo);
            if(BigIntegerUtils.isLessThan(actualFee, targetFee)){
                chain.getLogger().error("手续费不足");
                throw new NulsException(NulsCrossChainErrorCode.INSUFFICIENT_FEE);
            }
        }
        return true;
    }

    /**
     * 跨链交易签名拜占庭验证
     * Byzantine Verification of Cross-Chain Transaction Signature
     *
     * */
    private boolean signByzantineVerify(Chain chain,Transaction ctx,  List<String> verifierList,int byzantineCount,int verifierChainId)throws NulsException{
        TransactionSignature transactionSignature = new TransactionSignature();
        try {
            transactionSignature.parse(ctx.getTransactionSignature(),0);
        }catch (NulsException e){
            chain.getLogger().error(e);
            throw e;
        }
        if(transactionSignature.getP2PHKSignatures().size() < byzantineCount){
            chain.getLogger().error("跨链交易签名数量小于拜占庭数量，Hash:{},signCount:{},byzantineCount:{}", ctx.getHash().toHex(),transactionSignature.getP2PHKSignatures().size(),byzantineCount);
            return false;
        }
        chain.getLogger().debug("当前验证人列表：{}",verifierList.toString());
        Iterator<P2PHKSignature> iterator = transactionSignature.getP2PHKSignatures().iterator();
        int passCount = 0;
        Set<String> passedAddress  = new HashSet<>();
        while (iterator.hasNext()){
            P2PHKSignature signature = iterator.next();
            for (String verifier:verifierList) {
                if(passedAddress.contains(verifier)){
                    continue;
                }
                if(Arrays.equals(AddressTool.getAddress(signature.getPublicKey(), verifierChainId), AddressTool.getAddress(verifier))){
                    passedAddress.add(verifier);
                    passCount++;
                    break;
                }
            }
        }
        if(passCount < byzantineCount){
            chain.getLogger().error("跨链交易签名验证通过数小于拜占庭数量，Hash:{},passCount:{},byzantineCount:{}", ctx.getHash().toHex(),passCount,byzantineCount);
            return false;
        }
        return true;
    }

    private boolean validateCtxSignature(byte[] txHash,TransactionSignature transactionSignature)throws NulsException{
        for (P2PHKSignature signature : transactionSignature.getP2PHKSignatures()) {
            if (!ECKey.verify(txHash, signature.getSignData().getSignBytes(), signature.getPublicKey())) {
                throw new NulsException(new Exception("Transaction signature error !"));
            }
        }
        return true;
    }
}
