package io.nuls.poc.storage;

import io.nuls.poc.model.bo.config.ConfigBean;

import java.util.Map;

/**
 * @author tag
 * 2018/11/8
 * */
public interface ConfigeService {
    /**
     * 保存指定链的配置信息
     * @param bean     配置类
     * @param chainID  链ID
     * */
    public boolean save(ConfigBean bean,int chainID)throws Exception;

    /**
     * 查询某条链的配置信息
     * @param chainID 链ID
     * */
    public ConfigBean get(int chainID);

    /**
     * 删除某条链的配置信息
     * @param chainID 链ID
     * */
    public boolean delete(int chainID);

    /**
     * 获取当前节点所有的链信息
     * */
    public Map<Integer,ConfigBean> getList();
}
