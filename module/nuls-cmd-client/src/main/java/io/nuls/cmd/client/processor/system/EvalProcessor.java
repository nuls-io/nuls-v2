package io.nuls.cmd.client.processor.system;

import io.nuls.cmd.client.CommandHandler;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.CommandGroup;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @Author: zhoulijun
 * @Time: 2020-06-01 15:37
 * @Description: Function Description
 */
@Component
public class EvalProcessor implements CommandProcessor {

    @Autowired
    CommandHandler commandHandler;

    @Override
    public String getCommand() {
        return "eval";
    }

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.System;
    }

    @Override
    public String getHelp() {
        return null;
    }

    @Override
    public String getCommandDescription() {
        return null;
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1);
        String command = args[1];
        checkArgs(commandHandler.hasCommand(command),"not found nuls command!");
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        List<String> argList = Arrays.asList(args);
        argList = argList.subList(1,argList.size());
        return commandHandler.processCommand(argList.toArray(new String[argList.size()]));
    }
}
