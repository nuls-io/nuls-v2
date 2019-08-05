/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.provider.api.jsonrpc.controller;

import io.nuls.base.api.provider.account.facade.*;
import io.nuls.provider.api.config.Context;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.account.AccountService;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Controller;
import io.nuls.core.core.annotation.RpcMethod;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.FormatValidUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.provider.model.dto.AccountBalanceDto;
import io.nuls.provider.model.dto.AccountKeyStoreDto;
import io.nuls.provider.model.jsonrpc.RpcResult;
import io.nuls.provider.model.jsonrpc.RpcResultError;
import io.nuls.provider.rpctools.AccountTools;
import io.nuls.provider.rpctools.LegderTools;
import io.nuls.provider.rpctools.vo.AccountBalance;
import io.nuls.provider.utils.ResultUtil;
import io.nuls.provider.utils.VerifyUtils;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;
import io.nuls.v2.model.annotation.ApiType;
import io.nuls.v2.model.dto.AccountDto;
import io.nuls.v2.model.dto.AliasDto;
import io.nuls.v2.model.dto.MultiSignAliasDto;
import io.nuls.v2.model.dto.SignDto;
import io.nuls.v2.util.NulsSDKTool;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Niels
 */
@Controller
@Api(type = ApiType.JSONRPC)
public class AccountController {

    @Autowired
    private LegderTools legderTools;
    @Autowired
    private AccountTools accountTools;

    AccountService accountService = ServiceManager.get(AccountService.class);

