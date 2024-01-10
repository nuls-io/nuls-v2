/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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
        InputStream in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/NRC721Metadata-test.jar").getFile());
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
    public void testContractMethod() throws Exception {
        InputStream in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/NRC721Metadata-test.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        List<ProgramMethod> programMethods = programExecutor.jarMethod(contractCode);
        Log.info("\n");
        Log.info(JSONUtils.obj2PrettyJson(programMethods));
        Log.info("\n");
    }

    @Test
    public void testFull() throws Exception {
        // -------------------------------------------------------------------------------------//
        InputStream in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/NRC721Metadata-test.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);

        byte[] initialStateRoot = HexUtil.decode("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421");
        byte[] prevStateRoot = super.create(initialStateRoot, SENDER, contractCode, "atom", "ATOM");

        // expect token name
        String name = super.view(prevStateRoot, "name", new String[]{});
        Assert.assertEquals("name expect atom", "atom", name);
        // expect token symbol
        String symbol = super.view(prevStateRoot, "symbol", new String[]{});
        Assert.assertEquals("symbol expect ATOM", "ATOM", symbol);

        // -------------------------------------------------------------------------------------//

        // Coinmaking id:1 -> toAddress0
        Object[] objects = super.call(prevStateRoot, SENDER, "mint", new String[]{toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // expecttoAddress0The balance is1
        String balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // expecttoAddress0havetoken1
        String ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress0, toAddress0, ownerOf);

        // -------------------------------------------------------------------------------------//
        // Coinmaking id:2 -> toAddress0
        objects = super.call(prevStateRoot, SENDER, "mint", new String[]{toAddress0, String.valueOf(2)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // expecttoAddress0The balance is2
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 2", "2", balanceOf);
        // expecttoAddress0havetoken2
        ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"2"});
        Assert.assertEquals("ownerOf expect " + toAddress0, toAddress0, ownerOf);

        // Expectation failure, notminter
        objects = super.call(prevStateRoot, sender, "mint", new String[]{toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("onlyMinter expect error", programResult.isSuccess());

        // -------------------------------------------------------------------------------------//

        // Expectation failure, repetitiontokenId
        objects = super.call(prevStateRoot, SENDER, "mintWithTokenURI", new String[]{toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("duplicate tokenId 1", programResult.isSuccess());
        // Expectation failure, repetitiontokenId
        objects = super.call(prevStateRoot, SENDER, "mintWithTokenURI", new String[]{toAddress0, String.valueOf(2)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("duplicate tokenId 2", programResult.isSuccess());

        // -------------------------------------------------------------------------------------//

        // Coinmaking id:3, beltURI -> toAddress1
        objects = super.call(prevStateRoot, SENDER, "mintWithTokenURI", new String[]{toAddress1, String.valueOf(3), "https://nuls.io"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // expect tokenId 3 URI yes https://nuls.io
        String uri = super.view(prevStateRoot, "tokenURI", new String[]{"3"});
        Assert.assertEquals("tokenURI expect https://nuls.io", "https://nuls.io", uri);

        // -------------------------------------------------------------------------------------//

        // Coinmaking id:4, beltURI -> toAddress1
        objects = super.call(prevStateRoot, SENDER, "mintWithTokenURI", new String[]{toAddress1, String.valueOf(4), "https://nulscan.io"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // expect tokenId 4 URI
        uri = super.view(prevStateRoot, "tokenURI", new String[]{"4"});
        Assert.assertEquals("tokenURI expect https://nulscan.io", "https://nulscan.io", uri);
        // Expected failure tokenId 2ofURINot present
        String tokenURI = super.view(prevStateRoot, "tokenURI", new String[]{"2"});
        Assert.assertNull("expect URI not exists", tokenURI);
        // Expected failure tokenId 5 Not present
        String tokenURI1 = super.view(prevStateRoot, "tokenURI", new String[]{"5"});
        Assert.assertNull("expect token not exists", tokenURI1);

        // -------------------------------------------------------------------------------------//

        // Expectations fail,token5Not present
        objects = super.call(prevStateRoot, SENDER, "safeTransferFrom", new String[]{toAddress0, toAddress2, String.valueOf(5), "remark_data"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("expect fail", programResult.isSuccess());
        // Expectations fail,SENDER Unauthorized transfertoken1 from toAddress0 reach toAddress2
        objects = super.call(prevStateRoot, SENDER, "safeTransferFrom", new String[]{toAddress0, toAddress2, String.valueOf(1), "remark_data"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("expect fail", programResult.isSuccess());

        // -------------------------------------------------------------------------------------//

        // expecttoAddress0The balance is2
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 2", "2", balanceOf);
        // expecttoAddress2The balance is0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress2});
        Assert.assertEquals("balance expect 0", "0", balanceOf);

        // transfertoken1 from toAddress0 reach toAddress2
        objects = super.call(prevStateRoot, toAddress0, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId, String data) return void", new String[]{toAddress0, toAddress2, String.valueOf(1), "remark_data"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // expecttoAddress0The balance is1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // expecttoAddress2The balance is1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress2});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // expecttoAddress2havetoken1
        ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress2, toAddress2, ownerOf);

        // transfertoken1 from toAddress2 reach toAddress0
        objects = super.call(prevStateRoot, toAddress2, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId) return void", new String[]{toAddress2, toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // expecttoAddress0The balance is2
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 2", "2", balanceOf);
        // expecttoAddress2The balance is0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress2});
        Assert.assertEquals("balance expect 0", "0", balanceOf);
        // expecttoAddress0havetoken1
        ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress0, toAddress0, ownerOf);

        // --------------------------------------[The recipient is the contract address safeTransferFrom]---------------------------------------------//

        in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/NRC721Metadata-test.jar").getFile());
        contractCode = IOUtils.toByteArray(in);
        prevStateRoot = super.create(prevStateRoot, contractAddress1, SENDER, contractCode, "atom", "ATOM");
        // Expectation failure, transfertoken1 from toAddress0 reach contractAddress1, contractAddress1This is the contract address, this contract is not implemented onNRC721Received method
        objects = super.call(prevStateRoot, toAddress0, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId) return void", new String[]{toAddress0, contractAddress1, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("expect fail", programResult.isSuccess());

        in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/NRC721Receiver-test.jar").getFile());
        contractCode = IOUtils.toByteArray(in);
        prevStateRoot = super.create(prevStateRoot, contractAddress2, SENDER, contractCode);
        // Expected success, transfertoken1 from toAddress0 reach contractAddress2, contractAddress2This is the contract address, which implements the onNRC721Received method
        objects = super.call(prevStateRoot, toAddress0, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId) return void", new String[]{toAddress0, contractAddress2, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());

        // expect toAddress0 The balance is1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // expect contractAddress2 The balance is1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{contractAddress2});
        Assert.assertEquals("balance expect 1", "1", balanceOf);

        // Expectation failed, not contract address
        objects = super.call(contractAddress2, prevStateRoot, sender, "transferOtherNRC721", new String[]{toAddress6, toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse(programResult.isSuccess());
        // Expected failure, notokenAt the contracted address contractAddress6
        objects = super.call(contractAddress2, prevStateRoot, sender, "transferOtherNRC721", new String[]{contractAddress6, toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse(programResult.isSuccess());
        // Expected failure, notoken2At the contracted address ADDRESS
        objects = super.call(contractAddress2, prevStateRoot, sender, "transferOtherNRC721", new String[]{ADDRESS, toAddress0, String.valueOf(2)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse(programResult.isSuccess());

        in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/NRC721Metadata-test.jar").getFile());
        contractCode = IOUtils.toByteArray(in);
        prevStateRoot = super.create(prevStateRoot, contractAddress3, SENDER, contractCode, "atom", "ATOM");
        // Expectation failure, transfertoken1 from contractAddress2 reach contractAddress3, contractAddress3This is the contract address, this contract is not implemented onNRC721Received method
        objects = super.call(contractAddress2, prevStateRoot, sender, "transferOtherNRC721", new String[]{ADDRESS, contractAddress3, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse(programResult.isSuccess());
        // Expected success, transfertoken1 from contractAddress2 reach contractAddress4, contractAddress4This is the contract address, which implements the onNRC721Received method
        in = new FileInputStream(InvokeExternalCmdLocalTest.class.getResource("/NRC721Receiver-test.jar").getFile());
        contractCode = IOUtils.toByteArray(in);
        prevStateRoot = super.create(prevStateRoot, contractAddress4, SENDER, contractCode);
        objects = super.call(contractAddress2, prevStateRoot, sender, "transferOtherNRC721", new String[]{ADDRESS, contractAddress4, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // expect contractAddress2 The balance is0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{contractAddress2});
        Assert.assertEquals("balance expect 0", "0", balanceOf);
        // expect contractAddress4 The balance is1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{contractAddress4});
        Assert.assertEquals("balance expect 1", "1", balanceOf);

        // Expected success, transfertoken1 from contractAddress4 reach toAddress0
        objects = super.call(contractAddress4, prevStateRoot, sender, "transferOtherNRC721", new String[]{ADDRESS, toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());

        // expect contractAddress4 The balance is0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{contractAddress4});
        Assert.assertEquals("balance expect 0", "0", balanceOf);
        // -------------------------------------------------------------------------------------//

        // expecttoAddress0The balance is2
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 2", "2", balanceOf);
        // expecttoAddress2The balance is0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress2});
        Assert.assertEquals("balance expect 0", "0", balanceOf);

        // transfertoken1 from toAddress0 reach toAddress2
        objects = super.call(prevStateRoot, toAddress0, "transferFrom", new String[]{toAddress0, toAddress2, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // expecttoAddress0The balance is1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // expecttoAddress2The balance is1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress2});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // expecttoAddress2havetoken1
        ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress2, toAddress2, ownerOf);

        // transfertoken1 from toAddress2 reach toAddress0
        objects = super.call(prevStateRoot, toAddress2, "transferFrom", new String[]{toAddress2, toAddress0, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // expecttoAddress0The balance is2
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 2", "2", balanceOf);
        // expecttoAddress2The balance is0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress2});
        Assert.assertEquals("balance expect 0", "0", balanceOf);
        // expecttoAddress0havetoken1
        ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress0, toAddress0, ownerOf);

        // -------------------------------------------------------------------------------------//

        // Expectations fail,token5Not present
        objects = super.call(prevStateRoot, toAddress1, "approve", new String[]{toAddress2, String.valueOf(5)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("expect fail", programResult.isSuccess());

        // Expectations fail,toAddress1Unauthorized authorizationtoken1
        objects = super.call(prevStateRoot, toAddress1, "approve", new String[]{toAddress2, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("expect fail", programResult.isSuccess());

        // toAddress0authorizationtoken1totoAddress2
        objects = super.call(prevStateRoot, toAddress0, "approve", new String[]{toAddress2, String.valueOf(1)});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // expecttoken1Authorized totoAddress2
        ownerOf = super.view(prevStateRoot, "getApproved", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress2, toAddress2, ownerOf);
        // Expectations fail,token5Not present
        ownerOf = super.view(prevStateRoot, "getApproved", new String[]{"5"});
        Assert.assertNull("ownerOf expect null", ownerOf);

        // -------------------------------------------------------------------------------------//

        // expecttoAddress0The balance is2
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 2", "2", balanceOf);
        // expecttoAddress3The balance is0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress3});
        Assert.assertEquals("balance expect 0", "0", balanceOf);

        // Expected failure, using authorized account toAddress2 transfer token1,token1 Not belonging to toAddress1
        objects = super.call(prevStateRoot, toAddress2, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId, String data) return void", new String[]{toAddress1, toAddress3, String.valueOf(1), "remark_data"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse(programResult.isSuccess());
        // Using an authorized account toAddress2 transfer token1 from toAddress0 reach toAddress3
        objects = super.call(prevStateRoot, toAddress2, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId, String data) return void", new String[]{toAddress0, toAddress3, String.valueOf(1), "remark_data"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // expecttoAddress0The balance is1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // expecttoAddress3The balance is1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress3});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // expecttoAddress3havetoken1
        ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress3, toAddress3, ownerOf);
        // expecttoken1Unauthorized account
        ownerOf = super.view(prevStateRoot, "getApproved", new String[]{"1"});
        Assert.assertNull("ownerOf expect null", ownerOf);

        // -------------------------------------------------------------------------------------//

        // Expectations fail,toAddress1 No authorization granted to oneself
        objects = super.call(prevStateRoot, toAddress1, "setApprovalForAll", new String[]{toAddress1, "true"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse("expect fail", programResult.isSuccess());

        // toAddress3 Authorized to toAddress4
        objects = super.call(prevStateRoot, toAddress3, "setApprovalForAll", new String[]{toAddress4, "true"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // expect toAddress3 Authorized to toAddress4
        String bool = super.view(prevStateRoot, "isApprovedForAll", new String[]{toAddress3, toAddress4});
        Assert.assertEquals("result expect true", "true", bool);
        // Expectations fail,toAddress5 Unauthorized
        bool = super.view(prevStateRoot, "isApprovedForAll", new String[]{toAddress3, toAddress5});
        Assert.assertEquals("result expect false", "false", bool);

        // -------------------------------------------------------------------------------------//

        // Expected failure, using authorized account toAddress4 transfer token2,token2 Not belonging to toAddress3
        objects = super.call(prevStateRoot, toAddress4, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId, String data) return void", new String[]{toAddress3, toAddress5, String.valueOf(2), "remark_data"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertFalse(programResult.isSuccess());
        // Using an authorized account toAddress4 transfer token1 from toAddress3 reach toAddress5
        objects = super.call(prevStateRoot, toAddress4, "safeTransferFrom", "(Address from, Address to, BigInteger tokenId, String data) return void", new String[]{toAddress3, toAddress5, String.valueOf(1), "remark_data"});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        // expecttoAddress3The balance is0
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress3});
        Assert.assertEquals("balance expect 0", "0", balanceOf);
        // expecttoAddress5The balance is1
        balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress5});
        Assert.assertEquals("balance expect 1", "1", balanceOf);
        // expecttoAddress5havetoken1
        ownerOf = super.view(prevStateRoot, "ownerOf", new String[]{"1"});
        Assert.assertEquals("ownerOf expect " + toAddress5, toAddress5, ownerOf);
        // expecttoken1Unauthorized account
        ownerOf = super.view(prevStateRoot, "getApproved", new String[]{"1"});
        Assert.assertNull("ownerOf expect null", ownerOf);
        // expect toAddress3 Authorized to toAddress4
        bool = super.view(prevStateRoot, "isApprovedForAll", new String[]{toAddress3, toAddress4});
        Assert.assertEquals("result expect true", "true", bool);
        // Expectations fail,toAddress5 Unauthorized
        bool = super.view(prevStateRoot, "isApprovedForAll", new String[]{toAddress3, toAddress5});
        Assert.assertEquals("result expect false", "false", bool);

        // -------------------------------------------------------------------------------------//

        Log.info("stateRoot: " + HexUtil.encode(prevStateRoot));
        Log.info("\n");
    }

    @Override
    protected void protocolUpdate() {

    }
}
