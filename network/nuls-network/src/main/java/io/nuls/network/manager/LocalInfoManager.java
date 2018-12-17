/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.network.manager;

import io.nuls.network.constant.NetworkParam;
import io.nuls.network.model.dto.IpAddress;
import io.nuls.tools.log.Log;

import java.net.*;
import java.util.*;

/**
 * 用于管理本地节点的相关信息
 * local node info  manager
 * @author lan
 * @date 2018/11/01
 *
 */
public class LocalInfoManager extends BaseManager {

    private static LocalInfoManager instance = new LocalInfoManager();
    public static LocalInfoManager getInstance(){
        return instance;
    }

    private IpAddress externalAddress=new IpAddress("0.0.0.0",0);

    /**
     * 是否地址广播
     */
    private volatile boolean isAddrBroadcast=false;
    /**
     * 是否已尝试过自己连接自己
     */
    private  volatile boolean connectedMySelf;
    /**
     * 当前节点是否是自身网络种子节点
     */
    private volatile boolean isSelfNetSeed;

    /**
     * 本机IP集合
     */
    private static final Set<String> IPS = new HashSet<>();

    public void updateExternalAddress(String ip,int port){
        externalAddress.setIpStr(ip);
        externalAddress.setPort(port);

    }

    boolean isConnectedMySelf() {
        return connectedMySelf;
    }

    void setConnectedMySelf(boolean connectedMySelf) {
        this.connectedMySelf = connectedMySelf;
    }

    boolean isSelfNetSeed() {
        return isSelfNetSeed;
    }



    /**
     *
     * @return boolean
     */
    boolean isAddrBroadcast() {
        return isAddrBroadcast;
    }

    /**
     * 设置是否已经广播过自身地址
     * @param addrBroadcast true or false
     */
    void setAddrBroadcast(boolean addrBroadcast) {
        isAddrBroadcast = addrBroadcast;
    }

    public IpAddress getExternalAddress() {
        return externalAddress;
    }

    /**
     *  setExternalAddress
     * @param externalAddress IpAddress
     */
    public void setExternalAddress(IpAddress externalAddress) {
        this.externalAddress = externalAddress;
    }


    public boolean isSelfIp(String ip){
       if(externalAddress.getIp().getHostAddress().equals(ip)){
           return true;
       }
        return IPS.contains(ip);
    }
    static {
        List<String> localIPs = getLocalIP();
        IPS.addAll(localIPs);
    }

    private static ArrayList<String> getLocalIP() {
        ArrayList<String> iplist = new ArrayList<>();
        String bindip;
        Enumeration<?> network;
        List<NetworkInterface> netlist = new ArrayList<>();
        try {
            network = NetworkInterface.getNetworkInterfaces();
            while (network.hasMoreElements()) {
                NetworkInterface ni = (NetworkInterface) network.nextElement();
                if (ni.isLoopback()) {
                    continue;
                }
                netlist.add(0, ni);
                InetAddress ip;
                for (NetworkInterface list : netlist) {
                    Enumeration<?> card = list.getInetAddresses();
                    while (card.hasMoreElements()) {
                        while (true) {
                            ip = null;
                            try {
                                Object object = card.nextElement();
                                if(object instanceof  InetAddress){
                                    ip = (InetAddress) card.nextElement();
                                }
                            } catch (Exception e) {
                                Log.debug(e.getMessage());
                            }
                            if (ip == null) {
                                break;
                            }
                            if (!ip.isLoopbackAddress()) {
                                if ("127.0.0.1".equalsIgnoreCase(ip.getHostAddress())) {
                                    continue;
                                }
                            }
                            if (ip instanceof Inet4Address) {
                                bindip = ip.getHostAddress();
                                boolean addto = true;
                                for (String anIplist : iplist) {
                                    if (bindip.equals(anIplist)) {
                                        addto = false;
                                        break;
                                    }
                                }
                                if (addto) {
                                    iplist.add(bindip);
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (SocketException e) {
            // skip
            Log.error("Get local IP error: " + e.getMessage());
        }

        return iplist;
    }

    @Override
    public void init() {
        for (String ip : IPS) {
            if (isSelfSeedNode(ip)) {
                isSelfNetSeed = true;
                break;
            }
        }
    }

    /**
     * 是否是种子节点
     * Is it a seed node
     * @param ip  just ip address not port
     * @return boolean
     */
    private boolean isSelfSeedNode(String ip) {
        List<String> seedList=NetworkParam.getInstance().getSeedIpList();
        for (String seedIp : seedList) {
            if (seedIp.equals(ip)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void start() {

    }
}
