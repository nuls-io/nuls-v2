package io.nuls.core.rpc.netty.bootstrap;

import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.info.HostInfo;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.netty.thread.StartServerProcessor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * nettyServer side implementation class
 * Server-side implementation class
 *
 * @author tag
 * 2019/2/21
 */
public class NettyServer {

    /**
     * Obtain a service instance based on predefined modules
     * Get a server instance with predefined module
     *
     * @param moduleE Predefined modules / Pre-defined module
     * @return WsServer
     */
    public static NettyServer getInstance(ModuleE moduleE) {
        return getInstance(moduleE.abbr, moduleE.name, moduleE.domain);
    }

    /**
     * Obtain a service instance based on parameters
     * Get a server instance with Abbreviation & Name
     *
     * @param abbr   role / Role
     * @param name   name / Name
     * @param domain domain name / Domian
     * @return WsServer
     */
    public static NettyServer getInstance(String abbr, String name, String domain) {
        int port = HostInfo.randomPort();
        startServer(port);
        ConnectManager.LOCAL.setAbbreviation(abbr);
        ConnectManager.LOCAL.setModuleName(name);
        ConnectManager.LOCAL.setModuleDomain(domain);
        Map<String, String> connectionInformation = new HashMap<>(2);
        connectionInformation.put(Constants.KEY_IP, HostInfo.getLocalIP());
        connectionInformation.put(Constants.KEY_PORT, String.valueOf(port));
        ConnectManager.LOCAL.setConnectionInformation(connectionInformation);
        ConnectManager.LOCAL.setMethods(new ArrayList<>());
        ConnectManager.LOCAL.setDependencies(new HashMap<>(8));
        ConnectManager.LOCAL.setModuleRoles(new HashMap<>(1));
        return new NettyServer();
    }

    /**
     * start-upnettyServer, listen to specified port
     * Start the netty server to listen on the specified port
     */
    public static void startServer(int port, String host, String path) {
        Thread serverThread = new Thread(new StartServerProcessor(port, host, path));
        serverThread.start();
    }

    /**
     * start-upnettyServer, listen to specified port
     * Start the netty server to listen on the specified port
     */
    public static void startServer(int port) {
        Thread serverThread = new Thread(new StartServerProcessor(port));
        serverThread.start();
    }

    /**
     * Set the dependent roles for this module
     * Setting Dependent Roles for this Module
     *
     * @param key   Dependent roles / Dependent roles
     * @param value Version of dependent roles / Version of dependent roles
     * @return WsServer
     */
    public NettyServer dependencies(String key, String value) {
        ConnectManager.LOCAL.getDependencies().put(key, value);
        return this;
    }

    /**
     * Set the role of this module（Role name is assumed to be module number by default）
     * Setting up the role of this module(Role name defaults to module code)
     *
     * @param value Character version / Version of role
     * @return WsServer
     */
    public NettyServer moduleRoles(String[] value) {
        ConnectManager.LOCAL.getModuleRoles().put(ConnectManager.LOCAL.getAbbreviation(), value);
        return this;
    }

    /**
     * Set the role of this module
     * Setting up the role of this module
     *
     * @param key   role / Role
     * @param value Character version / Version of role
     * @return WsServer
     */
    public NettyServer moduleRoles(String key, String[] value) {
        ConnectManager.LOCAL.getModuleRoles().put(key, value);
        return this;
    }

    /**
     * Set module version
     * Set module version
     *
     * @param moduleVersion Version of the module / Version of module
     * @return WsServer
     */
    public NettyServer moduleVersion(String moduleVersion) {
        ConnectManager.LOCAL.setModuleVersion(moduleVersion);
        return this;
    }

    /**
     * Scan the specified path to obtain detailed information about all interfaces
     * Scan the specified path for details of all interfaces
     *
     * @param scanPackage Package path to be scanned / Packet paths to be scanned
     * @return WsServer
     * @throws Exception Found duplicate commands(cmd) / Find duplicate commands (cmd)
     */
    public NettyServer scanPackage(Set<String>scanPackage) throws Exception {
        ConnectManager.scanPackage(scanPackage);
        return this;
    }

    /**
     * AddRPCinterface
     * add RPC insterface
     *
     * @param cmdClass
     * @return
     */
    public NettyServer addCmdDetail(Class<?> cmdClass) {
        ConnectManager.addCmdDetail(cmdClass);
        return this;
    }

}
