package io.nuls.core.rpc.modulebootstrap;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-04 09:36
 * @Description: Function Description
 */
public enum RpcModuleState {

    /**
     * Preparing
     */
    Start(0),
    /**
     * Ready to provide available services to external parties
     */
    Ready(1),
    /**
     * All dependencies have entered the prepared state, and the module is starting to work
     */
    Running(2);

    final int index;

    RpcModuleState(int index) {
        this.index = index;
    }

    public int getIndex() {
        return index;
    }

}
