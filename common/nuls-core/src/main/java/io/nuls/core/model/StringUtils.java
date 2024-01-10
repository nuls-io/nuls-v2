package io.nuls.core.model;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * @author tag
 */
public class StringUtils {

    /**
     * Check if the string is empty（nullOr an empty string）
     *
     * @param str character string
     * @return Verification results
     */
    public static boolean isBlank(String str) {
        return null == str || str.trim().length() == 0;
    }

    /**
     * Determine if the string isnull
     *
     * @param str character string
     * @return Verification results
     */
    public static boolean isNull(String str) {
        return null == str || str.trim().length() == 0 || "null".equalsIgnoreCase(str.trim());
    }

    /**
     * Check if the string is not empty（nullOr an empty string）
     *
     * @param str character string
     * @return Verification results
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Check if the string is notnull
     *
     * @param str character string
     * @return Verification results
     */
    public static boolean isNotNull(String str) {
        return !isNull(str);
    }

    /**
     * Get aUUID
     *
     * @return UUID
     */
    public static String getNewUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * Remove spaces before and after the string+Verify if it isnull
     *
     * @param str character string
     * @return Remove empty strings before and after
     */
    public static String strTrim(String str) {
        return (isNull(str)) ? null : str.trim();
    }

    /**
     * Is it a positive integer
     *
     * @param str character string
     * @return Verification results
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
     * Is it a number
     *
     * @param str character string
     * @return Verification results
     */
    public static boolean isNumber(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        Matcher isNum = NUMBER_PATTERN.matcher(str);
        return isNum.matches();
    }

    private static final Pattern GT_ZERO_NUMBER_PATTERN = Pattern.compile("([1-9][0-9]*(\\.\\d+)?)|(0\\.\\d*[1-9]+0*)");

    /**
     * Verification is greater than0The number of(Contains decimals,Unlimited number of digits)
     *
     * @param str character string
     * @return Verification results
     */
    public static boolean isNumberGtZero(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        Matcher isNum = GT_ZERO_NUMBER_PATTERN.matcher(str);
        return isNum.matches();
    }

    /**
     * Remove excess decimals.Related to0
     *
     * @param s character string
     * @return Conversion results
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
     * Is the match correctnuls
     * Verification is greater than0The number of(Including decimals, More than significant decimal places8Illegal position)
     *
     * @param str character string
     * @return Verification results
     */
    public static boolean isNuls(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        str = subZeroAndDot(str);
        Matcher isNum = NULS_PATTERN.matcher(str);
        return isNum.matches();
    }

    private static final Pattern GT_ZERO_NUMBER_LIMIT_2_PATTERN = Pattern.compile("([1-9]\\d*(\\.\\d{1,2})?)|(0\\.\\d{1,2})");

    /**
     * Verification is greater than0The number of(Including decimals, More than significant decimal places2Illegal position)
     *
     * @param str character string
     * @return Verification results
     */
    public static boolean isNumberGtZeroLimitTwo(String str) {
        if (StringUtils.isBlank(str)) {
            return false;
        }
        str = subZeroAndDot(str);
        Matcher isNum = GT_ZERO_NUMBER_LIMIT_2_PATTERN.matcher(str);
        return isNum.matches();
    }


    /**
     * Convert string to byte array
     *
     * @param value character string
     * @return Converted byte array
     */
    public static byte[] bytes(String value) {
        return (value == null) ? null : value.getBytes(UTF_8);
    }

    /**
     * Compare two non empty values(No, it's notnull, not an empty string、Not blank)Is the string"equal"
     *
     * @param one      The first string to be compared
     * @param theOther Another string participating in the comparison
     * @return When Two strings None of them are empty strings And The content is completely consistent (After removing the first and last blanks、Same capitalization)Time return true
     */
    public static boolean equals(String one, String theOther) {
        return equals(one, theOther, true, false);
    }

    /**
     * Compare two strings to see if "equal"
     *
     * @param one         The first string participating in the comparison
     * @param theOther    Another string participating in the comparison
     * @param escapeSpace Do you need to remove the first and last blanks ( true Indicates the need to remove first and last blanks,false Indicates not to exclude )
     * @param ignoreCase  Do you want to ignore capitalization ( true Indicates ignoring capitalization ,false Indicates that case is not ignored )
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
