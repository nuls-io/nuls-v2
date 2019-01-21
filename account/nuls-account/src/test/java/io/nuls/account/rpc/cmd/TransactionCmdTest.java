package io.nuls.account.rpc.cmd;

import io.nuls.account.constant.RpcConstant;
import io.nuls.account.model.dto.CoinDto;
import io.nuls.account.model.dto.TransferDto;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.parse.JSONUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * @author: qinyifeng
 * @description:
 * @date: 2018/11/2
 */
public class TransactionCmdTest {

    //protected static AccountService accountService;

    protected int chainId = 12345;
    protected String password = "nuls123456";
    protected String newPassword = "c12345678";
    protected String version = "1.0";
    protected String success = "1";

    @BeforeClass
    public static void start() throws Exception {
        NoUse.mockModule();
    }

    @Test
    public void transfer() throws Exception {
        TransferDto transferDto = new TransferDto();
        transferDto.setChainId(chainId);
        transferDto.setRemark("transfer test");
        List<CoinDto> inputs=new ArrayList<>();
        List<CoinDto> outputs=new ArrayList<>();
        CoinDto inputCoin1=new CoinDto();
        inputCoin1.setAddress("LU6eNP3pJ5UMn5yn8LeDE3Pxeapsq3930");
        inputCoin1.setPassword(password);
        inputCoin1.setAssetsChainId(chainId);
        inputCoin1.setAssetsId(1);
        inputCoin1.setAmount(new BigInteger("10000000"));
        inputs.add(inputCoin1);

        CoinDto outputCoin1=new CoinDto();
        outputCoin1.setAddress("JcgbDRvBqQ67Uq4Tb52U22ieJdr3G3930");
        outputCoin1.setPassword(password);
        outputCoin1.setAssetsChainId(chainId);
        outputCoin1.setAssetsId(1);
        outputCoin1.setAmount(new BigInteger("10000000"));
        outputs.add(outputCoin1);

        transferDto.setInputs(inputs);
        transferDto.setOutputs(outputs);
        Response cmdResp = CmdDispatcher.requestAndResponse(ModuleE.AC.abbr, "ac_transfer", JSONUtils.json2map(JSONUtils.obj2json(transferDto)));
        HashMap result = (HashMap) (((HashMap) cmdResp.getResponseData()).get("ac_transfer"));
        String txDigestHex = (String) result.get(RpcConstant.VALUE);
        System.out.println(txDigestHex);
    }

}
