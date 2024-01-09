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
 * Chain management,Responsible for initializing each chain,working,start-up,Parameter maintenance, etc
 * Chain management class, responsible for the initialization, operation, start-up, parameter maintenance of each chain, etc.
 *
 */
@Component
public class ConfigManager {

    @Autowired
    private NulsCoresConfig config;

    /**
     * Initialize and start the chain
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
            /*Register transaction processor*/
            boolean regSuccess = RegisterHelper.registerTx(chainId, ProtocolGroupManager.getCurrentProtocol(chainId));
            if (!regSuccess) {
                LOG.error("RegisterHelper.registerTx fail..");
                System.exit(-1);
            }
            LOG.info("regTxRpc complete.....");
            //Registration related transactions
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
     * Read configuration file to create and initialize chain
     * Read the configuration file to create and initialize the chain
     */
    private Map<Integer, ConfigBean> configChain() {
        try {
            /*
            Read database chain information configuration
            Read database chain information configuration
             */
            //Map<Integer, ConfigBean> configMap = configService.getList();// Cancel Persistenceconfig
            Map<Integer, ConfigBean> configMap = new HashMap<>();

            /*
            If the system is running for the first time and there is no storage chain information in the local database, it is necessary to read the main chain configuration information from the configuration file
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

                // Cancel Persistenceconfig
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
