package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.service.MultiSignAccountService;
import io.nuls.account.util.log.LogUtil;
import io.nuls.base.data.MultiSigAccount;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.message.Response;
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
    public Response createMultiSigAccount(Map params) {
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
            map.put("address",multiSigAccount.getAddress().getBase58());
            map.put("minSigns",minSigns);
            map.put("pubKeys",pubKeys);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_createMultiSigAccount end");
        return success(map);
    }

    /**
     * 导入多签账户
     * <p>
     * import multi sign account
     *
     * @param params [chainId,address,pubKeys,minSigns]
     * @return
     */
    @CmdAnnotation(cmd = "ac_importMultiSigAccount", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "inport a multi sign account")
    public Response importMultiSigAccount(Map params) {
        LogUtil.debug("ac_importMultiSigAccount start");
        Map<String, Object> map = new HashMap<>();
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            Object pubKeysObj = params == null ? null : params.get(RpcParameterNameConstant.PUB_KEYS);
            Object minSignsObj = params == null ? null : params.get(RpcParameterNameConstant.MIN_SIGNS);
            if (params == null || chainIdObj == null || addressObj == null || pubKeysObj == null || minSignsObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            int chainId = (int) chainIdObj;
            String address = (String) addressObj;
            List<String> pubKeys = (List<String>) pubKeysObj;
            int minSigns = (int) minSignsObj;
            //create the account
            MultiSigAccount multiSigAccount = multiSignAccountService.importMultiSigAccount(chainId,address,pubKeys, minSigns);
            if (multiSigAccount == null) { //create failed
                throw new NulsRuntimeException(AccountErrorCode.FAILED);
            }
            map.put("address",multiSigAccount.getAddress().getBase58());
            map.put("minSigns",minSigns);
            map.put("pubKeys",pubKeys);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_createMultiSigAccount end");
        return success(map);
    }

    /**
     * 移出多签账户
     * <p>
     * import multi sign account
     *
     * @param params [chainId,address]
     * @return
     */
    @CmdAnnotation(cmd = "ac_removeMultiSigAccount", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "remove the multi sign account")
    public Response removeMultiSigAccount(Map params) {
        LogUtil.debug("ac_removeMultiSigAccount start");
        Map<String, Object> map = new HashMap<>();
        try {
            // check parameters
            Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
            Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            // parse params
            int chainId = (int) chainIdObj;
            String address = (String) addressObj;
            //create the account
            boolean result = multiSignAccountService.removeMultiSigAccount(chainId,address);
            map.put("value",result);
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        }
        LogUtil.debug("ac_removeMultiSigAccount end");
        return success(map);
    }

    /**
     * set the alias of multi sign account
     *
     * @param params
     * @return txhash
     */
    @CmdAnnotation(cmd = "ac_setMultiSigAlias", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "set the alias of multi sign account")
    public Object setMultiAlias(Map params) {
        LogUtil.debug("ac_setMultiSigAlias start,params size:{}", params == null ? 0 : params.size());
        int chainId;
        String address, password, alias, signAddress,txHash = null;
        Object chainIdObj = params == null ? null : params.get(RpcParameterNameConstant.CHAIN_ID);
        Object addressObj = params == null ? null : params.get(RpcParameterNameConstant.ADDRESS);
        Object passwordObj = params == null ? null : params.get(RpcParameterNameConstant.PASSWORD);
        Object aliasObj = params == null ? null : params.get(RpcParameterNameConstant.ALIAS);
        Object aliasObj = params == null ? null : params.get(RpcParameterNameConstant.ALIAS);
        try {
            // check parameters
            if (params == null || chainIdObj == null || addressObj == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }
            chainId = (Integer) chainIdObj;
            address = (String) addressObj;
            password = (String) passwordObj;
            alias = (String) aliasObj;
            Transaction transaction = aliasService.setAlias(chainId, address, password, alias);
            if (transaction != null && transaction.getHash() != null) {
                txHash = transaction.getHash().getDigestHex();
            }
        } catch (NulsRuntimeException e) {
            LogUtil.info("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LogUtil.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, String> result = new HashMap<>();
        result.put("txHash", txHash);
        LogUtil.debug("ac_getAliasByAddress end");
        return success(result);
    }

}
