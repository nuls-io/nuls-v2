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

import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandConstant;
import io.nuls.cmd.client.CommandHandler;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.cmd.client.processor.CommandGroup;
import io.nuls.core.core.annotation.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author: Charlie
 */
@Component
public class HelpProcessor implements CommandProcessor {

    @Override
    public String getCommand() {
        return "help";
    }

    @Override
    public CommandGroup getGroup() {
        return CommandGroup.System;
    }

    @Override
    public String getHelp() {
        CommandBuilder bulider = new CommandBuilder();
        bulider.newLine("help [-a | group | command]")
                .newLine("\t[-a] show all commands and options of command - optional")
                .newLine("\t[group] show commands and options of this group. group list: " + Arrays.stream(CommandGroup.values()).map(g->g.getTitle().toLowerCase()).collect(Collectors.toList()))
                .newLine("\t[command] shwo this command info ");
        return bulider.toString();
    }

    @Override
    public String getCommandDescription() {
        return "help [-a  -- print all commands info | group -- print command info for this group | command -- print this command info ] ";
    }

    @Override
    public boolean argsValidate(String[] args) {
        checkArgsNumber(args,1);
        checkArgs(
                CommandConstant.NEED_ALL.equals(args[1]) || Arrays.stream(CommandGroup.values()).anyMatch(g->g.getTitle().equals(args[1]))
                        ||  CommandHandler.PROCESSOR_MAP.values().stream().anyMatch(p->p.getCommand().equals(args[1])),getCommandDescription());
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {

        StringBuilder str = new StringBuilder();
        if(args.length == 1){
            Arrays.stream(CommandGroup.values()).forEach(group->{
                printGroup(group,str,false);
            });
        }else{
            String cmd = args[1];
            if(CommandConstant.NEED_ALL.equals(cmd)){
                Arrays.stream(CommandGroup.values()).forEach(group->{
                    printGroup(group,str,true);
                });
            }else{
                Optional<CommandGroup> group = Arrays.stream(CommandGroup.values()).filter(g->g.getTitle().equals(cmd)).findFirst();
                if(group.isPresent()){
                    printGroup(group.get(),str,true);
                }else{
                    Optional<CommandProcessor> processor = CommandHandler.PROCESSOR_MAP.values().stream().filter(p->p.getCommand().equals(cmd)).findFirst();
                    if(processor.isPresent()){
                        n(str);
                        n(str);
                        str.append(processor.get().getHelp());
                    }else{
                        CommandResult.failed("error cmd");
                    }
                }
            }

        }

        return CommandResult.getSuccess(str.toString());
    }

    private StringBuilder printGroup(CommandGroup group, StringBuilder str, boolean printHelp){
        n(str);
        n(str);
        str.append("-------------------------------------------------- ");
        n(str);
        str.append("group : ").append(group.getTitle());
        n(str);
        str.append("-------------------------------------------------- ");
        n(str);
        CommandHandler.PROCESSOR_MAP.values().stream()
                .filter(p->group.equals(p.getGroup()))
                .forEach(p->{
                    n(str);
                    if(printHelp){
                        str.append(p.getHelp());
                    }else{
                        str.append(p.getCommandDescription());
                    }
                });
        n(str);
        return str;
    }

    private StringBuilder n(StringBuilder buf){
        return buf.append("\n");
    }

}
