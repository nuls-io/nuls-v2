package io.nuls.crosschain.nuls.rpc.call;

import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.crosschain.base.message.base.BaseMessage;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConstant;
import io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.util.RPCUtil;
import io.nuls.core.exception.NulsException;

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
            List<String> list = List.of(GET_CTX_MESSAGE,GET_OTHER_CTX_MESSAGE,NEW_CTX_MESSAGE,NEW_OTHER_CTX_MESSAGE,VERIFY_CTX_MESSAGE,CTX_VERIFY_RESULT_MESSAGE,GET_CTX_STATE_MESSAGE,CTX_STATE_MESSAGE,BROAD_CTX_HASH_MESSAGE,BROAD_CTX_SIGN_MESSAGE,GET_CIRCULLAT_MESSAGE,REGISTERED_CHAIN_MESSAGE,NulsCrossChainConstant.GET_REGISTERED_CHAIN_MESSAGE);
            for (String s : list) {
                Map<String, String> cmd = new HashMap<>(2);
                cmd.put("protocolCmd", s);
                cmd.put("handler", s);
                cmds.add(cmd);
            }
            map.put("protocolCmds", cmds);
            boolean success = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_protocolRegister", map).isSuccess();
            while (!success) {
                Thread.sleep(1000L);
                success = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_protocolRegister", map).isSuccess();
            }
            return success;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 给网络上节点广播消息
     *
     * @param chainId 该消息由那条链处理/chain id
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
            return ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_broadcast", params, NulsCrossChainConstant.RPC_TIME_OUT).isSuccess();
        } catch (Exception e) {
            e.printStackTrace();
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
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取网络节点连接数
     *
     * @param chainId chain ID
     * @param isCross 是否获取跨链节点连接数/Whether to Get the Number of Connections across Chains
     * @return int    连接节点数/Number of Connecting Nodes
     */
    public static int getAvailableNodeAmount(int chainId, boolean isCross) throws NulsException {
        Map<String, Object> callParams = new HashMap<>(4);
        callParams.put("chainId", chainId);
        callParams.put("isCross", isCross);
        try {
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_getChainConnectAmount", callParams);
            if (!callResp.isSuccess()) {
                throw new NulsException(NulsCrossChainErrorCode.INTERFACE_CALL_FAILED);
            }
            HashMap callResult = (HashMap) ((HashMap) callResp.getResponseData()).get("nw_getChainConnectAmount");
            return (Integer) callResult.get("connectAmount");
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }

    /**
     * 激活跨链网络
     * @param chainId    激活跨链的链Id
     * @param maxIn      最多被多少节点链接
     * @param maxOut     最多链接多少哥个节点
     * @param seedIps    主网种子节点列表
     * */
    public static void activeCrossNet(int chainId,int maxOut,int maxIn,String seedIps )throws NulsException{
        Map<String, Object> callParams = new HashMap<>(4);
        callParams.put("chainId", chainId);
        callParams.put("maxOut", maxOut);
        callParams.put("maxIn", maxIn);
        callParams.put("seedIps", seedIps);
        try {
            Response callResp = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_activeCross", callParams);
            if (!callResp.isSuccess()) {
                Log.error(JSONUtils.obj2json(callResp));
                throw new NulsException(NulsCrossChainErrorCode.INTERFACE_CALL_FAILED);
            }
        } catch (Exception e) {
            throw new NulsException(e);
        }
    }
}
