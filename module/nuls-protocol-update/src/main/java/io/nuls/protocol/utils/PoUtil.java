package io.nuls.protocol.utils;

import io.nuls.base.basic.ProtocolVersion;
import io.nuls.protocol.model.po.ProtocolVersionPo;

/**
 * PO工具类
 *
 * @author captain
 * @version 1.0
 * @date 2019/5/7 15:17
 */
public class PoUtil {

    public static ProtocolVersionPo getProtocolVersionPo(ProtocolVersion protocolVersion, long beginHeight, long endHeight) {
        ProtocolVersionPo po = new ProtocolVersionPo();
        po.setVersion(protocolVersion.getVersion());
        po.setBeginHeight(beginHeight);
        po.setEndHeight(endHeight);
        po.setContinuousIntervalCount(protocolVersion.getContinuousIntervalCount());
        po.setEffectiveRatio(protocolVersion.getEffectiveRatio());
        return po;
    }

    public static ProtocolVersion getProtocolVersion(ProtocolVersionPo protocolVersionPo) {
        ProtocolVersion protocolVersion = new ProtocolVersion();
        protocolVersion.setVersion(protocolVersionPo.getVersion());
        protocolVersion.setContinuousIntervalCount(protocolVersionPo.getContinuousIntervalCount());
        protocolVersion.setEffectiveRatio(protocolVersionPo.getEffectiveRatio());
        return protocolVersion;
    }
}
