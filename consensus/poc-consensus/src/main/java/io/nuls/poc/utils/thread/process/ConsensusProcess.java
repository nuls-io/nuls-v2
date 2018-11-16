package io.nuls.poc.utils.thread.process;

import io.nuls.base.data.Block;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.poc.constant.ConsensusConstant;
import io.nuls.poc.model.bo.consensus.ConsensusStatus;
import io.nuls.poc.model.bo.round.MeetingMember;
import io.nuls.poc.model.bo.round.MeetingRound;
import io.nuls.poc.utils.manager.ConfigManager;
import io.nuls.poc.utils.manager.ConsensusManager;
import io.nuls.poc.utils.manager.RoundManager;
import io.nuls.tools.data.DateUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.thread.TimeService;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;

/**
 * 共识处理器
 * @author tag
 * 2018/11/15
 * */
public class ConsensusProcess {
    //是否正在打包
    private boolean hasPacking;
    public void process(int chain_id) {
        //检查该共识节点状态
        boolean canPackage = checkCanPackage(chain_id);
        if (!canPackage) {
            return;
        }
        doWork(chain_id);
    }

    /**
     * 检查节点打包状态
     * */
    private boolean checkCanPackage(int chain_id) {
        //检查模块状态是否为运行中
        if (ConsensusManager.getInstance().getAgent_status().get(chain_id).ordinal() <= ConsensusStatus.WAIT_RUNNING.ordinal()) {
            return false;
        }
        //todo
        //检查网络状态是否正常（调用网络模块接口获取当前链接节点数）
        int availableNodes = 5;
        if(availableNodes < ConsensusConstant.ALIVE_MIN_NODE_COUNT){
            return false;
        }
        //检查节点状态是否可打包
        if(!ConsensusManager.getInstance().getPacking_status().get(chain_id)){
            return false;
        }
        return true;
    }

    private void doWork(int chain_id){
        //检查节点状态
        if (ConsensusManager.getInstance().getAgent_status().get(chain_id).ordinal() < ConsensusStatus.RUNNING.ordinal()) {
            return;
        }
        //获取当前轮次信息
        MeetingRound round = RoundManager.getInstance().getOrResetCurrentRound(chain_id,true);
        if (round == null) {
            return;
        }
        //检查本地节点是否为共识节点
        MeetingMember member = round.getMyMember();
        if(member == null){
            return;
        }
        //如果是共识节点则判断是否轮到自己出块  1.节点是否正在打包 2.当前时间是否处于节点打包开始时间和结束时间之间
        if(!hasPacking && member.getPackStartTime() < TimeService.currentTimeMillis() && member.getPackEndTime() > TimeService.currentTimeMillis()){
            hasPacking = true;
            try {
                if (Log.isDebugEnabled()) {
                    Log.debug("当前网络时间： " + DateUtils.convertDate(new Date(TimeService.currentTimeMillis())) + " , 我的打包开始时间: " +
                            DateUtils.convertDate(new Date(member.getPackStartTime())) + " , 我的打包结束时间: " +
                            DateUtils.convertDate(new Date(member.getPackEndTime())) + " , 当前轮开始时间: " +
                            DateUtils.convertDate(new Date(round.getStartTime())) + " , 当前轮结束开始时间: " +
                            DateUtils.convertDate(new Date(round.getEndTime())));
                }
                packing(chain_id, member, round);
            } catch (Exception e) {
                Log.error(e);
            }
            while (member.getPackEndTime() > TimeService.currentTimeMillis()) {
                try {
                    Thread.sleep(500L);
                } catch (InterruptedException e) {
                    Log.error(e);
                }
            }
            hasPacking = false;
        }
    }

    /**
     * 打包
     * */
    private void packing(int chain_id,MeetingMember self, MeetingRound round) throws IOException, NulsException {
        waitReceiveNewestBlock(chain_id,self, round);
        //开始打包
        long start = System.currentTimeMillis();
        Block block = doPacking(chain_id, self, round);
        Log.info("doPacking use:" + (System.currentTimeMillis() - start) + "ms");
        //打包完成之后，查看打包区块和主链最新区块是否连续，如果不连续表示打包过程中收到了上一个共识节点打包的区块，此时本地节点需要重新打包区块
        //todo
        //从区块管理模块获取最新区块
        BlockHeader header = new BlockHeader();
        boolean rePacking = !block.getHeader().getPreHash().equals(header.getHash());
        if(rePacking){
            start = System.currentTimeMillis();
            block=doPacking(chain_id, self, round);
            Log.info("doPacking use:" + (System.currentTimeMillis() - start) + "ms");
        }
        if (null == block) {
            Log.error("make a null block");
            return;
        }
        //todo
        //打包成功后降区块传给区块管理模块广播

    }

    /**
     * 等待接收最新区块
     * */
    private boolean waitReceiveNewestBlock(int chain_id,MeetingMember self, MeetingRound round){
        long timeout = ConfigManager.config_map.get(chain_id).getPacking_interval()/2;
        long endTime = self.getPackStartTime() + timeout;
        boolean hasReceiveNewestBlock = false;
        try {
            while (!hasReceiveNewestBlock) {
                hasReceiveNewestBlock = hasReceiveNewestBlock(chain_id, self, round);
                if (hasReceiveNewestBlock) {
                    break;
                }
                Thread.sleep(100L);
                if (TimeService.currentTimeMillis() >= endTime) {
                    break;
                }
            }
        } catch (InterruptedException e) {
            Log.error(e);
        }
        return !hasReceiveNewestBlock;
    }

    /**
     * 判断上一个节点是否出块
     * */
    private boolean hasReceiveNewestBlock(int chain_id,MeetingMember self, MeetingRound round){
        //判断本地最新区块是否为轮次信息中指定的本节点的上一个共识节点所出
        int myIndex = self.getPackingIndexOfRound();
        MeetingMember preMember;
        MeetingRound  preRound = round;
        //如果当前节点为该轮次第一个出块节点，则本地最新区块应该是上一轮的最后一个出块节点所出
        if(myIndex == 1){
            preRound = round.getPreRound();
            if(preRound == null){
                Log.error("PreRound is null!");
                return true;
            }
            preMember = preRound.getMember(preRound.getMemberCount());
        }else{
            preMember = round.getMember(self.getPackingIndexOfRound()-1);
        }
        if (preMember == null) {
            return true;
        }
        //todo
        //获取本地最新区块（从区块管理模块获取）
        BlockHeader bestBlockHeader = new BlockHeader();
        BlockExtendsData blockRoundData = new BlockExtendsData(bestBlockHeader.getExtend());
        byte[] bestPackingAddress = bestBlockHeader.getPackingAddress();
        long bestRoundIndex = blockRoundData.getRoundIndex();
        int bestPackingIndex = blockRoundData.getPackingIndexOfRound();

        byte[] prePackingAddress = preMember.getAgent().getPackingAddress();
        long preRoundIndex = preRound.getIndex();
        int prePackingIndex = preMember.getPackingIndexOfRound();
        if (Arrays.equals(bestPackingAddress, prePackingAddress) && bestRoundIndex == preRoundIndex && bestPackingIndex == prePackingIndex) {
            return true;
        } else {
            return false;
        }
    }

    private Block doPacking(int chain_id, MeetingMember self, MeetingRound round) throws NulsException, IOException{
        //todo
        //从区块管理模块获取最新区块
        Block bestBlock = new Block();

        return null;
    }
}
