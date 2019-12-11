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

package io.nuls.cmd.client;


import io.nuls.cmd.client.processor.CommandProcessor;
import io.nuls.cmd.client.processor.account.*;
import io.nuls.cmd.client.processor.block.GetBestBlockHeaderProcessor;
import io.nuls.cmd.client.processor.block.GetBlockHeaderProcessor;
import io.nuls.cmd.client.processor.consensus.*;
import io.nuls.cmd.client.processor.contract.*;
import io.nuls.cmd.client.processor.crosschain.*;
import io.nuls.cmd.client.processor.ledger.GetBalanceProcessor;
import io.nuls.cmd.client.processor.ledger.RegisterLocalAssetProcessor;
import io.nuls.cmd.client.processor.network.GetNetworkProcessor;
import io.nuls.cmd.client.processor.system.ExitProcessor;
import io.nuls.cmd.client.processor.system.HelpProcessor;
import io.nuls.cmd.client.processor.transaction.*;
import io.nuls.core.basic.InitializingBean;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.core.ioc.SpringLiteContext;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import jline.console.ConsoleReader;
import jline.console.completer.ArgumentCompleter;
import jline.console.completer.Completer;
import jline.console.completer.StringsCompleter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.nuls.core.core.ioc.SpringLiteContext.getBean;

@Component
public class CommandHandler implements InitializingBean {

    public static final Map<String, CommandProcessor> PROCESSOR_MAP = new TreeMap<>();

    public static ConsoleReader CONSOLE_READER;

    private static final Pattern CMD_PATTERN = Pattern.compile("\"[^\"]+\"|'[^']+'");

    public CommandHandler() {

    }

    /**
     * 初始化加载所有命令行实现
     */
    @Override
    public void afterPropertiesSet() throws NulsException {
        /**
         * account
         */
        //create account
        register(getBean(CreateProcessor.class));
        //backup account to key store
        register(getBean(BackupAccountProcessor.class));
        //import account for key store
        register(getBean(ImportByKeyStoreProcessor.class));
        //import account for private key
        register(getBean(ImportByPrivateKeyProcessor.class));
        //update account password
        register(getBean(UpdatePasswordProcessor.class));
        //get account info
        register(getBean(GetAccountProcessor.class));
        //get all account list
        register(getBean(GetAccountsProcessor.class));
        //remove account by address
        register(getBean(RemoveAccountProcessor.class));
        //get private key by address
        register(getBean(GetPrivateKeyProcessor.class));
        //set account alias
        register(getBean(SetAliasProcessor.class));
        //transfer
        register(getBean(TransferProcessor.class));
        //transfer by alias
//        register(getBean(TransferByAliasProcessor.class));

        //get last height block header
        register(getBean(GetBestBlockHeaderProcessor.class));
        //get block header by hash or height
        register(getBean(GetBlockHeaderProcessor.class));

        //get tx by hash
        register(getBean(GetTxProcessor.class));

        //get account balance
        register(getBean(GetBalanceProcessor.class));
        //local asset register
        register(getBean(RegisterLocalAssetProcessor.class));

        /**
         * consensus
         */
        //create consensus node
        register(getBean(CreateAgentProcessor.class));
        //stop consensus node
        register(getBean(StopAgentProcessor.class));

        //deposit
        register(getBean(DepositProcessor.class));
        //withdraw
        register(getBean(WithdrawProcessor.class));
        register(getBean(GetAgentsProcessor.class));
        register(getBean(GetAgentInfoProcessor.class));
        /**
         * system
         */
        register(SpringLiteContext.getBean(ExitProcessor.class));
        register(SpringLiteContext.getBean(HelpProcessor.class));

        register(getBean(GetNetworkProcessor.class));

        /**
         * multi sign transfer
         */
        register(getBean(CreateMultiAccountProcessor.class));
        register(getBean(RemoveMultiSignAccountProcessor.class));
        register(getBean(CreateMultiSignTransferProcessor.class));
        register(getBean(CreateMultiSignTransferAndSignProcessor.class));
        register(getBean(SignMultiSingTransferProcessor.class));
        register(getBean(SetMultiSignAliasProcessor.class));
        register(getBean(GetMultiSignAccountProcessor.class));
        register(getBean(CreateMultiSignAgentProcessor.class));
        register(getBean(StopMultiSignAgentProcessor.class));
        register(getBean(DepositForMultiSignProcessor.class));
        register(getBean(WithdrawForMultiSignProcessor.class));

        register(getBean(CreateContractProcessor.class));
        register(getBean(CallContractProcessor.class));
        register(getBean(DeleteContractProcessor.class));
        register(getBean(GetContractConstructorProcessor.class));
        register(getBean(GetContractInfoProcessor.class));
        register(getBean(GetContractResultProcessor.class));
        register(getBean(GetContractTxProcessor.class));
        register(getBean(TokenTransferProcessor.class));
        register(getBean(TransferToContractProcessor.class));
        register(getBean(ViewContractProcessor.class));
        register(getBean(GetAccountContractListProcessor.class));


        register(getBean(RegisterCrossChainProcessor.class));
        register(getBean(CrossAssetAddProcessor.class));
        register(getBean(CrossAssetDisableProcessor.class));
        register(getBean(UpdateCrossChainProcessor.class));
        register(getBean(CrossLocalAssetAddProcessor.class));

        register(getBean(CreateCrossTxProcessor.class));
        register(getBean(GetCrossChainsSimpleInfoProcessor.class));
        register(getBean(GetCrossChainRegisterInfoProcessor.class));
        register(getBean(GetCrossAssetInfoProcessor.class));
        register(getBean(GetCrossTxStateProcessor.class));
    }

