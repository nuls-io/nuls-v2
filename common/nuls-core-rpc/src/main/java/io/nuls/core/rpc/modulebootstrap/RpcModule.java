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
 * @Description: RPCModule Foundation Class
 * AdministrationmoduleStartup, state management, module lifecycle management
 * Responsible for connectingservic manager,And callregisterAPI
 * Manage module lifecycle and control the lifecycle of the module itself based on the operational status of dependent modules.
 * Defining Abstract MethodsonStart,onDependenciesReady,ononDependenciesLossThe implementation of abstract lifecycle in other ways
 */
@Order(Integer.MIN_VALUE)
public abstract class RpcModule implements InitializingBean {
    @Override
    public String toString() {
        return new StringJoiner(", ", RpcModule.class.getSimpleName() + "[", "]")
                .add("dependencies=" + dependencies)
                .add("state=" + state)
                .add("followerList=" + followerList)
                .add("dependentReadyState=" + dependentReadyState)
                .toString();
    }

    private static final String LANGUAGE = "en";
    private static final String LANGUAGE_PATH =  "languages";
    protected static final String ROLE = "1.0";

    private Set<Module> dependencies;

    /**
     * Module operation status
     */
    private RpcModuleState state = RpcModuleState.Start;

    /**
     * List of other modules that depend on the current module
     */
    private Map<Module, Boolean> followerList = new ConcurrentHashMap<>();

    /**
     * The running status of other modules that the current module depends on（Have you received the push from the modulereadynotice）
     */
    private Map<Module, Boolean> dependentReadyState = new ConcurrentHashMap<>();

    @Autowired
    NotifySender notifySender;

    protected String[] startArgs;

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
     * Listening to dependent modules enteringreadyNotification of status
     *
     * @param module
     */
    void listenerDependenciesReady(Module module) {
        try {

            if (dependentReadyState.containsKey(module)) {
                if(dependentReadyState.get(module).equals(Boolean.TRUE)){
                    return ;
                }
                dependentReadyState.put(module, Boolean.TRUE);
            }
            Log.info("RMB:ModuleReadyListener :{}", module);
            tryRunModule();
            ConnectData connectData = ConnectManager.getConnectDataByRole(module.getName());
            connectData.addCloseEvent(() -> {
                if (!ConnectManager.ROLE_CHANNEL_MAP.containsKey(module.getName())) {
                    Log.warn("RMB:dependencie:{}Module triggers connection disconnection event", module);
                    dependentReadyState.put(module, Boolean.FALSE);
                    if(followerList.containsKey(module)){
                        followerList.remove(module);
                    }
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
            Log.error("",e.getMessage(),e);
            e.printStackTrace();
        }
    }

    /**
     * Listening to the registration of other modules that depend on the current module
     *
     * @param module
     */
    void addFollower(Module module) {
        synchronized (this) {
            if (!followerList.containsKey(module)) {
                Log.info("RMB:registerModuleDependencies :{}", module);
                followerList.put(module, Boolean.FALSE);
                if(dependentReadyState.containsKey(module)){
                    dependentReadyState.put(module, Boolean.FALSE);
                }
                try {
                    //Monitoring andfollowerIf the connection is disconnected, the notification status needs to be modified
                    ConnectData connectData = ConnectManager.getConnectDataByRole(module.getName());
                    connectData.addCloseEvent(() -> {
                        if (!ConnectManager.ROLE_CHANNEL_MAP.containsKey(module.getName())) {
                            Log.warn("RMB:follower:{}Module triggers connection disconnection event", module);
                            //Change notification status to not notified
                            followerList.remove(module);
                        }
                    });
                    Log.debug("Bind connection disconnection event:{}",module.name);
                } catch (Exception e) {
                    Log.error("RMB:obtainfollower:{}Abnormal module connection.", module, e);
                }
            }
        }
        if (this.isReady()) {
            notifyFollowerReady(module);
        }
    }

    /**
     * noticefollowerThe current module has enteredreadystate
     *
     * @param module
     */
    private void notifyFollowerReady(Module module) {
        notifySender.send("notifyFollowerReady_"+module.toString(),10,() -> {
//            if (followerList.get(module)) {
//                return true;
//            }
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
     * Notify allfollowerThe current module has enteredreadystate
     */
    private void notifyFollowerReady() {
        followerList.keySet().forEach(this::notifyFollowerReady);
    }

    /**
     * Start module
     *
     * @param serviceManagerUrl
     */
    void run(String modulePackage, String serviceManagerUrl,String[] startArgs) {
        this.startArgs = startArgs;
        //Initialize dependent modulesreadystate
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
                    //Registration Management Module StatusRPCinterfaceifc
                    .addCmdDetail(ModuleStatusCmd.class);
            dependentReadyState.keySet().forEach(d -> server.dependencies(d.getName(), d.getVersion()));
            // Get information from kernel
            ConnectManager.getConnectByUrl(serviceManagerUrl);
            Log.info("RMB:Start connectingservice manager:{}",serviceManagerUrl);
            ResponseMessageProcessor.syncKernel(serviceManagerUrl, new RegisterInvoke(moduleInfo(), dependentReadyState.keySet()));
            //Module entryreadyPreparation work for status, if conditions are not met, wait10Second retry
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
     * Attempt to start the module
     * Trigger if all dependencies are readyonDependenciesReady
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
     * Is the module running
     *
     * @return
     */
    protected boolean isRunning() {
        return state.getIndex() >= RpcModuleState.Running.getIndex();
    }

    /**
     * Is the module ready
     *
     * @return
     */
    protected boolean isReady() {
        return state.getIndex() >= RpcModuleState.Ready.getIndex();
    }

    /**
     * Obtain the readiness status of dependent modules
     *
     * @param module
     * @return true Ready
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
     * Dependent modules are all accessed by enteringReadystate
     */
    protected boolean isDependencieReady() {
        return dependentReadyState.entrySet().stream().allMatch(d -> d.getValue());
    }

    public Set<Module> getDependencies() {
        return dependencies;
    }

    /**
     * Declare the dependent modules of this module
     *
     * @return
     */
    public abstract Module[] declareDependent();

    /**
     * specifyRpcCmdPackage Name
     *
     * @return
     */
    public Set<String> getRpcCmdPackage() {
        return null;
    }


    /**
     * Return the description of the current module
     *
     * @return
     */
    public abstract Module moduleInfo();


    public void onDependenciesReady(Module module){
        Log.debug("dependentReadyState module {} ready",module);
    }


    /**
     * Initialize module
     * stayonStartI will call this method before
     */
    public void init() {
        Log.info("module inited");
    }


    /**
     * Completedspring initInject, start module startup
     * Module entryreadyPreparation work before status, triggered when the module starts
     * If prepared, returntrue
     * Return if conditions are not metfalse
     *
     * @return
     */
    public abstract boolean doStart();

    /**
     * All external dependencies enterreadyThis method will be called after the state is reached, and will return after normal startupRunningstate
     *
     * @return
     */
    public abstract RpcModuleState onDependenciesReady();

    /**
     * After a certain external dependency connection is lost, this method will be called,
     * Controllable module status, if returnedReady,This indicates that the module has degraded toReadyThe state will be triggered again when the dependency is fully preparedonDependenciesReadyMethod,
     * If the returned status isRunning, will not be triggered againonDependenciesReady
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
