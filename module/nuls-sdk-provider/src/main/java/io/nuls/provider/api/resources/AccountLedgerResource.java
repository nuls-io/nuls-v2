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
package io.nuls.provider.api.resources;

import io.nuls.provider.api.config.Config;
import io.nuls.base.RPCUtil;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.ledger.LedgerProvider;
import io.nuls.base.api.provider.transaction.TransferService;
import io.nuls.base.api.provider.transaction.facade.TransferReq;
import io.nuls.base.basic.AddressTool;
import io.nuls.base.basic.NulsByteBuffer;
import io.nuls.base.data.Transaction;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.provider.model.ErrorData;
import io.nuls.provider.model.RpcClientResult;
import io.nuls.provider.model.dto.AccountBalanceDto;
import io.nuls.provider.model.form.BalanceForm;
import io.nuls.provider.model.form.TransferForm;
import io.nuls.provider.model.form.TxForm;
import io.nuls.provider.model.txdata.CallContractData;
import io.nuls.provider.model.txdata.CreateContractData;
import io.nuls.provider.model.txdata.DeleteContractData;
import io.nuls.provider.rpctools.ContractTools;
import io.nuls.provider.rpctools.LegderTools;
import io.nuls.provider.rpctools.TransactionTools;
import io.nuls.provider.rpctools.vo.AccountBalance;
import io.nuls.provider.utils.Log;
import io.nuls.provider.utils.ResultUtil;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;
import io.nuls.v2.model.dto.MultiSignTransferDto;
import io.nuls.v2.model.dto.MultiSignTransferTxFeeDto;
import io.nuls.v2.model.dto.TransferDto;
import io.nuls.v2.model.dto.TransferTxFeeDto;
import io.nuls.v2.util.CommonValidator;
import io.nuls.v2.util.NulsSDKTool;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import static io.nuls.core.constant.TxType.*;
import static io.nuls.provider.utils.Utils.extractTxTypeFromTx;

/**
 * @author: PierreLuo
 * @date: 2019-06-27
 */
@Path("/api/accountledger")
@Component
@Api
public class AccountLedgerResource {

    @Autowired
    Config config;

    TransferService transferService = ServiceManager.get(TransferService.class);
    LedgerProvider ledgerProvider = ServiceManager.get(LedgerProvider.class);
    @Autowired
    TransactionTools transactionTools;
    @Autowired
    private ContractTools contractTools;
    @Autowired
    private LegderTools legderTools;

