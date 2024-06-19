/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.transaction.service.impl;

import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.basic.TransactionFeeCalculator;
import io.nuls.base.data.*;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.base.protocol.TxRegisterDetail;
import io.nuls.base.signture.MultiSignTxSignature;
import io.nuls.base.signture.SignatureUtil;
import io.nuls.contract.config.ContractContext;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.constant.TxStatusEnum;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.log.logback.NulsLogger;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.ByteArrayWrapper;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.parse.SerializeUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.core.thread.commom.NulsThreadFactory;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.common.NulsCoresConfig;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxContext;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.TxManager;
import io.nuls.transaction.model.bo.*;
import io.nuls.transaction.model.dto.AccountBlockDTO;
import io.nuls.transaction.model.dto.ModuleTxRegisterDTO;
import io.nuls.transaction.model.po.TransactionConfirmedPO;
import io.nuls.transaction.model.po.TransactionNetPO;
import io.nuls.transaction.model.po.TransactionUnconfirmedPO;
import io.nuls.transaction.rpc.call.*;
import io.nuls.transaction.service.ConfirmedTxService;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.storage.ConfirmedTxStorageService;
import io.nuls.transaction.storage.UnconfirmedTxStorageService;
import io.nuls.transaction.utils.TxDuplicateRemoval;
import io.nuls.transaction.utils.TxUtil;

import java.io.IOException;
import java.math.BigInteger;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import static io.nuls.transaction.constant.TxConstant.CACHED_SIZE;

/**
 * @author: Charlie
 * @date: 2018/11/22
 */
@Component
public class TxServiceImpl implements TxService {

    @Autowired
    private PackablePool packablePool;

    @Autowired
    private UnconfirmedTxStorageService unconfirmedTxStorageService;

    @Autowired
    private ConfirmedTxService confirmedTxService;

    @Autowired
    private ConfirmedTxStorageService confirmedTxStorageService;

    @Autowired
    private NulsCoresConfig txConfig;

    private ExecutorService verifySignExecutor = ThreadUtils.createThreadPool(Runtime.getRuntime().availableProcessors(), CACHED_SIZE, new NulsThreadFactory(TxConstant.VERIFY_TX_SIGN_THREAD));

    @Override
    public boolean register(Chain chain, ModuleTxRegisterDTO moduleTxRegisterDto) {
        try {
            for (TxRegisterDetail txRegisterDto : moduleTxRegisterDto.getList()) {
                TxRegister txRegister = new TxRegister();
                txRegister.setModuleCode(moduleTxRegisterDto.getModuleCode());
                txRegister.setTxType(txRegisterDto.getTxType());
                txRegister.setSystemTx(txRegisterDto.getSystemTx());
                txRegister.setUnlockTx(txRegisterDto.getUnlockTx());
                txRegister.setVerifySignature(txRegisterDto.getVerifySignature());
                txRegister.setVerifyFee(txRegisterDto.getVerifyFee());
                chain.getTxRegisterMap().put(txRegister.getTxType(), txRegister);
                chain.getLogger().info("register:{}", JSONUtils.obj2json(txRegister));
            }
            List<Integer> delList = moduleTxRegisterDto.getDelList();
            if (!delList.isEmpty()) {
                delList.forEach(e -> chain.getTxRegisterMap().remove(e));
            }
            return true;
        } catch (Exception e) {
            chain.getLogger().error(e);
        }
        return false;
    }

    @Override
    public void newBroadcastTx(Chain chain, TransactionNetPO txNet) {
        Transaction tx = txNet.getTx();
        if (!isTxExists(chain, tx.getHash())) {
            try {
                verifyTransactionInCirculation(chain, tx);
                //todo fro 2.18.0 version
                CoinData cd = tx.getCoinDataInstance();
                for (CoinFrom from : cd.getFrom()) {
                    if (chain.getChainId() == 1 && AddressTool.getChainIdByAddress(from.getAddress()) == 2) {
                        throw new NulsException(TxErrorCode.INVALID_ADDRESS, "address is testnet address Exception");
                    }
                }
                for (CoinTo to : cd.getTo()) {
                    if (chain.getChainId() == 1 && AddressTool.getChainIdByAddress(to.getAddress()) == 2) {
                        throw new NulsException(TxErrorCode.INVALID_ADDRESS, "address is testnet address Exception");
                    }
                }
                chain.getUnverifiedQueue().addLast(txNet);
            } catch (NulsException e) {
                chain.getLogger().error(e);
            } catch (IllegalStateException e) {
                chain.getLogger().error("UnverifiedQueue full!");
            }
        }
    }


