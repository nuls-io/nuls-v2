package io.nuls.base.protocol;

import java.util.Comparator;

public class ProtocolConstant {

    /**
     * 协议配置信息排序器
     */
    public static Comparator<ProtocolConfigJson> PROTOCOL_CONFIG_COMPARATOR = Comparator.comparingInt(ProtocolConfigJson::getVersion);

    /**
     * 协议配置文件名称
     * Protocol configuration file name.
     */
    public static String PROTOCOL_CONFIG_FILE = "protocol-config.json";
}