    public void start() {
//        try {
//            ServerSocket serverSocket = new ServerSocket(1122,1);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//       System.out.println("服务器启动1111!");
        /**
         * 如果操作系统是windows, 可能会使控制台读取部分处于死循环，可以设置为false，绕过本地Windows API，直接使用Java IO流输出
         * If the operating system is windows, it may cause the console to read part of the loop, can be set to false,
         * bypass the native Windows API, use the Java IO stream output directly
         */
        if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1) {
            System.setProperty("jline.WindowsTerminal.directConsole", "false");
        }
        try {
            CONSOLE_READER = new ConsoleReader();
            List<Completer> completers = new ArrayList<Completer>();
            completers.add(new StringsCompleter(PROCESSOR_MAP.keySet()));
            CONSOLE_READER.addCompleter(new ArgumentCompleter(completers));
            String line;
            do {
                line = CONSOLE_READER.readLine(CommandConstant.COMMAND_PS1);
                if (StringUtils.isBlank(line)) {
                    continue;
                }
                String[] cmdArgs = parseArgs(line);
                System.out.print(this.processCommand(cmdArgs) + "\n");
            } while (line != null);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (!CONSOLE_READER.delete()) {
                    CONSOLE_READER.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private static String[] parseArgs(String line) throws UnsupportedEncodingException {
        if (StringUtils.isBlank(line)) {
            return new String[0];
        }
        Matcher matcher = CMD_PATTERN.matcher(line);
        String result = line;
        while (matcher.find()) {
            String group = matcher.group();
            String subGroup = group.substring(1, group.length() - 1);
            String encoder = URLEncoder.encode(subGroup, StandardCharsets.UTF_8.toString());
            result = result.replace(group, encoder);
        }

        String[] args = result.split("\\s+");
        for (int i = 0, length = args.length; i < length; i++) {
            args[i] = URLDecoder.decode(args[i], StandardCharsets.UTF_8.toString());
        }
        return args;
    }

    private String processCommand(String[] args) {
        int length = args.length;
        if (length == 0) {
            return CommandConstant.COMMAND_ERROR;
        }
        String command = args[0].trim();
        CommandProcessor processor = PROCESSOR_MAP.get(command);
        if (processor == null) {
            return command + " not a nuls command!";
        }
        if (length == 2 && CommandConstant.NEED_HELP.equals(args[1])) {
            return processor.getHelp();
        }
        try {
            try {
                boolean result = processor.argsValidate(args);
                if (!result) {
                    return "args incorrect:\n" + processor.getHelp();
                }
            } catch (ParameterException e) {
                return e.getMessage() + "\n" + "args incorrect:\n" + processor.getHelp();
            }
            return processor.execute(args).toString();
        } catch (Exception e) {
            if (System.Logger.Level.DEBUG.getName().equals(System.getProperty("log.level"))) {
                e.printStackTrace();
            }
            return CommandConstant.EXCEPTION + ": " + e.getMessage();
        }
    }

    private void register(CommandProcessor processor) {
        PROCESSOR_MAP.put(processor.getCommand(), processor);
    }

}
