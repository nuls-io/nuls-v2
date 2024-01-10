package io.nuls.crosschain.utils.validator;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.base.signture.P2PHKSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.base.signture.TransactionSignature;
import io.nuls.common.NulsCoresConfig;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.ECKey;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.crosschain.base.model.bo.ChainInfo;
import io.nuls.crosschain.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.model.bo.Chain;
import io.nuls.crosschain.rpc.call.ChainManagerCall;
import io.nuls.crosschain.srorage.ConvertCtxService;
import io.nuls.crosschain.srorage.ConvertHashService;
import io.nuls.crosschain.srorage.RegisteredCrossChainService;
import io.nuls.crosschain.utils.CommonUtil;
import io.nuls.crosschain.utils.TxUtil;
import io.nuls.crosschain.utils.manager.ChainManager;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Cross chain transaction verification tool class
 * Transaction Verification Tool Class
 *
 * @author tag
 * 2019/4/15
 */
@Component
public class CrossTxValidator {
    @Autowired
    private NulsCoresConfig config;

    @Autowired
    private ConvertHashService convertHashService;

    @Autowired
    private ConvertCtxService convertCtxService;

    @Autowired
    private ChainManager chainManager;

    @Autowired
    RegisteredCrossChainService registeredCrossChainService;

    /**
     * Verify transactions
     * Verifying transactions
     *
     * @param chain           chainID/chain id
     * @param tx              transaction/transaction info
     * @param blockHeader     Block header information/block header info
     * @return boolean
     */
    @SuppressWarnings("unchecked")
    public boolean validateTx(Chain chain, Transaction tx, BlockHeader blockHeader) throws NulsException, IOException{
        //Determine whether this cross chain transaction belongs to this chain
        CoinData coinData = tx.getCoinDataInstance();
        //If this chain is the initiating chain and not the main chain,Then cross chain transaction verification and signature verification of the main network protocol need to be generated
        int fromChainId = AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress());
        int toChainId = AddressTool.getChainIdByAddress(coinData.getTo().get(0).getAddress());

        if (ProtocolGroupManager.getCurrentVersion(chain.getChainId()) >= 18) {
            if (coinData.getTo().size() != 1) {
                throw new NulsException(NulsCrossChainErrorCode.TO_ADDRESS_ERROR);
            }
        }
        
        if(toChainId == 0){
            throw new NulsException(NulsCrossChainErrorCode.TO_ADDRESS_ERROR);
        }
        //This chain protocol does not require signature Byzantine verification for cross chain transactions, only transaction signatures need to be verified
        if(chain.getChainId() == fromChainId){
            if(tx.getType() == TxType.CROSS_CHAIN){
                for (CoinFrom from : coinData.getFrom()) {
                    //If the contract address is not included in the deduplication judgment
                    if (AddressTool.validContractAddress(from.getAddress(),AddressTool.getChainIdByAddress(from.getAddress()))) {
                        continue;
                    }
                    //todo
                    if (!registeredCrossChainService.canCross(from.getAssetsChainId(),from.getAssetsId())){
//                        throw new NulsException(NulsCrossChainErrorCode.ASSET_NOT_REG_CROSS_CHAIN);
                    }

                }
                if (!coinDataValid(chain, coinData, tx.size())) {
                    throw new NulsException(NulsCrossChainErrorCode.COINDATA_VERIFY_FAIL);
                }
                //validateFromHave all the addresses in the middle been signed
                Set<String> fromAddressSet = tx.getCoinDataInstance().getFromAddressList();
                TransactionSignature transactionSignature = new TransactionSignature();
                transactionSignature.parse(tx.getTransactionSignature(), 0);
                String signAddress;
                boolean verifyResult = false;
                byte[] txHashByte = tx.getHash().getBytes();
                for (P2PHKSignature signature : transactionSignature.getP2PHKSignatures()){
                    if (!ECKey.verify(txHashByte, signature.getSignData().getSignBytes(), signature.getPublicKey())) {
                        chain.getLogger().error("Signature verification failed");
                        throw new NulsException(new Exception("Transaction signature error !"));
                    }
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
            }
        }else{
            //When verifying transaction fees, do not verify the space occupied by signature data
            int validateTxSize = tx.size() - SerializeUtils.sizeOfBytes(tx.getTransactionSignature());
            if (!coinDataValid(chain, coinData, validateTxSize)) {
                throw new NulsException(NulsCrossChainErrorCode.COINDATA_VERIFY_FAIL);
            }
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
                realCtx = TxUtil.friendConvertToMain(chain, tx, txType, true);
            }
            chainInfo = chainManager.getChainInfo(verifierChainId);
            if(chainInfo == null){
                chain.getLogger().error("Chain not registered,chainId:{}",verifierChainId);
                throw new NulsException(NulsCrossChainErrorCode.CHAIN_UNREGISTERED);
            }
            verifierList = new ArrayList<>(chainInfo.getVerifierList());
            if(verifierList.isEmpty()){
                chain.getLogger().error("The chain has not registered a verifier yet,chainId:{}",verifierChainId);
                throw new NulsException(NulsCrossChainErrorCode.CHAIN_UNREGISTERED_VERIFIER);
            }
            minPassCount = chainInfo.getMinPassCount();

            if(!SignatureUtil.validateCtxSignture(realCtx)){
                chain.getLogger().info("Main network protocol cross chain transaction signature verification failed！");
                throw new NulsException(NulsCrossChainErrorCode.SIGNATURE_ERROR);
            }

            if(!TxUtil.signByzantineVerify(chain, realCtx, verifierList, minPassCount, verifierChainId)){
                chain.getLogger().info("Signature Byzantine verification failed！");
                throw new NulsException(NulsCrossChainErrorCode.CTX_SIGN_BYZANTINE_FAIL);
            }
        }

