package io.nuls.block.rpc.callback;

import io.nuls.block.manager.ContextManager;
import io.nuls.block.model.ChainContext;
import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.message.Response;

public class ProtocolVersionInvoke extends BaseInvoke {

    private int chainId;
    public ProtocolVersionInvoke(int chainId) {
        this.chainId = chainId;
    }

    @Override
    public void callBack(Response response) {
        ChainContext context = ContextManager.getContext(chainId);
        if (response.isSuccess()) {
            short version = Short.parseShort((String) response.getResponseData());
            context.setVersion(version);
        }
    }
}
