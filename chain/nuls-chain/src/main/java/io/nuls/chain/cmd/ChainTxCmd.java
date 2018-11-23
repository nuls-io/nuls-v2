package io.nuls.chain.cmd;


import io.nuls.base.data.Transaction;
import io.nuls.chain.model.dto.Chain;
import io.nuls.chain.model.tx.CrossChainDestroyTransaction;
import io.nuls.chain.model.tx.CrossChainRegTransaction;
import io.nuls.chain.model.tx.txdata.ChainTx;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.RpcService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.info.Constants;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.log.Log;

import java.util.Map;

/**
 * @author lan
 * @date 2018/11/21
 * @description
 */
@Component
public class ChainTxCmd extends BaseCmd {

    @Autowired
    private ChainService chainService;

    @Autowired
    private RpcService rpcService;


    @CmdAnnotation(cmd = "cm_chainRegValidator", version = 1.0,description = "chainRegValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainRegValidator(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            Chain  chain = buildChainTxData(txHex,new CrossChainRegTransaction());
            int chainId = chain.getChainId();
            if (chainId < 0) {
                return failed("C10002");
            }
            Chain dbChain = chainService.getChain(chain.getChainId());
            if (dbChain != null ) {
                return failed("C10001");
            }
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }
    @CmdAnnotation(cmd = "cm_chainRegCommit", version = 1.0,description = "chainRegCommit")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainRegCommit(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            Chain  chain = buildChainTxData(txHex,new CrossChainRegTransaction());
            Chain dbChain = chainService.getChain(chain.getChainId());
            if (dbChain != null ) {
                return failed("C10001");
            }
            //进行存储:
            chainService.saveChain(chain);
            //通知网络模块创建链
            rpcService.createCrossGroup(chain);
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "cm_chainRegRollback", version = 1.0,description = "chainRegRollback")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainRegRollback(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            Chain  chain = buildChainTxData(txHex,new CrossChainRegTransaction());
            Chain dbChain = chainService.getChain(chain.getChainId());
            if ( null == chain || null == dbChain || !chain.getTxHash().equalsIgnoreCase(dbChain.getTxHash())) {
                return failed("C10001");
            }
            chain.setDelete(true);
             chainService.updateChain(chain);
             rpcService.destroyCrossGroup(chain);
            return success(chain);
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }
    @CmdAnnotation(cmd = "cm_chainDestroyValidator", version = 1.0,description = "chainDestroyValidator")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainDestroyValidator(Map params) {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            Chain chain = buildChainTxData(txHex,new CrossChainDestroyTransaction());
            return destroyValidator(chain);
    }
    private Response destroyValidator(Chain chain){
        try {
            if(null == chain) {
                return failed("C10003");
            }
            Chain dbChain = chainService.getChain(chain.getChainId());
            if(null == dbChain || !dbChain.getTxHash().equalsIgnoreCase(chain.getTxHash())){
                return failed("C10003");
            }
            if(!ByteUtils.arrayEquals(dbChain.getAddress(),chain.getAddress())){
                return failed("C10004");
            }
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }
    @CmdAnnotation(cmd = "cm_chainDestroyCommit", version = 1.0,description = "chainDestroyCommit")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainDestroyCommit(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            Chain chain = buildChainTxData(txHex,new CrossChainDestroyTransaction());
            Response cmdResponse =  destroyValidator(chain);
            if(cmdResponse.getResponseStatus() != (Constants.RESPONSE_STATUS_SUCCESS)){
                return cmdResponse;
            }
            Chain dbChain = chainService.getChain(chain.getChainId());
            dbChain.setDelete(true);
            chainService.updateChain(dbChain);
            rpcService.destroyCrossGroup(dbChain);
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }
    @CmdAnnotation(cmd = "cm_chainDestroyRollback", version = 1.0,description = "chainDestroyRollback")
    @Parameter(parameterName = "chainId", parameterType = "int", parameterValidRange = "[1,65535]", parameterValidRegExp = "")
    @Parameter(parameterName = "txHex", parameterType = "String")
    @Parameter(parameterName = "secondaryData", parameterType = "String")
    public Response chainDestroyRollback(Map params) {
        try {
            String txHex = String.valueOf(params.get("txHex"));
            String secondaryData = String.valueOf(params.get("secondaryData"));
            Chain chain = buildChainTxData(txHex,new CrossChainDestroyTransaction());
            Response cmdResponse =  destroyValidator(chain);
            if(cmdResponse.getResponseStatus() != (Constants.RESPONSE_STATUS_SUCCESS)){
                return cmdResponse;
            }
            Chain dbChain = chainService.getChain(chain.getChainId());
            if(!dbChain.isDelete()){
                return failed("C10005");
            }
            dbChain.setDelete(false);
            chainService.updateChain(dbChain);
            rpcService.createCrossGroup(dbChain);
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }

    private Chain buildChainTxData(String txHex, Transaction tx){
        try {
            byte []txBytes = HexUtil.hexToByte(txHex);
            tx.parse(txBytes,0);
            ChainTx chainTx =  new ChainTx();
            chainTx.parse(tx.getTxData(),0);
            Chain chain = new Chain(chainTx);
            chain.setTxHash(tx.getHash().toString());
            return chain;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }
}
