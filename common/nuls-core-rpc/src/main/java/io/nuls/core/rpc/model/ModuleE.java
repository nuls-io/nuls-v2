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
    CMD(Constant.CMD,ModuleE.DOMAIN),
    KE(Constant.KERNEL, ModuleE.DOMAIN),
    CM(Constant.CHAIN, ModuleE.DOMAIN),
    AC(Constant.ACCOUNT,ModuleE.DOMAIN),
    NW(Constant.NETWORK, ModuleE.DOMAIN),
    CS(Constant.CONSENSUS, ModuleE.DOMAIN),
    BL(Constant.BLOCK, ModuleE.DOMAIN),
    LG(Constant.LEDGER, ModuleE.DOMAIN),
    TX(Constant.TRANSACTION, ModuleE.DOMAIN),
    EB(Constant.EVENT_BUS, ModuleE.DOMAIN),
    PU(Constant.PROTOCOL, ModuleE.DOMAIN),
    CC(Constant.CROSS_CHAIN, ModuleE.DOMAIN),
    SC(Constant.SMART_CONTRACT, ModuleE.DOMAIN),
    AP(Constant.API_MODULE, ModuleE.DOMAIN);

    public static final String DOMAIN = "nuls.io";

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

    ModuleE(String name, String domain) {
        this.abbr = name;
        this.name = name.toLowerCase();
        this.domain = domain;
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
