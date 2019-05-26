package io.nuls.core.constant;

/**
 * @author tag
 */
public class BaseConstant {
    /**
     * 主网和测试网的默认chainID
     */
    public static final short MAINNET_CHAIN_ID = 1;
    public static final short TESTNET_CHAIN_ID = 2;

    public static final String MAINNET_DEFAULT_ADDRESS_PREFIX = "NULS";
    public static final String TESTNET_DEFAULT_ADDRESS_PREFIX = "tNULS";
    /**
     * hash length
     */
    public static final int ADDRESS_LENGTH = 23;

    /**
     * 默认的地址类型，一条链可以包含几种地址类型，地址类型包含在地址中
     * The default address type, a chain can contain several address types, and the address type is contained in the address.
     */
    public static byte DEFAULT_ADDRESS_TYPE = 1;

    /**
     * 智能合约地址类型
     * contract address type
     */
    public static byte CONTRACT_ADDRESS_TYPE = 2;

    /**
     * 多重签名地址
     * contract address type
     */
    public static byte P2SH_ADDRESS_TYPE = 3;

    /**
     * 主网运行中的版本，默认为1，会根据钱包更新到的块的最新版本做修改
     */
    public static volatile Integer MAIN_NET_VERSION = 1;

    /**
     * utxo锁定时间分界值
     * 小于该值表示按照高度锁定
     * 大于该值表示按照时间锁定
     */
    public static long BlOCKHEIGHT_TIME_DIVIDE = 1000000000000L;

    /**
     * 出块间隔时间（秒）
     * Block interval time.
     * unit:second
     */
    public static long BLOCK_TIME_INTERVAL_SECOND = 10;
    /**
     *
     */
    public static final String MSG_PROCESS = "msgProcess";

    /**
     * 模块统一交易验证器RPC接口
     */
    public static final String TX_VALIDATOR = "txValidator";

    /**
     * 模块统一交易提交RPC接口
     */
    public static final String TX_COMMIT = "txCommit";

    /**
     * 模块统一交易回滚RPC接口
     */
    public static final String TX_ROLLBACK = "txRollback";
}
