package io.nuls.core.rpc.cmd.common;

import io.nuls.base.data.BaseBusinessMessage;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.ObjectUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.protocol.MessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息统一分发，各个有消息要处理的模块写具体实现
 *
 *
 * @author captain
 * @version 1.0
 * @date 2019/5/23 21:05
 */
@Component
public class MessageDispatcher extends BaseCmd {

    private List<MessageProcessor> processors;

    public List<MessageProcessor> getProcessors() {
        return processors;
    }

    public void setProcessors(List<MessageProcessor> processors) {
        this.processors = processors;
    }

    /**
     * 获取最新主链高度
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = BaseConstant.MSG_PROCESS, version = 1.0, description = "")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "cmd", parameterType = "String")
    @Parameter(parameterName = "msg", parameterType = "String")
    public Response msgProcess(Map params) {
        ObjectUtils.canNotEmpty(params.get(Constants.CHAIN_ID), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("cmd"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("msg"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        int chainId = Integer.parseInt(params.get(Constants.CHAIN_ID).toString());
        String cmd = (String) params.get("cmd");
        String msgStr = (String) params.get("msg");
        BaseBusinessMessage message = RPCUtil.getInstanceRpcStr(msgStr, BaseBusinessMessage.class);
        for (MessageProcessor processor : processors) {
            if (cmd.equals(processor.getName())) {
                processor.process(chainId, message);
            }
        }
        Map resultMap = new HashMap<>(2);
        resultMap.put("value", true);
        return success(resultMap);
    }
}