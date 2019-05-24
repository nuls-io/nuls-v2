package io.nuls.crosschain.nuls.utils.manager;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.Coin;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.dto.input.CoinDTO;
import io.nuls.crosschain.nuls.rpc.call.AccountCall;
import io.nuls.crosschain.nuls.rpc.call.LedgerCall;
import io.nuls.crosschain.nuls.utils.CommonUtil;

import java.math.BigInteger;
import java.util.*;

import static io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode.*;

/**
 * 跨链模块CoinData管理类
 *
 * @author tag
 */
@Component
public class CoinDataManager {
    @Autowired
    private NulsCrossChainConfig config;
    @Autowired
    private ChainManager chainManager;

    /**
     * assembly coinFrom
     *
     * @param listFrom Initiator set coinFrom
     * @return List<CoinFrom>
     * @throws NulsException
     */
    public List<CoinFrom> assemblyCoinFrom(Chain chain, List<CoinDTO> listFrom, boolean isMultiSign) throws NulsException {
        List<CoinFrom> coinFroms = new ArrayList<>();
        byte[] multiAddress = null;
        for (CoinDTO coinDTO : listFrom) {
            String addressStr = coinDTO.getAddress();
            byte[] address = AddressTool.getAddress(addressStr);
            if (isMultiSign) {
                //如果为多签交易，所有froms中有且仅有一个地址，并且只能是多签地址，但可以包含多个资产(from)
                if (null == multiAddress) {
                    multiAddress = address;
                } else if (!Arrays.equals(multiAddress, address)) {
                    chain.getLogger().error("不支持多签账户多账户转账");
                    throw new NulsException(ONLY_ONE_MULTI_SIGNATURE_ADDRESS_ALLOWED);
                }
                if (!AddressTool.isMultiSignAddress(address)) {
                    chain.getLogger().error("普通账户不允许发送多签账户转账交易");
                    throw new NulsException(IS_NOT_MULTI_SIGNATURE_ADDRESS);
                }
            } else {
                //不是多签交易，from中不能有多签地址
                if (AddressTool.isMultiSignAddress(address)) {
                    chain.getLogger().error("普通账户转账中不允许包含多签账户");
                    throw new NulsException(IS_MULTI_SIGNATURE_ADDRESS);
                }
                if (!AccountCall.isEncrypted(addressStr)) {
                    chain.getLogger().error("账户未为加密账户");
                    throw new NulsException(ACCOUNT_NOT_ENCRYPTED);
                }
            }
            if (!AddressTool.validAddress(chain.getChainId(), addressStr)) {
                //转账交易转出地址必须是本链地址
                chain.getLogger().error("跨链交易转出账户不为本链账户");
                throw new NulsException(ADDRESS_IS_NOT_THE_CURRENT_CHAIN);
            }
            int assetChainId = coinDTO.getAssetsChainId();
            int assetId = coinDTO.getAssetsId();
            //检查对应资产余额 是否足够
            BigInteger amount = coinDTO.getAmount();
            Map<String, Object> result = LedgerCall.getBalanceAndNonce(chain, address, assetChainId, assetId);
            byte[] nonce = RPCUtil.decode((String) result.get("nonce"));
            BigInteger balance = new BigInteger(result.get("available").toString());
            if (BigIntegerUtils.isLessThan(balance, amount)) {
                chain.getLogger().error("账户余额不足");
                throw new NulsException(INSUFFICIENT_BALANCE);
            }
            CoinFrom coinFrom = new CoinFrom(address, assetChainId, assetId, amount, nonce, NulsCrossChainConstant.CORSS_TX_LOCKED);
            coinFroms.add(coinFrom);
        }
        return coinFroms;
    }