    @Override
    public void newTx(Chain chain, Transaction tx) throws NulsException {
        try {
            if (!chain.getProcessTxStatus().get()) {
                //Node block synchronization or rollback,Suspend acceptance of new transactions
                throw new NulsException(TxErrorCode.PAUSE_NEWTX);
            }
            NulsHash hash = tx.getHash();
            if (isTxExists(chain, hash)) {
                throw new NulsException(TxErrorCode.TX_ALREADY_EXISTS);
            }
            VerifyResult verifyResult = verify(chain, tx);
            if (!verifyResult.getResult()) {
                chain.getLogger().error("verify failed: type:{} - txhash:{}, code:{}",
                        tx.getType(), hash.toHex(), verifyResult.getErrorCode().getCode());
                throw new NulsException(ErrorCode.init(verifyResult.getErrorCode().getCode()));
            }

            //todo fro 2.18.0 version
            CoinData cd = tx.getCoinDataInstance();
            for (CoinFrom from : cd.getFrom()) {
                if (chain.getChainId() == 1 && AddressTool.getChainIdByAddress(from.getAddress()) == 2) {
                    throw new NulsException(TxErrorCode.INVALID_ADDRESS, "address is testnet address Exception");
                }
            }
            for (CoinTo to : cd.getTo()) {
                if (chain.getChainId() == 1 && AddressTool.getChainIdByAddress(to.getAddress()) == 2) {
                    throw new NulsException(TxErrorCode.INVALID_ADDRESS, "address is testnet address Exception");
                }
            }


            VerifyLedgerResult verifyLedgerResult = LedgerCall.commitUnconfirmedTx(chain, RPCUtil.encode(tx.serialize()));
            if (!verifyLedgerResult.businessSuccess()) {

                String errorCode = verifyLedgerResult.getErrorCode() == null ? TxErrorCode.ORPHAN_TX.getCode() : verifyLedgerResult.getErrorCode().getCode();
                chain.getLogger().error(
                        "coinData verify fail - orphan: {}, - code:{}, type:{} - txhash:{}", verifyLedgerResult.getOrphan(),
                        errorCode, tx.getType(), hash.toHex());
                throw new NulsException(ErrorCode.init(errorCode));
            }
            if (chain.getPackaging().get()) {
                //IfmapIf it is full, it may not necessarily be able to join the queue for packaging
                packablePool.add(chain, tx);
            }
            unconfirmedTxStorageService.putTx(chain.getChainId(), tx);
            //Broadcast complete transactions
            boolean broadcastResult = false;
            for (int i = 0; i < 3; i++) {
                if (ModuleE.CC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(tx.getType()))) {
                    broadcastResult = NetworkCall.forwardTxHash(chain, tx.getHash());
                } else {
                    broadcastResult = NetworkCall.broadcastTx(chain, tx);
                }

                if (broadcastResult) {
                    break;
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    chain.getLogger().error(e);
                }
            }
            if (!broadcastResult) {
                throw new NulsException(TxErrorCode.TX_BROADCAST_FAIL);
            }
            //Add to the set of deduplication filters,Prevent other nodes from forwarding back and processing the transaction again
            TxDuplicateRemoval.insertAndCheck(hash.toHex());

        } catch (IOException e) {
            throw new NulsException(TxErrorCode.DESERIALIZE_ERROR);
        } catch (RuntimeException e) {
            chain.getLogger().error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }

    }

    @Override
    public TransactionConfirmedPO getTransaction(Chain chain, NulsHash hash) {
        TransactionUnconfirmedPO txPo = unconfirmedTxStorageService.getTx(chain.getChainId(), hash);
        if (null != txPo) {
            return new TransactionConfirmedPO(txPo.getTx(), -1L, TxStatusEnum.UNCONFIRM.getStatus());
        } else {
            return confirmedTxService.getConfirmedTransaction(chain, hash);
        }
    }

    @Override
    public boolean isTxExists(Chain chain, NulsHash hash) {
        boolean rs = unconfirmedTxStorageService.isExists(chain.getChainId(), hash);
        if (!rs) {
            rs = confirmedTxStorageService.isExists(chain.getChainId(), hash);
        }
        return rs;
    }

    /**
     * Legal circulation of transactions and basic verification
     *
     * @param chain
     * @param tx
     * @throws NulsException
     */
    public void verifyTransactionInCirculation(Chain chain, Transaction tx) throws NulsException {
        TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
        if (null == txRegister) {
            throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
        }
        if (txRegister.getSystemTx()) {
            throw new NulsException(TxErrorCode.SYS_TX_TYPE_NON_CIRCULATING);
        }
        baseValidateTx(chain, tx, txRegister);
    }

    /**
     * Complete verification of individual transactions（basis+business）
     *
     * @param chain
     * @param tx
     * @return
     */
    @Override
    public VerifyResult verify(Chain chain, Transaction tx) {
        try {
            verifyTransactionInCirculation(chain, tx);
            TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
            Map<String, Object> result = TransactionCall.txModuleValidator(chain, txRegister.getModuleCode(), RPCUtil.encode(tx.serialize()));
            List<String> txHashList = (List<String>) result.get("list");
            if (txHashList.isEmpty()) {
                return VerifyResult.success();
            } else {
                chain.getLogger().error("tx validator fail -type:{}, -hash:{} ", tx.getType(), tx.getHash().toHex());
                String errorCodeStr = (String) result.get("errorCode");
                ErrorCode errorCode = null == errorCodeStr ? TxErrorCode.SYS_UNKOWN_EXCEPTION : ErrorCode.init(errorCodeStr);
                return VerifyResult.fail(errorCode);
            }
        } catch (IOException e) {
            return VerifyResult.fail(TxErrorCode.SERIALIZE_ERROR);
        } catch (NulsException e) {
            return VerifyResult.fail(e.getErrorCode());
        } catch (Exception e) {
            return VerifyResult.fail(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    /**
     * Basic verification of single transaction
     *
     * @param chain
     * @param tx
     * @param txRegister
     * @throws NulsException
     */
    @Override
    public void baseValidateTx(Chain chain, Transaction tx, TxRegister txRegister) throws NulsException {
        if (null == tx) {
            throw new NulsException(TxErrorCode.TX_NOT_EXIST);
        }
        if (tx.getHash() == null || !tx.getHash().verify()) {
            throw new NulsException(TxErrorCode.HASH_ERROR);
        }
        if (!TxManager.contains(chain, tx.getType())) {
            throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
        }
        if (tx.getTime() == 0L) {
            throw new NulsException(TxErrorCode.TX_DATA_VALIDATION_ERROR);
        }
        if (tx.size() > chain.getConfig().getTxMaxSize()) {
            throw new NulsException(TxErrorCode.TX_SIZE_TOO_LARGE);
        }
        //Verify signature
        if (ProtocolGroupManager.getCurrentVersion(chain.getChainId()) >= TxContext.UPDATE_VERSION_ACCOUNT_BLOCK_UPGRADE) {
            validateTxSignatureProtocol12(tx, txRegister, chain);
        } else {
            validateTxSignature(tx, txRegister, chain);
        }

        //If there is anycoinData, Then proceed with verification,There are some transactions(Yellow card)absencecoinDatadata
        int txType = tx.getType();
        if (txType == TxType.YELLOW_PUNISH
                || txType == TxType.VERIFIER_CHANGE
                || txType == TxType.VERIFIER_INIT
                || txType == TxType.REGISTERED_CHAIN_CHANGE) {
            return;
        }
        CoinData coinData = TxUtil.getCoinData(tx);
        validateCoinFromBase(chain, txRegister, coinData.getFrom());
        validateCoinToBase(chain, txRegister, coinData.getTo());
        if (txRegister.getVerifyFee()) {
            /* 2020/11/24 Transaction for obtaining verification fees in basic verificationsizeTime, Remove transaction signaturesize */
            int validateTxSize = tx.size() - SerializeUtils.sizeOfBytes(tx.getTransactionSignature());
            validateFee(chain, tx.getType(), validateTxSize, coinData, txRegister);
        }
    }

    /**
     * Verify signature Just need to verify,Transactions that require signature verification(Some system transactions do not require signatures)
     * Verify the public key andfromDoes it match in the middle, Verify signature correctness
     *
     * @param tx
     * @throws NulsException
     */
    private void validateTxSignature(Transaction tx, TxRegister txRegister, Chain chain) throws NulsException {
        if (!txRegister.getVerifySignature() || ModuleE.CC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(tx.getType()))) {
            //Transactions that do not require signature verification during registration(Some system transactions),And cross chain module transactions(individualization).
            return;
        }
        CoinData coinData = TxUtil.getCoinData(tx);
        if (null == coinData || null == coinData.getFrom() || coinData.getFrom().size() <= 0) {
            throw new NulsException(TxErrorCode.COINDATA_NOT_FOUND);
        }
        //Obtain a list of transaction signer addresses
        Set<String> addressSet = SignatureUtil.getAddressFromTX(tx, chain.getChainId());
        if (addressSet == null) {
            throw new NulsException(TxErrorCode.SIGNATURE_ERROR);
        }
        int chainId = chain.getChainId();
        byte[] multiSignAddress = null;
        if (tx.isMultiSignTx()) {
            /**
             * If it is a multi signature transaction, Then, first extract the public key list and minimum number of signatures of the original creator with multiple signed addresses from the signing object,
             * Generate a new multi signature address,To engage in transactionsfromMultiple address matches in, unable to match. This verification does not pass.
             */
            MultiSignTxSignature multiSignTxSignature = new MultiSignTxSignature();
            multiSignTxSignature.parse(new NulsByteBuffer(tx.getTransactionSignature()));
            //Verify if the signer is sufficient for the minimum number of signatures
            if (addressSet.size() < multiSignTxSignature.getM()) {
                throw new NulsException(TxErrorCode.INSUFFICIENT_SIGNATURES);
            }
            //Is the signer one of the creators of the multi signature account
            for (String address : addressSet) {
                boolean rs = false;
                for (byte[] bytes : multiSignTxSignature.getPubKeyList()) {
                    String addr = AddressTool.getStringAddressByBytes(AddressTool.getAddress(bytes, chainId));
                    if (address.equals(addr)) {
                        rs = true;
                    }
                }
                if (!rs) {
                    throw new NulsException(TxErrorCode.SIGN_ADDRESS_NOT_MATCH_COINFROM);
                }
            }
            //Generate a multi signature address
            List<String> pubKeys = new ArrayList<>();
            for (byte[] pubkey : multiSignTxSignature.getPubKeyList()) {
                pubKeys.add(HexUtil.encode(pubkey));
            }
            try {
                byte[] hash160 = SerializeUtils.sha256hash160(AddressTool.createMultiSigAccountOriginBytes(chainId, multiSignTxSignature.getM(), pubKeys));
                Address address = new Address(chainId, BaseConstant.P2SH_ADDRESS_TYPE, hash160);
                multiSignAddress = address.getAddressBytes();
            } catch (Exception e) {
                chain.getLogger().error(e);
                throw new NulsException(TxErrorCode.SIGNATURE_ERROR);
            }
        }
        for (CoinFrom coinFrom : coinData.getFrom()) {
            if (tx.getType() == TxType.STOP_AGENT || tx.getType() == TxType.DELAY_STOP_AGENT) {
                //Stop nodefromThe first one in the middle is the signature address, Only verifyfromFirst in the middle
                break;
            }
            if (tx.isMultiSignTx()) {
                if (!Arrays.equals(coinFrom.getAddress(), multiSignAddress)) {
                    throw new NulsException(TxErrorCode.SIGNATURE_ERROR);
                }
            } else if (!addressSet.contains(AddressTool.getStringAddressByBytes(coinFrom.getAddress()))) {
                throw new NulsException(TxErrorCode.SIGN_ADDRESS_NOT_MATCH_COINFROM);
            }
        }
        if (!SignatureUtil.validateTransactionSignture(chainId, tx)) {
            throw new NulsException(TxErrorCode.SIGNATURE_ERROR);
        }
    }

    private void validateTxSignatureProtocol12(Transaction tx, TxRegister txRegister, Chain chain) throws NulsException {
        //Just need to verify,Transactions that require signature verification(Some system transactions do not require signatures)
        if (!txRegister.getVerifySignature()) {
            //Transactions that do not require signature verification during registration(Some system transactions)
            return;
        }
        CoinData coinData = TxUtil.getCoinData(tx);
        if (null == coinData || null == coinData.getFrom() || coinData.getFrom().size() <= 0) {
            throw new NulsException(TxErrorCode.COINDATA_NOT_FOUND);
        }
        if (ModuleE.CC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(tx.getType()))) {
            if (tx.getType() != TxType.CROSS_CHAIN) {
                // Cross chain transfer transactions for non local protocols of cross chain modules(individualization).
                return;
            }
            int fromChainId = AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress());
            // Cross chain module non native protocol cross chain transactions(individualization).
            if (chain.getChainId() != fromChainId) {
                return;
            }
        }
        //Obtain a list of transaction signer addresses
        Set<String> addressSet = SignatureUtil.getAddressFromTX(tx, chain.getChainId());
        if (addressSet == null) {
            throw new NulsException(TxErrorCode.SIGNATURE_ERROR);
        }
        int chainId = chain.getChainId();
        byte[] multiSignAddress = null;
        if (tx.isMultiSignTx()) {
            /**
             * If it is a multi signature transaction, Then, first extract the public key list and minimum number of signatures of the original creator with multiple signed addresses from the signing object,
             * Generate a new multi signature address,To engage in transactionsfromMultiple address matches in, unable to match. This verification does not pass.
             */
            MultiSignTxSignature multiSignTxSignature = new MultiSignTxSignature();
            multiSignTxSignature.parse(new NulsByteBuffer(tx.getTransactionSignature()));
            //Verify if the signer is sufficient for the minimum number of signatures
            if (addressSet.size() < multiSignTxSignature.getM()) {
                throw new NulsException(TxErrorCode.INSUFFICIENT_SIGNATURES);
            }
            //Is the signer one of the creators of the multi signature account
            for (String address : addressSet) {
                boolean rs = false;
                for (byte[] bytes : multiSignTxSignature.getPubKeyList()) {
                    String addr = AddressTool.getStringAddressByBytes(AddressTool.getAddress(bytes, chainId));
                    if (address.equals(addr)) {
                        rs = true;
                    }
                }
                if (!rs) {
                    throw new NulsException(TxErrorCode.SIGN_ADDRESS_NOT_MATCH_COINFROM);
                }
            }
            //Generate a multi signature address
            List<String> pubKeys = new ArrayList<>();
            for (byte[] pubkey : multiSignTxSignature.getPubKeyList()) {
                pubKeys.add(HexUtil.encode(pubkey));
            }
            try {
                byte[] hash160 = SerializeUtils.sha256hash160(AddressTool.createMultiSigAccountOriginBytes(chainId, multiSignTxSignature.getM(), pubKeys));
                Address address = new Address(chainId, BaseConstant.P2SH_ADDRESS_TYPE, hash160);
                multiSignAddress = address.getAddressBytes();
            } catch (Exception e) {
                chain.getLogger().error(e);
                throw new NulsException(TxErrorCode.SIGNATURE_ERROR);
            }
        }
        for (CoinFrom coinFrom : coinData.getFrom()) {
            if (tx.getType() == TxType.STOP_AGENT || tx.getType() == TxType.DELAY_STOP_AGENT) {
                //Stop nodefromThe first one in the middle is the signature address, Only verifyfromFirst in the middle
                break;
            }
            if (tx.isMultiSignTx()) {
                if (!Arrays.equals(coinFrom.getAddress(), multiSignAddress)) {
                    throw new NulsException(TxErrorCode.SIGNATURE_ERROR);
                }
            } else if (!addressSet.contains(AddressTool.getStringAddressByBytes(coinFrom.getAddress()))) {
                throw new NulsException(TxErrorCode.SIGN_ADDRESS_NOT_MATCH_COINFROM);
            }
        }
        do {
            int txType = tx.getType();
            // Pledge and withdrawal of pledge do not verify locked address
            if (txType == TxType.DEPOSIT || txType == TxType.CANCEL_DEPOSIT || txType == TxType.STOP_AGENT || txType == TxType.DELAY_STOP_AGENT) {
                break;
            }
            boolean needAccountManagerSign = false;
            for (CoinFrom coinFrom : coinData.getFrom()) {
                byte[] fromAddress = coinFrom.getAddress();
                AccountBlockDTO dto = AccountCall.getBlockAccount(chainId, AddressTool.getStringAddressByBytes(fromAddress));
                if (dto == null) {
                    continue;
                }
                int[] types = dto.getTypes();
                if (types == null) {
                    // Completely locked account, signature verification required
                    needAccountManagerSign = true;
                    break;
                } else {
                    // Transaction type whitelist
                    boolean whiteType = false;
                    for (int type : types) {
                        if (txType == type) {
                            whiteType = true;
                            break;
                        }
                    }
                    if (!whiteType) {
                        // Not on the whitelist of transaction types, signature verification is required
                        needAccountManagerSign = true;
                        break;
                    }
                    // Verify the whitelist of contract addresses
                    if (txType == TxType.CALL_CONTRACT) {
                        if (dto.getContracts() == null || dto.getContracts().length == 0) {
                            // Not on the whitelist of contract addresses, signature verification is required
                            needAccountManagerSign = true;
                            break;
                        }
                        String[] contracts = dto.getContracts();
                        NulsByteBuffer byteBuffer = new NulsByteBuffer(tx.getTxData());
                        byteBuffer.readBytes(Address.ADDRESS_LENGTH);
                        byte[] contractAddressBytes = byteBuffer.readBytes(Address.ADDRESS_LENGTH);
                        String contractAddress = AddressTool.getStringAddressByBytes(contractAddressBytes);
                        // Contract address whitelist
                        boolean whiteContract = false;
                        for (String contract : contracts) {
                            if (contractAddress.equals(contract)) {
                                whiteContract = true;
                                break;
                            }
                        }
                        if (!whiteContract) {
                            // Not on the whitelist of contract addresses, signature verification is required
                            needAccountManagerSign = true;
                            break;
                        }
                    }
                }
            }
            if (needAccountManagerSign) {
                // Three fifths of the signature, read the locked account administrator public key from the configuration file, calculate the address, and`addressSet`Middle matching,>=60% Satisfy immediately
                int count = 0;
                for (String signedAddress : addressSet) {
                    if (TxContext.ACCOUNT_BLOCK_MANAGER_ADDRESS_SET.contains(signedAddress)) {
                        count++;
                    }
                }
                if (count < TxContext.ACCOUNT_BLOCK_MIN_SIGN_COUNT) {
                    throw new NulsException(TxErrorCode.BLOCK_ADDRESS, "address is blockAddress Exception");
                }
            }
        } while (false);

        if (!SignatureUtil.validateTransactionSignture(chainId, tx)) {
            throw new NulsException(TxErrorCode.SIGNATURE_ERROR);
        }
    }

    private void validateCoinFromBase(Chain chain, TxRegister txRegister, List<CoinFrom> listFrom) throws NulsException {
        int type = txRegister.getTxType();
        //coinBasetransaction/Smart contract refundgasTransaction not availablefrom
        if (type == TxType.COIN_BASE || type == TxType.CONTRACT_RETURN_GAS) {
            return;
        }
        if (null == listFrom || listFrom.size() == 0) {
            throw new NulsException(TxErrorCode.COINFROM_NOT_FOUND);
        }
        int chainId = chain.getConfig().getChainId();
        //Verify if the payer belongs to the same chain
        Integer fromChainId = null;
        Set<String> uniqueCoin = new HashSet<>();
        byte[] existMultiSignAddress = null;

        boolean forked = chain.getBestBlockHeight() >= 878000;

        for (CoinFrom coinFrom : listFrom) {
            byte[] addrBytes = coinFrom.getAddress();
            if (ProtocolGroupManager.getCurrentVersion(chainId) < TxContext.UPDATE_VERSION_ACCOUNT_BLOCK_UPGRADE && type != TxType.DEPOSIT && type != TxType.CANCEL_DEPOSIT && TxUtil.isBlockAddress(chainId, addrBytes)) {
                throw new NulsException(TxErrorCode.BLOCK_ADDRESS, "address is blockAddress Exception");
            }
            if (AddressTool.isBlackHoleAddress(TxUtil.blackHolePublicKey, chainId, addrBytes)) {
                throw new NulsException(TxErrorCode.INVALID_ADDRESS, "address is blackHoleAddress Exception");
            }
            if (ProtocolGroupManager.getCurrentVersion(chainId) >= TxContext.UPDATE_VERSION_CM_UPGRADE && chainId == 1 && AddressTool.getChainIdByAddress(coinFrom.getAddress()) == 2) {
                throw new NulsException(TxErrorCode.INVALID_ADDRESS, "address is testnet address Exception");
            }
            if (forked && TxUtil.isBlackHoleAddress(chainId, addrBytes)) {
                throw new NulsException(TxErrorCode.INVALID_ADDRESS, "Address is blackHoleAddress Exception[x]");
            }
            String addr = AddressTool.getStringAddressByBytes(addrBytes);
            //Verify the legality of the transaction address,Cross chain module transactions require retrieving the original chain from the addressidTo verify
            int validAddressChainId = chainId;
            if (ModuleE.CC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(type))) {
                validAddressChainId = AddressTool.getChainIdByAddress(addrBytes);
            }
            if (!AddressTool.validAddress(validAddressChainId, addr)) {
                throw new NulsException(TxErrorCode.INVALID_ADDRESS);
            }
            if (null == existMultiSignAddress && AddressTool.isMultiSignAddress(addrBytes)) {
                existMultiSignAddress = addrBytes;
            }
            int addrChainId = AddressTool.getChainIdByAddress(addrBytes);
            if (coinFrom.getAmount().compareTo(BigInteger.ZERO) < 0) {
                throw new NulsException(TxErrorCode.DATA_ERROR);
            }
            //AllfromIs it the same address on the same chain
            if (null == fromChainId) {
                fromChainId = addrChainId;
            } else if (fromChainId != addrChainId) {
                throw new NulsException(TxErrorCode.COINFROM_NOT_SAME_CHAINID);
            }
            //If it's not a cross chain transaction,fromThe chain corresponding to the middle addressidChain must be initiatedidCross chain transactions are validated in validators
            if (!TxManager.isCrossTx(type)) {
                if (chainId != addrChainId) {
                    throw new NulsException(TxErrorCode.FROM_ADDRESS_NOT_MATCH_CHAIN);
                }
            }
            //Verify account address,Asset Chainid,assetidThe combination uniqueness of
            int assetsChainId = coinFrom.getAssetsChainId();
            int assetsId = coinFrom.getAssetsId();
            boolean rs = uniqueCoin.add(addr + "-" + assetsChainId + "-" + assetsId + "-" + HexUtil.encode(coinFrom.getNonce()));
            if (!rs) {
                throw new NulsException(TxErrorCode.COINFROM_HAS_DUPLICATE_COIN);
            }
            //User issued[Non stopping nodes,Red card]Transaction not allowedfromThere is a contract address in the middle,IffromInclude contract address,So this transaction must have been sent by the system,Transactions sent by the system will not go through basic verification
            if (type != TxType.STOP_AGENT && type != TxType.RED_PUNISH && TxUtil.isLegalContractAddress(coinFrom.getAddress(), chain)) {
                chain.getLogger().error("Tx from cannot have contract address ");
                throw new NulsException(TxErrorCode.TX_FROM_CANNOT_HAS_CONTRACT_ADDRESS);
            }

            if (!txRegister.getUnlockTx() && coinFrom.getLocked() == -1) {
                chain.getLogger().error("This transaction type can not unlock the token");
                throw new NulsException(TxErrorCode.TX_VERIFY_FAIL);
            }
        }
        if (null != existMultiSignAddress && type != TxType.STOP_AGENT && type != TxType.DELAY_STOP_AGENT && type != TxType.RED_PUNISH) {
            //IffromContains multiple signed addresses,This indicates that the transaction is a multi signature transaction,Then it must be met,fromsOnly this multiple signed address exists in the system
            for (CoinFrom coinFrom : listFrom) {
                if (!Arrays.equals(existMultiSignAddress, coinFrom.getAddress())) {
                    throw new NulsException(TxErrorCode.MULTI_SIGN_TX_ONLY_SAME_ADDRESS);
                }
            }
        }
    }

    private void validateCoinToBase(Chain chain, TxRegister txRegister, List<CoinTo> listTo) throws NulsException {
        String moduleCode = txRegister.getModuleCode();
        int type = txRegister.getTxType();
        if (type != TxType.COIN_BASE && !ModuleE.SC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(type))) {
            if (null == listTo || listTo.size() == 0) {
                throw new NulsException(TxErrorCode.COINTO_NOT_FOUND);
            }
        }
        int localChainId = chain.getChainId();
        //Verify if the payee belongs to the same chain
        Integer addressChainId = null;
        int txChainId = chain.getChainId();
        Set<String> uniqueCoin = new HashSet<>();
        for (CoinTo coinTo : listTo) {
            String addr = AddressTool.getStringAddressByBytes(coinTo.getAddress());

            //Verify the legality of the transaction address,Cross chain module transactions require retrieving the original chain from the addressidTo verify
            int validAddressChainId = txChainId;
            if (ModuleE.CC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(type))) {
                validAddressChainId = AddressTool.getChainIdByAddress(coinTo.getAddress());
            }

            if (ProtocolGroupManager.getCurrentVersion(localChainId) >= TxContext.UPDATE_VERSION_CM_UPGRADE && localChainId == 1 && AddressTool.getChainIdByAddress(coinTo.getAddress()) == 2) {
                throw new NulsException(TxErrorCode.INVALID_ADDRESS, "address is testnet address Exception");
            }

            if (!AddressTool.validAddress(validAddressChainId, addr)) {
                throw new NulsException(TxErrorCode.INVALID_ADDRESS);
            }

            int chainId = AddressTool.getChainIdByAddress(coinTo.getAddress());
            if (null == addressChainId) {
                addressChainId = chainId;
            } else if (addressChainId != chainId) {
                throw new NulsException(TxErrorCode.COINTO_NOT_SAME_CHAINID);
            }
            if (coinTo.getAmount().compareTo(BigInteger.ZERO) < 0) {
                throw new NulsException(TxErrorCode.DATA_ERROR);
            }
            //If it's not a cross chain transaction,toThe chain corresponding to the middle addressidMust be the chain initiating the transactionid
            if (!TxManager.isCrossTx(type)) {
                if (chainId != txChainId) {
                    throw new NulsException(TxErrorCode.TO_ADDRESS_NOT_MATCH_CHAIN);
                }
            }
            int assetsChainId = coinTo.getAssetsChainId();
            int assetsId = coinTo.getAssetsId();
            long lockTime = coinTo.getLockTime();
            //toInside address、Asset Chainid、assetid、The combination of lock times cannot be repeated
            boolean rs = uniqueCoin.add(addr + "-" + assetsChainId + "-" + assetsId + "-" + lockTime);
            if (!rs) {
                throw new NulsException(TxErrorCode.COINTO_HAS_DUPLICATE_COIN);
            }
            //Contract address acceptanceNULSThe transaction can only becoinBasetransaction,Call contract transactions,Normal stop node(Contract stop node trading is a system transaction,Not going through basic verification)
            if (TxUtil.isLegalContractAddress(coinTo.getAddress(), chain)) {
                boolean sysTx = txRegister.getSystemTx();
                if (!sysTx && type != TxType.COIN_BASE
                        && type != TxType.CALL_CONTRACT
                        && type != TxType.STOP_AGENT) {
                    chain.getLogger().error("contract data error: The contract does not accept transfers of this type{} of transaction.", type);
                    throw new NulsException(TxErrorCode.TX_DATA_VALIDATION_ERROR);
                }
            }
        }
    }

    /**
     * Verify if transaction fees are correct
     *
     * @param chain    chainid
     * @param type     tx type
     * @param txSize   tx size
     * @param coinData
     * @return Result
     */
    private void validateFee(Chain chain, int type, int txSize, CoinData coinData, TxRegister txRegister) throws NulsException {
        if (txRegister.getSystemTx()) {
            //There is no transaction fee for system transactions
            return;
        }
        int feeAssetChainId;
        int feeAssetId;
        if (TxManager.isCrossTx(type) && AddressTool.getChainIdByAddress(coinData.getFrom().get(0).getAddress()) != chain.getChainId()) {
            //When initiating a chain for cross chain transactions and not for transactions,Calculate the main assets of the main network as transaction feesNULS
            feeAssetChainId = txConfig.getMainChainId();
            feeAssetId = txConfig.getMainAssetId();
            BigInteger fee = coinData.getFeeByAsset(feeAssetChainId, feeAssetId);
            if (BigIntegerUtils.isEqualOrLessThan(fee, BigInteger.ZERO)) {
                throw new NulsException(TxErrorCode.INSUFFICIENT_FEE);
            }
            //根据交易大小重新计算手续费，用来验证实际手续费
            BigInteger targetFee;
            if (TxManager.isCrossTx(type)) {
                targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
            } else {
                targetFee = TransactionFeeCalculator.getNormalTxFee(txSize, chain.getConfig().getFeeUnit(feeAssetChainId, feeAssetId));
            }
            if (BigIntegerUtils.isLessThan(fee, targetFee)) {
                throw new NulsException(TxErrorCode.INSUFFICIENT_FEE);
            }
        } else {
            boolean result = false;
            Set<String> set = chain.getConfig().getFeeAssetsSet();
            for (String tokenId : set) {
                String[] arr = tokenId.split("-");
        //Calculate the main asset as a handling fee
                feeAssetChainId = Integer.parseInt(arr[0]);
                feeAssetId = Integer.parseInt(arr[1]);
                if (feeAssetId != 1 && ProtocolGroupManager.getCurrentVersion(chain.getChainId()) < ContractContext.PROTOCOL_20) {
                    continue;
                }
                BigInteger fee = coinData.getFeeByAsset(feeAssetChainId, feeAssetId);
                if (BigIntegerUtils.isEqualOrLessThan(fee, BigInteger.ZERO)) {
                    continue;
                }
        //Recalculate transaction fees based on transaction size to verify actual transaction fees
                BigInteger targetFee;
                if (TxManager.isCrossTx(type)) {
                    targetFee = TransactionFeeCalculator.getCrossTxFee(txSize);
                } else {
                    targetFee = TransactionFeeCalculator.getNormalTxFee(txSize, chain.getConfig().getFeeUnit(feeAssetChainId, feeAssetId));
                }
                if (BigIntegerUtils.isLessThan(fee, targetFee)) {
                    continue;
                }
                result = true;
                break;
            }
            if (!result) {
                throw new NulsException(TxErrorCode.INSUFFICIENT_FEE);
            }
        }

    }

    /**
     * When packaging,Retrieve transaction stages from the queue to be packaged,Will generate a temporary transaction list,When interrupting the acquisition of transactions, it is necessary to return the remaining temporary transactions to the queue for packaging
     */
    private void backTempPackablePool(Chain chain, List<TxPackageWrapper> listTx) {
        for (int i = listTx.size() - 1; i >= 0; i--) {
            packablePool.offerFirstOnlyHash(chain, listTx.get(i).getTx());
        }
    }

    @Override
    public TxPackage getPackableTxs(Chain chain, long endtimestamp, long maxTxDataSize, long blockTime, String packingAddress, String preStateRoot) {
        chain.getPackageLock().lock();
        long startTime = NulsDateUtils.getCurrentTimeMillis();
        List<TxPackageWrapper> packingTxList = new ArrayList<>();
        //Record orphan transactions in the ledger,Filter out when returning to consensus,Because when repackaging due to height changes,Need to restore to the queue to be packaged
        Set<TxPackageWrapper> orphanTxSet = new HashSet<>();
        NulsLogger nulsLogger = chain.getLogger();
        try {
            //The height of this packaging
            long blockHeight = chain.getBestBlockHeight() + 1;

            long packableTime = endtimestamp - startTime;
            nulsLogger.info("[Package start] -Packaging time：{}, -Packable capacity：{}B , - height:{}, - Current queue transactions to be packagedhashnumber:{}, - Actual number of transactions in the queue to be packaged:{}",
                    packableTime, maxTxDataSize, blockHeight, packablePool.packableHashQueueSize(chain), packablePool.packableTxMapSize(chain));
            long batchValidReserve = TxConstant.PACKAGE_MODULE_VALIDATOR_RESERVE_TIME;
            if (packableTime <= batchValidReserve) {
                //Directly hit the empty block
                return new TxPackage(new ArrayList<>(), null, chain.getBestBlockHeight() + 1);
            }
            //Reset Flag
            chain.setContractTxFail(false);
            //Assemble unified validation parameter data,keyUnify validators for each modulecmd
            Map<String, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);

            long packingTime = endtimestamp - startTime;
            //Statistics of total waiting time
            int allSleepTime = 0;
            //Recurrent acquisition of transaction usage time
            long whileTime;
            //Total time for verifying ledger
            long totalLedgerTime = 0;
            //Total time for module unified verification usage
            long batchModuleTime;
            long totalSize = 0L;
            //Calculate the total number of blocks when obtaining transactionssizeTemporary size value
            long totalSizeTemp = 0L;
            int maxCount = TxConstant.PACKAGE_TX_MAX_COUNT - TxConstant.PACKAGE_TX_VERIFY_COINDATA_NUMBER_OF_TIMES_TO_PROCESS;
            //Calculate the time reserved for batch validation from the total packaging time based on the configured percentage
            //            long batchValidReserve = packagingReservationTime(chain, packingTime);
            long packageRpcReserveTime = chain.getConfig().getPackageRpcReserveTime();

            //Smart contract notification identifier,When the first smart contract transaction occurs and the validator is called to pass,If there is, only notify on the first attempt.
            boolean contractNotify = false;

            //Send batch verification to the ledger modulecoinDataIdentification of
            LedgerCall.coinDataBatchNotify(chain);
            //Retrieved transaction set(Need to be sent to ledger verification)
            List<String> batchProcessList = new ArrayList<>();
            Set<String> duplicatesVerify = new HashSet<>();
            //Retrieved transaction set
            List<TxPackageWrapper> currentBatchPackableTxs = new ArrayList<>();
            //This packaging includes the number of cross chain transactions
            int corssTxCount = 0;
            //Batch processing, including the number of cross chain transactions
            int batchCorssTxCount = 0;
            //This packaging includes the number of contract transactions
            int contractTxCount = 0;
            //Batch processing, including the number of contract transactions
            int batchContractTxCount = 0;
            //Whether to stop executing the functional contract,If bittrue,The extracted smart contract will no longer be processed in this packaging process,Need to return to the packaging queue
            boolean stopInvokeContract = false;

            int packageContractTxMaxCount;
            Random random = new Random();
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            if (availableProcessors <= 4) {
                packageContractTxMaxCount = 20 + random.nextInt(10);
            } else if (availableProcessors <= 8) {
                packageContractTxMaxCount = 50 + random.nextInt(10);
            } else {
                packageContractTxMaxCount = 100 + random.nextInt(20);
            }
            //packageContractTxMaxCount = 15;

            for (int index = 0; ; index++) {
                long currentTimeMillis = NulsDateUtils.getCurrentTimeMillis();
                long currentReserve = endtimestamp - currentTimeMillis;
                if (currentReserve <= batchValidReserve) {
                    if (nulsLogger.isDebugEnabled()) {
                        nulsLogger.debug("Get transaction time up to,Entering the module validation phase: currentTimeMillis:{}, -endtimestamp:{}, -offset:{}, -remaining:{}",
                                currentTimeMillis, endtimestamp, batchValidReserve, currentReserve);
                    }
                    backTempPackablePool(chain, currentBatchPackableTxs);
                    break;
                }
                if (currentReserve < packageRpcReserveTime) {
                    //overtime,Leave for final data assembly andRPCInsufficient transmission time
                    nulsLogger.error("getPackableTxs time out, endtimestamp:{}, current:{}, endtimestamp-current:{}, reserveTime:{}",
                            endtimestamp, currentTimeMillis, currentReserve, packageRpcReserveTime);
                    backTempPackablePool(chain, currentBatchPackableTxs);
                    throw new NulsException(TxErrorCode.PACKAGE_TIME_OUT);
                }
                if (chain.getProtocolUpgrade().get()) {
                    chain.getCanProtocolUpgrade().set(false);
                    nulsLogger.info("1_chain.getCanProtocolUpgrade().set(false);");
                    nulsLogger.info("Protocol Upgrade Package stop -chain:{} -best block height", chain.getChainId(), chain.getBestBlockHeight());
                    backTempPackablePool(chain, currentBatchPackableTxs);
                    //Put back packable transactions and orphans
                    putBackPackablePool(chain, packingTxList, orphanTxSet);
                    //Directly hit the empty block
                    TxPackage txPackage = new TxPackage(new ArrayList<>(), null, chain.getBestBlockHeight() + 1);
                    chain.getCanProtocolUpgrade().set(true);
                    nulsLogger.info("1_chain.getCanProtocolUpgrade().set(true);");
                    return txPackage;
                }
                //If the latest local block+1 Greater than the current height of the packaging block, Explanation: The latest local block has been updated,Need to repackage,Put the retrieved transaction back into the packaging queue
                if (blockHeight < chain.getBestBlockHeight() + 1) {
                    nulsLogger.info("Obtaining the latest block height during the transaction process has increased,Put the retrieved transactions and orphans back into the packaging queue, Repackaging...");
                    backTempPackablePool(chain, currentBatchPackableTxs);
                    //Put back packable transactions and orphans
                    putBackPackablePool(chain, packingTxList, orphanTxSet);
                    return getPackableTxs(chain, endtimestamp, maxTxDataSize, blockTime, packingAddress, preStateRoot);
                }
                if (packingTxList.size() > maxCount) {
                    if (nulsLogger.isDebugEnabled()) {
                        nulsLogger.debug("Obtaining transaction completedmax count,Entering the module validation phase: currentTimeMillis:{}, -endtimestamp:{}, -offset:{}, -remaining:{}",
                                currentTimeMillis, endtimestamp, batchValidReserve, endtimestamp - currentTimeMillis);
                    }
                    backTempPackablePool(chain, currentBatchPackableTxs);
                    break;
                }
                int batchProcessListSize = batchProcessList.size();
                boolean process = false;
                Transaction tx = null;
                boolean maxDataSize = false;
                try {
                    tx = packablePool.poll(chain);
                    if (tx == null && batchProcessListSize == 0) {
                        Thread.sleep(10L);
                        allSleepTime += 10;
                        continue;
                    } else if (tx == null && batchProcessListSize > 0) {
                        //Meet the conditions for processing this batch
                        process = true;
                    } else if (tx != null) {
                        if (!duplicatesVerify.add(tx.getHash().toHex())) {
                            //If you don't join, it means it already exists
                            continue;
                        }
                        long txSize = tx.size();
                        if ((totalSizeTemp + txSize) > maxTxDataSize) {
                            packablePool.offerFirstOnlyHash(chain, tx);
                            nulsLogger.info("The transaction has reached its maximum capacity, actual value: {}, totalSizeTemp:{}, Current transactionsize：{} - Reserve maximum valuemaxTxDataSize:{}, txhash:{}", totalSize, totalSizeTemp, txSize, maxTxDataSize, tx.getHash().toHex());
                            maxDataSize = true;
                            if (batchProcessListSize > 0) {
                                //Meet the conditions for processing this batch
                                process = true;
                            } else {
                                break;
                            }
                        } else {
                            //Limit the number of cross chain transactions
                            if (ModuleE.CC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(tx.getType()))) {
                                if (corssTxCount + (++batchCorssTxCount) >= TxConstant.PACKAGE_CROSS_TX_MAX_COUNT) {
                                    //Limit the total number of cross chain transactions contained in a single block. If the maximum number of cross chain transactions is exceeded, put it back, Then stop obtaining transactions
                                    packablePool.add(chain, tx);
                                    if (batchProcessListSize > 0) {
                                        //Meet the conditions for processing this batch
                                        process = true;
                                    } else {
                                        break;
                                    }
                                }
                            }
                            //Limit the number of smart contract transactions
                            boolean isContract = ModuleE.SC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(tx.getType()));
                            if (isContract) {
                                if (contractTxCount + (++batchContractTxCount) >= packageContractTxMaxCount) {
                                    //Limit the total number of cross chain transactions contained in a single block. If the maximum number of cross chain transactions is exceeded, put it back, Then stop obtaining transactions
                                    packablePool.add(chain, tx);
                                    if (batchProcessListSize > 0) {
                                        //Meet the conditions for processing this batch
                                        process = true;
                                    } else {
                                        break;
                                    }
                                }
                            }
                            String txHex;
                            try {
                                txHex = RPCUtil.encode(tx.serialize());
                            } catch (Exception e) {
                                nulsLogger.warn(e.getMessage(), e);
                                nulsLogger.error("Discard acquisitionhexWrong transaction, txHash:{}, - type:{}, - time:{}", tx.getHash().toHex(), tx.getType(), tx.getTime());
                                clearInvalidTx(chain, tx);
                                continue;
                            }
                            TxPackageWrapper txPackageWrapper = new TxPackageWrapper(tx, index, txHex);
                            batchProcessList.add(txHex);
                            currentBatchPackableTxs.add(txPackageWrapper);
                            if (batchProcessList.size() == TxConstant.PACKAGE_TX_VERIFY_COINDATA_NUMBER_OF_TIMES_TO_PROCESS) {
                                //Meet the conditions for processing this batch
                                process = true;
                            }
                        }
                        //Total size plus the size of each transaction in the current batch
                        totalSizeTemp += txSize;
                    }
                    if (process) {
                        long verifyLedgerStart = NulsDateUtils.getCurrentTimeMillis();
                        if (!chain.getPackableState().get()) {
                            nulsLogger.info("Saving or rolling back blocks during the transaction process triggers ledger submission or rollback, Repackaging...");
                            //Put back packable transactions and orphans
                            packingTxList.addAll(currentBatchPackableTxs);
                            putBackPackablePool(chain, packingTxList, orphanTxSet);
                            Thread.sleep(30L);
                            return getPackableTxs(chain, endtimestamp, maxTxDataSize, blockTime, packingAddress, preStateRoot);
                        }
                        verifyLedger(chain, batchProcessList, currentBatchPackableTxs, orphanTxSet, false, false);
                        totalLedgerTime += NulsDateUtils.getCurrentTimeMillis() - verifyLedgerStart;

                        Iterator<TxPackageWrapper> it = currentBatchPackableTxs.iterator();
                        while (it.hasNext()) {
                            TxPackageWrapper txPackageWrapper = it.next();
                            Transaction transaction = txPackageWrapper.getTx();
                            TxRegister txRegister = TxManager.getTxRegister(chain, transaction.getType());
                            String moduleCode = txRegister.getModuleCode();
                            boolean isSmartContractTx = ModuleE.SC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(transaction.getType()));
                            boolean isCrossTx = ModuleE.CC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(transaction.getType()));
                            // add by pierre at 2019-11-02 Cross chain transfer transaction sent to the smart contract module for parsing, is it a contract asset cross chain transfer Protocol upgrade required done
                            if (ProtocolGroupManager.getCurrentVersion(chain.getChainId()) >= TxContext.UPDATE_VERSION_V250) {
                                boolean isCrossTransferTx = TxType.CROSS_CHAIN == transaction.getType();
                                if (!isSmartContractTx && txConfig.isCollectedSmartContractModule()) {
                                    isSmartContractTx = isCrossTransferTx;
                                }
                            }
                            // end code by pierre
                            if (isSmartContractTx) {
                                if (stopInvokeContract) {
                                    //This logotrue,Indicates that smart contract transactions will no longer be processed,Need to temporarily store transactions,Unified return to packaging queue
                                    orphanTxSet.add(txPackageWrapper);
                                    it.remove();
                                    continue;
                                }
                                // Smart contracts appear,And the notification identifier isfalse,Then call the notification first
                                if (!contractNotify) {
                                    ContractCall.contractBatchBegin(chain, blockHeight, blockTime, packingAddress, preStateRoot, 0);
                                    contractNotify = true;
                                }
                                try {
                                    //Calling and executing smart contracts,returnfalse.Then smart contracts will no longer be processed
                                    boolean invokeContractRs = ContractCall.invokeContract(chain, txPackageWrapper.getTxHex(), 0);
                                    if (!invokeContractRs) {
                                        //No more postsinvoke
                                        stopInvokeContract = true;
                                        orphanTxSet.add(txPackageWrapper);
                                        it.remove();
                                        continue;
                                    }
                                } catch (NulsException e) {
                                    chain.getLogger().error(e);
                                    clearInvalidTx(chain, transaction);
                                    continue;
                                }
                            }
                            totalSize += transaction.getSize();

                            //Calculate the number of cross chain transactions
                            if (isCrossTx) {
                                corssTxCount++;
                            }
                            //Calculate the number of contract transactions
                            if (isSmartContractTx) {
                                contractTxCount++;
                            }
                            //According to the unified validator name of the module, group all transactions and prepare for unified verification of each module
                            TxUtil.moduleGroups(moduleVerifyMap, txRegister, RPCUtil.encode(transaction.serialize()));
                        }
                        //Update to the latest total block transaction size
                        totalSizeTemp = totalSize;
                        packingTxList.addAll(currentBatchPackableTxs);

                        //Batch end reset data
                        batchProcessList.clear();
                        currentBatchPackableTxs.clear();
                        batchCorssTxCount = 0;
                        batchContractTxCount = 0;
                        if (maxDataSize) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    currentBatchPackableTxs.clear();
                    nulsLogger.error("Packaging transaction exception, txHash:{}, - type:{}, - time:{}", tx.getHash().toHex(), tx.getType(), tx.getTime());
                    nulsLogger.error(e);
                    continue;
                }

            }
            //Recurrent acquisition of transaction usage time
            whileTime = NulsDateUtils.getCurrentTimeMillis() - startTime;
            if (nulsLogger.isDebugEnabled()) {
                nulsLogger.debug("-Retrieved transactions -count:{} - data size:{}", packingTxList.size(), totalSize);
            }

            boolean contractBefore = false;
            if (contractNotify) {
                contractBefore = ContractCall.contractBatchBeforeEnd(chain, blockHeight, 0);
            }
            //Processing smart contracts
            String stateRoot = preStateRoot;
            boolean hasTxbackPackablePool = false;
            long contractStart = NulsDateUtils.getCurrentTimeMillis();
            /** Smart contracts When the notification identifier istrue, This indicates that a smart contract has been called and executed*/
            List<String> contractGenerateTxs = new ArrayList<>();
            if (contractNotify && !chain.getContractTxFail()) {
                //Processing smart contract execution results
                Map map = processContractResult(chain, packingTxList, orphanTxSet, contractGenerateTxs, blockHeight, contractBefore, stateRoot);
                stateRoot = (String) map.get("stateRoot");
                hasTxbackPackablePool = (boolean) map.get("hasTxbackPackablePool");
            }
            //If the contractinvokeContract transactions that need to be returned from time to time,Or there may be a transaction where the contract execution result is returned,All require revalidation of the ledger
            if (stopInvokeContract || hasTxbackPackablePool) {
                //If there are transactions that are returned or fail verification in the smart contract Then it is necessary to verify the ledger again
                moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_16);
                verifyAgain(chain, moduleVerifyMap, packingTxList, orphanTxSet, true);
            }
            long contractTime = NulsDateUtils.getCurrentTimeMillis() - contractStart;

            //Module Unified Verifier
            long batchStart = NulsDateUtils.getCurrentTimeMillis();
            txModuleValidatorPackable(chain, moduleVerifyMap, packingTxList, orphanTxSet);
            //Total time for module unified verification usage
            batchModuleTime = NulsDateUtils.getCurrentTimeMillis() - batchStart;

            List<String> packableTxs = new ArrayList<>();
            Iterator<TxPackageWrapper> iterator = packingTxList.iterator();
            Map<NulsHash, Integer> txPackageOrphanMap = chain.getTxPackageOrphanMap();
            while (iterator.hasNext()) {
                TxPackageWrapper txPackageWrapper = iterator.next();
                Transaction tx = txPackageWrapper.getTx();
                NulsHash hash = tx.getHash();
                if (txPackageOrphanMap.containsKey(hash)) {
                    txPackageOrphanMap.remove(hash);
                }
                try {
                    packableTxs.add(RPCUtil.encode(tx.serialize()));
                } catch (Exception e) {
                    clearInvalidTx(chain, tx);
                    iterator.remove();
                    throw new NulsException(e);
                }
            }
            //Return the generated smart contractGASoftxAdd to the end of the team
            if (!hasTxbackPackablePool && contractGenerateTxs.size() > 0) {
                String csTxStr = contractGenerateTxs.get(contractGenerateTxs.size() - 1);
                if (TxUtil.extractTxTypeFromTx(csTxStr) == TxType.CONTRACT_RETURN_GAS) {
                    packableTxs.add(csTxStr);
                }
            }
            //Check the latest height
            if (blockHeight < chain.getBestBlockHeight() + 1) {
                //This stage is not enough time to pack again,So directly timeout the exception handling transaction and roll it back to the queue to be packaged,Empty block
                nulsLogger.info("Obtain transaction completion time,The current latest height has increased,Not enough time to repackage,Directly timeout exception handling transaction rollback to the queue to be packaged,Empty block");
                throw new NulsException(TxErrorCode.HEIGHT_UPDATE_UNABLE_TO_REPACKAGE);
            }

            //Add the orphan transaction back to the pending packaging queue
            putBackPackablePool(chain, orphanTxSet);
            if (chain.getProtocolUpgrade().get()) {
                chain.getCanProtocolUpgrade().set(false);
                nulsLogger.info("2_chain.getCanProtocolUpgrade().set(false);");
                //Protocol upgrade directly hits empty blocks,Retrieved transactions are placed in reverse order in the new transaction processing queue
                int size = packingTxList.size();
                for (int i = size - 1; i >= 0; i--) {
                    TxPackageWrapper txPackageWrapper = packingTxList.get(i);
                    Transaction tx = txPackageWrapper.getTx();
                    //Perform basic transaction verification
                    TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                    if (null == txRegister) {
                        throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
                    }
                    baseValidateTx(chain, tx, txRegister);
                    chain.getUnverifiedQueue().addLast(new TransactionNetPO(txPackageWrapper.getTx()));
                }
                TxPackage txPackage = new TxPackage(new ArrayList<>(), null, chain.getBestBlockHeight() + 1);
                chain.getCanProtocolUpgrade().set(true);
                nulsLogger.info("2_chain.getCanProtocolUpgrade().set(true);");
                return txPackage;
            }
            //Detect reserved transmission time
            long current = NulsDateUtils.getCurrentTimeMillis();
            if (endtimestamp - current < packageRpcReserveTime) {
                //overtime,Leave for final data assembly andRPCInsufficient transmission time
                nulsLogger.error("getPackableTxs time out, endtimestamp:{}, current:{}, endtimestamp-current:{}, reserveTime:{}",
                        endtimestamp, current, endtimestamp - current, packageRpcReserveTime);
                throw new NulsException(TxErrorCode.PACKAGE_TIME_OUT);
            }

            TxPackage txPackage = new TxPackage(packableTxs, stateRoot, blockHeight);

            long totalTime = NulsDateUtils.getCurrentTimeMillis() - startTime;
            nulsLogger.info("[Packaging time statistics]  Total execution time:{}, Remaining time:{}, Packaging available time:{}, Obtain transactions(loop)Total waiting time:{}, " +
                            "Obtain transactions(loop)execution time:{}, Obtain transactions(loop)Total time for verifying ledger:{}, Module unified verification execution time:{}, " +
                            "Contract execution time:{},", totalTime, endtimestamp - NulsDateUtils.getCurrentTimeMillis(),
                    packingTime, allSleepTime, whileTime, totalLedgerTime, batchModuleTime,
                    contractTime);

            nulsLogger.info("[Package end] - height:{} - The number of packaged transactions this time:{} - Current queue transactions to be packagedhashnumber:{}, - Actual number of transactions in the queue to be packaged:{}" + TxUtil.nextLine(),
                    blockHeight, packableTxs.size(), packablePool.packableHashQueueSize(chain), packablePool.packableTxMapSize(chain));

            return txPackage;
        } catch (Exception e) {
            nulsLogger.error(e);
            //Packable transactions,Orphan Trading,Add it all back
            putBackPackablePool(chain, packingTxList, orphanTxSet);
            return new TxPackage(new ArrayList<>(), null, chain.getBestBlockHeight() + 1);
        } finally {
            chain.getPackageLock().unlock();
        }
    }

    /**
     * packing verify ledger
     *
     * @param chain
     * @param batchProcessList
     * @param currentBatchPackableTxs
     * @param orphanTxSet
     * @param proccessContract        Whether to handle smart contracts
     * @param orphanNoCount           (Is it necessary to verify the ledger again due to the return of the contract)When the orphan transaction was returned Not counting the number of times it is returned
     * @throws NulsException
     */
    private void verifyLedger(Chain chain, List<String> batchProcessList, List<TxPackageWrapper> currentBatchPackableTxs,
                              Set<TxPackageWrapper> orphanTxSet, boolean proccessContract, boolean orphanNoCount) throws NulsException {
        //Start processing
        Map verifyCoinDataResult = LedgerCall.verifyCoinDataBatchPackaged(chain, batchProcessList);
        List<String> failHashs = (List<String>) verifyCoinDataResult.get("fail");
        List<String> orphanHashs = (List<String>) verifyCoinDataResult.get("orphan");
        if (!failHashs.isEmpty() || !orphanHashs.isEmpty()) {
            chain.getLogger().error("Package verify Ledger fail tx count:{}", failHashs.size());
            chain.getLogger().error("Package verify Ledger orphan tx count:{}", orphanHashs.size());

            Iterator<TxPackageWrapper> it = currentBatchPackableTxs.iterator();
            boolean backContract = false;
            removeAndGo:
            while (it.hasNext()) {
                TxPackageWrapper txPackageWrapper = it.next();
                Transaction transaction = txPackageWrapper.getTx();
                //Remove transactions with failed ledger verification
                for (String hash : failHashs) {
                    String hashStr = transaction.getHash().toHex();
                    if (hash.equals(hashStr)) {
                        if (!backContract && proccessContract && TxManager.isUnSystemSmartContract(chain, transaction.getType())) {
                            //Set Flag,If it is a non system transaction of smart contracts,Not Verified Passed,Then all non system smart contract transactions need to be returned to the waiting queue for packaging.
                            backContract = true;
                        } else {
                            clearInvalidTx(chain, transaction);
                        }
                        it.remove();
                        continue removeAndGo;
                    }
                }
                //Remove orphan transactions, Simultaneously placing orphan transactions into the orphan pool
                for (String hash : orphanHashs) {
                    String hashStr = transaction.getHash().toHex();
                    if (hash.equals(hashStr)) {
                        if (!backContract && proccessContract && TxManager.isUnSystemSmartContract(chain, transaction.getType())) {
                            //Set Flag, If it is a non system transaction of smart contracts,Not Verified Passed,Then all non system smart contract transactions need to be returned to the waiting queue for packaging.
                            backContract = true;
                        } else {
                            //Orphan Trading
                            if (orphanNoCount) {
                                //If it's because after the contract is returned,Verifying that the ledger is an orphan transaction does not require counting Directly return it
                                orphanTxSet.add(txPackageWrapper);
                            } else {
                                addOrphanTxSet(chain, orphanTxSet, txPackageWrapper);
                            }
                        }
                        it.remove();
                        continue removeAndGo;
                    }
                }
            }
            //If there are non system transactions of smart contracts that have not been verified,Then all non system smart contract transactions need to be returned to the waiting queue for packaging.
            if (backContract && proccessContract) {
                Iterator<TxPackageWrapper> its = currentBatchPackableTxs.iterator();
                while (its.hasNext()) {
                    TxPackageWrapper txPackageWrapper = it.next();
                    Transaction transaction = txPackageWrapper.getTx();
                    if (TxManager.isUnSystemSmartContract(chain, transaction.getType())) {
                        //If it is a non system transaction of smart contracts,Not Verified Passed,Then all non system smart contract transactions need to be returned to the waiting queue for packaging.
                        packablePool.offerFirstOnlyHash(chain, transaction);
                        chain.setContractTxFail(true);
                        it.remove();
                    }
                }
            }
        }
    }

    /**
     * Processing smart contract transactions results of enforcement
     *
     * @param chain
     * @param packingTxList
     * @param orphanTxSet
     * @param contractGenerateTxs
     * @param blockHeight
     * @param contractBefore
     * @param stateRoot
     * @return Return newly generatedstateRoot
     * @throws IOException
     */
    private Map processContractResult(Chain chain, List<TxPackageWrapper> packingTxList, Set<TxPackageWrapper> orphanTxSet, List<String> contractGenerateTxs,
                                      long blockHeight, boolean contractBefore, String stateRoot) throws IOException {

        boolean hasTxbackPackablePool = false;
        /**WhencontractBeforeNotification failed,perhapscontractBatchEndIf it fails, the smart contract transaction needs to be returned to the waiting queue for packaging*/
        boolean isRollbackPackablePool = false;
        /**
         * When consensus transaction verification generated by smart contracts fails, The corresponding original transaction has a limited number of returns to the packaging queue
         * (Record the original transactions of contracts with a return limithash)
         */
        Set<String> setLimitedRollbackOriginTx = new HashSet<>();
        if (!contractBefore) {
            isRollbackPackablePool = true;
        } else {
            try {
                Map<String, Object> map = ContractCall.contractPackageBatchEnd(chain, blockHeight);
                List<String> scNewList = (List<String>) map.get("txList");
                List<String> originTxList = (List<String>) map.get("originTxList");
                if (null != scNewList) {
                    /**
                     * 1.Consensus verification If there is any
                     * 2.If only consensus transactions for smart contracts fail,isRollbackPackablePool=true
                     * 3.If only other consensus transactions fail, delete them separately
                     * 4.blend implement2.
                     */
                    List<String> scNewConsensusList = new ArrayList<>();
                    List<String> scNewTokenCrossTransferList = new ArrayList<>();
//                    for (String scNewTx : scNewList) {
                    for (int i = 0; i < scNewList.size(); i++) {
                        String scNewTx = scNewList.get(i);
                        int scNewTxType = TxUtil.extractTxTypeFromTx(scNewTx);
                        if (scNewTxType == TxType.CONTRACT_CREATE_AGENT
                                || scNewTxType == TxType.CONTRACT_DEPOSIT
                                || scNewTxType == TxType.CONTRACT_CANCEL_DEPOSIT
                                || scNewTxType == TxType.CONTRACT_STOP_AGENT) {
                            scNewConsensusList.add(scNewTx);
                            setLimitedRollbackOriginTx.add(originTxList.get(i));
                        } else if (scNewTxType == TxType.CONTRACT_TOKEN_CROSS_TRANSFER) {
                            scNewTokenCrossTransferList.add(scNewTx);
                        }
                    }
                    if (!scNewConsensusList.isEmpty() || !scNewTokenCrossTransferList.isEmpty()) {
                        //Collect consensus module/All transactions across chain modules, Add the newly generated smart contract consensus transaction and perform module unified verification again together
                        List<String> consensusList = new ArrayList<>();
                        List<String> crossTransferList = new ArrayList<>();
                        for (TxPackageWrapper txPackageWrapper : packingTxList) {
                            Transaction tx = txPackageWrapper.getTx();
                            if (ModuleE.CS.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(tx.getType()))) {
                                consensusList.add(RPCUtil.encode(txPackageWrapper.getTx().serialize()));
                            }
                            if (ModuleE.CC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(tx.getType()))) {
                                crossTransferList.add(RPCUtil.encode(txPackageWrapper.getTx().serialize()));
                            }
                        }
                        consensusList.addAll(scNewConsensusList);
                        crossTransferList.addAll(scNewTokenCrossTransferList);
                        if (!consensusList.isEmpty()) {
                            isRollbackPackablePool = processContractTxs(chain, ResponseMessageProcessor.ROLE_MAPPING.get(ModuleE.CS.abbr), consensusList, packingTxList, false);
                        }
                        if (!isRollbackPackablePool && !crossTransferList.isEmpty()) {
                            isRollbackPackablePool = processContractTxs(chain, ResponseMessageProcessor.ROLE_MAPPING.get(ModuleE.CC.abbr), crossTransferList, packingTxList, false);
                        }
                    }
                    if (!isRollbackPackablePool) {
                        // Contract consensus There are no failed transactions across contract chains Then obtain and use the newstateRoot
                        String sr = (String) map.get("stateRoot");
                        if (null != sr) {
                            stateRoot = sr;
                        }
                        // Join the contract to generate transaction validation through the set(Will be added to the packaging collection)
                        contractGenerateTxs.addAll(scNewList);
                    }
                }
                if (!isRollbackPackablePool) {
                    //If the contract transaction does not need to be fully put back into the waiting queue for packaging,Just check if there are any unexecuted smart contracts,Then put it back in the queue to be packaged,Next execution.
                    List<String> nonexecutionList = (List<String>) map.get("pendingTxHashList");
                    if (null != nonexecutionList && !nonexecutionList.isEmpty()) {
                        chain.getLogger().debug("contract pending tx count:{} ", nonexecutionList.size());
                        Iterator<TxPackageWrapper> iterator = packingTxList.iterator();
                        while (iterator.hasNext()) {
                            TxPackageWrapper txPackageWrapper = iterator.next();
                            for (String hash : nonexecutionList) {
                                if (hash.equals(txPackageWrapper.getTx().getHash().toHex())) {
                                    orphanTxSet.add(txPackageWrapper);
                                    //Remove from packable collection
                                    iterator.remove();
                                    if (!hasTxbackPackablePool) {
                                        hasTxbackPackablePool = true;
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            } catch (NulsException e) {
                chain.getLogger().error(e);
                isRollbackPackablePool = true;
            }
        }
        // Determine if the smart contract needs to be added back to the queue for packaging
        if (isRollbackPackablePool) {
            Iterator<TxPackageWrapper> iterator = packingTxList.iterator();
            while (iterator.hasNext()) {
                TxPackageWrapper txPackageWrapper = iterator.next();
                if (TxManager.isUnSystemSmartContract(chain, txPackageWrapper.getTx().getType())) {
                    if (setLimitedRollbackOriginTx.contains(txPackageWrapper.getTx().getHash().toHex())) {
                        // Transactions with a limit on the number of times they can be added back
                        addOrphanTxSet(chain, orphanTxSet, txPackageWrapper);
                    } else {
                        // Without a limit on the number of times to add back, Directly join the set,Can share a set with orphan transactions
                        orphanTxSet.add(txPackageWrapper);
                    }
                    //Remove from packable collection
                    iterator.remove();
                    if (!hasTxbackPackablePool) {
                        hasTxbackPackablePool = true;
                    }
                }
            }
        }
        Map rs = new HashMap();
        rs.put("stateRoot", stateRoot);
        rs.put("hasTxbackPackablePool", hasTxbackPackablePool);
        return rs;
    }

    /**
     * Handling consensus transactions for smart contracts
     *
     * @param chain
     * @param verifyList
     * @param packingTxList
     * @param batchVerify
     * @return
     * @throws NulsException
     */
    private boolean processContractTxs(Chain chain, String moduleCode, List<String> verifyList, List<TxPackageWrapper> packingTxList, boolean batchVerify) throws NulsException {
        while (true) {
            List<String> txHashList = null;
            try {
                txHashList = TransactionCall.txModuleValidator(chain, moduleCode, verifyList);
            } catch (NulsException e) {
                chain.getLogger().error("Package module verify failed -txModuleValidator Exception:{}, module-code:{}, count:{} , return count:{}",
                        BaseConstant.TX_VALIDATOR, moduleCode, verifyList.size(), txHashList.size());
                txHashList = new ArrayList<>(verifyList.size());
                for (String txStr : verifyList) {
                    Transaction tx = TxUtil.getInstanceRpcStr(txStr, Transaction.class);
                    txHashList.add(tx.getHash().toHex());
                }
            }
            if (txHashList.isEmpty()) {
                //All executed successfully
                return false;
            }
            chain.getLogger().warn("Package module verify failed -txModuleValidator Exception:{}, module-code:{}, count:{} , return count:{}",
                    BaseConstant.TX_VALIDATOR, moduleCode, verifyList.size(), txHashList.size());
            if (batchVerify) {
                //If it is a verification block transaction, there are some that do not pass Directly return
                return true;
            }
            Iterator<String> it = verifyList.iterator();
            while (it.hasNext()) {
                Transaction tx = TxUtil.getInstanceRpcStr(it.next(), Transaction.class);
                int type = tx.getType();
                for (String hash : txHashList) {
                    if (hash.equals(tx.getHash().toHex()) && (type == TxType.CONTRACT_CREATE_AGENT
                            || type == TxType.CONTRACT_DEPOSIT
                            || type == TxType.CONTRACT_CANCEL_DEPOSIT
                            || type == TxType.CONTRACT_STOP_AGENT
                            || type == TxType.CONTRACT_TOKEN_CROSS_TRANSFER)) {
                        //There is a smart contract transaction that does not pass Then return all smart contract transactions to the queue to be packaged
                        return true;
                    }
                }
            }
            /**
             * No smart contract failed,Only situations where ordinary consensus transactions fail
             * 1.Delete from the queue to be packaged
             * 2.Remove from the unified validation set of modules and verify again until all validations pass
             */
            for (int i = 0; i < txHashList.size(); i++) {
                String hash = txHashList.get(i);
                Iterator<TxPackageWrapper> its = packingTxList.iterator();
                while (its.hasNext()) {
                    /**Conflict detection has failed, Execute clear and unconfirmed rollback frompackingTxListdelete*/
                    Transaction tx = its.next().getTx();
                    if (hash.equals(tx.getHash().toHex())) {
                        clearInvalidTx(chain, tx);
                        its.remove();
                    }
                }
                Iterator<String> itcs = verifyList.iterator();
                while (itcs.hasNext()) {
                    Transaction tx = TxUtil.getInstanceRpcStr(itcs.next(), Transaction.class);
                    if (hash.equals(tx.getHash().toHex())) {
                        itcs.remove();
                    }

                }
            }
        }
    }

    /**
     * When adding orphan transactions back to the pending packaging queue, To determine how many times it has been added(Because it was verified to be an orphan transaction during the next packaging, it will be added back again), Once the threshold is reached, it will no longer be added back
     */
    private void addOrphanTxSet(Chain chain, Set<TxPackageWrapper> orphanTxSet, TxPackageWrapper txPackageWrapper) {
        NulsHash hash = txPackageWrapper.getTx().getHash();
        Integer count = chain.getTxPackageOrphanMap().get(hash);
        if (count == null || count < TxConstant.PACKAGE_ORPHAN_MAXCOUNT) {
            orphanTxSet.add(txPackageWrapper);
            if (count == null) {
                count = 1;
            } else {
                count++;
            }
            if (chain.getTxPackageOrphanMap().size() > TxConstant.PACKAGE_ORPHAN_MAP_MAXCOUNT) {
                chain.getTxPackageOrphanMap().clear();
            }
            chain.getTxPackageOrphanMap().put(hash, count);
        } else {
            //Do not add back(discard), Simultaneously deletemapMiddlekey,And clean up
            chain.getLogger().debug("exceed5Secondary Orphan Trading hash:{}", hash.toHex());
            clearInvalidTx(chain, txPackageWrapper.getTx());
            chain.getTxPackageOrphanMap().remove(hash);
        }
    }

    /**
     * Add the transaction back to the packaging queue
     * Trading Orphans(If there is any),Add to the verified transaction set,Sort in reverse order of removal,Then add the frontend of the queue to be packaged in sequence
     *
     * @param chain
     * @param txList      Verified transactions
     * @param orphanTxSet Orphan Trading
     */
    private void putBackPackablePool(Chain chain, List<TxPackageWrapper> txList, Set<TxPackageWrapper> orphanTxSet) {
        if (null == txList) {
            txList = new ArrayList<>();
        }
        if (null != orphanTxSet && !orphanTxSet.isEmpty()) {
            txList.addAll(orphanTxSet);
        }
        if (txList.isEmpty()) {
            return;
        }
        //Orphan transactions in reverse order,Add all back to the waiting to be packed queue
        txList.sort(new Comparator<TxPackageWrapper>() {
            @Override
            public int compare(TxPackageWrapper o1, TxPackageWrapper o2) {
                return o1.compareTo(o2.getIndex());
            }
        });
        for (TxPackageWrapper txPackageWrapper : txList) {
            packablePool.offerFirstOnlyHash(chain, txPackageWrapper.getTx());
        }
        chain.getLogger().info("putBackPackablePool count:{}", txList.size());
    }

    private void putBackPackablePool(Chain chain, Set<TxPackageWrapper> orphanTxSet) {
        putBackPackablePool(chain, null, orphanTxSet);
    }

    /**
     * 1.Unified verification
     * 2a:If there are no transactions that fail verification, end!!
     * 2b.When there are failed verifications,moduleVerifyMapFilter out transactions that do not pass.
     * 3.Re validate transactions in the same module that do not pass after the transaction(Including individualverifyandcoinData), execute again1.recursion？
     *
     * @param moduleVerifyMap
     */
    private boolean txModuleValidatorPackable(Chain chain, Map<String, List<String>> moduleVerifyMap, List<TxPackageWrapper> packingTxList, Set<TxPackageWrapper> orphanTxSet) throws NulsException {
        Iterator<Map.Entry<String, List<String>>> it = moduleVerifyMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            List<String> moduleList = entry.getValue();
            if (moduleList.size() == 0) {
                //When the module transaction is filtered out midway through recursion, it will causelistEmpty,At this point, there is no need to call the module's unified validator again
                it.remove();
                continue;
            }
            String moduleCode = entry.getKey();
            List<String> txHashList = null;
            try {
                txHashList = TransactionCall.txModuleValidator(chain, moduleCode, moduleList);
            } catch (NulsException e) {
                chain.getLogger().error("Package module verify failed -txModuleValidator Exception:{}, module-code:{}, count:{} , return count:{}",
                        BaseConstant.TX_VALIDATOR, moduleCode, moduleList.size(), txHashList.size());
                //If there is an error, delete the entire transaction of the module
                Iterator<TxPackageWrapper> its = packingTxList.iterator();
                while (its.hasNext()) {
                    Transaction tx = its.next().getTx();
                    TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                    if (txRegister.getModuleCode().equals(moduleCode)) {
                        clearInvalidTx(chain, tx);
                        its.remove();
                    }
                }
                continue;
            }
            if (null == txHashList || txHashList.isEmpty()) {
                //Module unified verification without conflicts, frommapKill it in the middle
                it.remove();
                continue;
            }
            chain.getLogger().error("[Package module verify failed] module:{}, module-code:{}, count:{} , return count:{}",
                    BaseConstant.TX_VALIDATOR, moduleCode, moduleList.size(), txHashList.size());
            /**Conflict detection has failed, Execute clear and unconfirmed rollback frompackingTxListdelete*/
            for (int i = 0; i < txHashList.size(); i++) {
                String hash = txHashList.get(i);
                Iterator<TxPackageWrapper> its = packingTxList.iterator();
                while (its.hasNext()) {
                    Transaction tx = its.next().getTx();
                    if (hash.equals(tx.getHash().toHex())) {
                        clearInvalidTx(chain, tx);
                        its.remove();
                    }
                }
            }
        }

        if (moduleVerifyMap.isEmpty()) {
            return true;
        }
        moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_16);
        verifyAgain(chain, moduleVerifyMap, packingTxList, orphanTxSet, false);
        return txModuleValidatorPackable(chain, moduleVerifyMap, packingTxList, orphanTxSet);
    }

    /**
     * @param chain
     * @param moduleVerifyMap
     * @param packingTxList
     * @param orphanTxSet
     * @param orphanNoCount   (Is it necessary to verify the ledger again due to the return of the contract)When the orphan transaction was returned Not counting the number of times it is returned
     * @throws NulsException
     */
    private void verifyAgain(Chain chain, Map<String, List<String>> moduleVerifyMap, List<TxPackageWrapper> packingTxList, Set<TxPackageWrapper> orphanTxSet, boolean orphanNoCount) throws NulsException {
        chain.getLogger().debug("------ verifyAgain Batch verification notification for packaging again ------");
        //Send batch verification to the ledger modulecoinDataIdentification of
        LedgerCall.coinDataBatchNotify(chain);
        List<String> batchProcessList = new ArrayList<>();
        for (TxPackageWrapper txPackageWrapper : packingTxList) {
            /* 2019-12-31
            if (TxManager.isSystemSmartContract(chain, txPackageWrapper.getTx().getType())) {
                //Smart contract system transactions do not require verification of ledgers
                continue;
            }*/
            batchProcessList.add(txPackageWrapper.getTxHex());
        }
        verifyLedger(chain, batchProcessList, packingTxList, orphanTxSet, true, orphanNoCount);

        for (TxPackageWrapper txPackageWrapper : packingTxList) {
            Transaction tx = txPackageWrapper.getTx();
            TxUtil.moduleGroups(chain, moduleVerifyMap, tx);
        }
    }

    /**
     * Only one transaction is allowed in the verification block, and there cannot be multiple transactions
     */
    public void verifySysTxCount(Set<Integer> onlyOneTxTypes, int type) throws NulsException {
        switch (type) {
            case TxType.COIN_BASE:
            case TxType.YELLOW_PUNISH:
            case TxType.CONTRACT_RETURN_GAS:
                if (!onlyOneTxTypes.add(type)) {
                    throw new NulsException(TxErrorCode.CONTAINS_MULTIPLE_UNIQUE_TXS);
                }
                break;
            default:
                break;
        }
    }

    @Override
    public Map<String, Object> batchVerify(Chain chain, List<String> txStrList, BlockHeader blockHeader, String blockHeaderStr, String preStateRoot) throws NulsException {
        NulsLogger logger = chain.getLogger();
        long s1 = NulsDateUtils.getCurrentTimeMillis();
        long blockHeight = blockHeader.getHeight();
        if (logger.isDebugEnabled()) {
            logger.debug("[Verify block transactions] start -----height:{} -----Number of block transactions:{}", blockHeight, txStrList.size());
        }
        List<TxVerifyWrapper> txList = new ArrayList<>();
        //Only one transaction is allowed in the verification block, and there cannot be multiple transactions
        Set<Integer> onlyOneTxTypes = new HashSet<>();
        //Smart contract notification identifier,When the first smart contract transaction occurs and the validator is called to pass,If there is, only notify on the first attempt.
        boolean contractNotify = false;
        Transaction scReturnGas = null;
        long blockTime = blockHeader.getTime();
        List<Future<Boolean>> futures = new ArrayList<>();
        //Assemble unified validation parameter data,keyUnify validators for each modulecmd
        Map<String, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        int chainId = chain.getChainId();
        long timeF1 = 0L;
        long timeF2 = 0L;
        long timeF3 = 0L;
        long timeF4 = 0L;
        List<byte[]> keys = new ArrayList<>();
        long f1 = System.currentTimeMillis();
        for (String txStr : txStrList) {
            Transaction tx = TxUtil.getInstanceRpcStr(txStr, Transaction.class);
            txList.add(new TxVerifyWrapper(tx, txStr));
            int type = tx.getType();
            verifySysTxCount(onlyOneTxTypes, type);
            TxRegister txRegister = TxManager.getTxRegister(chain, type);
            if (null == txRegister) {
                throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
            }
            if (type == TxType.CONTRACT_RETURN_GAS) {
                //recordgasReturn transaction
                scReturnGas = tx;
            }
            boolean isSmartContractTx = TxManager.isUnSystemSmartContract(txRegister);
            // add by pierre at 2019-11-02 Cross chain transfer transaction sent to the smart contract module for parsing, is it a contract asset cross chain transfer Protocol upgrade required done
            if (ProtocolGroupManager.getCurrentVersion(chain.getChainId()) >= TxContext.UPDATE_VERSION_V250) {
                boolean isCrossTransferTx = TxType.CROSS_CHAIN == type;
                if (!isSmartContractTx && txConfig.isCollectedSmartContractModule()) {
                    isSmartContractTx = isCrossTransferTx;
                }
            }
            // end code by pierre
            /** Smart contracts*/
            if (isSmartContractTx) {
                /** Smart contracts appear,And the notification identifier isfalse,Then call the notification first */
                if (!contractNotify) {
                    String packingAddress = AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress(chain.getChainId()));
                    ContractCall.contractBatchBegin(chain, blockHeight, blockTime, packingAddress, preStateRoot, 1);
                    contractNotify = true;
                }
                try {
                    if (!ContractCall.invokeContract(chain, RPCUtil.encode(tx.serialize()), 1, Constants.TIMEOUT_TIMEMILLIS * 10)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("batch verify failed. invokeContract fail");
                        }
                        throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
                    }
                } catch (IOException e) {
                    throw new NulsException(TxErrorCode.SERIALIZE_ERROR);
                }
            }
            if (chain.getContractGenerateTxTypes().contains(tx.getType())) {
                //Transactions generated by the contract module that should not be included in the block transaction list
                throw new NulsException(TxErrorCode.SYS_CONTRACT_TX_NON_CIRCULATING);
            }
            keys.add(tx.getHash().getBytes());
            //According to the unified validator name of the module, group all transactions and prepare for unified verification of each module
            TxUtil.moduleGroups(moduleVerifyMap, txRegister, txStr);
        }
        if (!contractNotify && null != scReturnGas) {
            throw new NulsException(TxErrorCode.EXIST_GAS_RETURN_WITHOUT_SC_RETURN);
        }

        onlyOneTxTypes = null;
        long f2 = System.currentTimeMillis();
        timeF1 = f2 - f1;
        //Verify if the transaction has been confirmed
        List<byte[]> confirmedList = confirmedTxStorageService.getExistTxs(chainId, keys);
        if (!confirmedList.isEmpty()) {
            logger.error("There are confirmed transactions");
            try {
                for (byte[] cfmtx : confirmedList) {
                    logger.error("confirmed hash:{}", TxUtil.getTransaction(cfmtx).getHash().toHex());
                }
            } finally {
                logger.error("Show confirmed transaction deserialize fail");
                throw new NulsException(TxErrorCode.TX_CONFIRMED);
            }
        }
        long f3 = System.currentTimeMillis();
        timeF2 = f3 - f2;

        //Verify transactions that are not available locally
        List<String> unconfirmedList = unconfirmedTxStorageService.getExistKeysStr(chainId, keys);

        long f4 = System.currentTimeMillis();
        timeF3 = f4 - f3;

        Set<String> set = new HashSet<>();
        set.addAll(unconfirmedList);
        unconfirmedList = null;
        long d = 0L;
        for (TxVerifyWrapper txVerifyWrapper : txList) {
            Transaction tx = txVerifyWrapper.getTx();
            tx.setBlockHeight(blockHeight);
            //Being able to join indicates that there is no confirmation yet,Then it needs to be handled
            if (set.add(tx.getHash().toHex())) {
                long d1 = System.currentTimeMillis();
                //Perform basic validation without confirmation
                //Multi threaded processing of individual transactions
                Future<Boolean> res = verifySignExecutor.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        try {
                            //Verify only the basic content of a single transaction(TXModule Local Validation)
                            TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                            if (null == txRegister) {
                                throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
                            }
                            baseValidateTx(chain, tx, txRegister);
                        } catch (Exception e) {
                            logger.error("batchVerify failed, single tx verify failed. hash:{}, -type:{}", tx.getHash().toHex(), tx.getType());
                            logger.error(e);
                            return false;
                        }
                        return true;
                    }
                });
                futures.add(res);
                d += (System.currentTimeMillis() - d1);
            }
        }

        if (logger.isDebugEnabled()) {
            timeF4 = System.currentTimeMillis() - f4;
            logger.debug("[Verify block transactions] Deserialization,contract,grouping:{} -Have you confirmed:{} -Is it in unconfirmed status:{}, -Single validation:{} -Single internal processing:{} -Total time:{}",
                    timeF1, timeF2, timeF3, d, timeF4, NulsDateUtils.getCurrentTimeMillis() - s1);
        }

        if (contractNotify) {
            if (!ContractCall.contractBatchBeforeEnd(chain, blockHeight, 1)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("batch verify failed. contractBatchBeforeEnd fail");
                }
                throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
            }
        }

        long coinDataV = NulsDateUtils.getCurrentTimeMillis();
        //Ledger verification
        if (!LedgerCall.verifyBlockTxsCoinData(chain, txStrList, blockHeight)) {
            if (logger.isDebugEnabled()) {
                logger.debug("batch verifyCoinData failed.");
            }
            throw new NulsException(TxErrorCode.TX_LEDGER_VERIFY_FAIL);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[Verify block transactions] coinData -Time from the start of the method:{},-Verification time:{}",
                    NulsDateUtils.getCurrentTimeMillis() - s1, NulsDateUtils.getCurrentTimeMillis() - coinDataV);
        }

        //Module Unified Verifier
        long moduleV = NulsDateUtils.getCurrentTimeMillis();
        Iterator<Map.Entry<String, List<String>>> it = moduleVerifyMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            List<String> txHashList = TransactionCall.txModuleValidator(chain,
                    entry.getKey(), entry.getValue(), blockHeaderStr);
            if (txHashList != null && txHashList.size() > 0) {
                logger.error("batch module verify fail, module-code:{},  return count:{}", entry.getKey(), txHashList.size());
                throw new NulsException(TxErrorCode.TX_VERIFY_FAIL);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[Verify block transactions] Module Unified Verification Time:{}", NulsDateUtils.getCurrentTimeMillis() - moduleV);
            logger.debug("[Verify block transactions] Unified module verification -Time from the start of the method:{}", NulsDateUtils.getCurrentTimeMillis() - s1);
        }

        /** Smart contracts When the notification identifier istrue, This indicates that a smart contract has been called and executed*/
        List<String> scNewList = new ArrayList<>();
        String scStateRoot = preStateRoot;
        if (contractNotify) {
            Map<String, Object> map;
            try {
                map = ContractCall.contractBatchEnd(chain, blockHeight, Constants.TIMEOUT_TIMEMILLIS * 10);
            } catch (NulsException e) {
                logger.error(e);
                throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
            }
            scStateRoot = (String) map.get("stateRoot");

            scNewList = (List<String>) map.get("txList");
            if (null == scNewList) {
                logger.error("contract new txs is null");
                throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
            }
            /**
             * 1.Consensus verification If there is any
             * 2.If only consensus transactions for smart contracts fail,isRollbackPackablePool=true
             * 3.If only other consensus transactions fail, delete them separately
             * 4.blend implement2.
             */
            List<String> scNewConsensusList = new ArrayList<>();
            List<String> scNewTokenCrossTransferList = new ArrayList<>();
            for (String scNewTx : scNewList) {
                int scNewTxType = TxUtil.extractTxTypeFromTx(scNewTx);
                if (scNewTxType == TxType.CONTRACT_CREATE_AGENT
                        || scNewTxType == TxType.CONTRACT_DEPOSIT
                        || scNewTxType == TxType.CONTRACT_CANCEL_DEPOSIT
                        || scNewTxType == TxType.CONTRACT_STOP_AGENT) {
                    scNewConsensusList.add(scNewTx);
                } else if (scNewTxType == TxType.CONTRACT_TOKEN_CROSS_TRANSFER) {
                    scNewTokenCrossTransferList.add(scNewTx);
                }
            }
            if (!scNewConsensusList.isEmpty() || !scNewTokenCrossTransferList.isEmpty()) {
                //Collect consensus module/All transactions across chain modules, Add the newly generated smart contract consensus transaction and perform module unified verification again together
                List<String> consensusList = new ArrayList<>();
                List<String> crossTransferList = new ArrayList<>();
                int txType;
                for (TxVerifyWrapper txVerifyWrapper : txList) {
                    Transaction tx = txVerifyWrapper.getTx();
                    txType = tx.getType();
                    // The consensus transactions generated by smart contracts in the block are not added repeatedly
                    if (txType == TxType.CONTRACT_CREATE_AGENT
                            || txType == TxType.CONTRACT_DEPOSIT
                            || txType == TxType.CONTRACT_CANCEL_DEPOSIT
                            || txType == TxType.CONTRACT_STOP_AGENT) {
                        continue;
                    }
                    if (ModuleE.CS.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(tx.getType()))) {
                        consensusList.add(txVerifyWrapper.getTxStr());
                    }
                    if (ModuleE.CC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(tx.getType()))) {
                        crossTransferList.add(txVerifyWrapper.getTxStr());
                    }
                }
                consensusList.addAll(scNewConsensusList);
                crossTransferList.addAll(scNewTokenCrossTransferList);
                if (!consensusList.isEmpty()) {
                    boolean rsProcess = processContractTxs(chain, ResponseMessageProcessor.ROLE_MAPPING.get(ModuleE.CS.abbr), consensusList, null, true);
                    if (rsProcess) {
                        logger.error("contract tx consensus module verify fail.");
                        throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
                    }
                }
                if (!crossTransferList.isEmpty()) {
                    boolean rsProcess = processContractTxs(chain, ResponseMessageProcessor.ROLE_MAPPING.get(ModuleE.CC.abbr), crossTransferList, null, true);
                    if (rsProcess) {
                        logger.error("contract tx cross-chain module verify fail.");
                        throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
                    }
                }
            }
            //Verify smart contractsgasReturned transactionshex Is it correct.The transaction returned during packaging is added to the end of the block transaction queue
            int size = scNewList.size();
            if (size > 0) {
                int txSize = txStrList.size();
                String scNewTxHex = null;
                for (int i = size - 1; i >= 0; i--) {
                    String hex = scNewList.get(i);
                    int txType = TxUtil.extractTxTypeFromTx(hex);
                    if (txType == TxType.CONTRACT_RETURN_GAS) {
                        scNewTxHex = hex;
                        break;
                    }
                }
                if (scNewTxHex != null) {
                    String receivedScNewTxHex = null;
                    boolean rs = false;
                    for (int i = txSize - 1; i >= 0; i--) {
                        String txHex = txStrList.get(i);
                        int txType = TxUtil.extractTxTypeFromTx(txHex);
                        if (txType == TxType.CONTRACT_RETURN_GAS) {
                            receivedScNewTxHex = txHex;
                            if (txHex.equals(scNewTxHex)) {
                                rs = true;
                            }
                            break;
                        }
                    }
                    if (!rs) {
                        logger.error("contract error.Contract generatedgasReturn transaction:{}, - Received contractgasReturn transaction：{}", scNewTxHex, receivedScNewTxHex);
                        throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
                    }
                    //Return smart contract transactions to blocks
                    scNewList.remove(scNewTxHex);
                } else {
                    if (null != scReturnGas) {
                        throw new NulsException(TxErrorCode.EXIST_GAS_RETURN_WITHOUT_SC_RETURN);
                    }
                }
            } else {
                if (null != scReturnGas) {
                    throw new NulsException(TxErrorCode.EXIST_GAS_RETURN_WITHOUT_SC_RETURN);
                }
            }
        }
        //stateRootSend to consensus,Compare after processing
        String coinBaseTx = null;
        for (TxVerifyWrapper txVerifyWrapper : txList) {
            Transaction tx = txVerifyWrapper.getTx();
            if (tx.getType() == TxType.COIN_BASE) {
                coinBaseTx = txVerifyWrapper.getTxStr();
                break;
            }
        }
        String stateRootNew = ConsensusCall.triggerCoinBaseContract(chain, coinBaseTx, blockHeaderStr, scStateRoot);
        String stateRoot = RPCUtil.encode(blockHeader.getExtendsData().getStateRoot());
        if (!stateRoot.equals(stateRootNew)) {
            logger.warn("contract stateRoot error.");
            throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
        }

        //Multithreaded processing results
        try {
            for (Future<Boolean> future : futures) {
                if (!future.get()) {
                    logger.error("batchVerify failed, single tx verify failed");
                    throw new NulsException(TxErrorCode.TX_VERIFY_FAIL);
                }
            }
        } catch (InterruptedException e) {
            logger.error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        } catch (ExecutionException e) {
            logger.error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[Verify block transactions] Total execution time:{}, - height:{} - Number of block transactions:{}" + TxUtil.nextLine(),
                    NulsDateUtils.getCurrentTimeMillis() - s1, blockHeight, txStrList.size());
        }
        Map<String, Object> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_4);
        resultMap.put("value", true);
        resultMap.put("contractList", scNewList);
        return resultMap;
    }


    @Override
    public void clearInvalidTx(Chain chain, Transaction tx) {
        clearInvalidTx(chain, tx, true);
    }

    @Override
    public void clearInvalidTx(Chain chain, Transaction tx, boolean changeStatus) {
        unconfirmedTxStorageService.removeTx(chain.getChainId(), tx.getHash());
        //Store transactions from the waiting to be packaged teammapRemove from middle
        ByteArrayWrapper wrapper = new ByteArrayWrapper(tx.getHash().getBytes());
        chain.getPackableTxMap().remove(wrapper);
        //Store actual transactions from the queue to be packagedmapRemove this transaction from the middle
        packablePool.removeInvalidTxFromMap(chain, tx);
        //If the transaction has been confirmed, there is no need to call for ledger cleaning!!
        TransactionConfirmedPO txConfirmed = confirmedTxService.getConfirmedTransaction(chain, tx.getHash());
        if (txConfirmed == null) {
            try {
                //If it is a cleaning mechanism call, Call the unconfirmed rollback of the ledger
                LedgerCall.rollBackUnconfirmTx(chain, RPCUtil.encode(tx.serialize()));
                if (changeStatus) {
                    //Notify ledger status change
                    LedgerCall.rollbackTxValidateStatus(chain, RPCUtil.encode(tx.serialize()));
                }
            } catch (NulsException e) {
                chain.getLogger().error(e);
            } catch (Exception e) {
                chain.getLogger().error(e);
            }
        }
    }

    long MAX_GAS_COST_IN_BLOCK = 13000000L;

    @Override
    public TxPackage getPackableTxsV8(Chain chain, long endtimestamp, long maxTxDataSize, long blockTime, String packingAddress, String preStateRoot) {
        chain.getPackageLock().lock();
        long startTime = NulsDateUtils.getCurrentTimeMillis();
        List<TxPackageWrapper> packingTxList = new ArrayList<>();
        //Record orphan transactions in the ledger,Filter out when returning to consensus,Because when repackaging due to height changes,Need to restore to the queue to be packaged
        Set<TxPackageWrapper> orphanTxSet = new HashSet<>();
        NulsLogger nulsLogger = chain.getLogger();
        try {
            //The height of this packaging
            long blockHeight = chain.getBestBlockHeight() + 1;

            long packableTime = endtimestamp - startTime;
            nulsLogger.info("[Package start] -Packaging time：{}, -Packable capacity：{}B , - height:{}, - Current queue transactions to be packagedhashnumber:{}, - Actual number of transactions in the queue to be packaged:{}",
                    packableTime, maxTxDataSize, blockHeight, packablePool.packableHashQueueSize(chain), packablePool.packableTxMapSize(chain));
            long batchValidReserve = TxConstant.PACKAGE_MODULE_VALIDATOR_RESERVE_TIME;
            if (packableTime <= batchValidReserve) {
                //Directly hit the empty block
                return new TxPackage(new ArrayList<>(), null, chain.getBestBlockHeight() + 1);
            }
            //Reset Flag
            chain.setContractTxFail(false);
            //Assemble unified validation parameter data,keyUnify validators for each modulecmd
            Map<String, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);

            long packingTime = endtimestamp - startTime;
            //Statistics of total waiting time
            int allSleepTime = 0;
            //Recurrent acquisition of transaction usage time
            long whileTime;
            //Total time for verifying ledger
            long totalLedgerTime = 0;
            //Total time for module unified verification usage
            long batchModuleTime;
            long totalSize = 0L;
            //Calculate the total number of blocks when obtaining transactionssizeTemporary size value
            long totalSizeTemp = 0L;
            int maxCount = TxConstant.PACKAGE_TX_MAX_COUNT - TxConstant.PACKAGE_TX_VERIFY_COINDATA_NUMBER_OF_TIMES_TO_PROCESS;
            //Calculate the time reserved for batch validation from the total packaging time based on the configured percentage
            //            long batchValidReserve = packagingReservationTime(chain, packingTime);
            long packageRpcReserveTime = chain.getConfig().getPackageRpcReserveTime();

            //Smart contract notification identifier,When the first smart contract transaction occurs and the validator is called to pass,If there is, only notify on the first attempt.
            boolean contractNotify = false;

            //Send batch verification to the ledger modulecoinDataIdentification of
            LedgerCall.coinDataBatchNotify(chain);
            //Retrieved transaction set(Need to be sent to ledger verification)
            List<String> batchProcessList = new ArrayList<>();
            Set<String> duplicatesVerify = new HashSet<>();
            //Retrieved transaction set
            List<TxPackageWrapper> currentBatchPackableTxs = new ArrayList<>();
            //This packaging includes the number of cross chain transactions
            int corssTxCount = 0;
            //Batch processing, including the number of cross chain transactions
            int batchCorssTxCount = 0;
            //This packaging includes the number of contract transactions
            int contractTxCount = 0;
            //Batch processing, including the number of contract transactions
            int batchContractTxCount = 0;
            //Whether to stop executing the functional contract,If bittrue,The extracted smart contract will no longer be processed in this packaging process,Need to return to the packaging queue
            boolean stopInvokeContract = false;

            int packageContractTxMaxCount;
            /*Random random = new Random();
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            if (availableProcessors <= 4) {
                packageContractTxMaxCount = 20 + random.nextInt(10);
            } else if (availableProcessors <= 8) {
                packageContractTxMaxCount = 50 + random.nextInt(10);
            } else {
                packageContractTxMaxCount = 100 + random.nextInt(20);
            }*/
            packageContractTxMaxCount = 300;
            long totalGasInBlock = 0;
            List<String> contractGenerateTxs = new ArrayList<>();
            List<String> originTxList = new ArrayList<>();

            for (int index = 0; ; index++) {
                long currentTimeMillis = NulsDateUtils.getCurrentTimeMillis();
                long currentReserve = endtimestamp - currentTimeMillis;
                if (currentReserve <= batchValidReserve) {
                    if (nulsLogger.isDebugEnabled()) {
                        nulsLogger.debug("Get transaction time up to,Entering the module validation phase: currentTimeMillis:{}, -endtimestamp:{}, -offset:{}, -remaining:{}",
                                currentTimeMillis, endtimestamp, batchValidReserve, currentReserve);
                    }
                    backTempPackablePool(chain, currentBatchPackableTxs);
                    break;
                }
                if (currentReserve < packageRpcReserveTime) {
                    //overtime,Leave for final data assembly andRPCInsufficient transmission time
                    nulsLogger.error("getPackableTxs time out, endtimestamp:{}, current:{}, endtimestamp-current:{}, reserveTime:{}",
                            endtimestamp, currentTimeMillis, currentReserve, packageRpcReserveTime);
                    backTempPackablePool(chain, currentBatchPackableTxs);
                    throw new NulsException(TxErrorCode.PACKAGE_TIME_OUT);
                }
                if (chain.getProtocolUpgrade().get()) {
                    chain.getCanProtocolUpgrade().set(false);
                    nulsLogger.info("3_chain.getCanProtocolUpgrade().set(false);");
                    nulsLogger.info("Protocol Upgrade Package stop -chain:{} -best block height", chain.getChainId(), chain.getBestBlockHeight());
                    backTempPackablePool(chain, currentBatchPackableTxs);
                    //Put back packable transactions and orphans
                    putBackPackablePool(chain, packingTxList, orphanTxSet);
                    //Directly hit the empty block
                    TxPackage txPackage = new TxPackage(new ArrayList<>(), null, chain.getBestBlockHeight() + 1);
                    chain.getCanProtocolUpgrade().set(true);
                    nulsLogger.info("3_chain.getCanProtocolUpgrade().set(true);");
                    return txPackage;

                }
                //If the latest local block+1 Greater than the current height of the packaging block, Explanation: The latest local block has been updated,Need to repackage,Put the retrieved transaction back into the packaging queue
                if (blockHeight < chain.getBestBlockHeight() + 1) {
                    nulsLogger.info("Obtaining the latest block height during the transaction process has increased,Put the retrieved transactions and orphans back into the packaging queue, Repackaging...");
                    backTempPackablePool(chain, currentBatchPackableTxs);
                    //Put back packable transactions and orphans
                    putBackPackablePool(chain, packingTxList, orphanTxSet);
                    return getPackableTxsV8(chain, endtimestamp, maxTxDataSize, blockTime, packingAddress, preStateRoot);
                }
                if (packingTxList.size() > maxCount) {
                    if (nulsLogger.isDebugEnabled()) {
                        nulsLogger.debug("Obtaining transaction completedmax count,Entering the module validation phase: currentTimeMillis:{}, -endtimestamp:{}, -offset:{}, -remaining:{}",
                                currentTimeMillis, endtimestamp, batchValidReserve, endtimestamp - currentTimeMillis);
                    }
                    backTempPackablePool(chain, currentBatchPackableTxs);
                    break;
                }
                int batchProcessListSize = batchProcessList.size();
                boolean process = false;
                Transaction tx = null;
                boolean maxDataSize = false;
                try {
                    tx = packablePool.poll(chain);
                    if (tx == null && batchProcessListSize == 0) {
                        Thread.sleep(10L);
                        allSleepTime += 10;
                        continue;
                    } else if (tx == null && batchProcessListSize > 0) {
                        //Meet the conditions for processing this batch
                        process = true;
                    } else if (tx != null) {
                        if (!duplicatesVerify.add(tx.getHash().toHex())) {
                            //If you don't join, it means it already exists
                            continue;
                        }
                        long txSize = tx.size();
                        if ((totalSizeTemp + txSize) > maxTxDataSize) {
                            packablePool.offerFirstOnlyHash(chain, tx);
                            nulsLogger.info("The transaction has reached its maximum capacity, actual value: {}, totalSizeTemp:{}, Current transactionsize：{} - Reserve maximum valuemaxTxDataSize:{}, txhash:{}", totalSize, totalSizeTemp, txSize, maxTxDataSize, tx.getHash().toHex());
                            maxDataSize = true;
                            if (batchProcessListSize > 0) {
                                //Meet the conditions for processing this batch
                                process = true;
                            } else {
                                break;
                            }
                        } else {
                            //Limit the number of cross chain transactions
                            if (ModuleE.CC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(tx.getType()))) {
                                if (corssTxCount + (++batchCorssTxCount) >= TxConstant.PACKAGE_CROSS_TX_MAX_COUNT) {
                                    //Limit the total number of cross chain transactions contained in a single block. If the maximum number of cross chain transactions is exceeded, put it back, Then stop obtaining transactions
                                    packablePool.add(chain, tx);
                                    if (batchProcessListSize > 0) {
                                        //Meet the conditions for processing this batch
                                        process = true;
                                    } else {
                                        break;
                                    }
                                }
                            }
                            //Limit the number of smart contract transactions
                            boolean isContract = ModuleE.SC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(tx.getType()));
                            if (isContract) {
                                if (contractTxCount + (++batchContractTxCount) >= packageContractTxMaxCount) {
                                    //Limit the total number of cross chain transactions contained in a single block. If the maximum number of cross chain transactions is exceeded, put it back, Then stop obtaining transactions
                                    packablePool.add(chain, tx);
                                    if (batchProcessListSize > 0) {
                                        //Meet the conditions for processing this batch
                                        process = true;
                                    } else {
                                        break;
                                    }
                                }
                            }
                            String txHex;
                            try {
                                txHex = RPCUtil.encode(tx.serialize());
                            } catch (Exception e) {
                                nulsLogger.warn(e.getMessage(), e);
                                nulsLogger.error("Discard acquisitionhexWrong transaction, txHash:{}, - type:{}, - time:{}", tx.getHash().toHex(), tx.getType(), tx.getTime());
                                clearInvalidTx(chain, tx);
                                continue;
                            }
                            TxPackageWrapper txPackageWrapper = new TxPackageWrapper(tx, index, txHex);
                            batchProcessList.add(txHex);
                            currentBatchPackableTxs.add(txPackageWrapper);
                            if (batchProcessList.size() == TxConstant.PACKAGE_TX_VERIFY_COINDATA_NUMBER_OF_TIMES_TO_PROCESS) {
                                //Meet the conditions for processing this batch
                                process = true;
                            }
                        }
                        //Total size plus the size of each transaction in the current batch
                        totalSizeTemp += txSize;
                    }
                    if (process) {
                        long verifyLedgerStart = NulsDateUtils.getCurrentTimeMillis();
                        if (!chain.getPackableState().get()) {
                            nulsLogger.info("Saving or rolling back blocks during the transaction process triggers ledger submission or rollback, Repackaging...");
                            //Put back packable transactions and orphans
                            packingTxList.addAll(currentBatchPackableTxs);
                            putBackPackablePool(chain, packingTxList, orphanTxSet);
                            Thread.sleep(30L);
                            return getPackableTxsV8(chain, endtimestamp, maxTxDataSize, blockTime, packingAddress, preStateRoot);
                        }
                        verifyLedger(chain, batchProcessList, currentBatchPackableTxs, orphanTxSet, false, false);
                        totalLedgerTime += NulsDateUtils.getCurrentTimeMillis() - verifyLedgerStart;

                        Iterator<TxPackageWrapper> it = currentBatchPackableTxs.iterator();
                        while (it.hasNext()) {
                            TxPackageWrapper txPackageWrapper = it.next();
                            Transaction transaction = txPackageWrapper.getTx();
                            TxRegister txRegister = TxManager.getTxRegister(chain, transaction.getType());
                            boolean isSmartContractTx = ModuleE.SC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(transaction.getType()));
                            boolean isCrossTx = ModuleE.CC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(transaction.getType()));
                            // add by pierre at 2019-11-02 Cross chain transfer transaction sent to the smart contract module for parsing, is it a contract asset cross chain transfer Protocol upgrade required done
                            if (ProtocolGroupManager.getCurrentVersion(chain.getChainId()) >= TxContext.UPDATE_VERSION_V250) {
                                boolean isCrossTransferTx = TxType.CROSS_CHAIN == transaction.getType();
                                if (!isSmartContractTx && txConfig.isCollectedSmartContractModule()) {
                                    isSmartContractTx = isCrossTransferTx;
                                }
                            }
                            // end code by pierre
                            if (isSmartContractTx) {
                                if (stopInvokeContract) {
                                    //This logotrue,Indicates that smart contract transactions will no longer be processed,Need to temporarily store transactions,Unified return to packaging queue
                                    orphanTxSet.add(txPackageWrapper);
                                    it.remove();
                                    continue;
                                }
                                // Smart contracts appear,And the notification identifier isfalse,Then call the notification first
                                if (!contractNotify) {
                                    ContractCall.contractBatchBegin(chain, blockHeight, blockTime, packingAddress, preStateRoot, 0);
                                    contractNotify = true;
                                }
                                try {
                                    // Calling and executing smart contracts
                                    Map<String, Object> invokeContractRs = ContractCall.invokeContractV8(chain, txPackageWrapper.getTxHex(), 0);
                                    //boolean success = (boolean) invokeContractRs.get("success");
                                    long gasUsed = Long.valueOf(invokeContractRs.get("gasUsed").toString());
                                    List<String> txList = (List<String>) invokeContractRs.get("txList");
                                    totalGasInBlock += gasUsed;
                                    if (txList != null && !txList.isEmpty()) {
                                        contractGenerateTxs.addAll(txList);
                                        String txHash = transaction.getHash().toString();
                                        for (int i = 0, size = txList.size(); i < size; i++) {
                                            originTxList.add(txHash);
                                        }
                                    }

                                    // Check the blocks that have been usedGAS
                                    if (totalGasInBlock >= MAX_GAS_COST_IN_BLOCK) {
                                        //No more postsinvoke
                                        stopInvokeContract = true;
                                        continue;
                                    }
                                } catch (NulsException e) {
                                    chain.getLogger().error(e);
                                    clearInvalidTx(chain, transaction);
                                    continue;
                                }
                            }
                            totalSize += transaction.getSize();

                            //Calculate the number of cross chain transactions
                            if (isCrossTx) {
                                corssTxCount++;
                            }
                            //Calculate the number of contract transactions
                            if (isSmartContractTx) {
                                contractTxCount++;
                            }
                            //According to the unified validator name of the module, group all transactions and prepare for unified verification of each module
                            TxUtil.moduleGroups(moduleVerifyMap, txRegister, RPCUtil.encode(transaction.serialize()));
                            // Check remaining packaging time
                            long _currentTimeMillis = NulsDateUtils.getCurrentTimeMillis();
                            long _currentReserve = endtimestamp - _currentTimeMillis;
                            if (_currentReserve <= batchValidReserve) {
                                nulsLogger.info("Package transaction time is up,Entering the module validation phase: currentTimeMillis:{}, -endtimestamp:{}, -offset:{}, -remaining:{}",
                                        _currentTimeMillis, endtimestamp, batchValidReserve, _currentReserve);
                                //No more postsinvoke
                                stopInvokeContract = true;
                                continue;
                            }
                        }
                        //Update to the latest total block transaction size
                        totalSizeTemp = totalSize;
                        packingTxList.addAll(currentBatchPackableTxs);

                        //Batch end reset data
                        batchProcessList.clear();
                        currentBatchPackableTxs.clear();
                        batchCorssTxCount = 0;
                        batchContractTxCount = 0;
                        if (maxDataSize) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    currentBatchPackableTxs.clear();
                    nulsLogger.error("Packaging transaction exception, txHash:{}, - type:{}, - time:{}", tx.getHash().toHex(), tx.getType(), tx.getTime());
                    nulsLogger.error(e);
                    continue;
                }

            }
            //Recurrent acquisition of transaction usage time
            whileTime = NulsDateUtils.getCurrentTimeMillis() - startTime;
            nulsLogger.info("-Retrieved transactions -count:{} - data size:{}", packingTxList.size(), totalSize);

            boolean contractBefore = false;
            if (contractNotify) {
                contractBefore = ContractCall.contractBatchBeforeEnd(chain, blockHeight, 0);
            }
            //Processing smart contracts
            String stateRoot = preStateRoot;
            boolean hasTxbackPackablePool = false;
            long contractStart = NulsDateUtils.getCurrentTimeMillis();
            /** Smart contracts When the notification identifier istrue, This indicates that a smart contract has been called and executed*/
            if (contractNotify && !chain.getContractTxFail()) {
                //Processing smart contract execution results
                Map map = processContractResultV8(chain, packingTxList, orphanTxSet, contractGenerateTxs, originTxList, blockHeight, contractBefore, stateRoot);
                stateRoot = (String) map.get("stateRoot");
                hasTxbackPackablePool = (boolean) map.get("hasTxbackPackablePool");
            }
            //If the contractinvokeContract transactions that need to be returned from time to time,Or there may be a transaction where the contract execution result is returned,All require revalidation of the ledger
            if (stopInvokeContract || hasTxbackPackablePool) {
                //If there are transactions that are returned or fail verification in the smart contract Then it is necessary to verify the ledger again
                moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_16);
                verifyAgain(chain, moduleVerifyMap, packingTxList, orphanTxSet, true);
            }
            long contractTime = NulsDateUtils.getCurrentTimeMillis() - contractStart;

            //Module Unified Verifier
            long batchStart = NulsDateUtils.getCurrentTimeMillis();
            txModuleValidatorPackable(chain, moduleVerifyMap, packingTxList, orphanTxSet);
            //Total time for module unified verification usage
            batchModuleTime = NulsDateUtils.getCurrentTimeMillis() - batchStart;

            List<String> packableTxs = new ArrayList<>();
            Iterator<TxPackageWrapper> iterator = packingTxList.iterator();
            Map<NulsHash, Integer> txPackageOrphanMap = chain.getTxPackageOrphanMap();
            while (iterator.hasNext()) {
                TxPackageWrapper txPackageWrapper = iterator.next();
                Transaction tx = txPackageWrapper.getTx();
                NulsHash hash = tx.getHash();
                if (txPackageOrphanMap.containsKey(hash)) {
                    txPackageOrphanMap.remove(hash);
                }
                try {
                    packableTxs.add(RPCUtil.encode(tx.serialize()));
                } catch (Exception e) {
                    clearInvalidTx(chain, tx);
                    iterator.remove();
                    throw new NulsException(e);
                }
            }
            //Return the generated smart contractGASoftxAdd to the end of the team
            if (!hasTxbackPackablePool && contractGenerateTxs.size() > 0) {
                String csTxStr = contractGenerateTxs.get(contractGenerateTxs.size() - 1);
                if (TxUtil.extractTxTypeFromTx(csTxStr) == TxType.CONTRACT_RETURN_GAS) {
                    packableTxs.add(csTxStr);
                }
            }
            //Check the latest height
            if (blockHeight < chain.getBestBlockHeight() + 1) {
                //This stage is not enough time to pack again,So directly timeout the exception handling transaction and roll it back to the queue to be packaged,Empty block
                nulsLogger.info("Obtain transaction completion time,The current latest height has increased,Not enough time to repackage,Directly timeout exception handling transaction rollback to the queue to be packaged,Empty block");
                throw new NulsException(TxErrorCode.HEIGHT_UPDATE_UNABLE_TO_REPACKAGE);
            }

            //Add the orphan transaction back to the pending packaging queue
            putBackPackablePool(chain, orphanTxSet);
            if (chain.getProtocolUpgrade().get()) {
                chain.getCanProtocolUpgrade().set(false);
                nulsLogger.info("4_chain.getCanProtocolUpgrade().set(false);");
                //Protocol upgrade directly hits empty blocks,Retrieved transactions are placed in reverse order in the new transaction processing queue
                int size = packingTxList.size();
                for (int i = size - 1; i >= 0; i--) {
                    TxPackageWrapper txPackageWrapper = packingTxList.get(i);
                    Transaction tx = txPackageWrapper.getTx();
                    //Perform basic transaction verification
                    TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                    if (null == txRegister) {
                        throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
                    }
                    baseValidateTx(chain, tx, txRegister);
                    chain.getUnverifiedQueue().addLast(new TransactionNetPO(txPackageWrapper.getTx()));
                }
                TxPackage txPackage = new TxPackage(new ArrayList<>(), null, chain.getBestBlockHeight() + 1);
                chain.getCanProtocolUpgrade().set(true);
                nulsLogger.info("4_chain.getCanProtocolUpgrade().set(true);");
                return txPackage;
            }
            //Detect reserved transmission time
            long current = NulsDateUtils.getCurrentTimeMillis();
            if (endtimestamp - current < packageRpcReserveTime) {
                //overtime,Leave for final data assembly andRPCInsufficient transmission time
                nulsLogger.error("getPackableTxs time out, endtimestamp:{}, current:{}, endtimestamp-current:{}, reserveTime:{}",
                        endtimestamp, current, endtimestamp - current, packageRpcReserveTime);
                throw new NulsException(TxErrorCode.PACKAGE_TIME_OUT);
            }

            TxPackage txPackage = new TxPackage(packableTxs, stateRoot, blockHeight);

            long totalTime = NulsDateUtils.getCurrentTimeMillis() - startTime;
            nulsLogger.info("[Packaging time statistics]  Total execution time:{}, Remaining time:{}, Packaging available time:{}, Obtain transactions(loop)Total waiting time:{}, " +
                            "Obtain transactions(loop)execution time:{}, Obtain transactions(loop)Total time for verifying ledger:{}, Module unified verification execution time:{}, " +
                            "Contract execution time:{},", totalTime, endtimestamp - NulsDateUtils.getCurrentTimeMillis(),
                    packingTime, allSleepTime, whileTime, totalLedgerTime, batchModuleTime,
                    contractTime);

            nulsLogger.info("[Package end] - height:{} - The number of packaged transactions this time:{} - Current queue transactions to be packagedhashnumber:{}, - Actual number of transactions in the queue to be packaged:{}" + TxUtil.nextLine(),
                    blockHeight, packableTxs.size(), packablePool.packableHashQueueSize(chain), packablePool.packableTxMapSize(chain));

            return txPackage;
        } catch (Exception e) {
            nulsLogger.error(e);
            //Packable transactions,Orphan Trading,Add it all back
            putBackPackablePool(chain, packingTxList, orphanTxSet);
            return new TxPackage(new ArrayList<>(), null, chain.getBestBlockHeight() + 1);
        } finally {
            chain.getPackageLock().unlock();
        }
    }

    @Override
    public Map<String, Object> batchVerifyV8(Chain chain, List<String> txStrList, BlockHeader blockHeader, String blockHeaderStr, String preStateRoot) throws NulsException {
        NulsLogger logger = chain.getLogger();
        long s1 = NulsDateUtils.getCurrentTimeMillis();
        long blockHeight = blockHeader.getHeight();
        logger.info("[Verify block transactions] start -----height:{} -----Number of block transactions:{}", blockHeight, txStrList.size());
        List<TxVerifyWrapper> txList = new ArrayList<>();
        //Only one transaction is allowed in the verification block, and there cannot be multiple transactions
        Set<Integer> onlyOneTxTypes = new HashSet<>();
        //Smart contract notification identifier,When the first smart contract transaction occurs and the validator is called to pass,If there is, only notify on the first attempt.
        boolean contractNotify = false;
        Transaction scReturnGas = null;
        long blockTime = blockHeader.getTime();
        List<Future<Boolean>> futures = new ArrayList<>();
        //Assemble unified validation parameter data,keyUnify validators for each modulecmd
        Map<String, List<String>> moduleVerifyMap = new HashMap<>(TxConstant.INIT_CAPACITY_8);
        int chainId = chain.getChainId();
        long timeF1;
        long timeF2;
        long timeF3;
        long timeF4;
        List<byte[]> keys = new ArrayList<>();
        long f1 = System.currentTimeMillis();
        long totalGasInBlock = 0;
        List<String> contractGenerateTxs = new ArrayList<>();

        for (String txStr : txStrList) {
            Transaction tx = TxUtil.getInstanceRpcStr(txStr, Transaction.class);
            txList.add(new TxVerifyWrapper(tx, txStr));
            int type = tx.getType();
            verifySysTxCount(onlyOneTxTypes, type);
            TxRegister txRegister = TxManager.getTxRegister(chain, type);
            if (null == txRegister) {
                throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
            }
            if (type == TxType.CONTRACT_RETURN_GAS) {
                //recordgasReturn transaction
                scReturnGas = tx;
            }
            boolean isSmartContractTx = TxManager.isUnSystemSmartContract(txRegister);
            // add by pierre at 2019-11-02 Cross chain transfer transaction sent to the smart contract module for parsing, is it a contract asset cross chain transfer Protocol upgrade required done
            if (ProtocolGroupManager.getCurrentVersion(chain.getChainId()) >= TxContext.UPDATE_VERSION_V250) {
                boolean isCrossTransferTx = TxType.CROSS_CHAIN == type;
                if (!isSmartContractTx && txConfig.isCollectedSmartContractModule()) {
                    isSmartContractTx = isCrossTransferTx;
                }
            }
            // end code by pierre
            /** Smart contracts*/
            if (isSmartContractTx) {
                // Check the blocks that have been usedGAS
                if (totalGasInBlock >= MAX_GAS_COST_IN_BLOCK) {
                    if (TxManager.isGasCostContractTransaction(type)) {
                        Log.error("verify block failed: Excess block gas limit of contract transaction detected.");
                        throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
                    }
                }
                /** Smart contracts appear,And the notification identifier isfalse,Then call the notification first */
                if (!contractNotify) {
                    String packingAddress = AddressTool.getStringAddressByBytes(blockHeader.getPackingAddress(chain.getChainId()));
                    ContractCall.contractBatchBegin(chain, blockHeight, blockTime, packingAddress, preStateRoot, 1);
                    contractNotify = true;
                }
                try {
                    // Calling and executing smart contracts
                    Map<String, Object> invokeContractRs = ContractCall.invokeContractV8(chain, RPCUtil.encode(tx.serialize()), 1, Constants.TIMEOUT_TIMEMILLIS * 20);
                    //boolean success = (boolean) invokeContractRs.get("success");
                    long gasUsed = Long.valueOf(invokeContractRs.get("gasUsed").toString());
                    List<String> contractTxList = (List<String>) invokeContractRs.get("txList");
                    totalGasInBlock += gasUsed;
                    if (contractTxList != null && !contractTxList.isEmpty()) {
                        contractGenerateTxs.addAll(contractTxList);
                    }
                } catch (IOException e) {
                    throw new NulsException(TxErrorCode.SERIALIZE_ERROR);
                }
            }
            if (chain.getContractGenerateTxTypes().contains(tx.getType())) {
                //Transactions generated by the contract module that should not be included in the block transaction list
                throw new NulsException(TxErrorCode.SYS_CONTRACT_TX_NON_CIRCULATING);
            }
            keys.add(tx.getHash().getBytes());
            //According to the unified validator name of the module, group all transactions and prepare for unified verification of each module
            TxUtil.moduleGroups(moduleVerifyMap, txRegister, txStr);
        }
        if (!contractNotify && null != scReturnGas) {
            throw new NulsException(TxErrorCode.EXIST_GAS_RETURN_WITHOUT_SC_RETURN);
        }

        long f2 = System.currentTimeMillis();
        timeF1 = f2 - f1;
        //Verify if the transaction has been confirmed
        List<byte[]> confirmedList = confirmedTxStorageService.getExistTxs(chainId, keys);
        if (!confirmedList.isEmpty()) {
            logger.error("There are confirmed transactions");
            try {
                for (byte[] cfmtx : confirmedList) {
                    logger.error("confirmed hash:{}", TxUtil.getTransaction(cfmtx).getHash().toHex());
                }
            } finally {
                logger.error("Show confirmed transaction deserialize fail");
                throw new NulsException(TxErrorCode.TX_CONFIRMED);
            }
        }
        long f3 = System.currentTimeMillis();
        timeF2 = f3 - f2;

        //Verify transactions that are not available locally
        List<String> unconfirmedList = unconfirmedTxStorageService.getExistKeysStr(chainId, keys);

        long f4 = System.currentTimeMillis();
        timeF3 = f4 - f3;

        Set<String> set = new HashSet<>();
        set.addAll(unconfirmedList);
        long d = 0L;
        for (TxVerifyWrapper txVerifyWrapper : txList) {
            Transaction tx = txVerifyWrapper.getTx();
            tx.setBlockHeight(blockHeight);
            //Being able to join indicates that there is no confirmation yet,Then it needs to be handled
            if (set.add(tx.getHash().toHex())) {
                long d1 = System.currentTimeMillis();
                //Perform basic validation without confirmation
                //Multi threaded processing of individual transactions
                Future<Boolean> res = verifySignExecutor.submit(new Callable<Boolean>() {
                    @Override
                    public Boolean call() {
                        try {
                            //Verify only the basic content of a single transaction(TXModule Local Validation)
                            TxRegister txRegister = TxManager.getTxRegister(chain, tx.getType());
                            if (null == txRegister) {
                                throw new NulsException(TxErrorCode.TX_TYPE_INVALID);
                            }
                            baseValidateTx(chain, tx, txRegister);
                        } catch (Exception e) {
                            logger.error("batchVerify failed, single tx verify failed. hash:{}, -type:{}", tx.getHash().toHex(), tx.getType());
                            logger.error(e);
                            return false;
                        }
                        return true;
                    }
                });
                futures.add(res);
                d += (System.currentTimeMillis() - d1);
            }
        }

        timeF4 = System.currentTimeMillis() - f4;
        logger.info("[Verify block transactions] Deserialization,contract,grouping:{} -Have you confirmed:{} -Is it in unconfirmed status:{}, -Single validation:{} -Single internal processing:{} -Total time:{}",
                timeF1, timeF2, timeF3, d, timeF4, NulsDateUtils.getCurrentTimeMillis() - s1);

        if (contractNotify) {
            ContractCall.contractBatchBeforeEnd(chain, blockHeight, 1);
        }

        long coinDataV = NulsDateUtils.getCurrentTimeMillis();
        //Ledger verification
        if (!LedgerCall.verifyBlockTxsCoinData(chain, txStrList, blockHeight)) {
            if (logger.isDebugEnabled()) {
                logger.debug("batch verifyCoinData failed.");
            }
            throw new NulsException(TxErrorCode.TX_LEDGER_VERIFY_FAIL);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[Verify block transactions] coinData -Time from the start of the method:{},-Verification time:{}",
                    NulsDateUtils.getCurrentTimeMillis() - s1, NulsDateUtils.getCurrentTimeMillis() - coinDataV);
        }

        //Module Unified Verifier
        long moduleV = NulsDateUtils.getCurrentTimeMillis();
        Iterator<Map.Entry<String, List<String>>> it = moduleVerifyMap.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, List<String>> entry = it.next();
            List<String> txHashList = TransactionCall.txModuleValidator(chain,
                    entry.getKey(), entry.getValue(), blockHeaderStr);
            if (txHashList != null && txHashList.size() > 0) {
                if (logger.isDebugEnabled()) {
                    logger.debug("batch module verify fail, module-code:{},  return count:{}", entry.getKey(), txHashList.size());
                }
                throw new NulsException(TxErrorCode.TX_VERIFY_FAIL);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("[Verify block transactions] Module Unified Verification Time:{}", NulsDateUtils.getCurrentTimeMillis() - moduleV);
            logger.debug("[Verify block transactions] Unified module verification -Time from the start of the method:{}", NulsDateUtils.getCurrentTimeMillis() - s1);
        }

        /** Smart contracts When the notification identifier istrue, This indicates that a smart contract has been called and executed*/
        String scStateRoot = preStateRoot;
        if (contractNotify) {
            Map<String, Object> map;
            try {
                map = ContractCall.contractBatchEnd(chain, blockHeight, Constants.TIMEOUT_TIMEMILLIS * 20);
            } catch (NulsException e) {
                logger.error(e);
                throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
            }
            scStateRoot = (String) map.get("stateRoot");
            List<String> returnGasTx = (List<String>) map.get("txList");
            contractGenerateTxs.addAll(returnGasTx);
            /**
             * 1.Consensus verification If there is any
             * 2.If only consensus transactions for smart contracts fail,isRollbackPackablePool=true
             * 3.If only other consensus transactions fail, delete them separately
             * 4.blend implement2.
             */
            List<String> scNewConsensusList = new ArrayList<>();
            List<String> scNewTokenCrossTransferList = new ArrayList<>();
            for (String scNewTx : contractGenerateTxs) {
                int scNewTxType = TxUtil.extractTxTypeFromTx(scNewTx);
                if (scNewTxType == TxType.CONTRACT_CREATE_AGENT
                        || scNewTxType == TxType.CONTRACT_DEPOSIT
                        || scNewTxType == TxType.CONTRACT_CANCEL_DEPOSIT
                        || scNewTxType == TxType.CONTRACT_STOP_AGENT) {
                    scNewConsensusList.add(scNewTx);
                } else if (scNewTxType == TxType.CONTRACT_TOKEN_CROSS_TRANSFER) {
                    scNewTokenCrossTransferList.add(scNewTx);
                }
            }
            if (!scNewConsensusList.isEmpty() || !scNewTokenCrossTransferList.isEmpty()) {
                //Collect consensus module/All transactions across chain modules, Add the newly generated smart contract consensus transaction and perform module unified verification again together
                List<String> consensusList = new ArrayList<>();
                List<String> crossTransferList = new ArrayList<>();
                int txType;
                for (TxVerifyWrapper txVerifyWrapper : txList) {
                    Transaction tx = txVerifyWrapper.getTx();
                    txType = tx.getType();
                    // The consensus transactions generated by smart contracts in the block are not added repeatedly
                    if (txType == TxType.CONTRACT_CREATE_AGENT
                            || txType == TxType.CONTRACT_DEPOSIT
                            || txType == TxType.CONTRACT_CANCEL_DEPOSIT
                            || txType == TxType.CONTRACT_STOP_AGENT) {
                        continue;
                    }
                    if (ModuleE.CS.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(tx.getType()))) {
                        consensusList.add(txVerifyWrapper.getTxStr());
                    }
                    if (ModuleE.CC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(tx.getType()))) {
                        crossTransferList.add(txVerifyWrapper.getTxStr());
                    }
                }
                consensusList.addAll(scNewConsensusList);
                crossTransferList.addAll(scNewTokenCrossTransferList);
                if (!consensusList.isEmpty()) {
                    boolean rsProcess = processContractTxs(chain, ResponseMessageProcessor.ROLE_MAPPING.get(ModuleE.CS.abbr), consensusList, null, true);
                    if (rsProcess) {
                        logger.error("contract tx consensus module verify fail.");
                        throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
                    }
                }
                if (!crossTransferList.isEmpty()) {
                    boolean rsProcess = processContractTxs(chain, ResponseMessageProcessor.ROLE_MAPPING.get(ModuleE.CC.abbr), crossTransferList, null, true);
                    if (rsProcess) {
                        logger.error("contract tx cross-chain module verify fail.");
                        throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
                    }
                }
            }
            //Verify smart contractsgasReturned transactionshex Is it correct.The transaction returned during packaging is added to the end of the block transaction queue
            int size = contractGenerateTxs.size();
            if (size > 0) {
                int txSize = txStrList.size();
                String scNewTxHex = null;
                int returnGasIndex = -1;
                for (int i = size - 1; i >= 0; i--) {
                    String hex = contractGenerateTxs.get(i);
                    int txType = TxUtil.extractTxTypeFromTx(hex);
                    if (txType == TxType.CONTRACT_RETURN_GAS) {
                        scNewTxHex = hex;
                        returnGasIndex = i;
                        break;
                    }
                }
                if (scNewTxHex != null) {
                    String receivedScNewTxHex = null;
                    boolean rs = false;
                    for (int i = txSize - 1; i >= 0; i--) {
                        String txHex = txStrList.get(i);
                        int txType = TxUtil.extractTxTypeFromTx(txHex);
                        if (txType == TxType.CONTRACT_RETURN_GAS) {
                            receivedScNewTxHex = txHex;
                            if (txHex.equals(scNewTxHex)) {
                                rs = true;
                            }
                            break;
                        }
                    }
                    if (!rs) {
                        logger.error("contract error.Contract generatedgasReturn transaction:{}, - Received contractgasReturn transaction：{}", scNewTxHex, receivedScNewTxHex);
                        throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
                    }
                    //Return smart contract transactions to blocks
                    if (returnGasIndex != -1) {
                        contractGenerateTxs.remove(returnGasIndex);
                    }
                } else {
                    if (null != scReturnGas) {
                        throw new NulsException(TxErrorCode.EXIST_GAS_RETURN_WITHOUT_SC_RETURN);
                    }
                }
            } else {
                if (null != scReturnGas) {
                    throw new NulsException(TxErrorCode.EXIST_GAS_RETURN_WITHOUT_SC_RETURN);
                }
            }
        }
        //stateRootSend to consensus,Compare after processing
        String coinBaseTx = null;
        for (TxVerifyWrapper txVerifyWrapper : txList) {
            Transaction tx = txVerifyWrapper.getTx();
            if (tx.getType() == TxType.COIN_BASE) {
                coinBaseTx = txVerifyWrapper.getTxStr();
                break;
            }
        }
        String stateRootNew = ConsensusCall.triggerCoinBaseContract(chain, coinBaseTx, blockHeaderStr, scStateRoot);
        String stateRoot = RPCUtil.encode(blockHeader.getExtendsData().getStateRoot());
        if (!stateRoot.equals(stateRootNew)) {
            logger.warn("contract stateRoot error.");
            throw new NulsException(TxErrorCode.CONTRACT_VERIFY_FAIL);
        }

        //Multithreaded processing results
        try {
            for (Future<Boolean> future : futures) {
                if (!future.get()) {
                    logger.error("batchVerify failed, single tx verify failed");
                    throw new NulsException(TxErrorCode.TX_VERIFY_FAIL);
                }
            }
        } catch (InterruptedException e) {
            logger.error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        } catch (ExecutionException e) {
            logger.error(e);
            throw new NulsException(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }

        if (logger.isDebugEnabled()) {
            logger.debug("[Verify block transactions] Total execution time:{}, - height:{} - Number of block transactions:{}" + TxUtil.nextLine(),
                    NulsDateUtils.getCurrentTimeMillis() - s1, blockHeight, txStrList.size());
        }
        Map<String, Object> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_4);
        resultMap.put("value", true);
        resultMap.put("contractList", contractGenerateTxs);
        return resultMap;
    }

    private Map processContractResultV8(Chain chain, List<TxPackageWrapper> packingTxList, Set<TxPackageWrapper> orphanTxSet, List<String> contractGenerateTxs, List<String> originTxList,
                                        long blockHeight, boolean contractBefore, String stateRoot) throws IOException {

        boolean hasTxbackPackablePool = false;
        /**WhencontractBeforeNotification failed,perhapscontractBatchEndIf it fails, the smart contract transaction needs to be returned to the waiting queue for packaging*/
        boolean isRollbackPackablePool = false;
        /**
         * When consensus transaction verification generated by smart contracts fails, The corresponding original transaction has a limited number of returns to the packaging queue
         * (Record the original transactions of contracts with a return limithash)
         */
        Set<String> setLimitedRollbackOriginTx = new HashSet<>();
        if (!contractBefore) {
            isRollbackPackablePool = true;
        } else {
            try {
                Map<String, Object> map = ContractCall.contractPackageBatchEnd(chain, blockHeight);
                List<String> returnGasTx = (List<String>) map.get("txList");
                contractGenerateTxs.addAll(returnGasTx);
                if (null != contractGenerateTxs) {
                    /**
                     * 1.Consensus verification If there is any
                     * 2.If only consensus transactions for smart contracts fail,isRollbackPackablePool=true
                     * 3.If only other consensus transactions fail, delete them separately
                     * 4.blend implement2.
                     */
                    List<String> scNewConsensusList = new ArrayList<>();
                    List<String> scNewTokenCrossTransferList = new ArrayList<>();
//                    for (String scNewTx : scNewList) {
                    for (int i = 0; i < contractGenerateTxs.size(); i++) {
                        String scNewTx = contractGenerateTxs.get(i);
                        int scNewTxType = TxUtil.extractTxTypeFromTx(scNewTx);
                        if (scNewTxType == TxType.CONTRACT_CREATE_AGENT
                                || scNewTxType == TxType.CONTRACT_DEPOSIT
                                || scNewTxType == TxType.CONTRACT_CANCEL_DEPOSIT
                                || scNewTxType == TxType.CONTRACT_STOP_AGENT) {
                            scNewConsensusList.add(scNewTx);
                            setLimitedRollbackOriginTx.add(originTxList.get(i));
                        } else if (scNewTxType == TxType.CONTRACT_TOKEN_CROSS_TRANSFER) {
                            scNewTokenCrossTransferList.add(scNewTx);
                        }
                    }
                    if (!scNewConsensusList.isEmpty() || !scNewTokenCrossTransferList.isEmpty()) {
                        //Collect consensus module/All transactions across chain modules, Add the newly generated smart contract consensus transaction and perform module unified verification again together
                        List<String> consensusList = new ArrayList<>();
                        List<String> crossTransferList = new ArrayList<>();
                        for (TxPackageWrapper txPackageWrapper : packingTxList) {
                            Transaction tx = txPackageWrapper.getTx();
                            if (ModuleE.CS.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(tx.getType()))) {
                                consensusList.add(RPCUtil.encode(txPackageWrapper.getTx().serialize()));
                            }
                            if (ModuleE.CC.abbr.equals(ResponseMessageProcessor.TX_TYPE_MODULE_MAP.get(tx.getType()))) {
                                crossTransferList.add(RPCUtil.encode(txPackageWrapper.getTx().serialize()));
                            }
                        }
                        consensusList.addAll(scNewConsensusList);
                        crossTransferList.addAll(scNewTokenCrossTransferList);
                        if (!consensusList.isEmpty()) {
                            isRollbackPackablePool = processContractTxs(chain, ResponseMessageProcessor.ROLE_MAPPING.get(ModuleE.CS.abbr), consensusList, packingTxList, false);
                        }
                        if (!isRollbackPackablePool && !crossTransferList.isEmpty()) {
                            isRollbackPackablePool = processContractTxs(chain, ResponseMessageProcessor.ROLE_MAPPING.get(ModuleE.CC.abbr), crossTransferList, packingTxList, false);
                        }
                    }
                    if (!isRollbackPackablePool) {
                        // Contract consensus There are no failed transactions across contract chains Then obtain and use the newstateRoot
                        String sr = (String) map.get("stateRoot");
                        if (null != sr) {
                            stateRoot = sr;
                        }
                    }
                }
            } catch (NulsException e) {
                chain.getLogger().error(e);
                isRollbackPackablePool = true;
            }
        }
        // Determine if the smart contract needs to be added back to the queue for packaging
        if (isRollbackPackablePool) {
            Iterator<TxPackageWrapper> iterator = packingTxList.iterator();
            while (iterator.hasNext()) {
                TxPackageWrapper txPackageWrapper = iterator.next();
                if (TxManager.isUnSystemSmartContract(chain, txPackageWrapper.getTx().getType())) {
                    if (setLimitedRollbackOriginTx.contains(txPackageWrapper.getTx().getHash().toHex())) {
                        // Transactions with a limit on the number of times they can be added back
                        addOrphanTxSet(chain, orphanTxSet, txPackageWrapper);
                    } else {
                        // Without a limit on the number of times to add back, Directly join the set,Can share a set with orphan transactions
                        orphanTxSet.add(txPackageWrapper);
                    }
                    //Remove from packable collection
                    iterator.remove();
                    if (!hasTxbackPackablePool) {
                        hasTxbackPackablePool = true;
                    }
                }
            }
        }
        Map rs = new HashMap();
        rs.put("stateRoot", stateRoot);
        rs.put("hasTxbackPackablePool", hasTxbackPackablePool);
        return rs;
    }

}
