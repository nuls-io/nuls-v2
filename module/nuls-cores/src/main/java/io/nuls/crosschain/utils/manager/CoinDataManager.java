package io.nuls.crosschain.utils.manager;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.Coin;
import io.nuls.base.data.CoinData;
import io.nuls.base.data.CoinFrom;
import io.nuls.base.data.CoinTo;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.base.model.dto.input.CoinDTO;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.rpc.call.LedgerCall;
import io.nuls.crosschain.utils.CommonUtil;

import java.math.BigInteger;
import java.util.*;

/**
 * Cross chain moduleCoinDataManagement
 *
 * @author tag
 */
@Component
public class CoinDataManager {
    @Autowired
    private NulsCoresConfig config;
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
                //If it is a multi sign transaction, allfromsThere is only one address in it, and it can only be multiple signed addresses, but it can contain multiple assets(from)
                if (null == multiAddress) {
                    multiAddress = address;
                } else if (!Arrays.equals(multiAddress, address)) {
                    chain.getLogger().error("Multiple account transfers are not supported with multiple signatures");
                    throw new NulsException(NulsCrossChainErrorCode.ONLY_ONE_MULTI_SIGNATURE_ADDRESS_ALLOWED);
                }
                if (!AddressTool.isMultiSignAddress(address)) {
                    chain.getLogger().error("Ordinary accounts do not allow sending multiple account transfer transactions");
                    throw new NulsException(NulsCrossChainErrorCode.IS_NOT_MULTI_SIGNATURE_ADDRESS);
                }
            } else {
                //It's not about signing multiple transactions,fromMultiple signed addresses are not allowed in the middle
                if (AddressTool.isMultiSignAddress(address)) {
                    chain.getLogger().error("Multiple signature accounts are not allowed in regular account transfers");
                    throw new NulsException(NulsCrossChainErrorCode.IS_MULTI_SIGNATURE_ADDRESS);
                }
            }
            if (!AddressTool.validAddress(chain.getChainId(), addressStr)) {
                //The transfer transaction transfer address must be a local chain address
                chain.getLogger().error("Cross chain transaction transfer out account is not a local chain account");
                throw new NulsException(NulsCrossChainErrorCode.ADDRESS_IS_NOT_THE_CURRENT_CHAIN);
            }
            int assetChainId = coinDTO.getAssetsChainId();
            int assetId = coinDTO.getAssetsId();
            //Check the corresponding asset balance Is it sufficient
            BigInteger amount = coinDTO.getAmount();
            Map<String, Object> result = LedgerCall.getBalanceAndNonce(chain, addressStr, assetChainId, assetId);
            byte[] nonce = RPCUtil.decode((String) result.get("nonce"));
            BigInteger balance = new BigInteger(result.get("available").toString());
            if (BigIntegerUtils.isLessThan(balance, amount)) {
                chain.getLogger().error("Insufficient account balance");
                throw new NulsException(NulsCrossChainErrorCode.INSUFFICIENT_BALANCE);
            }
            CoinFrom coinFrom = new CoinFrom(address, assetChainId, assetId, amount, nonce, NulsCrossChainConstant.CORSS_TX_LOCKED);
            coinFroms.add(coinFrom);
        }
        return coinFroms;
    }

    /**
     * assembly coinTo assembleto
     * condition：toAll addresses in the must be addresses on the same chain
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
                    chain.getLogger().error("There are multiple consecutive payees");
                    throw new NulsException(NulsCrossChainErrorCode.CROSS_TX_PAYEE_CHAIN_NOT_SAME);
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
     * Verify cross chaincoin Does the data exist, fromandtoCannot be the same chain
     *
     * @param coinFromList
     * @param coinToList
     * @throws NulsException
     */
    public void verifyCoin(List<CoinFrom> coinFromList, List<CoinTo> coinToList,Chain chain) throws NulsException {
        if (coinFromList.size() == 0 || coinToList.size() == 0) {
            chain.getLogger().error("The payer or payee is empty");
            throw new NulsException(NulsCrossChainErrorCode.COINDATA_IS_INCOMPLETE);
        }
        byte[] toAddress = coinToList.get(0).getAddress();
        int toChainId = AddressTool.getChainIdByAddress(toAddress);
        byte[] fromAddress = coinFromList.get(0).getAddress();
        int fromChainId = AddressTool.getChainIdByAddress(fromAddress);
        //fromandtoIf the address is on the same chain, cross chain transactions cannot be created
        if (fromChainId == toChainId) {
            chain.getLogger().error("The payer and payee of cross chain transactions cannot be the same chain account");
            throw new NulsException(NulsCrossChainErrorCode.PAYEE_AND_PAYER_IS_THE_SAME_CHAIN);
        }
        //Has the initiating chain and receiving chain been registered
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
                chain.getLogger().error("This chain{}Cross chain registration not yet registered", fromChainId);
                throw new NulsException(NulsCrossChainErrorCode.CURRENT_CHAIN_UNREGISTERED_CROSS_CHAIN);
            }
            if (!toChainRegistered) {
                chain.getLogger().error("Target Chain{}Cross chain registration not yet registered", toChainId);
                throw new NulsException(NulsCrossChainErrorCode.TARGET_CHAIN_UNREGISTERED_CROSS_CHAIN);
            }
            Set<String> verifiedAssets = new HashSet<>();
            for (Coin coin : coinFromList) {
                String key = String.valueOf(coin.getAssetsChainId()) + NulsCrossChainConstant.STRING_SPLIT + coin.getAssetsId();
                if (!verifiedAssets.contains(key)) {
                    boolean assetAvailable = false;
                    for (ChainInfo chainInfo : chainManager.getRegisteredCrossChainList()) {
                        assetAvailable = chainInfo.verifyAssetAvailability(coin.getAssetsChainId(), coin.getAssetsId());
                        if(assetAvailable){
                            break;
                        }
                    }
                    if (!assetAvailable) {
                        chain.getLogger().error("chain{}Assets of{}Unregistered cross chain", coin.getAssetsChainId(), coin.getAssetsId());
                        throw new NulsException(NulsCrossChainErrorCode.ASSET_UNREGISTERED_CROSS_CHAIN);
                    }
                    verifiedAssets.add(key);
                }
            }
        }
    }

    /**
     * assembly coinData
     * Assembling cross chain transactions within this chainCoinData
     *
     * @param listFrom
     * @param listTo
     * @param txSize
     * @param isMainNet Launching cross-chain transactions Is it the main network
     * @return
     * @throws NulsException
     */
    public CoinData getCrossCoinData(Chain chain, List<CoinFrom> listFrom, List<CoinTo> listTo, int txSize , boolean isMainNet) throws NulsException {
        CoinData coinData = new CoinData();
        coinData.setFrom(listFrom);
        coinData.setTo(listTo);
        txSize += coinData.size();

        //Asset handling fees for this chain
        BigInteger localFeeTotalFrom = BigInteger.ZERO;
        //Cross chain handling fees（The main asset of the main network can only be obtained through parallel chain initiation）
        BigInteger crossFeeTotalFrom = BigInteger.ZERO;
        for (CoinFrom coinFrom : listFrom) {
            if (CommonUtil.isLocalAsset(coinFrom)) {
                localFeeTotalFrom = localFeeTotalFrom.add(coinFrom.getAmount());
                continue;
            }
            if (!isMainNet && CommonUtil.isNulsAsset(coinFrom)) {
                crossFeeTotalFrom = crossFeeTotalFrom.add(coinFrom.getAmount());
            }
        }

        BigInteger localFeeTotalTo = BigInteger.ZERO;
        BigInteger crossFeeTotalTo = BigInteger.ZERO;
        for (CoinTo coinTo : listTo) {
            if (CommonUtil.isLocalAsset(coinTo)) {
                localFeeTotalTo = localFeeTotalTo.add(coinTo.getAmount());
                continue;
            }
            if (!isMainNet && CommonUtil.isNulsAsset(coinTo)) {
                crossFeeTotalTo = crossFeeTotalTo.add(coinTo.getAmount());
            }
        }

        //The expected handling fee for this transaction
        BigInteger targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
        //The transaction fees already charged for this chain in the transaction
        BigInteger localActualFee = localFeeTotalFrom.subtract(localFeeTotalTo);
        if (BigIntegerUtils.isLessThan(localActualFee, BigInteger.ZERO)) {
            chain.getLogger().error("The amount transferred out is less than the amount transferred in");
            //AllfromThe total balance of the middle account is less thantoThe total amount is not enough to pay the handling fee
            throw new NulsException(NulsCrossChainErrorCode.INSUFFICIENT_FEE);
        } else if (BigIntegerUtils.isLessThan(localActualFee, targetFee)) {
            //Collect from the account with handling fee assets first
            localActualFee = getFeeDirect(chain, listFrom, targetFee, localActualFee, true);
            if (BigIntegerUtils.isLessThan(localActualFee, targetFee)) {
                //If you have not received enough transaction fees, you will receive them fromCoinFromMedium assets are not fee assetscoinSearch for asset balance in the account and assemble a new onecoinfromTo collect handling fees
                if (!getFeeIndirect(chain, listFrom, txSize, targetFee, localActualFee, true)) {
                    chain.getLogger().error("Insufficient balance");
                    //AllfromThe total balance of the middle account is not enough to pay the handling fee
                    throw new NulsException(NulsCrossChainErrorCode.INSUFFICIENT_FEE);
                }
            }
        }

        //If it is not the main network, verify the assembly cross chain handling fee
        if (!isMainNet){
            //Transaction fees already collected
            BigInteger crossActualFee = crossFeeTotalFrom.subtract(crossFeeTotalTo);
            if (BigIntegerUtils.isLessThan(crossActualFee, BigInteger.ZERO)) {
                chain.getLogger().error("The amount transferred out is less than the amount transferred in");
                //AllfromThe total balance of the middle account is less thantoThe total amount is not enough to pay the handling fee
                throw new NulsException(NulsCrossChainErrorCode.INSUFFICIENT_FEE);
            } else if (BigIntegerUtils.isLessThan(crossActualFee, targetFee)) {
                //Collect from the account with handling fee assets first
                crossActualFee = getFeeDirect(chain, listFrom, targetFee, crossActualFee, false);
                if (BigIntegerUtils.isLessThan(crossActualFee, targetFee)) {
                    //If you have not received enough transaction fees, you will receive them fromCoinFromMedium assets are not fee assetscoinSearch for asset balance in the account and assemble a new onecoinfromTo collect handling fees
                    if (!getFeeIndirect(chain, listFrom, txSize, targetFee, crossActualFee, false)) {
                        chain.getLogger().error("Insufficient balance");
                        //AllfromThe total balance of the middle account is not enough to pay the handling fee
                        throw new NulsException(NulsCrossChainErrorCode.INSUFFICIENT_FEE);
                    }
                }
            }
        }
        return coinData;
    }

    /**
     * assembly coinData
     * Assembling cross chain transactions within this chainCoinData
     *
     * @param listFrom
     * @param listTo
     * @param txSize
     * @param isMainNet Launching cross-chain transactions Is it the main network
     * @return
     * @throws NulsException
     */
    public CoinData getCoinData(Chain chain, List<CoinFrom> listFrom, List<CoinTo> listTo, int txSize , boolean isMainNet) throws NulsException {
        BigInteger feeTotalFrom = BigInteger.ZERO;
        for (CoinFrom coinFrom : listFrom) {
            txSize += coinFrom.size();
            if (isMainNet) {
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
            if (isMainNet) {
                if (CommonUtil.isLocalAsset(coinTo)) {
                    feeTotalTo = feeTotalTo.add(coinTo.getAmount());
                }
            } else {
                if (CommonUtil.isNulsAsset(coinTo)) {
                    feeTotalTo = feeTotalTo.add(coinTo.getAmount());
                }
            }
        }
        //The expected handling fee for this transaction
        BigInteger targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
        //Transaction fees already collected
        BigInteger actualFee = feeTotalFrom.subtract(feeTotalTo);
        if (BigIntegerUtils.isLessThan(actualFee, BigInteger.ZERO)) {
            chain.getLogger().error("The amount transferred out is less than the amount transferred in");
            //AllfromThe total balance of the middle account is less thantoThe total amount is not enough to pay the handling fee
            throw new NulsException(NulsCrossChainErrorCode.INSUFFICIENT_FEE);
        } else if (BigIntegerUtils.isLessThan(actualFee, targetFee)) {
            //Collect from the account with handling fee assets first
            actualFee = getFeeDirect(chain, listFrom, targetFee, actualFee, isMainNet);
            if (BigIntegerUtils.isLessThan(actualFee, targetFee)) {
                //If you have not received enough transaction fees, you will receive them fromCoinFromMedium assets are not fee assetscoinSearch for asset balance in the account and assemble a new onecoinfromTo collect handling fees
                if (!getFeeIndirect(chain, listFrom, txSize, targetFee, actualFee, isMainNet)) {
                    chain.getLogger().error("Insufficient balance");
                    //AllfromThe total balance of the middle account is not enough to pay the handling fee
                    throw new NulsException(NulsCrossChainErrorCode.INSUFFICIENT_FEE);
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
     * Only fromCoinFromMedium assets are designated assetscoinCollect transaction fees and return the actual amount collected
     *
     * @param listFrom  All coins transferred out All transferred outcoin
     * @param targetFee The amount of the fee that needs to be charged The amount of handling fee to be charged
     * @param actualFee Actual amount charged Actual amount collected
     * @param isLocalFee Launching cross-chain transactions Is it for collecting transaction fees on this chain
     * @return BigInteger The amount of the fee actually charged The actual amount of handling fees collected
     * @throws NulsException
     */
    private BigInteger getFeeDirect(Chain chain, List<CoinFrom> listFrom, BigInteger targetFee, BigInteger actualFee, boolean isLocalFee) throws NulsException {
        BigInteger balance;
        int chainId;
        int assertId;
        if(!isLocalFee){
            chainId = config.getMainChainId();
            assertId = config.getMainAssetId();
        }else{
            chainId = chain.getChainId();
            assertId = chain.getConfig().getAssetId();
        }
        for (CoinFrom coinFrom : listFrom) {
            boolean isDirectCoin = (isLocalFee && CommonUtil.isLocalAsset(coinFrom)) || (!isLocalFee && CommonUtil.isNulsAsset(coinFrom));
            if(!isDirectCoin){
                continue;
            }
            balance = LedgerCall.getBalanceNonce(chain, coinFrom.getAddress(), chainId, assertId);
            if (balance.compareTo(BigInteger.ZERO) == 0) {
                continue;
            }
            //Available balance=Current balance minus current transfer out
            balance = balance.subtract(coinFrom.getAmount());
            //Current outstanding handling fees
            BigInteger current = targetFee.subtract(actualFee);
            //If the balance is greater than or equal to the target handling fee, the full handling fee will be charged directly
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
     * fromCoinFromMedium assets are notnulsofcoinMiddle collectionnulsHandling fee, whether the return has been collected and completed
     * Only collect the nuls fee from the coin in CoinFrom whose assets are not nuls, and return whether the charge is completed.
     *
     * @param listFrom  All coins transferred out All transferred outcoin
     * @param txSize    Current transaction size
     * @param targetFee Estimated fee
     * @param actualFee actual Fee
     * @param isLocalFee Launching cross-chain transactions Is it for collecting transaction fees on this chain
     * @return boolean
     * @throws NulsException
     */
    private boolean getFeeIndirect(Chain chain, List<CoinFrom> listFrom, int txSize, BigInteger targetFee, BigInteger actualFee, boolean isLocalFee) throws NulsException {
        BigInteger balance;
        int chainId;
        int assertId;
        //If the chain is initiated, a handling fee will be charged to the main asset of the chain; otherwise, a handling fee will be charged to the main network asset
        if(!isLocalFee){
            chainId = config.getMainChainId();
            assertId = config.getMainAssetId();
        }else{
            chainId = chain.getChainId();
            assertId = chain.getConfig().getAssetId();
        }
        List<CoinFrom> newCoinFromList = new ArrayList<>();
        for (CoinFrom coinFrom : listFrom) {
            boolean isDirectCoin = (isLocalFee && CommonUtil.isLocalAsset(coinFrom)) || (!isLocalFee && CommonUtil.isNulsAsset(coinFrom));
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
            //New additioncoinfromRecalculate the expected transaction fees for this transaction
            targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
            //Current outstanding handling fees
            BigInteger current = targetFee.subtract(actualFee);
            //The handling fees that this account can pay
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
        //If the final actual collection amount is greater than or equal to the expected collection amount, it can be assembled correctlyCoinData
        if (BigIntegerUtils.isEqualOrGreaterThan(actualFee, targetFee)) {
            return true;
        }
        return false;
    }

    /**
     * adoptcoinfromCalculate signature datasize
     * IfcoinfromCalculate only once if there are duplicate addresses；If there are multiple addresses, only calculatemAddressessize
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
