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
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.util.RPCUtil;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.nuls.account.util.LoggerUtil.LOG;

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
     * 模块交易统一验证器，验证模块内各种交易，以及冲突检测等
     * validate the transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = BaseConstant.TX_VALIDATOR, version = 1.0, description = "validate the transaction")
    public Response accountTxValidate(Map params) {
        Chain chain = null;
        List<Transaction> txList = new ArrayList<>();
        List<String> txHashList = null;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txListObj = params == null ? null : params.get(RpcParameterNameConstant.TX_LIST);
        try {
            // check parameters
            if (params == null || chainIdObj == null || txListObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            List<String> txListStr = (List<String>) txListObj;
            if (txListStr != null) {
                txHashList = transactionService.accountTxValidate(chain, txListStr);
            }
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, List<String>> resultMap = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        resultMap.put("list", txHashList);
        return success(resultMap);
    }

    /**
     * batch commit the transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = BaseConstant.TX_COMMIT, version = 1.0, description = "batch commit the transaction")
    public Response commitTx(Map params) {
        boolean result = true;
        Chain chain = null;
        List<String> txList;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txListgObj = params == null ? null : params.get(RpcParameterNameConstant.TX_LIST);
        Object blockHeaderDigest = params == null ? null : params.get(RpcParameterNameConstant.BLOCK_HEADER_DIGEST);
        List<Transaction> commitSucTxList = new ArrayList<>();
        // check parameters
        if (params == null || chainIdObj == null || txListgObj == null) {
            LoggerUtil.LOG.warn("ac_commitTx params is null");
            return failed(AccountErrorCode.NULL_PARAMETER);
        }
        chain = chainManager.getChain((int) chainIdObj);
        if (null == chain) {
            throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
        }
        int chainId = chain.getChainId();
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
                        LoggerUtil.LOG.warn("ac_commitTx alias tx commit error");
                        break;
                    }
                    commitSucTxList.add(tx);
                }
            }
        } catch (NulsException e) {
            LoggerUtil.LOG.info("", e);
            result = false;
        } catch (Exception e) {
            LoggerUtil.LOG.error("", e);
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
                    LoggerUtil.LOG.error("ac_commitTx alias tx rollback error");
                    throw new NulsException(AccountErrorCode.ALIAS_ROLLBACK_ERROR);
                }
            }
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }

        Map<String, Boolean> resultMap = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        resultMap.put("value", result);
        return success(resultMap);
    }

    /**
     * batch rollback the transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = BaseConstant.TX_ROLLBACK, version = 1.0, description = "batch rollback the transaction")
    public Response rollbackTx(Map params) {
        //默认回滚成功
        boolean result = true;
        Chain chain = null;
        List<String> txList;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object txListgObj = params == null ? null : params.get(RpcParameterNameConstant.TX_LIST);
        Object blockHeaderDigest = params == null ? null : params.get(RpcParameterNameConstant.BLOCK_HEADER_DIGEST);
        List<Transaction> rollbackSucTxList = new ArrayList<>();
        // check parameters
        if (params == null || chainIdObj == null || txListgObj == null) {
            LoggerUtil.LOG.warn("ac_rollbackTx params is null");
            return failed(AccountErrorCode.NULL_PARAMETER);
        }
        chain = chainManager.getChain((int) chainIdObj);
        if (null == chain) {
            throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
        }
        int chainId = chain.getChainId();
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
                        LoggerUtil.LOG.warn("ac_rollbackTx alias tx rollback error");
                        break;
                    }
                    rollbackSucTxList.add(tx);
                }
            }
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            result = false;
        } catch (Exception e) {
            errorLogProcess(chain, e);
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
                    LoggerUtil.LOG.error("ac_rollbackTx alias tx commit error");
                    throw new NulsException(AccountErrorCode.ALIAS_SAVE_ERROR);
                }
            }
        }catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }

        Map<String, Boolean> resultMap = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
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
        Chain chain = null;
        Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        try {
            // check parameters
            if (params == null) {
                LoggerUtil.LOG.warn("ac_transfer params is null");
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // parse params
            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            TransferDto transferDto = JSONUtils.map2pojo(params, TransferDto.class);
            chain = chainManager.getChain(transferDto.getChainId());
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            int chainId = chain.getChainId();
            Function<CoinDto, CoinDto> checkAddress = cd -> {
                //如果address 不是地址就当别名处理
                if (!AddressTool.validAddress(chainId, cd.getAddress())) {
                    AliasPo aliasPo = aliasStorageService.getAlias(chainId, cd.getAddress());
                    Preconditions.checkNotNull(aliasPo, AccountErrorCode.ALIAS_NOT_EXIST);
                    cd.setAddress(AddressTool.getStringAddressByBytes(aliasPo.getAddress()));
                }
                return cd;
            };
            List<CoinDto> inputList = transferDto.getInputs().stream().map(checkAddress).collect(Collectors.toList());
            List<CoinDto> outputList = transferDto.getOutputs().stream().map(checkAddress).collect(Collectors.toList());

            if (inputList == null || outputList == null) {
                LoggerUtil.LOG.warn("ac_transfer params input or output is null");
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
            Transaction tx = transactionService.transfer(chainId, inputList, outputList, transferDto.getRemark());
            map.put("value", tx.getHash().toHex());
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
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
        Chain chain = null;
        Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
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
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            int chainId = chain.getChainId();
            int assetsId;
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
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
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
        Chain chain = null;
        Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
        Object signAddressObj = params == null ? null : params.get(RpcParameterNameConstant.SIGN_ADDREESS);
        Object txStrObj = params == null ? null : params.get(RpcParameterNameConstant.TX);
        try {
            // check parameters
            if (params == null || chainIdObj == null || signAddressObj == null ||
                    txStrObj == null) {
                LoggerUtil.LOG.warn("ac_signMultiSignTransaction params is null");
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chain = chainManager.getChain((int) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            int chainId = chain.getChainId();
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
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
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

    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LOG.error(e);
        } else {
            chain.getLogger().error(e);
        }
    }
}
