package io.nuls.poc.utils.manager;

import io.nuls.base.data.BlockHeader;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.utils.compare.BlockHeaderComparator;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.crypto.HexUtil;

import java.util.*;

/**
 * 链区块管理类
 * Chain Block Management Class
 *
 * @author tag
 * 2018/12/20
 * */
@Component
public class BlockManager {

    /**
     * 初始化链区块头数据，缓存指定数量的区块头
     * Initialize chain block header data to cache a specified number of block headers
     *
     * @param chain  chain info
     * */
    public void loadBlockHeader(Chain chain){
        try {
            Map params = new HashMap(ConsensusConstant.INIT_CAPACITY);
            params.put("chainId",chain.getConfig().getChainId());
            params.put("size", ConsensusConstant.INIT_BLOCK_HEADER_COUNT);
            Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.BL.abbr,"getLatestBlockHeaders", params);
            if(!cmdResp.isSuccess()){
                chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error("init blockHeader failed!");
                return;
            }
            List<String> blockHeaderHexs = (List<String>)cmdResp.getResponseData();
            List<BlockHeader>blockHeaders = new ArrayList<>();
            for (String blockHeaderHex:blockHeaderHexs) {
                BlockHeader blockHeader = new BlockHeader();
                blockHeader.parse(HexUtil.decode(blockHeaderHex),0);
                blockHeaders.add(blockHeader);
            }
            Collections.sort(blockHeaders,new BlockHeaderComparator());
            chain.setBlockHeaderList(blockHeaders);
            chain.setNewestHeader(blockHeaders.get(blockHeaders.size()-1));
        }catch (Exception e){
            chain.getLoggerMap().get(ConsensusConstant.CONSENSUS_LOGGER_NAME).error(e);
        }
    }

    /**
     * 收到最新区块头，更新链区块缓存数据
     * Receive the latest block header, update the chain block cache data
     *
     * @param chain            chain info
     * @param blockHeader      block header
     * */
    public void addNewBlock(Chain chain,BlockHeader blockHeader){
        chain.getBlockHeaderList().add(blockHeader);
        if(chain.getBlockHeaderList().size() > ConsensusConstant.INIT_BLOCK_HEADER_COUNT){
            chain.getBlockHeaderList().remove(0);
        }
        chain.setNewestHeader(blockHeader);
    }
}
