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
import io.nuls.contract.mock.basetest.MockBase;
import io.nuls.contract.util.Log;
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
 *  15. 双合约测试，调用者向A合约转入100，A调用B转入100，B使用30，转移70给调用者
 *  期望执行结果中，有退回到调用者的70
 *
 *  16. 单合约测试，A有一个int数组变量，长度为3，值为[0, 1, 2]，修改数组第二个元素为8
 *                  有一个String数组变量，长度为3，值为["a", "b", "c"]，修改数组第二个元素为f
 *                  有一个Integer数组变量，长度为4，值为[5, 6, 7, 8]，修改数组第一个元素为2
 *                  有一个double数组变量，长度为5, 值为[2.3, 4.5, 6.7, 8.9, 10.11]，修改第五个元素为12.13
 *  期望返回值a=082 afc 2678 2.34.56.78.912.13，调用view方法期望a=a82 afc 2678 2.34.56.78.912.13
 *
 *  17. 单合约测试，A有一个int数组变量，长度为3，值为[0, 1, 2]，修改数组第二个元素为8，再修改为7
 *                  有一个String数组变量，长度为3，值为["a", "b", "c"]，修改数组第二个元素为f，再修改为g
 *                  有一个Integer数组变量，长度为4，值为[5, 6, 7, 8]，修改数组第一个元素为2，再修改为1
 *                  有一个double数组变量，长度为5, 值为[2.3, 4.5, 6.7, 8.9, 10.11]，修改第五个元素为12.13，再修改为11.13
 *  期望返回值a=072 agc 1678 2.34.56.78.911.13，调用view方法期望a=a72 agc 1678 2.34.56.78.911.13
 *
 *  18. 单合约测试，A有一个int数组变量，长度为3，值为[0, 1, 2]，修改数组第二个元素为8，私有方法再修改为7
 *                  有一个String数组变量，长度为3，值为["a", "b", "c"]，修改数组第二个元素为f，再修改为g
 *                  有一个Integer数组变量，长度为4，值为[5, 6, 7, 8]，修改数组第一个元素为2，在修改为1
 *                  有一个double数组变量，长度为5, 值为[2.3, 4.5, 6.7, 8.9, 10.11]，修改第五个元素为12.13，再修改为11.13
 *  期望返回值a=072 agc 1678 2.34.56.78.911.13，调用view方法期望a=a72 agc 1678 2.34.56.78.911.13
 *
 *  19. 双合约测试，A有一个int数组变量，长度为3，值为[0, 1, 2]，
 *                  有一个String数组变量，长度为3，值为["a", "b", "c"]，
 *                  有一个Integer数组变量，长度为4，值为[5, 6, 7, 8]，
 *                  有一个double数组变量，长度为5, 值为[2.3, 4.5, 6.7, 8.9, 10.11]，
 *         A调用B执行：B调用A，修改int数组第二个元素为8，私有方法再修改为7
 *                          修改String数组第二个元素为f，再修改为g
 *                          修改Integer数组第一个元素为2，在修改为1
 *                          修改double数组第五个元素为12.13，再修改为11.13
 *  期望返回值a=072 agc 1678 2.34.56.78.911.13，调用view方法期望a=a72 agc 1678 2.34.56.78.911.13
 *
 *  20. 双合约测试，A有一个int数组变量，长度为3，值为[0, 1, 2]，修改数组第二个元素为8
 *                  有一个String数组变量，长度为3，值为["a", "b", "c"]，修改数组第二个元素为f
 *                  有一个Integer数组变量，长度为4，值为[5, 6, 7, 8]，修改数组第一个元素为2
 *                  有一个double数组变量，长度为5， 值为[2.3, 4.5, 6.7, 8.9, 10.11]，修改第五个元素为12.13
 *         A调用B执行：B调用A，查询t1=int[1], t2=String[1], t3=Integer[0], t4=double[4]
 *                   B调用A，修改以下数据
 *                          修改int数组第二个元素为 int[1] + t1  ->(8 + 8)
 *                          修改String数组第三个元素为 String[2] + t2  ->("c" + "f")
 *                          修改Integer数组第二个元素为 Integer[0] + t3  ->(2 + 2)
 *                          修改double数组第三个元素为 double[1] + t4  ->(4.5 + 12.13)
 *  期望返回值a=0162 afcf 2478 2.34.516.638.912.13，调用view方法期望a=0162 afcf 2478 2.34.516.638.912.13
 *
 *  21. 双合约测试，A有一个int数组变量，长度为3，值为[0, 1, 2]，修改数组第二个元素为8
 *                  有一个String数组变量，长度为3，值为["a", "b", "c"]，修改数组第二个元素为f
 *                  有一个Integer数组变量，长度为4，值为[5, 6, 7, 8]，修改数组第一个元素为2
 *                  有一个double数组变量，长度为5， 值为[2.3, 4.5, 6.7, 8.9, 10.11]，修改第五个元素为12.13
 *         A调用B执行：B调用A，查询t1=int[1], t2=String[1], t3=Integer[0], t4=double[4]
 *                   B调用A，修改以下数据
 *                          修改int数组第二个元素为 int[1] + t1  ->(8 + 8)
 *                          修改String数组第三个元素为 String[2] + t2  ->("c" + "f")
 *                          修改Integer数组第二个元素为 Integer[0] + t3  ->(2 + 2)
 *                          修改double数组第三个元素为 double[1] + t4  ->(4.5 + 12.13)
 *         此时a=0162 afcf 2478 2.34.516.638.912.13
 *         A修改以下数据
 *                  修改int数组第二个元素为7
 *                  修改String数组第二个元素为g
 *                  修改Integer数组第一个元素为1
 *                  修改double数组第五个元素为11.13
 *  期望返回值a=072 agcf 1478 2.34.516.638.911.13，调用view方法期望a=072 agcf 1478 2.34.516.638.911.13
 *
 *  22. 双合约测试，A有一个int数组变量，长度为3，值为[0, 1, 2]，修改数组第二个元素为8
 *                  有一个String数组变量，长度为3，值为["a", "b", "c"]，修改数组第二个元素为f
 *                  有一个Integer数组变量，长度为4，值为[5, 6, 7, 8]，修改数组第一个元素为2
 *                  有一个double数组变量，长度为5， 值为[2.3, 4.5, 6.7, 8.9, 10.11]，修改第五个元素为12.13
 *                  0,8,2| a,f,c| 2,6,7,8| 2.3,4.5,6.7,8.9,12.13
 *         A调用B执行：B调用A，查询t1=int[1], t2=String[1], t3=Integer[0], t4=double[4]
 *                   B调用A，修改以下数据
 *                          修改int数组第二个元素为 int[1] + t1  ->(8 + 8)
 *                          修改String数组第三个元素为 String[2] + t2  ->("c" + "f")
 *                          修改Integer数组第二个元素为 Integer[0] + t3  ->(2 + 2)
 *                          修改double数组第三个元素为 double[1] + t4  ->(4.5 + 12.13)
 *                   此时数组值依次为
 *                   0,16,2| a,f,cf| 2,4,7,8| 2.3,4.5,16.63,8.9,12.13
 *                   B调用A，查询y1=int[2], y2=String[2], y3=Integer[2], y4=double[2]
 *                   B调用A，修改以下数据
 *                          修改int数组第三个元素为 int[1] + y1  ->(16 + 2)
 *                          修改String数组第一个元素为 String[0] + y2  ->("a" + "cf")
 *                          修改Integer数组第三个元素为 Integer[1] + y3  ->(4 + 7)
 *                          修改double数组第四个元素为 double[1] + y4  ->(4.5 + 16.63)
 *                   B 成员变量 B-int数组[t1, y1]  ->8, 2
 *                             B-String数组[t2, y2]  -> "f", "cf"
 *                             B-Integer数组[t3, y3]  -> 2, 7
 *                             B-double数组[t4, y4]  -> 12.13, 16.63
 *  期望返回值a=01618 acffcf 24118 2.34.516.6321.1312.13|82 fcf 27 12.1316.63
 *  调用view方法期望a=01618 acffcf 24118 2.34.516.6321.1312.13
 *  调用view方法期望b=82 fcf 27 12.1316.63
 *
 *  23. 双合约测试，A有一个int数组变量，长度为3，值为[0, 1, 2]，
 *                  有一个String数组变量，长度为3，值为["a", "b", "c"]，
 *                  有一个Integer数组变量，长度为4，值为[5, 6, 7, 8]，
 *                  有一个double数组变量，长度为5， 值为[2.3, 4.5, 6.7, 8.9, 10.11]，
 *                B有一个int数组变量，长度为2，值为[10, 11]，
 *                  有一个String数组变量，长度为2，值为["qa", "qb"]，
 *                  有一个Integer数组变量，长度为2，值为[25, 26]，
 *                  有一个double数组变量，长度为2， 值为[32.3, 34.5]，
 *         A调用B，查询t1=B-int[1], t2=B-String[1], t3=B-Integer[1], t4=B-double[1]
 *         A修改以下数据
 *                  修改int数组第二个元素为 int[1] + t1  ->(1 + 11)
 *                  修改String数组第三个元素为 String[2] + t2  ->("c" + "qb")
 *                  修改Integer数组第二个元素为 Integer[0] + t3  ->(5 + 26)
 *                  修改double数组第三个元素为 double[1] + t4  ->(4.5 + 34.5)
 *         此时A数据为
 *         0,12,2 a,b,cqb 5,31,7,8 2.3,4.5,39,8.9,10.11
 *         A调用B执行：修改以下数据
 *                          修改B-int[0]为 2
 *                          修改B-String[0]为 2
 *                          修改B-Integer[0]为 2
 *                          修改B-double[0]为 2
 *                   此时B数组值依次为
 *                   211 2qb 226 234.5
 *         A调用B，查询y1=B-int[0], y2=B-String[0], y3=B-Integer[0], y4=B-double[0]
 *         A修改以下数据
 *                  修改int数组第二个元素为 int[1] + y1  ->(12 + 2)
 *                  修改String数组第三个元素为 String[2] + y2  ->("cqb" + "2")
 *                  修改Integer数组第二个元素为 Integer[0] + y3  ->(5 + 2)
 *                  修改double数组第三个元素为 double[1] + y4  ->(4.5 + 2)
 *  期望返回值a=0142 abcqb2 5778 2.34.56.58.910.11|211 2qb 226 234.5
 *  调用view方法期望a=0142 abcqb2 5778 2.34.56.58.910.11
 *  调用view方法期望b=211 2qb 226 234.5
 *
 *  24. 双合约测试，A有一个int数组变量，长度为3，值为[0, 1, 2]，
 *                  有一个String数组变量，长度为3，值为["a", "b", "c"]，
 *                  有一个Integer数组变量，长度为4，值为[5, 6, 7, 8]，
 *                  有一个double数组变量，长度为5， 值为[2.3, 4.5, 6.7, 8.9, 10.11]，
 *                B有一个int数组变量，长度为2，值为[10, 11]，
 *                  有一个String数组变量，长度为2，值为["qa", "qb"]，
 *                  有一个Integer数组变量，长度为2，值为[25, 26]，
 *                  有一个double数组变量，长度为2， 值为[32.3, 34.5]，
 *         A调用B执行：修改以下数据
 *                          修改B-int[0]为 2
 *                          修改B-String[0]为 2
 *                          修改B-Integer[0]为 2
 *                          修改B-double[0]为 2
 *                   此时B数组值依次为
 *                   211 2qb 226 234.5
 *         A调用B执行：修改以下数据
 *                          修改B-int[0]为 3
 *                          修改B-String[0]为 3
 *                          修改B-Integer[0]为 3
 *                          修改B-double[0]为 3
 *                   此时B数组值依次为
 *                   311 3qb 326 334.5
 *         A分割返回值`311 3qb 326 334.5`，得到
 *                      y1=311, y2="3qb", y3=326, y4=334.5
 *         A修改以下数据
 *                  修改int数组第二个元素为 int[1] + y1  ->(1 + 311)
 *                  修改String数组第三个元素为 String[2] + y2  ->("c" + "3qb")
 *                  修改Integer数组第二个元素为 Integer[0] + y3  ->(5 + 326)
 *                  修改double数组第三个元素为 double[1] + y4  ->(4.5 + 334.5)
 *  期望返回值a=03122 abc3qb 533178 2.34.5339.08.910.11|311 3qb 326 334.5
 *  调用view方法期望a=03122 abc3qb 533178 2.34.5339.08.910.11
 *  调用view方法期望b=311 3qb 326 334.5
 *
 *
 * @author: PierreLuo
 * @date: 2019-06-11
 */
