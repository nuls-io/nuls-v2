package io.nuls.base.protocol;

import io.nuls.core.log.Log;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.util.*;

/**
 * 帮助模块实现自动注册交易、注册消息的工具类
 *
 * @author captain
 * @version 1.0
 * @date 2019/5/28 11:44
 */
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
                detail.setVerifyFee(config.getVerifyFee());
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
    public static boolean registerMsg(Protocol protocol, String role) {
        try {
            Map<String, Object> map = new HashMap<>(2);
            List<String> cmds = new ArrayList<>();
            map.put("role", role);
            protocol.getAllowMsg().forEach(e -> cmds.addAll(Arrays.asList(e.getProtocolCmd().split(","))));
            map.put("protocolCmds", cmds);
            return ResponseMessageProcessor.requestAndResponse(ModuleE.NW.abbr, "nw_protocolRegister", map).isSuccess();
        } catch (Exception e) {
            Log.error("registerMsg fail", e);
            return false;
        }
    }

    /**
     * 向网络模块注册消息
     *
     * @return
     */
    public static boolean registerMsg(Protocol protocol) {
        return registerMsg(protocol, ConnectManager.LOCAL.getAbbreviation());
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
