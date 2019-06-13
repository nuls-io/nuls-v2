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
import io.nuls.contract.util.Log;
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

        Log.info("stateRoot: " + HexUtil.encode(stateRoot));
        Log.info("\n");
    }

    @Test
    public void testCall() throws JsonProcessingException {
        byte[] prevStateRoot = HexUtil.decode("52517a7c3889a129cea32531cdc45109a5228d31e51bdfcd55394fa580d11ab4");
        Object[] objects = super.call(prevStateRoot, SENDER, "mint", new String[]{toAddress0, "1"});
        byte[] stateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot));
        Log.info("\n");
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

    @Test
    public void testContractMethod() throws IOException {

        InputStream in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/nrc721_metadata-test.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        List<ProgramMethod> programMethods = programExecutor.jarMethod(contractCode);

        Log.info("\n");
        Log.info(JSONUtils.obj2PrettyJson(programMethods));
        Log.info("\n");
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
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
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
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
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
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // 期望 tokenId 3 URI 是 https://nuls.io
        String uri = super.view(prevStateRoot, "tokenURI", new String[]{"3"});
        Assert.assertEquals("tokenURI expect https://nuls.io", "https://nuls.io", uri);

        // -------------------------------------------------------------------------------------//

        // 造币 id:4, 带URI -> toAddress1
        objects = super.call(prevStateRoot, SENDER, "mintWithTokenURI", new String[]{toAddress1, String.valueOf(4), "https://nulscan.io"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
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
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
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
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // 期望toAddress0余额是2
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 2", "2", balanceOf);
        // 期望toAddress2余额是0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress2});
        Assert.assertEquals("balance expect 0", "0", balanceOf);
        // 期望toAddress0拥有token1
        ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress0, toAddress0, ownerOf);

        // --------------------------------------[接收者是合约地址 safeTransferFrom]---------------------------------------------//

        in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/nrc721_metadata-test.jar").getFile());
        contractCode = IOUtils.toByteArray(in);
        prevStateRoot = super.create(prevStateRoot, contractAddress1, SENDER, contractCode, "atom", "ATOM");
        // 期望失败，转移token1 从 toAddress0 到 contractAddress1, contractAddress1是合约地址，此合约未实现 onNRC721Received 方法
        objects = super.call(prevStateRoot, toAddress0, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId) return void", new String[]{toAddress0, contractAddress1, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("expect fail", programResult.isSuccess());

        in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/NRC721Receiver-test.jar").getFile());
        contractCode = IOUtils.toByteArray(in);
        prevStateRoot = super.create(prevStateRoot, contractAddress2, SENDER, contractCode);
        // 期望成功，转移token1 从 toAddress0 到 contractAddress2, contractAddress2是合约地址，此合约实现了 onNRC721Received 方法
        objects = super.call(prevStateRoot, toAddress0, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId) return void", new String[]{toAddress0, contractAddress2, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());

        // 期望 toAddress0 余额是1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // 期望 contractAddress2 余额是1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{contractAddress2});
        Assert.assertEquals("balance expect 1", "1", balanceOf);

        // 期望失败，不是合约地址
        objects = super.call(contractAddress2, prevStateRoot, sender, "transferOtherNRC721", new String[]{toAddress6, toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse(programResult.isSuccess());
        // 期望失败，没有token在合约地址 contractAddress6
        objects = super.call(contractAddress2, prevStateRoot, sender, "transferOtherNRC721", new String[]{contractAddress6, toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse(programResult.isSuccess());
        // 期望失败，没有token2在合约地址 ADDRESS
        objects = super.call(contractAddress2, prevStateRoot, sender, "transferOtherNRC721", new String[]{ADDRESS, toAddress0, String.valueOf(2)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse(programResult.isSuccess());

        in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/nrc721_metadata-test.jar").getFile());
        contractCode = IOUtils.toByteArray(in);
        prevStateRoot = super.create(prevStateRoot, contractAddress3, SENDER, contractCode, "atom", "ATOM");
        // 期望失败，转移token1 从 contractAddress2 到 contractAddress3, contractAddress3是合约地址，此合约未实现 onNRC721Received 方法
        objects = super.call(contractAddress2, prevStateRoot, sender, "transferOtherNRC721", new String[]{ADDRESS, contractAddress3, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse(programResult.isSuccess());
        // 期望成功，转移token1 从 contractAddress2 到 contractAddress4, contractAddress4是合约地址，此合约实现了 onNRC721Received 方法
        in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/NRC721Receiver-test.jar").getFile());
        contractCode = IOUtils.toByteArray(in);
        prevStateRoot = super.create(prevStateRoot, contractAddress4, SENDER, contractCode);
        objects = super.call(contractAddress2, prevStateRoot, sender, "transferOtherNRC721", new String[]{ADDRESS, contractAddress4, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // 期望 contractAddress2 余额是0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{contractAddress2});
        Assert.assertEquals("balance expect 0", "0", balanceOf);
        // 期望 contractAddress4 余额是1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{contractAddress4});
        Assert.assertEquals("balance expect 1", "1", balanceOf);

        // 期望成功，转移token1 从 contractAddress4 到 toAddress0
        objects = super.call(contractAddress4, prevStateRoot, sender, "transferOtherNRC721", new String[]{ADDRESS, toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());

        // 期望 contractAddress4 余额是0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{contractAddress4});
        Assert.assertEquals("balance expect 0", "0", balanceOf);
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
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
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
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
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
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
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
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
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

        // 期望失败，toAddress1 无权授权给自己
        objects = super.call(prevStateRoot, toAddress1, "setApprovalForAll", new String[]{toAddress1, "true"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("expect fail", programResult.isSuccess());

        // toAddress3 授权给 toAddress4
        objects = super.call(prevStateRoot, toAddress3, "setApprovalForAll", new String[]{toAddress4, "true"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // 期望 toAddress3 授权给了 toAddress4
        String bool = super.view(prevStateRoot, "isApprovedForAll", new String[]{toAddress3, toAddress4});
        Assert.assertEquals("result expect true", "true", bool);
        // 期望失败，toAddress5 未被授权
        bool = super.view(prevStateRoot, "isApprovedForAll", new String[]{toAddress3, toAddress5});
        Assert.assertEquals("result expect false", "false", bool);

        // -------------------------------------------------------------------------------------//

        // 期望失败，使用授权账户 toAddress4 转移 token2，token2 不属于 toAddress3
        objects = super.call(prevStateRoot, toAddress4, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId, String data) return void", new String[]{toAddress3, toAddress5, String.valueOf(2), "remark_data"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse(programResult.isSuccess());
        // 使用授权账户 toAddress4 转移 token1 从 toAddress3 到 toAddress5
        objects = super.call(prevStateRoot, toAddress4, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId, String data) return void", new String[]{toAddress3, toAddress5, String.valueOf(1), "remark_data"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // 期望toAddress3余额是0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress3});
        Assert.assertEquals("balance expect 0", "0", balanceOf);
        // 期望toAddress5余额是1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress5});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // 期望toAddress5拥有token1
        ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress5, toAddress5, ownerOf);
        // 期望token1没有授权账户
        ownerOf = super.view(prevStateRoot, "getApproved", new String[]{"1"});
        Assert.assertNull("ownerOf expect null", ownerOf);
        // 期望 toAddress3 授权给了 toAddress4
        bool = super.view(prevStateRoot, "isApprovedForAll", new String[]{toAddress3, toAddress4});
        Assert.assertEquals("result expect true", "true", bool);
        // 期望失败，toAddress5 未被授权
        bool = super.view(prevStateRoot, "isApprovedForAll", new String[]{toAddress3, toAddress5});
        Assert.assertEquals("result expect false", "false", bool);

        // -------------------------------------------------------------------------------------//

        Log.info("stateRoot: " + HexUtil.encode(prevStateRoot));
        Log.info("\n");
    }

}
