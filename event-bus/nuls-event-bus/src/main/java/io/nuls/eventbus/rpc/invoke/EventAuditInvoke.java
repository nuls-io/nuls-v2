package io.nuls.eventbus.rpc.invoke;

import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.log.Log;

/**
 * @author naveen
 */
public class EventAuditInvoke extends BaseInvoke {

    @Override
    public void callBack(Response response) {
        Log.info("Call back invoke:"+response);
        //TODO implement if any audit related info is required
    }
}
