package io.nuls.chain.info;

import io.nuls.base.basic.AddressTool;
import io.nuls.tools.constant.ErrorCode;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

/**
 * @author tangyi
 * @date 2018/11/8
 */
public class CmConstants {

    private CmConstants() {
    }

    /**
     * 数据库文件存放路径
     * The database file storage path
     */
    public static final String DATA_PATH = "../data";

    public static final BigInteger ZERO = new BigInteger("0");

    public static final String MODULE_ROLE = "CM";
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
    public static final String ASSET_DEPOSIT_NULS = "asset_depositNuls";
    public static final String ASSET_DEPOSIT_NULS_DESTROY = "asset_depositNuls_destroy_rate";
    public static final String ASSET_DEPOSIT_NULS_lOCK = "asset_depositNuls_lock_rate";

    public static final String ASSET_INIT_NUMBER_MIN = "asset_initNumber_min";
    public static final String ASSET_INIT_NUMBER_MAX = "asset_initNumber_max";
    public static final String ASSET_DECIMAL_PLACES_MIN = "asset_decimalPlaces_min";
    public static final String ASSET_DECIMAL_PLACES_MAX = "asset_decimalPlaces_max";
    public static final String ASSET_RECOVERY_RATE = "asset_recovery_rate";

    public static final Map<String, String> CHAIN_ASSET_MAP = new HashMap<>();
    public static final String CHAIN_ASSET = "defaultAsset";
    public static final String NULS_CHAIN_ID = "nuls_chain_id";
    public static final String NULS_CHAIN_NAME = "nuls_chain_name";
    public static final String NULS_ASSET_ID = "nuls_asset_id";
    public static final String NULS_ASSET_MAX = "nuls_asset_initNumber_max";
    public static final String NULS_ASSET_SYMBOL = "nuls_asset_symbol";




    /**
     * 黑洞地址，该地址的资产无法找回
     * //TODO 该地址需要加上链ID，否则无法适配新的地址规则
     */
    public static final byte[] BLACK_HOLE_ADDRESS = AddressTool.getAddress("JQJmP5xKDzAgJ8tJSQkCtKwbodAu20423");


}
