package io.nuls.rpc.modulebootstrap;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-04 09:36
 * @Description: 功能描述
 */
public enum RpcModuleState {

    /**
     * 准备中
     */
    Start(0),
    /**
     * 已准备完毕，可给外部提供可用服务
     */
    Ready(1),
    /**
     * 所有依赖已进入准备完毕状态，模块开始工作
     */
    Running(2);

    final int index;

    RpcModuleState(int index){
        this.index = index;
    }

    public int getIndex(){
        return index;
    }

}
