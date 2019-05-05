package io.nuls.eventbus.rpc.invoke;

import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.message.Response;
import static io.nuls.eventbus.util.EbLog.Log;

/**
 * Call back command to be executed when the event is sent to subscriber
 * @author naveen
 */
public class EventAuditInvoke extends BaseInvoke {

    /**
     *
     * @param response from subscriber
     */
    @Override
    public void callBack(Response response) {
        Log.info("Call back invoke:"+response);
        //TODO implement if any audit related info is required
    }
}