    /**
     * assembly coinTo 组装to
     * 条件：to中所有地址必须是同一条链的地址
     *
     * @param listTo Initiator set coinTo
     * @return List<CoinTo>
     * @throws NulsException
     */
    public List<CoinTo> assemblyCoinTo(List<CoinDTO> listTo,Chain chain) throws NulsException {
        List<CoinTo> coinTos = new ArrayList<>();
        int receiveChainId = 0;
        for (CoinDTO coinDTO : listTo) {
            byte[] address = AddressTool.getAddress(coinDTO.getAddress());
            if (0 == receiveChainId) {
                receiveChainId = AddressTool.getChainIdByAddress(address);
            } else {
                if (receiveChainId != AddressTool.getChainIdByAddress(address)) {
                    chain.getLogger().error("存在多条连收款方");
                    throw new NulsException(CROSS_TX_PAYEE_CHAIN_NOT_SAME);
                }
            }
            CoinTo coinTo = new CoinTo();
            coinTo.setAddress(address);
            int chainId = coinDTO.getAssetsChainId();
            int assetId = coinDTO.getAssetsId();
            coinTo.setAmount(coinDTO.getAmount());
            coinTo.setAssetsChainId(chainId);
            coinTo.setAssetsId(assetId);
            coinTos.add(coinTo);
        }
        return coinTos;
    }

    /**
     * 验证跨链coin 数据是否存在， from和to不能是同一链
     *
     * @param coinFromList
     * @param coinToList
     * @throws NulsException
     */
    public void verifyCoin(List<CoinFrom> coinFromList, List<CoinTo> coinToList,Chain chain) throws NulsException {
        if (coinFromList.size() == 0 || coinToList.size() == 0) {
            chain.getLogger().error("付款方或收款方为空");
            throw new NulsException(COINDATA_IS_INCOMPLETE);
        }
        byte[] toAddress = coinToList.get(0).getAddress();
        int toChainId = AddressTool.getChainIdByAddress(toAddress);
        byte[] fromAddress = coinFromList.get(0).getAddress();
        int fromChainId = AddressTool.getChainIdByAddress(fromAddress);
        //from和to地址是同一链的地址，则不能创建跨链交易
        if (fromChainId == toChainId) {
            chain.getLogger().error("跨链交易付款方和收款方不能为同一条链账户");
            throw new NulsException(PAYEE_AND_PAYER_IS_THE_SAME_CHAIN);
        }
        //发起链和接收链是否已注册
        if (!chain.isMainChain()) {
            boolean fromChainRegistered = false;
            boolean toChainRegistered = false;
            for (ChainInfo chainInfo : chainManager.getRegisteredCrossChainList()) {
                if (!fromChainRegistered && chainInfo.getChainId() == fromChainId) {
                    fromChainRegistered = true;
                }
                if (!toChainRegistered && chainInfo.getChainId() == toChainId) {
                    toChainRegistered = true;
                }
                if (fromChainRegistered && toChainRegistered) {
                    break;
                }
            }
            if (!fromChainRegistered) {
                chain.getLogger().error("本链{}还未注册跨链", fromChainId);
                throw new NulsException(CURRENT_CHAIN_UNREGISTERED_CROSS_CHAIN);
            }
            if (!toChainRegistered) {
                chain.getLogger().error("目标链{}还未注册跨链", toChainId);
                throw new NulsException(TARGET_CHAIN_UNREGISTERED_CROSS_CHAIN);
            }
            Set<String> verifiedAssets = new HashSet<>();
            for (Coin coin : coinFromList) {
                String key = String.valueOf(coin.getAssetsChainId()) + coin.getAssetsId();
                if (!verifiedAssets.contains(key)) {
                    boolean assetAvailable = false;
                    for (ChainInfo chainInfo : chainManager.getRegisteredCrossChainList()) {
                        assetAvailable = chainInfo.verifyAssetAvailability(coin.getAssetsChainId(), coin.getAssetsId());
                    }
                    if (!assetAvailable) {
                        chain.getLogger().error("链{}的资产{}未注册跨链", coin.getAssetsChainId(), coin.getAssetsId());
                        throw new NulsException(ASSET_UNREGISTERED_CROSS_CHAIN);
                    }
                    verifiedAssets.add(key);
                }
            }
        }
    }

