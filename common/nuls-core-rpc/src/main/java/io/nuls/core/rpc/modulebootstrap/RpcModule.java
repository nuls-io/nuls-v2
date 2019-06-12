package io.nuls.core.rpc.modulebootstrap;

import io.nuls.core.basic.InitializingBean;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Order;
import io.nuls.core.core.config.ConfigurationLoader;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.parse.I18nUtils;
import io.nuls.core.parse.MapUtils;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.bootstrap.NettyServer;
import io.nuls.core.rpc.netty.channel.ConnectData;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.thread.ThreadUtils;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * @Author: zhoulijun
 * @Time: 2019-02-27 17:41
 * @Description: RPC模块基础类
 * 管理module的启动，状态管理，模块生命周期管理
 * 负责连接servic manager,并调用registerAPI
 * 管理模块生命周期，根据依赖模块的运行状况控制模块本身的生命周期。
 * 定义抽象方法onStart,onDependenciesReady,ononDependenciesLoss等方式抽象生命周期的实现
 */
@Order(Integer.MIN_VALUE)
public abstract class RpcModule implements InitializingBean {

    private static final String LANGUAGE = "en";
    private static final String LANGUAGE_PATH =  "languages";
    protected static final String ROLE = "1.0";

    private Set<Module> dependencies;

    /**
     * 模块运行状态
     */
    private RpcModuleState state = RpcModuleState.Start;

    /**
     * 依赖当前模块的其他模块列表
     */
    private Map<Module, Boolean> followerList = new ConcurrentHashMap<>();

    /**
     * 当前模块依赖的其他模块的运行状态（是否接收到模块推送的ready通知）
     */
    private Map<Module, Boolean> dependentReadyState = new ConcurrentHashMap<>();

    @Autowired
    NotifySender notifySender;

    @Override
    public final void afterPropertiesSet() throws NulsException {
        try {
            dependencies = new HashSet<>();
            Module[] depend = declareDependent();
            if(depend != null){
                dependencies.addAll(Arrays.asList(depend));
            }
            ConfigurationLoader configLoader = SpringLiteContext.getBean(ConfigurationLoader.class);
            String configDomain = moduleInfo().name;
            if(ModuleE.hasOfAbbr(moduleInfo().name)){
                configDomain = ModuleE.valueOfAbbr(moduleInfo().getName()).name;
            }
            String dependentList = configLoader.getValue(configDomain,"dependent");
            if(dependentList != null){
                ConfigurationLoader.ConfigItem configItem = configLoader.getConfigItem(configDomain,"dependent");
                Log.info("{}.dependent : {} ==> {}[{}] ",this.getClass().getSimpleName(),dependentList,configItem.getConfigFile(),configDomain);
                String[] temp = dependentList.split(",");
                Arrays.stream(temp).forEach(ds->{
                    dependencies.add(new Module(ds, ROLE));
                });
            }
            Log.info("module dependents:");
            dependencies.forEach(d->{
                Log.info("{}:{}",d.name,d.version);
            });
            I18nUtils.loadLanguage(this.getClass(), getLanguagePath(),LANGUAGE);
            init();
        } catch (Exception e) {
            Log.error("rpc module init fail", e);
            throw new NulsException(e);
        }
    }

    /**
     * 监听依赖的模块进入ready状态的通知
     *
     * @param module
     */
    void listenerDependenciesReady(Module module) {
        try {
            if (dependentReadyState.containsKey(module)) {
                dependentReadyState.put(module, Boolean.TRUE);
            }
            Log.info("RMB:ModuleReadyListener :{}", module);
            tryRunModule();
            ConnectData connectData = ConnectManager.getConnectDataByRole(module.getName());
            connectData.addCloseEvent(() -> {
                if (!ConnectManager.ROLE_CHANNEL_MAP.containsKey(module.getName())) {
                    Log.warn("RMB:dependencie:{}模块触发连接断开事件", module);
                    dependentReadyState.put(module, Boolean.FALSE);
                    if (isRunning()) {
                        state = this.onDependenciesLoss(module);
                        if (state == null) {
                            Log.error("onDependenciesReady return null state", new NullPointerException("onDependenciesReady return null state"));
                            System.exit(0);
                        }
                        Log.info("RMB:module state : {}", state);
                    }
                }
            });
            this.onDependenciesReady(module);
        } catch (Exception e) {
            Log.error("");
            e.printStackTrace();
        }
    }

