package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.service.AccountService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;

import java.util.List;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/5
 */
@Component
public class AccountCmd extends BaseCmd {

    @Autowired
    private AccountService accountService;

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
        Log.debug("createAccount start:");
        try {
            // check parameters
            if (params.get(0) == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            Integer chainId = (Integer) params.get(0);
            Integer count = null;
            String password = null;
            if (params.get(1) != null) {
                count = (Integer) params.get(1);
            }
            if (params.get(2) != null) {
                password = (String) params.get(2);
            }
            accountService.createAccount(chainId, count, password);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode(), 1.0, null);
        }
        return success(1.0, "success", null);
    }

}