    @RpcMethod("createAccount")
    @ApiOperation(description = "批量创建账户", order = 101, detailDesc = "创建的账户存在于本地钱包内")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "count", requestType = @TypeDescriptor(value = int.class), parameterDes = "创建数量"),
            @Parameter(parameterName = "password", requestType = @TypeDescriptor(value = String.class), parameterDes = "密码")
    })
    @ResponseData(name = "返回值", description = "返回账户地址集合", responseType = @TypeDescriptor(value = List.class, collectionElement = String.class))
    public RpcResult createAccount(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId, count;
        String password;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            count = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[count] is inValid");
        }
        try {
            password = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        if (!FormatValidUtils.validPassword(password)) {
            return RpcResult.paramError("[password] is inValid");
        }

        CreateAccountReq req = new CreateAccountReq(count, password);
        req.setChainId(chainId);
        Result<String> result = accountService.createAccount(req);
        RpcResult rpcResult = new RpcResult();
        if (result.isFailed()) {
            rpcResult.setError(new RpcResultError(result.getStatus(), result.getMessage(), null));
        } else {
            rpcResult.setResult(result.getList());
        }
        return rpcResult;
    }

    @RpcMethod("updatePassword")
    @ApiOperation(description = "修改账户密码", order = 102)
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "账户地址"),
            @Parameter(parameterName = "oldPassword", requestType = @TypeDescriptor(value = String.class), parameterDes = "原密码"),
            @Parameter(parameterName = "newPassword", requestType = @TypeDescriptor(value = String.class), parameterDes = "新密码")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = Boolean.class, description = "是否修改成功")
    }))
    public RpcResult updatePassword(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId;
        String address, oldPassword, newPassword;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            oldPassword = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[oldPassword] is inValid");
        }
        try {
            newPassword = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[newPassword] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!FormatValidUtils.validPassword(oldPassword)) {
            return RpcResult.paramError("[oldPassword] is inValid");
        }
        if (!FormatValidUtils.validPassword(newPassword)) {
            return RpcResult.paramError("[newPassword] is inValid");
        }
        UpdatePasswordReq req = new UpdatePasswordReq(address, oldPassword, newPassword);
        req.setChainId(chainId);
        Result<Boolean> result = accountService.updatePassword(req);
        RpcResult rpcResult = new RpcResult();
        if (result.isSuccess()) {
            rpcResult.setResult(result.getData());
        } else {
            rpcResult.setError(new RpcResultError(result.getStatus(), result.getMessage(), null));
        }
        return rpcResult;
    }

    @RpcMethod("getPriKey")
    @ApiOperation(description = "导出账户私钥", order = 103, detailDesc = "只能导出本地钱包已存在账户的私钥")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "address", parameterDes = "账户地址"),
            @Parameter(parameterName = "password", parameterDes = "密码")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "私钥")
    }))
    public RpcResult getPriKey(List<Object> params) {
        int chainId;
        String address, password;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            password = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!FormatValidUtils.validPassword(password)) {
            return RpcResult.paramError("[password] is inValid");
        }

        GetAccountPrivateKeyByAddressReq req = new GetAccountPrivateKeyByAddressReq(password, address);
        req.setChainId(chainId);
        Result<String> result = accountService.getAccountPrivateKey(req);
        RpcResult rpcResult = new RpcResult();
        if (result.isSuccess()) {
            rpcResult.setResult(result.getData());
        } else {
            rpcResult.setError(new RpcResultError(result.getStatus(), result.getMessage(), null));
        }
        return rpcResult;
    }

    @RpcMethod("importPriKey")
    @ApiOperation(description = "根据私钥导入账户", order = 104, detailDesc = "导入私钥时，需要输入密码给明文私钥加密")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "priKey", requestType = @TypeDescriptor(value = String.class), parameterDes = "账户明文私钥"),
            @Parameter(parameterName = "password", requestType = @TypeDescriptor(value = String.class), parameterDes = "新密码")
    })
    @ResponseData(name = "返回值", description = "返回账户地址", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "账户地址")
    }))
    public RpcResult importPriKey(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId;
        String priKey, password;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            priKey = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[priKey] is inValid");
        }
        try {
            password = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        if (StringUtils.isBlank(priKey)) {
            return RpcResult.paramError("[priKey] is inValid");
        }
        if (!FormatValidUtils.validPassword(password)) {
            return RpcResult.paramError("[password] is inValid");
        }

        ImportAccountByPrivateKeyReq req = new ImportAccountByPrivateKeyReq(password, priKey, true);
        req.setChainId(chainId);
        Result<String> result = accountService.importAccountByPrivateKey(req);
        RpcResult rpcResult = new RpcResult();
        if (result.isSuccess()) {
            rpcResult.setResult(result.getData());
        } else {
            rpcResult.setError(new RpcResultError(result.getStatus(), result.getMessage(), null));
        }
        return rpcResult;
    }

    @RpcMethod("importKeystore")
    @ApiOperation(description = "根据keystore导入账户", order = 105)
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "keyStoreJson", requestType = @TypeDescriptor(value = Map.class), parameterDes = "keyStoreJson"),
            @Parameter(parameterName = "password", requestType = @TypeDescriptor(value = String.class), parameterDes = "keystore密码")
    })
    @ResponseData(name = "返回值", description = "返回账户地址", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "账户地址")
    }))
    public RpcResult importKeystore(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId;
        String password, keyStoreJson;
        Map keyStoreMap;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            keyStoreMap = (Map) params.get(1);
            keyStoreJson = JSONUtils.obj2json(keyStoreMap);
        } catch (Exception e) {
            return RpcResult.paramError("[keyStoreJson] is inValid");
        }
        try {
            password = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        if (!FormatValidUtils.validPassword(password)) {
            return RpcResult.paramError("[password] is inValid");
        }

        ImportAccountByKeyStoreReq req = new ImportAccountByKeyStoreReq(password, HexUtil.encode(keyStoreJson.getBytes()), true);
        req.setChainId(chainId);
        Result<String> result = accountService.importAccountByKeyStore(req);
        RpcResult rpcResult = new RpcResult();
        if (result.isSuccess()) {
            rpcResult.setResult(result.getData());
        } else {
            rpcResult.setError(new RpcResultError(result.getStatus(), result.getMessage(), null));
        }
        return rpcResult;
    }

    @RpcMethod("exportKeystore")
    @ApiOperation(description = "账户备份，导出账户keystore信息", order = 106)
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "账户地址"),
            @Parameter(parameterName = "password", requestType = @TypeDescriptor(value = String.class), parameterDes = "账户密码")
    })
    @ResponseData(name = "返回值", description = "返回keystore字符串", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "result", description = "keystore")
    }))
    public RpcResult exportKeystore(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId;
        String address, password;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            password = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!FormatValidUtils.validPassword(password)) {
            return RpcResult.paramError("[password] is inValid");
        }
        KeyStoreReq req = new KeyStoreReq(password, address);
        req.setChainId(chainId);
        Result<String> result = accountService.getAccountKeyStore(req);
        RpcResult rpcResult = new RpcResult();
        try {
            if (result.isSuccess()) {
                AccountKeyStoreDto keyStoreDto = JSONUtils.json2pojo(result.getData(), AccountKeyStoreDto.class);
                rpcResult.setResult(keyStoreDto);
            } else {
                rpcResult.setError(new RpcResultError(result.getStatus(), result.getMessage(), null));
            }
            return rpcResult;
        } catch (IOException e) {
            return RpcResult.failed(CommonCodeConstanst.DATA_PARSE_ERROR);
        }
    }

    @RpcMethod("getAccountBalance")
    @ApiOperation(description = "查询账户余额", order = 107, detailDesc = "根据资产链ID和资产ID，查询本链账户对应资产的余额与nonce值")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "assetChainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "资产的链ID"),
            @Parameter(parameterName = "assetId", requestType = @TypeDescriptor(value = int.class), parameterDes = "资产ID"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "账户地址")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = AccountBalanceDto.class))
    public RpcResult getAccountBalance(List<Object> params) {
        VerifyUtils.verifyParams(params, 4);
        int chainId, assetChainId, assetId;
        String address;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            assetChainId = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[assetChainId] is inValid");
        }
        try {
            assetId = (int) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[assetId] is inValid");
        }
        try {
            address = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }

        if (!Context.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }
        RpcResult rpcResult = new RpcResult();
        Result<AccountBalance> balanceResult = legderTools.getBalanceAndNonce(chainId, assetChainId, assetId, address);
        if (balanceResult.isFailed()) {
            return rpcResult.setError(new RpcResultError(balanceResult.getStatus(), balanceResult.getMessage(), null));
        }
        return rpcResult.setResult(balanceResult.getData());
    }

    @RpcMethod("setAlias")
    @ApiOperation(description = "设置账户别名", order = 108, detailDesc = "别名格式为1-20位小写字母和数字的组合，设置别名会销毁1个NULS")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "address", requestType = @TypeDescriptor(value = String.class), parameterDes = "账户地址"),
            @Parameter(parameterName = "alias", requestType = @TypeDescriptor(value = String.class), parameterDes = "别名"),
            @Parameter(parameterName = "password", requestType = @TypeDescriptor(value = String.class), parameterDes = "账户密码")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "设置别名交易的hash")
    }))
    public RpcResult setAlias(List<Object> params) {
        int chainId;
        String address, alias, password;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            alias = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[alias] is inValid");
        }
        try {
            password = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.dataNotFound();
        }

        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (!FormatValidUtils.validAlias(alias)) {
            return RpcResult.paramError("[alias] is inValid");
        }
        if (StringUtils.isBlank(password)) {
            return RpcResult.paramError("[password] is inValid");
        }
        SetAccountAliasReq aliasReq = new SetAccountAliasReq(password, address, alias);
        Result<String> result = accountService.setAccountAlias(aliasReq);
        RpcResult rpcResult = new RpcResult();
        if (result.isSuccess()) {
            rpcResult.setResult(result.getData());
        } else {
            rpcResult.setError(new RpcResultError(result.getStatus(), result.getMessage(), null));
        }
        return rpcResult;
    }

    @RpcMethod("createAccountOffline")
    @ApiOperation(description = "离线 - 批量创建账户", order = 151, detailDesc = "创建的账户不会保存到钱包中,接口直接返回账户的keystore信息")
    @Parameters(value = {
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "count", requestType = @TypeDescriptor(value = int.class), parameterDes = "创建数量"),
            @Parameter(parameterName = "prefix", requestType = @TypeDescriptor(value = String.class), parameterDes = "地址前缀", canNull = true),
            @Parameter(parameterName = "password", requestType = @TypeDescriptor(value = String.class), parameterDes = "密码")
    })
    @ResponseData(name = "返回值", description = "返回账户信息集合", responseType = @TypeDescriptor(value = List.class, collectionElement = AccountDto.class))
    public RpcResult createAccountOffline(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        int chainId, count;
        String prefix, password;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            count = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[count] is inValid");
        }
        try {
            prefix = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[prefix] is inValid");
        }
        try {
            password = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        if (!FormatValidUtils.validPassword(password)) {
            return RpcResult.paramError("[password] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        io.nuls.core.basic.Result<List<AccountDto>> result;
        if (StringUtils.isBlank(prefix)) {
            result = NulsSDKTool.createOffLineAccount(count, password);
        } else {
            result = NulsSDKTool.createOffLineAccount(count, prefix, password);
        }
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("getPriKeyOffline")
    @ApiOperation(description = "离线获取账户明文私钥", order = 152)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址"),
            @Parameter(parameterName = "encryptedPrivateKey", parameterType = "String", parameterDes = "账户密文私钥"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "明文私钥")
    }))
    public RpcResult getPriKeyOffline(List<Object> params) {
        int chainId;
        String address, encryptedPriKey, password;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            encryptedPriKey = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[encryptedPriKey] is inValid");
        }
        try {
            password = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        io.nuls.core.basic.Result result = NulsSDKTool.getPriKeyOffline(address, encryptedPriKey, password);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("resetPasswordOffline")
    @ApiOperation(description = "离线修改账户密码", order = 153)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址"),
            @Parameter(parameterName = "encryptedPrivateKey", parameterType = "String", parameterDes = "账户密文私钥"),
            @Parameter(parameterName = "oldPassword", parameterType = "String", parameterDes = "原密码"),
            @Parameter(parameterName = "newPassword", parameterType = "String", parameterDes = "新密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "重置密码后的加密私钥")
    }))
    public RpcResult resetPasswordOffline(List<Object> params) {
        int chainId;
        String address, encryptedPriKey, oldPassword, newPassword;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            address = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            encryptedPriKey = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[encryptedPriKey] is inValid");
        }
        try {
            oldPassword = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[oldPassword] is inValid");
        }
        try {
            newPassword = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[newPassword] is inValid");
        }

        io.nuls.core.basic.Result result = NulsSDKTool.resetPasswordOffline(address, encryptedPriKey, oldPassword, newPassword);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("multiSign")
    @ApiOperation(description = "多账户摘要签名", order = 154, detailDesc = "用于签名离线组装的多账户转账交易,调用接口时，参数可以传地址和私钥，或者传地址和加密私钥和加密密码")
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "signDtoList", parameterDes = "摘要签名表单", requestType = @TypeDescriptor(value = List.class, collectionElement = SignDto.class)),
            @Parameter(parameterName = "txHex", parameterType = "String", parameterDes = "交易序列化16进制字符串")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "签名后的交易16进制字符串")
    }))
    public RpcResult multiSign(List<Object> params) {
        int chainId;
        String txHex;
        List<Map> signMap;
        List<SignDto> signDtoList = new ArrayList<>();
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }

        try {
            signMap = (List<Map>) params.get(1);
            for (Map map : signMap) {
                SignDto signDto = JSONUtils.map2pojo(map, SignDto.class);
                signDtoList.add(signDto);
            }
        } catch (Exception e) {
            return RpcResult.paramError("[signDto] is inValid");
        }
        txHex = (String) params.get(2);

        io.nuls.core.basic.Result result = NulsSDKTool.sign(signDtoList, txHex);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("priKeySign")
    @ApiOperation(description = "明文私钥摘要签名", order = 155)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "txHex", parameterType = "String", parameterDes = "交易序列化16进制字符串"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址"),
            @Parameter(parameterName = "privateKey", parameterType = "String", parameterDes = "账户明文私钥")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "签名后的交易16进制字符串")
    }))
    public RpcResult sign(List<Object> params) {
        int chainId;
        String txHex, address, priKey;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            txHex = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[txHex] is inValid");
        }
        try {
            address = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            priKey = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[priKey] is inValid");
        }
        if (!Context.isChainExist(chainId)) {
            return RpcResult.paramError(String.format("chainId [%s] is invalid", chainId));
        }
        if (StringUtils.isBlank(txHex)) {
            return RpcResult.paramError("[txHex] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (StringUtils.isBlank(priKey)) {
            return RpcResult.paramError("[priKey] is inValid");
        }

        io.nuls.core.basic.Result result = NulsSDKTool.sign(txHex, address, priKey);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("encryptedPriKeySign")
    @ApiOperation(description = "密文私钥摘要签名", order = 156)
    @Parameters({
            @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链ID"),
            @Parameter(parameterName = "txHex", parameterType = "String", parameterDes = "交易序列化16进制字符串"),
            @Parameter(parameterName = "address", parameterType = "String", parameterDes = "账户地址"),
            @Parameter(parameterName = "encryptedPrivateKey", parameterType = "String", parameterDes = "账户密文私钥"),
            @Parameter(parameterName = "password", parameterType = "String", parameterDes = "密码")
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "签名后的交易16进制字符串")
    }))
    public RpcResult encryptedPriKeySign(List<Object> params) {
        int chainId;
        String txHex, address, encryptedPriKey, password;
        try {
            chainId = (int) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[chainId] is inValid");
        }
        try {
            txHex = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[txHex] is inValid");
        }
        try {
            address = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            encryptedPriKey = (String) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[encryptedPriKey] is inValid");
        }
        try {
            password = (String) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[password] is inValid");
        }
        if (StringUtils.isBlank(txHex)) {
            return RpcResult.paramError("[txHex] is inValid");
        }
        if (!AddressTool.validAddress(chainId, address)) {
            return RpcResult.paramError("[address] is inValid");
        }
        if (StringUtils.isBlank(encryptedPriKey)) {
            return RpcResult.paramError("[encryptedPriKey] is inValid");
        }
        io.nuls.core.basic.Result result = NulsSDKTool.sign(txHex, address, encryptedPriKey, password);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("createMultiSignAccount")
    @ApiOperation(description = "创建多签账户", order = 157, detailDesc = "根据多个账户的公钥创建多签账户，minSigns为多签账户创建交易时需要的最小签名数")
    @Parameters(value = {
            @Parameter(parameterName = "pubKeys", requestType = @TypeDescriptor(value = List.class, collectionElement = String.class), parameterDes = "账户公钥集合"),
            @Parameter(parameterName = "minSigns", requestType = @TypeDescriptor(value = int.class), parameterDes = "最小签名数")
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "账户的地址")
    }))
    public RpcResult createMultiSignAccount(List<Object> params) {
        VerifyUtils.verifyParams(params, 2);
        int minSigns;
        List<String> pubKeys;

        try {
            pubKeys = (List<String>) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[pubKeys] is inValid");
        }
        try {
            minSigns = (int) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[minSigns] is inValid");
        }
        if (pubKeys.isEmpty()) {
            return RpcResult.paramError("[pubKeys] is empty");
        }
        if (minSigns < 1 || minSigns > pubKeys.size()) {
            return RpcResult.paramError("[minSigns] is inValid");
        }

        io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignAccount(pubKeys, minSigns);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("createAliasTx")
    @ApiOperation(description = "离线创建设置别名交易", order = 158)
    @Parameters({
            @Parameter(parameterName = "AliasDto", parameterDes = "创建别名交易表单", requestType = @TypeDescriptor(value = AliasDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化16进制字符串")
    }))
    public RpcResult createAliasTx(List<Object> params) {
        VerifyUtils.verifyParams(params, 3);
        String address, alias, nonce;
        try {
            address = (String) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            alias = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[alias] is inValid");
        }
        try {
            nonce = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[nonce] is inValid");
        }

        AliasDto dto = new AliasDto();
        dto.setAddress(address);
        dto.setAlias(alias);
        dto.setNonce(nonce);
        io.nuls.core.basic.Result result = NulsSDKTool.createAliasTxOffline(dto);
        return ResultUtil.getJsonRpcResult(result);
    }

    @RpcMethod("createMultiSignAliasTx")
    @ApiOperation(description = "多签账户离线创建设置别名交易", order = 159)
    @Parameters({
            @Parameter(parameterName = "多签账户离线创建设置别名交易", parameterDes = "创建别名交易表单", requestType = @TypeDescriptor(value = MultiSignAliasDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化16进制字符串")
    }))
    public RpcResult createMultiSignAliasTx(List<Object> params) {
        String address, alias, nonce;
        List<String> pubKeys;
        int minSigns;
        try {
            address = (String) params.get(0);
        } catch (Exception e) {
            return RpcResult.paramError("[address] is inValid");
        }
        try {
            alias = (String) params.get(1);
        } catch (Exception e) {
            return RpcResult.paramError("[alias] is inValid");
        }
        try {
            nonce = (String) params.get(2);
        } catch (Exception e) {
            return RpcResult.paramError("[nonce] is inValid");
        }
        try {
            pubKeys = (List<String>) params.get(3);
        } catch (Exception e) {
            return RpcResult.paramError("[pubKeys] is inValid");
        }
        try {
            minSigns = (int) params.get(4);
        } catch (Exception e) {
            return RpcResult.paramError("[minSigns] is inValid");
        }
        MultiSignAliasDto dto = new MultiSignAliasDto();
        dto.setAddress(address);
        dto.setAlias(alias);
        dto.setNonce(nonce);
        dto.setPubKeys(pubKeys);
        dto.setMinSigns(minSigns);
        io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignAliasTxOffline(dto);
        return ResultUtil.getJsonRpcResult(result);
    }
}
