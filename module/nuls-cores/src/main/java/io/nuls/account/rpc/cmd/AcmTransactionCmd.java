package io.nuls.account.rpc.cmd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcConstant;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.dto.*;
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
@NulsCoresCmd(module = ModuleE.AC)
public class AcmTransactionCmd extends BaseCmd {

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

    @CmdAnnotation(cmd = "ac_transfer", version = 1.0, description = "Create a regular transfer transaction/create transfer transaction")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "inputs", requestType = @TypeDescriptor(value = List.class, collectionElement = CoinDTO.class), parameterDes = "Transaction payer data"),
            @Parameter(parameterName = "outputs", requestType = @TypeDescriptor(value = List.class, collectionElement = CoinDTO.class), parameterDes = "Transaction recipient data"),
            @Parameter(parameterName = "remark", parameterType = "String", parameterDes = "Transaction notes")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = RpcConstant.VALUE,  description = "transactionhash")
    }))
    public Response transfer(Map params) {
        Chain chain = null;
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
            Map<String, String> map = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
            map.put(RpcConstant.VALUE, tx.getHash().toHex());
            return success(map);
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

    }

    @CmdAnnotation(cmd = "ac_createMultiSignTransfer", version = 1.0, description = "Create multiple address transfer transactions/create multi sign transfer")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "inputs", requestType = @TypeDescriptor(value = List.class, collectionElement = BaseCoinDTO.class), parameterDes = "Transaction payer data"),
            @Parameter(parameterName = "outputs", requestType = @TypeDescriptor(value = List.class, collectionElement = MultiSignCoinToDTO.class), parameterDes = "Transaction recipient data"),
            @Parameter(parameterName = "remark", parameterType = "String", parameterDes = "Transaction notes"),
            @Parameter(parameterName = "signAddress", parameterType = "String", canNull = true, parameterDes = "First signature account address(If left blank, only create transactions without signing)"),
            @Parameter(parameterName = "signPassword", parameterType = "String", canNull = true, parameterDes = "First signature account password(If left blank, only create transactions without signing)")
    })
    @ResponseData(name = "Return value", description = "Return aMap,Including threekey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "Complete transaction serialization string,If the transaction does not reach the minimum number of signatures, you can continue to sign"),
            @Key(name = "txHash",  description = "transactionhash"),
            @Key(name = "completed", valueType = boolean.class, description = "true:Transaction completed(Broadcasted),false:Transaction not completed,Not reaching the minimum number of signatures")
    }))
    public Response multiSignTransfer(Map params) {
        Chain chain = null;
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
            Map<String, Object> map = new HashMap<>(AccountConstant.INIT_CAPACITY_8);
            map.put("completed", result);
            map.put("txHash", tx.getHash().toHex());
            map.put("tx", RPCUtil.encode(tx.serialize()));
            return success(map);
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
    }

    @CmdAnnotation(cmd = "ac_signMultiSignTransaction", version = 1.0, description = "Multiple transaction signatures/sign MultiSign Transaction")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "Transaction data string"),
            @Parameter(parameterName = "signAddress", parameterType = "String", parameterDes = "Signature account address"),
            @Parameter(parameterName = "signPassword", parameterType = "String", parameterDes = "Signature account password")
    })
    @ResponseData(name = "Return value", description = "Return aMap,Including threekey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx",  description = "Complete transaction serialization string,If the transaction does not reach the minimum number of signatures, you can continue to sign"),
            @Key(name = "txHash",  description = "transactionhash"),
            @Key(name = "completed", valueType = boolean.class, description = "true:Transaction completed(Broadcasted),false:Transaction not completed,Not reaching the minimum number of signatures")
    }))
    public Response signMultiSignTransaction(Map params) {
        Chain chain = null;

        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.SIGN_PASSWORD);
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
            //Retrieve account information
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
            Map<String, Object> map = new HashMap<>(AccountConstant.INIT_CAPACITY_8);
            map.put("completed", result);
            map.put("txHash", tx.getHash().toHex());
            map.put("tx", RPCUtil.encode(tx.serialize()));
            return success(map);
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
    }

    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LOG.error(e);
        } else {
            chain.getLogger().error(e);
        }
    }
}
