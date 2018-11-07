package io.nuls.poc.model.bo.config;

import java.util.HashMap;
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
}
