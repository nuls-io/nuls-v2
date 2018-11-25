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
    KE("ke", "Kernel", "nuls.io"),
    CM("cm", "Chain Manager", "nuls.io"),
    AC("ac", "Account Manager", "nuls.io"),
    NW("nw", "Network", "nuls.io"),
    CS("cs", "Consensus", "nuls.io"),
    BL("bl", "Block", "nuls.io"),
    LG("lg", "Ledger", "nuls.io"),
    TX("tx", "Transaction", "nuls.io");

    public final String abbr;
    public final String name;
    public final String domain;

    ModuleE(String abbr, String name, String domain) {
        this.abbr = abbr;
        this.name = name;
        this.domain = domain;
    }
}
