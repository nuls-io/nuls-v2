/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.transaction.rpc.cmd;

import io.nuls.base.RPCUtil;
import io.nuls.base.data.NulsHash;
import io.nuls.base.data.Transaction;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.log.Log;
import io.nuls.core.model.ObjectUtils;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.transaction.cache.PackablePool;
import io.nuls.transaction.constant.TxCmd;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.VerifyLedgerResult;
import io.nuls.transaction.model.bo.VerifyResult;
import io.nuls.transaction.model.po.TransactionConfirmedPO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.service.ConfirmedTxService;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.service.impl.TransferTestImpl;
import io.nuls.transaction.storage.UnconfirmedTxStorageService;
import io.nuls.transaction.utils.LoggerUtil;
import io.nuls.transaction.utils.TxUtil;

import java.util.HashMap;
import java.util.Map;

import static io.nuls.transaction.utils.LoggerUtil.LOG;

/**
 * @author: Charlie
 * @date: 2019/3/12
 */
@Component
public class ClientCmd extends BaseCmd {

    @Autowired
    private TxService txService;

    @Autowired
    private ConfirmedTxService confirmedTxService;

    @Autowired
    private ChainManager chainManager;

    @Autowired
    private UnconfirmedTxStorageService unconfirmedTxStorageService;

    @Autowired
    private PackablePool packablePool;

    @CmdAnnotation(cmd = TxCmd.CLIENT_GETTX, version = 1.0, description = "根据hash获取交易，先查未确认，查不到再查已确认/Get transaction by tx hash")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "待查询交易hash")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象，包含三个key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx", description = "获取到的交易的序列化数据的字符串"),
            @Key(name = "height", description = "获取到的交易的确认高度，未确认交易高度为-1"),
            @Key(name = "status", description = "获取到的交易是否确认的状态")
    }))
    public Response getTx(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHash"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txHash = (String) params.get("txHash");
            if (!NulsHash.validHash(txHash)) {
                throw new NulsException(TxErrorCode.HASH_ERROR);
            }
            TransactionConfirmedPO tx = txService.getTransaction(chain, NulsHash.fromHex(txHash));
            Map<String, Object> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_4);
            if (tx == null) {
                resultMap.put("tx", null);
            } else {
                resultMap.put("tx", RPCUtil.encode(tx.getTx().serialize()));
                resultMap.put("height", tx.getBlockHeight());
                resultMap.put("status", tx.getStatus());
            }
            return success(resultMap);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @CmdAnnotation(cmd = TxCmd.CLIENT_GETTX_CONFIRMED, version = 1.0, description = "根据hash获取已确认交易(只查已确认)/Get confirmed transaction by tx hash")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "待查询交易hash")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象，包含三个key", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx", description = "获取到的交易的序列化数据的字符串"),
            @Key(name = "height", description = "获取到的交易的确认高度"),
            @Key(name = "status", description = "获取到的交易是否确认的状态")
    }))
    public Response getConfirmedTx(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHash"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txHash = (String) params.get("txHash");
            if (!NulsHash.validHash(txHash)) {
                throw new NulsException(TxErrorCode.HASH_ERROR);
            }
            TransactionConfirmedPO tx = confirmedTxService.getConfirmedTransaction(chain, NulsHash.fromHex(txHash));
            Map<String, Object> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_4);
            if (tx == null) {
                LOG.debug("getConfirmedTransaction fail, tx is null. txHash:{}", txHash);
                resultMap.put("tx", null);
            } else {
                LOG.debug("getConfirmedTransaction success. txHash:{}", txHash);
                resultMap.put("tx", RPCUtil.encode(tx.getTx().serialize()));
                resultMap.put("height", tx.getBlockHeight());
                resultMap.put("status", tx.getStatus());
            }
            return success(resultMap);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }


    @CmdAnnotation(cmd = TxCmd.TX_VERIFYTX, version = 1.0, description = "验证交易接口，包括含基础验证、验证器、账本验证/Verify transation")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
            @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "待验证交易完整字符串")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易hash")
    }))
    public Response verifyTx(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("tx"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((Integer) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txStr = (String) params.get("tx");
            //将txStr转换为Transaction对象
            Transaction tx = TxUtil.getInstanceRpcStr(txStr, Transaction.class);

            VerifyResult verifyResult = txService.verify(chain, tx, true);
            if(!verifyResult.getResult()){
                return failed(verifyResult.getErrorCode());
            }
            VerifyLedgerResult verifyLedgerResult = LedgerCall.verifyCoinData(chain, RPCUtil.encode(tx.serialize()));
            if(!verifyLedgerResult.getSuccess()){
                return failed(verifyLedgerResult.getErrorCode());
            }
            Map<String, Object> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_2);
            resultMap.put("value", tx.getHash().toHex());
            return success(resultMap);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }

    @Autowired
    private TransferTestImpl transferTest;

    /**
     * cmd 执行批量发交易的测试用例
     * @param params
     * @return
     */
    @CmdAnnotation(cmd = "transferCMDTest", version = 1.0, description = "")
    public Response transferCMDTest(Map params) {
        try {
            ObjectUtils.canNotEmpty(params.get("act"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("address1"), TxErrorCode.PARAMETER_ERROR.getMsg());
            Integer method = (Integer) params.get("act");
            String address1 = (String) params.get("address1");
            String adddress2 = null;
            transferTest.importPriKeyTest();
            Log.info("transferCMDTest -method:{} -address1:{} -address2:{}",method,address1);
            LoggerUtil.LOG.info("transferCMDTest -method:{} -address1:{} -address2:{}",method,address1);
            if(1 == method){
                transferTest.mAddressTransfer(address1);
            }
            if(2 == method){
                adddress2 = (String) params.get("address2");
                transferTest.mAddressTransferLjs(address1, adddress2);
            }
            return success();
        } catch (Exception e) {
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }


    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LoggerUtil.LOG.error(e);
        } else {
            chain.getLogger().error(e);
        }
    }

}
