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
import io.nuls.provider.api.config.Context;
import io.nuls.provider.model.ErrorData;
import io.nuls.provider.model.RpcClientResult;
import io.nuls.provider.model.dto.AccountKeyStoreDto;
import io.nuls.provider.model.form.*;
import io.nuls.provider.model.jsonrpc.RpcResult;
import io.nuls.provider.rpctools.AccountTools;
import io.nuls.provider.utils.Log;
import io.nuls.provider.utils.ResultUtil;
import io.nuls.v2.error.AccountErrorCode;
import io.nuls.v2.model.annotation.Api;
import io.nuls.v2.model.annotation.ApiOperation;
import io.nuls.v2.model.dto.AccountDto;
import io.nuls.v2.model.dto.AliasDto;
import io.nuls.v2.model.dto.MultiSignAliasDto;
import io.nuls.v2.model.dto.SignDto;
import io.nuls.v2.util.NulsSDKTool;
import org.glassfish.jersey.media.multipart.FormDataParam;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
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
    private long time;


    @POST
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Batch Create Accounts", order = 101, detailDesc = "The created account exists in the local wallet")
    @Parameters({
            @Parameter(parameterName = "count", parameterDes = "Number of new accounts created,Value[1-10000]"),
            @Parameter(parameterName = "password", parameterDes = "Account password")
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "list", valueType = List.class, valueElement = String.class, description = "Account address")
    }))
    public RpcClientResult create(AccountCreateForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        if (form.getCount() <= 0 || form.getCount() > 10000) {
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
    @ApiOperation(description = "Change account password", order = 102)
    @Parameters({
            @Parameter(parameterName = "address", parameterDes = "Account address"),
            @Parameter(parameterName = "form", parameterDes = "Account Password Information Form", requestType = @TypeDescriptor(value = AccountUpdatePasswordForm.class))
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", valueType = Boolean.class, description = "Is the modification successful")
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
        if (Context.accessLimit && System.currentTimeMillis() - time < 3000L) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "Access frequency limit."));
        }
        time = System.currentTimeMillis();
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
    @ApiOperation(description = "Export account private key", order = 103, detailDesc = "Only the private key of an existing account in the local wallet can be exported")
    @Parameters({
            @Parameter(parameterName = "address", parameterDes = "Account address"),
            @Parameter(parameterName = "form", parameterDes = "Account Password Information Form", requestType = @TypeDescriptor(value = AccountPasswordForm.class))
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "Private key")
    }))
    public RpcClientResult getPriKey(@PathParam("address") String address, AccountPasswordForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        if (address == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "address is empty"));
        }
        if (Context.accessLimit && System.currentTimeMillis() - time < 3000L) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "Access frequency limit."));
        }
        time = System.currentTimeMillis();
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
    @ApiOperation(description = "Import account based on private key", order = 104, detailDesc = "When importing a private key, you need to enter a password to encrypt the plaintext private key")
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "Import account form based on private key", requestType = @TypeDescriptor(value = AccountPriKeyPasswordForm.class))
    })
    @ResponseData(name = "Return value", description = "Return account address", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "Account address")
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
    @ApiOperation(description = "according tokeyStoreImport account", order = 105)
    @Parameters({
            @Parameter(parameterName = "Import account based on private key", parameterDes = "Import account form based on private key", requestType = @TypeDescriptor(value = InputStream.class))
    })
    @ResponseData(name = "Return value", description = "Return account address", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "Account address")
    }))
    public RpcClientResult importAccountByKeystoreFile(@FormDataParam("keystore") InputStream in,
                                                       @FormDataParam("password") String password) {
        if (in == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "inputStream is empty"));
        }
        Result<AccountKeyStoreDto> dtoResult = this.getAccountKeyStoreDto(in);
        if (dtoResult.isFailed()) {
            return RpcClientResult.getFailed(new ErrorData(dtoResult.getStatus(), dtoResult.getMessage()));
        }
        AccountKeyStoreDto dto = dtoResult.getData();
        try {
            ImportAccountByKeyStoreReq req = new ImportAccountByKeyStoreReq(password, HexUtil.encode(JSONUtils.obj2json(dto).getBytes()), true);
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
    @ApiOperation(description = "according tokeystoreFile path import account", order = 106)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "according tokeystoreFile path import account form", requestType = @TypeDescriptor(value = AccountKeyStoreImportForm.class))
    })
    @ResponseData(name = "Return value", description = "Return account address", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "Account address")
    }))
    public RpcClientResult importAccountByKeystoreFilePath(AccountKeyStoreImportForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        String keystore = accountService.getAccountKeystoreDto(form.getPath());
        ImportAccountByKeyStoreReq req = new ImportAccountByKeyStoreReq(form.getPassword(), HexUtil.encode(keystore.getBytes()), true);
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
    @ApiOperation(description = "according tokeystoreString import account", order = 107)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "according tokeystoreString import account form", requestType = @TypeDescriptor(value = AccountKeyStoreJsonImportForm.class))
    })
    @ResponseData(name = "Return value", description = "Return account address", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "Account address")
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
        ImportAccountByKeyStoreReq req = new ImportAccountByKeyStoreReq(form.getPassword(), HexUtil.encode(keystore.getBytes()), true);
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
    @ApiOperation(description = "Account backup, exportAccountKeyStoreFile to specified directory", order = 108)
    @Parameters({
            @Parameter(parameterName = "address", parameterDes = "Account address", requestType = @TypeDescriptor(value = String.class)),
            @Parameter(parameterName = "form", parameterDes = "keystoneExport Information Form", requestType = @TypeDescriptor(value = AccountKeyStoreBackup.class))
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "path", description = "Exported file path")
    }))
    public RpcClientResult exportAccountKeyStore(@PathParam("address") String address, AccountKeyStoreBackup form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        if (address == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "address is empty"));
        }
        if (System.currentTimeMillis() - time < 3000L) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "Access frequency limit."));
        }
        time = System.currentTimeMillis();
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
    @ApiOperation(description = "Account setting alias", order = 109, detailDesc = "The alias format is1-20A combination of lowercase letters and numbers, setting an alias will destroy it1individualNULS")
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "Account alias setting form", requestType = @TypeDescriptor(value = SetAliasForm.class))
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "Setting up alias transactionshash")
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
    @Path("/address/validate")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Verify if the address format is correct", order = 110, detailDesc = "Verify if the address format is correct")
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "Account alias setting form", requestType = @TypeDescriptor(value = ValidateAddressForm.class))
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "true")
    }))
    public RpcClientResult validateAddress(ValidateAddressForm form) {
        boolean b = AddressTool.validAddress(form.getChainId(), form.getAddress());
        if (b) {
            Map map = new HashMap();
            map.put("value", true);
            return RpcClientResult.getSuccess(map);
        } else {
            return RpcClientResult.getFailed(new ErrorData(AccountErrorCode.ADDRESS_ERROR.getCode(), "address is wrong"));
        }
    }

    @POST
    @Path("/address/publickey")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Generate account address based on account public key", order = 111, detailDesc = "Generate account address based on account public key")
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "Generate account address based on account public key", requestType = @TypeDescriptor(value = AccountPublicKeyForm.class))
    })
    @ResponseData(name = "Return value", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "address", description = "Account address")
    }))
    public RpcClientResult getAddressByPublicKey(AccountPublicKeyForm form) {
        try {
            byte[] address = AddressTool.getAddress(HexUtil.decode(form.getPublicKey()), form.getChainId());
            return RpcClientResult.getSuccess(Map.of("address", AddressTool.getStringAddressByBytes(address)));
        } catch (Exception e) {
            Log.error(e);
            return RpcClientResult.getFailed(new ErrorData(AccountErrorCode.ADDRESS_ERROR.getCode(), "address is wrong"));
        }
    }

    @POST
    @Path("/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "off-line - Batch Create Accounts", order = 151, detailDesc = "The created account will not be saved to the wallet,The interface directly returns the account'skeystoreinformation")
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "Offline batch creation of account forms", requestType = @TypeDescriptor(value = AccountCreateForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "list", valueType = List.class, valueElement = AccountDto.class, description = "accountkeystorelist")
    }))
    public RpcClientResult createOffline(AccountCreateForm form) {
        if (form == null) {
            return RpcClientResult.getFailed(new ErrorData(CommonCodeConstanst.PARAMETER_ERROR.getCode(), "form is empty"));
        }
        io.nuls.core.basic.Result<List<AccountDto>> result;
        if (StringUtils.isBlank(form.getPrefix())) {
            result = NulsSDKTool.createOffLineAccount(form.getCount(), form.getPassword());
        } else {
            result = NulsSDKTool.createOffLineAccount(form.getChainId(), form.getCount(), form.getPrefix(), form.getPassword());
        }
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/priKey/offline")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Offline acquisition of account plaintext private key", order = 152)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "Offline access to account plaintext private key form", requestType = @TypeDescriptor(value = GetPriKeyForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "Clear text private key")
    }))
    public RpcClientResult getPriKeyOffline(GetPriKeyForm form) {
        io.nuls.core.basic.Result result = NulsSDKTool.getPriKeyOffline(form.getAddress(), form.getEncryptedPriKey(), form.getPassword());
        return ResultUtil.getRpcClientResult(result);
    }

    @PUT
    @Path("/password/offline/")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Offline account password modification", order = 153)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "Offline account password modification form", requestType = @TypeDescriptor(value = ResetPasswordForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "Encryption private key after resetting password")
    }))
    public RpcClientResult resetPasswordOffline(ResetPasswordForm form) {
        io.nuls.core.basic.Result result = NulsSDKTool.resetPasswordOffline(form.getAddress(), form.getEncryptedPriKey(), form.getOldPassword(), form.getNewPassword());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/multi/sign")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Multiple Account Summary Signature", order = 154, detailDesc = "For multi account transfer transactions used for offline assembly of signatures, when calling the interface, parameters can be passed to the address and private key, or to the address and encrypted private key and encrypted password")
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "Multiple Account Summary Signature Form", requestType = @TypeDescriptor(value = MultiSignForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Signed transaction16Hexadecimal Strings")
    }))
    public RpcClientResult multiSign(MultiSignForm form) {
        io.nuls.core.basic.Result result = NulsSDKTool.sign(form.getDtoList(), form.getTxHex());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/priKey/sign")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Clear text private key digest signature", order = 155)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "Clear text private key abstract signature form", requestType = @TypeDescriptor(value = PriKeySignForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Signed transaction16Hexadecimal Strings")
    }))
    public RpcClientResult priKeySign(PriKeySignForm form) {
        io.nuls.core.basic.Result result = NulsSDKTool.sign(form.getTxHex(), form.getAddress(), form.getPriKey());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/encryptedPriKey/sign")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Cryptography private key digest signature", order = 156)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "Cryptography private key digest signature form", requestType = @TypeDescriptor(value = EncryptedPriKeySignForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Signed transaction16Hexadecimal Strings")
    }))
    public RpcClientResult encryptedPriKeySign(EncryptedPriKeySignForm form) {
        io.nuls.core.basic.Result result = NulsSDKTool.sign(form.getTxHex(), form.getAddress(), form.getEncryptedPriKey(), form.getPassword());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/encryptedPriKeys/sign")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Multiple account ciphertext private key digest signature", order = 156)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "Cryptography private key digest signature form", requestType = @TypeDescriptor(value = EncryptedPriKeySignForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Signed transaction16Hexadecimal Strings")
    }))
    public RpcClientResult encryptedPriKeysSign(EncryptedPriKeysSignForm form) {
//        io.nuls.core.basic.Result result = NulsSDKTool.sign(form.getTxHex(), form.getAddress(), form.getEncryptedPriKey(), form.getPassword());
//        return ResultUtil.getRpcClientResult(result);
//        return null;
        io.nuls.core.basic.Result result = NulsSDKTool.sign(form.getChainId(), form.getPrefix(), form.getSignDtoList(), form.getTxHex());
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/multiSign/create")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Create a multi signature account", order = 157, detailDesc = "Create multiple signed accounts based on the public keys of multiple accounts,minSignsThe minimum number of signatures required to create transactions for multi signature accounts")
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "Create a multi signature account form", requestType = @TypeDescriptor(value = MultiSignAccountCreateForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMap", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "The address of the account")
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
    @ApiOperation(description = "Offline creation and setting of alias transactions", order = 158, detailDesc = "Create multiple signed accounts based on the public keys of multiple accounts,minSignsThe minimum number of signatures required to create transactions for multi signature accounts")
    @Parameters({
            @Parameter(parameterName = "dto", parameterDes = "Create a multi signature account form", requestType = @TypeDescriptor(value = AliasDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization16Hexadecimal Strings")
    }))
    public RpcClientResult createAliasTxOffLine(AliasDto dto) {
        io.nuls.core.basic.Result result = NulsSDKTool.createAliasTxOffline(dto);
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/multiSign/aliasTx/create")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Offline creation and setting of alias transactions for multiple signed accounts", order = 159)
    @Parameters({
            @Parameter(parameterName = "dto", parameterDes = "Create an alias transaction form", requestType = @TypeDescriptor(value = MultiSignAliasDto.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "hash", description = "transactionhash"),
            @Key(name = "txHex", description = "Transaction serialization16Hexadecimal Strings")
    }))
    public RpcClientResult createMultiSignAliasTxOffLine(MultiSignAliasDto dto) {
        io.nuls.core.basic.Result result = NulsSDKTool.createMultiSignAliasTxOffline(dto);
        return ResultUtil.getRpcClientResult(result);
    }

    @POST
    @Path("/address/priKey")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiOperation(description = "Obtain account address format based on private key", order = 160)
    @Parameters({
            @Parameter(parameterName = "form", parameterDes = "Private Key Form", requestType = @TypeDescriptor(value = PriKeyForm.class))
    })
    @ResponseData(name = "Return value", description = "Return aMapobject", responseType = @TypeDescriptor(value = Map.class, mapKeys = {
            @Key(name = "value", description = "Account address")
    }))
    public RpcClientResult getAddressByPriKey(PriKeyForm form) {
        io.nuls.core.basic.Result result = NulsSDKTool.getAddressByPriKey(form.getPriKey());
        return ResultUtil.getRpcClientResult(result);
    }
}
