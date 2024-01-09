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
package io.nuls.crosschain;

import io.nuls.base.data.NulsHash;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.log.Log;
import io.nuls.core.model.ByteUtils;
import io.nuls.core.rockdb.service.RocksDBService;
import io.nuls.crosschain.base.model.bo.txdata.RegisteredChainMessage;
import io.nuls.crosschain.constant.NulsCrossChainConstant;
import io.nuls.crosschain.model.po.CtxStatusPO;
import io.nuls.crosschain.model.po.LocalVerifierPO;
import io.nuls.crosschain.model.po.SendCtxHashPO;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/**
 * @author: PierreLuo
 * @date: 2022/3/3
 */
public class DbTest {


    @BeforeClass
    public static void before() {
        Log.info("init");
        RocksDBService.init("/Users/pierreluo/IdeaProjects/nuls_newer_2.0/data/cross-chain/");
        System.out.println();
    }

    @Test
    public void local_verifier() throws Exception {
        byte[] stream = RocksDBService.get("local_verifier", ByteUtils.intToBytes(1));
        LocalVerifierPO po = new LocalVerifierPO();
        try {
            po.parse(stream,0);
        }catch (Exception e){
            Log.error(e);
        }
        Set<String> set = new HashSet<>(po.getVerifierList());
        System.out.println(set.contains("NULSd6HgeBrHjCu88naeGG2etPbyh31YMfWc7"));
        //System.out.println(set.contains("NULSd6HggfvuZoDMJ26kRpJ5uzC6WrRwko9iM"));
        //System.out.println(set.contains("NULSd6Hga4mQkHAnQdhUiMmv1V3kQ4a84JaSb"));
        //System.out.println(set.contains("NULSd6HgjHLnaPdyPYADSZy9UqewvJFEkAUP1"));
    }

    @Test
    public void registered_chain() throws Exception {
        byte[] messageBytes = RocksDBService.get("registered_chain", NulsCrossChainConstant.DB_NAME_REGISTERED_CHAIN.getBytes());
        RegisteredChainMessage registeredChainMessage = new RegisteredChainMessage();
        registeredChainMessage.parse(messageBytes,0);
        System.out.println();
    }

    @Test
    public void new_ctx_status1() throws Exception {
        //String hash = "75178d3968c258b58896f64c7fc93ed8d00c4ec69c51a06821cc0d1c1ef1cca7";
        String hash = "ace4c1ef75a047105604d12928ee9ae101ebeb1c6eb86c0ca124a725de4f2c90";
        byte[] stream = RocksDBService.get("new_ctx_status1", HexUtil.decode(hash));
        CtxStatusPO tx = new CtxStatusPO();
        tx.parse(stream,0);
        System.out.println();
    }

    @Test
    public void send_height() throws Exception {
        byte[] valueBytes = RocksDBService.get(NulsCrossChainConstant.DB_NAME_SEND_HEIGHT+1, ByteUtils.longToBytes(12108843));
        SendCtxHashPO po = new SendCtxHashPO();
        po.parse(valueBytes,0);
        System.out.println();
    }

    @Test
    public void convert_hash_ctx() throws Exception {
        String hash = "75178d3968c258b58896f64c7fc93ed8d00c4ec69c51a06821cc0d1c1ef1cca7";
        byte[] valueBytes = RocksDBService.get(NulsCrossChainConstant.DB_NAME_CONVERT_HASH_CTX+1, HexUtil.decode(hash));
        NulsHash nulsHash = new NulsHash(valueBytes);
        System.out.println(nulsHash.toHex());
    }


}
