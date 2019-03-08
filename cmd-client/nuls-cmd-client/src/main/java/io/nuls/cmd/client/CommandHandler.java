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
import io.nuls.cmd.client.processor.system.ExitProcessor;
import io.nuls.cmd.client.processor.system.HelpProcessor;
import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.parse.I18nUtils;
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
import static io.nuls.tools.core.ioc.SpringLiteContext.getBean;

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
//         * ledger
//         */
//        register(new GetTxProcessor());
//
//        /**
//         * block
//         */
//        register(new GetBlockHeaderProcessor());
//        register(new GetBlockProcessor());
//        register(new GetBestBlockHeaderProcessor());
//
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
//        register(new GetAccountProcessor());
//        register(new GetAccountsProcessor());
////        register(new GetAssetProcessor());//
//        register(new GetBalanceProcessor());
////        register(new GetWalletBalanceProcessor());//
//        register(new GetPrivateKeyProcessor());
//        register(new ImportByKeyStoreProcessor());
//        register(new ImportByPrivateKeyProcessor());
//        register(new ImportForcedByPrivateKeyProcessor());
//        register(new RemoveAccountProcessor());
//        register(new ResetPasswordProcessor());
//        register(new SetAliasProcessor());
//        register(new SetPasswordProcessor());
//
//        /**
//         * Multi-signature account
//         */
//        register(new CreateMultiSigAccountProcessor());
//        register(new ImportMultiSigAccountProcessor());
//        register(new GetMultiSigAccountListProcessor());
//        register(new GetMultiSigAccountProcessor());
//        register(new RemoveMultiSigAccountProcessor());
//        register(new GetMultiSigAccountCountProcessor());
//        register(new CreateMultiSigAccountProcessor());
//
//        register(new CreateMultiTransferProcess());
//        register(new SignMultiTransactionProcess());
//        register(new CreateMultiAliasProcess());
//        register(new CreateMultiAgentProcessor());
//        register(new CreateMultiDepositProcessor());
//        register(new CreateMultiWithdrawProcessor());
//        register(new CreateMultiStopAgentProcessor());
//
//        /**
//         * accountLedger
//         */
//        register(new TransferProcessor());
//        register(new GetAccountTxListProcessor());
////        register(new GetUTXOProcessor());//
//
//        /**
//         * consensus
//         */
//        register(new CreateAgentProcessor());
//        register(new GetConsensusProcessor());
//        register(new DepositProcessor());
//        register(new WithdrawProcessor());
//        register(new StopAgentProcessor());
//        register(new GetAgentProcessor());
//        register(new GetAgentsProcessor());
//        register(new GetDepositedAgentsProcessor());
//        register(new GetDepositedsProcessor());
//        register(new GetDepositedInfoProcessor());
//
//        /**
//         * network
//         */
//        register(new GetNetInfoProcessor());
//        register(new GetNetNodesProcessor());
//
        /**
         * system
         */
        register(SpringLiteContext.getBean(ExitProcessor.class));
        register(SpringLiteContext.getBean(HelpProcessor.class));
//        register(new VersionProcessor());
//        register(new UpgradeProcessor());
//        /**
//         * utxoAccounts
//         */
//        register(new GetUtxoAccountsProcessor());
//
//        /**
//         * contract
//         */
//        register(new GetContractTxProcessor());
//        register(new GetContractResultProcessor());
//        register(new GetContractInfoProcessor());
//        register(new GetContractBalanceProcessor());
//        register(new GetContractTxListProcessor());
//        register(new GetContractAddressValidProcessor());
//        register(new GetWalletContractsProcessor());
//        register(new GetTokenBalanceProcessor());
//        register(new CreateContractProcessor());
//        register(new CallContractProcessor());
//        register(new ViewContractProcessor());
//        register(new TransferToContractProcessor());
//        register(new TokenTransferProcessor());
//        register(new DeleteContractProcessor());
//        register(new GetContractConstructorProcessor());
//        JSONUtils.getInstance().configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
//        sdkInit();
    }

