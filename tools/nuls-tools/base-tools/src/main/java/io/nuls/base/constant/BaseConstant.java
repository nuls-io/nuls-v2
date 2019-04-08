package io.nuls.base.constant;

import io.nuls.tools.protocol.ProtocolConfigJson;

import java.util.Comparator;

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
     * 切换序列化交易HASH方法的高度
     */
    public static Long CHANGE_HASH_SERIALIZE_HEIGHT;

    /**
     * utxo锁定时间分界值
     * 小于该值表示按照高度锁定
     * 大于该值表示按照时间锁定
     */
    public static long BlOCKHEIGHT_TIME_DIVIDE = 1000000000000L;

    /**
     * 默认链id（nuls主链）,链id会影响地址的生成，当前地址以“Ns”开头
     * The default chain id (nuls main chain), the chain id affects the generation of the address,
     * and the current address begins with "Ns".8964.
     */
//    public static short DEFAULT_CHAIN_ID = 261;

    /**
     * 出块间隔时间（秒）
     * Block interval time.
     * unit:second
     */
    public static long BLOCK_TIME_INTERVAL_SECOND = 10;

    /**
     * 协议配置文件名称
     * Protocol configuration file name.
     */
    public static String PROTOCOL_CONFIG_FILE = "protocol-config.json";

    /**
     * 协议配置信息排序器
     */
    public static Comparator<ProtocolConfigJson> PROTOCOL_CONFIG_COMPARATOR = Comparator.comparingInt(ProtocolConfigJson::getVersion);
}
