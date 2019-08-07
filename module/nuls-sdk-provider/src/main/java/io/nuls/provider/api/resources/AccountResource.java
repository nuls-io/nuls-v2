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

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.base.api.provider.account.facade.*;
import io.nuls.provider.api.config.Config;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.account.AccountService;
import io.nuls.base.basic.AddressTool;
import io.nuls.core.constant.CommonCodeConstanst;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.FormatValidUtils;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import io.nuls.core.rpc.model.*;
import io.nuls.provider.model.ErrorData;
import io.nuls.provider.model.RpcClientResult;
import io.nuls.provider.model.dto.AccountKeyStoreDto;
import io.nuls.provider.model.form.*;
import io.nuls.provider.rpctools.AccountTools;
import io.nuls.provider.utils.Log;
import io.nuls.provider.utils.ResultUtil;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;
import io.nuls.v2.model.dto.AccountDto;
import io.nuls.v2.model.dto.AliasDto;
import io.nuls.v2.model.dto.MultiSignAliasDto;
import io.nuls.v2.util.NulsSDKTool;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-06-27
 */
@Path("/api/account")
@Component
@Api
public class AccountResource {

    @Autowired
    Config config;

    AccountService accountService = ServiceManager.get(AccountService.class);
    @Autowired
    private AccountTools accountTools;


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "批量创建账户", order = 101, detailDesc = "创建的账户存在于本地钱包内")
    @Parameters({
            @Parameter(parameterName = "批量创建账户", parameterDes = "批量创建账户表单", requestType = @TypeDescriptor(value = AccountCreateForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "list", valueType = List.class, valueElement = String.class, description = "账户地址")
    }))
    public RpcClientResult create(AccountCreateForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        if (form.getCount() <= 0) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "[count] is invalid"));
        }
        if (!FormatValidUtils.validPassword(form.getPassword())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "[password] is invalid"));
        }

        CreateAccountReq req = new CreateAccountReq(form.getCount(), form.getPassword());
        req.setChainId(config.getChainId());
        Result<String> result = accountService.createAccount(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("list", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @PUT
    @Path("/password/{address}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(description = "修改账户密码", order = 102)
    @Parameters({
            @Parameter(parameterName = "address", parameterDes = "账户地址"),
            @Parameter(parameterName = "账户密码信息", parameterDes = "账户密码信息表单", requestType = @TypeDescriptor(value = AccountUpdatePasswordForm.class))
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = Boolean.class, description = "是否修改成功")
    }))
    public RpcClientResult updatePassword(@PathParam("address") String address, AccountUpdatePasswordForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        if (!AddressTool.validAddress(config.getChainId(), address)) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "[address] is invalid"));
        }
        if (!FormatValidUtils.validPassword(form.getPassword())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "[password] is invalid"));
        }
        if (!FormatValidUtils.validPassword(form.getNewPassword())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "[newPassword] is invalid"));
        }
        UpdatePasswordReq req = new UpdatePasswordReq(address, form.getPassword(), form.getNewPassword());
        req.setChainId(config.getChainId());
        Result<Boolean> result = accountService.updatePassword(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/prikey/{address}")
    @Produces({MediaType.APPLICATION_JSON})
    @ApiOperation(description = "导出账户私钥", order = 103, detailDesc = "只能导出本地钱包已存在账户的私钥")
    @Parameters({
            @Parameter(parameterName = "address", parameterDes = "账户地址"),
            @Parameter(parameterName = "账户密码信息", parameterDes = "账户密码信息表单", requestType = @TypeDescriptor(value = AccountPasswordForm.class))
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "私钥")
    }))
    public RpcClientResult getPriKey(@PathParam("address") String address, AccountPasswordForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        if (address == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "address is empty"));
        }
        GetAccountPrivateKeyByAddressReq req = new GetAccountPrivateKeyByAddressReq(form.getPassword(), address);
        req.setChainId(config.getChainId());
        Result<String> result = accountService.getAccountPrivateKey(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/import/pri")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "根据私钥导入账户", order = 104, detailDesc = "导入私钥时，需要输入密码给明文私钥加密")
    @Parameters({
            @Parameter(parameterName = "根据私钥导入账户", parameterDes = "根据私钥导入账户表单", requestType = @TypeDescriptor(value = AccountPriKeyPasswordForm.class))
    })
    @ResponseData(name = "返回值", description = "返回账户地址", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "账户地址")
    }))
    public RpcClientResult importPriKey(AccountPriKeyPasswordForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        if (!FormatValidUtils.validPassword(form.getPassword())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "[password] is invalid"));
        }
        ImportAccountByPrivateKeyReq req = new ImportAccountByPrivateKeyReq(form.getPassword(), form.getPriKey(), form.getOverwrite());
        req.setChainId(config.getChainId());
        Result<String> result = accountService.importAccountByPrivateKey(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/import/keystore")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @ApiOperation(description = "根据keyStore导入账户", order = 105)
    @Parameters({
            @Parameter(parameterName = "根据私钥导入账户", parameterDes = "根据私钥导入账户表单", requestType = @TypeDescriptor(value = InputStream.class))
    })
    @ResponseData(name = "返回值", description = "返回账户地址", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "账户地址")
    }))
    public RpcClientResult importAccountByKeystoreFile(@FormDataParam("keystore") InputStream in,
                                                       @FormDataParam("password") String password,
                                                       @FormDataParam("overwrite") Boolean overwrite) {
        if (in == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "inputStream is empty"));
        }
        Result<AccountKeyStoreDto> dtoResult = this.getAccountKeyStoreDto(in);
        if (dtoResult.isFailed()) {
            return RpcClientResult.getFailed(new ErrorData(dtoResult.getStatus(), dtoResult.getMessage()));
        }
        AccountKeyStoreDto dto = dtoResult.getData();
        try {
            ImportAccountByKeyStoreReq req = new ImportAccountByKeyStoreReq(password, HexUtil.encode(JSONUtils.obj2json(dto).getBytes()), overwrite);
            req.setChainId(config.getChainId());
            Result<String> result = accountService.importAccountByKeyStore(req);
            RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
            if (clientResult.isSuccess()) {
                return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
            }
            return clientResult;
        } catch (JsonProcessingException e) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.DATA_ERROR.getCode(), e.getMessage()));
        }
    }

    @POST
    @Path("/import/keystore/path")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "根据keystore文件路径导入账户", order = 106)
    @Parameters({
            @Parameter(parameterName = "根据keystore文件路径导入账户", parameterDes = "根据keystore文件路径导入账户表单", requestType = @TypeDescriptor(value = AccountKeyStoreImportForm.class))
    })
    @ResponseData(name = "返回值", description = "返回账户地址", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "账户地址")
    }))
    public RpcClientResult importAccountByKeystoreFilePath(AccountKeyStoreImportForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        String keystore = accountService.getAccountKeystoreDto(form.getPath());
        ImportAccountByKeyStoreReq req = new ImportAccountByKeyStoreReq(form.getPassword(), HexUtil.encode(keystore.getBytes()), form.getOverwrite());
        req.setChainId(config.getChainId());
        Result<String> result = accountService.importAccountByKeyStore(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/import/keystore/json")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "根据keystore字符串导入账户", order = 107)
    @Parameters({
            @Parameter(parameterName = "根据keystore字符串导入账户", parameterDes = "根据keystore字符串导入账户表单", requestType = @TypeDescriptor(value = AccountKeyStoreJsonImportForm.class))
    })
    @ResponseData(name = "返回值", description = "返回账户地址", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "账户地址")
    }))
    public RpcClientResult importAccountByKeystoreJson(AccountKeyStoreJsonImportForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        String keystore = null;
        try {
            keystore = JSONUtils.obj2json(form.getKeystore());
        } catch (JsonProcessingException e) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "keystore is invalid"));
        }
        ImportAccountByKeyStoreReq req = new ImportAccountByKeyStoreReq(form.getPassword(), HexUtil.encode(keystore.getBytes()), form.getOverwrite());
        req.setChainId(config.getChainId());
        Result<String> result = accountService.importAccountByKeyStore(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/export/{address}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "账户备份，导出AccountKeyStore文件到指定目录", order = 108)
    @Parameters({
            @Parameter(parameterName = "address", parameterDes = "账户地址", requestType = @TypeDescriptor(value = String.class)),
            @Parameter(parameterName = "keystone导出信息", parameterDes = "keystone导出信息表单", requestType = @TypeDescriptor(value = AccountKeyStoreBackup.class))
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "path", description = "导出的文件路径")
    }))
    public RpcClientResult exportAccountKeyStore(@PathParam("address") String address, AccountKeyStoreBackup form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        if (address == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "address is empty"));
        }
        BackupAccountReq req = new BackupAccountReq(form.getPassword(), address, form.getPath());
        req.setChainId(config.getChainId());
        Result<String> result = accountService.backupAccount(req);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("path", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    private Result<AccountKeyStoreDto> getAccountKeyStoreDto(InputStream in) {
        StringBuilder ks = new StringBuilder();
        InputStreamReader inputStreamReader = null;
        BufferedReader bufferedReader = null;
        String str;
        try {
            inputStreamReader = new InputStreamReader(in);
            bufferedReader = new BufferedReader(inputStreamReader);
            while ((str = bufferedReader.readLine()) != null) {
                if (!str.isEmpty()) {
                    ks.append(str);
                }
            }
            AccountKeyStoreDto accountKeyStoreDto = JSONUtils.json2pojo(ks.toString(), AccountKeyStoreDto.class);
            return new Result(accountKeyStoreDto);
        } catch (Exception e) {
            return Result.fail(CommonCodeConstanst.FILE_OPERATION_FAILD.getCode(), "key store file error");
        } finally {
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
            if (inputStreamReader != null) {
                try {
                    inputStreamReader.close();
                } catch (IOException e) {
                    Log.error(e);
                }
            }
        }
    }

    @POST
    @Path("/alias")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "账户设置别名", order = 109, detailDesc = "别名格式为1-20位小写字母和数字的组合，设置别名会销毁1个NULS")
    @Parameters({
            @Parameter(parameterName = "账户设置别名", parameterDes = "账户设置别名表单", requestType = @TypeDescriptor(value = SetAliasForm.class))
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "设置别名交易的hash")
    }))
    public RpcClientResult setAlias(SetAliasForm form) {
        if (!AddressTool.validAddress(config.getChainId(), form.getAddress())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "address is invalid"));
        }
        if (!FormatValidUtils.validAlias(form.getAlias())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "alias is invalid"));
        }
        if (StringUtils.isBlank(form.getPassword())) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "password is invalid"));
        }
        SetAccountAliasReq aliasReq = new SetAccountAliasReq(form.getPassword(), form.getAddress(), form.getAlias());
        Result<String> result = accountService.setAccountAlias(aliasReq);
        RpcClientResult clientResult = ResultUtil.getRpcClientResult(result);
        if (clientResult.isSuccess()) {
            return clientResult.resultMap().map("value", clientResult.getData()).mapToData();
        }
        return clientResult;
    }

    @POST
    @Path("/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "离线 - 批量创建账户", order = 151, detailDesc = "创建的账户不会保存到钱包中,接口直接返回账户的keystore信息")
    @Parameters({
            @Parameter(parameterName = "离线批量创建账户", parameterDes = "离线批量创建账户表单", requestType = @TypeDescriptor(value = AccountCreateForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "list", valueType = List.class, valueElement = AccountDto.class, description = "账户keystore列表")
    }))
    public RpcClientResult createOffline(AccountCreateForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        io.nuls.core.basic.Result<List<AccountDto>> result;
        if (StringUtils.isBlank(form.getPrefix())) {
            result = NulsSDKTool.createOffLineAccount(form.getCount(), form.getPassword());
        } else {
            result = NulsSDKTool.createOffLineAccount(form.getCount(), form.getPrefix(), form.getPassword());
        }
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/priKey/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "离线获取账户明文私钥", order = 152)
    @Parameters({
            @Parameter(parameterName = "离线获取账户明文私钥", parameterDes = "离线获取账户明文私钥表单", requestType = @TypeDescriptor(value = GetPriKeyForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "明文私钥")
    }))
    public RpcClientResult getPriKeyOffline(GetPriKeyForm form) {
        io.nuls.core.basic.Result result = NulsSDKTool.getPriKeyOffline(form.getAddress(), form.getEncryptedPriKey(), form.getPassword());
        return ResultUtil.getRpcClientResult(result);
    }

    @PUT
    @Path("/password/offline/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "离线修改账户密码", order = 153)
    @Parameters({
            @Parameter(parameterName = "离线修改账户密码", parameterDes = "离线修改账户密码表单", requestType = @TypeDescriptor(value = ResetPasswordForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "重置密码后的加密私钥")
    }))
    public RpcClientResult resetPasswordOffline(ResetPasswordForm form) {
        io.nuls.core.basic.Result result = NulsSDKTool.resetPasswordOffline(form.getAddress(), form.getEncryptedPriKey(), form.getOldPassword(), form.getNewPassword());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/multi/sign")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "多账户摘要签名", order = 154, detailDesc = "用于签名离线组装的多账户转账交易，调用接口时，参数可以传地址和私钥，或者传地址和加密私钥和加密密码")
    @Parameters({
            @Parameter(parameterName = "多账户摘要签名", parameterDes = "多账户摘要签名表单", requestType = @TypeDescriptor(value = MultiSignForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "签名后的交易16进制字符串")
    }))
    public RpcClientResult multiSign(MultiSignForm form) {
        io.nuls.core.basic.Result result = NulsSDKTool.sign(form.getDtoList(), form.getTxHex());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/priKey/sign")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "明文私钥摘要签名", order = 155)
    @Parameters({
            @Parameter(parameterName = "明文私钥摘要签名", parameterDes = "明文私钥摘要签名表单", requestType = @TypeDescriptor(value = PriKeySignForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "签名后的交易16进制字符串")
    }))
    public RpcClientResult priKeySign(PriKeySignForm form) {
        io.nuls.core.basic.Result result = NulsSDKTool.sign(form.getTxHex(), form.getAddress(), form.getPriKey());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/encryptedPriKey/sign")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "密文私钥摘要签名", order = 156)
    @Parameters({
            @Parameter(parameterName = "密文私钥摘要签名", parameterDes = "密文私钥摘要签名表单", requestType = @TypeDescriptor(value = EncryptedPriKeySignForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "签名后的交易16进制字符串")
    }))
    public RpcClientResult encryptedPriKeySign(EncryptedPriKeySignForm form) {
        io.nuls.core.basic.Result result = NulsSDKTool.sign(form.getTxHex(), form.getAddress(), form.getEncryptedPriKey(), form.getPassword());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/multiSign/create")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "创建多签账户", order = 157, detailDesc = "根据多个账户的公钥创建多签账户，minSigns为多签账户创建交易时需要的最小签名数")
    @Parameters({
            @Parameter(parameterName = "创建多签账户", parameterDes = "创建多签账户表单", requestType = @TypeDescriptor(value = MultiSignAccountCreateForm.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "账户的地址")
    }))
    public RpcClientResult createMultiSignAccount(MultiSignAccountCreateForm form) {
        if (form.getPubKeys() == null || form.getPubKeys().isEmpty()) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "pubKeys is empty"));
        }
        if (form.getMinSigns() < 1 || form.getMinSigns() > form.getPubKeys().size()) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "[minSigns] is invalid"));
        }
        io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignAccount(form.getPubKeys(), form.getMinSigns());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/aliasTx/create")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "离线创建设置别名交易", order = 158, detailDesc = "根据多个账户的公钥创建多签账户，minSigns为多签账户创建交易时需要的最小签名数")
    @Parameters({
            @Parameter(parameterName = "创建多签账户", parameterDes = "创建多签账户表单", requestType = @TypeDescriptor(value = AliasDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化16进制字符串")
    }))
    public RpcClientResult createAliasTxOffLine(AliasDto dto) {
        io.nuls.core.basic.Result result = NulsSDKTool.createAliasTxOffline(dto);
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/multiSign/aliasTx/create")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "多签账户离线创建设置别名交易", order = 159)
    @Parameters({
            @Parameter(parameterName = "多签账户离线创建设置别名交易", parameterDes = "创建别名交易表单", requestType = @TypeDescriptor(value = MultiSignAliasDto.class))
    })
    @ResponseData(name = "返回值", description = "返回一个Map对象", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "交易hash"),
            @Key(name = "txHex", description = "交易序列化16进制字符串")
    }))
    public RpcClientResult createMultiSignAliasTxOffLine(MultiSignAliasDto dto) {
        io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignAliasTxOffline(dto);
        return ResultUtil.getRpcClientResult(result);
    }
}
