/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.contract.manager;

import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.model.bo.config.ConfigItem;
import io.nuls.contract.storage.ConfigStorageService;
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
 */
public class ConfigManager {
    /**
     * 配置参数是否可修改
     * param_name  -  是否可修改标识
     */
    public static Map<String, Boolean> param_modify = new HashMap<>();

    /**
     * 初始化配置信息
     * Initialize configuration information
     *
     * @param items 配置参数列表
     */
    public static ConfigBean initManager(List<ConfigItem> items) throws Exception {
        ConfigBean bean = new ConfigBean();
        Class beanClass = bean.getClass();
        Field field;
        /*
        通过反射设置bean属性值
        Setting bean attribute values by reflection
         */
        for (ConfigItem item : items) {
            param_modify.put(item.getKey(), item.isReadOnly());
            field = beanClass.getDeclaredField(item.getKey());
            field.setAccessible(true);
            if ("java.math.BigInteger".equals(field.getType().getName())) {
                field.set(bean, new BigInteger((String) item.getValue()));
            } else {
                field.set(bean, item.getValue());
            }
        }
        /*
        保存配置信息到数据库
        Save configuration information to database
        */
        ConfigStorageService configService = SpringLiteContext.getBean(ConfigStorageService.class);
        boolean saveSuccess = configService.save(bean, bean.getChainId());
        if (saveSuccess) {
            return bean;
        }
        return null;
    }
}
