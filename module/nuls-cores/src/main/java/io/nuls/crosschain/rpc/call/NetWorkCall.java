package io.nuls.crosschain.rpc.call;

import io.nuls.base.RPCUtil;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.crosschain.base.message.base.BaseMessage;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.constant.NulsCrossChainErrorCode;
import io.nuls.crosschain.utils.LoggerUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static io.nuls.crosschain.base.constant.CommandConstant.*;

/**
 * Interaction with network modules
 * Interaction class with network module
 * @author tag
 * 2019/4/10
 */
public class NetWorkCall {

    /**
     * Broadcast messages to nodes on the network
     *
     * @param chainId The message is processed by which chain/chain id
     * @param message
     * @return
     */
    public static boolean broadcast(int chainId, BaseMessage message, String command,boolean isCross) {
        return broadcast(chainId, message, null, command,isCross);
    }

    /**
     * Broadcast messages to nodes on the network
     *
     * @param chainId chainId/chain id
     * @param message
     * @param excludeNodes Excluded nodes
     * @return
     */
    public static boolean broadcast(int chainId, BaseMessage message, String excludeNodes, String command,boolean isCross) {
        try {
            Map<String, Object> params = new HashMap<>(5);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("excludeNodes", excludeNodes);
            params.put("messageBody", RPCUtil.encode(message.serialize()));
            params.put("command", command);
            params.put("isCross", isCross);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_broadcast", params, NulsCrossChainConstant.RPC_TIME_OUT);
            if (!cmdResp.isSuccess()) {
                LoggerUtil.commonLog.error("Packing state failed to send!");
                return false;
            }
            return   (boolean)((HashMap) ((HashMap) cmdResp.getResponseData()).get("nw_broadcast")).get("value");
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return false;
        }
    }

    /**
     * Send messages to specified nodes
     *
     * @param chainId chainId/chain id
     * @param message
     * @param nodeId
     * @return
     */
    public static boolean sendToNode(int chainId, BaseMessage message, String nodeId, String command) {
        try {
            Map<String, Object> params = new HashMap<>(5);
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("nodes", nodeId);
            params.put("messageBody", RPCUtil.encode(message.serialize()));
            params.put("command", command);
            return ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_sendPeersMsg", params).isSuccess();
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            return false;
        }
    }

    /**
     * Get the number of network node connections
     *
     * @param chainId chain ID
     * @param isCross Whether to obtain the number of cross chain node connections/Whether to Get the Number of Connections across Chains
     * @return int    Number of connected nodes/Number of Connecting Nodes
     */
    public static int getAvailableNodeAmount(int chainId, boolean isCross) throws NulsException {
        Map<String, Object> callParams = new HashMap<>(4);
        callParams.put(Constants.CHAIN_ID, chainId);
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
     * Activate cross chain network
     * @param chainId    Activate cross chain chainsId
     * @param maxIn      How many nodes can be linked at most
     * @param maxOut     How many nodes can be linked at most
     * @param seedIps    Main network seed node list
     * */
    public static void activeCrossNet(int chainId,int maxOut,int maxIn,String seedIps )throws NulsException{
        Map<String, Object> callParams = new HashMap<>(4);
        callParams.put(Constants.CHAIN_ID, chainId);
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
