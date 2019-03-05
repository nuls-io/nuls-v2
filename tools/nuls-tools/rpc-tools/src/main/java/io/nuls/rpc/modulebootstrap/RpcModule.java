package io.nuls.rpc.modulebootstrap;

import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Message;
import io.nuls.rpc.model.message.MessageType;
import io.nuls.rpc.model.message.MessageUtil;
import io.nuls.rpc.model.message.Request;
import io.nuls.rpc.netty.bootstrap.NettyServer;
import io.nuls.rpc.netty.channel.ConnectData;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Order;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
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
@Getter
@Setter
@Slf4j
@Order(0)
public abstract class RpcModule implements InitializingBean {

    private static final String ROLE = "1.0";

    /**
     * 模块运行状态
     */
    private RpcModuleState state = RpcModuleState.Start;

    /**
     * 依赖当前模块的其他模块列表
     */
    private Set<Module> followerList = new HashSet<>();

    /**
     * 当前模块依赖的其他模块列表
     */
    private Map<Module, Boolean> dependencies = new HashMap<>();

    @Override
    public final void afterPropertiesSet() throws NulsException {
        try{
            init();
        }catch (Exception e){
            log.error("rpc module init fail",e);
            throw new NulsException(e);
        }
    }

    /**
     * 监听依赖的模块进入ready状态的通知
     * @param module
     */
    void listenerDependenciesReady(Module module) {
        try {
            if (dependencies.containsKey(module)) {
                dependencies.put(module, Boolean.TRUE);
            }
            tryRunModule();
            ConnectData connectData = ConnectManager.getConnectDataByRole(module.getName());
            connectData.addCloseEvent(() -> {
                log.warn("{}模块触发连接断开事件", module);
                if (isRunning()) {
                    state = this.onDependenciesLoss(module);
                    Objects.requireNonNull(state,"onDependenciesLoss return null state");
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.debug("ModuleReadyListener :{}", module);
    }

    /**
     * 监听依赖当前模块的其他模块的注册
     * @param module
     */
    void followModule(Module module) {
        log.info("registerModuleDependencies :{}", module);
        synchronized (this) {
            followerList.add(module);
        }
        if (this.isReady()) {
            notifyFollowerReady(module);
        }
    }

    /**
     * 通知follower当前模块已经进入ready状态
     * @param module
     */
    private void notifyFollowerReady(Module module) {
        Request request = MessageUtil.defaultRequest();
        request.getRequestMethods().put("listenerDependenciesReady", this.moduleInfo());
        Message message = MessageUtil.basicMessage(MessageType.Request);
        message.setMessageData(request);
        try {
            ConnectManager.sendMessage(module.getName(), message);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 通知所有follower当前模块已经进入ready状态
     */
    private void notifyFollowerReady() {
        followerList.stream().forEach(this::notifyFollowerReady);
    }

    /**
     * 启动模块
     * @param serviceManagerUrl
     */
    void run(String modulePackage,String serviceManagerUrl) {
        //初始化依赖模块的ready状态
        Arrays.stream(this.getDependencies()).forEach(d -> dependencies.put(d, Boolean.FALSE));
        try {
            // Start server instance
            NettyServer server = NettyServer.getInstance(ModuleE.valueOfAbbr(moduleInfo().getName()))
                    .moduleRoles(new String[]{getRole()})
                    .moduleVersion(moduleInfo().getVersion())
                    .scanPackage(StringUtils.isBlank(getRpcCmdPackage()) ? modulePackage : getRpcCmdPackage())
                    //注册管理模块状态的RPC接口
                    .addCmdDetail(ModuleStatusCmd.class);
            dependencies.keySet().stream().forEach(d -> server.dependencies(d.getName(), d.getVersion()));
            // Get information from kernel
            ConnectManager.getConnectByUrl(serviceManagerUrl);
            ResponseMessageProcessor.syncKernel(serviceManagerUrl, new RegisterInvoke(moduleInfo(), dependencies.keySet()));

            //模块进入ready状态的准备工作，如果条件未达到，等待10秒重新尝试
            while (!doStart()) {
                TimeUnit.SECONDS.sleep(10L);
            }
            state = RpcModuleState.Ready;
            this.notifyFollowerReady();
            tryRunModule();
        } catch (Exception e) {
            Log.error(moduleInfo().toString() + " initServer failed", e);
        }
    }

    /**
     * 尝试启动模块
     * 如果所有依赖准备就绪就触发onDependenciesReady
     */
    private synchronized void tryRunModule() {
        Boolean dependencieReady = dependencies.isEmpty();
        if(!dependencieReady){
            dependencieReady = dependencies.entrySet().stream().allMatch(d -> d.getValue());
        }
        if (dependencieReady) {
            if(!isRunning()){
                state = onDependenciesReady();
                Objects.requireNonNull(state,"onDependenciesReady return null state");
            }
        }else{
            log.info("dependencie state");
            dependencies.entrySet().forEach(entry->log.info("{}:{}",entry.getKey().getName(),entry.getValue()));
        }
    }

    protected String getRole(){
        return ROLE;
    };

    /**
     * 模块是否已运行
     * @return
     */
    protected boolean isRunning(){
        return state.getIndex() >= RpcModuleState.Running.getIndex();
    }

    /**
     * 模块是否已准备好
     * @return
     */
    protected boolean isReady(){
        return state.getIndex() >= RpcModuleState.Ready.getIndex();
    }

    /**
     * 获取依赖模块的准备状态
     * @param module
     * @return true 已准备好
     */
    public  boolean isDependencieReady(Module module){
        if(!dependencies.containsKey(module)){
            throw new IllegalArgumentException("can not found "+module.getName());
        }
        return dependencies.get(module);
    }

    /**
     * 返回此模块的依赖模块
     *
     * @return
     */
    public abstract Module[] getDependencies();

    /**
     * 指定RpcCmd的包名
     * @return
     */
    public String getRpcCmdPackage(){
        return null;
    };

    /**
     * 返回当前模块的描述
     * @return
     */
    public abstract Module moduleInfo();


    /**
     * 初始化模块
     * 在onStart前会调用此方法
     *
     */
    public void init() {
        log.info("module inited");
    }


    /**
     * 已完成spring init注入，开始启动模块
     * 模块进入ready状态前的准备工作，模块启动时触发
     * 如果准备完毕返回true
     * 条件未达到返回false
     * @return
     */
    public abstract boolean doStart();

    /**
     * 所有外部依赖进入ready状态后会调用此方法，正常启动后返回Running状态
     * @return
     */
    public abstract RpcModuleState onDependenciesReady();

    /**
     * 某个外部依赖连接丢失后，会调用此方法，
     * 可控制模块状态，如果返回Ready,则表明模块退化到Ready状态，当依赖重新准备完毕后，将重新触发onDependenciesReady方法，
     * 若返回的状态是Running，将不会重新触发onDependenciesReady
     * @param dependenciesModule
     * @return
     */
    public abstract RpcModuleState onDependenciesLoss(Module dependenciesModule);

}