//    private void sdkInit() {
//        String port = null;
//        try {
//            NulsConfig.NULS_CONFIG = ConfigLoader.loadIni(NulsConstant.USER_CONFIG_FILE);
//
//            String mode = NulsConfig.NULS_CONFIG.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, "mode", "main");
//            if ("main".equals(mode)) {
//                NulsConfig.MODULES_CONFIG = ConfigLoader.loadIni(NulsConstant.MODULES_CONFIG_FILE);
//            } else {
//                NulsConfig.MODULES_CONFIG = ConfigLoader.loadIni(mode + "/" + NulsConstant.MODULES_CONFIG_FILE);
//            }
//            port = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, RpcConstant.CFG_RPC_SERVER_PORT);
//            String chainId = NulsConfig.NULS_CONFIG.getCfgValue(NulsConstant.CFG_SYSTEM_SECTION, NulsConstant.CFG_SYSTEM_DEFAULT_CHAIN_ID, "8964");
//            NulsContext.getInstance().setDefaultChainId(Short.parseShort(chainId));
//        } catch (Exception e) {
//            Log.error("CommandHandler start failed", e);
//            throw new NulsRuntimeException(KernelErrorCode.FAILED);
//        }
//        if (StringUtils.isBlank(port)) {
//            RestFulUtils.getInstance().setServerUri("http://" + RpcConstant.DEFAULT_IP + ":" + RpcConstant.DEFAULT_PORT + RpcConstant.PREFIX);
//        } else {
//            String ip = null;
//            try {
//                ip = NulsConfig.MODULES_CONFIG.getCfgValue(RpcConstant.CFG_RPC_SECTION, "server.ip").trim();
//                if ("0.0.0.0".equals(ip)) {
//                    ip = RpcConstant.DEFAULT_IP;
//                }
//            } catch (Exception e) {
//                ip = RpcConstant.DEFAULT_IP;
//            }
//            RestFulUtils.getInstance().setServerUri("http://" + ip + ":" + port + RpcConstant.PREFIX);
//        }
//    }

    public void start() {
        /**
         * 如果操作系统是windows, 可能会使控制台读取部分处于死循环，可以设置为false，绕过本地Windows API，直接使用Java IO流输出
         * If the operating system is windows, it may cause the console to read part of the loop, can be set to false,
         * bypass the native Windows API, use the Java IO stream output directly
         */
        if (System.getProperties().getProperty("os.name").toUpperCase().indexOf("WINDOWS") != -1) {
            System.setProperty("jline.WindowsTerminal.directConsole", "false");
        }
        try {
            I18nUtils.setLanguage("en");
        } catch (NulsException e) {
            e.printStackTrace();
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
        if(StringUtils.isBlank(line)) {
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
        for(int i = 0, length = args.length; i < length; i++) {
            args[i] = URLDecoder.decode(args[i], StandardCharsets.UTF_8.toString());
        }
        return args;
    }

    private String processCommand(String[] args) {
        int length = args.length;
        if (length == 0) {
            return CommandConstant.COMMAND_ERROR;
        }
        String command = args[0];
        CommandProcessor processor = PROCESSOR_MAP.get(command);
        if (processor == null) {
            return command + " not a nuls command!";
        }
        if (length == 2 && CommandConstant.NEED_HELP.equals(args[1])) {
            return processor.getHelp();
        }
        try {
            boolean result = processor.argsValidate(args);
            if (!result) {
                return "args incorrect:\n" + processor.getHelp();
            }
            return processor.execute(args).toString();
        } catch (Exception e) {
            return CommandConstant.EXCEPTION + ": " + e.getMessage();
        }
    }

    private void register(CommandProcessor processor) {
        PROCESSOR_MAP.put(processor.getCommand(), processor);
    }

}
