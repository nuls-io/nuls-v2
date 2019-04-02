package io.nuls.api.provider;

import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.exception.NulsException;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 11:10
 * @Description:
 *     call api fail. responseStatus != 1
 */
public class NulsApiProviderCallFailException extends NulsException {

    public NulsApiProviderCallFailException(ErrorCode message) {
        super(message);
    }

}
