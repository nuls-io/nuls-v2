package io.nuls.base.protocol;

/**
 * 交易属性
 * Transaction attribute
 *
 * @author tag
 * 2019/1/16
 */
public enum MsgProperty {

    /**
     * 批量获取区块结束时返回的结束消息
     */
    COMPLETE_MESSAGE("complete", "complete"),
    /**
     * 完整的区块消息
     */
    BLOCK_MESSAGE("block", "block"),
    /**
     * 根据区块HASH获取区块
     */
    GET_BLOCK_MESSAGE("getBlock", "getBlock"),
    /**
     * 转发区块
     */
    FORWARD_SMALL_BLOCK_MESSAGE("forward", "forward"),
    /**
     * 批量获取小区块消息
     */
    GET_BLOCKS_BY_HEIGHT_MESSAGE("getBlocks", "getBlocks"),
    /**
     * 批量获取交易
     */
    GET_TXGROUP_MESSAGE("getTxs", "getTxs"),
    /**
     * 小区块消息
     */
    SMALL_BLOCK_MESSAGE("sBlock", "sBlock"),
    /**
     * 获取小区块消息
     */
    GET_SMALL_BLOCK_MESSAGE("getsBlock", "getsBlock"),
    /**
     * 批量交易消息
     */
    TXGROUP_MESSAGE("txs", "txs");

    private final String protocolCmd;
    private final String handler;

    MsgProperty(String protocolCmd, String handler) {
        this.protocolCmd = protocolCmd;
        this.handler = handler;
    }
}
