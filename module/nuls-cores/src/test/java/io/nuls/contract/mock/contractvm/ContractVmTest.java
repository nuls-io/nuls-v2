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
import io.nuls.base.basic.AddressTool;
import io.nuls.base.protocol.ProtocolGroupManager;
import io.nuls.contract.config.ContractContext;
import io.nuls.contract.mock.basetest.MockBase;
import io.nuls.contract.util.Log;
import io.nuls.contract.vm.VMFactory;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.contract.vm.program.ProgramResult;
import io.nuls.contract.vm.program.ProgramTransfer;
import io.nuls.core.crypto.HexUtil;
import io.nuls.core.model.StringUtils;
import io.nuls.core.parse.JSONUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.util.List;

/**
 * Test scenario:
 *  1. Single contract testing, modifying a valuea=2
 *  Expected return valuea=2, callingviewMethod expects the same valuea=2
 *
 *  2. Single contract testing, modifying a value within the same methodaTwicea=2,a=3,
 *  Expected return valuea=3, callingviewMethod expects the same valuea=3
 *
 *  3. Single contract testing, within the method, modify the value toa=2Calling private methods within a method, modifying the value of the private method toa=3,
 *  Expected return valuea=3, callingviewMethod expects the same valuea=3
 *
 *  4. Dual contract testing,AThe contract has a valuea=1,AcallB, execute：BContract CallAContract inquirya, makeBlocal variablet=a, BContract CallAContract modificationa=t+5
 *  Expected return valuea=6, callingviewMethod Expectationsa=6
 *
 *  5. Dual contract testing,AThe contract has a valuea=1,AcallB, execute：BContract CallAContract inquirya, makeBlocal variablet1=a, BContract CallAContract modificationa=t1+5
 *  BContract CallAContract inquirya, makeBlocal variablet2=a, BContract CallAContract modificationa=t2+3
 *  Expected return valuea=9, callingviewMethod Expectationsa=9
 *
 *  6. Dual contract testing,AThe contract has a valuea=1,AcallB, execute：BContract CallAContract inquirya, makeBlocal variablet1=a, BContract CallAContract modificationa=t1+5
 *  BContract CallAContract inquirya, makeBlocal variablet2=a, BContract CallAContract modificationa=t2+3, BContract return66toA
 *  AContract modification a = a + 66 + 1
 *  Expected return valuea=76, callingviewMethod Expectationsa=76
 *
 *  7. Dual contract testing,AContract modification of a valuea=2,AcallB, execute：BContract CallAContract inquirya,Blocal variablet1=a,BContract CallAContract modificationa=t1+5
 *  BcallAquerya,Blocal variablet2=a+1,Breturnt2toA,Amodifya=t2+a
 *  Expected return valuea=15, callingviewMethod Expectationsa=15
 *
 *  8. Dual contract testing,AContract modification of a valuea=2,AcallB, execute：BContract CallAContract inquirya,Blocal variablet1=a,BContract CallAContract modificationa=t1+5
 *  BcallAquerya,Blocal variablet2=a+1,BContract CallAContract modificationa=t2+3, Breturnt2toA,Amodifya=t2+a
 *  Expected return valuea=19, callingviewMethod Expectationsa=19
 *
 *  9. Dual contract testing,AThe contract has a valuea=1,AcallBqueryb=1,Amodifya=a+b
 *  Expected return valuea=2, callingviewMethod Expectationsa=2
 *
 *  10. Dual contract testing,AThe contract has a valuea=1,BThe contract has a valueb=1,AcallBqueryb,Amodifya=a+b,AcallBmodifyb=2,AcallBqueryb,Amodifya=a+b
 *  Expected return valuea=4, callingviewMethod Expectationsa=4, b=2
 *
 *  11. Dual contract testing,BThere is a valueb=1,AcallBmodifyb=2,AcallBmodifyb=3
 *  Expected return valueb=3, callingviewMethod Expectationsb=3
 *
 *  12. Dual contract testing,AThere is a valuea=1,BThere is a valueb=1,AcallBmodifyb=2,AcallBmodifyb=3,bReturn toA,Amodifya=b
 *  Expected return valuea=3, callingviewMethod Expectationsa=3,b=3
 *
 *  13. Dual contract testing,AThere is onemap1,map2, map1[a]Value is100,map2[a]Value is100, modifymap1[a]Value is60,AcallB, execute：BcallAquerymap1[a], senddebugEventChange member variablesbbymap1[a]
 *  BcallAmodifymap2[a]Value is80
 *  expectviewMethod Querymap[1]aValue is60,map2[a]by80, b=60
 *
 *  14. Dual contract testing,AThe contract includesmap1,map1[a]Value is1, AcallB, execute：BContract CallAContract inquirymap1[a], makeBlocal variablet=a, BContract CallAContract modificationmap1[a]=t+5
 *  Expected return valuemap1[a]=105, callingviewMethod Expectationsmap1[a]=105
 *
 *  15. Dual contract testing, caller toAContract transfer100,AcallBTransfer in100,Bapply30Transfer70To the caller
 *  In the expected execution result, there is a return to the caller70
 *
 *  16. Single contract testing,AThere is oneintArray variable, length of3, value is[0, 1, 2]Modify the second element of the array to8
 *                  There is oneStringArray variable, length of3, value is["a", "b", "c"]Modify the second element of the array tof
 *                  There is oneIntegerArray variable, length of4, value is[5, 6, 7, 8]Modify the first element of the array to2
 *                  There is onedoubleArray variable, length of5, Value is[2.3, 4.5, 6.7, 8.9, 10.11]Modify the fifth element to12.13
 *  Expected return valuea=082 afc 2678 2.34.56.78.912.13, callingviewMethod Expectationsa=a82 afc 2678 2.34.56.78.912.13
 *
 *  17. Single contract testing,AThere is oneintArray variable, length of3, value is[0, 1, 2]Modify the second element of the array to8, then modify to7
 *                  There is oneStringArray variable, length of3, value is["a", "b", "c"]Modify the second element of the array tof, then modify tog
 *                  There is oneIntegerArray variable, length of4, value is[5, 6, 7, 8]Modify the first element of the array to2, then modify to1
 *                  There is onedoubleArray variable, length of5, Value is[2.3, 4.5, 6.7, 8.9, 10.11]Modify the fifth element to12.13, then modify to11.13
 *  Expected return valuea=072 agc 1678 2.34.56.78.911.13, callingviewMethod Expectationsa=a72 agc 1678 2.34.56.78.911.13
 *
 *  18. Single contract testing,AThere is oneintArray variable, length of3, value is[0, 1, 2]Modify the second element of the array to8Modify the private method to7
 *                  There is oneStringArray variable, length of3, value is["a", "b", "c"]Modify the second element of the array tof, then modify tog
 *                  There is oneIntegerArray variable, length of4, value is[5, 6, 7, 8]Modify the first element of the array to2, modify to1
 *                  There is onedoubleArray variable, length of5, Value is[2.3, 4.5, 6.7, 8.9, 10.11]Modify the fifth element to12.13, then modify to11.13
 *  Expected return valuea=072 agc 1678 2.34.56.78.911.13, callingviewMethod Expectationsa=a72 agc 1678 2.34.56.78.911.13
 *
 *  19. Dual contract testing,AThere is oneintArray variable, length of3, value is[0, 1, 2],
 *                  There is oneStringArray variable, length of3, value is["a", "b", "c"],
 *                  There is oneIntegerArray variable, length of4, value is[5, 6, 7, 8],
 *                  There is onedoubleArray variable, length of5, Value is[2.3, 4.5, 6.7, 8.9, 10.11],
 *         AcallBimplement：BcallA, modifyintThe second element of the array is8Modify the private method to7
 *                          modifyStringThe second element of the array isf, then modify tog
 *                          modifyIntegerThe first element of the array is2, modify to1
 *                          modifydoubleThe fifth element of the array is12.13, then modify to11.13
 *  Expected return valuea=072 agc 1678 2.34.56.78.911.13, callingviewMethod Expectationsa=a72 agc 1678 2.34.56.78.911.13
 *
 *  20. Dual contract testing,AThere is oneintArray variable, length of3, value is[0, 1, 2]Modify the second element of the array to8
 *                  There is oneStringArray variable, length of3, value is["a", "b", "c"]Modify the second element of the array tof
 *                  There is oneIntegerArray variable, length of4, value is[5, 6, 7, 8]Modify the first element of the array to2
 *                  There is onedoubleArray variable, length of5, Value is[2.3, 4.5, 6.7, 8.9, 10.11]Modify the fifth element to12.13
 *         AcallBimplement：BcallA, Queryt1=int[1], t2=String[1], t3=Integer[0], t4=double[4]
 *                   BcallAModify the following data
 *                          modifyintThe second element of the array is int[1] + t1  ->(8 + 8)
 *                          modifyStringThe third element of the array is String[2] + t2  ->("c" + "f")
 *                          modifyIntegerThe second element of the array is Integer[0] + t3  ->(2 + 2)
 *                          modifydoubleThe third element of the array is double[1] + t4  ->(4.5 + 12.13)
 *  Expected return valuea=0162 afcf 2478 2.34.516.638.912.13, callingviewMethod Expectationsa=0162 afcf 2478 2.34.516.638.912.13
 *
 *  21. Dual contract testing,AThere is oneintArray variable, length of3, value is[0, 1, 2]Modify the second element of the array to8
 *                  There is oneStringArray variable, length of3, value is["a", "b", "c"]Modify the second element of the array tof
 *                  There is oneIntegerArray variable, length of4, value is[5, 6, 7, 8]Modify the first element of the array to2
 *                  There is onedoubleArray variable, length of5, Value is[2.3, 4.5, 6.7, 8.9, 10.11]Modify the fifth element to12.13
 *         AcallBimplement：BcallA, Queryt1=int[1], t2=String[1], t3=Integer[0], t4=double[4]
 *                   BcallAModify the following data
 *                          modifyintThe second element of the array is int[1] + t1  ->(8 + 8)
 *                          modifyStringThe third element of the array is String[2] + t2  ->("c" + "f")
 *                          modifyIntegerThe second element of the array is Integer[0] + t3  ->(2 + 2)
 *                          modifydoubleThe third element of the array is double[1] + t4  ->(4.5 + 12.13)
 *         herea=0162 afcf 2478 2.34.516.638.912.13
 *         AModify the following data
 *                  modifyintThe second element of the array is7
 *                  modifyStringThe second element of the array isg
 *                  modifyIntegerThe first element of the array is1
 *                  modifydoubleThe fifth element of the array is11.13
 *  Expected return valuea=072 agcf 1478 2.34.516.638.911.13, callingviewMethod Expectationsa=072 agcf 1478 2.34.516.638.911.13
 *
 *  22. Dual contract testing,AThere is oneintArray variable, length of3, value is[0, 1, 2]Modify the second element of the array to8
 *                  There is oneStringArray variable, length of3, value is["a", "b", "c"]Modify the second element of the array tof
 *                  There is oneIntegerArray variable, length of4, value is[5, 6, 7, 8]Modify the first element of the array to2
 *                  There is onedoubleArray variable, length of5, Value is[2.3, 4.5, 6.7, 8.9, 10.11]Modify the fifth element to12.13
 *                  0,8,2| a,f,c| 2,6,7,8| 2.3,4.5,6.7,8.9,12.13
 *         AcallBimplement：BcallA, Queryt1=int[1], t2=String[1], t3=Integer[0], t4=double[4]
 *                   BcallAModify the following data
 *                          modifyintThe second element of the array is int[1] + t1  ->(8 + 8)
 *                          modifyStringThe third element of the array is String[2] + t2  ->("c" + "f")
 *                          modifyIntegerThe second element of the array is Integer[0] + t3  ->(2 + 2)
 *                          modifydoubleThe third element of the array is double[1] + t4  ->(4.5 + 12.13)
 *                   At this point, the array values are sequentially
 *                   0,16,2| a,f,cf| 2,4,7,8| 2.3,4.5,16.63,8.9,12.13
 *                   BcallA, Queryy1=int[2], y2=String[2], y3=Integer[2], y4=double[2]
 *                   BcallAModify the following data
 *                          modifyintThe third element of the array is int[1] + y1  ->(16 + 2)
 *                          modifyStringThe first element of the array is String[0] + y2  ->("a" + "cf")
 *                          modifyIntegerThe third element of the array is Integer[1] + y3  ->(4 + 7)
 *                          modifydoubleThe fourth element of the array is double[1] + y4  ->(4.5 + 16.63)
 *                   B Member variables B-intarray[t1, y1]  ->8, 2
 *                             B-Stringarray[t2, y2]  -> "f", "cf"
 *                             B-Integerarray[t3, y3]  -> 2, 7
 *                             B-doublearray[t4, y4]  -> 12.13, 16.63
 *  Expected return valuea=01618 acffcf 24118 2.34.516.6321.1312.13|82 fcf 27 12.1316.63
 *  callviewMethod Expectationsa=01618 acffcf 24118 2.34.516.6321.1312.13
 *  callviewMethod Expectationsb=82 fcf 27 12.1316.63
 *
 *  23. Dual contract testing,AThere is oneintArray variable, length of3, value is[0, 1, 2],
 *                  There is oneStringArray variable, length of3, value is["a", "b", "c"],
 *                  There is oneIntegerArray variable, length of4, value is[5, 6, 7, 8],
 *                  There is onedoubleArray variable, length of5, Value is[2.3, 4.5, 6.7, 8.9, 10.11],
 *                BThere is oneintArray variable, length of2, value is[10, 11],
 *                  There is oneStringArray variable, length of2, value is["qa", "qb"],
 *                  There is oneIntegerArray variable, length of2, value is[25, 26],
 *                  There is onedoubleArray variable, length of2, Value is[32.3, 34.5],
 *         AcallB, Queryt1=B-int[1], t2=B-String[1], t3=B-Integer[1], t4=B-double[1]
 *         AModify the following data
 *                  modifyintThe second element of the array is int[1] + t1  ->(1 + 11)
 *                  modifyStringThe third element of the array is String[2] + t2  ->("c" + "qb")
 *                  modifyIntegerThe second element of the array is Integer[0] + t3  ->(5 + 26)
 *                  modifydoubleThe third element of the array is double[1] + t4  ->(4.5 + 34.5)
 *         hereAThe data is
 *         0,12,2 a,b,cqb 5,31,7,8 2.3,4.5,39,8.9,10.11
 *         AcallBimplement：Modify the following data
 *                          modifyB-int[0]by 2
 *                          modifyB-String[0]by 2
 *                          modifyB-Integer[0]by 2
 *                          modifyB-double[0]by 2
 *                   hereBThe array values are sequentially
 *                   211 2qb 226 234.5
 *         AcallB, Queryy1=B-int[0], y2=B-String[0], y3=B-Integer[0], y4=B-double[0]
 *         AModify the following data
 *                  modifyintThe second element of the array is int[1] + y1  ->(12 + 2)
 *                  modifyStringThe third element of the array is String[2] + y2  ->("cqb" + "2")
 *                  modifyIntegerThe second element of the array is Integer[0] + y3  ->(5 + 2)
 *                  modifydoubleThe third element of the array is double[1] + y4  ->(4.5 + 2)
 *  Expected return valuea=0142 abcqb2 5778 2.34.56.58.910.11|211 2qb 226 234.5
 *  callviewMethod Expectationsa=0142 abcqb2 5778 2.34.56.58.910.11
 *  callviewMethod Expectationsb=211 2qb 226 234.5
 *
 *  24. Dual contract testing,AThere is oneintArray variable, length of3, value is[0, 1, 2],
 *                  There is oneStringArray variable, length of3, value is["a", "b", "c"],
 *                  There is oneIntegerArray variable, length of4, value is[5, 6, 7, 8],
 *                  There is onedoubleArray variable, length of5, Value is[2.3, 4.5, 6.7, 8.9, 10.11],
 *                BThere is oneintArray variable, length of2, value is[10, 11],
 *                  There is oneStringArray variable, length of2, value is["qa", "qb"],
 *                  There is oneIntegerArray variable, length of2, value is[25, 26],
 *                  There is onedoubleArray variable, length of2, Value is[32.3, 34.5],
 *         AcallBimplement：Modify the following data
 *                          modifyB-int[0]by 2
 *                          modifyB-String[0]by 2
 *                          modifyB-Integer[0]by 2
 *                          modifyB-double[0]by 2
 *                   hereBThe array values are sequentially
 *                   211 2qb 226 234.5
 *         AcallBimplement：Modify the following data
 *                          modifyB-int[0]by 3
 *                          modifyB-String[0]by 3
 *                          modifyB-Integer[0]by 3
 *                          modifyB-double[0]by 3
 *                   hereBThe array values are sequentially
 *                   311 3qb 326 334.5
 *         ASplit return value`311 3qb 326 334.5`Obtain
 *                      y1=311, y2="3qb", y3=326, y4=334.5
 *         AModify the following data
 *                  modifyintThe second element of the array is int[1] + y1  ->(1 + 311)
 *                  modifyStringThe third element of the array is String[2] + y2  ->("c" + "3qb")
 *                  modifyIntegerThe second element of the array is Integer[0] + y3  ->(5 + 326)
 *                  modifydoubleThe third element of the array is double[1] + y4  ->(4.5 + 334.5)
 *  Expected return valuea=03122 abc3qb 533178 2.34.5339.08.910.11|311 3qb 326 334.5
 *  callviewMethod Expectationsa=03122 abc3qb 533178 2.34.5339.08.910.11
 *  callviewMethod Expectationsb=311 3qb 326 334.5
 *
 *
 * @author: PierreLuo
 * @date: 2019-06-11
 */