public class ContractVmTest extends MockBase {

    String contractA = "tNULSeBaN5xpQLvYBMJuybAzgzRkRXL4r3tqMx";
    String contractB = "tNULSeBaN1gZJobF3bxuLwXxvvAosdwQTVxWFn";
    byte[] prevStateRoot;

    @Before
    public void createAndInit() throws Exception {
        // 加载协议升级的数据
        ProtocolGroupManager.setLoadProtocol(false);
        ProtocolGroupManager.updateProtocol(chainId, (short) 4);

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
        Assert.assertTrue(String.format("测试方法[test4_integer]view期望integerValue=6, 实际integerValue=%s", integerValue), "6".equals(integerValue));
    }
    @Test
    public void test4_int() throws Exception{
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

    @Test
    public void test15() throws Exception{
        byte[] currentStateRoot;
        String a;
        Object[] objects;
        ProgramResult programResult;
        objects = super.call(contractA, prevStateRoot, SENDER, "test15", new String[]{}, BigInteger.valueOf(100L));
        currentStateRoot = (byte[]) objects[0];
        programResult = (ProgramResult) objects[1];
        Assert.assertTrue("测试方法[test15]expect success, " + programResult.getErrorMessage() + ", " + programResult.getStackTrace(), programResult.isSuccess());

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
        Assert.assertTrue("测试方法[test15]期望 退回70", success);
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
        Assert.assertTrue(String.format("测试方法[test22]view期望a=\"01618 acffcf 24118 2.34.516.6321.1312.13\", 实际a=%s", a), "01618 acffcf 24118 2.34.516.6321.1312.13".equals(a));
        String b = super.view(contractB, currentStateRoot, "arrayContact", new String[]{});
        Assert.assertTrue(String.format("测试方法[test22]view期望b=\"82 fcf 27 12.1316.63\", 实际b=%s", b), "82 fcf 27 12.1316.63".equals(b));
    }

    @Test
    public void test23() throws Exception{
        byte[] currentStateRoot = this.callVmTest(prevStateRoot, "test23", "0142 abcqb2 5778 2.34.56.58.910.11|211 2qb 226 234.5", false);
        String a = super.view(contractA, currentStateRoot, "arrayContact", new String[]{});
        Assert.assertTrue(String.format("测试方法[test23]view期望a=\"0142 abcqb2 5778 2.34.56.58.910.11\", 实际a=%s", a), "0142 abcqb2 5778 2.34.56.58.910.11".equals(a));
        String b = super.view(contractB, currentStateRoot, "arrayContact", new String[]{});
        Assert.assertTrue(String.format("测试方法[test23]view期望b=\"211 2qb 226 234.5\", 实际b=%s", b), "211 2qb 226 234.5".equals(b));
    }

    @Test
    public void test24() throws Exception{
        byte[] currentStateRoot = this.callVmTest(prevStateRoot, "test24", "03122 abc3qb 533178 2.34.5339.08.910.11|311 3qb 326 334.5", false);
        String a = super.view(contractA, currentStateRoot, "arrayContact", new String[]{});
        Assert.assertTrue(String.format("测试方法[test24]view期望a=\"03122 abc3qb 533178 2.34.5339.08.910.11\", 实际a=%s", a), "03122 abc3qb 533178 2.34.5339.08.910.11".equals(a));
        String b = super.view(contractB, currentStateRoot, "arrayContact", new String[]{});
        Assert.assertTrue(String.format("测试方法[test24]view期望b=\"311 3qb 326 334.5\", 实际b=%s", b), "311 3qb 326 334.5".equals(b));
    }

    private byte[] callVmTest(byte[] prevStateRoot, String method, String expect, String viewMethod) throws Exception {
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

        if(StringUtils.isNotBlank(viewMethod)) {
            String a = super.view(contractA, prevStateRoot, viewMethod, new String[]{});
            Assert.assertTrue(String.format("测试方法[%s]View期望a=%s, 实际a=%s", method, expect, a), expect.equals(a));
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
