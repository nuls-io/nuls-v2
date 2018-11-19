package io.nuls.chain.cmd;


import io.nuls.chain.info.CmConstants;
import io.nuls.chain.model.dto.Seed;
import io.nuls.chain.model.txdata.Asset;
import io.nuls.chain.model.txdata.Chain;
import io.nuls.chain.service.AssetService;
import io.nuls.chain.service.ChainService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
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

    @CmdAnnotation(cmd = "chain", version = 1.0, preCompatible = true)
    public CmdResponse chain(List params) {
        try {
            short chainId = Short.valueOf(params.get(0).toString());
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

    @CmdAnnotation(cmd = "chainReg", version = 1.0, preCompatible = true)
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
            chain.setCreateTime(TimeService.currentTimeMillis());

            // TODO
            return success("sent newTx", chain);
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "chainRegValidator", version = 1.0, preCompatible = true)
    public CmdResponse chainRegValidator(List params) {
        try {
            Chain chain = JSONUtils.json2pojo(JSONUtils.obj2json(params.get(0)), Chain.class);
            if (chain.getChainId() < 0) {
                return failed("C10002");
            }
            if (chainService.getChain(chain.getChainId()) != null) {
                return failed("C10001");
            }

            return success();
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "chainRegCommit", version = 1.0, preCompatible = true)
    public CmdResponse chainRegCommit(List params) {
        try {
            Chain chain = JSONUtils.json2pojo(JSONUtils.obj2json(params.get(0)), Chain.class);

            chainService.saveChain(chain);

            return success("chainRegCommit", null);
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "chainRegRollback", version = 1.0, preCompatible = true)
    public CmdResponse chainRegRollback(List params) {
        try {
            return success("chainRegRollback", null);
        } catch (Exception e) {
            Log.error(e);
            return failed(ErrorCode.init("-100"), e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "setChainAssetCurrentNumber", version = 1.0, preCompatible = true)
    public CmdResponse setChainAssetCurrentNumber(List params) {
        short chainId = Short.valueOf(params.get(0).toString());
        long assetId = Long.valueOf(params.get(1).toString());
        long currentNumber = Long.valueOf(params.get(2).toString());
        chainService.setAssetNumber(chainId, assetId, currentNumber);
        return success("setChainAssetCurrentNumber", null);
    }

    @CmdAnnotation(cmd = "setChainAssetCurrentNumberValidator", version = 1.0, preCompatible = true)
    public CmdResponse setChainAssetCurrentNumberValidator(List params) {
        long assetId = Long.valueOf(params.get(1).toString());
        long currentNumber = Long.valueOf(params.get(2).toString());
        Asset asset = assetService.getAsset(assetId);
        if (currentNumber > asset.getInitNumber()) {
            return failed(CmConstants.ERROR_ASSET_EXCEED_INIT);
        }
        return success();
    }
}
