package io.nuls.poc.utils.manager;

import io.nuls.poc.model.bo.Chain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 链管理类
 * Chain management class
 *
 * @author tag
 * 2018/12/4
 * */
public class ChainManager {

    private Map<Integer, Chain> chainMap = new ConcurrentHashMap<>();

    public static ChainManager instance = null;
    private ChainManager() { }
    private static Integer LOCK = 0;
    public static ChainManager getInstance() {
        synchronized (LOCK) {
            if (instance == null) {
                instance = new ChainManager();
            }
            return instance;
        }
    }

    /**
     * 添加一条新链
     * Add a new chain
     *
     * @param chainId  链ID/chain id
     * @param chain    链对象/chain object
     * */
    public void addChain(int chainId,Chain chain){

    }

    /**
     * 删除一条链
     * Delete a chain
     *
     * @param chainId 链ID/chain id
     * */
    public void stopChain(int chainId){

    }


}
