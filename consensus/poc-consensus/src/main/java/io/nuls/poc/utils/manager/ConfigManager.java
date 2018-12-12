package io.nuls.poc.utils.manager;

import io.nuls.poc.model.bo.config.ConfigBean;
import io.nuls.poc.model.bo.config.ConfigItem;
import io.nuls.poc.storage.ConfigService;
import io.nuls.tools.core.ioc.SpringLiteContext;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 共识模块配置文件管理类
 * Consensus Module Profile Management Class
 *
 * @author tag
 * 2018/11/20
 * */
public class ConfigManager {
    /**
     * 配置参数是否可修改
     * param_name  -  是否可修改标识
     * */
    public static  Map<String,Boolean> param_modify = new HashMap<>();

    /**
     * 初始化配置信息
     * Initialize configuration information
     *
     * @param items       配置参数列表
     * */
    public static ConfigBean initManager(List<ConfigItem> items) throws Exception{
        ConfigBean bean = new ConfigBean();
        Class beanClass = bean.getClass();
        Field field;
        /*
        通过反射设置bean属性值
        Setting bean attribute values by reflection
         */
        for (ConfigItem item : items) {
            param_modify.put(item.getKey(),item.isReadOnly());
            field = beanClass.getDeclaredField(item.getKey());
            field.setAccessible(true);
            if("java.math.BigInteger".equals(field.getType().getName())){
                field.set(bean, new BigInteger((String) item.getValue()));
            }else{
                field.set(bean,item.getValue());
            }
        }
        /*
        保存配置信息到数据库
        Save configuration information to database
        */
        ConfigService configService = SpringLiteContext.getBean(ConfigService.class);
        boolean saveSuccess = configService.save(bean,bean.getChainId());
        if(saveSuccess){
            return bean;
        }
        return null;
    }
}
