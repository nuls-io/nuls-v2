package io.nuls.eventbus.constant;

import java.util.HashMap;
import java.util.Map;

public class EBConstants {

    public static String SUBSCRIBE = "subscribe";

    public static String UNSUBSCRIBE = "unsubscribe";

    public static String SEND = "send";

    public static String EB_SUBSCRIBE = "eb_subscribe";

    public static String EB_UNSUBSCRIBE = "eb_unsubscribe";

    public static String EB_SEND = "eb_send";

    public static String TB_EB_TOPIC = "topic";

    public static Map<String,String> MODULE_CONFIG_MAP = new HashMap<>();

    public static String KERNEL_URL = "kernelUrl";

    public static String ROCKS_DB_PATH = "rocksdb.path";

    public static String LANGUAGE = "language";

    public static String ENCODING = "encoding";

    public static String MODULE_FILE = "modules.ini";

    public static String SYSTEM_SECTION = "system";

    public static String DB_SECTION = "db";

    public static String RPC_PACKAGE_EB = "io.nuls.eventbus.rpc.cmd";

    public static String EB_BASE_PACKAGE = "io.nuls.eventbus";

}
