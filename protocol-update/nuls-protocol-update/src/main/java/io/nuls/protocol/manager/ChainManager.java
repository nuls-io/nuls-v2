package io.nuls.protocol.manager;

/**
 * 链管理器，负责本地节点多链的初始化、销毁、动态配置
 *
 * @author captain
 * @version 1.0
 * @date 19-2-26 下午1:43
 */
public class ChainManager {

    /**
     * 首先明确单条链的启动过程
     *      优先读取数据库中缓存的配置JSON，根据配置JSON初始化配置(可能包含多条链的配置)
     *      如果数据库中没有缓存的配置，则加载默认位置的配置文件读取配置JSON，并进行默认链的初始化
     *
     */
    public static void init() {

        //更新运行状态
    }

    public static void start() {

    }

    public static void destroy() {

    }

    public static void setParameter() {

    }

}
