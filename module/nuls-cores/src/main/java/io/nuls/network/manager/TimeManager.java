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

package io.nuls.network.manager;

import io.nuls.common.NulsCoresConfig;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import io.nuls.core.thread.ThreadUtils;
import io.nuls.network.constant.NetworkConstant;
import io.nuls.network.model.Node;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.model.dto.NetTimeUrl;
import io.nuls.network.model.message.GetTimeMessage;
import io.nuls.network.utils.LoggerUtil;
import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Time service category：Used to synchronize network standard time
 * Time service class:Used to synchronize network standard time.
 *
 * @author vivi & lan
 */
public class TimeManager {
    private static TimeManager instance = new TimeManager();
    /**
     * NTPServer websiteurlCollection for synchronizing network time
     */
    private List<String> ntpSeverUrlList = new ArrayList<>();

    private List<NetTimeUrl> netTimeSevers = new CopyOnWriteArrayList<>();
    /**
     * Trigger point for time offset difference, exceeding this value will cause local time reset in milliseconds
     * Time migration gap trigger point, which can cause local time reset, unit milliseconds.
     **/
    public static final long TIME_OFFSET_BOUNDARY = 3000L;
    /**
     * Waiting for peer node reply time
     **/
    public static final long TIME_WAIT_PEER_RESPONSE = 2000L;
    /**
     * Resynchronization interval
     * Resynchronize the interval.
     * 2 minutes;
     */
    public static final long NET_REFRESH_TIME = 2 * 60 * 1000L;
    /**
     * Network time offset value
     */
    public static long netTimeOffset;
    /**
     * Last synchronization time point
     * The last synchronization point.
     */
    public static long lastSyncTime;

    //Inquire about the maximum number of network times for peer nodes
    private static final int MAX_REQ_PEER_NUMBER = 8;
    private static Map<String, Long> peerTimesMap = new ConcurrentHashMap<>();
    private static long currentRequestId;
    private long syncStartTime;
    private long syncEndTime;
    private long netTime;

    public static TimeManager getInstance() {
        return instance;
    }

    private TimeManager() {
        if (0 == ntpSeverUrlList.size()) {
            NulsCoresConfig networkConfig = SpringLiteContext.getBean(NulsCoresConfig.class);
            String timeServers = networkConfig.getTimeServers();
            if (StringUtils.isNotBlank(timeServers)) {
                String[] urlArray = timeServers.split(NetworkConstant.COMMA);
                ntpSeverUrlList.addAll(Arrays.asList(urlArray));
            }
        }
    }

