package io.nuls.account.rpc.cmd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcConstant;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
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
import io.nuls.account.util.manager.ChainManager;
import io.nuls.account.util.validator.TxValidator;
import io.nuls.base.RPCUtil;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.TxType;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.model.BigIntegerUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
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

    @CmdAnnotation(cmd = "ac_transfer", version = 1.0, description = "创建转账交易/create transfer transaction")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "inputs", parameterType = "List", parameterDes = "交易支付方数据"),
            @Parameter(parameterName = "outputs", parameterType = "List", parameterDes = "交易接受方数据"),
            @Parameter(parameterName = "remark", parameterType = "String", parameterDes = "交易备注")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE,  description = "交易hash")
    }))
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
            Transaction tx = transactionService.transfer(chain, inputList, outputList, transferDto.getRemark());
            map.put(RpcConstant.VALUE, tx.getHash().toHex());
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

    @CmdAnnotation(cmd = "ac_createMultiSignTransfer", version = 1.0, description = "创建多签转账交易/create multi sign transfer")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "assetChainId", parameterType = "int", parameterDes = "资产链id"),
            @Parameter(parameterName = "assetId", parameterType = "int", parameterDes = "资产id"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "多签账户地址"),
            @Parameter(parameterName = "signAddress", parameterType = "String", parameterDes = "第一个签名账户地址"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "第一个签名账户密码"),
            @Parameter(parameterName = "alias", parameterType = "String", parameterDes = "别名"),
            @Parameter(parameterName = "type", parameterType = "int", parameterDes = "类型"),
            @Parameter(parameterName = "toAddress", parameterType = "String", parameterDes = "接收者地址"),
            @Parameter(parameterName = "amount", parameterType = "BigInteger", parameterDes = "金额"),
            @Parameter(parameterName = "remark", parameterType = "String", parameterDes = "备注"),
    })
    @ResponseData(name = "返回值", description = "返回一个Map,包含三个key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "完整交易序列化字符串,如果交易没达到最小签名数可继续签名(没有广播)"),
            @Key(name = "txhash",  description = "交易hash,交易已完成(已广播)"),
            @Key(name = "completed", valueType = boolean.class, description = "true:交易已完成(已广播),false:交易没完成,没有达到最小签名数")
    }))
    public Response createMultiSignTransfer(Map params) {
        Chain chain = null;
        Map<String, Object> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        MultiSigAccount multiSigAccount = null;
        try {
            Preconditions.checkNotNull(params, AccountErrorCode.NULL_PARAMETER);
            Object chainIdObj = params.get(RpcParameterNameConstant.CHAIN_ID);
            Object assetIdObj = params.get(RpcParameterNameConstant.ASSET_ID);
            Object assetChainIdObj = params.get(RpcParameterNameConstant.ASSET_CHAIN_ID);
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
            int assetId;
            int assetChainId;
            // if the assetsId is null,the default assetsId is the chain's main assets

            if (assetChainIdObj == null) {
                assetChainId = chain.getConfig().getChainId();
            } else {
                assetChainId = (int) assetIdObj;
            }

            if (assetIdObj == null) {
                assetId = chain.getConfig().getAssetId();
            } else {
                assetId = (int) assetIdObj;
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
            multiSigAccount = multiSignAccountService.getMultiSigAccountByAddress(address);
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

            MultiSignTransactionResultDto multiSignTransactionResultDto = transactionService.createMultiSignTransfer(chain, assetChainId, assetId, account, password, multiSigAccount, toAddress, amount, remark);
            boolean result = false;
            if (multiSignTransactionResultDto.isBroadcasted()) {
                result = true;
            }
            Transaction tx = multiSignTransactionResultDto.getTransaction();
            map.put("result", result);
            map.put("txHash", tx.getHash().toHex());
            map.put("tx", RPCUtil.encode(tx.serialize()));
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

    @CmdAnnotation(cmd = "ac_signMultiSignTransaction", version = 1.0, description = "多签交易签名/sign MultiSign Transaction")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", parameterType = "int", parameterDes = "链id"),
            @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "交易数据字符串"),
            @Parameter(parameterName = "signAddress", parameterType = "String", parameterDes = "签名账户地址"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "签名账户密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map,包含三个key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "完整交易序列化字符串,如果交易没达到最小签名数可继续签名(没有广播)"),
            @Key(name = "txhash",  description = "交易hash,交易已完成(已广播)"),
            @Key(name = "completed", valueType = boolean.class, description = "true:交易已完成(已广播),false:交易没完成,没有达到最小签名数")
    }))
    public Response signMultiSignTransaction(Map params) {
        Chain chain = null;
        Map<String, Object> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
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
            MultiSignTransactionResultDto multiSignTransactionResultDto = transactionService.signMultiSignTransaction(chain, account, password, txStr);
            boolean result = false;
            if (multiSignTransactionResultDto.isBroadcasted()) {
                result = true;
            }
            Transaction tx = multiSignTransactionResultDto.getTransaction();
            map.put("result", result);
            map.put("txHash", tx.getHash().toHex());
            map.put("tx", RPCUtil.encode(tx.serialize()));
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
