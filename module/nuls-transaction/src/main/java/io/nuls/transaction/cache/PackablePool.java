package io.nuls.transaction.cache;

import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.ByteArrayWrapper;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.storage.UnconfirmedTxStorageService;

import java.util.List;
import java.util.Map;

/**
 * 交易已完成交易管理模块的校验(打包的时候从这里取)
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
@Component
public class PackablePool {

    @Autowired
    private UnconfirmedTxStorageService unconfirmedTxStorageService;

    /**
     * 将交易加入到待打包队列最前端，打包时最先取出
     *
     * @param chain
     * @param tx
     * @return
     */
    public boolean offerFirst(Chain chain, Transaction tx) {
        ByteArrayWrapper hash = new ByteArrayWrapper(tx.getHash().getBytes());
        synchronized (hash) {
            if (chain.getPackableHashQueue().offerFirst(hash)) {
                chain.getPackableTxMap().put(hash, tx);
                return true;
            }
        }
        chain.getLogger().error("PackableHashQueue offerFirst false");
        return false;
    }

    /**
     * 将交易加入到待打包队列队尾
     *
     * @param chain
     * @param tx
     * @return
     */
    public boolean add(Chain chain, Transaction tx) {
        ByteArrayWrapper hash = new ByteArrayWrapper(tx.getHash().getBytes());
        synchronized (hash) {
            if (chain.getPackableHashQueue().offer(hash)) {
                chain.getPackableTxMap().put(hash, tx);
                return true;
            }
        }
        chain.getLogger().error("PackableHashQueue add false");
        return false;
    }

    /**
     * 从待打包队列获取一笔交易
     * 1.从队列中取出hash，然后再去map中获取交易
     * 2.如果map没有说明已经被打包确认，然后接着拿下一个，直到获取到一个交易，或者队列为空
     *
     * @param chain
     * @return
     */
    public Transaction poll(Chain chain) {
        while (true) {
            ByteArrayWrapper hash = chain.getPackableHashQueue().poll();
            if (null == hash) {
                return null;
            }
            synchronized (hash) {
                Transaction tx = chain.getPackableTxMap().get(hash);
                if (null != tx) {
                    return tx;
                } else {
                    unconfirmedTxStorageService.removeTx(chain.getChainId(), hash.getBytes());
                }
            }
        }
    }

    /**
     * 获取并移除此双端队列的最后一个元素；如果此双端队列为空，则返回 null
     * 协议升级时需要重新处理未打包的交易
     *
     * @param chain
     * @return
     */
    public Transaction pollLast(Chain chain) {
        while (true) {
            ByteArrayWrapper hash = chain.getPackableHashQueue().pollLast();
            if (null == hash) {
                return null;
            }
            synchronized (hash) {
                Transaction tx = chain.getPackableTxMap().get(hash);
                if (null != tx) {
                    return tx;
                } else {
                    unconfirmedTxStorageService.removeTx(chain.getChainId(), hash.getBytes());
                }
            }
        }
    }

    public void clearConfirmedTxs(Chain chain, List<byte[]> txHashs) {
        Map<ByteArrayWrapper, Transaction> map = chain.getPackableTxMap();
        for (byte[] hash : txHashs) {
            ByteArrayWrapper wrapper = new ByteArrayWrapper(hash);
            map.remove(wrapper);
        }
    }

    public boolean exist(Chain chain, Transaction tx) {
        ByteArrayWrapper hash = new ByteArrayWrapper(tx.getHash().getBytes());
        return chain.getPackableHashQueue().contains(hash);
    }

    public int packableHashQueueSize(Chain chain) {
        return chain.getPackableHashQueue().size();
    }

    public int packableTxMapSize(Chain chain) {
        return chain.getPackableTxMap().size();
    }

    public void clear(Chain chain) {
        chain.getPackableHashQueue().clear();
    }

}
