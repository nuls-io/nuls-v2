package io.nuls.cmd.client.processor.contract;


import io.nuls.api.provider.Result;
import io.nuls.api.provider.contract.facade.GetContractConstructorArgsReq;
import io.nuls.cmd.client.CommandBuilder;
import io.nuls.cmd.client.CommandResult;
import io.nuls.cmd.client.processor.ErrorCodeConstants;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.model.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * get contract program constructors
 * Created by wangkun23 on 2018/9/25.
 */
@Component
public class GetContractConstructorProcessor extends ContractBaseProcessor {

    @Override
    public String getCommand() {
        return "getcontractcontructor";
    }

    @Override
    public String getHelp() {
        CommandBuilder builder = new CommandBuilder();
        builder.newLine(getCommandDescription())
                .newLine("\t<contractCode> contract code -required");
        return builder.toString();
    }

    @Override
    public String getCommandDescription() {
        return "getcontractcontructor <contractCode> --get contract contructor from smart contract program";
    }

    @Override
    public boolean argsValidate(String[] args) {
        int length = args.length;
        if (length != 2) {
            return false;
        }
        return true;
    }

    @Override
    public CommandResult execute(String[] args) {
        String code = args[1];
        if (StringUtils.isBlank(code)) {
            return CommandResult.getFailed(ErrorCodeConstants.PARAM_ERR.getMsg());
        }
        /**
         * assemble request body JSON
         */
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("contractCode", code);
        String url = "/contract/constructor";
        Result<Map> result = contractProvider.getContractConstructorArgs(new GetContractConstructorArgsReq(code));
        if (result.isFailed()) {
            return CommandResult.getFailed(result);
        }
        return CommandResult.getResult(result);
    }
}