public class ContractVmTest extends MockBase {

    String contractA = "tNULSeBaN5xpQLvYBMJuybAzgzRkRXL4r3tqMx";
    String contractB = "tNULSeBaN1gZJobF3bxuLwXxvvAosdwQTVxWFn";
    byte[] prevStateRoot;

    @Override
    protected void protocolUpdate() {
        short version = 5;
        ProtocolGroupManager.setLoadProtocol(false);
        ProtocolGroupManager.updateProtocol(chainId, version);
    }

    @Before
    public void createAndInit() throws Exception {
        // -------------------------------------------------------------------------------------//
        InputStream inA = new FileInputStream(getClass().getResource("/contract-vm-testA-testA.jar").getFile());
        InputStream inB = new FileInputStream(getClass().getResource("/contract-vm-testB-testB.jar").getFile());
        //InputStream inA = new FileInputStream("/Users/pierreluo/IdeaProjects/contract-vm-testA/target/contract-vm-testA-testA.jar");
        //InputStream inB = new FileInputStream("/Users/pierreluo/IdeaProjects/contract-vm-testB/target/contract-vm-testB-testB.jar");
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
    public void test() {
        System.out.println(ProtocolGroupManager.getCurrentVersion(chainId));
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
        byte[] currentStateRoot = this.callVmTest(prevStateRoot, "test4_integer", "6", false);
        String integerValue = super.view(contractA, currentStateRoot, "getIntegerValue", new String[]{});
        Assert.assertTrue(String.format("test method[test4_integer]viewexpectintegerValue=6, actualintegerValue=%s", integerValue), "6".equals(integerValue));
    }
    @Test
    public void test4_int() throws Exception{
        byte[] currentStateRoot = this.callVmTest(prevStateRoot, "test4_int", "6", false);
        String intValue = super.view(contractA, currentStateRoot, "getIntValue", new String[]{});
        Assert.assertTrue(String.format("test method[test4_int]viewexpectintValue=6, actualintValue=%s", intValue), "6".equals(intValue));
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
        Assert.assertTrue(String.format("test method[test10]expectb=2, actualb=%s", b), "2".equals(b));
    }

    @Test
    public void test11() throws Exception{
        byte[] currentStateRoot;
        String b;
        currentStateRoot = this.callVmTest(prevStateRoot, "test11", "3", false);
        b = super.view(contractB, currentStateRoot, "viewB", new String[]{});
        Assert.assertTrue(String.format("test method[test11]expectb=3, actualb=%s", b), "3".equals(b));
    }

    @Test
    public void test12() throws Exception{
        byte[] currentStateRoot;
        String b;
        currentStateRoot = this.callVmTest(prevStateRoot, "test12", "3");
        b = super.view(contractB, currentStateRoot, "viewB", new String[]{});
        Assert.assertTrue(String.format("test method[test12]expectb=3, actualb=%s", b), "3".equals(b));
    }

    @Test
    public void test13() throws Exception{
        byte[] currentStateRoot;
        String a, b;
        Object[] objects;
        ProgramResult programResult;
        //expectviewMethod Querymap[1]aValue is60,map2[a]by80, b=60
        objects = super.call(contractA, prevStateRoot, SENDER, "test13", new String[]{});
        currentStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("test method[test13]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        Assert.assertTrue(String.format("test method[test13]Expected return valuemap2a=80, actualmap2a=%s", programResult.getResult()), "80".equals(programResult.getResult()));

        a = super.view(contractA, currentStateRoot, "viewMap1ByKey", new String[]{"a"});
        Assert.assertTrue(String.format("test method[test13]Viewexpectmap1a=60, actualmap1a=%s", a), "60".equals(a));

        a = super.view(contractA, currentStateRoot, "viewMap2ByKey", new String[]{"a"});
        Assert.assertTrue(String.format("test method[test13]Viewexpectmap2a=80, actualmap2a=%s", a), "80".equals(a));

        b = super.view(contractB, currentStateRoot, "viewB", new String[]{});
        Assert.assertTrue(String.format("test method[test13]expectb=60, actualb=%s", b), "60".equals(b));
    }

    @Test
    public void test14() throws Exception{
        byte[] currentStateRoot;
        String a;
        Object[] objects;
        ProgramResult programResult;
        //Expected return valuemap1[a]=105, callingviewMethod Expectationsmap1[a]=105
        objects = super.call(contractA, prevStateRoot, SENDER, "test14", new String[]{});
        currentStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("test method[test14]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        Assert.assertTrue(String.format("test method[test14]Expected return valuemap1a=105, actualmap1a=%s", programResult.getResult()), "105".equals(programResult.getResult()));

        a = super.view(contractA, currentStateRoot, "viewMap1ByKey", new String[]{"a"});
        Assert.assertTrue(String.format("test method[test14]Viewexpectmap1a=105, actualmap1a=%s", a), "105".equals(a));
    }

    @Test
    public void test15() throws Exception{
        byte[] currentStateRoot;
        String a;
        Object[] objects;
        ProgramResult programResult;
        objects = super.call(contractA, prevStateRoot, SENDER, "test15", new String[]{}, BigInteger.valueOf(100L));
        currentStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("test method[test15]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());

        List<ProgramTransfer> transfers = programResult.getTransfers();
        boolean success = false;
        for(ProgramTransfer transfer : transfers) {
            String from = AddressTool.getStringAddressByBytes(transfer.getFrom());
            String to = AddressTool.getStringAddressByBytes(transfer.getTo());
            Log.info("transfer from: {}, to: {}, value: {}", from, to, transfer.getValue().toString());
            if(from.equals(contractB) && to.equals(SENDER) && transfer.getValue().longValue() == 70L) {
                success = true;
                break;
            }
        }
        Assert.assertTrue("test method[test15]expect return70", success);
    }

    @Test
    public void test16() throws Exception{
        this.callVmTest(prevStateRoot, "test16", "082 afc 2678 2.34.56.78.912.13", "arrayContact");
    }

    @Test
    public void test17() throws Exception{
        this.callVmTest(prevStateRoot, "test17", "072 agc 1678 2.34.56.78.911.13", "arrayContact");
    }

    @Test
    public void test18() throws Exception{
        this.callVmTest(prevStateRoot, "test18", "072 agc 1678 2.34.56.78.911.13", "arrayContact");
    }

    @Test
    public void test19() throws Exception{
        this.callVmTest(prevStateRoot, "test19", "072 agc 1678 2.34.56.78.911.13", "arrayContact");
    }

    @Test
    public void test20() throws Exception{
        this.callVmTest(prevStateRoot, "test20", "0162 afcf 2478 2.34.516.638.912.13", "arrayContact");
    }

    @Test
    public void test21() throws Exception{
        this.callVmTest(prevStateRoot, "test21", "072 agcf 1478 2.34.516.638.911.13", "arrayContact");
    }

    @Test
    public void test22() throws Exception{
        byte[] currentStateRoot = this.callVmTest(prevStateRoot, "test22", "01618 acffcf 24118 2.34.516.6321.1312.13|82 fcf 27 12.1316.63", false);
        String a = super.view(contractA, currentStateRoot, "arrayContact", new String[]{});
        Assert.assertTrue(String.format("test method[test22]viewexpecta=\"01618 acffcf 24118 2.34.516.6321.1312.13\", actuala=%s", a), "01618 acffcf 24118 2.34.516.6321.1312.13".equals(a));
        String b = super.view(contractB, currentStateRoot, "arrayContact", new String[]{});
        Assert.assertTrue(String.format("test method[test22]viewexpectb=\"82 fcf 27 12.1316.63\", actualb=%s", b), "82 fcf 27 12.1316.63".equals(b));
    }

    @Test
    public void test23() throws Exception{
        byte[] currentStateRoot = this.callVmTest(prevStateRoot, "test23", "0142 abcqb2 5778 2.34.56.58.910.11|211 2qb 226 234.5", false);
        String a = super.view(contractA, currentStateRoot, "arrayContact", new String[]{});
        Assert.assertTrue(String.format("test method[test23]viewexpecta=\"0142 abcqb2 5778 2.34.56.58.910.11\", actuala=%s", a), "0142 abcqb2 5778 2.34.56.58.910.11".equals(a));
        String b = super.view(contractB, currentStateRoot, "arrayContact", new String[]{});
        Assert.assertTrue(String.format("test method[test23]viewexpectb=\"211 2qb 226 234.5\", actualb=%s", b), "211 2qb 226 234.5".equals(b));
    }

    @Test
    public void test24() throws Exception{
        byte[] currentStateRoot = this.callVmTest(prevStateRoot, "test24", "03122 abc3qb 533178 2.34.5339.08.910.11|311 3qb 326 334.5", false);
        String a = super.view(contractA, currentStateRoot, "arrayContact", new String[]{});
        Assert.assertTrue(String.format("test method[test24]viewexpecta=\"03122 abc3qb 533178 2.34.5339.08.910.11\", actuala=%s", a), "03122 abc3qb 533178 2.34.5339.08.910.11".equals(a));
        String b = super.view(contractB, currentStateRoot, "arrayContact", new String[]{});
        Assert.assertTrue(String.format("test method[test24]viewexpectb=\"311 3qb 326 334.5\", actualb=%s", b), "311 3qb 326 334.5".equals(b));
    }

    private byte[] callVmTest(byte[] prevStateRoot, String method, String expect, String viewMethod) throws Exception {
        Object[] objects;
        ProgramResult programResult;
        //objects = super.call(contractA, prevStateRoot, SENDER, "resetA", new String[]{});
        //prevStateRoot = (byte[]) objects[0];
        //programResult = (ProgramResult) objects[1];
        //Assert.assertTrue("Reset method[resetA]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        //Assert.assertTrue(String.format("Reset method[resetA]expecta=1, actuala=%s", programResult.getResult()), "1".equals(programResult.getResult()));
        //
        //objects = super.call(contractB, prevStateRoot, SENDER, "resetB", new String[]{});
        //prevStateRoot = (byte[]) objects[0];
        //programResult = (ProgramResult) objects[1];
        //Assert.assertTrue("Reset method[resetB]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        //Assert.assertTrue(String.format("Reset method[resetB]expectb=1, actualb=%s", programResult.getResult()), "1".equals(programResult.getResult()));

        objects = super.call(contractA, prevStateRoot, SENDER, method, new String[]{});
        prevStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("test method["+method+"]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        Assert.assertTrue(String.format("test method[%s]Expected return valuea=%s, actuala=%s", method, expect, programResult.getResult()), expect.equals(programResult.getResult()));

        if(StringUtils.isNotBlank(viewMethod)) {
            String a = super.view(contractA, prevStateRoot, viewMethod, new String[]{});
            Assert.assertTrue(String.format("test method[%s]Viewexpecta=%s, actuala=%s", method, expect, a), expect.equals(a));
        }
        return prevStateRoot;
    }

    private byte[] callVmTest(byte[] stateRoot, String method, String expect) throws Exception {
        return callVmTest(stateRoot, method, expect, "viewA");
    }

    private byte[] callVmTest(byte[] stateRoot, String method, String expect, boolean containViewExpect) throws Exception {
        String viewMethod = null;
        if(containViewExpect) {
            viewMethod = "viewA";
        }
        return callVmTest(stateRoot, method, expect, viewMethod);
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
        //this.callVmTest(prevStateRoot, "test5", "9");// fail tNULSeBaN5xpQLvYBMJuybAzgzRkRXL4r3tqMx
        //this.callVmTest(prevStateRoot, "test6", "76");// fail
        //this.callVmTest(prevStateRoot, "test7", "15");
        //this.callVmTest(prevStateRoot, "test8", "19");

        //this.callVmTest(prevStateRoot, "test4_integer", "6");// fail 1
        //this.callVmTest(prevStateRoot, "test4_int", "6");// fail 1
        //this.callVmTest(prevStateRoot, "test1", "2");
        //this.callVmTest(prevStateRoot, "test2", "3");
        //this.callVmTest(prevStateRoot, "test3", "3");
        //this.callVmTest(prevStateRoot, "test4", "6");
        //this.callVmTest(prevStateRoot, "test9", "2");
        //// ------------------------------test10----------------------------------------------------//
        //currentStateRoot = this.callVmTest(prevStateRoot, "test10", "4");
        //String b = super.view(contractB, currentStateRoot, "viewB", new String[]{});
        //Assert.assertTrue(String.format("test method[test10]expectb=2, actualb=%s", b), "2".equals(b));
        //// ------------------------------test11----------------------------------------------------//
        //currentStateRoot = this.callVmTest(prevStateRoot, "test11", "3", false);
        //b = super.view(contractB, currentStateRoot, "viewB", new String[]{});
        //Assert.assertTrue(String.format("test method[test11]expectb=3, actualb=%s", b), "3".equals(b));
        //// ------------------------------test12----------------------------------------------------//
        //currentStateRoot = this.callVmTest(prevStateRoot, "test12", "3");
        //b = super.view(contractB, currentStateRoot, "viewB", new String[]{});
        //Assert.assertTrue(String.format("test method[test12]expectb=3, actualb=%s", b), "3".equals(b));
        //// ------------------------------test13----------------------------------------------------//
        ////expectviewMethod Querymap[1]aValue is60,map2[a]by80, b=60
        //objects = super.call(contractA, prevStateRoot, SENDER, "test13", new String[]{});
        //currentStateRoot = (byte[]) objects[0];
        //programResult = (ProgramResult) objects[1];
        //Assert.assertTrue("test method[test13]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        //Assert.assertTrue(String.format("test method[test13]Expected return valuemap2a=80, actualmap2a=%s", programResult.getResult()), "80".equals(programResult.getResult()));
        //
        //a = super.view(contractA, currentStateRoot, "viewMap1ByKey", new String[]{"a"});
        //Assert.assertTrue(String.format("test method[test13]Viewexpectmap1a=60, actualmap1a=%s", a), "60".equals(a));
        //
        //a = super.view(contractA, currentStateRoot, "viewMap2ByKey", new String[]{"a"});
        //Assert.assertTrue(String.format("test method[test13]Viewexpectmap2a=80, actualmap2a=%s", a), "80".equals(a));
        //
        //b = super.view(contractB, currentStateRoot, "viewB", new String[]{});
        //Assert.assertTrue(String.format("test method[test13]expectb=60, actualb=%s", b), "60".equals(b));
        //// ------------------------------test14----------------------------------------------------//
        ////Expected return valuemap1[a]=105, callingviewMethod Expectationsmap1[a]=105
        //objects = super.call(contractA, prevStateRoot, SENDER, "test14", new String[]{});
        //currentStateRoot = (byte[]) objects[0];
        //programResult = (ProgramResult) objects[1];
        //Assert.assertTrue("test method[test14]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());
        //Assert.assertTrue(String.format("test method[test14]Expected return valuemap1a=105, actualmap1a=%s", programResult.getResult()), "105".equals(programResult.getResult()));
        //
        //a = super.view(contractA, currentStateRoot, "viewMap1ByKey", new String[]{"a"});
        //Assert.assertTrue(String.format("test method[test14]Viewexpectmap1a=105, actualmap1a=%s", a), "105".equals(a));
        //// -------------------------------------------------------------------------------------//
    }

}
