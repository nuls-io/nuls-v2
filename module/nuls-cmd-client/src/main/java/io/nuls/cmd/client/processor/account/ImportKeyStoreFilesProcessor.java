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
import io.nuls.base.api.provider.account.facade.ImportKeyStoreFilesReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandHelper;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.crypto.HexUtil;

import java.io.File;

/**
 * according tokeystoreBatch import of accounts,
 *
 * @author: Charlie
 */
@Component
public class ImportKeyStoreFilesProcessor extends AccountBaseProcessor implements CommandProcessor {

    @Override
    public String getCommand() {
        return "importkeystorefiles";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<dirPath> The path to the AccountKeystore file ");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "importkeystorefiles <dirPath> -- import accounts according to AccountKeystore files";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args, 1);
        checkArgs(new File(args[1]).exists(), "keystore dir not exists");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String path = args[1];
        ImportKeyStoreFilesReq req = new ImportKeyStoreFilesReq();
        req.setDirPath(path);
        Result<String> result = accountService.importKeyStoreFiles(req);
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result.getData());
    }

}
