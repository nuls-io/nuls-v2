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
package io.nuls.contract.mock.contractvm;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.contract.mock.basetest.MockBase;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.parse.JSONUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
/**
 * 测试场景:
 *  1. 单合约测试，修改一个值a=2
 *  期望返回值a=2，调用view方法期望相同的值a=2
 *
 *  2. 单合约测试，同一个方法内，修改一个值a两次a=2，a=3，
 *  期望返回值a=3，调用view方法期望相同的值a=3
 *
 *  3. 单合约测试，方法内，修改值为a=2，方法内调用私有方法，私有方法修改值为a=3，
 *  期望返回值a=3，调用view方法期望相同的值a=3
 *
 *  4. 双合约测试，A合约有一个值a=1，A调用B，执行：B合约调用A合约查询a，使B局部变量t=a, B合约调用A合约修改a=t+5
 *  期望返回值a=6，调用view方法期望a=6
 *
 *  5. 双合约测试，A合约有一个值a=1，A调用B，执行：B合约调用A合约查询a，使B局部变量t1=a, B合约调用A合约修改a=t1+5
 *  B合约调用A合约查询a，使B局部变量t2=a, B合约调用A合约修改a=t2+3
 *  期望返回值a=9，调用view方法期望a=9
 *
 *  6. 双合约测试，A合约有一个值a=1，A调用B，执行：B合约调用A合约查询a，使B局部变量t1=a, B合约调用A合约修改a=t1+5
 *  B合约调用A合约查询a，使B局部变量t2=a, B合约调用A合约修改a=t2+3, B合约返回66给A
 *  A合约修改 a = a + 66 + 1
 *  期望返回值a=76，调用view方法期望a=76
 *
 *  7. 双合约测试，A合约修改一个值a=2，A调用B，执行：B合约调用A合约查询a，B局部变量t1=a，B合约调用A合约修改a=t1+5
 *  B调用A查询a，B局部变量t2=a+1，B返回t2给A，A修改a=t2+a
 *  期望返回值a=15，调用view方法期望a=15
 *
 *  8. 双合约测试，A合约修改一个值a=2，A调用B，执行：B合约调用A合约查询a，B局部变量t1=a，B合约调用A合约修改a=t1+5
 *  B调用A查询a，B局部变量t2=a+1，B合约调用A合约修改a=t2+3, B返回t2给A，A修改a=t2+a
 *  期望返回值a=19，调用view方法期望a=19
 *
 *  9. 双合约测试，A合约有一个值a=1，A调用B查询b=1，A修改a=a+b
 *  期望返回值a=2，调用view方法期望a=2
 *
 *  10. 双合约测试，A合约有一个值a=1，B合约有一个值b=1，A调用B查询b，A修改a=a+b，A调用B修改b=2，A调用B查询b，A修改a=a+b
 *  期望返回值a=4，调用view方法期望a=4, b=2
 *
 *  11. 双合约测试，B有一个值b=1，A调用B修改b=2，A调用B修改b=3
 *  期望返回值b=3，调用view方法期望b=3
 *
 *  12. 双合约测试，A有一个值a=1，B有一个值b=1，A调用B修改b=2，A调用B修改b=3，b返回给A，A修改a=b
 *  期望返回值a=3，调用view方法期望a=3，b=3
 *
 *  13. 双合约测试，A有一个map1，map2, map1[a]值为100，map2[a]值为100, 修改map1[a]值为60，A调用B，执行：B调用A查询map1[a]，发debugEvent，更改成员变量b为map1[a]
 *  B调用A修改map2[a]值为80
 *  期望view方法查询map[1]a值为60，map2[a]为80, b=60
 *
 *  14. 双合约测试，A合约有map1，map1[a]值为1, A调用B，执行：B合约调用A合约查询map1[a]，使B局部变量t=a, B合约调用A合约修改map1[a]=t+5
 *  期望返回值map1[a]=105，调用view方法期望map1[a]=105
 *
 *  //TODO pierre 增加数组类型的成员变量值的单元测试
 * @author: PierreLuo
 * @date: 2019-06-11
 */
public class ContractVmTest extends MockBase {

    String contractA = "tNULSeBaN5xpQLvYBMJuybAzgzRkRXL4r3tqMx";
    String contractB = "tNULSeBaN1gZJobF3bxuLwXxvvAosdwQTVxWFn";
    byte[] prevStateRoot;

