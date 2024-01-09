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
import io.nuls.common.CommonContext;
import io.nuls.common.ConfigBean;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.io.IoUtils;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.protocol.manager.ContextManager;

import java.io.File;
import java.util.ArrayList;
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

    private static List<ProtocolVersion> versions;

    static {
        try {
            versions = JSONUtils.json2list(IoUtils.read("protocol" + File.separator + PROTOCOL_CONFIG_FILE), ProtocolVersion.class);
        } catch (Exception e) {
            Log.error(e);
            System.exit(1);
        }
    }


    /**
     * 加载配置文件
     *
     */
    public static void load() {
        List<ConfigBean> list = new ArrayList<>(CommonContext.CONFIG_BEAN_MAP.values());
        if (list == null || list.size() == 0) {
            loadDefault();
        } else {
            for (ConfigBean chainParameters : list) {
                ContextManager.init(chainParameters, versions);
            }
        }
    }

    /**
     * 加载默认配置文件
     *
     */
    private static void loadDefault() {
        ContextManager.init(protocolConfig, versions);
    }

}
