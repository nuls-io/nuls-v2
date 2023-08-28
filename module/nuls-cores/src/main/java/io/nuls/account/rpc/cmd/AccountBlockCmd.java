package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.bo.Chain;
import io.nuls.account.model.po.AccountBlockExtendPO;
import io.nuls.account.model.po.AccountBlockPO;
import io.nuls.account.model.vo.AccountBlockVO;
import io.nuls.account.storage.AccountBlockStorageService;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.nuls.account.util.LoggerUtil.LOG;

/**
 * @author: PierreLuo
 * @date: 2022/1/18
 */
@Component
@NulsCoresCmd(module = ModuleE.AC)
public class AccountBlockCmd extends BaseCmd {

    @Autowired
    private AccountBlockStorageService accountBlockStorageService;
    @Autowired
    private ChainManager chainManager;

    @CmdAnnotation(cmd = "ac_isBlockAccount", version = 1.0, description = "检查账户是否锁定")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String",  parameterDes = "地址")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = Boolean.class, description = "账户是否锁定")
    }))
    public Response isBlockAccount(Map params) {
        boolean isBlock;
        Chain chain = null;
        String address;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object addressObj = params == null ? null : params.get("address");
        try {
            // check parameters
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            address = (String) addressObj;
            isBlock = accountBlockStorageService.existAccount(AddressTool.getAddress(address));
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> result = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
        result.put("value", isBlock);
        return success(result);
    }

    @CmdAnnotation(cmd = "ac_getAllBlockAccount", version = 1.0, description = "查询所有锁定账户")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class))
    public Response getAllBlockAccount(Map params) {
        Chain chain = null;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        try {
            // check parameters
            if (params == null || chainIdObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            List<AccountBlockPO> accountList = accountBlockStorageService.getAccountList();
            if (accountList == null) {
                accountList = Collections.EMPTY_LIST;
            }
            List<String> collect = accountList.stream().map(a -> AddressTool.getStringAddressByBytes(a.getAddress())).collect(Collectors.toList());
            Map<String, Object> result = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
            result.put("value", collect);
            return success(result);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @CmdAnnotation(cmd = "ac_getBlockAccountBytes", version = 1.0, description = "查询锁定账户的信息")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String",  parameterDes = "地址")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = String.class, description = "锁定账户的序列化字符串")
    }))
    public Response getBlockAccountBytes(Map params) {
        Chain chain = null;
        try {
            String address;
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get("address");
            // check parameters
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            address = (String) addressObj;
            byte[] bytes = accountBlockStorageService.getAccountBytes(AddressTool.getAddress(address));
            Map<String, String> result = new HashMap<>(AccountConstant.INIT_CAPACITY_2);
            String resultHex;
            if (bytes == null) {
                resultHex = "";
            } else {
                resultHex = HexUtil.encode(bytes);
            }
            result.put("value", resultHex);
            return success(result);
        } catch (NulsRuntimeException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @CmdAnnotation(cmd = "ac_getBlockAccountInfo", version = 1.0, description = "查询锁定账户的信息")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "address", parameterType = "String",  parameterDes = "地址")
    })
    @ResponseData(name = "返回值", description = "返回一个对象", responseType = @TypeDescriptor(value = AccountBlockVO.class))
    public Response getBlockAccountInfo(Map params) {
        Chain chain = null;
        try {
            String address;
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get("address");
            // check parameters
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chain = chainManager.getChain((Integer) chainIdObj);
            if (null == chain) {
                throw new NulsRuntimeException(AccountErrorCode.CHAIN_NOT_EXIST);
            }
            address = (String) addressObj;
            AccountBlockPO account = accountBlockStorageService.getAccount(AddressTool.getAddress(address));
            if (account == null) {
                return failed(AccountErrorCode.DATA_NOT_FOUND);
            }
            AccountBlockVO vo = new AccountBlockVO();
            vo.setAddress(AddressTool.getStringAddressByBytes(account.getAddress()));
            if (account.getExtend() != null) {
                AccountBlockExtendPO extendPO = new AccountBlockExtendPO();
                extendPO.parse(account.getExtend(), 0);
                vo.setTypes(extendPO.getTypes());
                vo.setContracts(extendPO.getContracts());
            }
            return success(vo);
        } catch (NulsRuntimeException e) {
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
