/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuls.account.util.LoggerUtil;
import io.nuls.base.api.provider.Provider;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.protocol.ModuleHelper;
import io.nuls.common.*;
import io.nuls.contract.tx.SmartContractVersionChangeInvoker;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.config.ConfigurationLoader;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.InvokeBean;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.NulsCoresCmd;
import io.nuls.core.rpc.modulebootstrap.Module;
import io.nuls.core.rpc.modulebootstrap.NulsRpcModuleBootstrap;
import io.nuls.core.rpc.modulebootstrap.RpcModule;
import io.nuls.core.rpc.modulebootstrap.RpcModuleState;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.AddressPrefixDatas;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.rpc.upgrade.TxVersionChangeInvoker;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.*;

import static io.nuls.transaction.utils.LoggerUtil.LOG;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author: Charlie
 * @date: 2019/3/4
 */
@Component
public class NulsCoresBootstrap extends RpcModule {

    private static String[] args;
    @Autowired
    private NulsCoresConfig config;
    @Autowired
    private AddressPrefixDatas addressPrefixDatas;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private ConfigManager configManager;

    public static void main(String[] args) throws Exception {
        initSys();
        if (args == null || args.length == 0) {
            args = new String[]{"ws://" + HostInfo.getLocalIP() + ":7771"};
        }
        NulsCoresBootstrap.args = args;
        NulsRpcModuleBootstrap.run("io.nuls", args);
    }

    @Override
    public void init() {
        try {
            super.init();
            CommonVersionChangeInvoker.addProcess(TxVersionChangeInvoker.instance());
            CommonVersionChangeInvoker.addProcess(SmartContractVersionChangeInvoker.instance());
            //初始化配置项
            initCfg();
            ModuleHelper.init(this);
        } catch (Exception e) {
            LOG.error("Transaction init error!");
            LOG.error(e);
        }
    }

