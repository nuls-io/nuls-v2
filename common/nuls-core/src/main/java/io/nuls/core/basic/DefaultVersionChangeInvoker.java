package io.nuls.core.basic;

import io.nuls.core.log.Log;

public class DefaultVersionChangeInvoker implements VersionChangeInvoker{
    @Override
    public void process() {
        Log.info("DefaultVersionChangeInvoker trigger");
    }
}
