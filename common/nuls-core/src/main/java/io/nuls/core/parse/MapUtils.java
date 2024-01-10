package io.nuls.core.parse;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import net.sf.cglib.beans.BeanMap;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * Created by zhoulijun on 2018/4/4.
 */
public class MapUtils {

    /**
     * Replace the object withlinkedmap
     * mapIn order keyValue ofacsiiSort by code
     * @param bean
     * @return
     */
    public static <T> Map<String, Object> beanToLinkedMap(T bean) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            Object[] keys = beanMap.keySet().toArray();
            Arrays.sort(keys);
            for (Object key : keys) {
                map.put(key + "", beanMap.get(key));
            }
        }
        return map;
    }

    /**
     * Replace the object withmap
     *
     * @param bean
     * @return
     */
    public static <T> Map<String, Object> beanToMap(T bean) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (bean != null) {
            BeanMap beanMap = BeanMap.create(bean);
            for (Object key : beanMap.keySet()) {
                map.put(key + "", beanMap.get(key));
            }
        }
        return map;
    }

    /**
     * takemapReplace withjavabeanobject
     *
     * @param map
     * @param bean
     * @return
     */
    public static <T> T mapToBean(Map<String, Object> map, T bean) {
        BeanMap beanMap = BeanMap.create(bean);
        beanMap.putAll(map);
        return bean;
    }

    /**
     * takeList<T>Convert toList<Map<String, Object>>
     *
     * @param objList
     * @return
     * @throws JsonGenerationException
     * @throws JsonMappingException
     */
    public static <T> List<Map<String, Object>> objectsToMaps(List<T> objList) {
        List<Map<String, Object>> list = new ArrayList<>();
        if (objList != null && objList.size() > 0) {
            Map<String, Object> map = null;
            T bean = null;
            for (int i = 0, size = objList.size(); i < size; i++) {
                bean = objList.get(i);
                map = beanToMap(bean);
                list.add(map);
            }
        }
        return list;
    }

    /**
     * takeList<Map<String,Object>>Convert toList<T>
     *
     * @param maps
     * @param clazz
     * @return
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <T> List<T> mapsToObjects(List<Map<String, Object>> maps, Class<T> clazz) throws InstantiationException, IllegalAccessException, NoSuchMethodException, InvocationTargetException {
        List<T> list = new ArrayList<>();
        if (maps != null && maps.size() > 0) {
            Map<String, Object> map = null;
            T bean = null;
            for (int i = 0, size = maps.size(); i < size; i++) {
                map = maps.get(i);
                bean = clazz.getDeclaredConstructor().newInstance();
                mapToBean(map, bean);
                list.add(bean);
            }
        }
        return list;
    }

}