    @Override
    public boolean doStart() {
        try {
            configManager.init();
            NulsDateUtils.getInstance().start();
            LoggerUtil.LOG.info("Nuls-Core onDependenciesReady");
            LoggerUtil.LOG.info("START-SUCCESS");
            try {
                Collection<Object> list = SpringLiteContext.getAllBeanList();
                List<INulsCoresBootstrap> coreList = new ArrayList<>();
                for (Object object : list) {
                    if(object instanceof INulsCoresBootstrap) {
                        coreList.add((INulsCoresBootstrap) object);
                    }
                }
                // 按指定顺序执行异构链注册
                coreList.sort(new Comparator<INulsCoresBootstrap>() {
                    @Override
                    public int compare(INulsCoresBootstrap o1, INulsCoresBootstrap o2) {
                        if (o1.order() > o2.order()) {
                            return 1;
                        } else if (o1.order() < o2.order()) {
                            return -1;
                        }
                        return 0;
                    }
                });
                initCores(coreList);
                configManager.registerProtocol();
                runCores(coreList);
            } catch (Exception e) {
                LOG.error(e);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }


    @Override
    public void onDependenciesReady(Module module) {
    }

    @Override
    public RpcModuleState onDependenciesReady() {
        LOG.info("Transaction onDependenciesReady");
        NulsDateUtils.getInstance().start();
        CommonContext.START_BOOT.countDown();
        return RpcModuleState.Running;
    }

    @Override
    public RpcModuleState onDependenciesLoss(Module module) {
        return RpcModuleState.Ready;
    }

    @Override
    protected long getTryRuningTimeout() {
        return 60000;
    }

    @Override
    public Module[] declareDependent() {
        return new Module[]{
                Module.build(ModuleE.NC)
        };
    }

    @Override
    public Module moduleInfo() {
        return new Module(ModuleE.NC.abbr, "1.0");
    }


    private void initCfg() {
        try {
            Provider.ProviderType providerType = Provider.ProviderType.RPC;
            ServiceManager.init(config.getChainId(), providerType);
            /**
             * 地址工具初始化
             */
            AddressTool.init(addressPrefixDatas);
            AddressTool.addPrefix(config.getChainId(), config.getAddressPrefix());

            // 核心模块cmd集合
            List<BaseCmd> cmdList = SpringLiteContext.getBeanList(BaseCmd.class);
            for (BaseCmd cmd : cmdList) {
                Class<?> clazs = cmd.getClass();
                NulsCoresCmd nulsCoresCmd = clazs.getAnnotation(NulsCoresCmd.class);
                if (nulsCoresCmd == null) {
                    continue;
                }
                ModuleE module = nulsCoresCmd.module();
                String moduleAbbr = module.abbr;
                Method[] methods = clazs.getMethods();
                for (Method method : methods) {
                    CmdAnnotation annotation = method.getAnnotation(CmdAnnotation.class);
                    if (annotation == null) {
                        continue;
                    }
                    String cmdName = annotation.cmd();
                    //System.out.println(String.format(
                    //        "moduleName: %s, cmd: %s, class instance: %s, method: %s",
                    //        moduleAbbr, cmdName, cmd.getClass().getName(), method.getName()
                    //));
                    ResponseMessageProcessor.INVOKE_BEAN_MAP.put(moduleAbbr + "_" + cmdName, new InvokeBean(cmd, method));
                }
            }
            // 配置合并模块前，每个模块下的交易
            Object[][] txTypeModules = new Object[][] {
                    new Object[]{ModuleE.AC.abbr, new int[]{2, 3, 63, 64, 65}},
                    new Object[]{ModuleE.BL.abbr, new int[]{}},
                    new Object[]{ModuleE.CS.abbr, new int[]{1, 4, 5, 6, 7, 8, 9, 20, 21, 22, 23, 34}},
                    new Object[]{ModuleE.CM.abbr, new int[]{11, 12, 13, 14}},
                    new Object[]{ModuleE.CC.abbr, new int[]{10, 24, 25, 26, 60, 61}},
                    new Object[]{ModuleE.LG.abbr, new int[]{27}},
                    new Object[]{ModuleE.NW.abbr, new int[]{}},
                    new Object[]{ModuleE.PU.abbr, new int[]{}},
                    new Object[]{ModuleE.SC.abbr, new int[]{15, 16, 17, 18, 19}},
                    new Object[]{ModuleE.TX.abbr, new int[]{}}
            };
            for (Object[] txTypeModule : txTypeModules) {
                String moduleAbbr = (String) txTypeModule[0];
                int[] txTypes = (int[]) txTypeModule[1];
                if (txTypes.length == 0) {
                    continue;
                }
                for (int txType : txTypes) {
                    ResponseMessageProcessor.TX_TYPE_MODULE_MAP.put(txType, moduleAbbr);
                }
            }

        } catch (Exception e) {
            LoggerUtil.LOG.error("NulsCores Bootstrap initCfg failed :{}", e.getMessage(), e);
            throw new RuntimeException("NulsCores Bootstrap initCfg failed");
        }
    }

    /**
     * 初始化系统编码
     */
    private static void initSys() throws Exception {
        try {
            Class.forName("io.nuls.core.rpc.netty.processor.ResponseMessageProcessor");
            ConfigurationLoader configurationLoader = new ConfigurationLoader();
            configurationLoader.load();
            int defaultChainId = Integer.parseInt(configurationLoader.getValue("chainId"));
            ServiceManager.init(defaultChainId, Provider.ProviderType.RPC);
            System.setProperty(TxConstant.SYS_ALLOW_NULL_ARRAY_ELEMENT, "true");
            System.setProperty(TxConstant.SYS_FILE_ENCODING, UTF_8.name());
            Field charset = Charset.class.getDeclaredField("defaultCharset");
            charset.setAccessible(true);
            charset.set(null, UTF_8);
            ObjectMapper objectMapper = JSONUtils.getInstance();
            objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        } catch (Exception e) {
            Log.error(e);
            throw e;
        }
    }

    private void initCores(List<INulsCoresBootstrap> coreList) {
        for (INulsCoresBootstrap core : coreList) {
            LOG.info("Nerve core module [{}] init", core.moduleInfo().getName());
            core.mainFunction(args);
        }
    }

    private void runCores(List<INulsCoresBootstrap> coreList) {
        for (INulsCoresBootstrap core : coreList) {
            LOG.info("Nerve core module ready [{}]", core.moduleInfo().getName());
            core.onDependenciesReady();
        }
    }

}
