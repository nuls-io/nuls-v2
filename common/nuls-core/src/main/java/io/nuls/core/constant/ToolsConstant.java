package io.nuls.core.constant;

/**
 * @author tag
 */
public class ToolsConstant {
    /**
     * The encoding method used by the system
     * The encoding used by the nuls system.
     */
    public static String DEFAULT_ENCODING = "UTF-8";

    /**
     * 48Bit integer data length
     * 48 bit integer entity length.
     */
    public static int INT48_VALUE_LENGTH = 6;

    /**
     * Blank value placeholder
     * Null placeholder.
     */
    public static byte[] PLACE_HOLDER = new byte[]{(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};

    /**
     * Script identifier bit
     * Null placeholder.
     */
    public static byte[] SIGN_HOLDER = new byte[]{(byte) 0x00, (byte) 0x00};
}
