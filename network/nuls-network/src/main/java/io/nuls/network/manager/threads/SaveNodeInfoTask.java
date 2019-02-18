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
package io.nuls.network.manager.threads;

import io.nuls.network.manager.NodeGroupManager;
import io.nuls.network.manager.StorageManager;
import io.nuls.network.model.NodeGroup;
import io.nuls.network.netty.container.NodesContainer;
import io.nuls.network.storage.DbService;

import java.util.List;

import static io.nuls.network.utils.LoggerUtil.Log;

/**
 * 维护节点高度的定时任务
 *
 * @author: ln
 * @date: 2018/12/8
 */
public class SaveNodeInfoTask implements Runnable {

    private final NodeGroupManager nodeGroupManager = NodeGroupManager.getInstance();


    public SaveNodeInfoTask() {

    }

    /**
     * 每5分钟一次，将整个NodeContainer对象存储到文件中
     */
    @Override
    public void run() {
        try {
            doCommit();
        } catch (Exception e) {
            Log.error(e);
        }
    }

    private void doCommit() {
        DbService networkStorageService = StorageManager.getInstance().getDbService();
        List<NodeGroup> list = nodeGroupManager.getNodeGroups();
        for (NodeGroup nodeGroup : list) {
            networkStorageService.saveNodes(nodeGroup);
        }
    }
}
