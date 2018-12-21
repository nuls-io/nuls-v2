package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.service.MultiSignAccountService;
import io.nuls.account.util.log.LogUtil;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsRuntimeException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: EdwardChan
 *
 * @description:
 *
 * @date: Dec.20th 2018
 *
 */
@Component
public class MultiSignAccountCmd extends BaseCmd {

    @Autowired
    private MultiSignAccountService multiSignAccountService;

    /**
     * 创建多签账户
     * <p>
     * create a multi sign account
     *
     * @param params [chainId,pubKeys,minSigns]
     * @return
     */
    @CmdAnnotation(cmd = "ac_createMultiSigAccount", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "create a multi sign account")
    public Object createMultiSigAccount(Map params) {
        LogUtil.debug("ac_createMultiSigAccount start");
        Map<String, Object> map = new HashMap<>();
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object pubKeysObj = params == null ? null : params.get(RpcParameterNameConstant.PUB_KEYS);
            Object minSignsObj = params == null ? null : params.get(RpcParameterNameConstant.MIN_SIGNS);
            if (params == null || chainIdObj == null || pubKeysObj == null || minSignsObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            int chainId = (int) chainIdObj;
            List<String> pubKeys = (List<String>) pubKeysObj;
            int minSigns = (int) minSignsObj;
            //create the account
            MultiSigAccount multiSigAccount = multiSignAccountService.createMultiSigAccount(chainId, pubKeys, minSigns);
            if (multiSigAccount == null) { //create failed
                throw new NulsRuntimeException(AccountErrorCode.FAILED);
            }
            map.put("address","");
            map.put("minSigns",minSigns);
            map.put("pubKeys",pubKeys);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_createMultiSigAccount end");
        return success(map);
    }

}
