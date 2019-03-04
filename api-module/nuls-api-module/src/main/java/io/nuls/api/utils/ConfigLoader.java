/*
 *
 *  * MIT License
 *  * Copyright (c) 2017-2019 nuls.io
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package io.nuls.api.utils;

import io.nuls.api.constant.Constant;
import io.nuls.api.manager.ChainManager;
import io.nuls.api.model.po.config.ConfigBean;
import io.nuls.api.service.ConfigStorageService;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.io.IoUtils;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.parse.config.ConfigItem;

import java.util.List;
import java.util.Map;

import static io.nuls.api.constant.Constant.MODULES_CONFIG_FILE;

/**
 * 配置加载器
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 下午1:37
 */
public class ConfigLoader {

    private static ConfigStorageService configService = SpringLiteContext.getBean(ConfigStorageService.class);

    /**
     * 加载配置文件
     *
     * @throws Exception
     */
    public static void load() throws Exception {
        Map<Integer, ConfigBean> configMap = configService.getList();
        if (configMap != null && configMap.isEmpty()) {
            for (ConfigBean bean : configMap.values()) {
                ChainManager.addConfigBean(bean.getChainID(), bean);
            }
        } else {
            loadDefault();
        }
    }

    private static void loadDefault() throws Exception {
        String configJson = IoUtils.read(MODULES_CONFIG_FILE);
        List<ConfigItem> configItems = JSONUtils.json2list(configJson, ConfigItem.class);

        ConfigBean bean = new ConfigBean();
        for (ConfigItem item : configItems) {
            if (item.getName().equals(Constant.CHAIN_ID)) {
                bean.setChainID(Integer.parseInt(item.getValue()));
            } else if (item.getName().equals(Constant.DB_IP)) {
                bean.setDbIp(item.getValue());
            } else if (item.getName().equals(Constant.DB_PORT)) {
                bean.setPort(Integer.parseInt(item.getValue()));
            }
        }

        ChainManager.addConfigBean(bean.getChainID(), bean);
    }
}
