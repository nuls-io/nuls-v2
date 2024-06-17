package io.nuls.core.model;

import java.nio.charset.StandardCharsets;

/**
 * @author tag
 * */
public class FormatValidUtils {
    private static final String NULS = "NULS";

    /**
     *  Check the difficulty of the password
     *  length between 8 and 20, the combination of characters and numbers
     *  Password verification rules
     *  @param password   Password to be verified
     *  @return boolean
     */
    public static boolean validPassword(String password) {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        if (password.length() < 8 || password.length() > 20) {
            return false;
        }
        if (password.matches("(.*)[a-zA-Z](.*)")
                && password.matches("(.*)\\d+(.*)")
                && !password.matches("(.*)\\s+(.*)")
                && !password.matches("(.*)[\u4e00-\u9fa5\u3000]+(.*)")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Alias rules:Only lowercase letters are allowed、number、Underline（The underline cannot be at both ends）1~20byte
     * @param alias  alias
     * @return       Verification results
     */
    public static boolean validAlias(String alias) {
        if (StringUtils.isBlank(alias)) {
            return false;
        }
        alias = alias.trim();
        byte[] aliasBytes = alias.getBytes(StandardCharsets.UTF_8);
        if (aliasBytes.length < 1 || aliasBytes.length > 20) {
            return false;
        }
        return alias.matches("^([a-z0-9]+[a-z0-9_]*[a-z0-9]+)|[a-z0-9]+${1,20}");
    }

    /**
     * tokenNaming rules:Only allow the use of large、Lowercase letters、number、Underline（The underline cannot be at both ends）1~20byte
     * @param name   token
     * @return       Verification results
     */
    public static boolean validTokenNameOrSymbol(String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }

        String upperCaseName = name.toUpperCase();
        if(upperCaseName.contains(NULS)) {
            return false;
        }

        byte[] aliasBytes = name.getBytes(StandardCharsets.UTF_8);
        if (aliasBytes.length < 1 || aliasBytes.length > 20) {
            return false;
        }
        return name.matches("^([a-zA-Z0-9]+[a-zA-Z0-9_]*[a-zA-Z0-9]+)|[a-zA-Z0-9]+${1,20}");
    }
    public static boolean validTokenNameOrSymbolV15(String name) {
        if (StringUtils.isBlank(name)) {
            return false;
        }

        String upperCaseName = name.toUpperCase();
        if(upperCaseName.equals(NULS)) {
            return false;
        }

        byte[] aliasBytes = name.getBytes(StandardCharsets.UTF_8);
        if (aliasBytes.length < 1 || aliasBytes.length > 20) {
            return false;
        }
        return name.matches("^([a-zA-Z0-9]+[a-zA-Z0-9_]*[a-zA-Z0-9]+)|[a-zA-Z0-9]+${1,20}");
    }
    /**
     * Note rules: Can be empty,Or not greater than60byte
     * @param remark   Remarks
     * @return         Verification results
     */
    public static boolean validRemark(String remark) {
        if (null == remark) {
            return true;
        }
        remark = remark.trim();
        byte[] aliasBytes = remark.getBytes(StandardCharsets.UTF_8);
        return aliasBytes.length <= 60;
    }
}
