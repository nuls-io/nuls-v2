package io.nuls.chain.info;

import io.nuls.tools.constant.ErrorCode;

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
    public static final String TB_NAME_ASSET = "asset";
    public static final String TB_NAME_CHAIN = "chain";

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
}
