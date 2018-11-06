package io.nuls.account.init;

import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountParam;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.ConfigLoader;

import java.io.IOException;

/**
 * @author: qinyifeng
 * @date: 2018/10/15
 */
public class AccountBootstrap {
    public static void main(String[] args) {
        Log.info("bootstarp account module start...");

        // 初始化配置
        cfgInit();

    }

    public static void cfgInit() {
        try {
            NulsConfig.MODULES_CONFIG = ConfigLoader.loadIni(NulsConfig.MODULES_CONFIG_FILE);
            AccountParam accountParam = AccountParam.getInstance();
            accountParam.setDataPath(NulsConfig.MODULES_CONFIG.getCfgValue(AccountConstant.DB_SECTION, AccountConstant.DB_DATA_PATH, null));
            Log.info(String.valueOf(accountParam.getDataPath()));
        } catch (IOException e) {
            Log.error("Account Bootstrap cfgInit failed", e);
            throw new RuntimeException("Account Bootstrap cfgInit failed");
        }
    }
}