    @Before
    public void createAndInit() throws Exception {
        // -------------------------------------------------------------------------------------//
        //InputStream inA = new FileInputStream(getClass().getResource("/contract-vm-testA-testA.jar").getFile());
        //InputStream inB = new FileInputStream(getClass().getResource("/contract-vm-testA-testB.jar").getFile());
        InputStream inA = new FileInputStream("/Users/pierreluo/IdeaProjects/contract-vm-testA/target/contract-vm-testA-testA.jar");
        InputStream inB = new FileInputStream("/Users/pierreluo/IdeaProjects/contract-vm-testB/target/contract-vm-testB-testB.jar");
        byte[] contractCodeA = IOUtils.toByteArray(inA);
        byte[] contractCodeB = IOUtils.toByteArray(inB);

        byte[] initialStateRoot = HexUtil.decode("56e81f171bcc55a6ff8345e692c0f86e5b48e01b996cadc001622fb5e363b421");
        prevStateRoot = super.create(initialStateRoot, contractA, SENDER, contractCodeA);
        Log.info("stateRoot: {}", HexUtil.encode(prevStateRoot));

        prevStateRoot = super.create(prevStateRoot, contractB, SENDER, contractCodeB);
        Log.info("stateRoot: {}", HexUtil.encode(prevStateRoot));

        // ------------------------------initial----------------------------------------------------//
        Object[] objects = super.call(contractA, prevStateRoot, SENDER, "setContractB", new String[]{contractB});
        prevStateRoot = (byte[]) objects[0];
        Log.info("stateRoot: {}", HexUtil.encode(prevStateRoot));
        ProgramResult programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());

