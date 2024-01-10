package io.nuls.crosschain.rpc.call;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.BlockHeader;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.crosschain.model.bo.Chain;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Calling other modules and transaction related interfaces
 *
 * @author: tag
 * @date: 2019/4/12
 */
public class ConsensusCall {
    /**
     * Query whether this node is a consensus node. If so, return the consensus account and password
     * Query whether the node is a consensus node, if so, return, consensus account and password
     * */
    @SuppressWarnings("unchecked")
    public static Map getPackerInfo(Chain chain) {
        try {
            Map<String, Object> params = new HashMap(4);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getPackerInfo", params);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("Packing state failed to send!");
            }
            return  (HashMap) ((HashMap) cmdResp.getResponseData()).get("cs_getPackerInfo");
        } catch (Exception e) {
            chain.getLogger().error(e);
            return null;
        }
    }

    /**
     * Query the list of all consensus nodes in the current network
     * Query whether the node is a consensus node, if so, return, consensus account and password
     * */
    @SuppressWarnings("unchecked")
    public static List<String> getWorkAgentList(Chain chain) {
        try {
            Map<String, Object> params = new HashMap(4);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getAgentAddressList", params);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("Packing state failed to send!");
            }
            return  (List<String>)((HashMap) ((HashMap) cmdResp.getResponseData()).get("cs_getAgentAddressList")).get("packAddress");
        } catch (Exception e) {
            chain.getLogger().error(e);
            return null;
        }
    }

    /**
     * Query whether this node is a consensus node. If so, return the consensus account and password, and query the current list of seed nodes
     * Query whether the node is a consensus node, if so, return, consensus account and password
     * */
    @SuppressWarnings("unchecked")
    public static Map getSeedNodeList(Chain chain) {
        try {
            Map<String, Object> params = new HashMap(4);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getSeedNodeInfo", params);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("Packing state failed to send!");
            }
            return  (HashMap) ((HashMap) cmdResp.getResponseData()).get("cs_getSeedNodeInfo");
        } catch (Exception e) {
            chain.getLogger().error(e);
            return null;
        }
    }

    /**
     * Query the list of all outgoing block addresses for the specified time round
     * Query the list of all out-of-block addresses for a specified time round
     * */
    @SuppressWarnings("unchecked")
    public static List<String> getRoundMemberList(Chain chain, BlockHeader blockHeader) {
        try {
            Map<String, Object> params = new HashMap(4);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            params.put("extend", RPCUtil.encode(blockHeader.getExtend()));
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getRoundMemberList", params);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("Packing state failed to send!");
            }
            return  (List<String>)((HashMap) ((HashMap) cmdResp.getResponseData()).get("cs_getRoundMemberList")).get("packAddressList");
        } catch (Exception e) {
            chain.getLogger().error(e);
            return null;
        }
    }

    /**
     * Query information on node changes between two rounds
     * Query for two rounds of inter-section change information
     * */
    @SuppressWarnings("unchecked")
    public static Map<String,List<String>> getAgentChangeInfo(Chain chain, byte[] lastRound, byte[] currentRound) {
        try {
            Map<String, Object> params = new HashMap(4);
            params.put(Constants.CHAIN_ID, chain.getChainId());
            if(lastRound == null){
                params.put("lastRound", null);
            }else{
                params.put("lastRound", RPCUtil.encode(lastRound));
            }
            params.put("currentRound", RPCUtil.encode(currentRound));
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.CS.abbr, "cs_getAgentChangeInfo", params);
            if (!cmdResp.isSuccess()) {
                chain.getLogger().error("Packing state failed to send!");
            }
            return (HashMap) ((HashMap) cmdResp.getResponseData()).get("cs_getAgentChangeInfo");
        } catch (Exception e) {
            chain.getLogger().error(e);
            return null;
        }
    }
}
