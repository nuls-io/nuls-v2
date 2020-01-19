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

package io.nuls.cmd.client.processor.system;

import io.nuls.base.api.provider.BaseReq;
import io.nuls.base.api.provider.Result;
import io.nuls.base.api.provider.ServiceManager;
import io.nuls.base.api.provider.protocol.ProtocolProvider;
import io.nuls.base.api.provider.protocol.facade.GetVersionReq;
import io.nuls.base.api.provider.protocol.facade.VersionInfo;
import io.nuls.base.api.provider.transaction.TransferService;
import io.nuls.base.basic.ProtocolVersion;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandConstant;
import io.nuls.cmd.client.CommandHandler;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.config.Config;
import io.nuls.cmd.client.processor.CommandGroup;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.parse.MapUtils;
import io.nuls.core.rpc.model.ModuleE;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.core.rpc.netty.processor.ResponseMessageProcessor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: Charlie
 */
@Component

public class VersionProcessor implements CommandProcessor {

    ProtocolProvider transferService = ServiceManager.get(ProtocolProvider.class);

    @Autowired
    Config config;

    @Override
    public String getCommand() {
        return "version";
    }

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.System;
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine("version -- print node version info");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "version";
    }

    @Override
    public boolean argsValidate(String[] args) {
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        Result<VersionInfo> res = transferService.getVersion(new GetVersionReq());
        if(config.getClientVersion() != null){
            Map<String,Object> m = MapUtils.beanToLinkedMap(res.getData());
            m.put("clientVersion",config.getClientVersion());
            return CommandResult.getSuccess(new Result(m));
        }else{
            return CommandResult.getSuccess(res);
        }
    }

}
