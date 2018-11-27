package io.nuls.poc.storage;

import io.nuls.poc.model.po.PunishLogPo;

import java.util.List;

/**
 * 惩罚管理类
 * @author tag
 * */
public interface PunishStorageService {
    /**
     * 保存
     * @param po 红/黄牌对象
     * */
    boolean save(PunishLogPo po,int chainID);

    /**
     * 删除
     * */
    boolean delete(byte[] key,int chainID);

    /**
     * 获取列表
     * */
    List<PunishLogPo> getPunishList(int chainID) throws Exception;
}
