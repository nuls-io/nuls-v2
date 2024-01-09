package io.nuls.crosschain.base.utils.enumeration;

public enum ChainInfoChangeType {
    /**
     * Broadcast all registered cross chain chain information to the new registered chain
     * */
    INIT_REGISTER_CHAIN(0),

    /**
     * Broadcast new registration chain information to registered cross chain chains
     * */
    NEW_REGISTER_CHAIN(1),

    /**
     * Chain asset change
     * */
    REGISTERED_CHAIN_CHANGE(2);

    private final int type;

    ChainInfoChangeType(int type){
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
