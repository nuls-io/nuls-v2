package io.nuls.account.rpc.cmd;

import com.fasterxml.jackson.databind.DeserializationFeature;
import io.nuls.account.config.NulsConfig;
import io.nuls.account.constant.AccountConstant;
import io.nuls.account.constant.AccountErrorCode;
import io.nuls.account.constant.RpcParameterNameConstant;
import io.nuls.account.model.dto.CoinDto;
import io.nuls.account.model.dto.TransferDto;
import io.nuls.account.service.TransactionService;
import io.nuls.account.util.TxUtil;
import io.nuls.account.util.annotation.ResisterTx;
import io.nuls.account.util.annotation.TxMethodType;
import io.nuls.account.util.log.LogUtil;
import io.nuls.account.util.manager.ChainManager;
import io.nuls.account.util.validator.TxValidator;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.Transaction;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.data.BigIntegerUtils;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.parse.JSONUtils;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/5
 */
@Component
public class TransactionCmd extends BaseCmd {

    @Autowired
    private TransactionService transactionService;
    @Autowired
    private ChainManager chainManager;
    @Autowired
    private TxValidator txValidator;

    /**
     * 转账交易验证
     */
    @CmdAnnotation(cmd = "ac_transferTxValidate", version = 1.0, description = "create transfer transaction validate 1.0")
    @ResisterTx(txType = AccountConstant.TX_TYPE_TRANSFER, methodType = TxMethodType.VALID, methodName = "ac_transferTxValidate")
    @Parameter(parameterName = RpcParameterNameConstant.CHAIN_ID, parameterType = "int")
    @Parameter(parameterName = RpcParameterNameConstant.TX_HEX, parameterType = "String")
    public Response transferTxValidate(Map<String, Object> params) {
        LogUtil.debug("ac_transferTxValidate start");
        boolean result;
        try {
            if (params.get(RpcParameterNameConstant.CHAIN_ID) == null || params.get(RpcParameterNameConstant.TX_HEX) == null) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            int chainId = (Integer) params.get(RpcParameterNameConstant.CHAIN_ID);
            String txHex = (String) params.get(RpcParameterNameConstant.TX_HEX);
            if (chainId <= 0) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            Transaction transaction = TxUtil.getTransaction(txHex);
            result = txValidator.validateTx(chainId, transaction);
        } catch (NulsException e) {
            LogUtil.warn("", e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            LogUtil.error("", e);
            return failed(AccountErrorCode.SYS_UNKOWN_EXCEPTION);
        }
        Map<String, Boolean> resultMap = new HashMap<>();
        resultMap.put("value", result);
        LogUtil.debug("ac_transferTxValidate end");
        return success(resultMap);
    }

    /**
     * 转账交易提交
     */
    @CmdAnnotation(cmd = "ac_transferTxCommit", version = 1.0, description = "create transfer transaction commit 1.0")
    @ResisterTx(txType = AccountConstant.TX_TYPE_TRANSFER, methodType = TxMethodType.COMMIT, methodName = "ac_transferTxCommit")
    public Response transferTxCommit(Map<String, Object> params) {
        return success();
    }

    /**
     * 转账交易回滚
     */
    @CmdAnnotation(cmd = "ac_transferTxRollback", version = 1.0, description = "create transfer transaction rollback 1.0")
    @ResisterTx(txType = AccountConstant.TX_TYPE_TRANSFER, methodType = TxMethodType.ROLLBACK, methodName = "ac_transferTxRollback")
    public Response transferTxRollback(Map<String, Object> params) {
        return success();
    }

    /**
     * 创建多账户转账交易
     * create a multi-account transfer transaction
     *
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "ac_transfer", version = 1.0, scope = "private", minEvent = 0, minPeriod = 0, description = "create a multi-account transfer transaction")
    public Response transfer(Map params) {
        LogUtil.debug("ac_transfer start");
        Map<String, String> map = new HashMap<>(1);
        try {
            // check parameters
            if (params == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // parse params
            JSONUtils.getInstance().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            TransferDto transferDto = JSONUtils.json2pojo(JSONUtils.obj2json(params), TransferDto.class);
            List<CoinDto> inputList = transferDto.getInputs();
            List<CoinDto> outputList = transferDto.getOutputs();
            if (inputList == null || outputList == null) {
                throw new NulsRuntimeException(AccountErrorCode.NULL_PARAMETER);
            }

            // check address validity
            BigInteger fromTotal = BigInteger.ZERO;
            for (CoinDto from : inputList) {
                if (!AddressTool.validAddress(from.getAssetsChainId(), from.getAddress())) {
                    throw new NulsException(AccountErrorCode.ADDRESS_ERROR);
                }
                fromTotal = fromTotal.add(from.getAmount());
            }
            BigInteger toTotal = BigInteger.ZERO;
            for (CoinDto to : outputList) {
                if (!AddressTool.validAddress(to.getAssetsChainId(), to.getAddress())) {
                    throw new NulsException(AccountErrorCode.ADDRESS_ERROR);
                }
                toTotal = toTotal.add(to.getAmount());
            }

            // check transfer amount
            if (BigIntegerUtils.isLessThan(fromTotal, BigInteger.ZERO) || BigIntegerUtils.isLessThan(toTotal, BigInteger.ZERO)) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            // check transaction remark
            if (!validTxRemark(transferDto.getRemark())) {
                throw new NulsException(AccountErrorCode.PARAMETER_ERROR);
            }
            String txDigestHex = transactionService.transfer(transferDto.getChainId(), inputList, outputList, transferDto.getRemark());
            map.put("value", txDigestHex);
        } catch (NulsException e) {
            return failed(e.getErrorCode());
        } catch (NulsRuntimeException e) {
            return failed(e.getErrorCode());
        } catch (IOException e) {
            return failed(e.getMessage());
        } catch (Exception e) {
            return failed(e.getMessage());
        }
        LogUtil.debug("ac_transfer end");
        return success(map);
    }

    /**
     * 校验转账交易备注是否有效
     *
     * @param remark
     * @return
     */
    private boolean validTxRemark(String remark) {
        if (StringUtils.isBlank(remark)) {
            return true;
        }
        try {
            byte[] bytes = remark.getBytes(NulsConfig.DEFAULT_ENCODING);
            if (bytes.length > 100) {
                return false;
            }
            return true;
        } catch (UnsupportedEncodingException e) {
            return false;
        }
    }
}
