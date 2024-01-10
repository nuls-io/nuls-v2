package io.nuls.core.rpc.netty.channel;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-01 11:13
 * @Description: Function Description
 */
@FunctionalInterface
public interface EventListener {

    void apply();

}
