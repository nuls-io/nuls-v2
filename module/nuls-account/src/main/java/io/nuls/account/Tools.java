package io.nuls.account;

import io.nuls.account.rpc.cmd.AccountCmd;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;

import java.util.HashMap;
import java.util.Map;

import static io.nuls.account.constant.RpcParameterNameConstant.*;

/**
 * @Author: zhoulijun
 * @Time: 2019-08-21 16:18
 * @Description: 功能描述
 */
public class Tools {

    public static void main(String[] args) {
        SpringLiteContext.init("io.nuls");
        AccountCmd accountCmd = SpringLiteContext.getBean(AccountCmd.class);
        Map<String,Object> param = new HashMap<>();
        param.put(CHAIN_ID,2);
        param.put(PRIKEY,"477059f40708313626cccd26f276646e4466032cabceccbf571a7c46f954eb75");
        param.put(PASSWORD,"nuls123456");
        param.put(OVERWRITE,true);
        Log.info("{}",accountCmd.importAccountByPriKey(param));
    }

}
