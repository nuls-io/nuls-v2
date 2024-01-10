package io.nuls.base.protocol;

public interface MessageProcessor {

    /**
     * Obtain the corresponding message to be processedcmd
     *
     * @return
     */
    String getCmd();

    /**
     * Message processing methods
     *
     * @param chainId
     * @param message
     */
    void process(int chainId, String nodeId, String message);

}
