package io.nuls.network.task;

import io.nuls.network.constant.NetworkParam;
import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.TaskManager;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.netty.container.NodesContainer;

import java.util.List;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * @author ljs
 */
public class RunAfterNetStableTask implements Runnable {
    private final NetworkParam networkParam = NetworkParam.getInstance();

    @Override
    public void run() {
        while (true) {
            //先执行本地的网络组
            int count = localNetAddrShare();
            //再执行跨链网络组
            count += crossNetAddrShare();
            if (count == 0) {
                try {
                    //让子线程执行一会儿
                    Thread.sleep(10000L);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            }
        }

    }

    private int localNetAddrShare() {
        List<NodeGroup> nodeGroups = NodeGroupManager.getInstance().getNodeGroups();
        int count = 0;
        for (NodeGroup nodeGroup : nodeGroups) {
            NodesContainer nodesContainer = nodeGroup.getLocalNetNodeContainer();
            if (nodesContainer.getStatus() == NodeGroup.WAIT1) {
                count++;
            } else if (!nodesContainer.isHadShareAddr()) {
                //执行分享地址线程
                TaskManager.getInstance().createShareAddressTask(nodeGroup, false);
                nodesContainer.setHadShareAddr(true);
            }
            try {
                //让子线程执行一会儿
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
        return count;
    }

    private int crossNetAddrShare() {
        List<NodeGroup> nodeGroups = NodeGroupManager.getInstance().getNodeGroups();
        int count = 0;
        for (NodeGroup nodeGroup : nodeGroups) {
            NodesContainer nodesContainer = nodeGroup.getCrossNodeContainer();
            if (nodesContainer.getStatus() == NodeGroup.WAIT1) {
                count++;
            } else if (!nodesContainer.isHadShareAddr()) {
                //执行分享地址线程
                TaskManager.getInstance().createShareAddressTask(nodeGroup, false);
                nodesContainer.setHadShareAddr(true);
            }
            try {
                //让子线程执行一会儿
                Thread.sleep(1000L);
            } catch (InterruptedException e) {
                Log.error(e);
            }
        }
        return count;
    }
}
