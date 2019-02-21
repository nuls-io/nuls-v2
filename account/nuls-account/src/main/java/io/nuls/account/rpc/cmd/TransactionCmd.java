package io.nuls.account.rpc.cmd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.bo.tx.txdata.Alias;
import io.nuls.account.model.dto.CoinDto;
import io.nuls.account.model.dto.MultiSignTransactionResultDto;
import io.nuls.account.model.dto.TransferDto;
import io.nuls.account.model.po.AliasPo;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.service.MultiSignAccountService;
import io.nuls.account.service.TransactionService;
import io.nuls.account.storage.AliasStorageService;
import io.nuls.account.util.TxUtil;
import io.nuls.account.util.annotation.ResisterTx;
import io.nuls.account.util.annotation.TxMethodType;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.tools.log.Log;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.account.util.validator.TxValidator;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.JSONUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/5
 */
@Component
public class TransactionCmd extends BaseCmd {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxValidator txValidator;
    @Autowired
    private AliasStorageService aliasStorageService;
    @Autowired
    private AccountService accountService;
    @Autowired
    private MultiSignAccountService multiSignAccountService;
    @Autowired
    private AliasService aliasService;

    /**
     * validate the transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_accountTxValidate", version = 1.0, description = "validate the transaction")
    public Response accountTxValidate(Map params) {
        Log.debug("ac_accountTxValidate start,params size:{}", params == null ? 0 : params.size());
        int chainId = 0;
        List<String> txHexList;
        List<Transaction> lists = new ArrayList<>();
        List<Transaction> result = null;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txHexListObj = params == null ? null : params.get(RpcParameterNameConstant.TX_HEX_LIST);
        try {
            // check parameters
            if (params == null || chainIdObj == null || txHexListObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            txHexList = (List<String>) txHexListObj;
            //TODO after the parameter format was determine,here will be modify
            if (txHexList != null) {
                txHexList.forEach(txHex -> {
                    try {
                        lists.add(Transaction.getInstance(txHex));
                    } catch (NulsException e) {
                        e.printStackTrace();
                    }
                });
                result = transactionService.accountTxValidate(chainId, lists);
            }
        } catch (NulsRuntimeException e) {
            Log.error("", e);
            return failed(e.getErrorCode());
        } catch (NulsException e) {
            Log.error("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            Log.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, List<Transaction>> resultMap = new HashMap<>();
        resultMap.put("list", result);
        Log.debug("ac_accountTxValidate end");
        return success(resultMap);
    }

    /**
     * batch commit the transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_commitTx", version = 1.0, description = "batch commit the transaction")
    public Response commitTx(Map params) {
        Log.debug("ac_commitTx start,params size:{}", params == null ? 0 : params.size());
        boolean result = true;
        int chainId;
        List<String> txHexList;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txHexListgObj = params == null ? null : params.get(RpcParameterNameConstant.TX_HEX_LIST);
        Object blockHeaderDigest = params == null ? null : params.get(RpcParameterNameConstant.BLOCK_HEADER_DIGEST);
        List<Transaction> commitSucTxList = new ArrayList<>();
        // check parameters
        if (params == null || chainIdObj == null || txHexListgObj == null) {
            return failed(AccountErrorCode.NULL_PARAMETER);
        }
        chainId = (Integer) chainIdObj;
        txHexList = (List<String>) txHexListgObj;
        //交易提交
        try {
            for (String txHex : txHexList) {
                Transaction tx = Transaction.getInstance(txHex);
                //别名交易
                if (AccountConstant.TX_TYPE_ACCOUNT_ALIAS == tx.getType()) {
                    Alias alias = new Alias();
                    alias.parse(new NulsByteBuffer(tx.getTxData()));
                    result = aliasService.aliasTxCommit(chainId, alias);
                    if (!result) {
                        break;
                    }
                    commitSucTxList.add(tx);
                }
            }
        } catch (NulsException e) {
            Log.info("", e);
            result = false;
        } catch (Exception e) {
            Log.error("", e);
            result = false;
        }
        //交易回滚
        try {
            //如果提交失败，将已经提交成功的交易回滚
            if (!result) {
                boolean rollback = true;
                for (Transaction tx : commitSucTxList) {
                    Alias alias = new Alias();
                    alias.parse(new NulsByteBuffer(tx.getTxData()));
                    rollback = aliasService.rollbackAlias(chainId, alias);
                }
                //回滚失败，抛异常
                if (!rollback) {
                    throw new NulsException(AccountErrorCode.ALIAS_ROLLBACK_ERROR);
                }
            }
        } catch (NulsException e) {
            Log.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            Log.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }

        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        Log.debug("ac_commitTx end");
        return success(resultMap);
    }

    /**
     * batch rollback the transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_rollbackTx", version = 1.0, description = "batch rollback the transaction")
    public Response rollbackTx(Map params) {
        Log.debug("ac_rollbackTx start,params size:{}", params == null ? 0 : params.size());

        boolean result = false;
        int chainId;
        List<String> txHexList;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txHexListgObj = params == null ? null : params.get(RpcParameterNameConstant.TX_HEX_LIST);
        Object blockHeaderDigest = params == null ? null : params.get(RpcParameterNameConstant.BLOCK_HEADER_DIGEST);
        List<Transaction> rollbackSucTxList = new ArrayList<>();
        // check parameters
        if (params == null || chainIdObj == null || txHexListgObj == null) {
            return failed(AccountErrorCode.NULL_PARAMETER);
        }
        chainId = (Integer) chainIdObj;
        txHexList = (List<String>) txHexListgObj;
        //交易回滚
        try {
            for (String txHex : txHexList) {
                Transaction tx = Transaction.getInstance(txHex);
                //别名交易
                if (AccountConstant.TX_TYPE_ACCOUNT_ALIAS == tx.getType()) {
                    Alias alias = new Alias();
                    alias.parse(new NulsByteBuffer(tx.getTxData()));
                    result = aliasService.rollbackAlias(chainId, alias);
                    if (!result) {
                        break;
                    }
                    rollbackSucTxList.add(tx);
                }
            }
        } catch (NulsException e) {
            Log.info("", e);
            result = false;
        } catch (Exception e) {
            Log.error("", e);
            result = false;
        }
        //交易提交
        try {
            //如果回滚失败，将已经回滚成功的交易重新保存
            if (!result) {
                boolean commit = true;
                for (Transaction tx : rollbackSucTxList) {
                    Alias alias = new Alias();
                    alias.parse(new NulsByteBuffer(tx.getTxData()));
                    commit = aliasService.aliasTxCommit(chainId, alias);
                }
                //保存失败，抛异常
                if (!commit) {
                    throw new NulsException(AccountErrorCode.ALIAS_SAVE_ERROR);
                }
            }
        } catch (NulsException e) {
            Log.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            Log.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }

        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        Log.debug("ac_rollbackTx end");
        return success(resultMap);
    }


    /**
     * 转账交易验证
     */
    @CmdAnnotation(cmd = "ac_transferTxValidate", version = 1.0, description = "create transfer transaction validate 1.0")
    @ResisterTx(txType = AccountConstant.TX_TYPE_TRANSFER, methodType = TxMethodType.VALID, methodName = "ac_transferTxValidate")
    @Parameter(parameterName = RpcParameterNameConstant.CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = RpcParameterNameConstant.TX_HEX, parameterType = "String")
    public Response transferTxValidate(Map<String, Object> params) {
        Log.debug("ac_transferTxValidate start");
        Map<String, Boolean> resultMap = new HashMap<>();
        boolean result;
        try {
            if (params.get(RpcParameterNameConstant.CHAIN_ID) == null || params.get(RpcParameterNameConstant.TX_HEX) == null) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            int chainId = (Integer) params.get(RpcParameterNameConstant.CHAIN_ID);
            String txHex = (String) params.get(RpcParameterNameConstant.TX_HEX);
            if (chainId <= 0) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            Transaction transaction = TxUtil.getTransaction(txHex);
            result = txValidator.validateTx(chainId, transaction);
        } catch (NulsException e) {
            Log.warn("", e);
            result = false;
        } catch (Exception e) {
            Log.error("", e);
            result = false;
        }

        resultMap.put("value", result);
        Log.debug("ac_transferTxValidate end");
        return success(resultMap);
    }

