package io.nuls.poc.utils.manager;

import io.nuls.base.data.BlockHeader;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.utils.compare.BlockHeaderComparator;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.log.Log;

import java.util.*;

/**
 * 链区块管理类
 * Chain Block Management Class
 *
 * @author tag
 * 2018/12/20
 */
@Component
public class BlockManager {

    /**
     * 初始化链区块头数据，缓存指定数量的区块头
     * Initialize chain block header data to cache a specified number of block headers
     *
     * @param chain chain info
     */
    @SuppressWarnings("unchecked")
    public void loadBlockHeader(Chain chain) throws Exception {
        Map params = new HashMap(ConsensusConstant.INIT_CAPACITY);
        params.put("chainId", chain.getConfig().getChainId());
        params.put("size", ConsensusConstant.INIT_BLOCK_HEADER_COUNT);
        Response cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, "getLatestBlockHeaders", params);
        Map<String, Object> resultMap;
        List<String> blockHeaderHexs = new ArrayList<>();
        if(cmdResp.isSuccess()){
            resultMap = (Map<String, Object>) cmdResp.getResponseData();
            blockHeaderHexs = (List<String>) resultMap.get("getLatestBlockHeaders");
        }
        while(!cmdResp.isSuccess() && blockHeaderHexs.size() == 0){
            cmdResp = ResponseMessageProcessor.requestAndResponse(ModuleE.BL.abbr, "getLatestBlockHeaders", params);
            if(cmdResp.isSuccess()){
                resultMap = (Map<String, Object>) cmdResp.getResponseData();
                blockHeaderHexs = (List<String>) resultMap.get("getLatestBlockHeaders");
                break;
            }
            Log.info("---------------------------区块加载失败！");
            Thread.sleep(1000);
        }
        List<BlockHeader> blockHeaders = new ArrayList<>();
        for (String blockHeaderHex : blockHeaderHexs) {
            BlockHeader blockHeader = new BlockHeader();
            blockHeader.parse(HexUtil.decode(blockHeaderHex), 0);
            blockHeaders.add(blockHeader);
        }
        Collections.sort(blockHeaders, new BlockHeaderComparator());
        chain.setBlockHeaderList(blockHeaders);
        chain.setNewestHeader(blockHeaders.get(blockHeaders.size() - 1));
        Log.info("---------------------------区块加载成功！");
    }

    /**
     * 收到最新区块头，更新链区块缓存数据
     * Receive the latest block header, update the chain block cache data
     *
     * @param chain       chain info
     * @param blockHeader block header
     */
    public void addNewBlock(Chain chain, BlockHeader blockHeader) {
        chain.getBlockHeaderList().add(blockHeader);
        if (chain.getBlockHeaderList().size() > ConsensusConstant.INIT_BLOCK_HEADER_COUNT) {
            chain.getBlockHeaderList().remove(0);
        }
        chain.setNewestHeader(blockHeader);
        chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).info("新区块保存成功，新区块高度为：" + blockHeader.getHeight() + ",本地最新区块高度为：" + chain.getNewestHeader().getHeight());
    }

    /**
     * 链分叉，区块回滚
     * Chain bifurcation, block rollback
     *
     * @param chain  chain info
     * @param height block height
     */
    public void chainRollBack(Chain chain, int height) {
        List<BlockHeader> headerList = chain.getBlockHeaderList();
        Collections.sort(headerList, new BlockHeaderComparator());
        for (int index = headerList.size() - 1; index >= 0; index--) {
            if (headerList.get(index).getHeight() >= height) {
                headerList.remove(index);
            } else {
                break;
            }
        }
        chain.setBlockHeaderList(headerList);
        chain.setNewestHeader(headerList.get(headerList.size() - 1));
        chain.getLoggerMap().get(ConsensusConstant.BASIC_LOGGER_NAME).info("区块回滚成功，回滚到的高度为：" + height + ",本地最新区块高度为：" + chain.getNewestHeader().getHeight());
    }
}
