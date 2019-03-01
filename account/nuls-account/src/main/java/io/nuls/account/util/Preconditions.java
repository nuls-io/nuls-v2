package io.nuls.account.util;

import io.nuls.tools.constant.ErrorCode;
import io.nuls.tools.exception.NulsRuntimeException;

import javax.annotation.Nullable;

/**
 * 检查对象是否满足条件类
 * <p>
 * check the object is satisfy the condition
 *
 * @Auther EdwardChan
 * <p>
 * Feb. 22th 2019
 **/
public class Preconditions {

    public static void checkArgument(boolean expression, @Nullable ErrorCode message) {
        if (!expression) {
            throw new NulsRuntimeException(message);
        }
    }

    /**
     * 检查参数是否为null
     * <p>
     * Check if the object is null ,if is null ,it will throw NulsRuntimeException
     *
     * @param object  the check object
     * @param message if dissatisfy the condition,it will throw the Exception which contain the message
     */
    public static void checkNotNull(Object object, @Nullable ErrorCode message) {
        if (object == null)
            throw new NulsRuntimeException(message);
    }

    /**
     * 检查参数是否为空
     * <p>
     * Check if the object is null, if the object is String, check the String is contain nothing or only space
     *
     * @param object  the check object
     * @param message if dissatisfy the condition,it will throw the Exception which contain the message
     */
    public static void checkNotEmpty(Object object, @Nullable ErrorCode message) {
        if (object == null)
            throw new NulsRuntimeException(message);
        if (object instanceof String && ((String) object).trim().length() == 0) {
            throw new NulsRuntimeException(message);
        }
    }

    /**
     * 检查参数是否为空
     * <p>
     * Check if the object is null ,if is null ,it will throw NulsRuntimeException
     *
     * @param objects the list of objects
     * @param message if dissatisfy the condition,it will throw the Exception which contain the message
     */
    public static void checkNotNull(Object[] objects, @Nullable ErrorCode message) {
        if (objects == null) {
            throw new NulsRuntimeException(message);
        }
        for (Object temp : objects)
            checkNotNull(temp, message);
    }
}
