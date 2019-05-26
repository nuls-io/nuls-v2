package io.nuls.core.rpc.protocol;

import io.nuls.base.data.BaseBusinessMessage;

public interface MessageProcessor {

    /**
     * 获取要处理的消息对应的cmd
     *
     * @return
     */
    String getCmd();

    /**
     * 消息处理方法
     *
     * @param chainId
     * @param message
     */
    void process(int chainId, String nodeId, String message);

}
