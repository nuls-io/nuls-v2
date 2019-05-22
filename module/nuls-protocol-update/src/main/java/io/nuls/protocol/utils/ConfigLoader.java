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

package io.nuls.protocol.utils;

import io.nuls.base.basic.ProtocolVersion;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.io.IoUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ChainParameters;
import io.nuls.protocol.storage.ParametersStorageService;

import java.util.List;

import static io.nuls.protocol.ProtocolUpdateBootstrap.protocolConfig;
import static io.nuls.protocol.constant.Constant.PROTOCOL_CONFIG_FILE;

/**
 * 配置加载器
 *
 * @author captain
 * @version 1.0
 * @date 18-11-8 下午1:37
 */
@Component
public class ConfigLoader {

    @Autowired
    private static ParametersStorageService service;

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
                String versionJson = service.getVersionJson(chainId);
                List<ProtocolVersion> versions = JSONUtils.json2list(versionJson, ProtocolVersion.class);
                ContextManager.init(chainParameters, versions);
            }

        }
    }

    /**
     * 加载默认配置文件
     *
     * @throws Exception
     */
    private static void loadDefault() throws Exception {
        String versionJson = IoUtils.read(PROTOCOL_CONFIG_FILE);
        List<ProtocolVersion> versions = JSONUtils.json2list(versionJson, ProtocolVersion.class);
        int chainId = protocolConfig.getChainId();
        ContextManager.init(protocolConfig, versions);
        service.save(protocolConfig, chainId);
        service.saveVersionJson(versionJson, chainId);
    }

}
