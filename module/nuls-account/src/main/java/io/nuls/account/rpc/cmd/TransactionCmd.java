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
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.Preconditions;
import io.nuls.account.util.TxUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.account.util.validator.TxValidator;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.data.Transaction;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
        int chainId = 0;
        List<String> txList;
        List<Transaction> lists = new ArrayList<>();
        List<Transaction> result = null;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txListObj = params == null ? null : params.get(RpcParameterNameConstant.TX_LIST);
        try {
            // check parameters
            if (params == null || chainIdObj == null || txListObj == null) {
                LoggerUtil.logger.warn("ac_accountTxValidate params is null");
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            txList = (List<String>) txListObj;
            if (txList != null) {
                txList.forEach(tx -> {
                    try {
                        lists.add(TxUtil.getInstanceRpcStr(tx, Transaction.class));
                    } catch (NulsException e) {
                        LoggerUtil.logger.error("ac_accountTxValidate tx format error", e);
                    }
                });
                result = transactionService.accountTxValidate(chainId, lists);
            }
        } catch (NulsRuntimeException e) {
            LoggerUtil.logger.error("", e);
            return failed(e.getErrorCode());
        } catch (NulsException e) {
            LoggerUtil.logger.error("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, List<Transaction>> resultMap = new HashMap<>();
        resultMap.put("list", result);
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
        boolean result = true;
        int chainId;
        List<String> txList;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txListgObj = params == null ? null : params.get(RpcParameterNameConstant.TX_LIST);
        Object blockHeaderDigest = params == null ? null : params.get(RpcParameterNameConstant.BLOCK_HEADER_DIGEST);
        List<Transaction> commitSucTxList = new ArrayList<>();
        // check parameters
        if (params == null || chainIdObj == null || txListgObj == null) {
            LoggerUtil.logger.warn("ac_commitTx params is null");
            return failed(AccountErrorCode.NULL_PARAMETER);
        }
        chainId = (Integer) chainIdObj;
        txList = (List<String>) txListgObj;
        //交易提交
        try {
            for (String txStr : txList) {
                Transaction tx = TxUtil.getInstanceRpcStr(txStr, Transaction.class);
                //别名交易
                if (TxType.ACCOUNT_ALIAS == tx.getType()) {
                    Alias alias = new Alias();
                    alias.parse(new NulsByteBuffer(tx.getTxData()));
                    result = aliasService.aliasTxCommit(chainId, alias);
                    if (!result) {
                        LoggerUtil.logger.warn("ac_commitTx alias tx commit error");
                        break;
                    }
                    commitSucTxList.add(tx);
                }
            }
        } catch (NulsException e) {
            LoggerUtil.logger.info("", e);
            result = false;
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
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
                    LoggerUtil.logger.error("ac_commitTx alias tx rollback error");
                    throw new NulsException(AccountErrorCode.ALIAS_ROLLBACK_ERROR);
                }
            }
        } catch (NulsException e) {
            LoggerUtil.logger.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }

        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
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
        //默认回滚成功
        boolean result = true;
        int chainId;
        List<String> txList;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txListgObj = params == null ? null : params.get(RpcParameterNameConstant.TX_LIST);
        Object blockHeaderDigest = params == null ? null : params.get(RpcParameterNameConstant.BLOCK_HEADER_DIGEST);
        List<Transaction> rollbackSucTxList = new ArrayList<>();
        // check parameters
        if (params == null || chainIdObj == null || txListgObj == null) {
            LoggerUtil.logger.warn("ac_rollbackTx params is null");
            return failed(AccountErrorCode.NULL_PARAMETER);
        }
        chainId = (Integer) chainIdObj;
        txList = (List<String>) txListgObj;
        //交易回滚
        try {
            for (String txStr : txList) {
                Transaction tx = TxUtil.getInstanceRpcStr(txStr, Transaction.class);
                //别名交易
                if (TxType.ACCOUNT_ALIAS == tx.getType()) {
                    Alias alias = new Alias();
                    alias.parse(new NulsByteBuffer(tx.getTxData()));
                    result = aliasService.rollbackAlias(chainId, alias);
                    if (!result) {
                        LoggerUtil.logger.warn("ac_rollbackTx alias tx rollback error");
                        break;
                    }
                    rollbackSucTxList.add(tx);
                }
            }
        } catch (NulsException e) {
            LoggerUtil.logger.info("", e);
            result = false;
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
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
                    LoggerUtil.logger.error("ac_rollbackTx alias tx commit error");
                    throw new NulsException(AccountErrorCode.ALIAS_SAVE_ERROR);
                }
            }
        } catch (NulsException e) {
            LoggerUtil.logger.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LoggerUtil.logger.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }

        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
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
        Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        try {
            // check parameters
            if (params == null) {
                LoggerUtil.logger.warn("ac_transfer params is null");
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // parse params
            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            TransferDto transferDto = JSONUtils.map2pojo(params, TransferDto.class);

            Function<CoinDto, CoinDto> checkAddress = cd -> {
                //如果address 不是地址就当别名处理
                if (!AddressTool.validAddress(transferDto.getChainId(), cd.getAddress())) {
                    AliasPo aliasPo = aliasStorageService.getAlias(transferDto.getChainId(), cd.getAddress());
                    Preconditions.checkNotNull(aliasPo, AccountErrorCode.ALIAS_NOT_EXIST);
                    cd.setAddress(AddressTool.getStringAddressByBytes(aliasPo.getAddress()));
                }
                return cd;
            };
            List<CoinDto> inputList = transferDto.getInputs().stream().map(checkAddress).collect(Collectors.toList());
            List<CoinDto> outputList = transferDto.getOutputs().stream().map(checkAddress).collect(Collectors.toList());

            if (inputList == null || outputList == null) {
                LoggerUtil.logger.warn("ac_transfer params input or output is null");
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            for (CoinDto from : inputList) {
                //from中不能有多签地址
                if (AddressTool.isMultiSignAddress(from.getAddress())) {
                    throw new NulsException(AccountErrorCode.IS_MULTI_SIGNATURE_ADDRESS);
                }
            }

            if (!validTxRemark(transferDto.getRemark())) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            Transaction tx = transactionService.transfer(transferDto.getChainId(), inputList, outputList, transferDto.getRemark());
            map.put("value", tx.getHash().toHex());
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(e.getMessage());
        }
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
    @Deprecated(since = "此方法废弃，请使用transfer方法，该方法可接受别名转账")
    @CmdAnnotation(cmd = "ac_transferByAlias", version = 1.0, description = "transfer by alias")
    public Response transferByAlias(Map params) {
        Map<String, String> map = new HashMap<>(1);
        try {
            Preconditions.checkNotNull(params, AccountErrorCode.NULL_PARAMETER);
            Object chainIdObj = params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params.get(RpcParameterNameConstant.PASSWORD);
            Object aliasObj = params.get(RpcParameterNameConstant.ALIAS);
            Object amountObj = params.get(RpcParameterNameConstant.AMOUNT);
            Object remarkObj = params.get(RpcParameterNameConstant.REMARK);
            // check parameters
            Preconditions.checkNotNull(new Object[]{chainIdObj, addressObj, passwordObj, aliasObj, amountObj}, AccountErrorCode.NULL_PARAMETER);
            int chainId = (int) chainIdObj;
            String address = (String) addressObj;
            String password = (String) passwordObj;
            String alias = (String) aliasObj;
            BigInteger amount = new BigInteger(String.valueOf(amountObj));
            String remark = (String) remarkObj;
            if (BigIntegerUtils.isLessThan(amount, BigInteger.ZERO)) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // check transaction remark
            if (!validTxRemark(remark)) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            //根据别名查询出地址
            AliasPo toAddressAliasPo = aliasStorageService.getAlias(chainId, alias);
            Preconditions.checkNotNull(toAddressAliasPo, AccountErrorCode.ALIAS_NOT_EXIST);
            AliasPo formAddressAliasPo = aliasStorageService.getAlias(chainId, alias);
            Preconditions.checkNotNull(formAddressAliasPo, AccountErrorCode.ALIAS_NOT_EXIST);
            Chain chain = chainManager.getChainMap().get(chainId);
            Preconditions.checkNotNull(chain, AccountErrorCode.CHAIN_NOT_EXIST);
            int assetId = chain.getConfig().getAssetsId();
            CoinDto fromCoinDto = new CoinDto(AddressTool.getStringAddressByBytes(formAddressAliasPo.getAddress()), chainId, assetId, amount, password);
            CoinDto toCoinDto = new CoinDto(AddressTool.getStringAddressByBytes(toAddressAliasPo.getAddress()), chainId, assetId, amount, null);
            Transaction tx = transactionService.transferByAlias(chainId, fromCoinDto, toCoinDto, remark);
            map.put("txHash", tx.getHash().toHex());
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(e.getMessage());
        }
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
        Map<String, String> map = new HashMap<>(1);
        MultiSigAccount multiSigAccount = null;
        try {
            Preconditions.checkNotNull(params, AccountErrorCode.NULL_PARAMETER);
            Object chainIdObj = params.get(RpcParameterNameConstant.CHAIN_ID);
            Object assetsIdObj = params.get(RpcParameterNameConstant.ASSETS_ID);
            Object addressObj = params.get(RpcParameterNameConstant.ADDRESS);
            Object passwordObj = params.get(RpcParameterNameConstant.PASSWORD);
            Object signAddressObj = params.get(RpcParameterNameConstant.SIGN_ADDREESS);
            Object typeObj = params.get(RpcParameterNameConstant.TYPE);
            Object aliasObj = params.get(RpcParameterNameConstant.ALIAS);
            Object toAddressObj = params.get(RpcParameterNameConstant.TO_ADDRESS);
            Object amountObj = params.get(RpcParameterNameConstant.AMOUNT);
            Object remarkObj = params.get(RpcParameterNameConstant.REMARK);
            // check parameters
            Preconditions.checkNotNull(new Object[]{chainIdObj, addressObj, signAddressObj, amountObj}, AccountErrorCode.NULL_PARAMETER);
            int chainId = (int) chainIdObj;
            int assetsId;
            Chain chain = chainManager.getChainMap().get(chainId);
            Preconditions.checkNotNull(chain, AccountErrorCode.CHAIN_NOT_EXIST);
            // if the assetsId is null,the default assetsId is the chain's main assets
            if (assetsIdObj == null) {
                assetsId = chain.getConfig().getAssetsId();
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
                Preconditions.checkNotNull(toAddress, AccountErrorCode.PARAMETER_ERROR);
            } else if (type == TxType.TRANSFER) {
                Preconditions.checkNotNull(alias, AccountErrorCode.PARAMETER_ERROR);
                AliasPo aliasPo = aliasStorageService.getAlias(chainId, alias);
                Preconditions.checkNotNull(aliasPo, AccountErrorCode.ACCOUNT_NOT_EXIST);
                toAddress = AddressTool.getStringAddressByBytes(aliasPo.getAddress());
            } else {
                throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
            }
            multiSigAccount = multiSignAccountService.getMultiSigAccountByAddress(chainId, address);
            Preconditions.checkNotNull(multiSigAccount, AccountErrorCode.ACCOUNT_NOT_EXIST);
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
            Preconditions.checkNotNull(account, AccountErrorCode.ACCOUNT_NOT_EXIST);

            //验证签名账户是否属于多签账户的签名账户,如果不是多签账户下的地址则提示错误
            if (!AddressTool.validSignAddress(multiSigAccount.getPubKeyList(), account.getPubKey())) {
                throw new NulsRuntimeException(AccountErrorCode.SIGN_ADDRESS_NOT_MATCH);
            }

            MultiSignTransactionResultDto multiSignTransactionResultDto = transactionService.createMultiSignTransfer(chainId, assetsId, account, password, multiSigAccount, toAddress, amount, remark);
            if (multiSignTransactionResultDto.isBroadcasted()) {
                map.put("txHash", multiSignTransactionResultDto.getTransaction().getHash().toHex());
            } else {
                map.put("tx", RPCUtil.encode(multiSignTransactionResultDto.getTransaction().serialize()));
            }
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(e.getMessage());
        }
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
        Map<String, String> map = new HashMap<>(1);
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
        Object signAddressObj = params == null ? null : params.get(RpcParameterNameConstant.SIGN_ADDREESS);
        Object txStrObj = params == null ? null : params.get(RpcParameterNameConstant.TX);
        try {
            // check parameters
            if (params == null || chainIdObj == null || signAddressObj == null ||
                    txStrObj == null) {
                LoggerUtil.logger.warn("ac_signMultiSignTransaction params is null");
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            int chainId = (int) chainIdObj;
            String password = (String) passwordObj;
            String signAddress = (String) signAddressObj;
            String txStr = (String) txStrObj;
            //查询出账户
            Account account = accountService.getAccount(chainId, signAddress);
            if (account == null) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
            MultiSignTransactionResultDto multiSignTransactionResultDto = transactionService.signMultiSignTransaction(chainId, account, password, txStr);
            if (multiSignTransactionResultDto.isBroadcasted()) {
                map.put("txHash", multiSignTransactionResultDto.getTransaction().getHash().toHex());
            } else {
                map.put("tx", RPCUtil.encode(multiSignTransactionResultDto.getTransaction().serialize()));
            }
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (Exception e) {
            return failed(e.getMessage());
        }
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
            return bytes.length <= AccountConstant.TX_REMARK_MAX_LEN;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }
}
