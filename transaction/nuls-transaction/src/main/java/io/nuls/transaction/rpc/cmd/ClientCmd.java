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

import io.nuls.base.data.NulsDigestData;
import io.nuls.base.data.Page;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.rpc.util.RPCUtil;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.log.Log;
import io.nuls.tools.model.ObjectUtils;
import io.nuls.transaction.constant.TxCmd;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.constant.TxErrorCode;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.po.TransactionConfirmedPO;
import io.nuls.transaction.model.po.TransactionPO;
import io.nuls.transaction.service.ConfirmedTxService;
import io.nuls.transaction.service.TxService;
import io.nuls.transaction.storage.h2.TransactionH2Service;
import io.nuls.transaction.utils.LoggerUtil;

import java.util.HashMap;
import java.util.Map;

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
    private TransactionH2Service transactionH2Service;

    /**
     * 根据hash获取交易, 先查未确认, 查不到再查已确认
     * Get the transaction that have been packaged into the block from the database
     *
     * @param params
     * @return Response
     */
    @CmdAnnotation(cmd = TxCmd.CLIENT_GETTX, version = 1.0, description = "Get transaction ")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txHash", parameterType = "String")
    public Response getTx(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHash"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txHash = (String) params.get("txHash");
            if (!NulsDigestData.validHash(txHash)) {
                throw new NulsException(TxErrorCode.HASH_ERROR);
            }
            TransactionConfirmedPO tx = txService.getTransaction(chain, NulsDigestData.fromDigestHex(txHash));
            Map<String, Object> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_4);
            if (tx == null) {
                Log.debug("getTx - from all, fail! tx is null, txHash:{}", txHash);
                resultMap.put("tx", null);
            } else {
                Log.debug("getTx - from all, success txHash : " + tx.getTx().getHash().getDigestHex());
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

    /**
     * 根据hash获取已确认交易(只查已确认)
     * Get the transaction that have been packaged into the block from the database
     *
     * @param params
     * @return Response
     */
    @CmdAnnotation(cmd = TxCmd.CLIENT_GETTX_CONFIRMED, version = 1.0, description = "Get confirmed transaction ")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "txHash", parameterType = "String")
    public Response getConfirmedTx(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            ObjectUtils.canNotEmpty(params.get("txHash"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            String txHash = (String) params.get("txHash");
            if (!NulsDigestData.validHash(txHash)) {
                throw new NulsException(TxErrorCode.HASH_ERROR);
            }
            TransactionConfirmedPO tx = confirmedTxService.getConfirmedTransaction(chain, NulsDigestData.fromDigestHex(txHash));
            Map<String, Object> resultMap = new HashMap<>(TxConstant.INIT_CAPACITY_4);
            if (tx == null) {
                Log.debug("getConfirmedTransaction fail, tx is null. txHash:{}", txHash);
                resultMap.put("tx", null);
            } else {
                Log.debug("getConfirmedTransaction success. txHash:{}", txHash);
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


    /**
     * 分页查询交易记录
     * Query the transaction list based on conditions such as account, chain, asset, and paging information.
     *
     * @param params
     * @return Response
     */
    @CmdAnnotation(cmd = TxCmd.TX_GETTXS, version = 1.0, description = "Get transaction record")
    @Parameter(parameterName = "chainId", parameterType = "int")
    public Response getTxs(Map params) {
        Chain chain = null;
        try {
            ObjectUtils.canNotEmpty(params.get("chainId"), TxErrorCode.PARAMETER_ERROR.getMsg());
            chain = chainManager.getChain((int) params.get("chainId"));
            if (null == chain) {
                throw new NulsException(TxErrorCode.CHAIN_NOT_FOUND);
            }
            Integer assetChainId = null == params.get("assetChainId") ? null : Integer.parseInt(params.get("assetChainId").toString());
            Integer assetId = null == params.get("assetId") ? null : Integer.parseInt(params.get("assetId").toString());
            Integer type = null == params.get("type") ? null : Integer.parseInt(params.get("type").toString());
            Integer pageSize = null == params.get("pageSize") ? TxConstant.PAGESIZE : Integer.parseInt(params.get("pageSize").toString());
            Integer pageNumber = null == params.get("pageNumber") ? TxConstant.PAGENUMBER : Integer.parseInt(params.get("pageNumber").toString());
            String address = (String) params.get("address");

            Page<TransactionPO> list = transactionH2Service.getTxs(address, assetChainId, assetId, type, pageNumber, pageSize);
            return success(list);
        } catch (NulsException e) {
            errorLogProcess(chain, e);
            return failed(e.getErrorCode());
        } catch (Exception e) {
            errorLogProcess(chain, e);
            return failed(TxErrorCode.SYS_UNKOWN_EXCEPTION);
        }
    }




    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LoggerUtil.Log.error(e);
        } else {
            chain.getLoggerMap().get(TxConstant.LOG_TX).error(e);
        }
    }

}
