package io.nuls.eventbus.constant;

import io.nuls.rpc.info.Constants;
import io.nuls.tools.thread.ThreadUtils;
import io.nuls.tools.thread.commom.NulsThreadFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author naveen
 */
public class EbConstants {

    public static final String SUBSCRIBE = "subscribe";

    public static final String UNSUBSCRIBE = "unsubscribe";

    public static final String EB_SUBSCRIBE = "eb_subscribe";

    public static final String EB_UNSUBSCRIBE = "eb_unsubscribe";

    public static final String EB_SEND = "eb_send";

    public static final String TB_EB_TOPIC = "topic";

    public static final Map<String,String> MODULE_CONFIG_MAP = new HashMap<>();

    public static final String KERNEL_URL = "kernelUrl";

    public static final String ROCKS_DB_PATH = "rocksdb.path";

    public static final String LANGUAGE = "language";

    public static final String ENCODING = "encoding";

    public static final String MODULE_FILE = "modules.ini";

    public static final String SYSTEM_SECTION = "system";

    public static final String DB_SECTION = "db";

    public static final String RPC_PACKAGE_EB = "io.nuls.eventbus.rpc.cmd";

    public static final String EB_BASE_PACKAGE = "io.nuls.eventbus";

    public static final int EVENT_DISPATCH_RETRY_COUNT = 5;

    public static final long EVENT_RETRY_WAIT_TIME = 10 * Constants.MILLIS_PER_SECOND;

    public static final String CMD_PARAM_ROLE = "role";

    public static final String CMD_PARAM_ROLE_NAME = "roleName";

    public static final String CMD_PARAM_TOPIC = "topic";

    public static final String CMD_PARAM_DATA = "data";

    public static final String CMD_PARAM_DOMAIN = "domain";

    public static final String CMD_PARAM_ROLE_CALLBACK = "callBackCmd";

    /**
     * Thread pool for retry mechanism
     */
    public static final ExecutorService SEND_RETRY_THREAD_POOL = ThreadUtils.createThreadPool(5, 500, new NulsThreadFactory("SendRetryProcessor"));

    public static final ExecutorService EB_THREAD_POOL = ThreadUtils.createThreadPool(5, 500, new NulsThreadFactory("eventBus"));
}
