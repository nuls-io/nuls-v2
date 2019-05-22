package io.nuls.poc.utils.manager;

import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.rpc.call.CallMethodUtils;
import io.nuls.poc.utils.compare.BlockHeaderComparator;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.Iterator;
import java.util.List;

/**
 * 链区块管理类
 * Chain Block Management Class
 *
 * @author tag
 * 2018/12/20
 */
@Component
public class BlockManager {
    @Autowired
    private RoundManager roundManager;

    /**
     * 收到最新区块头，更新链区块缓存数据
     * Receive the latest block header, update the chain block cache entity
     *
     * @param chain       chain info
     * @param blockHeader block header
     */
    public void addNewBlock(Chain chain, BlockHeader blockHeader) {
        /*
        如果新增区块有轮次变化，则删除最小轮次区块
         */
        BlockHeader newestHeader = chain.getNewestHeader();
        BlockExtendsData newestExtendsData = new BlockExtendsData(newestHeader.getExtend());
        BlockExtendsData receiveExtendsData = new BlockExtendsData(blockHeader.getExtend());
        long receiveRoundIndex = receiveExtendsData.getRoundIndex();
        if(chain.getBlockHeaderList().size() >0){
            BlockExtendsData lastExtendsData = new BlockExtendsData(chain.getBlockHeaderList().get(0).getExtend());
            long lastRoundIndex = lastExtendsData.getRoundIndex();
            if (receiveRoundIndex > newestExtendsData.getRoundIndex() && (receiveRoundIndex - ConsensusConstant.INIT_BLOCK_HEADER_COUNT > lastRoundIndex)) {
                Iterator<BlockHeader> iterator = chain.getBlockHeaderList().iterator();
                while (iterator.hasNext()) {
                    lastExtendsData = new BlockExtendsData(iterator.next().getExtend());
                    if (lastExtendsData.getRoundIndex() == lastRoundIndex) {
                        iterator.remove();
                    } else if (lastExtendsData.getRoundIndex() > lastRoundIndex) {
                        break;
                    }
                }
            }
        }
        chain.getBlockHeaderList().add(blockHeader);
        chain.setNewestHeader(blockHeader);
        chain.getLogger().info("区块保存，高度为：" + blockHeader.getHeight() + " , txCount: " + blockHeader.getTxCount() + ",本地最新区块高度为：" + chain.getNewestHeader().getHeight());
    }

    /**
     * 链分叉，区块回滚
     * Chain bifurcation, block rollback
     *
     * @param chain  chain info
     * @param height block height
     */
    public void chainRollBack(Chain chain, int height) {
        chain.getLogger().info("区块开始回滚，回滚到的高度：" + height);
        List<BlockHeader> headerList = chain.getBlockHeaderList();
        headerList.sort(new BlockHeaderComparator());
        BlockHeader originalBlocHeader = chain.getNewestHeader();
        BlockExtendsData originalExtendsData = new BlockExtendsData(originalBlocHeader.getExtend());
        long originalRound = originalExtendsData.getRoundIndex();
        for (int index = headerList.size() - 1; index >= 0; index--) {
            if (headerList.get(index).getHeight() >= height) {
                headerList.remove(index);
            } else {
                break;
            }
        }
        chain.setBlockHeaderList(headerList);
        chain.setNewestHeader(headerList.get(headerList.size() - 1));
        BlockHeader newestBlocHeader = chain.getNewestHeader();
        BlockExtendsData bestExtendsData = new BlockExtendsData(newestBlocHeader.getExtend());
        long currentRound = bestExtendsData.getRoundIndex();
        //如果有轮次变化，回滚之后如果本地区块不足指定轮次的区块，则需向区块获取区块补足并回滚本地
        if(currentRound != originalRound){
            BlockHeader lastestBlocHeader = chain.getBlockHeaderList().get(0);
            BlockExtendsData lastestExtendsData = new BlockExtendsData(lastestBlocHeader.getExtend());
            long minRound = lastestExtendsData.getRoundIndex();
            int localRoundCount = (int)(currentRound - minRound + 1);
            int diffRoundCount = ConsensusConstant.INIT_BLOCK_HEADER_COUNT - localRoundCount;
            if(diffRoundCount > 0){
                try {
                    CallMethodUtils.getRoundBlockHeaders(chain,diffRoundCount,lastestBlocHeader.getHeight());
                }catch (Exception e){
                    chain.getLogger().error(e);
                }
            }
            long roundIndex;
            //回滚轮次
            if(bestExtendsData.getPackingIndexOfRound() > 1){
                roundIndex = bestExtendsData.getRoundIndex();
            }else{
                roundIndex = bestExtendsData.getRoundIndex()-1;
            }
            roundManager.rollBackRound(chain, roundIndex);
        }
        /*long roundIndex;
        BlockHeader newestBlocHeader = chain.getNewestHeader();
        BlockExtendsData bestExtendsData = new BlockExtendsData(newestBlocHeader.getExtend());
        if(bestExtendsData.getPackingIndexOfRound() > 1){
            roundIndex = bestExtendsData.getRoundIndex();
        }else{
            roundIndex = bestExtendsData.getRoundIndex()-1;
        }
        roundManager.rollBackRound(chain, roundIndex);*/
        chain.getLogger().info("区块回滚成功，回滚到的高度为：" + height + ",本地最新区块高度为：" + chain.getNewestHeader().getHeight());
    }
}
