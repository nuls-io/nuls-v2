package io.nuls.core.rpc;

import io.nuls.core.crypto.HexUtil;

import java.util.Base64;

public class EncoderTest {

    public static void main(String[] args) {

        long time = System.nanoTime();

        int count = 10000000;

        String str = "ksajdflkjsadflkjdasf2u98fflsjflksajdflksadflkasjdflkasjdf90sadf09sadfi0asfdlckFD程序执行2315632335233123231322132速度v32dszxsjlkjslkdfsadfwer3奖励空间哦iLJLKJLKLJLJLLSFDASDsakdfjlkljdlkfjasd9f809asdfklajsd2e";

        testHex(str, count);

        System.out.println("hex use time : " + ((System.nanoTime() - time) / 1000000) + " ms");
        time = System.nanoTime();

        testBase64(str, count);

        System.out.println("base64 use time : " + ((System.nanoTime() - time) / 1000000) + " ms");
    }

    private static void testBase64(String str, int count) {
        for(int i = 0 ; i < count ; i ++) {
            String hex = Base64.getEncoder().encodeToString((i + str + i).getBytes());
            byte[] b = Base64.getDecoder().decode(hex);
        }
        String hex = Base64.getEncoder().encodeToString(str.getBytes());
        System.out.println("base64 length : " + hex.length());
    }

    private static void testHex(String str, int count) {
        for(int i = 0 ; i < count ; i ++) {
            String hex = HexUtil.encode((i + str + i).getBytes());
            byte[] b = HexUtil.decode(hex);
        }
        String hex = HexUtil.encode(str.getBytes());
        System.out.println("hex length : " + hex.length());

    }
}
