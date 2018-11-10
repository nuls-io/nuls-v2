package io.nuls.poc.utils.manager;

import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.poc.model.bo.config.ConfigItem;
import io.nuls.poc.storage.ConfigeService;
import io.nuls.tools.core.ioc.SpringLiteContext;

import java.lang.reflect.Field;
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
    public static Map<Integer, ConfigBean> config_map = new HashMap<>();

    /**
     * 配置参数是否可修改
     * param_name  -  是否可修改标识
     * */
    public static  Map<String,Boolean> param_modify = new HashMap<>();

    /**
     * 节点各条链打包状态
     * */
    public static Map<Integer,Boolean> packing_status = new HashMap<>();

    public static void initManager(List<ConfigItem> items, int chain_id) throws Exception{
        ConfigBean bean = new ConfigBean();
        Class beanClass = bean.getClass();
        Field field = null;
        //通过反射设置bean属性值
        for (ConfigItem item : items) {
            param_modify.put(item.getKey(),item.isReadOnly());
            field = beanClass.getDeclaredField(item.getKey());
            field.setAccessible(true);
            field.set(bean,item.getValue());
        }
        config_map.put(chain_id,bean);
        packing_status.put(chain_id,false);
        //保存配置信息到数据库
        ConfigeService configeService = SpringLiteContext.getBean(ConfigeService.class);
        configeService.save(bean,chain_id);
    }
}
