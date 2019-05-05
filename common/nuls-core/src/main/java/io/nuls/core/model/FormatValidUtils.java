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
     *  密码验证规则
     *  @param password   需验证的密码
     *  @return boolean
     */
    public static boolean validPassword(String password) {
        if (StringUtils.isBlank(password)) {
            return false;
        }
        if (password.length() < 8 || password.length() > 20) {
            return false;
        }
        if (password.matches("(.*)[a-zA-z](.*)")
                && password.matches("(.*)\\d+(.*)")
                && !password.matches("(.*)\\s+(.*)")
                && !password.matches("(.*)[\u4e00-\u9fa5\u3000]+(.*)")) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * 别名规则:只允许使用小写字母、数字、下划线（下划线不能在两端）1~20字节
     * @param alias  别名
     * @return       验证结果
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
     * token命名规则:只允许使用大、小写字母、数字、下划线（下划线不能在两端）1~20字节
     * @param name   token
     * @return       验证结果
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

    /**
     * 备注规则: 可以为空,或者不大于60字节
     * @param remark   备注
     * @return         验证结果
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
