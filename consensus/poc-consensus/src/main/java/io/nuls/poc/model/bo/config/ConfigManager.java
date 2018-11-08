package io.nuls.poc.model.bo.config;

import io.nuls.poc.storage.ConfigeService;
import io.nuls.poc.utils.ConsensusConstant;
import io.nuls.tools.core.ioc.SpringLiteContext;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigManager {
    /**
     * 多链配置文件配置信息
     * chain_id  -  ConfigBean
     * 当运行一条新的子链时需要把配置信息保存到该MAP中并存入数据库，当系统刚启动时，
     * 先从数据库读出所有链的配置信息放入该MAP中，如果数据库中没有记录则读取配置文件
     * 加载主链的配置信息
     * */
    public static Map<Integer,ConfigBean> config_map = new HashMap<>();

    /**
     * 配置参数是否可修改
     * param_name  -  是否可修改标识
     * */
    public static  Map<String,Boolean> param_modify = new HashMap<>();

    public static void initManager(List<ConfigItem> items,int chain_id) throws Exception{
        ConfigBean bean = new ConfigBean();
        for (ConfigItem item : items) {
            param_modify.put(item.getKey(),item.isReadOnly());
            if(item.getKey().equals(ConsensusConstant.PARAM_BLOCK_SIZE)){
                bean.setBlock_size((Integer) item.getValue());
                continue;
            }
            if(item.getKey().equals(ConsensusConstant.PARAM_COINBASE_UNLOCK_HEIGHT)){
                bean.setCoinbase_unlock_height((Integer) item.getValue());
                continue;
            }
            if(item.getKey().equals(ConsensusConstant.PARAM_COMMISSION_MAX)){
                bean.setCommission_max((Long) item.getValue());
                continue;
            }
            if(item.getKey().equals(ConsensusConstant.PARAM_COMMISSION_MIN)){
                bean.setCommission_min((Long) item.getValue());
                continue;
            }
            if(item.getKey().equals(ConsensusConstant.PARAM_COMMISSION_RATE_MAX)){
                bean.setCommissionRate_max((Double) item.getValue());
                continue;
            }
            if(item.getKey().equals(ConsensusConstant.PARAM_COMMISSION_RATE_MIN)){
                bean.setCommissionRate_min((Double) item.getValue());
                continue;
            }
            if(item.getKey().equals(ConsensusConstant.PARAM_DEPOSIT_MAX)){
                bean.setDeposit_max((Long) item.getValue());
                continue;
            }
            if(item.getKey().equals(ConsensusConstant.PARAM_DEPOSIT_MIN)){
                bean.setDeposit_min((Long) item.getValue());
                continue;
            }
            if(item.getKey().equals(ConsensusConstant.PARAM_PACKING_AMOUNT)){
                bean.setPacking_amount((Long) item.getValue());
                continue;
            }
            if(item.getKey().equals(ConsensusConstant.PARAM_PACKING_INTERVAL)){
                bean.setPacking_interval((Long) item.getValue());
                continue;
            }
            if(item.getKey().equals(ConsensusConstant.PARAM_RED_PUBLISH_LOCKTIME)){
                bean.setRedPublish_lockTime((Long) item.getValue());
                continue;
            }
            if(item.getKey().equals(ConsensusConstant.PARAM_STOP_AGENT_LOCKTIME)){
                bean.setStopAgent_lockTime((Long) item.getValue());
                continue;
            }
        }
        config_map.put(chain_id,bean);
        //保存配置信息到数据库
        ConfigeService configeService = SpringLiteContext.getBean(ConfigeService.class);
        configeService.save(bean,chain_id);
    }
}
