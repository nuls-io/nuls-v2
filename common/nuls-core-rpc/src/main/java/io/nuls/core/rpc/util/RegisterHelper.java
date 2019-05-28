package io.nuls.core.rpc.util;

import io.nuls.core.log.Log;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.core.rpc.protocol.Protocol;
import io.nuls.core.rpc.protocol.ProtocolGroupManager;
import io.nuls.core.rpc.protocol.TxDefine;
import io.nuls.core.rpc.protocol.TxRegisterDetail;

import java.util.*;

public class RegisterHelper {

    /**
     * 向交易模块注册交易
     * Register transactions with the transaction module
     */
    public static boolean registerTx(int chainId, Protocol protocol, String moduleCode) {
        try {
            List<TxRegisterDetail> txRegisterDetailList = new ArrayList<>();
            List<TxDefine> allowTxs = protocol.getAllowTx();
            for (TxDefine config : allowTxs) {
                TxRegisterDetail detail = new TxRegisterDetail();
                detail.setSystemTx(config.isSystemTx());
                detail.setTxType(config.getType());
                detail.setUnlockTx(config.isUnlockTx());
                detail.setVerifySignature(config.isVerifySignature());
                txRegisterDetailList.add(detail);
            }
            //向交易管理模块注册交易
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            params.put("moduleCode", moduleCode);
            params.put("list", txRegisterDetailList);
            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.TX.abbr, "tx_register", params);
            if (!cmdResp.isSuccess()) {
                Log.error("chain ：" + chainId + " Failure of transaction registration,errorMsg: " + cmdResp.getResponseComment());
                return false;
            }
        } catch (Exception e) {
            Log.error("", e);
        }
        return true;
    }

    /**
     * 向交易模块注册交易
     * Register transactions with the transaction module
     */
    public static boolean registerTx(int chainId, Protocol protocol) {
        return registerTx(chainId, protocol, ConnectManager.LOCAL.getAbbreviation());
    }

    /**
     * 向网络模块注册消息
     *
     * @return
     */
    public static void registerMsg(Protocol protocol, String role) {
        try {
            Map<String, Object> map = new HashMap<>(2);
            List<String> cmds = new ArrayList<>();
            map.put("role", role);
            protocol.getAllowMsg().forEach(e -> cmds.addAll(Arrays.asList(e.getProtocolCmd().split(","))));
            map.put("protocolCmds", cmds);
            ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_protocolRegister", map);
        } catch (Exception e) {
            Log.error("registerMsg fail", e);
        }
    }

    /**
     * 向网络模块注册消息
     *
     * @return
     */
    public static void registerMsg(Protocol protocol) {
        registerMsg(protocol, ConnectManager.LOCAL.getAbbreviation());
    }

    /**
     * 向协议升级模块注册多版本协议配置
     * Register transactions with the transaction module
     */
    public static boolean registerProtocol(int chainId) {
        if (!ModuleHelper.isSupportProtocolUpdate()) {
            return true;
        }
        try {
            Collection<Protocol> protocols = ProtocolGroupManager.getProtocols(chainId);
            Map<String, Object> params = new HashMap<>();
            params.put(Constants.VERSION_KEY_STR, "1.0");
            params.put(Constants.CHAIN_ID, chainId);
            List<Protocol> list = new ArrayList<>(protocols);
            params.put("list", list);
            params.put("moduleCode", ConnectManager.LOCAL.getAbbreviation());

            Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.PU.abbr, "registerProtocol", params);
            if (!cmdResp.isSuccess()) {
                Log.error("chain ：" + chainId + " Failure of transaction registration,errorMsg: " + cmdResp.getResponseComment());
                return false;
            }
        } catch (Exception e) {
            Log.error("", e);
        }
        return true;
    }

}
