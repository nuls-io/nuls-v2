package io.nuls.tools.model;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author tag
 */
public class StringUtils {

    /**
     * 判断字符串是否为空（null或空字符串）
     *
     * @param str 字符串
     * @return 验证结果
     */
    public static boolean isBlank(String str) {
        return null == str || str.trim().length() == 0;
    }

    /**
     * 判断字符串是否为null
     *
     * @param str 字符串
     * @return 验证结果
     */
    public static boolean isNull(String str) {
        return null == str || str.trim().length() == 0 || "null".equalsIgnoreCase(str.trim());
    }

    /**
     * 判断字符串是否不为空（null或空字符串）
     *
     * @param str 字符串
     * @return 验证结果
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * 判断字符串是否不为null
     *
     * @param str 字符串
     * @return 验证结果
     */
    public static boolean isNotNull(String str) {
        return !isNull(str);
    }

    /**
     * 获取一个UUID
     *
     * @return UUID
     */
    public static String getNewUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 去掉字符串前后空格+验证是否为null
     *
     * @param str 字符串
     * @return 去掉前后空个的字符串
     */
    public static String strTrim(String str) {
        return (isNull(str)) ? null : str.trim();
    }

    /**
     * 是否为正整数
     *
     * @param str 字符串
     * @return 验证结果
     */
    public static boolean isNumeric(String str) {
        for (int i = 0, len = str.length(); i < len; i++) {
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }


    private static final Pattern NUMBER_PATTERN = Pattern.compile("-?[0-9]+(\\.[0-9]+)?");

    /**
     * 是否为数字
     *
     * @param str 字符串
     * @return 验证结果
     */
    public static boolean isNumber(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        Matcher isNum = NUMBER_PATTERN.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    private static final Pattern GT_ZERO_NUMBER_PATTERN = Pattern.compile("([1-9][0-9]*(\\.\\d+)?)|(0\\.\\d*[1-9]+0*)");

    /**
     * 验证是大于0的数(包含小数,不限位数)
     *
     * @param str 字符串
     * @return 验证结果
     */
    public static boolean isNumberGtZero(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        Matcher isNum = GT_ZERO_NUMBER_PATTERN.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    /**
     * 去掉小数多余的.与0
     *
     * @param s 字符串
     * @return 转换结果
     */
    private static String subZeroAndDot(String s) {
        if (s.indexOf(".") > 0) {
            s = s.replaceAll("0+?$", "");
            s = s.replaceAll("[.]$", "");
        }
        return s;
    }

    private static final Pattern NULS_PATTERN = Pattern.compile("([1-9]\\d*(\\.\\d{1,8})?)|(0\\.\\d{1,8})");

    /**
     * 匹配是否是nuls
     * 验证是大于0的数(包括小数, 小数点后有效位超过8位则不合法)
     *
     * @param str 字符串
     * @return 验证结果
     */
    public static boolean isNuls(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        str = subZeroAndDot(str);
        Matcher isNum = NULS_PATTERN.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }

    private static final Pattern GT_ZERO_NUMBER_LIMIT_2_PATTERN = Pattern.compile("([1-9]\\d*(\\.\\d{1,2})?)|(0\\.\\d{1,2})");

    /**
     * 验证是大于0的数(包括小数, 小数点后有效位超过2位则不合法)
     *
     * @param str 字符串
     * @return 验证结果
     */
    public static boolean isNumberGtZeroLimitTwo(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        str = subZeroAndDot(str);
        Matcher isNum = GT_ZERO_NUMBER_LIMIT_2_PATTERN.matcher(str);
        if (!isNum.matches()) {
            return false;
        }
        return true;
    }


    /**
     * 字符串转为字节数组
     *
     * @param value 字符串
     * @return 转换得到的字节数组
     */
    public static byte[] bytes(String value) {
        return (value == null) ? null : value.getBytes(UTF_8);
    }

    /**
     * 比较两个非空(不是null，不是空串、不是空白)字符串是否"相等"
     *
     * @param one      第一个需要比较的字符串
     * @param theOther 另一个参与比较的字符串
     * @return 当 两个字符串 都不为空串 且 内容完全一致 (剔除首尾空白后、大小写也一致)时返回 true
     */
    public static boolean equals(String one, String theOther) {
        return equals(one, theOther, true, false);
    }

    /**
     * 比较两个字符串是否 "相等"
     *
     * @param one         参与比较的第一个字符串
     * @param theOther    参与比较的另一个字符串
     * @param escapeSpace 是否需要剔除首尾空白 ( true 表示需要剔除首尾空白，false 表示不剔除 )
     * @param ignoreCase  是否忽略大小写 ( true 表示忽略大小写 ，false 表示不忽略大小写 )
     * @return
     */
    public static boolean equals(String one, String theOther, boolean escapeSpace, boolean ignoreCase) {

        if (one == null || theOther == null) {
            return false;
        }
        if (escapeSpace) {
            one = one.trim();
            theOther = theOther.trim();
        }
        return ignoreCase ? one.equalsIgnoreCase(theOther) : one.equals(theOther);
    }
}
