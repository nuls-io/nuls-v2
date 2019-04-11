package io.nuls.protocol.rpc.callback;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ChainParameters;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.model.ProtocolVersion;
import io.nuls.protocol.model.po.StatisticsInfo;
import io.nuls.protocol.storage.StatisticsStorageService;
import io.nuls.rpc.invoke.BaseInvoke;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.logback.NulsLogger;

import java.util.Map;
import java.util.Stack;

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
                long latestHeight = context.getLatestHeight();
                StatisticsInfo lastValidStatisticsInfo = context.getLastValidStatisticsInfo();
                if (height == latestHeight + 1) {
                    //保存区块
                    save(context, commonLog, data, height, lastValidStatisticsInfo);
                }
                if (height == latestHeight) {
                    //回滚区块
                    rollback(context, commonLog, data, height, lastValidStatisticsInfo);
                }
            } catch (NulsException e) {
                commonLog.error(e);
            }
        }
    }

    private void rollback(ProtocolContext context, NulsLogger commonLog, BlockExtendsData data, long height, StatisticsInfo lastValidStatisticsInfo) {
        //缓存统计总数-1
        int count = context.getCount();
        count--;
        Map<ProtocolVersion, Integer> proportionMap = context.getProportionMap();
        if (!validate(data, context)) {
            commonLog.error("chainId-" + chainId + ", invalid blockheader-" + height);
        } else {
            ProtocolVersion newProtocolVersion = new ProtocolVersion();
            newProtocolVersion.setVersion(data.getBlockVersion());
            newProtocolVersion.setEffectiveRatio(data.getEffectiveRatio());
            newProtocolVersion.setContinuousIntervalCount(data.getContinuousIntervalCount());
            commonLog.debug("chainId-" + chainId + ", rollback block, height-" + height + ", protocol-" + newProtocolVersion);
            //重新计算统计信息
            proportionMap.merge(newProtocolVersion, 1, (a, b) -> a - b);
        }
        //缓存统计总数==0时，从数据库加载上一条统计记录
        ChainParameters parameters = context.getParameters();
        short interval = parameters.getInterval();
        //区块高度到达阈值，从数据库删除一条统计记录
        if (count < 0) {
            boolean b = service.delete(chainId, height);
            commonLog.info("chainId-" + chainId + ", height-" + height + ", delete-" + b);
            count = interval - 1;
            StatisticsInfo newValidStatisticsInfo = service.get(chainId, lastValidStatisticsInfo.getLastHeight());
            context.setLastValidStatisticsInfo(newValidStatisticsInfo);
            context.setProportionMap(newValidStatisticsInfo.getProtocolVersionMap());
            ProtocolVersion currentProtocolVersion = context.getCurrentProtocolVersion();
            if (newValidStatisticsInfo.getProtocolVersion().equals(currentProtocolVersion) && newValidStatisticsInfo.getCount() < currentProtocolVersion.getContinuousIntervalCount()) {
                //设置新协议版本
                Stack<ProtocolVersion> history = context.getProtocolVersionHistory();
                if (history.size() > 1) {
                    ProtocolVersion pop = history.pop();
                    ProtocolVersion protocolVersion = history.peek();
                    context.setCurrentProtocolVersion(protocolVersion);
                    commonLog.info("chainId-" + chainId + ", height-" + height + ", protocol version rollback-" + pop + ", new protocol version available-" + protocolVersion);
                }
            }
        }
        context.setCount(count);
        context.setLatestHeight(height - 1);
    }

    private void save(ProtocolContext context, NulsLogger commonLog, BlockExtendsData data, long height, StatisticsInfo lastValidStatisticsInfo) {
        //缓存统计总数+1
        int count = context.getCount();
        count++;
        context.setCount(count);
        context.setLatestHeight(height);

        Map<ProtocolVersion, Integer> proportionMap = context.getProportionMap();
        ProtocolVersion currentProtocolVersion = context.getCurrentProtocolVersion();
        if (!validate(data, context)) {
            commonLog.error("chainId-" + chainId + ", invalid blockheader-" + height);
        } else {
            ProtocolVersion newProtocolVersion = new ProtocolVersion();
            newProtocolVersion.setVersion(data.getBlockVersion());
            newProtocolVersion.setEffectiveRatio(data.getEffectiveRatio());
            newProtocolVersion.setContinuousIntervalCount(data.getContinuousIntervalCount());
            commonLog.debug("chainId-" + chainId + ", save block, height-" + height + ", protocol-" + newProtocolVersion);
            //重新计算统计信息
            proportionMap.merge(newProtocolVersion, 1, Integer::sum);
        }
        ChainParameters parameters = context.getParameters();
        short interval = parameters.getInterval();
        //每1000块进行一次统计
        if (count == interval) {
            int already = 0;
            for (Map.Entry<ProtocolVersion, Integer> entry : proportionMap.entrySet()) {
                ProtocolVersion version = entry.getKey();
                int real = entry.getValue();
                already += real;
                int expect = interval * version.getEffectiveRatio() / 100;
                //占比超过阈值，保存一条新协议统计记录到数据库
                if (!version.equals(currentProtocolVersion) && real >= expect) {
                    //初始化一条新协议统计信息，与区块高度绑定，并存到数据库
                    StatisticsInfo statisticsInfo = new StatisticsInfo();
                    statisticsInfo.setHeight(height);
                    statisticsInfo.setLastHeight(lastValidStatisticsInfo.getHeight());
                    statisticsInfo.setProtocolVersion(version);
                    statisticsInfo.setProtocolVersionMap(proportionMap);
                    //计数统计
                    if (lastValidStatisticsInfo.getProtocolVersion().equals(version)) {
                        statisticsInfo.setCount((short) (lastValidStatisticsInfo.getCount() + 1));
                    } else {
                        statisticsInfo.setCount((short) 1);
                    }
                    boolean b = service.save(chainId, statisticsInfo);
                    commonLog.info("chainId-" + chainId + ", height-" + height + ", save-" + b + ", new statisticsInfo-" + statisticsInfo);
                    //如果某协议版本连续统计确认数大于阈值，则进行版本升级
                    if (statisticsInfo.getCount() >= version.getContinuousIntervalCount()) {
                        //设置新协议版本
                        context.setCurrentProtocolVersion(version);
                        context.setCurrentProtocolVersionCount(statisticsInfo.getCount());
                        context.getProtocolVersionHistory().push(version);
                        commonLog.info("chainId-" + chainId + ", height-"+ height + ", new protocol version available-" + version);
                    }
                    context.setCount(0);
                    context.setLastValidStatisticsInfo(statisticsInfo);
                    //清除旧统计数据
                    proportionMap.clear();
                    return;
                }
                //已经统计了1000个区块中的400个，但是还没有新协议生效，后面的就不需要统计了
                if (already > interval - (interval * parameters.getEffectiveRatioMinimum() / 100)) {
                    break;
                }
            }
            //初始化一条旧统计信息，与区块高度绑定，并存到数据库
            StatisticsInfo statisticsInfo = new StatisticsInfo();
            statisticsInfo.setHeight(height);
            statisticsInfo.setLastHeight(lastValidStatisticsInfo.getHeight());
            statisticsInfo.setProtocolVersion(currentProtocolVersion);
            statisticsInfo.setProtocolVersionMap(proportionMap);
            //计数统计
            statisticsInfo.setCount((short) (context.getCurrentProtocolVersionCount() + 1));
            boolean b = service.save(chainId, statisticsInfo);
            commonLog.info("chainId-" + chainId + ", height-" + height + ", save-" + b + ", new statisticsInfo-" + statisticsInfo);
            context.setCount(0);
            context.setLastValidStatisticsInfo(statisticsInfo);
            context.setCurrentProtocolVersionCount(context.getCurrentProtocolVersionCount() + 1);
            //清除旧统计数据
            proportionMap.clear();
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
        short blockVersion = data.getBlockVersion();
        ProtocolVersion currentProtocolVersion = context.getCurrentProtocolVersion();
        if (currentProtocolVersion.getVersion() > blockVersion) {
            return false;
        }
        ChainParameters parameters = context.getParameters();
        byte effectiveRatio = data.getEffectiveRatio();
        if (effectiveRatio > parameters.getEffectiveRatioMaximum()) {
            return false;
        }
        if (effectiveRatio < parameters.getEffectiveRatioMinimum()) {
            return false;
        }
        short continuousIntervalCount = data.getContinuousIntervalCount();
        if (continuousIntervalCount > parameters.getContinuousIntervalCountMaximum()) {
            return false;
        }
        return continuousIntervalCount >= parameters.getContinuousIntervalCountMinimum();
    }
}
