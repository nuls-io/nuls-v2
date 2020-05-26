package io.nuls.crosschain.base.utils.enumeration;

public enum ChainInfoChangeType {
    /**
     * 将已注册跨链的所有链信息广播给新注册链
     * */
    INIT_REGISTER_CHAIN(0),

    /**
     * 将新注册链信息广播给已注册跨链的链
     * */
    NEW_REGISTER_CHAIN(1),

    /**
     * 链资产变更
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
