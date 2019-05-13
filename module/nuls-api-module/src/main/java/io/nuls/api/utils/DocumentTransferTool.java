package io.nuls.api.utils;

import io.nuls.api.constant.ApiErrorCode;
import io.nuls.core.exception.NulsRuntimeException;
import org.bson.Document;

import java.lang.reflect.Field;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

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
                if ("isNew".equals(field.getName())) {
                    continue;
                }
                if ("java.math.BigInteger".equals(field.getType().getName())) {
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
                if ("isNew".equals(field.getName())) {
                    continue;
                }
                if ("java.math.BigInteger".equals(field.getType().getName())) {
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
                if ("isNew".equals(field.getName())) {
                    continue;
                }
                if (!document.containsKey(field.getName())) {
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
            LoggerUtil.commonLog.error(e);
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
                if ("isNew".equals(field.getName())) {
                    continue;
                }
                if (_id.equals(field.getName())) {
                    field.set(instance, document.get("_id"));
                } else if (!document.containsKey(field.getName())) {
                    continue;
                } else if (field.getType().getName().equals("java.math.BigInteger")) {
                    field.set(instance, new BigInteger(document.get(field.getName()).toString()));
                } else {
                    field.set(instance, document.get(field.getName()));
                }
            }
            return instance;
        } catch (Exception e) {
            LoggerUtil.commonLog.error(e);
            throw new NulsRuntimeException(ApiErrorCode.DATA_PARSE_ERROR, "Document to Model fail");
        }
    }

    public static List<Document> toDocumentList(List list) {
        List<Document> documentList = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            return documentList;
        }
        for (int i = 0; i < list.size(); i++) {
            documentList.add(toDocument(list.get(i)));
        }
        return documentList;
    }

    public static List<Document> toDocumentList(List list, String _id) {
        List<Document> documentList = new ArrayList<>();
        if (list == null || list.isEmpty()) {
            return documentList;
        }
        for (int i = 0; i < list.size(); i++) {
            documentList.add(toDocument(list.get(i), _id));
        }
        return documentList;
    }

    public static <T> List<T> toInfoList(List<Document> documents, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        if (documents == null || documents.isEmpty()) {
            return list;
        }
        for (int i = 0; i < documents.size(); i++) {
            list.add(toInfo(documents.get(i), clazz));
        }
        return list;
    }

    public static <T> List<T> toInfoList(List<Document> documents, String _id, Class<T> clazz) {
        List<T> list = new ArrayList<>();
        if (documents == null || documents.isEmpty()) {
            return list;
        }
        for (int i = 0; i < documents.size(); i++) {
            list.add(toInfo(documents.get(i), _id, clazz));
        }
        return list;
    }
}
