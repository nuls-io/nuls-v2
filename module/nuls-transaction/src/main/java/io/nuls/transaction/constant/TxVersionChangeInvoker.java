package io.nuls.transaction.constant;

import io.nuls.core.basic.VersionChangeInvoker;
import io.nuls.core.log.Log;

public class TxVersionChangeInvoker implements VersionChangeInvoker {
    @Override
    public void process() {
        Log.info("TxVersionChangeInvoker trigger");
    }
}
