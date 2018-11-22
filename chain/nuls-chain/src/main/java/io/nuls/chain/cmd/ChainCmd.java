package io.nuls.chain.cmd;


import io.nuls.base.basic.AddressTool;
import io.nuls.chain.model.dto.Chain;
import io.nuls.chain.model.dto.Seed;
import io.nuls.chain.model.tx.CrossChainDestroyTransaction;
import io.nuls.chain.model.tx.CrossChainRegTransaction;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.chain.service.RpcService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.ByteUtils;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * @author tangyi
 * @date 2018/11/7
 * @description
 */
@Component
public class ChainCmd extends BaseCmd {

    @Autowired
    private ChainService chainService;

    @Autowired
    private AssetService assetService;

    @Autowired
    private RpcService rpcService;

    public CmdResponse chain(List params) {
        try {
            int chainId = Integer.valueOf(params.get(0).toString());
            Chain chain = chainService.getChain(chainId);
            if (chain == null) {
                return failed("C10003");
            }
            return success("chain", chain);
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }


    public CmdResponse chainReg(List params) {
        try {
            Chain chain = new Chain();
            chain.setChainId(Short.valueOf(params.get(0).toString()));
            chain.setName((String) params.get(1));
            chain.setAddressType((String) params.get(2));
            chain.setMagicNumber(Integer.valueOf(params.get(3).toString()));
            chain.setSupportInflowAsset((Boolean) params.get(4));
            chain.setMinAvailableNodeNum(Integer.valueOf(params.get(5).toString()));
            chain.setSingleNodeMinConnectionNum(Integer.valueOf(params.get(6).toString()));
            chain.setTxConfirmedBlockNum(Integer.valueOf(params.get(7).toString()));
            List<Seed> seedList = new ArrayList<>();
            StringTokenizer seedStr = new StringTokenizer(params.get(8).toString(), ",");
            while (seedStr.hasMoreTokens()) {
                StringTokenizer ipPort = new StringTokenizer(seedStr.nextToken(), ":");
                Seed seed = new Seed();
                seed.setIp(ipPort.nextToken());
                seed.setPort(Integer.parseInt(ipPort.nextToken()));
                seedList.add(seed);
            }
            chain.setSeedList(seedList);
            chain.setAddress(AddressTool.getAddress(String.valueOf(params.get(9))));

            chain.setCreateTime(TimeService.currentTimeMillis());
            Chain dbChain = chainService.getChain(chain.getChainId());
            if (dbChain != null) {
                return failed("C10001");
            }

            // 组装交易发送
            CrossChainRegTransaction crossChainRegTransaction = new CrossChainRegTransaction();
            crossChainRegTransaction.setTxData(chain.parseToTransaction());
            //TODO:coindata 未封装
            boolean rpcReslt = rpcService.newTx(crossChainRegTransaction);
            if(rpcReslt) {
                return success("sent reg chain newTx", chain);
            }else{
                return failed(new ErrorCode(),chain);
            }
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }



    /**
     * 删除链
     * @param params
     * @return
     */

    public CmdResponse chainDestroy(List params) {
        int chainId = Integer.valueOf(params.get(0).toString());
       byte [] address = (AddressTool.getAddress(String.valueOf(params.get(1))));
        //身份的校验，地址账户校验
        Chain chain = chainService.getChain(chainId);
        if (chain == null) {
            return failed("C10003");
        }
        if(!ByteUtils.arrayEquals(chain.getAddress(),address)){
            return failed("C10004");
        }
        CrossChainDestroyTransaction crossChainDestroyTransaction = new CrossChainDestroyTransaction();
        crossChainDestroyTransaction.setTxData(chain.parseToTransaction());
        //TODO:coindata 未封装
        boolean rpcReslt = rpcService.newTx(crossChainDestroyTransaction);
        if(rpcReslt) {
            return success("sent destroy chain newTx", chain);
        }else{
            return failed(new ErrorCode(),chain);
        }
    }



//    public CmdResponse setChainAssetCurrentNumber(List params) {
//        short chainId = Short.valueOf(params.get(0).toString());
//        long assetId = Long.valueOf(params.get(1).toString());
//        long currentNumber = Long.valueOf(params.get(2).toString());
//        chainService.setAssetNumber(chainId, assetId, currentNumber);
//        return success("setChainAssetCurrentNumber", null);
//    }
//
//    public CmdResponse setChainAssetCurrentNumberValidator(List params) {
//        long assetId = Long.valueOf(params.get(1).toString());
//        long currentNumber = Long.valueOf(params.get(2).toString());
//        Asset asset = assetService.getAsset(assetId);
//        if (currentNumber > asset.getInitNumber()) {
//            return failed(CmConstants.ERROR_ASSET_EXCEED_INIT);
//        }
//        return success();
//    }

}
