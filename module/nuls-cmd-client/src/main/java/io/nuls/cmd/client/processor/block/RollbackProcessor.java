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

package io.nuls.cmd.client.processor.block;


import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.block.BlockService;
import io.nuls.base.api.provider.block.facade.BlockHeaderData;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHashReq;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByHeightReq;
import io.nuls.base.api.provider.block.facade.GetBlockHeaderByLastHeightReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.CommandGroup;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Component;

import java.util.regex.Matcher;

/**
 * @author: Charlie
 */
@Component
public class RollbackProcessor implements CommandProcessor {


    BlockService blockService = ServiceManager.get(BlockService.class);

    @Override
    public String getCommand() {
        return "rollback";
    }

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.Block;
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<count>   block count - Required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "rollback <count>  -- Roll back a number of blocks";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1);
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String hash = args[1];
        Matcher matcher = IS_NUMBERIC.matcher(args[1]);
        Long height = Long.parseLong(args[1]);
        if(height>1000){
            return CommandResult.getFailed("The count is too big");
        }
        Result<BlockHeaderData> result = blockService.rollback(new GetBlockHeaderByHeightReq(height));

        if(result.isFailed()){
            return CommandResult.getFailed(result);
        }
        return CommandResult.getSuccess(result);
    }
}
