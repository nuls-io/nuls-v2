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
package io.nuls.contract.mock.nrc721;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.contract.mock.basetest.MockBase;
import io.nuls.contract.mock.invokeexternalcmd.InvokeExternalCmdLocalTest;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.parse.JSONUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * @author: PierreLuo
 * @date: 2019-06-11
 */
public class NRC721Test extends MockBase {

    @Test
    public void testCreate() throws IOException {
        InputStream in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/nrc721_metadata-test.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);

        byte[] prevStateRoot = HexUtil.decode("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421");
        byte[] stateRoot = super.create(prevStateRoot, SENDER, contractCode, "atom", "ATOM");

        System.out.println("stateRoot: " + HexUtil.encode(stateRoot));
        System.out.println();
    }

    @Test
    public void testCall() throws JsonProcessingException {
        byte[] prevStateRoot = HexUtil.decode("52517a7c3889a129cea32531cdc45109a5228d31e51bdfcd55394fa580d11ab4");
        Object[] objects = super.call(prevStateRoot, SENDER, "mint", new String[]{toAddress0, "1"});
        byte[] stateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        System.out.println(JSONUtils.obj2PrettyJson(programResult));
        System.out.println("stateRoot: " + HexUtil.encode(stateRoot));
        System.out.println();
    }

    @Test
    public void callMuchMint() throws JsonProcessingException {
        byte[] prevStateRoot = HexUtil.decode("52517a7c3889a129cea32531cdc45109a5228d31e51bdfcd55394fa580d11ab4");
        int size = 10000;
        for(int i=1;i<=size;i++) {
            Object[] objects = super.call(prevStateRoot, SENDER, "mint", new String[]{toAddress0, String.valueOf(i)});
            prevStateRoot = (byte[]) objects[0];
            ProgramResult programResult = (ProgramResult) objects[1];
            System.out.println(String.format("%s cost: %s", i, programResult.getGasUsed()));
        }
        System.out.println("stateRoot: " + HexUtil.encode(prevStateRoot));
    }

    @Test
    public void testView() throws JsonProcessingException {
        byte[] prevStateRoot = HexUtil.decode("54803e34c33c92544ad8846bf4404944ae31e80143e4036359f8c60698bbc167");
        String balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        System.out.println("view result: " + balanceOf);
    }

    @Test
    public void testContractMethod() throws IOException {

        InputStream in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/nrc721_metadata-test.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        List<ProgramMethod> programMethods = programExecutor.jarMethod(contractCode);

        System.out.println();
        System.out.println(JSONUtils.obj2PrettyJson(programMethods));
        System.out.println();
    }

    @Test
    public void testFull() throws Exception {
        // -------------------------------------------------------------------------------------//
        InputStream in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/nrc721_metadata-test.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);

        byte[] initialStateRoot = HexUtil.decode("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421");
        byte[] prevStateRoot = super.create(initialStateRoot, SENDER, contractCode, "atom", "ATOM");

        // 期望 token name
        String name = super.view(prevStateRoot, "name", new String[]{});
        Assert.assertEquals("name expect atom", "atom", name);
        // 期望 token symbol
        String symbol = super.view(prevStateRoot, "symbol", new String[]{});
        Assert.assertEquals("symbol expect ATOM", "ATOM", symbol);

        // -------------------------------------------------------------------------------------//

        // 造币 id:1 -> toAddress0
        Object[] objects = super.call(prevStateRoot, SENDER, "mint", new String[]{toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        Assert.assertTrue(programResult.isSuccess());
        // 期望toAddress0余额是1
        String balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // 期望toAddress0拥有token1
        String ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress0, toAddress0, ownerOf);

        // -------------------------------------------------------------------------------------//
        // 造币 id:2 -> toAddress0
        objects = super.call(prevStateRoot, SENDER, "mint", new String[]{toAddress0, String.valueOf(2)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue(programResult.isSuccess());
        // 期望toAddress0余额是2
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 2", "2", balanceOf);
        // 期望toAddress0拥有token2
        ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"2"});
        Assert.assertEquals("ownerOf expect " + toAddress0, toAddress0, ownerOf);

        // 期望失败，非minter
        objects = super.call(prevStateRoot, sender, "mint", new String[]{toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("onlyMinter expect error", programResult.isSuccess());

        // -------------------------------------------------------------------------------------//

        // 期望失败，重复tokenId
        objects = super.call(prevStateRoot, SENDER, "mintWithTokenURI", new String[]{toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("duplicate tokenId 1", programResult.isSuccess());
        // 期望失败，重复tokenId
        objects = super.call(prevStateRoot, SENDER, "mintWithTokenURI", new String[]{toAddress0, String.valueOf(2)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("duplicate tokenId 2", programResult.isSuccess());

        // -------------------------------------------------------------------------------------//

        // 造币 id:3, 带URI -> toAddress1
        objects = super.call(prevStateRoot, SENDER, "mintWithTokenURI", new String[]{toAddress1, String.valueOf(3), "https://nuls.io"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue(programResult.isSuccess());
        // 期望 tokenId 3 URI 是 https://nuls.io
        String uri = super.view(prevStateRoot, "tokenURI", new String[]{"3"});
        Assert.assertEquals("tokenURI expect https://nuls.io", "https://nuls.io", uri);

        // -------------------------------------------------------------------------------------//

        // 造币 id:4, 带URI -> toAddress1
        objects = super.call(prevStateRoot, SENDER, "mintWithTokenURI", new String[]{toAddress1, String.valueOf(4), "https://nulscan.io"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue(programResult.isSuccess());
        // 期望 tokenId 4 URI
        uri = super.view(prevStateRoot, "tokenURI", new String[]{"4"});
        Assert.assertEquals("tokenURI expect https://nulscan.io", "https://nulscan.io", uri);
        // 期望失败 tokenId 2的URI不存在
        String tokenURI = super.view(prevStateRoot, "tokenURI", new String[]{"2"});
        Assert.assertNull("expect URI not exists", tokenURI);
        // 期望失败 tokenId 5 不存在
        String tokenURI1 = super.view(prevStateRoot, "tokenURI", new String[]{"5"});
        Assert.assertNull("expect token not exists", tokenURI1);

        // -------------------------------------------------------------------------------------//

        // 期望失败，token5不存在
        objects = super.call(prevStateRoot, SENDER, "safeTransferFrom", new String[]{toAddress0, toAddress2, String.valueOf(5), "remark_data"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("expect fail", programResult.isSuccess());
        // 期望失败，SENDER 无权转移token1 从 toAddress0 到 toAddress2
        objects = super.call(prevStateRoot, SENDER, "safeTransferFrom", new String[]{toAddress0, toAddress2, String.valueOf(1), "remark_data"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("expect fail", programResult.isSuccess());

        // -------------------------------------------------------------------------------------//

        // 期望toAddress0余额是2
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 2", "2", balanceOf);
        // 期望toAddress2余额是0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress2});
        Assert.assertEquals("balance expect 0", "0", balanceOf);

        // 转移token1 从 toAddress0 到 toAddress2
        objects = super.call(prevStateRoot, toAddress0, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId, String data) return void", new String[]{toAddress0, toAddress2, String.valueOf(1), "remark_data"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success", programResult.isSuccess());
        // 期望toAddress0余额是1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // 期望toAddress2余额是1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress2});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // 期望toAddress2拥有token1
        ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress2, toAddress2, ownerOf);

        // 转移token1 从 toAddress2 到 toAddress0
        objects = super.call(prevStateRoot, toAddress2, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId) return void", new String[]{toAddress2, toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success", programResult.isSuccess());
        // 期望toAddress0余额是2
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 2", "2", balanceOf);
        // 期望toAddress2余额是0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress2});
        Assert.assertEquals("balance expect 0", "0", balanceOf);
        // 期望toAddress0拥有token1
        ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress0, toAddress0, ownerOf);

        // -------------------------------------------------------------------------------------//

        //TODO pierre 接收者是合约地址 safeTransferFrom
        //....

        // -------------------------------------------------------------------------------------//

        // 期望toAddress0余额是2
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 2", "2", balanceOf);
        // 期望toAddress2余额是0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress2});
        Assert.assertEquals("balance expect 0", "0", balanceOf);

        // 转移token1 从 toAddress0 到 toAddress2
        objects = super.call(prevStateRoot, toAddress0, "transferFrom", new String[]{toAddress0, toAddress2, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success", programResult.isSuccess());
        // 期望toAddress0余额是1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // 期望toAddress2余额是1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress2});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // 期望toAddress2拥有token1
        ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress2, toAddress2, ownerOf);

        // 转移token1 从 toAddress2 到 toAddress0
        objects = super.call(prevStateRoot, toAddress2, "transferFrom", new String[]{toAddress2, toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success", programResult.isSuccess());
        // 期望toAddress0余额是2
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 2", "2", balanceOf);
        // 期望toAddress2余额是0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress2});
        Assert.assertEquals("balance expect 0", "0", balanceOf);
        // 期望toAddress0拥有token1
        ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress0, toAddress0, ownerOf);

        // -------------------------------------------------------------------------------------//

        // 期望失败，token5不存在
        objects = super.call(prevStateRoot, toAddress1, "approve", new String[]{toAddress2, String.valueOf(5)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("expect fail", programResult.isSuccess());

        // 期望失败，toAddress1无权授权token1
        objects = super.call(prevStateRoot, toAddress1, "approve", new String[]{toAddress2, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("expect fail", programResult.isSuccess());

        // toAddress0授权token1给toAddress2
        objects = super.call(prevStateRoot, toAddress0, "approve", new String[]{toAddress2, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success", programResult.isSuccess());
        // 期望token1授权给了toAddress2
        ownerOf = super.view(prevStateRoot, "getApproved", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress2, toAddress2, ownerOf);
        // 期望失败，token5不存在
        ownerOf = super.view(prevStateRoot, "getApproved", new String[]{"5"});
        Assert.assertNull("ownerOf expect null", ownerOf);

        // -------------------------------------------------------------------------------------//

        // 期望toAddress0余额是2
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 2", "2", balanceOf);
        // 期望toAddress3余额是0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress3});
        Assert.assertEquals("balance expect 0", "0", balanceOf);

        // 期望失败，使用授权账户 toAddress2 转移 token1，token1 不属于 toAddress1
        objects = super.call(prevStateRoot, toAddress2, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId, String data) return void", new String[]{toAddress1, toAddress3, String.valueOf(1), "remark_data"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse(programResult.isSuccess());
        // 使用授权账户 toAddress2 转移 token1 从 toAddress0 到 toAddress3
        objects = super.call(prevStateRoot, toAddress2, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId, String data) return void", new String[]{toAddress0, toAddress3, String.valueOf(1), "remark_data"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success", programResult.isSuccess());
        // 期望toAddress0余额是1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // 期望toAddress3余额是1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress3});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // 期望toAddress3拥有token1
        ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress3, toAddress3, ownerOf);
        // 期望token1没有授权账户
        ownerOf = super.view(prevStateRoot, "getApproved", new String[]{"1"});
        Assert.assertNull("ownerOf expect null", ownerOf);

        // -------------------------------------------------------------------------------------//

        //TODO pierre setApprovalForAll函数

        // -------------------------------------------------------------------------------------//

        System.out.println("stateRoot: " + HexUtil.encode(prevStateRoot));
        System.out.println();
    }

}
