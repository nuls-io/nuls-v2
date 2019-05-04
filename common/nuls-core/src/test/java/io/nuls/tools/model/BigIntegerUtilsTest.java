//package io.nuls.tools.model;
//
//import io.nuls.tools.crypto.HexUtil;
//import io.nuls.tools.parse.SerializeUtils;
//import org.junit.Test;
//
//import java.math.BigInteger;
//
//import static org.junit.Assert.*;
//
//public class BigIntegerUtilsTest {
//
//
//    @Test
//    public void testSerialize() {
//        //测试序列化和反序列化方法是否正确
//        String str = "267890267890267890";
//        BigInteger value = new BigInteger(str);
//
//        System.out.println(HexUtil.encode(value.toByteArray()));
//        System.out.println(HexUtil.encodeHex(SerializeUtils.bigInteger2Bytes(value)));
//
//        String old = value.toString();
//        String newStr = SerializeUtils.bigIntegerFromBytes(SerializeUtils.bigInteger2Bytes(value)).toString();
//        assertEquals(old, newStr);
//    }
//
//    @Test
//    public void testSmallEndian() {
//        //确保使用小端序，验证方式：跟旧版本的long行序列化做对比
//        String str = "267890123";
//        BigInteger value = new BigInteger(str);
//
//        String newstr = HexUtil.encode(SerializeUtils.bigInteger2Bytes(value));
//        String right = HexUtil.encode(ByteUtils.longToBytes(267890123L));
//        System.out.println(HexUtil.encode(value.toByteArray()));
//        System.out.println(newstr);
//        System.out.println(right);
//
//
//        assertTrue(newstr.startsWith(right));
//
//    }
//
//}