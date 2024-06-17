/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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
@NulsCoresCmd(module = ModuleE.TX)
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

    @CmdAnnotation(cmd = TxCmd.CLIENT_GETTX, version = 1.0, description = "according tohashObtain the transaction, first check for unconfirmed information, then check for confirmed information if not found/Get transaction by tx hash")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "Transaction to be queriedhash")
    })
    @ResponseData(name = "Return value", description = "Return aMapObject, containing threekey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx", description = "The string of serialized data obtained from the transaction"),
            @Key(name = "height", description = "The confirmation height of the transaction obtained, while the unconfirmed transaction height is-1"),
            @Key(name = "status", description = "The status of whether the obtained transaction is confirmed or not")
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

    @CmdAnnotation(cmd = TxCmd.CLIENT_GETTX_CONFIRMED, version = 1.0, description = "according tohashObtain confirmed transactions(Only check confirmed)/Get confirmed transaction by tx hash")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "txHash", parameterType = "String", parameterDes = "Transaction to be queriedhash")
    })
    @ResponseData(name = "Return value", description = "Return aMapObject, containing threekey", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "tx", description = "The string of serialized data obtained from the transaction"),
            @Key(name = "height", description = "The confirmation height of the transaction obtained"),
            @Key(name = "status", description = "The status of whether the obtained transaction is confirmed or not")
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


    @CmdAnnotation(cmd = TxCmd.TX_VERIFYTX, version = 1.0, description = "Verify transaction interfaces, including basic verification、Validator、Ledger verification/Verify transation")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "chainid"),
            @Parameter(parameterName = "tx", parameterType = "String", parameterDes = "Complete transaction string to be verified")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "transactionhash")
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
            //taketxStrConvert toTransactionobject
            Transaction tx = TxUtil.getInstanceRpcStr(txStr, Transaction.class);

            VerifyResult verifyResult = txService.verify(chain, tx);
            if (!verifyResult.getResult()) {
                return failed(verifyResult.getErrorCode());
            }
            VerifyLedgerResult verifyLedgerResult = LedgerCall.verifyCoinData(chain, RPCUtil.encode(tx.serialize()));
            if (!verifyLedgerResult.getSuccess()) {
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

    private void errorLogProcess(Chain chain, Exception e) {
        if (chain == null) {
            LoggerUtil.LOG.error(e);
        } else {
            chain.getLogger().error(e);
        }
    }

}
