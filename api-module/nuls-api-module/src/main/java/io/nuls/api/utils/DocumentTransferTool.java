package io.nuls.api.utils;

import io.nuls.api.constant.ApiErrorCode;
import io.nuls.tools.exception.NulsRuntimeException;
import org.bson.Document;

import java.lang.reflect.Field;
import java.math.BigInteger;

public class DocumentTransferTool {

    public static Document toDocument(Object obj) {
        if (null == obj) {
            return null;
        }
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Document document = new Document();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                if (field.getName().equals("isNew")) {
                    continue;
                }
                if (field.getType().getName().equals("java.math.BigInteger")) {
                    BigInteger value = (BigInteger) field.get(obj);
                    if (value == null) {
                        value = BigInteger.ZERO;
                    }
                    document.append(field.getName(), value.toString());
                } else {
                    document.append(field.getName(), field.get(obj));
                }

            } catch (IllegalAccessException e) {
                throw new NulsRuntimeException(ApiErrorCode.DATA_PARSE_ERROR, "class to Document fail");
            }
        }
        return document;
    }

    public static Document toDocument(Object obj, String _id) {
        if (null == obj) {
            return null;
        }
        Class clazz = obj.getClass();
        Field[] fields = clazz.getDeclaredFields();
        Document document = new Document();
        for (Field field : fields) {
            try {
                field.setAccessible(true);
                if (field.getName().equals("isNew")) {
                    continue;
                }
                if (field.getType().getName().equals("java.math.BigInteger")) {
                    BigInteger value = (BigInteger) field.get(obj);
                    if (value == null) {
                        value = BigInteger.ZERO;
                    }
                    document.append(field.getName(), value.toString());
                } else if (field.getName().equals(_id)) {
                    document.append("_id", field.get(obj));
                } else {
                    document.append(field.getName(), field.get(obj));
                }
            } catch (IllegalAccessException e) {
                throw new NulsRuntimeException(ApiErrorCode.DATA_PARSE_ERROR, "Model to Document fail");
            }
        }
        return document;
    }

    public static <T> T toInfo(Document document, Class<T> clazz) {
        if (null == document) {
            return null;
        }
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.getName().equals("isNew")) {
                    continue;
                }
                if (field.getType().getName().equals("java.math.BigInteger")) {
                    field.set(instance, new BigInteger(document.get(field.getName()).toString()));
                } else {
                    field.set(instance, document.get(field.getName()));
                }

            }
            return instance;
        } catch (Exception e) {
//            Log.error(e);
            throw new NulsRuntimeException(ApiErrorCode.DATA_PARSE_ERROR, "Document to Model fail");
        }
    }

    public static <T> T toInfo(Document document, String _id, Class<T> clazz) {
        if (null == document) {
            return null;
        }
        try {
            T instance = clazz.getDeclaredConstructor().newInstance();
            Field[] fields = clazz.getDeclaredFields();
            for (Field field : fields) {
                field.setAccessible(true);
                if (field.getName().equals("isNew")) {
                    continue;
                }
                if (field.getType().getName().equals("java.math.BigInteger")) {
                    field.set(instance, new BigInteger(document.get(field.getName()).toString()));
                } else if (_id.equals(field.getName())) {
                    field.set(instance, document.get("_id"));
                } else {
                    field.set(instance, document.get(field.getName()));
                }
            }
            return instance;
        } catch (Exception e) {
//            Log.error(e);
            throw new NulsRuntimeException(ApiErrorCode.DATA_PARSE_ERROR, "Document to Model fail");
        }
    }
}
