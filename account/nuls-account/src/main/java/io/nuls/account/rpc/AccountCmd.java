package io.nuls.account.rpc;

import io.nuls.account.service.AccountService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.StringUtils;
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
        if (params.get(0) == null) {
            return result(0, 1.0, "params chainId is null", null);
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
        return result(SUCCESS_CODE, 1.0, "hello nuls", null);
    }

}
