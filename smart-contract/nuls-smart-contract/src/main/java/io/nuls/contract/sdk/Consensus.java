package io.nuls.contract.sdk;

import java.math.BigInteger;

public class Consensus {

    /**
     * 创建共识节点(创建节点者地址是合约调用者, 奖励地址是当前合约地址, 佣金比例是100%)
     *
     * @param packageAddress 出块地址
     * @param depositAmount 抵押金
     */
    public static native void registerAgent(String packageAddress, BigInteger depositAmount);

    /**
     * 停止共识节点(停止节点者地址是合约调用者)
     */
    public static native void stopAgent();

    /**
     * 加入共识节点(委托者地址是合约调用者)
     *
     * @param agentHash 创建节点的交易hash
     * @param depositAmount 抵押金
     */
    public static native void joinConsensus(String agentHash, BigInteger depositAmount);

    /**
     * 退出共识节点(退出委托者地址是合约调用者)
     *
     * @param agentHash 创建节点的交易hash
     */
    public static native void cancelDeposit(String agentHash);


    /**
     * 退出共识节点
     *
     * @param cancelDepositAddress 退出委托者地址
     * @param agentHash 创建节点的交易hash
     */
    public static native void cancelDeposit(String cancelDepositAddress, String agentHash);
    //TODO pierre

    /**
     * 加入共识节点
     *
     * @param joinAddress 委托者地址
     * @param agentHash 创建节点的交易hash
     * @param depositAmount 抵押金
     */
    public static native void joinConsensus(String joinAddress, String agentHash, BigInteger depositAmount);
    //TODO pierre
}
