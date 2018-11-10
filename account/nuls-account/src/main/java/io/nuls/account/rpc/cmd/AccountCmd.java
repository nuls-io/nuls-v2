package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.model.bo.Account;
import io.nuls.account.model.dto.AccountOfflineDto;
import io.nuls.account.model.dto.SimpleAccountDto;
import io.nuls.account.service.AccountService;
import io.nuls.account.util.AccountTool;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

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
public class AccountCmd extends BaseCmd {

    @Autowired
    private AccountService accountService;
    //private AccountService accountService = SpringLiteContext.getBean(AccountService.class);

    /*
     * CmdAnnotation注解包含
     * 1. 调用的命令
     * 2. 调用的命令的版本
     * 3. 调用的命令是否兼容前一个版本
     *
     * 返回的结果包含：
     * 1. 内置编码
     * 2. 真正调用的版本号
     * 3. 返回的文本
     * 4. 返回的对象，由接口自己约定
     */

    /**
     * 创建账户
     *
     * @param params [chainId,count,password]
     * @return
     */
    @CmdAnnotation(cmd = "ac_createAccount", version = 1.0, preCompatible = true)
    public CmdResponse createAccount(List params) {
        Log.debug("createAccount start");
        Map<String, List<String>> map = new HashMap<>();
        List<String> list = new ArrayList<>();
        try {
            // check parameters
            if (params.get(0) == null || params.size() != 3) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            short chainId = 0;
            chainId += (Integer) params.get(0);
            //创建账户个数
            Integer count = params.get(1) != null ? (Integer) params.get(1) : 0;
            //账户密码
            String password = params.get(2) != null ? (String) params.get(2) : null;
            //创建账户
            List<Account> accountList = accountService.createAccount(chainId, count, password);
            if (accountList != null) {
                for (Account account : accountList) {
                    list.add(account.getAddress().toString());
                }
                map.put("list", list);
            }
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode(), AccountConstant.RPC_VERSION, null);
        }
        return success(AccountConstant.RPC_VERSION, AccountConstant.SUCCESS_MSG, map);
    }

    /**
     * 创建离线账户, 该账户不保存到数据库, 并将直接返回账户的所有信息
     *
     * @param params [chainId,count,password]
     * @return
     */
    @CmdAnnotation(cmd = "ac_createOfflineAccount", version = 1.0, preCompatible = true)
    public CmdResponse createOfflineAccount(List params) {
        Map<String, List<AccountOfflineDto>> map = new HashMap<>();
        try {
            // check parameters size
            if (params.get(0) == null || params.size() != 3) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            short chainId = 0;
            chainId += (Integer) params.get(0);
            //创建账户个数
            Integer count = params.get(1) != null ? (Integer) params.get(1) : 0;
            //账户密码
            String password = params.get(2) != null ? (String) params.get(2) : null;

            //Check parameter is correct.
            if (count <= 0 || count > AccountTool.CREATE_MAX_SIZE) {
                throw new NulsRuntimeException(AccountErrorCode.PARAMETER_ERROR);
            }
            if (StringUtils.isNotBlank(password) && !AccountTool.validPassword(password)) {
                throw new NulsRuntimeException(AccountErrorCode.PASSWORD_FORMAT_WRONG);
            }

            List<AccountOfflineDto> accounts = new ArrayList<>();
            try {
                for (int i = 0; i < count; i++) {
                    Account account = AccountTool.createAccount(chainId);
                    if (StringUtils.isNotBlank(password)) {
                        account.encrypt(password);
                    }
                    accounts.add(new AccountOfflineDto(account));
                }
            } catch (NulsException e) {
                throw e;
            }
            map.put("list", accounts);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode(), AccountConstant.RPC_VERSION, null);
        } catch (NulsException e) {
            return failed(e.getErrorCode(), AccountConstant.RPC_VERSION, null);
        }
        return success(AccountConstant.RPC_VERSION, AccountConstant.SUCCESS_MSG, map);
    }

    /**
     * 根据地址获取账户
     *
     * @param params [chainId,address]
     * @return
     */
    @CmdAnnotation(cmd = "ac_getAccountByAddress", version = 1.0, preCompatible = true)
    public CmdResponse getAccountByAddress(List params) {
        Log.debug("getAccountByAddress start");
        Account account;
        try {
            // check parameters
            if (params.get(0) == null || params.size() != 2) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            //链ID
            short chainId = 0;
            chainId += (Integer) params.get(0);
            //账户地址
            String address = params.get(1) != null ? (String) params.get(1) : null;
            //根据地址查询账户
            account = accountService.getAccount(chainId, address);
            if (null == account) {
                throw new NulsRuntimeException(AccountErrorCode.ACCOUNT_NOT_EXIST);
            }
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode(), AccountConstant.RPC_VERSION, null);
        }
        return success(AccountConstant.RPC_VERSION, AccountConstant.SUCCESS_MSG, new SimpleAccountDto(account));
    }

}
