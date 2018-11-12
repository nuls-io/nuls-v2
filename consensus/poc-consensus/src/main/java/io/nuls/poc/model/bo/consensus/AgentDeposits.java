package io.nuls.poc.model.bo.consensus;

import io.nuls.base.data.NulsDigestData;
import io.nuls.poc.model.bo.tx.txdata.Deposit;

import java.util.ArrayList;
import java.util.List;

public class AgentDeposits {
    /**
     * 节点Hash
     * */
    private static  NulsDigestData agentHash;

    /**
     * 该节点所有委托信息列表
     * */
    public static  List<Deposit> allDepositList = new ArrayList<>();

    /**
     * 该节点处于共识中的委托信息列表
     * */
    public static  List<Deposit> validDepositList = new ArrayList<>();
}
