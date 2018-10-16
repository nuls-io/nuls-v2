package io.nuls.tools.constant;

/**
 * @author tag
 */
public class ToolsConstant {
    /**
     * 系统使用的编码方式
     * The encoding used by the nuls system.
     */
    public static String DEFAULT_ENCODING = "UTF-8";

    /**
     * 48位整型数据长度
     * 48 bit integer data length.
     */
    public static int INT48_VALUE_LENGTH = 6;

    /**
     * 空值占位符
     * Null placeholder.
     */
    public static byte[] PLACE_HOLDER = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    /**
     * hash length
     */
    public static final int ADDRESS_LENGTH = 23;
}
