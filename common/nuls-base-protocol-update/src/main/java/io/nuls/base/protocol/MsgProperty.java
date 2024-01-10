package io.nuls.base.protocol;

/**
 * Transaction attributes
 * Transaction attribute
 *
 * @author tag
 * 2019/1/16
 */
public enum MsgProperty {

    /**
     * End message returned at the end of bulk block retrieval
     */
    COMPLETE_MESSAGE("complete", "complete"),
    /**
     * Complete block message
     */
    BLOCK_MESSAGE("block", "block"),
    /**
     * Based on blocksHASHGet blocks
     */
    GET_BLOCK_MESSAGE("getBlock", "getBlock"),
    /**
     * Forwarding blocks
     */
    FORWARD_SMALL_BLOCK_MESSAGE("forward", "forward"),
    /**
     * Batch acquisition of community block messages
     */
    GET_BLOCKS_BY_HEIGHT_MESSAGE("getBlocks", "getBlocks"),
    /**
     * Batch acquisition of transactions
     */
    GET_TXGROUP_MESSAGE("getTxs", "getTxs"),
    /**
     * Community block messages
     */
    SMALL_BLOCK_MESSAGE("sBlock", "sBlock"),
    /**
     * Get community block messages
     */
    GET_SMALL_BLOCK_MESSAGE("getsBlock", "getsBlock"),
    /**
     * Batch transaction messages
     */
    TXGROUP_MESSAGE("txs", "txs");

    private final String protocolCmd;
    private final String handler;

    MsgProperty(String protocolCmd, String handler) {
        this.protocolCmd = protocolCmd;
        this.handler = handler;
    }
}
