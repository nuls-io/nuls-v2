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
 * @Description: 功能描述
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
     * 缓存重置异构链存储的主链验证人的初始化验证人交易的hash
     * 用于在处理拜占庭签名时与普通的初始化验证人交易进行区别
     * 本节点处理完此交易后交易hash从此列表移除
     */
    private Set<String> resetOtherVerifierTxList = new HashSet<>();

    private CoinData assemblyCoinFrom(Chain chain, String addressStr) throws NulsException {
        byte[] address = AddressTool.getAddress(addressStr);
        if (!AddressTool.validAddress(chain.getChainId(), addressStr)) {
            //转账交易转出地址必须是本链地址
            chain.getLogger().error("跨链交易转出账户不为本链账户");
            throw new NulsException(NulsCrossChainErrorCode.ADDRESS_IS_NOT_THE_CURRENT_CHAIN);
        }
        int assetChainId = chain.getChainId();
        int assetId = nulsCrossChainConfig.getAssetId();
        //检查对应资产余额 是否足够
        Map<String, Object> result = LedgerCall.getBalanceAndNonce(chain, addressStr, assetChainId, assetId);
        byte[] nonce = RPCUtil.decode((String) result.get("nonce"));
        BigInteger balance = new BigInteger(result.get("available").toString());
        if (BigIntegerUtils.isLessThan(balance, NORMAL_PRICE_PRE_1024_BYTES)) {
            chain.getLogger().error("账户余额不足");
            throw new NulsException(NulsCrossChainErrorCode.INSUFFICIENT_BALANCE);
        }
        CoinData coinData = new CoinData();
        coinData.setFrom(List.of(new CoinFrom(address, assetChainId, assetId, NORMAL_PRICE_PRE_1024_BYTES, nonce, NulsCrossChainConstant.UNLOCKED_TX)));
        coinData.setTo(List.of(new CoinTo(address,assetChainId,assetId,BigInteger.ZERO)));
        return coinData;
    }

    /**
     * 创建并广播一个重置本链验证人交易
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
                chain.getLogger().error("重置本链验证人列表交易发送交易模块失败\n\n");
                throw new NulsException(NulsCrossChainErrorCode.INTERFACE_CALL_FAILED);
            }
            Map<String, Object> result = new HashMap<>(2);
            result.put(ParamConstant.TX_HASH, tx.getHash().toHex());
            return Result.getSuccess(CommonCodeConstanst.SUCCESS).setData(result);
        }catch (NulsException e){
            chain.getLogger().error("创建重置本链验证人列表交易时捕获异常",e);
            return Result.getFailed(e.getErrorCode());
        }catch (Throwable e){
            chain.getLogger().error("创建重置本链验证人列表交易时捕获到未知异常,{}",e.getMessage(),e);
            return Result.getFailed(CommonCodeConstanst.SYS_UNKOWN_EXCEPTION);
        }

    }

    /**
     * 验证此交易的coin data中的from中只能有1个种子节点签名的交易
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
        //一个区块只处理一条重置交易，其他的丢掉
        List<Transaction> invalidCtxList =  txs.stream().skip(1).collect(Collectors.toList());
        String errorCode = null;
        Transaction tx = txs.get(0);
        try {
            CoinData coinData = tx.getCoinDataInstance();
            //只能有一个from
            if (coinData.getFrom().size() != 1) {
                result.put("txList", txs);
                result.put("errorCode", NulsCrossChainErrorCode.COINDATA_VERIFY_FAIL.getCode());
                return result;
            }
            //必须是种子节点发出的交易
            if (coinData.getFromAddressList().stream().noneMatch(d -> nulsCrossChainConfig.getSeedNodeList().contains(d))) {
                result.put("txList", txs);
                result.put("errorCode", NulsCrossChainErrorCode.MUST_SEED_ADDRESS_SIGN.getCode());
                return result;
            }
            TransactionSignature transactionSignature = new TransactionSignature();
            transactionSignature.parse(tx.getTransactionSignature(), 0);
            byte[] txHashByte = tx.getHash().getBytes();
            //只能有一个签名
            if (transactionSignature.getP2PHKSignatures().size() != 1) {
                chain.getLogger().error("signatures can not be null");
                throw new NulsException(NulsCrossChainErrorCode.SIGNATURE_ERROR);
            }

            //验证签名
            P2PHKSignature signature = transactionSignature.getP2PHKSignatures().get(0);
            if (!ECKey.verify(txHashByte, signature.getSignData().getSignBytes(), signature.getPublicKey())) {
                chain.getLogger().error("Signature verification failed");
                throw new NulsException(NulsCrossChainErrorCode.SIGNATURE_ERROR);
            }
            //签名必须是种子节点
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
     * 1.将当前的本链验证人列表存储在old_local_verifier表中 key为高度
     * 2.从共识模块获取最新的节点列表，将出块地址刷新到本链验证人列表中。
     * 3.组装一个平行链验证人初始化交易广播到平行链,所有注册的平行链都要广播。
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
        chain.getLogger().info("获取到当前网络最新的出块地址列表（包括种子节点）:{}",allAgentPackingAddress);
        //备份当前本链验证人列表
        localVerifierService.backup(chainId,blockHeader.getHeight());
        chain.getSwitchVerifierLock().writeLock().lock();
        try{
            boolean res = LocalVerifierManager.initLocalVerifier(chain,new ArrayList<>(allAgentPackingAddress));
            if(!res){
                chain.getLogger().error("重置本链验证人列表失败");
                return false;
            }
        } catch (Exception e) {
            chain.getLogger().error(e.getMessage(), e);
        } finally {
            chain.getSwitchVerifierLock().writeLock().unlock();
        }
        chain.getLogger().info("重置本链验证人列表完成:{}",chain.getVerifierList());
        int syncStatus = BlockCall.getBlockStatus(chain);
        List<ChainInfo> otherChainInfoList = chainManager.getRegisteredCrossChainList().stream().filter(d->d.getChainId() != chainId).collect(Collectors.toList());
        List<Transaction> newTxList = Lists.newArrayList();
        otherChainInfoList.forEach(chainInfo -> {
            try {
                    //组装一个重置平行链存储的主网验证人列表的交易
                    newTxList.add(TxUtil.createVerifierInitTx(chain.getVerifierList(), tx.getTime(), chainInfo.getChainId()));
            } catch (IOException e) {
                chain.getLogger().error("组装重置平行链存储的主网验证人列表的交易失败",e);
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
            chain.getLogger().info("发起一笔重置平行链存储的主链验证人列表的交易,txHash:{}",txHash);
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
