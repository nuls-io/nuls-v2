package io.nuls.protocol.rpc.callback;

import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.BlockExtendsData;
import io.nuls.base.data.BlockHeader;
import io.nuls.protocol.manager.ContextManager;
import io.nuls.protocol.model.ProtocolContext;
import io.nuls.protocol.model.ProtocolVersion;
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
@NoArgsConstructor
@AllArgsConstructor
public class BlockHeaderInvoke extends BaseInvoke {

    private int chainId;

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
                boolean upgrade = data.isUpgrade();
                if (upgrade) {
                    commonLog.debug("chainId-" + chainId + ", protocol update");
                    ProtocolVersion protocolVersion = new ProtocolVersion();
                    protocolVersion.setVersion(data.getBlockVersion());
                    protocolVersion.setInterval(data.getInterval());
                    protocolVersion.setEffectiveRatio(data.getEffectiveRatio());
                    protocolVersion.setContinuousIntervalCount(data.getContinuousIntervalCount());
                    List<ProtocolVersion> versionList = context.getVersionList();
                    versionList.add(protocolVersion);
                }
            } catch (NulsException e) {
                commonLog.error(e);
            }
        }

    }
}
