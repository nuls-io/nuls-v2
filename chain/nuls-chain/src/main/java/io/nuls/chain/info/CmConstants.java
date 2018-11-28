package io.nuls.chain.info;

import io.nuls.tools.constant.ErrorCode;

import java.util.HashMap;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/8
 * @description
 */
public class CmConstants {

    private CmConstants() {
    }

    public static final String ADDRESS_TYPE_NULS = "nuls";
    public static final String ADDRESS_TYPE_OTHER = "other";

    /**
     * database data path
     */
    public static final String DB = "db";
    public static final String DB_DATA_PATH = "rocksdb.datapath";

    public static final ErrorCode DB_TABLE_CREATE_ERROR = ErrorCode.init("20011");

    /**
     * 系统配置项section名称
     * The configuration item section name of the kernel module.
     */
    public static final String CFG_SYSTEM_SECTION = "system";

    /**
     * 系统配置中语言设置的字段名
     * The field name of the language set in the system configuration.
     */
    public static final String CFG_SYSTEM_LANGUAGE = "language";

    /**
     * 系统配置中编码设置的字段名
     * The field name of the code setting in the system configuration.
     */
    public static final String CFG_SYSTEM_DEFAULT_ENCODING = "encoding";

    /**
     * 初始配置参数
     */
    public static final Map<String, String> PARAM_MAP = new HashMap<>();
    public static final String PARAM = "param";
    public static final String ASSET_SYMBOL_MAX = "asset_symbol_max";
    public static final String ASSET_NAME_MAX = "asset_name_max";
    public static final String ASSET_DEPOSITNULS = "asset_depositNuls";
    public static final String ASSET_INITNUMBER_MIN = "asset_initNumber_min";
    public static final String ASSET_INITNUMBER_MAX = "asset_initNumber_max";
    public static final String ASSET_DECIMALPLACES_MIN = "asset_decimalPlaces_min";
    public static final String ASSET_DECIMALPLACES_MAX = "asset_decimalPlaces_max";
    public static final String ASSET_RECOVERY_RATE = "asset_recovery_rate";

    /**
     * 远程RPC指令
     */
    /**
     * 交易注册
     */
    public static final String  CMD_TX_REGISTER = "tx_register";
    /**
     * 创建交易
     */
    public static final String  CMD_TX_NEW_TX = "newTx";

    public static final String CMD_NW_CROSS_SEEDS = "nw_getSeeds";

    public static final String CMD_NW_CREATE_NODEGROUP = "nw_createNodeGroup";

    public static final String CMD_NW_DELETE_NODEGROUP = "nw_delNodeGroup";



}
