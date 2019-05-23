package io.nuls.base.basic;

import io.nuls.base.data.BaseBusinessMessage;

public interface MessageProcessor {

    /**
     * 获取唯一消息名
     *
     * @return
     */
    String getName();

    /**
     * 消息处理方法
     *
     * @param chainId
     * @param message
     * @param objects
     */
    void process(int chainId, BaseBusinessMessage message, Object... objects);

}
