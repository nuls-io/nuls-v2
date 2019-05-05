package io.nuls.core.core.config;

import io.nuls.core.core.annotation.Value;
import io.nuls.core.parse.JSONUtils;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.function.Function;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-13 19:39
 * @Description: 配置项注入到成员变量中
 * config setting to field
 */
public class ConfigSetting {

    static Map<Class, Function<String, Object>> transfer = new HashMap<>();

    static {
        transfer.put(Integer.class, Integer::parseInt);
        transfer.put(int.class, Integer::parseInt);
        transfer.put(Long.class, Long::parseLong);
        transfer.put(long.class, Long::parseLong);
        transfer.put(Float.class, Float::parseFloat);
        transfer.put(float.class, Float::parseFloat);
        transfer.put(Double.class, Double::parseDouble);
        transfer.put(double.class, Double::parseDouble);
        transfer.put(Character.class, str -> str.charAt(0));
        transfer.put(char.class, str -> str.charAt(0));
        transfer.put(Short.class, Short::parseShort);
        transfer.put(short.class, Short::parseShort);
        transfer.put(Boolean.class, Boolean::parseBoolean);
        transfer.put(boolean.class, Boolean::parseBoolean);
        transfer.put(Byte.class, Byte::parseByte);
        transfer.put(byte.class, Byte::parseByte);
        transfer.put(String.class, str -> str);

    }

    public static void set(Object obj, Field field, String value) {
        if (null == value) {
            Value.NotNull notNull = field.getAnnotation(Value.NotNull.class);
            if(notNull != null){
                throw new RuntimeException("config item " + obj.getClass() + "#" + field.getName() + " can't be null ");
            }
            return ;
        };
        field.setAccessible(true);
        if (isPrimitive(field)) {
            try {
                field.set(obj, transfer.get(field.getType()).apply(value));
            } catch (IllegalAccessException e) {
                throw new RuntimeException("inject config item error : " + obj.getClass() + "#" + field.getName(), e);
            }
        } else {
            try {
                if (Collection.class.isAssignableFrom(field.getType())) {
                    if (List.class.isAssignableFrom(field.getType())) {
                        if (field.getGenericType() instanceof ParameterizedType) {
                            ParameterizedType pt = (ParameterizedType) field.getGenericType();
                            field.set(obj, JSONUtils.json2list(value, Class.forName(pt.getActualTypeArguments()[0].getTypeName())));
                        }
                    } else {
                        throw new RuntimeException("Collection type only support List ");
                    }
                } else if (field.getType().isEnum()) {
                    Arrays.stream(field.getType().getEnumConstants()).forEach(e->{
                        if(String.valueOf(e).equals(value)){
                            try {
                                field.set(obj,e);
                            } catch (IllegalAccessException e1) {
                                throw new RuntimeException("inject config item error : " + obj.getClass() + "#" + field.getName(), e1);
                            }
                        }
                    });
                } else {
                    field.set(obj, JSONUtils.json2pojo(value, field.getType()));
                }
            } catch (IllegalAccessException | IOException | ClassNotFoundException e) {
                throw new RuntimeException("inject config item error : " + obj.getClass() + "#" + field.getName(), e);
            }
        }
        field.setAccessible(false);
    }


    public static boolean isPrimitive(Class<?> cls) {
        return cls.isPrimitive() ||
                cls.equals(String.class) ||
                cls.equals(Integer.class) ||
                cls.equals(Long.class) ||
                cls.equals(Short.class) ||
                cls.equals(Float.class) ||
                cls.equals(Double.class) ||
                cls.equals(Character.class) ||
                cls.equals(Byte.class) ||
                cls.equals(Boolean.class);
    }

    private static boolean isPrimitive(Field field){
        return isPrimitive(field.getType());
    }

}
