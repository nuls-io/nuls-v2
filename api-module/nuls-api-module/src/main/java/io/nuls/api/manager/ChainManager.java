package io.nuls.api.manager;

import io.nuls.api.db.MongoDBService;
import io.nuls.api.model.po.config.ConfigBean;
import io.nuls.tools.core.annotation.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 链管理器，负责本地节点多链的初始化、销毁、动态配置
 *
 * @author captain
 * @version 1.0
 * @date 19-2-26 下午1:43
 */
public class ChainManager {

    /**
     * 链配置信息
     */
    private static Map<Integer, ConfigBean> configBeanMap = new ConcurrentHashMap<>();

    /**
     * 存放每条链对应的mangoDBSevice
     */
    private static Map<Integer, MongoDBService> dbServiceMap = new ConcurrentHashMap<>();



    /**
     * 首先明确单条链的启动过程
     * 优先读取数据库中缓存的配置文件，根据配置文件中配置的链ID
     */
    public void runChain() {

    }

    public static ConfigBean getConfigBean(int chainId) {
        return configBeanMap.get(chainId);
    }

    public static void addConfigBean(int chainId, ConfigBean configBean) {
        configBeanMap.put(chainId, configBean);
    }

    public static MongoDBService getDBService(int chainID) {
        return dbServiceMap.get(chainID);
    }

    public static void addDBService(int chainID, MongoDBService dbService) {
        dbServiceMap.put(chainID, dbService);
    }

    public static Map<Integer, ConfigBean> getConfigBeanMap() {
        return configBeanMap;
    }


}
