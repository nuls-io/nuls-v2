package io.nuls.poc.storage.impl;

import io.nuls.db.model.Entry;
import io.nuls.db.service.RocksDBService;
import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.poc.storage.ConfigService;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.utils.manager.ConfigManager;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Service;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.data.ObjectUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 配置信息存储管理类
 * Configuration Information Storage Management Class
 *
 * @author tag
 * 2018/11/8
 * */
@Service
public class ConfigServiceImpl implements ConfigService, InitializingBean {
    @Override
    public boolean save(ConfigBean bean, int chainID) throws Exception{
        if(bean == null){
            return  false;
        }
        boolean dbSuccess = RocksDBService.put(ConsensusConstant.DB_NAME_CONSUME_CONGIF, ByteUtils.intToBytes(chainID), ObjectUtils.objectToBytes(bean));
        if(!dbSuccess){
            return false;
        }
        ConfigManager.config_map.put(chainID,bean);
        return true;
    }

    @Override
    public ConfigBean get(int chainID) {
        try {
            byte[] value = RocksDBService.get(ConsensusConstant.DB_NAME_CONSUME_CONGIF,ByteUtils.intToBytes(chainID));
            return ObjectUtils.bytesToObject(value);
        }catch (Exception e){
            Log.error(e);
            return null;
        }
    }

    @Override
    public boolean delete(int chainID) {
        try {
            boolean dbSuccess = RocksDBService.delete(ConsensusConstant.DB_NAME_CONSUME_CONGIF,ByteUtils.intToBytes(chainID));
            if(!dbSuccess){
                return false;
            }
            ConfigManager.config_map.remove(chainID);
            return true;
        }catch (Exception e){
            Log.error(e);
            return  false;
        }
    }

    @Override
    public Map<Integer, ConfigBean> getList() {
        try {
            List<Entry<byte[], byte[]>> list = RocksDBService.entryList(ConsensusConstant.DB_NAME_CONSUME_CONGIF);
            Map<Integer, ConfigBean> configBeanMap = new HashMap<>();
            for (Entry<byte[], byte[]>entry:list) {
                int key = ByteUtils.bytesToInt(entry.getKey());
                ConfigBean value = ObjectUtils.bytesToObject(entry.getValue());
                configBeanMap.put(key,value);
            }
            return configBeanMap;
        }catch (Exception e){
            Log.error(e);
            return null;
        }
    }

    @Override
    public void afterPropertiesSet() throws NulsException {
        try {
            RocksDBService.createTable(ConsensusConstant.DB_NAME_CONSUME_CONGIF);
        }catch (Exception e){
            Log.error(e);
            throw new NulsException(e);
        }
    }
}
