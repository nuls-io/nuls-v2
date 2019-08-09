/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.transaction.utils;

import io.nuls.core.crypto.HexUtil;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Charlie
 * @date: 2019/5/9
 */
public class TxDuplicateRemoval {

    private static HashSetDuplicateProcessor processorOfTx = new HashSetDuplicateProcessor(1000000);

    public static boolean exist(String hash) {
        return processorOfTx.contains(hash);
    }

    /**
     * 加入，返回false则表示已存在
     * @param hash
     * @return
     */
    public static boolean insertAndCheck(String hash) {
        return processorOfTx.insertAndCheck(hash);
    }

    /**
     * 记录向本节点发送完整交易的其他网络节点，转发hash时排除掉
     */
    private static Map<String, StringBuffer> forwardHashExcludeNodesMap = new ConcurrentHashMap<>();

    /**
     * 超过指定数量则清理
     */
    private static int maxSize = 20000;

    public static void putExcludeNode(String hash, String newExcludeNode){
        if(forwardHashExcludeNodesMap.size() >= maxSize){
            forwardHashExcludeNodesMap.clear();
        }
        StringBuffer excludeNodes = forwardHashExcludeNodesMap.putIfAbsent(hash, new StringBuffer(newExcludeNode));
        if(null != excludeNodes){
            excludeNodes.append(",").append(newExcludeNode);
            forwardHashExcludeNodesMap.put(hash, excludeNodes);
        }
    }

    public static String getExcludeNode(String hash){
        StringBuffer excludeNodes = forwardHashExcludeNodesMap.get(hash);
        if(null != excludeNodes){
            return excludeNodes.toString();
        }
        return null;
    }

    public static void removeExcludeNode(String hash){
        forwardHashExcludeNodesMap.remove(hash);
    }

    public static void removeExcludeNode(List<byte[]> hashs){
        for(byte[] hash : hashs){
            forwardHashExcludeNodesMap.remove(HexUtil.encode(hash));
        }
    }

    public static int sizeExcludeNode(){
        return forwardHashExcludeNodesMap.size();
    }
}
