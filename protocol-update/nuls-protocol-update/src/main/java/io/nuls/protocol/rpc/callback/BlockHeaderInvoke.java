package io.nuls.protocol.rpc.callback;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolConfig;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.model.ProtocolVersion;
import io.nuls.protocol.model.po.Statistics;
import io.nuls.protocol.service.StatisticsStorageService;
import io.nuls.protocol.utils.module.BlockUtil;
import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.logback.NulsLogger;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Data
@Component
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class BlockHeaderInvoke extends BaseInvoke {

    private int chainId;

    private StatisticsStorageService service = SpringLiteContext.getBean(StatisticsStorageService.class);

    public BlockHeaderInvoke(int chainId) {
        this.chainId = chainId;
    }

    @Override
    public void callBack(Response response) {
        ProtocolContext context = ContextManager.getContext(chainId);
        NulsLogger commonLog = context.getCommonLog();
        if (response.isSuccess()) {
            String hex = (String) response.getResponseData();
            BlockHeader blockHeader = new BlockHeader();
            try {
                blockHeader.parse(new NulsByteBuffer(HexUtil.decode(hex)));
                byte[] extend = blockHeader.getExtend();
                BlockExtendsData data = new BlockExtendsData();
                data.parse(new NulsByteBuffer(extend));
                long height = blockHeader.getHeight();
                if (!validate(data, context)) {
                    commonLog.debug("chainId-" + chainId + ", invalid blockheader-" + height);
                }
                long latestHeight = context.getLatestHeight();
                //保存区块
                Statistics lastValidStatistics = context.getLastValidStatistics();
                if (height == latestHeight + 1) {
                    commonLog.debug("chainId-" + chainId + ", save block");
                    //缓存统计总数+1
                    int count = context.getCount();
                    count++;
                    context.setCount(count);
                    context.setLatestHeight(height);
                    boolean upgrade = data.isUpgrade();
                    List<ProtocolVersion> versionList = context.getVersionList();
                    Map<ProtocolVersion, Integer> proportionMap = context.getProportionMap();
                    ProtocolVersion currentProtocolVersion = context.getCurrentProtocolVersion();
                    //升级标志为真
                    if (upgrade) {
                        commonLog.debug("chainId-" + chainId + ", save block, protocol upgrade");
                        ProtocolVersion protocolVersion = new ProtocolVersion();
                        protocolVersion.setVersion(data.getBlockVersion());
                        protocolVersion.setInterval(data.getInterval());
                        protocolVersion.setEffectiveRatio(data.getEffectiveRatio());
                        protocolVersion.setContinuousIntervalCount(data.getContinuousIntervalCount());
                        //缓存起来
                        versionList.add(protocolVersion);
                        //重新计算统计信息
                        proportionMap.merge(protocolVersion, 1, (a, b) -> a + b);
                        int expect = protocolVersion.getInterval() * protocolVersion.getEffectiveRatio() / 100;
                        int real = proportionMap.get(protocolVersion);
                        //占比超过阈值，保存一条统计记录到数据库
                        if (real >= expect || count >= currentProtocolVersion.getInterval()) {
                            //初始化一条统计信息，与区块高度绑定，并存到数据库
                            Statistics statistics = new Statistics();
                            statistics.setHeight(height);
                            if (lastValidStatistics != null) {
                                statistics.setLastHeight(lastValidStatistics.getHeight());
                            }
                            //说明不是新版本的统计信息
                            if (lastValidStatistics != null && real < expect) {
                                statistics.setCount((short) (lastValidStatistics.getCount() + 1));
                                statistics.setProtocolVersion(currentProtocolVersion);
                            } else {
                                statistics.setProtocolVersion(protocolVersion);
                                statistics.setCount((short) 1);
                            }
                            boolean b = service.save(chainId, statistics);
                            commonLog.info("chainId-" + chainId + ", height-" + height + ", save-" + b + ", new statistics-" + statistics);
                            //如果某协议版本连续统计确认数大于阈值，则进行版本升级
                            if (statistics.getProtocolVersion().equals(protocolVersion) && statistics.getCount() >= protocolVersion.getContinuousIntervalCount()) {
                                //设置新协议版本
                                context.setCurrentProtocolVersion(protocolVersion);
                                commonLog.info("chainId-" + chainId + ", height-"+ height + ", new protocol version available-" + protocolVersion);
                            }
                            context.setCount(0);
                            context.setLastValidStatistics(statistics);
                            //清除旧统计数据
                            versionList.clear();
                            proportionMap.clear();
                        }
                    } else {
                        short interval = currentProtocolVersion.getInterval();
                        if (count >= interval) {
                            //初始化一条统计信息，与区块高度绑定，并存到数据库
                            Statistics statistics = new Statistics();
                            statistics.setHeight(height);
                            statistics.setProtocolVersion(currentProtocolVersion);
                            if (lastValidStatistics != null) {
                                statistics.setCount((short) (lastValidStatistics.getCount() + 1));
                                statistics.setLastHeight(lastValidStatistics.getHeight());
                            } else {
                                statistics.setCount((short) 1);
                            }
                            boolean b = service.save(chainId, statistics);
                            commonLog.info("chainId-" + chainId + ", height-" + height + ", save-" + b + ", new statistics-" + statistics);
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
                    commonLog.debug("chainId-" + chainId + ", rollback block");
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
                                context.setCurrentProtocolVersion(protocolVersion);
                                commonLog.info("chainId-" + chainId + ", protocol version rollback-" + protocolVersion);
                            }
                            List<ProtocolVersion> protocolVersions = BlockUtil.getBlockHeaders(chainId, latestHeight, latestHeight);
                            versionList.addAll(Objects.requireNonNull(protocolVersions));
                            context.setCount((int) (latestHeight - newValidStatistics.getHeight()));
                            context.setLastValidStatistics(newValidStatistics);
                            //复原旧统计数据
                            proportionMap.clear();
                        }
                    }
                }
            } catch (NulsException e) {
                commonLog.error(e);
            }
        }
    }

    /**
     * 验证区块头协议信息正确性
     *
     * @param data
     * @param context
     * @return
     */
    private boolean validate(BlockExtendsData data, ProtocolContext context){
        boolean upgrade = data.isUpgrade();
        if (upgrade) {
            short blockVersion = data.getBlockVersion();
            ProtocolVersion currentProtocolVersion = context.getCurrentProtocolVersion();
            if (currentProtocolVersion.getVersion() >= blockVersion) {
                return false;
            }
            short interval = data.getInterval();
            ProtocolConfig config = context.getConfig();
            if (interval > config.getIntervalMaximum()) {
                return false;
            }
            if (interval < config.getIntervalMinimum()) {
                return false;
            }
            byte effectiveRatio = data.getEffectiveRatio();
            if (effectiveRatio > config.getEffectiveRatioMaximum()) {
                return false;
            }
            if (effectiveRatio < config.getEffectiveRatioMinimum()) {
                return false;
            }
            short continuousIntervalCount = data.getContinuousIntervalCount();
            if (continuousIntervalCount > config.getContinuousIntervalCountMaximum()) {
                return false;
            }
            if (continuousIntervalCount < config.getContinuousIntervalCountMinimum()) {
                return false;
            }
        }
        return true;
    }
}
