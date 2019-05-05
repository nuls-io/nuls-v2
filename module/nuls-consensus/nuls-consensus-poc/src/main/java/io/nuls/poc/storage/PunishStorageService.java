package io.nuls.poc.storage;

import io.nuls.poc.model.po.PunishLogPo;

import java.util.List;

/**
 * 惩罚存储管理类
 * Penalty Storage Management Class
 *
 * @author tag
 * */
public interface PunishStorageService {
    /**
     * 保存
     * save
     *
     * @param po          红黄牌对象/Red and yellow card objects
     * @param chainID     链ID/chain id
     * @return boolean
     * */
    boolean save(PunishLogPo po,int chainID);

    /**
     * 删除
     * delete
     *
     * @param key      键/key
     * @param chainID  链ID/chain id
     * @return  boolean
     * */
    boolean delete(byte[] key,int chainID);

    /**
     * 获取列表
     * get list
     *
     * @param chainID  链ID/chain id
     * @return List<PunishLogPo>
     * @exception Exception 数据查询失败
     * */
    List<PunishLogPo> getPunishList(int chainID) throws Exception;
}