    /**
     * During initialization, synchronize the network time and place the successfully synchronized ones in the set backup
     * And sort according to the corresponding time
     */
    public void initWebTimeServer() {
        CountDownLatch latch = new CountDownLatch(ntpSeverUrlList.size());
        for (String url : ntpSeverUrlList) {
            ThreadUtils.asynExecuteRunnable(()->{
                long syncStartTime = System.currentTimeMillis();
                long netTime = getWebTime(url);
                if (netTime > 0) {
                    NetTimeUrl netTimeUrl = new NetTimeUrl(url, (System.currentTimeMillis() - syncStartTime));
                    netTimeSevers.add(netTimeUrl);
                }
                latch.countDown();
            });
        }
        try {
            latch.await(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.error("An exception occurred while waiting to obtain network time");
            System.exit(0);
        }
        if(netTimeSevers.size() < 3){
            LoggerUtil.COMMON_LOG.warn("Available servers are less than3individual");
        }
        Collections.sort(netTimeSevers);
        LoggerUtil.COMMON_LOG.info("Initialize time server completed");
        LoggerUtil.COMMON_LOG.info("=".repeat(100));
        netTimeSevers.forEach(d->{
            LoggerUtil.COMMON_LOG.info("site:{} time consuming:{}",d.getUrl(),d.getTime());
        });
        LoggerUtil.COMMON_LOG.info("=".repeat(100));
    }

    /**
     * Synchronize network time
     * If successfully synchronized to three, it is sufficient
     */
    public void syncWebTime() {
        int count = 0;
        long[] times = {0, 0, 0};
        for (int i = 0; i < netTimeSevers.size(); i++) {
            syncStartTime = System.currentTimeMillis();
            netTime = getWebTime(netTimeSevers.get(i).getUrl());
            if (netTime == 0) {
                continue;
            }
            syncEndTime = System.currentTimeMillis();
            times[count] = (netTime + (syncEndTime - syncStartTime) / 2) - syncEndTime;
            count++;
            /*
             * have3Once the network time is normal, it can be returned
             */
            if (count >= 3) {
                break;
            }
        }
        if (count == 3) {
            calNetTimeOffset(times[0], times[1], times[2]);
        } else {
            //Obtaining time from peer-to-peer networks
            LoggerUtil.COMMON_LOG.debug("count={} syncPeerTime .....", count);
            syncPeerTime();
        }
        lastSyncTime = currentTimeMillis();
    }

    public static void calNetTimeOffset(long time1, long time2, long time3) {
        //3Remove from network time Compared to others2Deviation greater than500msThe time value of.
        long differMs = 500;
        int count = 3;
        if (Math.abs(time1 - time2) > differMs && Math.abs(time1 - time3) > differMs) {
            time1 = 0;
            count--;
        }
        if (Math.abs(time2 - time1) > differMs && Math.abs(time2 - time3) > differMs) {
            time2 = 0;
            count--;
        }
        if (Math.abs(time3 - time1) > differMs && Math.abs(time3 - time2) > differMs) {
            time3 = 0;
            count--;
        }
        if (count > 1) {
            netTimeOffset = (time1 + time2 + time3) / count;
        }
    }

    /**
     * Synchronize time with peer nodes
     */
    private synchronized void syncPeerTime() {
        //Set requestid
        currentRequestId = System.currentTimeMillis();
        long beginTime = currentRequestId;
        // peerTimesMap empty
        peerTimesMap.clear();
        //Randomly send requests
        List<NodeGroup> list = NodeGroupManager.getInstance().getNodeGroups();
        if (list.size() == 0) {
            return;
        }
        Collections.shuffle(list);
        int count = 0;
        boolean nodesEnough = false;
        for (NodeGroup nodeGroup : list) {
            Collection<Node> nodes = nodeGroup.getLocalNetNodeContainer().getConnectedNodes().values();
            for (Node node : nodes) {
                sendGetTimeMessage(node);
                count++;
                if (count >= MAX_REQ_PEER_NUMBER) {
                    nodesEnough = true;
                    break;
                }
            }
            if (nodesEnough) {
                break;
            }
        }
        if (count == 0) {
            return;
        }

        //Quantity or time meets the requirements
        long intervalTime = 0;
        while (peerTimesMap.size() < MAX_REQ_PEER_NUMBER && intervalTime < TIME_WAIT_PEER_RESPONSE) {
            try {
                Thread.sleep(500L);
            } catch (InterruptedException e) {
                LoggerUtil.COMMON_LOG.error(e);
                Thread.currentThread().interrupt();
            }
            intervalTime = System.currentTimeMillis() - beginTime;
        }

        int size = peerTimesMap.size();
        if (size > 0) {
            long sum = 0L;
            Set set = peerTimesMap.keySet();
            //calculate
            for (Object aSet : set) {
                sum += peerTimesMap.get(aSet.toString());
            }
            netTimeOffset = sum / size;
            LoggerUtil.COMMON_LOG.debug("syncPeerTime netTimeOffset={}", netTimeOffset);
        }
    }


    public static void addPeerTime(String nodeId, long requestId, long time) {
        if (currentRequestId == requestId) {
            if (MAX_REQ_PEER_NUMBER > peerTimesMap.size()) {
                long localBeforeTime = currentRequestId;
                long localEndTime = System.currentTimeMillis();
                long value = (time + (localEndTime - localBeforeTime) / 2) - localEndTime;
                peerTimesMap.put(nodeId, value);
            }
        }
    }

    private void sendGetTimeMessage(Node node) {
        GetTimeMessage getTimeMessage = MessageFactory.getInstance().buildTimeRequestMessage(node.getMagicNumber(), currentRequestId);
        MessageManager.getInstance().sendHandlerMsg(getTimeMessage, node, true);
    }

    /**
     * Get network time
     *
     * @return long
     */
    private long getWebTime(String address) {
        try {
            NTPUDPClient client = new NTPUDPClient();
            client.setDefaultTimeout(500);
            client.open();
            client.setSoTimeout(500);
            InetAddress inetAddress = InetAddress.getByName(address);
            //Log.debug("start ask time....");
            TimeInfo timeInfo = client.getTime(inetAddress);
            //Log.debug("done!");
            return timeInfo.getMessage().getTransmitTimeStamp().getTime();
        } catch (Exception e) {
            LoggerUtil.COMMON_LOG.warn("address={} sync time fail", address);
            return 0L;
        }
    }

    /**
     * Get the current network time in milliseconds
     * Gets the current network time in milliseconds.
     *
     * @return long
     */
    public static long currentTimeMillis() {
        return System.currentTimeMillis() + netTimeOffset;
    }

}