    /**
     * 监听依赖当前模块的其他模块的注册
     *
     * @param module
     */
    void followModule(Module module) {
        synchronized (this) {
            if (!followerList.containsKey(module)) {
                Log.info("RMB:registerModuleDependencies :{}", module);
                followerList.put(module, Boolean.FALSE);
                try {
                    //监听与follower的连接，如果断开后需要修改通知状态
                    ConnectData connectData = ConnectManager.getConnectDataByRole(module.getName());
                    connectData.addCloseEvent(() -> {
//                        if (!ConnectManager.ROLE_CHANNEL_MAP.containsKey(module.getName())) {
                            Log.warn("RMB:follower:{}模块触发连接断开事件", module);
                            //修改通知状态为未通知
                            followerList.put(module, Boolean.FALSE);
//                        }
                    });
                } catch (Exception e) {
                    Log.error("RMB:获取follower:{}模块连接发生异常.", module, e);
                }
            }
        }
        if (this.isReady()) {
            notifyFollowerReady(module);
        }
    }

    /**
     * 通知follower当前模块已经进入ready状态
     *
     * @param module
     */
    private void notifyFollowerReady(Module module) {
        notifySender.send("notifyFollowerReady_"+module.toString(),10,() -> {
            if (followerList.get(module)) {
                return true;
            }
            try {
                Response cmdResp = ResponseMessageProcessor.requestAndResponse(module.getName(), "listenerDependenciesReady", MapUtils.beanToLinkedMap(this.moduleInfo()),1000L);
                if (cmdResp.isSuccess()) {
                    followerList.put(module, Boolean.TRUE);
                    Log.info("notify follower {} is Ready success", module);
                    return true;
                } else {
                    return false;
                }
            } catch (Exception e) {
                Log.error("Calling remote interface failed. module:{} - interface:{} - message:{}", module, "registerModuleDependencies", e.getMessage());
                return false;
            }
        });
    }

    /**
     * 通知所有follower当前模块已经进入ready状态
     */
    private void notifyFollowerReady() {
        followerList.keySet().forEach(this::notifyFollowerReady);
    }

    /**
     * 启动模块
     *
     * @param serviceManagerUrl
     */
    void run(String modulePackage, String serviceManagerUrl) {
        //初始化依赖模块的ready状态
        this.getDependencies().forEach(d -> dependentReadyState.put(d, Boolean.FALSE));
        try {
            // Start server instance
            Set<String> scanCmdPackage = new TreeSet<>();
            scanCmdPackage.add("io.nuls.core.rpc.cmd");
            scanCmdPackage.add("io.nuls.base.protocol.cmd");
            scanCmdPackage.addAll((getRpcCmdPackage() == null) ? Set.of(modulePackage) : getRpcCmdPackage());
            NettyServer server = NettyServer.getInstance(moduleInfo().getName(), moduleInfo().getName(), ModuleE.DOMAIN)
                    .moduleRoles(new String[]{getRole()})
                    .moduleVersion(moduleInfo().getVersion())
                    .scanPackage(scanCmdPackage)
                    //注册管理模块状态的RPC接口
                    .addCmdDetail(ModuleStatusCmd.class);
            dependentReadyState.keySet().forEach(d -> server.dependencies(d.getName(), d.getVersion()));
            // Get information from kernel
            ConnectManager.getConnectByUrl(serviceManagerUrl);
            Log.info("RMB:开始连接service manager");
            ResponseMessageProcessor.syncKernel(serviceManagerUrl, new RegisterInvoke(moduleInfo(), dependentReadyState.keySet()));
            //模块进入ready状态的准备工作，如果条件未达到，等待10秒重新尝试
            while (!doStart()) {
                TimeUnit.SECONDS.sleep(10L);
            }
            Log.info("RMB:module is READY");
            state = RpcModuleState.Ready;
            this.notifyFollowerReady();
            tryRunModule();
        } catch (Exception e) {
            Log.error(moduleInfo().toString() + " initServer failed", e);
            System.exit(0);
        }
    }

