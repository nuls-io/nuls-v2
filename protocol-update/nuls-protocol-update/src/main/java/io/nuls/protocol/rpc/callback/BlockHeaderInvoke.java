package io.nuls.protocol.rpc.callback;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.model.ProtocolVersion;
import io.nuls.protocol.model.po.Statistics;
import io.nuls.protocol.service.StatisticsStorageService;
import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.logback.NulsLogger;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
public class BlockHeaderInvoke extends BaseInvoke {

    private int chainId;

    private StatisticsStorageService service;

    public BlockHeaderInvoke(int chainId) {
        this.chainId = chainId;
    }

    @Override
    public void callBack(Response response) {
        ProtocolContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getCommonLog();

        commonLog.debug("chainId-" + chainId + ", blockheader update");
        if (response.isSuccess()) {
            Map responseData = (Map) response.getResponseData();
            String hex = (String) responseData.get("latestBlockHeader");
            BlockHeader blockHeader = new BlockHeader();
            try {
                blockHeader.parse(new NulsByteBuffer(HexUtil.decode(hex)));
                byte[] extend = blockHeader.getExtend();
                BlockExtendsData data = new BlockExtendsData();
                data.parse(new NulsByteBuffer(extend));
                long height = blockHeader.getHeight();
                long latestHeight = context.getLatestHeight();
                //保存区块
                Statistics lastValidStatistics = context.getLastValidStatistics();
                if (height == latestHeight + 1) {
                    //缓存统计总数+1
                    int count = context.getCount();
                    count++;
                    context.setCount(count);
                    context.setLatestHeight(height);
                    boolean upgrade = data.isUpgrade();
                    //升级标志为真
                    if (upgrade) {
                        commonLog.debug("chainId-" + chainId + ", save block, protocol upgrade");
                        ProtocolVersion protocolVersion = new ProtocolVersion();
                        protocolVersion.setVersion(data.getBlockVersion());
                        protocolVersion.setInterval(data.getInterval());
                        protocolVersion.setEffectiveRatio(data.getEffectiveRatio());
                        protocolVersion.setContinuousIntervalCount(data.getContinuousIntervalCount());
                        //缓存起来
                        List<ProtocolVersion> versionList = context.getVersionList();
                        versionList.add(protocolVersion);
                        //重新计算统计信息
                        Map<ProtocolVersion, Integer> proportionMap = context.getProportionMap();
                        proportionMap.merge(protocolVersion, 1, (a, b) -> a + b);
                        int expect = count * protocolVersion.getEffectiveRatio();
                        int real = proportionMap.get(protocolVersion);
                        //占比超过阈值，保存一条统计记录到数据库
                        if (real >= expect) {
                            //初始化一条统计信息，与区块高度绑定，并存到数据库
                            Statistics statistics = new Statistics();
                            statistics.setHeight(height);
                            statistics.setProtocolVersion(protocolVersion);
                            if (lastValidStatistics.getProtocolVersion().equals(protocolVersion)) {
                                statistics.setCount((short) (lastValidStatistics.getCount() + 1));
                            } else {
                                statistics.setCount((short) 1);
                            }
                            service.save(chainId, statistics);
                            //如果某协议版本连续统计确认数大于阈值，则进行版本升级
                            if (statistics.getCount() >= protocolVersion.getContinuousIntervalCount()) {
                                //设置新协议版本
                                context.setProtocolVersion(protocolVersion);
                                commonLog.info("chainId-" + chainId + ", new protocol version available-" + protocolVersion);
                            }
                            context.setCount(0);
                            context.setLastValidStatistics(statistics);
                            //清除旧统计数据
                            versionList.clear();
                            proportionMap.clear();
                        }
                    }
                }
                //回滚区块
                if (height == latestHeight) {
                    //缓存统计总数-1
                    int count = context.getCount();
                    count--;
                    context.setCount(count);
                    boolean upgrade = data.isUpgrade();
                    //升级标志为真
                    if (upgrade) {
                        commonLog.debug("chainId-" + chainId + ", rollback block, protocol downgrade");
                        ProtocolVersion protocolVersion = new ProtocolVersion();
                        protocolVersion.setVersion(data.getBlockVersion());
                        protocolVersion.setInterval(data.getInterval());
                        protocolVersion.setEffectiveRatio(data.getEffectiveRatio());
                        protocolVersion.setContinuousIntervalCount(data.getContinuousIntervalCount());
                        //移除缓存
                        List<ProtocolVersion> versionList = context.getVersionList();
                        versionList.remove(protocolVersion);
                        //重新计算统计信息
                        Map<ProtocolVersion, Integer> proportionMap = context.getProportionMap();
                        proportionMap.merge(protocolVersion, 1, (a, b) -> a - b);
                        //区块高度到达阈值，从数据库删除一条统计记录
                        if (height == lastValidStatistics.getHeight()) {
                            service.delete(chainId, height);
                            Statistics newValidStatistics = service.get(chainId, lastValidStatistics.getLastHeight());
                            if (newValidStatistics.getCount() < protocolVersion.getContinuousIntervalCount()) {
                                //设置新协议版本
                                context.setProtocolVersion(protocolVersion);
                                commonLog.info("chainId-" + chainId + ", protocol version rollback-" + protocolVersion);
                            }
                            context.getLatestHeight();
                            context.setCount(0);
                            context.setLastValidStatistics(newValidStatistics);
                            //复原旧统计数据
                            versionList.clear();
                            proportionMap.clear();
                        }
                    }
                }
            } catch (NulsException e) {
                commonLog.error(e);
            }
        }

    }
}
