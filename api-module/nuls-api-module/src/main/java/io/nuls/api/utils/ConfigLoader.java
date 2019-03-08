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

import io.nuls.api.ApiContext;
import io.nuls.api.constant.ApiConstant;
import io.nuls.tools.io.IoUtils;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.parse.config.ConfigItem;

import java.util.List;

import static io.nuls.api.constant.ApiConstant.MODULES_CONFIG_FILE;

/**
 * 配置加载器
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 下午1:37
 */
public class ConfigLoader {

//    private static ConfigStorageService configService = SpringLiteContext.getBean(ConfigStorageService.class);

    /**
     * 加载配置文件
     *
     * @throws Exception
     */
//    public static void load() throws Exception {
//        Map<Integer, ConfigBean> configMap = configService.getList();
//        if (configMap != null && configMap.isEmpty()) {
//            for (ConfigBean bean : configMap.values()) {
//                ChainManager.addConfigBean(bean.getChainId(), bean);
//            }
//        } else {
//            loadDefault();
//        }
//    }
    public static void load() throws Exception {
        String configJson = IoUtils.read(MODULES_CONFIG_FILE);
        List<ConfigItem> configItems = JSONUtils.json2list(configJson, ConfigItem.class);

        for (ConfigItem item : configItems) {
            if (item.getName().equals(ApiConstant.CHAIN_ID)) {
                ApiContext.defaultChainId = Integer.parseInt(item.getValue());
            } else if (item.getName().equals(ApiConstant.ASSET_ID)) {
                ApiContext.defaultAssetId = Integer.parseInt(item.getValue());
            } else if (item.getName().equals(ApiConstant.DB_IP)) {
                ApiContext.dbIp = item.getValue();
            } else if (item.getName().equals(ApiConstant.DB_PORT)) {
                ApiContext.port = Integer.parseInt(item.getValue());
            }
        }

//        ConfigBean bean = new ConfigBean();
//        for (ConfigItem item : configItems) {
//            if (item.getName().equals(ApiConstant.CHAIN_ID)) {
//                bean.setChainId(Integer.parseInt(item.getValue()));
//            } else if (item.getName().equals(ApiConstant.DB_IP)) {
//                bean.setDbIp(item.getValue());
//            } else if (item.getName().equals(ApiConstant.DB_PORT)) {
//                bean.setPort(Integer.parseInt(item.getValue()));
//            }
//        }
//
//        ChainManager.addConfigBean(bean.getChainId(), bean);
    }
}
