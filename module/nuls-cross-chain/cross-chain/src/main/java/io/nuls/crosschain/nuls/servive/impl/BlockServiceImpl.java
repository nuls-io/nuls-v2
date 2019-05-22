package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Component;
import io.nuls.crosschain.base.constant.CommandConstant;
import io.nuls.crosschain.base.message.BroadCtxHashMessage;
import io.nuls.crosschain.nuls.constant.NulsCrossChainConfig;
import io.nuls.crosschain.nuls.model.bo.Chain;
import io.nuls.crosschain.nuls.model.po.SendCtxHashPo;
import io.nuls.crosschain.nuls.rpc.call.NetWorkCall;
import io.nuls.crosschain.nuls.servive.BlockService;
import io.nuls.crosschain.nuls.srorage.CommitedCtxService;
import io.nuls.crosschain.nuls.srorage.CompletedCtxService;
import io.nuls.crosschain.nuls.srorage.SendHeightService;
import io.nuls.crosschain.nuls.srorage.SendedHeightService;
import io.nuls.crosschain.nuls.utils.manager.ChainManager;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.exception.NulsException;

import java.util.*;

import static io.nuls.crosschain.nuls.constant.NulsCrossChainErrorCode.*;
import static io.nuls.crosschain.nuls.constant.ParamConstant.*;

/**
 * 提供给区块模块调用的接口实现类
 * @author tag
 * @date 2019/4/25
 */
@Component
public class BlockServiceImpl implements BlockService {
    @Autowired
    private ChainManager chainManager;

    @Autowired
    private SendHeightService sendHeightService;

    @Autowired
    private SendedHeightService sendedHeightService;

    @Autowired
    private CommitedCtxService commitedCtxService;

    @Autowired
    private CompletedCtxService completedCtxService;

    @Autowired
    private NulsCrossChainConfig config;

    @Override
    @SuppressWarnings("unchecked")
    public Result newBlockHeight(Map<String, Object> params) {
        if (params.get(CHAIN_ID) == null || params.get(NEW_BLOCK_HEIGHT) == null) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        int chainId = (int) params.get(CHAIN_ID);
        if (chainId <= 0) {
            return Result.getFailed(PARAMETER_ERROR);
        }
        Chain chain = chainManager.getChainMap().get(chainId);
        if (chain == null) {
            return Result.getFailed(CHAIN_NOT_EXIST);
        }
        long height = Long.valueOf(params.get(NEW_BLOCK_HEIGHT).toString());
        chain.getLogger().info("收到区块高度更新信息，最新区块高度为：{}", height);
        //查询是否有待广播的跨链交易
        Map<Long , SendCtxHashPo> sendHeightMap = sendHeightService.getList(chainId);
        if(sendHeightMap != null && sendHeightMap.size() >0){
            Set<Long> sortSet = new TreeSet<>(sendHeightMap.keySet());
            for (long cacheHeight:sortSet) {
                if(height >= cacheHeight){
                    chain.getLogger().info("广播区块高度为{}的跨链交易给其他链",cacheHeight );
                    SendCtxHashPo po = sendHeightMap.get(cacheHeight);
                    List<NulsHash> broadSuccessCtxHash = new ArrayList<>();
                    List<NulsHash> broadFailCtxHash = new ArrayList<>();
                    for (NulsHash ctxHash:po.getHashList()) {
                        BroadCtxHashMessage message = new BroadCtxHashMessage();
                        message.setRequestHash(ctxHash);
                        int toId = chainId;
                        Transaction ctx = commitedCtxService.get(ctxHash, chainId);
                        if(config.isMainNet()){
                            try {
                                toId = AddressTool.getChainIdByAddress(ctx.getCoinDataInstance().getTo().get(0).getAddress());
                            }catch (NulsException e){
                                chain.getLogger().error(e);
                            }
                        }
                        if(NetWorkCall.broadcast(toId, message, CommandConstant.BROAD_CTX_HASH_MESSAGE,true)){
                            if(!completedCtxService.save(ctxHash, ctx, chainId) || !commitedCtxService.delete(ctxHash, chainId)){
                                chain.getLogger().error("跨链交易从已提交表转存到已处理完成表失败,Hash:{}",ctxHash);
                                continue;
                            }
                            broadSuccessCtxHash.add(ctxHash);
                            chain.getLogger().info("跨链交易广播成功，Hash:{}",ctxHash );
                        }else{
                            broadFailCtxHash.add(ctxHash);
                            chain.getLogger().info("跨链交易广播失败，Hash:{}",ctxHash );
                        }
                    }
                    if(broadSuccessCtxHash.size() > 0){
                        SendCtxHashPo sendedPo = sendedHeightService.get(cacheHeight,chainId);
                        if(sendedPo != null){
                            sendedPo.getHashList().addAll(broadSuccessCtxHash);
                        }else{
                            sendedPo = new SendCtxHashPo(broadSuccessCtxHash);
                        }
                        if(!sendedHeightService.save(cacheHeight, sendedPo, chainId)){
                            continue;
                        }
                    }
                    if(broadFailCtxHash.size() > 0){
                        po.setHashList(broadFailCtxHash);
                        sendHeightService.save(cacheHeight, po, chainId);
                    }else{
                        sendHeightService.delete(cacheHeight, chainId);
                    }
                }else{
                    break;
                }
            }
        }
        chain.getLogger().info("区块高度更新消息处理完成,Height:{}\n\n",height);
        return Result.getSuccess(SUCCESS);
    }
}
