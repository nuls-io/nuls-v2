/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */
package io.nuls.network.model;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 节点组对象
 * @author lan
 * @date 2018/11/01
 *
 */
public class NodeGroup {
    private long magicNumber;
    private int chainId;
    private int maxOut;
    private int maxIn;
    /**
     * 友链跨链最大连接数
     */
    private int maxCrossOut;
    private int maxCrossIn;

    private int minAvailableCount;
    /**
     * 跨链网络是否激活,卫星链上的默认跨链true,
     * 友链默认false，在跨链模块请求时候这个属性才为true
     */
    private boolean isCrossActive=false;

    /**
     * 卫星链注册的跨链Group时为false
     */
    private boolean isSelf=true;
    /**
     * 是否卫星网络,只要卫星链上的节点就是true，如果是友链节点为false
     */
    private boolean isMoonNet=false;
    /**
     * self net-未连接或等待握手的节点
     */
    private  Map<String, Node> disConnectNodeMap=new ConcurrentHashMap<>();
    /**
     * self net-已经连接的节点
     */
    private  Map<String, Node> connectNodeMap=new ConcurrentHashMap<>();
    /**
     * 跨链-未连接或等待握手的节点
     */
     private  Map<String, Node> disConnectCrossNodeMap=new ConcurrentHashMap<>();
    /**
     * 跨链-已经连接的节点
     */
    private  Map<String, Node> connectCrossNodeMap=new ConcurrentHashMap<>();


    private volatile int hadConnectOut=0;

    private volatile int hadConnectIn=0;

    private volatile int hadCrossConnectOut=0;

    private volatile  int hadCrossConnectIn=0;

    /**
     * 0: wait ,1：ready， 2: WORKING
     */
    public final static int WAIT = 0;
    public final static int ready = 1;
    public final static int WORKING = 2;

    private volatile int status;

    public NodeGroup(long  magicNumber,int chainId,int maxIn,int maxOut,int minAvailableCount,boolean isMoonNet) {
        this.magicNumber = magicNumber;
        this.chainId=chainId;
        this.maxIn=maxIn;
        this.maxOut=maxOut;
        this.minAvailableCount=minAvailableCount;
        this.isMoonNet=isMoonNet;
        this.status=WAIT;
    }

    /**
     * 如果是连接的节点变更 connectChange=true，如果不是connectChange=false
     * @param node
     * @param connectChange
     */
    public void addDisConnetNode(Node node,boolean connectChange){
            if(!node.isCrossConnect()){
                disConnectNodeMap.put(node.getId(),node);
                if(connectChange) {
                    if (Node.OUT == node.getType()) {
                        hadConnectOut--;
                    } else {
                        hadConnectIn--;
                    }
                }
                if(connectNodeMap.containsKey(node.getId())){
                    connectNodeMap.remove(node.getId());
                }
            }else{
                disConnectCrossNodeMap.put(node.getId(),node);
                if(connectChange) {
                    if (Node.OUT == node.getType()) {
                        hadCrossConnectOut--;
                    } else {
                        hadCrossConnectIn--;
                    }
                }
                if(connectCrossNodeMap.containsKey(node.getId())){
                    connectCrossNodeMap.remove(node.getId());
                }
            }

    }
    /**
     * 如果是连接的节点变更 connectChange=true，如果不是connectChange=false
     * @param node
     * @param connectChange
     */
    public void addConnetNode(Node node,boolean connectChange){
        if(!node.isCrossConnect()) {
            connectNodeMap.put(node.getId(), node);
            if(connectChange) {
                if (Node.OUT == node.getType()) {
                    hadConnectOut++;
                } else {
                    hadConnectIn++;
                }
            }
            if (disConnectNodeMap.containsKey(node.getId())) {
                disConnectNodeMap.remove(node.getId());
            }
        }else{
            connectCrossNodeMap.put(node.getId(), node);
            if(connectChange) {
                if (Node.OUT == node.getType()) {
                    hadCrossConnectOut++;
                } else {
                    hadCrossConnectIn++;
                }
            }
            if (disConnectCrossNodeMap.containsKey(node.getId())) {
                disConnectCrossNodeMap.remove(node.getId());

            }
        }
    }


    public   Collection<Node> getConnectNodes(){

        return  connectNodeMap.values();
    }
    public   Collection<Node> getDisConnectNodes(){
        return  disConnectNodeMap.values();
    }

    public   Collection<Node> getDisConnectCrossNodes(){
        return  disConnectCrossNodeMap.values();
    }
    public   Collection<Node> getConnectCrossNodes(){
        return  connectCrossNodeMap.values();
    }



    public long getMagicNumber() {
        return magicNumber;
    }

    public void setMagicNumber(long magicNumber) {
        this.magicNumber = magicNumber;
    }

    public int getChainId() {
        return chainId;
    }

    public void setChainId(int chainId) {
        this.chainId = chainId;
    }

    public int getMaxOut() {
        return maxOut;
    }

    public void setMaxOut(int maxOut) {
        this.maxOut = maxOut;
    }

    public int getMaxIn() {
        return maxIn;
    }

    public void setMaxIn(int maxIn) {
        this.maxIn = maxIn;
    }

    public int getMinAvailableCount() {
        return minAvailableCount;
    }

    public void setMinAvailableCount(int minAvailableCount) {
        this.minAvailableCount = minAvailableCount;
    }

    public boolean isMoonNet() {
        return isMoonNet;
    }

    public void setMoonNet(boolean moonNet) {
        isMoonNet = moonNet;
    }



    public boolean isHadMaxOutFull() {
        return maxOut <= hadConnectOut;
    }

    public boolean isHadMaxInFull() {
        return maxIn <= hadConnectIn;
    }


    public boolean isHadCrossMaxOutFull() {
        return maxCrossOut <= hadCrossConnectOut;
    }

    public boolean isHadCrossMaxInFull() {
        return maxCrossIn <= hadCrossConnectIn;
    }

    public Map<String, Node> getDisConnectNodeMap() {
        return disConnectNodeMap;
    }

    public void setDisConnectNodeMap(Map<String, Node> disConnectNodeMap) {
        this.disConnectNodeMap = disConnectNodeMap;
    }

    public Map<String, Node> getConnectNodeMap() {
        return connectNodeMap;
    }

    public void setConnectNodeMap(Map<String, Node> connectNodeMap) {
        this.connectNodeMap = connectNodeMap;
    }

    public Map<String, Node> getDisConnectCrossNodeMap() {
        return disConnectCrossNodeMap;
    }

    public void setDisConnectCrossNodeMap(Map<String, Node> disConnectCrossNodeMap) {
        this.disConnectCrossNodeMap = disConnectCrossNodeMap;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isCrossActive() {
        return isCrossActive;
    }

    public void setCrossActive(boolean crossActive) {
        isCrossActive = crossActive;
    }

    public Map<String, Node> getConnectCrossNodeMap() {
        return connectCrossNodeMap;
    }

    public void setConnectCrossNodeMap(Map<String, Node> connectCrossNodeMap) {
        this.connectCrossNodeMap = connectCrossNodeMap;
    }

    public int getMaxCrossOut() {
        return maxCrossOut;
    }

    public void setMaxCrossOut(int maxCrossOut) {
        this.maxCrossOut = maxCrossOut;
    }

    public int getMaxCrossIn() {
        return maxCrossIn;
    }

    public void setMaxCrossIn(int maxCrossIn) {
        this.maxCrossIn = maxCrossIn;
    }

    public boolean isSelf() {
        return isSelf;
    }

    public void setSelf(boolean self) {
        isSelf = self;
    }
}
