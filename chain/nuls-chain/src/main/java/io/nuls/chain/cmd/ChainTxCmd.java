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
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.log.Log;

import java.util.List;

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



    public CmdResponse chainRegValidator(List params) {
        try {
            String txHex = String.valueOf(params.get(1));
            String secondaryData = String.valueOf(params.get(2));
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


    public CmdResponse chainRegCommit(List params) {
        try {
            String txHex = String.valueOf(params.get(1));
            String secondaryData = String.valueOf(params.get(2));
            Chain  chain = buildChainTxData(txHex,new CrossChainRegTransaction());
            Chain dbChain = chainService.getChain(chain.getChainId());
            if (dbChain != null ) {
                return failed("C10001");
            }
            //进行存储:
            chainService.saveChain(chain);
            //通知网络模块创建链
            rpcService.createCrossGroup(chain);
            return success("chainRegCommit", null);
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }


    public CmdResponse chainRegRollback(List params) {
        try {
            String txHex = String.valueOf(params.get(1));
            String secondaryData = String.valueOf(params.get(2));
            Chain  chain = buildChainTxData(txHex,new CrossChainRegTransaction());
            Chain dbChain = chainService.getChain(chain.getChainId());
            if ( null == chain || null == dbChain || !chain.getTxHash().equalsIgnoreCase(dbChain.getTxHash())) {
                return failed("C10001");
            }
            chain.setDelete(true);
             chainService.updateChain(chain);
             rpcService.destroyCrossGroup(chain);
            return success("chainRegRollback", null);
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }

    public CmdResponse chainDestroyValidator(List params) {
        try {
            String txHex = String.valueOf(params.get(1));
            String secondaryData = String.valueOf(params.get(2));
            Chain chain = buildChainTxData(txHex,new CrossChainDestroyTransaction());
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

    public CmdResponse chainDestroyCommit(List params) {
        try {
            CmdResponse cmdResponse =   chainDestroyValidator(params);
            if(!cmdResponse.getCode().equalsIgnoreCase(Constants.SUCCESS_CODE)){
                return cmdResponse;
            }
            int chainId = Integer.valueOf(String.valueOf(params.get(0)));
            Chain dbChain = chainService.getChain(chainId);
            dbChain.setDelete(true);
            chainService.updateChain(dbChain);
            rpcService.destroyCrossGroup(dbChain);
            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }

    public CmdResponse chainDestroyRollback(List params) {
        try {
            CmdResponse cmdResponse =   chainDestroyValidator(params);
            if(!cmdResponse.getCode().equalsIgnoreCase(Constants.SUCCESS_CODE)){
                return cmdResponse;
            }
            int chainId = Integer.valueOf(String.valueOf(params.get(0)));
            Chain dbChain = chainService.getChain(chainId);
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

    private Chain buildChainTxData(String txHex, Transaction<ChainTx> tx){
        try {
            byte []txBytes = HexUtil.hexToByte(txHex);
            tx.parse(txBytes,0);
            Chain chain = new Chain(tx.getTxData());
            chain.setTxHash(tx.getHash().toString());
            return chain;
        } catch (Exception e) {
            Log.error(e);
            return null;
        }
    }
}
