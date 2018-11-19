package io.nuls.rpc.model;

/**
 * Module information
 *
 * @author tangyi
 */
public enum ModuleE {
    /**
     * prefix + name
     */
    CM("cm", "Chain Manager"),
    AC("ac", "Account Manager"),
    NW("nw", "Network"),
    CS("cs", "Consensus"),
    BL("bl", "Block"),
    LG("lg", "Ledger"),
    TX("tx", "Transaction")
    ;

    public final String prefix;
    public final String name;

    ModuleE(String prefix, String name) {
        this.prefix = prefix;
        this.name = name;
    }
}
