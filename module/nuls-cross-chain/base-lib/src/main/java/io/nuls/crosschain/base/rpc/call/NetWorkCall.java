package io.nuls.crosschain.base.rpc.call;

import io.nuls.core.log.Log;
import io.nuls.crosschain.base.message.base.BaseMessage;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.crosschain.base.constant.CommandConstant.*;

/**
 * 与网络模块交互类
 * Interaction class with network module
 * @author tag
 * 2019/4/10
 */
public class NetWorkCall {
    /**
     * 注册消息处理器
     * @return
     */
    public static boolean register() {
        try {
            Map<String, Object> map = new HashMap<>(2);
            List<Map<String, String>> cmds = new ArrayList<>();
            map.put("role", ModuleE.CC.abbr);
            List<String> list = List.of(GET_CTX_MESSAGE,GET_OTHER_CTX_MESSAGE,NEW_CTX_MESSAGE,NEW_OTHER_CTX_MESSAGE,VERIFY_CTX_MESSAGE,CTX_VERIFY_RESULT_MESSAGE,GET_CTX_STATE_MESSAGE,CTX_STATE_MESSAGE,BROAD_CTX_HASH_MESSAGE,BROAD_CTX_SIGN_MESSAGE,GET_CIRCULLAT_MESSAGE);
            for (String s : list) {
                Map<String, String> cmd = new HashMap<>(2);
                cmd.put("protocolCmd", s);
                cmd.put("thread", s);
                cmds.add(cmd);
            }
            map.put("protocolCmds", cmds);
            boolean success = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_protocolRegister", map).isSuccess();
            while (!success) {
                Thread.sleep(1000L);
                success = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_protocolRegister", map).isSuccess();
            }
            return true;
        } catch (Exception e) {
            Log.error(e);
        }
        return false;
    }

    /**
     * 给网络上节点广播消息
     *
     * @param chainId 链Id/chain id
     * @param message
     * @return
     */
    public static boolean broadcast(int chainId, BaseMessage message, String command,boolean isCross) {
        return broadcast(chainId, message, null, command,isCross);
    }

    /**
     * 给网络上节点广播消息
     *
     * @param chainId 链Id/chain id
     * @param message
     * @param excludeNodes 排除的节点
     * @return
     */
    public static boolean broadcast(int chainId, BaseMessage message, String excludeNodes, String command,boolean isCross) {
        try {
            Map<String, Object> params = new HashMap<>(5);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("excludeNodes", excludeNodes);
            params.put("messageBody", RPCUtil.encode(message.serialize()));
            params.put("command", command);
            params.put("isCross", isCross);
            boolean success = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_broadcast", params).isSuccess();
            return success;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }

    /**
     * 给指定节点发送消息
     *
     * @param chainId 链Id/chain id
     * @param message
     * @param nodeId
     * @return
     */
    public static boolean sendToNode(int chainId, BaseMessage message, String nodeId, String command) {
        try {
            Map<String, Object> params = new HashMap<>(5);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put("chainId", chainId);
            params.put("nodes", nodeId);
            params.put("messageBody", RPCUtil.encode(message.serialize()));
            params.put("command", command);
            boolean success = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_sendPeersMsg", params).isSuccess();
            return success;
        } catch (Exception e) {
            Log.error(e);
            return false;
        }
    }
}
