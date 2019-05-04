/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package io.nuls.cmd.client.processor.account;

import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.account.facade.ImportAccountByKeyStoreReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;

import java.io.File;

/**
 * 根据keystore导出账户,
 * 密码用来验证(keystore), 如果keystore没有密码则可以不输
 * @author: Charlie
 */
@Component
public class ImportByKeyStoreProcessor extends AccountBaseProcessor implements CommandProcessor {

    @Override
    public String getCommand() {
        return "importkeystore";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<path> The path to the AccountKeystore file ");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "importkeystore <path> -- import accounts according to AccountKeystore files";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1);
        checkArgs(new File(args[1]).exists(),"keystore file not exists");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String path = args[1];
        String password = CommandHelper.getPwdOptional();
        String keystore = accountService.getAccountKeystoreDto(path);
        ImportAccountByKeyStoreReq req = new ImportAccountByKeyStoreReq(password, HexUtil.encode(keystore.getBytes()),false);
        Result<String> result = accountService.importAccountByKeyStore(req);
        if(result.isFailed()){
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result.getData());
    }

}
