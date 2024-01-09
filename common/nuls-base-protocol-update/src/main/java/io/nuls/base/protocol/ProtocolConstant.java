package io.nuls.base.protocol;

import java.util.Comparator;

public class ProtocolConstant {

    /**
     * Protocol Configuration Information Sorter
     */
    public static Comparator<ProtocolConfigJson> PROTOCOL_CONFIG_COMPARATOR = Comparator.comparingInt(ProtocolConfigJson::getVersion);

    /**
     * Protocol configuration file name
     * Protocol configuration file name.
     */
    public static String PROTOCOL_CONFIG_FILE = "protocol-config.json";
}