        if(config.isMainNet()){
            if(!ChainManagerCall.verifyCtxAsset(fromChainId, tx)){
                chain.getLogger().info("Cross chain asset verification failed！");
                throw new NulsException(NulsCrossChainErrorCode.CROSS_ASSERT_VALID_ERROR);
            }
        }
        return true;
    }


    public boolean coinDataValid(Chain chain, CoinData coinData, int txSize)throws NulsException{
        return coinDataValid(chain,coinData,txSize,true);
    }

    /**
     * CoinDataBasic verification
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
            chain.getLogger().error("The transferor or transferee is empty");
            throw new NulsException(NulsCrossChainErrorCode.COINFROM_NOT_FOUND);
        }
        int fromChainId = 0;
        int toChainId = 0;
        //Cross chain transactionsfromThe middle address must be an address on the same chain,toThe address in the must be a chain address
        for (CoinFrom coinFrom : coinFromList) {
            if (fromChainId == 0) {
                fromChainId = AddressTool.getChainIdByAddress(coinFrom.getAddress());
            }
            if (AddressTool.getChainIdByAddress(coinFrom.getAddress()) != fromChainId) {
                chain.getLogger().error("The transferor of cross chain transactions has multiple chain accounts");
                throw new NulsException(NulsCrossChainErrorCode.CROSS_TX_PAYER_CHAIN_NOT_SAME);
            }
        }
        for (CoinTo coinTo : coinToList) {
            if (toChainId == 0) {
                toChainId = AddressTool.getChainIdByAddress(coinTo.getAddress());
            }
            if (AddressTool.getChainIdByAddress(coinTo.getAddress()) != toChainId) {
                chain.getLogger().error("Cross chain transaction transferee has multiple chain accounts");
                throw new NulsException(NulsCrossChainErrorCode.CROSS_TX_PAYEE_CHAIN_NOT_SAME);
            }
        }
        //fromandtoCannot be the same address
        if (fromChainId == toChainId) {
            chain.getLogger().error("The transferor and transferee of cross chain transactions are the same chain account");
            throw new NulsException(NulsCrossChainErrorCode.PAYEE_AND_PAYER_IS_THE_SAME_CHAIN);
        }
        //Check if this cross chain transaction is related to this chain
        int chainId = chain.getChainId();
        if (fromChainId != chainId && toChainId != chainId && !config.isMainNet()) {
            chain.getLogger().error("This cross chain transaction is not a local cross chain transaction");
            throw new NulsException(NulsCrossChainErrorCode.NOT_BELONG_TO_CURRENT_CHAIN);
        }
        //If this chain is not the initiating chain, verifyCoinDataIs the main assets of the main network sufficient to pay transaction fees
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
            //The expected handling fee for this transaction
            BigInteger targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
            //Transaction fees already collected
            BigInteger actualFee = feeTotalFrom.subtract(feeTotalTo);
            if(BigIntegerUtils.isLessThan(actualFee, targetFee)){
                chain.getLogger().error("Insufficient handling fees");
                throw new NulsException(NulsCrossChainErrorCode.INSUFFICIENT_FEE);
            }
        }
        return true;
    }
}