        objects = super.call(contractB, prevStateRoot, SENDER, "setContractA", new String[]{contractA});
        prevStateRoot = (byte[]) objects[0];
        Log.info("stateRoot: {}", HexUtil.encode(prevStateRoot));
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());

    }

    @Test
    public void test1() throws Exception{ this.callVmTest(prevStateRoot, "test1", "2"); }
    @Test
    public void test2() throws Exception{ this.callVmTest(prevStateRoot, "test2", "3"); }
    @Test
    public void test3() throws Exception{ this.callVmTest(prevStateRoot, "test3", "3"); }
    @Test
    public void test4() throws Exception{ this.callVmTest(prevStateRoot, "test4", "6"); }
    @Test
    public void test4_integer() throws Exception{
        // 测试方法[test4_integer]View期望a=6, 实际a=1
        byte[] currentStateRoot = this.callVmTest(prevStateRoot, "test4_integer", "6", false);
        String integerValue = super.view(contractA, currentStateRoot, "getIntegerValue", new String[]{});
        Assert.assertTrue(String.format("测试方法[test4_integer]view期望integerValue=6, 实际integerValue=%s", integerValue), "6".equals(integerValue));
    }
    @Test
    public void test4_int() throws Exception{
        // 测试方法[test4_int]View期望a=6, 实际a=1
        byte[] currentStateRoot = this.callVmTest(prevStateRoot, "test4_int", "6", false);
        String intValue = super.view(contractA, currentStateRoot, "getIntValue", new String[]{});
        Assert.assertTrue(String.format("测试方法[test4_int]view期望intValue=6, 实际intValue=%s", intValue), "6".equals(intValue));
    }
    @Test
    public void test5() throws Exception{
        this.callVmTest(prevStateRoot, "test5", "9");
    }
    @Test
    public void test6() throws Exception{
        this.callVmTest(prevStateRoot, "test6", "76");
    }

    @Test
    public void test7() throws Exception{ this.callVmTest(prevStateRoot, "test7", "15"); }

    @Test
    public void test8() throws Exception{ this.callVmTest(prevStateRoot, "test8", "19"); }

    @Test
    public void test9() throws Exception{ this.callVmTest(prevStateRoot, "test9", "2"); }

    @Test
    public void test10() throws Exception{
        byte[] currentStateRoot;
        currentStateRoot = this.callVmTest(prevStateRoot, "test10", "4");
        String b = super.view(contractB, currentStateRoot, "viewB", new String[]{});
        Assert.assertTrue(String.format("测试方法[test10]期望b=2, 实际b=%s", b), "2".equals(b));
    }

    @Test
    public void test11() throws Exception{
        byte[] currentStateRoot;
        String b;
        currentStateRoot = this.callVmTest(prevStateRoot, "test11", "3", false);
        b = super.view(contractB, currentStateRoot, "viewB", new String[]{});
        Assert.assertTrue(String.format("测试方法[test11]期望b=3, 实际b=%s", b), "3".equals(b));
    }

    @Test
    public void test12() throws Exception{
        byte[] currentStateRoot;
        String b;
        currentStateRoot = this.callVmTest(prevStateRoot, "test12", "3");
        b = super.view(contractB, currentStateRoot, "viewB", new String[]{});
        Assert.assertTrue(String.format("测试方法[test12]期望b=3, 实际b=%s", b), "3".equals(b));
    }

    @Test
    public void test13() throws Exception{
        byte[] currentStateRoot;
        String a, b;
        Object[] objects;
        ProgramResult programResult;
        //期望view方法查询map[1]a值为60，map2[a]为80, b=60
        objects = super.call(contractA, prevStateRoot, SENDER, "test13", new String[]{});
        currentStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("测试方法[test13]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        Assert.assertTrue(String.format("测试方法[test13]返回值期望map2a=80, 实际map2a=%s", programResult.getResult()), "80".equals(programResult.getResult()));

        a = super.view(contractA, currentStateRoot, "viewMap1ByKey", new String[]{"a"});
        Assert.assertTrue(String.format("测试方法[test13]View期望map1a=60, 实际map1a=%s", a), "60".equals(a));

        a = super.view(contractA, currentStateRoot, "viewMap2ByKey", new String[]{"a"});
        Assert.assertTrue(String.format("测试方法[test13]View期望map2a=80, 实际map2a=%s", a), "80".equals(a));

        b = super.view(contractB, currentStateRoot, "viewB", new String[]{});
        Assert.assertTrue(String.format("测试方法[test13]期望b=60, 实际b=%s", b), "60".equals(b));
    }

    @Test
    public void test14() throws Exception{
        byte[] currentStateRoot;
        String a;
        Object[] objects;
        ProgramResult programResult;
        //期望返回值map1[a]=105，调用view方法期望map1[a]=105
        objects = super.call(contractA, prevStateRoot, SENDER, "test14", new String[]{});
        currentStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("测试方法[test14]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        Assert.assertTrue(String.format("测试方法[test14]返回值期望map1a=105, 实际map1a=%s", programResult.getResult()), "105".equals(programResult.getResult()));

        a = super.view(contractA, currentStateRoot, "viewMap1ByKey", new String[]{"a"});
        Assert.assertTrue(String.format("测试方法[test14]View期望map1a=105, 实际map1a=%s", a), "105".equals(a));
    }

    private byte[] callVmTest(byte[] prevStateRoot, String method, String expect, boolean containViewExpect) throws Exception {
        Object[] objects;
        ProgramResult programResult;
        //objects = super.call(contractA, prevStateRoot, SENDER, "resetA", new String[]{});
        //prevStateRoot = (byte[]) objects[0];
        //programResult = (ProgramResult) objects[1];
        //Assert.assertTrue("重置方法[resetA]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        //Assert.assertTrue(String.format("重置方法[resetA]期望a=1, 实际a=%s", programResult.getResult()), "1".equals(programResult.getResult()));
        //
        //objects = super.call(contractB, prevStateRoot, SENDER, "resetB", new String[]{});
        //prevStateRoot = (byte[]) objects[0];
        //programResult = (ProgramResult) objects[1];
        //Assert.assertTrue("重置方法[resetB]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        //Assert.assertTrue(String.format("重置方法[resetB]期望b=1, 实际b=%s", programResult.getResult()), "1".equals(programResult.getResult()));

        objects = super.call(contractA, prevStateRoot, SENDER, method, new String[]{});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("测试方法["+method+"]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        Assert.assertTrue(String.format("测试方法[%s]返回值期望a=%s, 实际a=%s", method, expect, programResult.getResult()), expect.equals(programResult.getResult()));

        if(containViewExpect) {
            String a = super.view(contractA, prevStateRoot, "viewA", new String[]{});
            Assert.assertTrue(String.format("测试方法[%s]View期望a=%s, 实际a=%s", method, expect, a), expect.equals(a));
        }
        return prevStateRoot;
    }

    private byte[] callVmTest(byte[] stateRoot, String method, String expect) throws Exception {
        return callVmTest(stateRoot, method, expect, true);
    }

    public void testCall() throws JsonProcessingException {
        byte[] prevStateRoot = HexUtil.decode("52517a7c3889a129cea32531cdc45109a5228d31e51bdfcd55394fa580d11ab4");
        Object[] objects = super.call(prevStateRoot, SENDER, "mint", new String[]{toAddress0, "1"});
        byte[] stateRoot = (byte[]) objects[0];
        ProgramResult programResult = (ProgramResult) objects[1];
        Log.info(JSONUtils.obj2PrettyJson(programResult));
        Log.info("stateRoot: " + HexUtil.encode(stateRoot));
        Log.info("\n");
    }

    public void testView() throws JsonProcessingException {
        byte[] prevStateRoot = HexUtil.decode("54803e34c33c92544ad8846bf4404944ae31e80143e4036359f8c60698bbc167");
        String balanceOf = super.view(prevStateRoot, "balanceOf", new String[]{toAddress0});
        Log.info("view result: " + balanceOf);
    }

    public void testContractMethod() throws Exception {
        InputStream in = new FileInputStream(getClass().getResource("/NRC721Metadata-test.jar").getFile());
        byte[] contractCode = IOUtils.toByteArray(in);
        List<ProgramMethod> programMethods = programExecutor.jarMethod(contractCode);
        Log.info("\n");
        Log.info(JSONUtils.obj2PrettyJson(programMethods));
        Log.info("\n");
    }

    private void testFull() throws Exception {

        //byte[] currentStateRoot;
        //String a;
        // ------------------------------test1----------------------------------------------------//
        //this.callVmTest(prevStateRoot, "test5", "9");// 失败 tNULSeBaN5xpQLvYBMJuybAzgzRkRXL4r3tqMx
        //this.callVmTest(prevStateRoot, "test6", "76");// 失败
        //this.callVmTest(prevStateRoot, "test7", "15");
        //this.callVmTest(prevStateRoot, "test8", "19");

        //this.callVmTest(prevStateRoot, "test4_integer", "6");// 失败 1
        //this.callVmTest(prevStateRoot, "test4_int", "6");// 失败 1
        //this.callVmTest(prevStateRoot, "test1", "2");
        //this.callVmTest(prevStateRoot, "test2", "3");
        //this.callVmTest(prevStateRoot, "test3", "3");
        //this.callVmTest(prevStateRoot, "test4", "6");
        //this.callVmTest(prevStateRoot, "test9", "2");
        //// ------------------------------test10----------------------------------------------------//
        //currentStateRoot = this.callVmTest(prevStateRoot, "test10", "4");
        //String b = super.view(contractB, currentStateRoot, "viewB", new String[]{});
        //Assert.assertTrue(String.format("测试方法[test10]期望b=2, 实际b=%s", b), "2".equals(b));
        //// ------------------------------test11----------------------------------------------------//
        //currentStateRoot = this.callVmTest(prevStateRoot, "test11", "3", false);
        //b = super.view(contractB, currentStateRoot, "viewB", new String[]{});
        //Assert.assertTrue(String.format("测试方法[test11]期望b=3, 实际b=%s", b), "3".equals(b));
        //// ------------------------------test12----------------------------------------------------//
        //currentStateRoot = this.callVmTest(prevStateRoot, "test12", "3");
        //b = super.view(contractB, currentStateRoot, "viewB", new String[]{});
        //Assert.assertTrue(String.format("测试方法[test12]期望b=3, 实际b=%s", b), "3".equals(b));
        //// ------------------------------test13----------------------------------------------------//
        ////期望view方法查询map[1]a值为60，map2[a]为80, b=60
        //objects = super.call(contractA, prevStateRoot, SENDER, "test13", new String[]{});
        //currentStateRoot = (byte[]) objects[0];
        //programResult = (ProgramResult) objects[1];
        //Assert.assertTrue("测试方法[test13]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        //Assert.assertTrue(String.format("测试方法[test13]返回值期望map2a=80, 实际map2a=%s", programResult.getResult()), "80".equals(programResult.getResult()));
        //
        //a = super.view(contractA, currentStateRoot, "viewMap1ByKey", new String[]{"a"});
        //Assert.assertTrue(String.format("测试方法[test13]View期望map1a=60, 实际map1a=%s", a), "60".equals(a));
        //
        //a = super.view(contractA, currentStateRoot, "viewMap2ByKey", new String[]{"a"});
        //Assert.assertTrue(String.format("测试方法[test13]View期望map2a=80, 实际map2a=%s", a), "80".equals(a));
        //
        //b = super.view(contractB, currentStateRoot, "viewB", new String[]{});
        //Assert.assertTrue(String.format("测试方法[test13]期望b=60, 实际b=%s", b), "60".equals(b));
        //// ------------------------------test14----------------------------------------------------//
        ////期望返回值map1[a]=105，调用view方法期望map1[a]=105
        //objects = super.call(contractA, prevStateRoot, SENDER, "test14", new String[]{});
        //currentStateRoot = (byte[]) objects[0];
        //programResult = (ProgramResult) objects[1];
        //Assert.assertTrue("测试方法[test14]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        //Assert.assertTrue(String.format("测试方法[test14]返回值期望map1a=105, 实际map1a=%s", programResult.getResult()), "105".equals(programResult.getResult()));
        //
        //a = super.view(contractA, currentStateRoot, "viewMap1ByKey", new String[]{"a"});
        //Assert.assertTrue(String.format("测试方法[test14]View期望map1a=105, 实际map1a=%s", a), "105".equals(a));
        //// -------------------------------------------------------------------------------------//
    }
}
