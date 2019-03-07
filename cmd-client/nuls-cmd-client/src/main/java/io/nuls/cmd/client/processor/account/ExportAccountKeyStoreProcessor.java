//package io.nuls.cmd.client.processor.account;
//
//import io.nuls.base.basic.AddressTool;
//import io.nuls.cmd.client.CommandBuilder;
//import io.nuls.cmd.client.CommandHelper;
//import io.nuls.cmd.client.CommandResult;
//import io.nuls.cmd.client.Config;
//import io.nuls.cmd.client.processor.CommandProcessor;
//import io.nuls.tools.core.annotation.Autowired;
//import io.nuls.tools.core.annotation.Component;
//
//import java.util.HashMap;
//import java.util.Map;
//
///**
// * @Author: zhoulijun
// * @Time: 2019-03-07 16:54
// * @Description: 导出账户到keystore文件中
// */
//@Component
//public class ExportAccountKeyStoreProcessor implements CommandProcessor {
//
//    @Autowired
//    Config config;
//}
//
//    @Override
//    public String getCommand() {
//        return "backup";
//    }
//
//    @Override
//    public String getHelp() {
//        CommandBuilder builder = new CommandBuilder();
//        builder.newLine(getCommandDescription())
//                .newLine("\t<address> the account you want to back up - Required")
//                .newLine("\t[path] The folder of the export file, defaults to the current directory");
//        return builder.toString();
//    }
//
//    @Override
//    public String getCommandDescription() {
//        return "backup <address> [path] --backup the account key store";
//    }
//
//    @Override
//    public boolean argsValidate(String[] args) {
//        int length = args.length;
//        if (length < 2 || length > 3) {
//            return false;
//        }
//        if (!CommandHelper.checkArgsIsNull(args)) {
//            return false;
//        }
//        if (!AddressTool.validAddress(config.getChainId(),args[1])) {
//            return false;
//        }
//        return true;
//    }
//
//    @Override
//    public CommandResult execute(String[] args) {
//        String address = args[1];
//        String path = args.length == 3 ? args[2] : "";
//        String password = CommandHelper.getPwd(null);
//        Map<String, Object> parameters = new HashMap<>();
//        parameters.put("password", password);
//        parameters.put("path", path);
//        RpcClientResult result = restFul.post("/account/export/" + address, parameters);
//        if (result.isFailed()) {
//            return CommandResult.getFailed(result);
//        }
//        return CommandResult.getSuccess("The path to the backup file is " + (String)(CommandResult.dataTransformValue(result).getData()));
//    }