    @POST
    @Path("/balance/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "查询账户余额", order = 109, detailDesc = "根据资产链ID和资产ID，查询本链账户对应资产的余额与nonce值")
    @Parameters({
            @Parameter(parameterName = "balanceDto", parameterDes = "账户余额表单", requestType = @TypeDescriptor(value = BalanceForm.class))
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = AccountBalanceDto.class))
    public RpcClientResult getBalance(@PathParam("address") String address, BalanceForm form) {
        if (!AddressTool.validAddress(config.getChainId(), address)) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "address is invalid"));
        }
        if (form.getAssetChainId() < 1 || form.getAssetChainId() > 65535) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "assetChainId is invalid"));
        }
        if (form.getAssetId() < 1 || form.getAssetId() > 65535) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "assetId is invalid"));
        }
        Result<AccountBalance> balanceResult = legderTools.getBalanceAndNonce(config.getChainId(), form.getAssetChainId(), form.getAssetId(), address);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(balanceResult);
        if (clientResult.isSuccess()) {
            clientResult.setData(new AccountBalanceDto((AccountBalance) clientResult.getData()));
        }
        return clientResult;
    }

    @POST
    @Path("/transaction/validate")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "验证交易", order = 302, detailDesc = "验证离线组装的交易,验证成功返回交易hash值,失败返回错误提示信息")
    @Parameters({
            @Parameter(parameterName = "验证交易是否正确", parameterDes = "验证交易是否正确表单", requestType = @TypeDescriptor(value = TxForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易hash")
    }))
    public RpcClientResult validate(TxForm form) {
        if (form == null || StringUtils.isBlank(form.getTxHex())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        Result result = transactionTools.validateTx(config.getChainId(), form.getTxHex());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/transaction/broadcast")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "广播交易", order = 303, detailDesc = "广播离线组装的交易,成功返回true,失败返回错误提示信息")
    @Parameters({
            @Parameter(parameterName = "广播交易", parameterDes = "广播交易表单", requestType = @TypeDescriptor(value = TxForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = boolean.class, description = "是否成功"),
            @Key(name = "hash", description = "交易hash")
    }))
    public RpcClientResult broadcast(TxForm form) {
        if (form == null || StringUtils.isBlank(form.getTxHex())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        try {
            String txHex = form.getTxHex();
            int type = extractTxTypeFromTx(txHex);
            Result result = null;
            switch (type) {
                case CREATE_CONTRACT:
                    Transaction tx = new Transaction();
                    tx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                    CreateContractData create = new CreateContractData();
                    create.parse(new NulsByteBuffer(tx.getTxData()));
                    result = contractTools.validateContractCreate(config.getChainId(),
                            AddressTool.getStringAddressByBytes(create.getSender()),
                            create.getGasLimit(),
                            create.getPrice(),
                            RPCUtil.encode(create.getCode()),
                            create.getArgs());
                    break;
                case CALL_CONTRACT:
                    Transaction callTx = new Transaction();
                    callTx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                    CallContractData call = new CallContractData();
                    call.parse(new NulsByteBuffer(callTx.getTxData()));
                    result = contractTools.validateContractCall(config.getChainId(),
                            AddressTool.getStringAddressByBytes(call.getSender()),
                            call.getValue(),
                            call.getGasLimit(),
                            call.getPrice(),
                            AddressTool.getStringAddressByBytes(call.getContractAddress()),
                            call.getMethodName(),
                            call.getMethodDesc(),
                            call.getArgs());
                    break;
                case DELETE_CONTRACT:
                    Transaction deleteTx = new Transaction();
                    deleteTx.parse(new NulsByteBuffer(RPCUtil.decode(txHex)));
                    DeleteContractData delete = new DeleteContractData();
                    delete.parse(new NulsByteBuffer(deleteTx.getTxData()));
                    result = contractTools.validateContractDelete(config.getChainId(),
                            AddressTool.getStringAddressByBytes(delete.getSender()),
                            AddressTool.getStringAddressByBytes(delete.getContractAddress()));
                    break;
                default:
                    break;
            }
            if (result != null) {
                Map contractMap = (Map) result.getData();
                if (contractMap != null && Boolean.FALSE.equals(contractMap.get("success"))) {
                    return RpcClientResult.getFailed((String) contractMap.get("msg"));
                }
            }
            result = transactionTools.newTx(config.getChainId(), txHex);
            return ResultUtil.getRpcClientResult(result);
        } catch (Exception e) {
            Log.error(e);
            return RpcClientResult.getFailed(e.getMessage());
        }
    }

    @POST
    @Path("/transfer")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "单笔转账", order = 304, detailDesc = "发起单账户单资产的转账交易")
    @Parameters({
            @Parameter(parameterName = "单笔转账", parameterDes = "单笔转账表单", requestType = @TypeDescriptor(value = TransferForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易hash")
    }))
    public RpcClientResult transfer(TransferForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        TransferReq.TransferReqBuilder builder =
                new TransferReq.TransferReqBuilder(config.getChainId(), config.getAssetsId())
                        .addForm(form.getAddress(), form.getPassword(), form.getAmount())
                        .addTo(form.getToAddress(), form.getAmount());
        Result<String> result = transferService.transfer(builder.build(new TransferReq()));
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/createTransferTxOffline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "离线组装转账交易", order = 350, detailDesc = "根据inputs和outputs离线组装转账交易，用于单账户或多账户的转账交易。" +
            "交易手续费为inputs里本链主资产金额总和，减去outputs里本链主资产总和")
    @Parameters({
            @Parameter(parameterName = "transferDto", parameterDes = "转账交易表单", requestType = @TypeDescriptor(value = TransferDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化16进制字符串")
    }))
    public RpcClientResult createTransferTxOffline(TransferDto transferDto) {
        try {
            CommonValidator.checkTransferDto(transferDto);
            io.nuls.core.basic.Result result = NulsSDKTool.createTransferTxOffline(transferDto);
            return ResultUtil.getRpcClientResult(result);
        } catch (NulsException e) {
            return RpcClientResult.getFailed(new ErrorData(e.getErrorCode().getCode(), e.getMessage()));
        }
    }

    @POST
    @Path("/calcTransferTxFee")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "计算离线创建转账交易所需手续费", order = 351)
    @Parameters({
            @Parameter(parameterName = "TransferTxFeeDto", parameterDes = "转账交易手续费", requestType = @TypeDescriptor(value = TransferTxFeeDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易手续费"),
    }))
    public RpcClientResult calcTransferTxFee(TransferTxFeeDto dto) {
        BigInteger fee = NulsSDKTool.calcTransferTxFee(dto);
        Map map = new HashMap();
        map.put("value", fee);
        RpcClientResult result = RpcClientResult.getSuccess(map);
        return result;
    }

    @POST
    @Path("/createMultiSignTransferTxOffline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "多签账户离线组装转账交易", order = 352, detailDesc = "根据inputs和outputs离线组装转账交易，用于单账户或多账户的转账交易。" +
            "交易手续费为inputs里本链主资产金额总和，减去outputs里本链主资产总和")
    @Parameters({
            @Parameter(parameterName = "transferDto", parameterDes = "多签账户转账交易表单", requestType = @TypeDescriptor(value = MultiSignTransferDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化16进制字符串")
    }))
    public RpcClientResult createMultiTransferTxOffline(MultiSignTransferDto transferDto) {
        try {
            CommonValidator.checkMultiSignTransferDto(transferDto);
            io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignTransferTxOffline(transferDto);
            return ResultUtil.getRpcClientResult(result);
        } catch (NulsException e) {
            return RpcClientResult.getFailed(new ErrorData(e.getErrorCode().getCode(), e.getMessage()));
        }
    }

    @POST
    @Path("/calcMultiSignTransferTxFee")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "计算离线创建多签账户转账交易所需手续费", order = 353)
    @Parameters({
            @Parameter(parameterName = "MultiSignTransferTxFeeDto", parameterDes = "多签账户转账交易手续费表单", requestType = @TypeDescriptor(value = MultiSignTransferTxFeeDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "交易手续费"),
    }))
    public RpcClientResult calcMultiSignTransferTxFee(MultiSignTransferTxFeeDto dto) {
        BigInteger fee = NulsSDKTool.calcMultiSignTransferTxFee(dto);
        Map map = new HashMap();
        map.put("value", fee);
        RpcClientResult result = RpcClientResult.getSuccess(map);
        return result;
    }

}
