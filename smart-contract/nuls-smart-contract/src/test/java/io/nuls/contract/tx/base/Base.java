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
package io.nuls.contract.tx.base;


import io.nuls.base.basic.AddressTool;
import io.nuls.contract.model.bo.Chain;
import io.nuls.contract.model.bo.config.ConfigBean;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.model.txdata.ContractData;
import io.nuls.contract.util.ContractUtil;
import io.nuls.rpc.info.HostInfo;
import io.nuls.rpc.info.NoUse;
import io.nuls.rpc.netty.processor.ResponseMessageProcessor;
import org.junit.Before;

import java.math.BigInteger;

/**
 * @author: PierreLuo
 * @date: 2018/12/4
 */
public class Base {

    @Before
    public void before() throws Exception {
        NoUse.mockModule();
        ResponseMessageProcessor.syncKernel("ws://" + HostInfo.getLocalIP() + ":8887/ws");
        chain = new Chain();
        chain.setConfig(new ConfigBean(chainId, assetId, 100000000L));
    }

    protected Chain chain;
    protected static int chainId = 2;
    protected static int assetId = 1;

    protected static String password = "nuls123456";//"nuls123456";

    protected String sender = "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG";
    protected String toAddress = "tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD";
    protected String toAddress1 = "tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24";
    protected String toAddress2 = "tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD";
    protected String toAddress3 = "tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL";
    protected String toAddress4 = "tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL";

    protected String hash = "0020393c317981937950c7a1181021fb5d917efb79c9c03a2f077f2d2760f73e9593";
    protected String contractAddress = "tNULSeBaN71xpUTKtLyHSsTfB11fNGtA7W2RLS";

    protected String callHash = "00207987a58e710d30974e88451840253bf75088940b4efa8de6f366e3568fd9753a";