    /**
     * assembly coinData
     * 组装跨链交易在本链的CoinData
     *
     * @param listFrom
     * @param listTo
     * @param txSize
     * @param isLocalCtx Launching cross-chain transactions 是否为发起链
     * @return
     * @throws NulsException
     */
    public CoinData getCoinData(Chain chain, List<CoinFrom> listFrom, List<CoinTo> listTo, int txSize , boolean isLocalCtx) throws NulsException {
        BigInteger feeTotalFrom = BigInteger.ZERO;
        for (CoinFrom coinFrom : listFrom) {
            txSize += coinFrom.size();
            if (isLocalCtx) {
                if (CommonUtil.isLocalAsset(coinFrom)) {
                    feeTotalFrom = feeTotalFrom.add(coinFrom.getAmount());
                }
            } else {
                if (CommonUtil.isNulsAsset(coinFrom)) {
                    feeTotalFrom = feeTotalFrom.add(coinFrom.getAmount());
                }
            }
        }
        BigInteger feeTotalTo = BigInteger.ZERO;
        for (CoinTo coinTo : listTo) {
            txSize += coinTo.size();
            if (isLocalCtx) {
                if (CommonUtil.isLocalAsset(coinTo)) {
                    feeTotalTo = feeTotalTo.add(coinTo.getAmount());
                }
            } else {
                if (CommonUtil.isNulsAsset(coinTo)) {
                    feeTotalTo = feeTotalTo.add(coinTo.getAmount());
                }
            }
        }
        //本交易预计收取的手续费
        BigInteger targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
        //交易中已收取的手续费
        BigInteger actualFee = feeTotalFrom.subtract(feeTotalTo);
        if (BigIntegerUtils.isLessThan(actualFee, BigInteger.ZERO)) {
            chain.getLogger().error("转出金额小于转入金额");
            //所有from中账户的余额总和小于to的总和，不够支付手续费
            throw new NulsException(INSUFFICIENT_FEE);
        } else if (BigIntegerUtils.isLessThan(actualFee, targetFee)) {
            //先从有手续费资产的账户收取
            actualFee = getFeeDirect(chain, listFrom, targetFee, actualFee, isLocalCtx);
            if (BigIntegerUtils.isLessThan(actualFee, targetFee)) {
                //如果没收到足够的手续费，则从CoinFrom中资产不是手续费资产的coin账户中查找资产余额，并组装新的coinfrom来收取手续费
                if (!getFeeIndirect(chain, listFrom, txSize, targetFee, actualFee, isLocalCtx)) {
                    chain.getLogger().error("余额不足");
                    //所有from中账户的余额总和都不够支付手续费
                    throw new NulsException(INSUFFICIENT_FEE);
                }
            }
        }
        CoinData coinData = new CoinData();
        coinData.setFrom(listFrom);
        coinData.setTo(listTo);
        return coinData;
    }

    /**
     * Only collect fees from CoinFrom's coins whose assets are nuls, and return the actual amount charged.
     * 只从CoinFrom中资产为指定资产的coin中收取手续费，返回实际收取的数额
     *
     * @param listFrom  All coins transferred out 转出的所有coin
     * @param targetFee The amount of the fee that needs to be charged 需要收取的手续费数额
     * @param actualFee Actual amount charged 实际收取的数额
     * @param isLocalCtx Launching cross-chain transactions 是否为发起链
     * @return BigInteger The amount of the fee actually charged 实际收取的手续费数额
     * @throws NulsException
     */
    private BigInteger getFeeDirect(Chain chain, List<CoinFrom> listFrom, BigInteger targetFee, BigInteger actualFee, boolean isLocalCtx) throws NulsException {
        BigInteger balance;
        int chainId;
        int assertId;
        if(!isLocalCtx){
            chainId = config.getMainChainId();
            assertId = config.getMainAssetId();
        }else{
            chainId = chain.getChainId();
            assertId = chain.getConfig().getAssetId();
        }
        for (CoinFrom coinFrom : listFrom) {
            boolean isDirectCoin = (isLocalCtx && CommonUtil.isLocalAsset(coinFrom)) || (!isLocalCtx && CommonUtil.isNulsAsset(coinFrom));
            if(!isDirectCoin){
                continue;
            }
            balance = LedgerCall.getBalanceNonce(chain, coinFrom.getAddress(), chainId, assertId);
            if (balance.compareTo(BigInteger.ZERO) == 0) {
                continue;
            }
            //可用余额=当前余额减去本次转出
            balance = balance.subtract(coinFrom.getAmount());
            //当前还差的手续费
            BigInteger current = targetFee.subtract(actualFee);
            //如果余额大于等于目标手续费，则直接收取全额手续费
            if (BigIntegerUtils.isEqualOrGreaterThan(balance, current)) {
                coinFrom.setAmount(coinFrom.getAmount().add(current));
                actualFee = actualFee.add(current);
                break;
            } else if (BigIntegerUtils.isGreaterThan(balance, BigInteger.ZERO)) {
                coinFrom.setAmount(coinFrom.getAmount().add(balance));
                actualFee = actualFee.add(balance);
            }
        }
        return actualFee;
    }

