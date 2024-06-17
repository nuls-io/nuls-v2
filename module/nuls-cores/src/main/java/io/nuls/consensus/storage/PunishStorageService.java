package io.nuls.consensus.storage;

import io.nuls.consensus.model.po.PunishLogPo;

import java.util.List;

/**
 * Punish storage management class
 * Penalty Storage Management Class
 *
 * @author tag
 * */
public interface PunishStorageService {
    /**
     * preserve
     * save
     *
     * @param po          Red and yellow card objects/Red and yellow card objects
     * @param chainID     chainID/chain id
     * @return boolean
     * */
    boolean save(PunishLogPo po,int chainID);

    /**
     * delete
     * delete
     *
     * @param key      key/key
     * @param chainID  chainID/chain id
     * @return  boolean
     * */
    boolean delete(byte[] key,int chainID);

    /**
     * Get List
     * get list
     *
     * @param chainID  chainID/chain id
     * @return List<PunishLogPo>
     * @exception Exception Data query failed
     * */
    List<PunishLogPo> getPunishList(int chainID) throws Exception;
}