    static String[] contractAddressSeeds = {
            "5MR_3PyGq4CgbfoH7sCZ5PoAXPCrzKokBTF",
            "5MR_3QANzLfMffP22BbyaBnQddkJQArJtJ7",
            "5MR_3Q7e7a57oWhKLkgjRWYhJCyHbLS8ik9",
            "5MR_3QAgjbDEjZyzvAoH5NK642m9CZx6NGy",
            "5MR_3QAbdkWLSbQi5xthL8WtyFAvd4cKeea",
            "5MR_3Q3E2ByDXf5cwX1abCi4Xb39Kyq6R7q",
            "5MR_3QDNQCfjP6qM9ZGZDnVy2NzudSWLAW6",
            "5MR_3Q9S65XtxXeJUmEiPENSSdXLaZSNpbS",
            "5MR_3Q2wnpwjHKaG9M9rGfyk2YrwThMb57R",
            "5MR_3QBpHo2nLXMXfysmswjCkfb8PJFVRT8",
            "5MR_3Q1qQYgNw2URT6Q1G3HMAuxtsWdLrw1",
            "5MR_3Q79uhPjevBdVMxazc9ncXHXkjiSC3Q",
            "5MR_3Py5YMtECe4sKuEQrfpEcedVmUXTaBD",
            "5MR_3Q4T7MqP8uEPKcuNckurf7KCA7yK78V",
            "5MR_3QD53q4YiszcZqABwoGSuz339MdRsKX",
            "5MR_3Q2LBUdaZLDFZAHEbtxoC9razeUFa3T",
            "5MR_3QDNNscDF6RWvkgTrgqMBSZZoz2RGCh",
            "5MR_3Q6rhNo7FmG94b6dvwDu9SAxQsCj2TX",
            "5MR_3QDkFRo3tkGagATk8NKkQfeB3NgVoN7",
            "5MR_3QD9vHcAxDETYevRiSWLPUnYPDWGoKY",
            "5MR_3QE6m4naDtbQdmJLvf6Vv4cH2q2Zn7V",
            "5MR_3Q4LrgUoSveo3VShU5iLCJinTdhdc2w",
            "5MR_3Q5Q9bUQMNh46wrd3WjCtFjy8ub8f31",
            "5MR_3Q5SzKAqZfMuQH8itmrPayKjMMpoj2a",
            "5MR_3PzxjVLqrDAfgxV9dp6j8ZfeccN4ugB",
            "5MR_3QBoJr2Up7xtEv584W8qVo3G5YLREyG",
            "5MR_3Q8qdssMypCBTxDMt2h4S7x5xmRhgvL",
            "5MR_3PzCafNm6w8GoEd3tF5KtuNRYQENEVf",
            "5MR_3QBoW2rFRGztpn3ZYhwaPLVB5tvSG5N",
            "5MR_3PyvLiZwRKxbE5oPjwFuCGobXkUq5Jy",
            "5MR_3PymLmXrzSRmsopyjUQSjPT8ddBX5pc",
            "5MR_3Q33fMwJeCXq2PfuYapjQbAz3cDiJGr",
            "5MR_3Q5Kq6i8rfubjoktiQCKp7pUB4SzrD4",
            "5MR_3Q2yPBHjCEy3ukix93AVmYNpWnLjkZA",
            "5MR_3PyaMBWWj2EvAreGnLZXM6Fkfu986NR",
            "5MR_3PyBHEuQKeAs4bJpajzSXz6k9VNYP2w",
            "5MR_3QCRpbhmx9Rrw6WVrjC7ifbtqkUNQx4",
            "5MR_3QAtLPVLPeqdmYSbwGwFi1HAhnnDKbH",
            "5MR_3Q1UA633p7n8eo8VTjaopSc5zfPrpbP",
            "5MR_3Q39PbCdFwcrP5Wg27MbZKB4JFXnpYK",
            "5MR_3Q643rgT962iQMoXrn6Tw9B7anLMm5z",
            "5MR_3QBaFvnHN9FtGDekFeWNgG7W9GnphJt",
            "5MR_3QBGBwPecpjz5x55SXXCTzND9kUSeM6",
            "5MR_3Q5u1tiyBNXcR8mCFy7M6Nf3KmkGBMq",
            "5MR_3Q4EG6vgda7w1UNk8XC1VsNk87JL9Tm",
            "5MR_3Q4nEPa8RQCToidsJH9M2vTektoacSu",
            "5MR_3QBeEUcF7uNhC4CerF2EpXbAw4Uym6s",
            "5MR_3QBcfXELZibdvemKdPLdFxFBLxQmbC2",
            "5MR_3Q2w3eD877yKWp4g9YX7zKhKJSsLE7e",
            "5MR_3PyVCTAWc35RMqf7j7yBUVsAUdfYUC2"
    };

    static String[] senderSeeds = {
            "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
            "5MR_2CbdqKcZktcxntG14VuQDy8YHhc6ZqW",
            "5MR_2Cj9tfgQpdeF7nDy5wyaGG6MZ35H3rA",
            "5MR_2CWKhFuoGVraaxL5FYY3RsQLjLDN7jw",
            "5MR_2CgwCFRoJ8KX37xNqjjR7ttYuJsg8rk",
            "5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo",
            "tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG",
            "5MR_2CVCFWH7o8AmrTBPLkdg2dYH1UCUJiM",
            "5MR_2CfUsasd33vQV3HqGw6M3JwVsuVxJ7r",
            "5MR_2CVuGjQ3CYVkhFszxfSt6sodg1gDHYF"};

    static String[] methodNameSeeds = {
            "transfer",
            "approve",
            "increaseApproval",
            "decreaseApproval"
    };

    static String[][] methodArgsSeeds = {
            {"tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "10000"},
            {"tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "10000"},
            {"tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "10000"},
            {"tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG", "10000"}
    };

    private ContractData makeCallContractData(String sender, String contractAddress, long value, String methodName, Object[] args) {
        CallContractData txData = new CallContractData();
        txData.setSender(AddressTool.getAddress(sender));
        txData.setContractAddress(AddressTool.getAddress(contractAddress));
        txData.setValue(BigInteger.valueOf(value));
        txData.setGasLimit(200000L);
        txData.setPrice(25L);
        txData.setMethodName(methodName);
        String[][] args2 = ContractUtil.twoDimensionalArray(args);
        txData.setArgsCount((byte) args.length);
        txData.setArgs(args2);
        return txData;
    }

}
