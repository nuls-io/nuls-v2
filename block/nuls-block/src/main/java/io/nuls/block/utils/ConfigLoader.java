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

package io.nuls.block.utils;

import io.nuls.block.constant.ConfigConstant;
import io.nuls.block.manager.ConfigManager;
import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainParameters;
import io.nuls.block.storage.ParametersStorageService;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.io.IoUtils;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.parse.config.ConfigItem;
import io.nuls.tools.protocol.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static io.nuls.block.constant.Constant.*;

/**
 * 配置加载器
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 下午1:37
 */
public class ConfigLoader {

    private static ParametersStorageService service = SpringLiteContext.getBean(ParametersStorageService.class);

    /**
     * 加载配置文件
     *
     * @throws Exception
     */
    public static void load() throws Exception {
        List<ChainParameters> list = service.getList();
        if (list == null || list.size() == 0) {
            loadDefault();
        } else {
            for (ChainParameters chainParameters : list) {
                int chainId = chainParameters.getChainId();
                String protocolConfigJson = service.getProtocolConfigJson(chainId);
                List<ProtocolConfigJson> protocolConfigs = JSONUtils.json2list(protocolConfigJson, ProtocolConfigJson.class);
                protocolConfigs.sort(PROTOCOL_CONFIG_COMPARATOR);
                Map<Short, Protocol> protocolMap = ProtocolLoader.load(protocolConfigs);
                ContextManager.init(chainParameters, protocolMap);
            }
        }
    }

    /**
     * 加载默认配置文件
     *
     * @throws Exception
     */
    private static void loadDefault() throws Exception {
        String json = IoUtils.read(PROTOCOL_CONFIG_FILE);
        List<ProtocolConfigJson> protocolConfigs = JSONUtils.json2list(json, ProtocolConfigJson.class);
        protocolConfigs.sort(PROTOCOL_CONFIG_COMPARATOR);
        Map<Short, Protocol> protocolMap = ProtocolLoader.load(protocolConfigs);
        String configJson = IoUtils.read(MODULES_CONFIG_FILE);
        List<ConfigItem> configItems = JSONUtils.json2list(configJson, ConfigItem.class);
        Map<String, ConfigItem> map = new HashMap<>(configItems.size());
        configItems.forEach(e -> map.put(e.getName(), e));
        int chainId = Integer.parseInt(map.get(ConfigConstant.CHAIN_ID).getValue());
        ConfigManager.add(chainId, map);
        ChainParameters po = new ChainParameters();
        po.init(map);
        ContextManager.init(po, protocolMap);
        service.save(po, chainId);
        service.saveProtocolConfigJson(json, chainId);
    }

}
