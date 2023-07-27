/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.common;

import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.base.protocol.ProtocolLoader;
import io.nuls.base.protocol.RegisterHelper;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.constant.NulsCrossChainConstant;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * 链管理类,负责各条链的初始化,运行,启动,参数维护等
 * Chain management class, responsible for the initialization, operation, start-up, parameter maintenance of each chain, etc.
 *
 */
@Component
public class ConfigManager {

    @Autowired
    private NulsCoresConfig config;

    /**
     * 初始化并启动链
     * Initialize and start the chain
     */
    public void init() throws Exception {
        Map<Integer, ConfigBean> configMap = configChain();
        if (configMap == null || configMap.size() == 0) {
            return;
        }
        CommonContext.CONFIG_BEAN_MAP.clear();
        CommonContext.CONFIG_BEAN_MAP.putAll(configMap);
    }

    public void registerProtocol() throws Exception {
        Map<Integer, ConfigBean> configMap = CommonContext.CONFIG_BEAN_MAP;
        if (configMap == null || configMap.size() == 0) {
            return;
        }
        for (Map.Entry<Integer, ConfigBean> entry : configMap.entrySet()) {
            int chainId = entry.getKey();
            ProtocolLoader.load(chainId);
            /*注册交易处理器*/
            boolean regSuccess = RegisterHelper.registerTx(chainId, ProtocolGroupManager.getCurrentProtocol(chainId));
            if (!regSuccess) {
                LOG.error("RegisterHelper.registerTx fail..");
                System.exit(-1);
            }
            LOG.info("regTxRpc complete.....");
            //注册相关交易
            regSuccess = RegisterHelper.registerProtocol(chainId);
            if (!regSuccess) {
                LOG.error("RegisterHelper.registerProtocol fail..");
                System.exit(-1);
            }
            LOG.info("register protocol ...");
            RegisterHelper.registerMsg(ProtocolGroupManager.getOneProtocol());
            LOG.info("register msg ...");
        }
    }

    /**
     * 读取配置文件创建并初始化链
     * Read the configuration file to create and initialize the chain
     */
    private Map<Integer, ConfigBean> configChain() {
        try {
            /*
            读取数据库链信息配置
            Read database chain information configuration
             */
            //Map<Integer, ConfigBean> configMap = configService.getList();// 取消持久化config
            Map<Integer, ConfigBean> configMap = new HashMap<>();

            /*
            如果系统是第一次运行，则本地数据库没有存储链信息，此时需要从配置文件读取主链配置信息
            If the system is running for the first time, the local database does not have chain information,
            and the main chain configuration information needs to be read from the configuration file at this time.
            */
            if (configMap.isEmpty()) {
                ConfigBean configBean = config;
                if(config.getVerifiers() != null){
                    configBean.setVerifierSet(new HashSet<>(Arrays.asList(config.getVerifiers().split(NulsCrossChainConstant.VERIFIER_SPLIT))));
                }else{
                    configBean.setVerifierSet(new HashSet<>());
                }

                // 取消持久化config
                //boolean saveSuccess = configService.save(configBean, configBean.getChainId());
                //if(saveSuccess){
                //    configMap.put(configBean.getChainId(), configBean);
                //}
                configMap.put(configBean.getChainId(), configBean);
            }
            return configMap;
        } catch (Exception e) {
            LOG.error(e);
            return null;
        }
    }

}
