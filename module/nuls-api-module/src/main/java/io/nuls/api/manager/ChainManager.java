package io.nuls.api.manager;

import java.util.Map;

/**
 * @author captain
 * @version 1.0
 * @date 19-2-26 下午1:43
 */
public class ChainManager {

    private static ThreadLocal<Map<String,Object>> sessionHolder = new ThreadLocal<>();
}
