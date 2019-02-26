package io.nuls.rpc.netty.bootstrap;

import io.nuls.rpc.info.Constants;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.netty.channel.manager.ConnectManager;
import io.nuls.rpc.netty.thread.StartServerProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * netty服务器端实现类
 * Server-side implementation class
 * @author tag
 * 2019/2/21
 * */
public class NettyServer {

    /**
     * 根据预定义模块获得一个服务实例
     * Get a server instance with predefined module
     *
     * @param moduleE 预定义模块 / Pre-defined module
     * @return WsServer
     */
    public static NettyServer getInstance(ModuleE moduleE){
        return getInstance(moduleE.abbr, moduleE.name, moduleE.domain);
    }

    /**
     * 根据参数获得一个服务实例
     * Get a server instance with Abbreviation & Name
     *
     * @param abbr   角色 / Role
     * @param name   名称 / Name
     * @param domain 域名 / Domian
     * @return WsServer
     */
    public static NettyServer getInstance(String abbr, String name, String domain){
        int port = HostInfo.randomPort();
        startServer(port);
        ConnectManager.LOCAL.setModuleAbbreviation(abbr);
        ConnectManager.LOCAL.setModuleName(name);
        ConnectManager.LOCAL.setModuleDomain(domain);
        Map<String, String> connectionInformation = new HashMap<>(2);
        connectionInformation.put(Constants.KEY_IP, HostInfo.getLocalIP());
        connectionInformation.put(Constants.KEY_PORT, String.valueOf(port));
        ConnectManager.LOCAL.setConnectionInformation(connectionInformation);
        ConnectManager.LOCAL.setApiMethods(new ArrayList<>());
        ConnectManager.LOCAL.setDependencies(new HashMap<>(8));
        ConnectManager.LOCAL.setModuleRoles(new HashMap<>(1));
        return new NettyServer();
    }

    /**
     * 启动netty服务器，监听指定端口
     * Start the netty server to listen on the specified port
     * */
    public static void startServer(int port){
        Thread serverThread = new Thread(new StartServerProcessor(port));
        serverThread.start();
    }

    /**
     * 设置本模块的依赖角色
     * Setting Dependent Roles for this Module
     *
     * @param key   依赖的角色 / Dependent roles
     * @param value 依赖角色的版本 / Version of dependent roles
     * @return WsServer
     */
    public NettyServer dependencies(String key, String value) {
        ConnectManager.LOCAL.getDependencies().put(key, value);
        return this;
    }

    /**
     * 设置本模块的角色（角色名默认为模块编号）
     * Setting up the role of this module(Role name defaults to module code)
     *
     * @param value 角色版本 / Version of role
     * @return WsServer
     */
    public NettyServer moduleRoles(String[] value) {
        ConnectManager.LOCAL.getModuleRoles().put(ConnectManager.LOCAL.getModuleAbbreviation(), value);
        return this;
    }

    /**
     * 设置本模块的角色
     * Setting up the role of this module
     *
     * @param key   角色 / Role
     * @param value 角色版本 / Version of role
     * @return WsServer
     */
    public NettyServer moduleRoles(String key, String[] value) {
        ConnectManager.LOCAL.getModuleRoles().put(key, value);
        return this;
    }

    /**
     * 设置模块版本
     * Set module version
     *
     * @param moduleVersion 模块的版本 / Version of module
     * @return WsServer
     */
    public NettyServer moduleVersion(String moduleVersion) {
        ConnectManager.LOCAL.setModuleVersion(moduleVersion);
        return this;
    }

    /**
     * 扫描指定路径，得到所有接口的详细信息
     * Scan the specified path for details of all interfaces
     *
     * @param scanPackage 需要扫描的包路径 / Packet paths to be scanned
     * @return WsServer
     * @throws Exception 找到重复命令(cmd) / Find duplicate commands (cmd)
     */
    public NettyServer scanPackage(String scanPackage) throws Exception {
        ConnectManager.scanPackage(scanPackage);
        return this;
    }
}
