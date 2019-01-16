/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.transaction;

import io.nuls.base.basic.AddressTool;
import io.nuls.base.data.*;
import io.nuls.rpc.client.CmdDispatcher;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.model.ModuleE;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.crypto.HexUtil;
import io.nuls.tools.log.Log;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.transaction.manager.ChainManager;
import io.nuls.transaction.model.bo.Chain;
import io.nuls.transaction.model.bo.config.ConfigBean;
import io.nuls.transaction.model.dto.CoinDTO;
import io.nuls.transaction.model.dto.CrossTxTransferDTO;
import io.nuls.transaction.rpc.call.LedgerCall;
import io.nuls.transaction.rpc.call.TransactionCall;
import io.nuls.transaction.service.TxService;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.validation.constraints.AssertTrue;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author: Charlie
 * @date: 2019-01-15
 */
public class TxFlowTest {

    static int chainId = 12345;
    static int assetChainId = 12345;
    static String address1 = "LU6eNP3pJ5UMn5yn8LeDE3Pxeapsq3930";
    static String address2 = "JcgbDRvBqQ67Uq4Tb52U22ieJdr3G3930";
    static String address3 = "Vxb3xxatcFFTZZe3wynX6CfAsvzAx3930";
    static String address4 = "R9CxmNqtBDEm9iWX2Cod46QGCNE2M3930";
    static int assetId = 1;
    //入账金额
    static BigInteger amount = BigInteger.valueOf(100000000000L);

    private TxService txService;
    private ChainManager chainManager;
    private Chain chain;
    private Transaction tx;
    private CoinData coinData;

    @Before
    public void before() throws Exception{
//        InitializerTest.init();
        NoUse.mockModule();
//        txService = SpringLiteContext.getBean(TxService.class);
//        chainManager = SpringLiteContext.getBean(ChainManager.class);
        //初始化token
//        addGenesisAsset();
//        chain = chainManager.getChain(chainId);
    }


    @Test
    public void newCtx() throws Exception{
        chain = new Chain();
        chain.setConfig(new ConfigBean(12345, 1));
        BigInteger balance = LedgerCall.getBalance(chain, AddressTool.getAddress(address2), assetChainId, assetId);
        System.out.println(balance.longValue());

        CrossTxTransferDTO ctxTransfer = new CrossTxTransferDTO(chain.getChainId(),
                createFromCoinDTOList(), createToCoinDTOList(), "this is cross-chain transaction");
        //调接口
        String json = JSONUtils.obj2json(ctxTransfer);

        Map<String, Object> params = JSONUtils.json2map(json);
//        Map map = (HashMap)TransactionCall.request("tx_createCtx", ModuleE.TX.abbr, params);
        Response response = CmdDispatcher.requestAndResponse(ModuleE.TX.abbr, "tx_createCtx", params);
        Assert.assertTrue(null != response.getResponseData());
        Map map = (HashMap)((HashMap) response.getResponseData()).get("tx_createCtx");
        Assert.assertTrue(null != map);
        Log.info("{}", map.get("value"));
        /*String hash = txService.createCrossTransaction(chain,
                createFromCoinDTOList(), createToCoinDTOList(), "this is cross-chain transaction");
        System.out.println(hash);*/

    }

    private List<CoinDTO> createFromCoinDTOList(){
        CoinDTO coinDTO = new CoinDTO();
        coinDTO.setAssetsId(assetId);
        coinDTO.setAssetsChainId(assetChainId);
        coinDTO.setAddress(address1);
        coinDTO.setAmount(new BigInteger("20000000000"));
        coinDTO.setPassword("nuls123456");

        CoinDTO coinDTO2 = new CoinDTO();
        coinDTO2.setAssetsId(assetId);
        coinDTO2.setAssetsChainId(assetChainId);
        coinDTO2.setAddress(address2);
        coinDTO2.setAmount(new BigInteger("10000000000"));
        coinDTO2.setPassword("nuls123456");
        List< CoinDTO > listFrom = new ArrayList<>();
        listFrom.add(coinDTO);
        listFrom.add(coinDTO2);
        return listFrom;
    }

    private List<CoinDTO> createToCoinDTOList(){
        CoinDTO coinDTO = new CoinDTO();
        coinDTO.setAssetsId(assetId);
        coinDTO.setAssetsChainId(8964);
        coinDTO.setAddress("VatuPuZeEc1YJ21iasZH6SMAD2VNL0423");
        coinDTO.setAmount(new BigInteger("20000000000"));

        CoinDTO coinDTO2 = new CoinDTO();
        coinDTO2.setAssetsId(assetId);
        coinDTO2.setAssetsChainId(8964);
        coinDTO2.setAddress("K7gb72AMXhymt8wBH3fwBUqSwf4EX0423");
        coinDTO2.setAmount(new BigInteger("10000000000"));
        List< CoinDTO > listTO = new ArrayList<>();
        listTO.add(coinDTO);
        listTO.add(coinDTO2);
        return listTO;
    }

    /**
     * 铸币交易
     * @return
     * @throws IOException
     */
    private static Transaction buildTransaction() throws IOException {
        //封装交易执行
        Transaction tx = new Transaction();
        CoinData coinData = new CoinData();
        CoinTo coinTo = new CoinTo();
        coinTo.setAddress(AddressTool.getAddress(address2));
        coinTo.setAmount(amount);
        coinTo.setAssetsChainId(assetChainId);
        coinTo.setAssetsId(assetId);
        coinTo.setLockTime(0);
        List<CoinFrom> coinFroms = new ArrayList<>();
        List<CoinTo> coinTos = new ArrayList<>();
        coinTos.add(coinTo);
        coinData.setFrom(coinFroms);
        coinData.setTo(coinTos);
        tx.setBlockHeight(1L);
        tx.setCoinData(coinData.serialize());
        tx.setHash(NulsDigestData.calcDigestData(tx.serializeForHash()));
        return tx;
    }

    /**
     * 铸币
     * @throws Exception
     */
    public static void addGenesisAsset() throws Exception {
        // Build params map
        Map<String, Object> params = new HashMap<>();
        params.put("chainId", chainId);
        Response response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "bathValidateBegin", params);
        Log.info("response {}", response);
        params.put("isBatchValidate", true);
        Transaction transaction = buildTransaction();
        params.put("txHex", HexUtil.encode(transaction.serialize()));
        response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "validateCoinData", params);
        Log.info("response {}", response);

        params.put("isConfirmTx",true);
        response = CmdDispatcher.requestAndResponse(ModuleE.LG.abbr, "commitTx", params);
        Log.info("response {}", response);
    }
}
