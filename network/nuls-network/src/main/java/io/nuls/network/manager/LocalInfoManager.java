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
    private long  blockHeight=0;
    private String blockHash="";
    /**
     * 是否地址广播
     */
    private volatile boolean isAddrBroadcast=false;
    /**
     * 是否已尝试过自己连接自己
     */
    volatile boolean connectedMySelf;
    /**
     * 当前节点是否是自身网络种子节点
     */
    volatile boolean isSelfNetSeed;

    public void updateExternalAddress(String ip,int port){
        externalAddress.setIpStr(ip);
        externalAddress.setPort(port);

    }

    public boolean isConnectedMySelf() {
        return connectedMySelf;
    }

    public void setConnectedMySelf(boolean connectedMySelf) {
        this.connectedMySelf = connectedMySelf;
    }

    public boolean isSelfNetSeed() {
        return isSelfNetSeed;
    }

    public void setSelfNetSeed(boolean selfNetSeed) {
        isSelfNetSeed = selfNetSeed;
    }



    public boolean isAddrBroadcast() {
        return isAddrBroadcast;
    }

    public void setAddrBroadcast(boolean addrBroadcast) {
        isAddrBroadcast = addrBroadcast;
    }

    public IpAddress getExternalAddress() {
        return externalAddress;
    }

    public void setExternalAddress(IpAddress externalAddress) {
        this.externalAddress = externalAddress;
    }

    public long getBlockHeight() {
        return blockHeight;
    }

    public void setBlockHeight(long blockHeight) {
        this.blockHeight = blockHeight;
    }

    public String getBlockHash() {
        return blockHash;
    }

    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }

    private static final Set<String> ips = new HashSet<>();

    public boolean isSelfConnect(String ip){
       if(externalAddress.getIp().getHostAddress().equals(ip)){
           return true;
       }
        if(ips.contains(ip)){
            return true;
        }
        return false;
    }
    static {
        List<String> localIPs = getLocalIP();
        for (String ip : localIPs) {
            ips.add(ip);
        }
    }

    private static ArrayList<String> getLocalIP() {
        ArrayList<String> iplist = new ArrayList<>();
        boolean loop = false;
        String bindip;
        Enumeration<?> network;
        List<NetworkInterface> netlist = new ArrayList<>();
        try {
            network = NetworkInterface.getNetworkInterfaces();
            while (network.hasMoreElements()) {
                loop = true;
                NetworkInterface ni = (NetworkInterface) network.nextElement();
                if (ni.isLoopback()) {
                    continue;
                }
                netlist.add(0, ni);
                InetAddress ip;
                for (NetworkInterface list : netlist) {
                    if (loop == false) {
                        break;
                    }
                    Enumeration<?> card = list.getInetAddresses();
                    while (card.hasMoreElements()) {
                        while (true) {
                            ip = null;
                            try {
                                ip = (InetAddress) card.nextElement();
                            } catch (Exception e) {

                            }
                            if (ip == null) {
                                break;
                            }
                            if (!ip.isLoopbackAddress()) {
                                if (ip.getHostAddress().equalsIgnoreCase("127.0.0.1")) {
                                    continue;
                                }
                            }
                            if (ip instanceof Inet6Address) {
                                continue;
                            }
                            if (ip instanceof Inet4Address) {
                                bindip = ip.getHostAddress();
                                boolean addto = true;
                                for (int n = 0; n < iplist.size(); n++) {
                                    if (bindip.equals(iplist.get(n))) {
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
        for (String ip : ips) {
            if (isSelfSeedNode(ip)) {
//                networkParam.setMaxInCount(networkParam.getMaxInCount() * 2);
                isSelfNetSeed = true;
            }
        }
    }
    /**
     * 是否是种子节点
     */
    public boolean isSelfSeedNode(String ip) {
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
