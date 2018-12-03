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
    CM("cm", "Chain", "nuls.io"),
    AC("ac", "Account", "nuls.io"),
    NW("nw", "Network", "nuls.io"),
    CS("cs", "Consensus", "nuls.io"),
    BL("bl", "Block", "nuls.io"),
    LG("lg", "Ledger", "nuls.io"),
    TX("tx", "Transaction", "nuls.io"),
    EB("eb", "EventBus", "nuls.io"),
    TEST("test", "I am test", "nuls.io");

    public final String abbr;
    public final String name;
    public final String domain;

    ModuleE(String abbr, String name, String domain) {
        this.abbr = abbr;
        this.name = name;
        this.domain = domain;
    }
}
