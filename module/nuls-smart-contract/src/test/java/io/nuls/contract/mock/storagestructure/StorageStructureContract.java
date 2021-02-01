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
package io.nuls.contract.mock.storagestructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.contract.mock.basetest.MockBase;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.VMFactory;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.parse.JSONUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * @author: PierreLuo
 * @date: 2020-12-15
 */
public class StorageStructureContract extends MockBase {

    @Override
    protected void protocolUpdate() {
        short version = 9;
        ProtocolGroupManager.setLoadProtocol(false);
        ProtocolGroupManager.updateProtocol(chainId, version);
        if (version >= 8) {
            VMFactory.reInitVM_v8();
        }
    }

    /*
        prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "getMapValue", new String[]{"a"});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);

        prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "getMapValue", new String[]{"b"});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);

        prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "setMapValue", new String[]{"c31", "c31"});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);

        prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "getMapValue", new String[]{"c30"});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);

        prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "getMapValue", new String[]{"c31"});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
     */

    @Test
    public void testCreate() throws IOException, InterruptedException {
        InputStream in = new FileInputStream(new File("/Users/pierreluo/IdeaProjects/storage-structure-contract/target/storage-structure-contract-1.0-SNAPSHOT.jar"));
        byte[] contractCode = IOUtils.toByteArray(in);

        byte[] prevStateRoot = HexUtil.decode("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421");
        byte[] stateRoot = super.create(prevStateRoot, SENDER, contractCode);

        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        // 32b05b57e5ddc8eabba05a9a7b006b3cd4c8d8d7662cb0553c3c05e471cd61fe
        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    public void testMapAdd() throws Exception {
        // eaa90d836849ec31698bdbaa7a7e94fd9a4292e33a0f405565a10128375bc9c8 12288 再添加一个，就触发resize
        // 8fd0a0b82976ecdc98d6f14e406b0ec8409d3c64b85da0f98b319944db15ecc6 接eaa, 扩容因子被改变成1
        // 87fa8f0b5f708c909961ab4abab30777c2e1455ca58aa6e5adae5601826501af 接eaa, 被扩容到65536
        // 096917a2d4a70bcdbecef243887f87feeba693d1e8ac518460ae2f8e7b58b243 接87f
        byte[] prevStateRoot = HexUtil.decode("6ee749e0360819359387c0c089928b27faaeab91fbd71427afc52c457df47e5c");
        Object[] objects = super.call(prevStateRoot, SENDER, "addMap", new String[]{"q", "1"});
        byte[] stateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(6);
    }

    @Test
    public void testListAdd() throws Exception {
        byte[] prevStateRoot = HexUtil.decode("1837fbeef99a7671e5ebd2eebcf83bb118a384ebaafb414da18ab9b3ed7fd140");
        Object[] objects = super.call(prevStateRoot, SENDER, "addList", new String[]{"a11-", "1"});
        byte[] stateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(3);
    }
    @Test
    public void getListByIndex() throws Exception {
        byte[] prevStateRoot = HexUtil.decode("eb38cb09d83bd402b2637d118e5b489f216131daea35a404dab29b1c2afc679f");
        Object[] objects = super.call(prevStateRoot, SENDER, "getListByIndex", new String[]{"71139"});
        byte[] stateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);

        prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "getListByIndex", new String[]{"32110"});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);

        prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "getListByIndex", new String[]{"15210"});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    public void getMapValue() throws Exception {
        byte[] prevStateRoot = HexUtil.decode("6ee749e0360819359387c0c089928b27faaeab91fbd71427afc52c457df47e5c");
        Object[] objects = super.call(prevStateRoot, SENDER, "getMapValue", new String[]{"p0"});
        byte[] stateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);

        /*prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "getMapValue", new String[]{"b121"});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);

        prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "getMapValue", new String[]{"b1211"});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);

        prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "getMapValue", new String[]{"b1999"});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);*/
    }



    @Test
    public void setLongStr() throws Exception {
        byte[] prevStateRoot = HexUtil.decode("7945b873f1b21c0b622c6488780c45e2fca37642ef28c1e02ac50afc05697229");
        Object[] objects = super.call(prevStateRoot, SENDER, "setLongStr", new String[]{"504b0304140008080800038b2d4d000000000000000000000000090004004d4554412d494e462ffeca00000300504b0708000000000200000000000000504b0304140008080800038b2d4d000000000000000000000000140000004d4554412d494e462f4d414e49464553542e4d46f34dcccb4c4b2d2ed10d4b2d2acecccfb35230d433e0e5722e4a4d2c494dd175aa040958e819c41b9a982868f8172526e7a42a38e71715e417259600d56bf272f1720100504b07089e7c76534400000045000000504b03040a0000080000fb8a2d4d0000000000000000000000001b00000074657374636f6e74726163742f6d756c74797472616e736665722f504b0304140008080800fb8a2d4d0000000000000000000000003200000074657374636f6e74726163742f6d756c74797472616e736665722f546573744d756c74795472616e736665722e636c6173739d56eb7313d715ffad257997454e8cea00f210aa262d91658cb00c989a94c498180cb6f143d8d86d4256d25a5a4bda95b52b83d31749daa4e933e9234ddf8f90a68fb40d2d96dd64265f3ac34cfb0ff463ff857eea97cc747acedd95ac48824e6a8ff69e7b9ebf73ef3967f7efff79e73d00c7f08e8a53585590df85fb505050e4d5e48725a3a442c62a3fd6149479b515382cacc85857710dd75584b0a1e219d8323eabe073bcff7c00fcf7daab3758f005667d51c18d1af75905cf0a5a1a1c8082e75cfa6f7fbda1e079155fc29715bcc0be5f64435ba5685f51f09282af32f36b8ce2eb0abec19b6ff2e65b0a5ee6cd2b0abeade03b2abe8beff1e35505dfe7f535053f50f043267fa4e0c7327e22e3a712fca656d425842657b5752d5ed0cc6c7cde291b66f694844e7ba398b20a12bac62cd37634d359d00a15d256327ada286a055b8234e"});
        byte[] stateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(3);
    }

    @Test
    public void getLongStr() throws Exception {
        byte[] prevStateRoot = HexUtil.decode("74b149e895922084d4e96f2eaaa4a65376e03768c6d9c2afc092331ae9616bb8");
        Object[] objects = super.call(prevStateRoot, SENDER, "getLongStr", new String[]{});
        byte[] stateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);
    }


    @Test
    public void testMapInitial() throws Exception {
        byte[] prevStateRoot = HexUtil.decode("7260585b3902b0cc30a235fc9b6b75b99d41829a21a3109cc29f9d7b08b89bf0");
        Object[] objects = super.call(prevStateRoot, SENDER, "mapInit", new String[]{});
        byte[] stateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(3);
    }

    @Test
    public void testMapInsert() throws Exception {
        byte[] prevStateRoot = HexUtil.decode("ba41dfe0b28c679a569a4f60847b74984f3510fa597fb086025798209d4b4efd");
        Object[] objects = super.call(prevStateRoot, SENDER, "getStr", new String[]{});
        byte[] stateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);

        prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "getMapValue", new String[]{"c31"});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);

        prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "setMapValue", new String[]{"c31", "c31-insert"});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);

        prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "getMapValue", new String[]{"c31"});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    public void testIntUpdate() throws Exception {
        byte[] prevStateRoot = HexUtil.decode("a75ed8e32a66957170fdad8105939903f36f9d0248a5e615e1d8c84c3cd794ec");
        Object[] objects = super.call(prevStateRoot, SENDER, "getAstr", new String[]{});
        byte[] stateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);

        prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "setA", new String[]{"88"});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);

        prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "getAstr", new String[]{});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    public void testIntFind() throws Exception {
        byte[] prevStateRoot = HexUtil.decode("81679624169423a3e480dae47f0e78c4935b83d94c35a6d741efa0af8e4b554f");
        Object[] objects = super.call(prevStateRoot, SENDER, "getAstr", new String[]{});
        byte[] stateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);

        prevStateRoot = stateRoot;
        objects = super.call(prevStateRoot, SENDER, "getAstr", new String[]{});
        stateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot) + "\n");
        TimeUnit.SECONDS.sleep(1);
    }

    @Test
    public void callMuchMint() throws JsonProcessingException {
        byte[] prevStateRoot = HexUtil.decode("52517a7c3889a129cea32531cdc45109a5228d31e51bdfcd55394fa580d11ab4");
        int size = 10000;
        for(int i=1;i<=size;i++) {
            Object[] objects = super.call(prevStateRoot, SENDER, "mint", new String[]{toAddress0, String.valueOf(i)});
            prevStateRoot = (byte[]) objects[0];
            ProgramResult programResult = (ProgramResult) objects[1];
            Log.info(String.format("%s cost: %s", i, programResult.getGasUsed()));
        }
        Log.info("stateRoot: " + HexUtil.encode(prevStateRoot));
    }

    @Test
    public void testView() throws JsonProcessingException {
        byte[] prevStateRoot = HexUtil.decode("54803e34c33c92544ad8846bf4404944ae31e80143e4036359f8c60698bbc167");
        String balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Log.info("view result: " + balanceOf);
    }

}
