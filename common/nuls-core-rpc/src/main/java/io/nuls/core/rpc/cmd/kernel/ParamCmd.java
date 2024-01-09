package io.nuls.core.rpc.cmd.kernel;

import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.core.annotation.Component;

import java.util.Map;

/**
 * Test interface, testingrpcService stability
 *
 * @author tag
 * @version 1.0
 * @date 19-1-22
 */

@Component
public class ParamCmd extends BaseCmd {
    @CmdAnnotation(cmd = "paramTestCmd", version = 1.0, scope = Constants.PUBLIC, description = "")
    @Parameter(parameterName = "intCount", parameterType = "int", parameterValidRange = "[0,65535]")
    @Parameter(parameterName = "byteCount", parameterType = "byte", parameterValidRange = "[-128,127]")
    @Parameter(parameterName = "shortCount", parameterType = "short", parameterValidRange = "[0,32767]")
    @Parameter(parameterName = "longCount", parameterType = "long", parameterValidRange = "[0,55555555]")
    public Response paramTest(Map map) {
        int count = Integer.parseInt(map.get("intCount").toString());
        int sum = 0;
        for (int i = 0; i < count; i++) {
            sum += i;
        }
        return success(sum);
    }
}
