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
     * 错误编码
     */
    public static final String ERROR_ASSET_SYMBOL_NULL = "A10000";
    public static final String ERROR_ASSET_SYMBOL_MAX = "A10001";
    public static final String ERROR_ASSET_SYMBOL_EXIST = "A10002";
    public static final String ERROR_JSON_TO_ASSET = "A10003";
    public static final String ERROR_ASSET_RECOVERY_RATE = "A10004";
    public static final String ERROR_ASSET_ID_EXIST = "A10005";
    public static final String ERROR_ASSET_NAME_NULL = "A10006";
    public static final String ERROR_ASSET_NAME_MAX = "A10007";
    public static final String ERROR_ASSET_DEPOSITNULS = "A10008";
    public static final String ERROR_ASSET_INITNUMBER_MIN = "A10009";
    public static final String ERROR_ASSET_INITNUMBER_MAX = "A10010";
    public static final String ERROR_ASSET_DECIMALPLACES_MIN = "A10011";
    public static final String ERROR_ASSET_DECIMALPLACES_MAX = "A10012";
    public static final String ERROR_CHAIN_ASSET_NOT_MATCH = "A10013";
    public static final String ERROR_ASSET_NOT_EXIST = "A10014";
    public static final String ERROR_ASSET_EXCEED_INIT = "A10015";
}
