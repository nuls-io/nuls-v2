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
//        //Test whether the serialization and deserialization methods are correct
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
//        //Ensure the use of small end sequences and verification methods：Same as the old versionlongComparing row serialization
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