    /**
     * 从CoinFrom中资产不为nuls的coin中收取nuls手续费，返回是否收取完成
     * Only collect the nuls fee from the coin in CoinFrom whose assets are not nuls, and return whether the charge is completed.
     *
     * @param listFrom  All coins transferred out 转出的所有coin
     * @param txSize    Current transaction size
     * @param targetFee Estimated fee
     * @param actualFee actual Fee
     * @return boolean
     * @throws NulsException
     */
    private boolean getFeeIndirect(Chain chain, List<CoinFrom> listFrom, int txSize, BigInteger targetFee, BigInteger actualFee, boolean isLocalCtx) throws NulsException {
        BigInteger balance;
        int chainId;
        int assertId;
        //如果为发起链则收取本链主资产做手续费，否则收取主网主资产做手续费
        if(!isLocalCtx){
            chainId = config.getMainChainId();
            assertId = config.getMainAssetId();
        }else{
            chainId = chain.getChainId();
            assertId = chain.getConfig().getAssetId();
        }
        List<CoinFrom> newCoinFromList = new ArrayList<>();
        for (CoinFrom coinFrom : listFrom) {
            boolean isDirectCoin = (isLocalCtx && CommonUtil.isLocalAsset(coinFrom)) || (!isLocalCtx && CommonUtil.isNulsAsset(coinFrom));
            if (isDirectCoin) {
                continue;
            }
            balance = LedgerCall.getBalanceNonce(chain, coinFrom.getAddress(), chainId, assertId);
            if (BigIntegerUtils.isEqualOrLessThan(balance, BigInteger.ZERO)) {
                continue;
            }
            CoinFrom feeCoinFrom = new CoinFrom();
            byte[] address = coinFrom.getAddress();
            feeCoinFrom.setAddress(address);
            feeCoinFrom.setNonce(LedgerCall.getNonce(chain, AddressTool.getStringAddressByBytes(address), chainId, assertId));
            txSize += feeCoinFrom.size();
            //新增coinfrom，重新计算本交易预计收取的手续费
            targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
            //当前还差的手续费
            BigInteger current = targetFee.subtract(actualFee);
            //此账户可以支付的手续费
            BigInteger fee = BigIntegerUtils.isEqualOrGreaterThan(balance, current) ? current : balance;

            feeCoinFrom.setLocked(NulsCrossChainConstant.CORSS_TX_LOCKED);
            feeCoinFrom.setAssetsChainId(chainId);
            feeCoinFrom.setAssetsId(assertId);
            feeCoinFrom.setAmount(fee);

            newCoinFromList.add(feeCoinFrom);
            actualFee = actualFee.add(fee);
            if (BigIntegerUtils.isEqualOrGreaterThan(actualFee, targetFee)) {
                break;
            }
        }
        listFrom.addAll(newCoinFromList);
        //最终的实际收取数额大于等于预计收取数额，则可以正确组装CoinData
        if (BigIntegerUtils.isEqualOrGreaterThan(actualFee, targetFee)) {
            return true;
        }
        return false;
    }

    /**
     * 通过coinfrom计算签名数据的size
     * 如果coinfrom有重复地址则只计算一次；如果有多签地址，只计算m个地址的size
     *
     * @param coinFroms
     * @return
     */
    public int getSignatureSize(List<CoinFrom> coinFroms) {
        int size = 0;
        Set<String> signAddress = new HashSet<>();
        for (CoinFrom coinFrom : coinFroms) {
            byte[] address = coinFrom.getAddress();
            signAddress.add(AddressTool.getStringAddressByBytes(address));
        }
        size += signAddress.size() * P2PHKSignature.SERIALIZE_LENGTH;
        return size;
    }
}
