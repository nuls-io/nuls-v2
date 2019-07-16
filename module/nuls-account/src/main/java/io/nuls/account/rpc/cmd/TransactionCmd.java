package io.nuls.account.rpc.cmd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcConstant;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.dto.CoinDTO;
import io.nuls.account.model.dto.MultiSignTransactionResultDTO;
import io.nuls.account.model.dto.MultiSignTransferDTO;
import io.nuls.account.model.dto.TransferDTO;
import io.nuls.account.service.AccountService;
import io.nuls.account.service.AliasService;
import io.nuls.account.service.MultiSignAccountService;
import io.nuls.account.service.TransactionService;
import io.nuls.account.storage.AliasStorageService;
import io.nuls.account.util.LoggerUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.account.util.validator.TxValidator;
import io.nuls.base.RPCUtil;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "inputs", requestType = @TypeDescriptor(value = List.class, collectionElement = CoinDTO.class), parameterDes = "交易支付方数据"),
            @Parameter(parameterName = "outputs", requestType = @TypeDescriptor(value = List.class, collectionElement = CoinDTO.class), parameterDes = "交易接受方数据"),
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
            TransferDTO transferDto = JSONUtils.map2pojo(params, TransferDTO.class);
            chain = chainManager.getChain(transferDto.getChainId());
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }

            Transaction tx = transactionService.transfer(chain, transferDto);
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

    @CmdAnnotation(cmd = "ac_createMultiSignTransfer", version = 1.0, description = "创建多签地址转账交易/create multi sign transfer")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "inputs", requestType = @TypeDescriptor(value = List.class, collectionElement = CoinDTO.class), parameterDes = "交易支付方数据"),
            @Parameter(parameterName = "outputs", requestType = @TypeDescriptor(value = List.class, collectionElement = CoinDTO.class), parameterDes = "交易接受方数据"),
            @Parameter(parameterName = "remark", parameterType = "String", parameterDes = "交易备注"),
            @Parameter(parameterName = "signAddress", parameterType = "String", canNull = true, parameterDes = "第一个签名账户地址(不填则只创建交易不签名)"),
            @Parameter(parameterName = "password", parameterType = "String", canNull = true, parameterDes = "第一个签名账户密码(不填则只创建交易不签名)")
    })
    @ResponseData(name = "返回值", description = "返回一个Map,包含三个key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "完整交易序列化字符串,如果交易没达到最小签名数可继续签名(没有广播)"),
            @Key(name = "txHash",  description = "交易hash,交易已完成(已广播)"),
            @Key(name = "completed", valueType = boolean.class, description = "true:交易已完成(已广播),false:交易没完成,没有达到最小签名数")
    }))
    public Response multiSignTransfer(Map params) {
        Chain chain = null;
        Map<String, Object> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        try {
            // check parameters
            if (params == null) {
                LoggerUtil.LOG.warn("ac_transfer params is null");
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            MultiSignTransferDTO multiSignTransferDTO = JSONUtils.map2pojo(params, MultiSignTransferDTO.class);
            chain = chainManager.getChain(multiSignTransferDTO.getChainId());
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }

            MultiSignTransactionResultDTO multiSignTransactionResultDto = transactionService.multiSignTransfer(chain, multiSignTransferDTO);
            boolean result = false;
            if (multiSignTransactionResultDto.isBroadcasted()) {
                result = true;
            }
            Transaction tx = multiSignTransactionResultDto.getTransaction();
            map.put("completed", result);
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
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "交易数据字符串"),
            @Parameter(parameterName = "signAddress", parameterType = "String", parameterDes = "签名账户地址"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "签名账户密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map,包含三个key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "完整交易序列化字符串,如果交易没达到最小签名数可继续签名(没有广播)"),
            @Key(name = "txHash",  description = "交易hash,交易已完成(已广播)"),
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
            chain = chainManager.getChain((Integer) chainIdObj);
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
            MultiSignTransactionResultDTO multiSignTransactionResultDto = transactionService.signMultiSignTransaction(chain, account, password, txStr);
            boolean result = false;
            if (multiSignTransactionResultDto.isBroadcasted()) {
                result = true;
            }
            Transaction tx = multiSignTransactionResultDto.getTransaction();
            map.put("completed", result);
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

    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LOG.error(e);
        } else {
            chain.getLogger().error(e);
        }
    }
}