    /**
     * 转账交易提交
     */
    @CmdAnnotation(cmd = "ac_transferTxCommit", version = 1.0, description = "create transfer transaction commit 1.0")
    //@ResisterTx(txType = AccountConstant.TX_TYPE_TRANSFER, methodType = TxMethodType.COMMIT, methodName = "ac_transferTxCommit")
    public Response transferTxCommit(Map<String, Object> params) {
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", true);
        return success(resultMap);
    }

    /**
     * 转账交易回滚
     */
    @CmdAnnotation(cmd = "ac_transferTxRollback", version = 1.0, description = "create transfer transaction rollback 1.0")
    //@ResisterTx(txType = AccountConstant.TX_TYPE_TRANSFER, methodType = TxMethodType.ROLLBACK, methodName = "ac_transferTxRollback")
    public Response transferTxRollback(Map<String, Object> params) {
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", true);
        return success(resultMap);
    }

    /**
     * 创建多账户转账交易
     * create a multi-account transfer transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_transfer", version = 1.0, description = "create a multi-account transfer transaction")
    public Response transfer(Map params) {
        Log.debug("ac_transfer start");
        Map<String, String> map = new HashMap<>(1);
        try {
            // check parameters
            if (params == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // parse params
            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            TransferDto transferDto = JSONUtils.json2pojo(JSONUtils.obj2json(params), TransferDto.class);
            List<CoinDto> inputList = transferDto.getInputs();
            List<CoinDto> outputList = transferDto.getOutputs();
            if (inputList == null || outputList == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // check address validity
            BigInteger fromTotal = BigInteger.ZERO;
            for (CoinDto from : inputList) {
                if (!AddressTool.validAddress(from.getAssetsChainId(), from.getAddress())) {
                    throw new NulsException(AccountErrorCode.ADDRESS_ERROR);
                }
                fromTotal = fromTotal.add(from.getAmount());
            }
            BigInteger toTotal = BigInteger.ZERO;
            for (CoinDto to : outputList) {
                if (!AddressTool.validAddress(to.getAssetsChainId(), to.getAddress())) {
                    throw new NulsException(AccountErrorCode.ADDRESS_ERROR);
                }
                toTotal = toTotal.add(to.getAmount());
            }

            // check transfer amount
            if (BigIntegerUtils.isLessThan(fromTotal, BigInteger.ZERO) || BigIntegerUtils.isLessThan(toTotal, BigInteger.ZERO)) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            // check transaction remark
            if (!validTxRemark(transferDto.getRemark())) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            String txDigestHex = transactionService.transfer(transferDto.getChainId(), inputList, outputList, transferDto.getRemark());
            map.put("value", txDigestHex);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (IOException e) {
            return failed(e.getMessage());
        } catch (Exception e) {
            return failed(e.getMessage());
        }
        Log.debug("ac_transfer end");
        return success(map);
    }

    /**
     * 创建别名转账交易,仅仅针对非多签账户
     * <p>
     * create the transaction of transfer by alias
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_transferByAlias", version = 1.0, description = "transfer by alias")
    public Response transferByAlias(Map params) {
        Log.debug("ac_transferByAlias start");
        Map<String, String> map = new HashMap<>(1);
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
        Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
        Object aliasObj = params == null ? null : params.get(RpcParameterNameConstant.ALIAS);
        Object amountObj = params == null ? null : params.get(RpcParameterNameConstant.AMOUNT);
        Object remarkObj = params == null ? null : params.get(RpcParameterNameConstant.REMARK);
        try {
            // check parameters
            if (params == null || chainIdObj == null || addressObj == null || passwordObj == null || aliasObj == null ||
                    amountObj == null || remarkObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            int chainId = (int) chainIdObj;
            String address = (String) addressObj;
            String password = (String) passwordObj;
            String alias = (String) aliasObj;
            BigInteger amount = new BigInteger((String) amountObj);
            String remark = (String) remarkObj;
            if (BigIntegerUtils.isLessThan(amount, BigInteger.ZERO)) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // check transaction remark
            if (!validTxRemark(remark)) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            //根据别名查询出地址
            AliasPo aliasPo = aliasStorageService.getAlias(chainId, alias);
            if (aliasPo == null) {
                throw new NulsRuntimeException(AccountErrorCode.ALIAS_NOT_EXIST);
            }
            Chain chain = chainManager.getChainMap().get(chainId);
            if (chain == null) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            int assetId = chain.getConfig().getAssetsId();
            CoinDto fromCoinDto = new CoinDto();
            CoinDto toCoinDto = new CoinDto();
            fromCoinDto.setAddress(address);
            fromCoinDto.setAmount(amount);
            fromCoinDto.setAssetsChainId(chainId);
            fromCoinDto.setAssetsId(assetId);
            fromCoinDto.setPassword(password);

            toCoinDto.setAddress(AddressTool.getStringAddressByBytes(aliasPo.getAddress()));
            toCoinDto.setAmount(amount);
            toCoinDto.setAssetsChainId(chainId);
            toCoinDto.setAssetsId(assetId);
            Transaction tx = transactionService.transferByAlias(chainId, fromCoinDto, toCoinDto, remark);
            map.put("txHash", tx.getHash().getDigestHex());
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(e.getMessage());
        }
        Log.debug("ac_transferByAlias end");
        return success(map);
    }

    /**
     * 创建多签转账交易
     * <p>
     * create the multi sign transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_createMultiSignTransfer", version = 1.0, description = "create multi sign transfer")
    public Response createMultiSignTransfer(Map params) {
        Log.debug("ac_createMultiSignTransfer start");
        Map<String, String> map = new HashMap<>(1);
        MultiSigAccount multiSigAccount = null;

        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object assetsIdObj = params == null ? null : params.get(RpcParameterNameConstant.ASSETS_Id);
        Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
        Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
        Object signAddressObj = params == null ? null : params.get(RpcParameterNameConstant.SIGN_ADDREESS);
        Object typeObj = params == null ? null : params.get(RpcParameterNameConstant.TYPE);
        Object aliasObj = params == null ? null : params.get(RpcParameterNameConstant.ALIAS);
        Object toAddressObj = params == null ? null : params.get(RpcParameterNameConstant.TO_ADDRESS);
        Object amountObj = params == null ? null : params.get(RpcParameterNameConstant.AMOUNT);
        Object remarkObj = params == null ? null : params.get(RpcParameterNameConstant.REMARK);
        try {
            // check parameters
            if (params == null || chainIdObj == null || addressObj == null || signAddressObj == null ||
                    amountObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            int chainId = (int) chainIdObj;
            int assetsId;
            if (assetsIdObj == null) {
                assetsId = -1;
            } else {
                assetsId = (int) assetsIdObj;
            }
            String address = (String) addressObj;
            String password = (String) passwordObj;
            String signAddress = (String) signAddressObj;
            int type = (int) typeObj;
            String alias = (String) aliasObj;
            String toAddress = (String) toAddressObj;
            if (type == 1) {
                if (toAddress == null) {
                    throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
                }
            } else if (type == 2) {
                if (alias == null) {
                    throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
                }
                AliasPo aliasPo = aliasStorageService.getAlias(chainId, alias);
                if (aliasPo == null) {
                    throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
                }
                toAddress = AddressTool.getStringAddressByBytes(aliasPo.getAddress());
            } else {
                throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
            }
            multiSigAccount = multiSignAccountService.getMultiSigAccountByAddress(chainId, address);
            if (multiSigAccount == null) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
            BigInteger amount = new BigInteger((String) amountObj);
            String remark = (String) remarkObj;
            if (BigIntegerUtils.isLessThan(amount, BigInteger.ZERO)) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // check transaction remark
            if (!validTxRemark(remark)) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            //查询出账户
            Account account = accountService.getAccount(chainId, signAddress);
            if (account == null) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }

            //验证签名账户是否属于多签账户的签名账户,如果不是多签账户下的地址则提示错误
            if (!AddressTool.validSignAddress(multiSigAccount.getPubKeyList(), account.getPubKey())) {
                throw new NulsRuntimeException(AccountErrorCode.SIGN_ADDRESS_NOT_MATCH);
            }
            Chain chain = chainManager.getChainMap().get(chainId);
            if (chain == null) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            MultiSignTransactionResultDto multiSignTransactionResultDto = transactionService.createMultiSignTransfer(chainId, assetsId, account, password, multiSigAccount, toAddress, amount, remark);
            if (multiSignTransactionResultDto.isBroadcasted()) {
                map.put("txHash", multiSignTransactionResultDto.getTransaction().getHash().getDigestHex());
            } else {
                map.put("txHex", HexUtil.encode(multiSignTransactionResultDto.getTransaction().serialize()));
            }
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(e.getMessage());
        }
        Log.debug("ac_createMultiSignTransfer end");
        return success(map);
    }

    /**
     * 多签交易签名
     * <p>
     * sign MultiSign Transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_signMultiSignTransaction", version = 1.0, description = "sign MultiSign Transaction")
    public Response signMultiSignTransaction(Map params) {
        Log.debug("ac_signMultiSignTransaction start");
        Map<String, String> map = new HashMap<>(1);
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
        Object signAddressObj = params == null ? null : params.get(RpcParameterNameConstant.SIGN_ADDREESS);
        Object txHexObj = params == null ? null : params.get(RpcParameterNameConstant.TX_HEX);
        try {
            // check parameters
            if (params == null || chainIdObj == null || signAddressObj == null ||
                    txHexObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            int chainId = (int) chainIdObj;
            String password = (String) passwordObj;
            String signAddress = (String) signAddressObj;
            String txHex = (String) txHexObj;
            //查询出账户
            Account account = accountService.getAccount(chainId, signAddress);
            if (account == null) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
            MultiSignTransactionResultDto multiSignTransactionResultDto = transactionService.signMultiSignTransaction(chainId, account, password, txHex);
            if (multiSignTransactionResultDto.isBroadcasted()) {
                map.put("txHash", multiSignTransactionResultDto.getTransaction().getHash().getDigestHex());
            } else {
                map.put("txHex", HexUtil.encode(multiSignTransactionResultDto.getTransaction().serialize()));
            }
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(e.getMessage());
        }
        Log.debug("ac_signMultiSignTransaction end");
        return success(map);
    }


    /**
     * 校验转账交易备注是否有效
     *
     * @param remark
     * @return
     */
    private boolean validTxRemark(String remark) {
        if (StringUtils.isBlank(remark)) {
            return true;
        }
        try {
            byte[] bytes = remark.getBytes(NulsConfig.DEFAULT_ENCODING);
            if (bytes.length > 100) {
                return false;
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }
}