    /**
     * 尝试启动模块
     * 如果所有依赖准备就绪就触发onDependenciesReady
     */
    private synchronized void tryRunModule() {
        if (!isReady()) {
            return;
        }
        boolean dependencieReady = dependentReadyState.isEmpty();
        if (!dependencieReady) {
            dependencieReady = dependentReadyState.entrySet().stream().allMatch(Map.Entry::getValue);
        }
        if (dependencieReady) {
            if (!isRunning()) {
                Log.info("RMB:dependencie state");
                dependentReadyState.forEach((key, value) -> Log.debug("{}:{}", key.getName(), value));
                Log.info("RMB:module try running");
                CountDownLatch latch = new CountDownLatch(1);
                ThreadUtils.createAndRunThread("module running", () -> {
                    try {
                        state = onDependenciesReady();
                        if (state == null) {
                            Log.error("onDependenciesReady return null state", new NullPointerException("onDependenciesReady return null state"));
                            System.exit(0);
                        }
                    } catch (Exception e) {
                        Log.error("RMB:try running module fail ", e);
                        System.exit(0);
                    }

                    latch.countDown();
                });
                try {
                    latch.await(getTryRuningTimeout(), TimeUnit.SECONDS);
                    if (state != RpcModuleState.Running) {
                        Log.error("RMB:module try running timeout");
                        System.exit(0);
                    }
                } catch (InterruptedException e) {
                    Log.error("wait module running fail");
                    System.exit(0);
                }
                Log.info("RMB:module state : " + state);
            }
        } else {
            Log.info("RMB:dependencie is not all ready");
            Log.info("RMB:dependencie state");
            dependentReadyState.forEach((key, value) -> Log.debug("{}:{}", key.getName(), value));
        }
    }

    protected long getTryRuningTimeout() {
        return 30;
    }

    protected String getRole() {
        return ROLE;
    }

    ;

    /**
     * 模块是否已运行
     *
     * @return
     */
    protected boolean isRunning() {
        return state.getIndex() >= RpcModuleState.Running.getIndex();
    }

    /**
     * 模块是否已准备好
     *
     * @return
     */
    protected boolean isReady() {
        return state.getIndex() >= RpcModuleState.Ready.getIndex();
    }

    /**
     * 获取依赖模块的准备状态
     *
     * @param module
     * @return true 已准备好
     */
    public boolean isDependencieReady(Module module) {
        if (!dependentReadyState.containsKey(module)) {
            throw new IllegalArgumentException("can not found " + module.getName());
        }
        return dependentReadyState.get(module);
    }

    public boolean hasDependent(ModuleE moduleE){
        return hasDependent(Module.build(moduleE));
    }

    public boolean hasDependent(Module module){
        return getDependencies().stream().anyMatch(module::equals);
    }

    public boolean isDependencieReady(String moduleName){
        return isDependencieReady(new Module(moduleName,ROLE));
    }

    /**
     * 依赖模块都以进入Ready状态
     */
    protected boolean isDependencieReady() {
        return dependentReadyState.entrySet().stream().allMatch(d -> d.getValue());
    }

    public Set<Module> getDependencies() {
        return dependencies;
    }

    /**
     * 申明此模块的依赖模块
     *
     * @return
     */
    public abstract Module[] declareDependent();

    /**
     * 指定RpcCmd的包名
     *
     * @return
     */
    public Set<String> getRpcCmdPackage() {
        return null;
    }


    /**
     * 返回当前模块的描述
     *
     * @return
     */
    public abstract Module moduleInfo();


    public void onDependenciesReady(Module module){
        Log.debug("dependentReadyState module {} ready",module);
    }


    /**
     * 初始化模块
     * 在onStart前会调用此方法
     */
    public void init() {
        Log.info("module inited");
    }


    /**
     * 已完成spring init注入，开始启动模块
     * 模块进入ready状态前的准备工作，模块启动时触发
     * 如果准备完毕返回true
     * 条件未达到返回false
     *
     * @return
     */
    public abstract boolean doStart();

    /**
     * 所有外部依赖进入ready状态后会调用此方法，正常启动后返回Running状态
     *
     * @return
     */
    public abstract RpcModuleState onDependenciesReady();

    /**
     * 某个外部依赖连接丢失后，会调用此方法，
     * 可控制模块状态，如果返回Ready,则表明模块退化到Ready状态，当依赖重新准备完毕后，将重新触发onDependenciesReady方法，
     * 若返回的状态是Running，将不会重新触发onDependenciesReady
     *
     * @param dependenciesModule
     * @return
     */
    public abstract RpcModuleState onDependenciesLoss(Module dependenciesModule);

    protected String getLanguagePath(){
        return LANGUAGE_PATH;
    }

    public static String getROLE() {
        return ROLE;
    }

    public RpcModuleState getState() {
        return state;
    }

    public void setState(RpcModuleState state) {
        this.state = state;
    }

    public Map<Module, Boolean> getFollowerList() {
        return followerList;
    }

    public void setFollowerList(Map<Module, Boolean> followerList) {
        this.followerList = followerList;
    }

    public void setDependentReadyState(Map<Module, Boolean> dependentReadyState) {
        this.dependentReadyState = dependentReadyState;
    }

    public NotifySender getNotifySender() {
        return notifySender;
    }

    public void setNotifySender(NotifySender notifySender) {
        this.notifySender = notifySender;
    }
}
