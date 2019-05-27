/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.core.rpc.model;

import java.util.Arrays;

/**
 * Module information
 *
 * @author tangyi
 */
public enum ModuleE {
    
    /**
     * prefix + name
     */
    CMD("cmd",Constant.CMD,ModuleE.DOMAIN),
    KE("ke", Constant.KERNEL, ModuleE.DOMAIN),
    CM("cm", Constant.CHAIN, ModuleE.DOMAIN),
    AC("ac", Constant.ACCOUNT,ModuleE.DOMAIN),
    NW("nw", Constant.NETWORK, ModuleE.DOMAIN),
    CS("cs", Constant.CONSENSUS, ModuleE.DOMAIN),
    BL("bl", Constant.BLOCK, ModuleE.DOMAIN),
    LG("lg", Constant.LEDGER, ModuleE.DOMAIN),
    TX("tx", Constant.TRANSACTION, ModuleE.DOMAIN),
    EB("eb", Constant.EVENT_BUS, ModuleE.DOMAIN),
    PU("pu", Constant.PROTOCOL, ModuleE.DOMAIN),
    CC("cc", Constant.CROSS_CHAIN, ModuleE.DOMAIN),
    SC("sc", Constant.SMART_CONTRACT, ModuleE.DOMAIN),
    AP("ap", Constant.API_MODULE, ModuleE.DOMAIN);

    public static final String DOMAIN = "Nuls";

    public static class Constant {
        
        public static final String KERNEL = "kernel";

        public static final String CHAIN = "chain-manager";

        public static final String ACCOUNT = "account";

        public static final String NETWORK = "network";

        public static final String CONSENSUS = "consensus";

        public static final String BLOCK = "block";

        public static final String LEDGER = "ledger";

        public static final String TRANSACTION = "transaction";

        public static final String EVENT_BUS = "eventbus";

        public static final String PROTOCOL = "protocol-update";

        public static final String CROSS_CHAIN = "cross-chain";

        public static final String SMART_CONTRACT = "smart-contract";

        public static final String API_MODULE = "api-module";

        public static final String CMD = "cmd-client";
    }

    public final String abbr;
    public final String name;
    public final String domain;

    public final String prefix;

    ModuleE(String prefix,String name, String domain) {
        this.abbr = name;
        this.name = name.toLowerCase();
        this.domain = domain;
        this.prefix = prefix;
    }

    public String getPrefix(){
        return prefix;
    }

    public String getName(){
        return this.name;
    }

    public static ModuleE valueOfAbbr(String abbr) {
        return Arrays.stream(ModuleE.values()).filter(m -> m.abbr.equals(abbr)).findFirst().orElseThrow(() -> new IllegalArgumentException("can not found abbr of " + abbr));
    }

    public static boolean hasOfAbbr(String abbr){
        return Arrays.stream(ModuleE.values()).anyMatch(m -> m.abbr.equals(abbr));
    }

    @Override
    public String toString() {
        return domain + "/" + name + "/" + abbr;
    }
}
