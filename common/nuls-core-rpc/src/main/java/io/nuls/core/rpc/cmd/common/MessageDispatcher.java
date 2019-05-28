package io.nuls.core.rpc.cmd.common;

import io.nuls.core.constant.BaseConstant;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.model.ObjectUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.CmdAnnotation;
import io.nuls.core.rpc.model.Parameter;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.protocol.MessageProcessor;

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
public final class MessageDispatcher extends BaseCmd {

    private List<MessageProcessor> processors;

    public List<MessageProcessor> getProcessors() {
        return processors;
    }

    public void setProcessors(List<MessageProcessor> processors) {
        processors.forEach(e -> Log.info("register MessageProcessor-" + e.toString()));
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
    @Parameter(parameterName = "nodeId", parameterType = "String")
    @Parameter(parameterName = "cmd", parameterType = "String")
    @Parameter(parameterName = "messageBody", parameterType = "String")
    public Response msgProcess(Map params) {
        ObjectUtils.canNotEmpty(params.get(Constants.CHAIN_ID), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("nodeId"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("cmd"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        ObjectUtils.canNotEmpty(params.get("messageBody"), CommonCodeConstanst.PARAMETER_ERROR.getMsg());
        int chainId = Integer.parseInt(params.get(Constants.CHAIN_ID).toString());
        String nodeId = (String) params.get("nodeId");
        String cmd = (String) params.get("cmd");
        String msgStr = (String) params.get("messageBody");
        for (MessageProcessor processor : processors) {
            if (cmd.equals(processor.getCmd())) {
                processor.process(chainId, nodeId, msgStr);
            }
        }
        return success();
    }
}