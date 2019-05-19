package io.nuls.contract.sdk;

/**
 * 合约接口，合约类实现这个接口
 */
public interface Contract {

    /**
     * 直接向合约转账，会触发这个方法，默认不做任何操作，可以重载这个方法。
     */
    default void _payable() {
    }

    /**
     * 1. 当共识节点奖励地址是合约地址时，会触发这个方法，参数是区块奖励地址明细 eg. [[address, amount], [address, amount], ...]
     * 2. 当委托节点地址是合约地址时，会触发这个方法，参数是合约地址和奖励金额 eg. [[address, amount]]
     *
     */
    default void _payable(String[][] args) {
    }

}
