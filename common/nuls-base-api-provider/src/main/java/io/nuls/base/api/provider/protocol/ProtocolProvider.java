package io.nuls.base.api.provider.protocol;

import io.nuls.base.api.provider.BaseReq;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.protocol.facade.GetVersionReq;
import io.nuls.base.api.provider.protocol.facade.VersionInfo;
import io.nuls.base.basic.ProtocolVersion;

/**
 * @Author: zhoulijun
 * @Time: 2020-01-15 18:16
 * @Description: 功能描述
 */
public interface ProtocolProvider {

    Result<VersionInfo> getVersion(GetVersionReq req);

}
