package io.nuls.chain.cmd;

import io.nuls.chain.model.AddressType;
import io.nuls.chain.model.Chain;
import io.nuls.chain.service.ChainService;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.CmdResponse;
import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;

import java.util.List;

/**
 * @author tangyi
 * @date 2018/11/7
 * @description
 */
@Component
public class ChainCmd extends BaseCmd {

    @Autowired
    private ChainService chainService;

    @CmdAnnotation(cmd = "chainInfo", version = 1.0, preCompatible = true)
    public CmdResponse chainInfo(List params) {
        try {
            if (params == null || params.get(0) == null) {
                return failed(ErrorCode.init("-100"), 1.0, "Need <chain id>");
            }

            Chain chain = chainService.chainInfo((String) params.get(0));
            return success(1.0, "success", chain);
        } catch (Exception e) {
            e.printStackTrace();
            return failed(ErrorCode.init("-100"), 1.0, e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "chainRegister", version = 1.0, preCompatible = true)
    public CmdResponse chainRegister(List params) {
        try {

            Chain chain = new Chain();
            chain.setChainId((Integer) params.get(0));
            chain.setName((String) params.get(1));
            chain.setMagicNumber((Integer) params.get(2));
            chain.setAddressType(Enum.valueOf(AddressType.class, (String) params.get(3)));

            System.out.println(chainService.chainRegister(chain));

            return success(1.0, "success", chain);
        } catch (Exception e) {
            //e.printStackTrace();
            return failed(ErrorCode.init("-100"), 1.0, e.getMessage());
        }
    }
}
