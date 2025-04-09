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
 * The transaction has been verified by the transaction management module(Take it from here when packing)
 * Waiting for a packaged transaction pool
 *
 * @author: Charlie
 * @date: 2018/11/13
 */
@Component
public class PackablePool {

    @Autowired
    private UnconfirmedTxStorageService unconfirmedTxStorageService;

    /**
     * Add the transaction to the forefront of the queue to be packaged, and retrieve it first during packaging
     * Add the transaction to the front of the queue to be packed, and take it out first when it is packed
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
     * Only returnhash No need to return itmapin
     * @param chain
     * @param tx
     * @return
     */
    public boolean offerFirstOnlyHash(Chain chain, Transaction tx) {
        ByteArrayWrapper hash = new ByteArrayWrapper(tx.getHash().getBytes());
        synchronized (hash) {
            if (chain.getPackableHashQueue().offerFirst(hash)) {
                return true;
            }
        }
        chain.getLogger().error("PackableHashQueue offerFirst false");
        return false;
    }

    /**
     * Add the transaction to the end of the queue to be packaged
     * Add the transaction to the end of the queue to be packed
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
     * Retrieve a transaction from the queue to be packaged
     * Gets a transaction from the queue to be packaged
     * <p>
     * 1.Remove from queuehashThen go aheadmapObtain transactions in the middle
     * 2.IfmapThere is no indication that it has been packaged and confirmed, and then the next one will be taken until a transaction is obtained or the queue is empty
     * <p>
     * 1.Fetch the hash from the queue, and then fetch the transaction from the map
     * 2.If the map does not indicate that it has been packaged for confirmation,
     * then take one down until a transaction is obtained, or if the queue is empty
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
     * Get and remove the last element of this dual end queue；If this dual end queue is empty, return null
     * Gets and removes the last element of the other double-ended queue; If this double-ended queue is empty, null is returned
     *
     * Unpackaged transactions need to be reprocessed during protocol upgrade
     * When the agreement is upgraded, unpackaged transactions need to be reprocessed
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

    /**
     * Removes the confirmed transactions from the packable transaction map.
     *
     * This method iterates through a list of transaction hashes that have been confirmed,
     * wraps each hash into a ByteArrayWrapper object, and removes the corresponding transaction
     * from the packable transaction map of the given chain. This ensures that confirmed
     * transactions are no longer considered for packaging.
     *
     * @param chain The blockchain instance containing the packable transaction map.
     * @param txHashs A list of byte arrays representing the hashes of confirmed transactions to be removed.
     */
    public void clearConfirmedTxs(Chain chain, List<byte[]> txHashs) {
        Map<ByteArrayWrapper, Transaction> map = chain.getPackableTxMap();
        for (byte[] hash : txHashs) {
            ByteArrayWrapper wrapper = new ByteArrayWrapper(hash);
            map.remove(wrapper);
        }
    }

    /**
     * Removes an invalid transaction from the packable transaction map.
     *
     * This method retrieves the packable transaction map from the given chain object,
     * wraps the transaction hash into a ByteArrayWrapper, and removes the corresponding
     * transaction from the map. This ensures that invalid transactions are no longer
     * considered for packaging.
     *
     * @param chain The blockchain instance containing the packable transaction map.
     * @param tx The transaction to be removed from the packable transaction map.
     */
    public void removeInvalidTxFromMap(Chain chain, Transaction tx) {
        Map<ByteArrayWrapper, Transaction> map = chain.getPackableTxMap();
        ByteArrayWrapper wrapper = new ByteArrayWrapper(tx.getHash().getBytes());
        map.remove(wrapper);
    }

    /**
     * Determine whether the transaction is in the queue to be packagedhash queueIf the transaction exists in the package to be packagedmapin, Not necessarily present inhashqueue.
     * Determine if the transaction is in the hash queue to be packaged;
     * if the transaction exists in the map, it does not necessarily exist in the hash queue to be packaged.
     *
     *
     * @param chain
     * @param tx
     * @return
     */
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
