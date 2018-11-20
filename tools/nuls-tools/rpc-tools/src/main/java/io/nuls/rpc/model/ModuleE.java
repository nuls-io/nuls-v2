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
    KE("ke", "Kernel"),
    CM("cm", "Chain Manager"),
    AC("ac", "Account Manager"),
    NW("nw", "Network"),
    CS("cs", "Consensus"),
    BL("bl", "Block"),
    LG("lg", "Ledger"),
    TX("tx", "Transaction");

    public final String abbr;
    public final String name;

    ModuleE(String abbr, String name) {
        this.abbr = abbr;
        this.name = name;
    }
}
