/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.tools.core.ioc;

import io.nuls.tools.basic.InitializingBean;
import io.nuls.tools.core.annotation.*;
import io.nuls.tools.core.inteceptor.DefaultMethodInterceptor;
import io.nuls.tools.core.inteceptor.base.BeanMethodInterceptor;
import io.nuls.tools.core.inteceptor.base.BeanMethodInterceptorManager;
import io.nuls.tools.data.StringUtils;
import io.nuls.tools.exception.NulsException;
import io.nuls.tools.exception.NulsRuntimeException;
import io.nuls.tools.log.Log;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 简化版本的ROC框架，参考spring-framework的使用方式，实现简单的依赖注入和动态代理实现的面向切面编程
 * <p>
 * The simplified version of the ROC framework, referring to the use of the spring-framework,
 * implements a simple dependency injection and aspect-oriented programming for dynamic proxy implementations.
 *
 * @author Niels Wang
 */
public class SpringLiteContext {

    private static final Map<String, Object> BEAN_OK_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Object> BEAN_TEMP_MAP = new ConcurrentHashMap<>();
    private static final Map<String, Class> BEAN_TYPE_MAP = new ConcurrentHashMap<>();
    private static final Map<Class, Set<String>> CLASS_NAME_SET_MAP = new ConcurrentHashMap<>();

    private static MethodInterceptor interceptor;

    private static boolean success;

    /**
     * 使用默认的拦截器加载roc环境
     * Load the roc environment with the default interceptor.
     *
     * @param packName 扫描的根路径,The root package of the scan.
     */
    public static void init(final String packName) {
        init(packName, new DefaultMethodInterceptor());
    }

    /**
     * 根据传入的参数加载roc环境
     * Load the roc environment based on the incoming parameters.
     *
     * @param packName    扫描的根路径,The root package of the scan.
     * @param interceptor 方法拦截器,Method interceptor
     */
    public static void init(final String packName, MethodInterceptor interceptor) {
        SpringLiteContext.interceptor = interceptor;
        List<Class> list = ScanUtil.scan(packName);
        list.stream()
                //通过Order注解控制类加载顺序
                .sorted((b1, b2) -> getOrderByClass(b1) > getOrderByClass(b2) ? 1 : -1)
                .forEach((Class clazz) -> checkBeanClass(clazz));
        autowireFields();
        callAfterPropertiesSet();
        success = true;
    }

    /**
     * 给对象中的属性自动赋值
     * Automatically assign values to attributes in an object.
     */
    private static void autowireFields() {
        Set<String> keySet = new HashSet<>(BEAN_TEMP_MAP.keySet());
        for (String key : keySet) {
            try {
                Object bean = BEAN_TEMP_MAP.get(key);
                boolean result = injectionBeanFields(bean, BEAN_TYPE_MAP.get(key));
                if (result) {
                    BEAN_OK_MAP.put(key, bean);
                    BEAN_TEMP_MAP.remove(key);
                    //call afterPropertiesSet 代码迁移到 callAfterPropertiesSet方法执行
                }
            } catch (Exception e) {
                Log.debug(key + " autowire fields failed!");
            }
        }
    }

    /**
     * 对实现了InitializingBean接口的对象，调用afterPropertiesSet方法
     */
    private static void callAfterPropertiesSet() {
        BEAN_OK_MAP.entrySet().stream()
                .sorted((e1, e2) ->
                    getOrderByClass(e1.getValue().getClass()) > getOrderByClass(e2.getValue().getClass()) ? 1 : -1)
                .forEach(entry -> {
                    Object bean = entry.getValue();
                    if (bean instanceof InitializingBean) {
                        try {
                            ((InitializingBean) bean).afterPropertiesSet();
                        } catch (Exception e) {
                            Log.error(e.getMessage());
                        }
                    }
                });
    }

    /**
     * 给对象的所有标记了Autowired注解的字段注入依赖
     * All of the objects tagged with Autowired annotation are injected with dependencies.
     *
     * @param obj     bean对象
     * @param objType 对象类型
     * @throws Exception
     */
    private static boolean injectionBeanFields(Object obj, Class objType) throws Exception {
        Set<Field> fieldSet = getFieldSet(objType);
        boolean result = true;
        for (Field field : fieldSet) {
            boolean b = injectionBeanField(obj, field);
            if (!b) {
                result = true;
            }
        }
        return result;
    }

    /**
     * 获取一个对象的所有字段
     * Gets all the fields of an object.
     *
     * @param objType 对象类型
     */
    private static Set<Field> getFieldSet(Class objType) {
        Set<Field> set = new HashSet<>();
        Field[] fields = objType.getDeclaredFields();
        for (Field field : fields) {
            set.add(field);
        }
        if (!objType.getSuperclass().equals(Object.class)) {
            set.addAll(getFieldSet(objType.getSuperclass()));
        }
        return set;
    }

    /**
     * 检查某个对象的某个属性，如果对象被标记了Autowired注解，则去相应的依赖，并将依赖赋值给对象的该属性
     * Check an attribute of an object, and if the object is marked with Autowired annotations,
     * it is dependent and will depend on the attribute that is assigned to the object.
     *
     * @param obj   bean对象
     * @param field 对象的一个属性
     */
    private static boolean injectionBeanField(Object obj, Field field) throws Exception {
        Annotation[] anns = field.getDeclaredAnnotations();
        if (anns == null || anns.length == 0) {
            return true;
        }
        Annotation automired = getFromArray(anns, Autowired.class);
        if (null == automired) {
            return true;
        }
        String name = ((Autowired) automired).value();
        Object value = null;
        if (null == name || name.trim().length() == 0) {
            Set<String> nameSet = CLASS_NAME_SET_MAP.get(field.getType());
            if (nameSet == null || nameSet.isEmpty()) {
                throw new Exception("Can't find the model,field:" + field.getName());
            } else if (nameSet.size() == 1) {
                name = nameSet.iterator().next();
            } else {
                name = field.getName();
            }
        }
        value = getBean(name);
        if (null == value) {
            throw new Exception("Can't find the model named:" + name);
        }
        field.setAccessible(true);
        field.set(obj, value);
        field.setAccessible(false);
        return true;
    }

    /**
     * 根据名称获取bean
     * get model by model name
     *
     * @param name 对象名称，Bean Name
     */
    private static Object getBean(String name) {
        Object value = BEAN_OK_MAP.get(name);
        if (null == value) {
            value = BEAN_TEMP_MAP.get(name);
        }
        return value;
    }

    /**
     * 根据对象类型字符串获取该类型实例的名称
     * Gets the name of the type instance according to the object type.
     */
    public static Object getBeanByClass(String clazzStr) {
        if (StringUtils.isBlank(clazzStr)) {
            return null;
        }
        String[] paths = clazzStr.split("\\.");
        if (paths.length == 0) {
            return null;
        }
        String beanName = paths[paths.length - 1];
        String start = beanName.substring(0, 1).toLowerCase();
        String end = beanName.substring(1);
        String lowerBeanName = start + end;
        Object value = BEAN_OK_MAP.get(lowerBeanName);
        if (null == value) {
            value = BEAN_TEMP_MAP.get(lowerBeanName);
        }
        return value;
    }

    /**
     * 检查一个类型，如果这个类型上被注释了我们关心的注解，如：Service/Component/Interceptor,就对这个对象进行加载，并放入bean管理器中
     * Check a type, if this is commented on the type annotation, we care about, such as: (Service/Component/Interceptor), is to load the object, and in the model manager
     *
     * @param clazz class type
     */
    private static void checkBeanClass(Class clazz) {
        Annotation[] anns = clazz.getDeclaredAnnotations();
        if (anns == null || anns.length == 0) {
            return;
        }
        Annotation ann = getFromArray(anns, Service.class);
        String beanName = null;
        boolean aopProxy = false;
        if (null == ann) {
            ann = getFromArray(anns, Component.class);
            if (null != ann) {
                beanName = ((Component) ann).value();
            }
        } else {
            beanName = ((Service) ann).value();
            aopProxy = true;
        }
        if (ann != null) {
            if (beanName == null || beanName.trim().length() == 0) {
                beanName = getBeanName(clazz);
            }
            try {
                loadBean(beanName, clazz, aopProxy);
            } catch (NulsException e) {
                Log.error(e.getMessage());
                return;
            }
        }
        Annotation interceptorAnn = getFromArray(anns, Interceptor.class);
        if (null != interceptorAnn) {
            BeanMethodInterceptor interceptor = null;
            try {
                Constructor constructor = clazz.getDeclaredConstructor();
                interceptor = (BeanMethodInterceptor) constructor.newInstance();
            } catch (Exception e) {
                Log.error(e.getMessage());
                return;
            }
            BeanMethodInterceptorManager.addBeanMethodInterceptor(((Interceptor) interceptorAnn).value(), interceptor);
        }
    }

    /**
     * 根据对象类型获取该类型实例的名称
     * Gets the name of the type instance according to the object type.
     */
    private static String getBeanName(Class clazz) {
        String start = clazz.getSimpleName().substring(0, 1).toLowerCase();
        String end = clazz.getSimpleName().substring(1);
        String beanName = start + end;
        if (BEAN_OK_MAP.containsKey(beanName) || BEAN_TEMP_MAP.containsKey(beanName)) {
            beanName = clazz.getName();
        }
        return beanName;
    }

    /**
     * 从数组中获取指定的注解类型的实例
     * Gets an instance of the specified annotation type from the array.
     *
     * @param anns  注解实例数组，Annotated instance array
     * @param clazz 目标注解类型，Target annotation type
     * @return Annotation
     */
    private static Annotation getFromArray(Annotation[] anns, Class clazz) {
        for (Annotation ann : anns) {
            if (ann.annotationType().equals(clazz)) {
                return ann;
            }
        }
        return null;
    }

    /**
     * 初始化该类型的实例，由参数决定是否使用动态代理的方式进行实例化，将实例化后的对象加入对象池
     * Instantiate an instance of this type by instantiating the instantiated object
     * into the object pool by determining whether the dynamic proxy is used.
     *
     * @param beanName 对象名称
     * @param clazz    对象类型
     * @param proxy    是否返回动态代理的对象
     */
    private static Object loadBean(String beanName, Class clazz, boolean proxy) throws NulsException {
        if (BEAN_OK_MAP.containsKey(beanName)) {
            Log.error("model name repetition (" + beanName + "):" + clazz.getName());
            return BEAN_OK_MAP.get(beanName);
        }
        if (BEAN_TEMP_MAP.containsKey(beanName)) {
            Log.error("model name repetition (" + beanName + "):" + clazz.getName());
            return BEAN_TEMP_MAP.get(beanName);
        }
        Object bean = null;
        if (proxy) {
            bean = createProxy(clazz, interceptor);
        } else {
            try {
                bean = clazz.newInstance();
            } catch (InstantiationException e) {
                Log.error(e.getMessage());
                throw new NulsException(e);
            } catch (IllegalAccessException e) {
                Log.error(e.getMessage());
                throw new NulsException(e);
            }
        }
        BEAN_TEMP_MAP.put(beanName, bean);
        BEAN_TYPE_MAP.put(beanName, clazz);
        addClassNameMap(clazz, beanName);
        return bean;
    }

    /**
     * 使用动态代理的方式创建对象的实例
     * Create an instance of the object using a dynamic proxy.
     */
    private static Object createProxy(Class clazz, MethodInterceptor interceptor) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(interceptor);
        System.out.println(clazz);
        return enhancer.create();
    }

    /**
     * 缓存类型和实例名称的关系
     * Cache the relationship between the cache type and the instance name.
     *
     * @param clazz    对象类型
     * @param beanName 对象实例名称
     */
    private static void addClassNameMap(Class clazz, String beanName) {
        Set<String> nameSet = CLASS_NAME_SET_MAP.get(clazz);
        if (null == nameSet) {
            nameSet = new HashSet<>();
        }
        nameSet.add(beanName);
        CLASS_NAME_SET_MAP.put(clazz, nameSet);
        if (null != clazz.getSuperclass() && !clazz.getSuperclass().equals(Object.class)) {
            addClassNameMap(clazz.getSuperclass(), beanName);
        }
        if (clazz.getInterfaces() != null && clazz.getInterfaces().length > 0) {
            for (Class intfClass : clazz.getInterfaces()) {
                addClassNameMap(intfClass, beanName);
            }
        }
    }

    /**
     * 返回bean的加载顺序权重值
     * 默认权重值为1
     *
     * @param clazz
     * @return
     */
    private static int getOrderByClass(Class clazz) {
        List<Annotation> anns = getAnnotationForBean(clazz);
        if (anns == null || anns.isEmpty()) {
            return Order.DEFALUT_ORDER;
        }
        Optional<Annotation> ann = anns.stream().filter(a -> a.annotationType().equals(Order.class)).findFirst();
        if (ann.isEmpty()) {
            return Order.DEFALUT_ORDER;
        }
        return ((Order) ann.get()).value();
    }

    private static List<Annotation> getAnnotationForBean(Class clazz) {
        Annotation[] anns = clazz.getDeclaredAnnotations();
        List<Annotation> res = Arrays.asList(anns);
        if (!(clazz.getSuperclass() instanceof Object) && clazz.getSuperclass() != null) {
            List<Annotation> supperAnno = getAnnotationForBean(clazz.getSuperclass());
            supperAnno.forEach(sa -> {
                if (!res.stream().anyMatch(a -> a.annotationType().equals(sa.annotationType()))) {
                    res.add(sa);
                }
            });
        }
        return res;
    }

    /**
     * 根据类型获取对象池中的对象
     * Gets the object in the object pool according to the type.
     *
     * @param beanClass 对象类型
     * @param <T>       泛型
     * @return 目标对象
     */
    public static <T> T getBean(Class<T> beanClass) {
        Set<String> nameSet = CLASS_NAME_SET_MAP.get(beanClass);
        if (null == nameSet || nameSet.isEmpty()) {
            throw new NulsRuntimeException(new Error());
        }
        if (nameSet.size() > 1) {
            throw new NulsRuntimeException(new Error());
        }
        T value = null;
        String beanName = null;
        for (String name : nameSet) {
            value = (T) BEAN_OK_MAP.get(name);
            beanName = name;
            break;
        }
        if (null == value) {
            value = (T) BEAN_TEMP_MAP.get(beanName);
        }
        return value;
    }

    /**
     * 向上下文中加入一个托管对象，该对象是根据传入的类型，使用动态代理的方式实例化的
     * A managed object is added to the context, which is instantiated using a dynamic proxy based on the incoming type.
     *
     * @param clazz 对象类型
     */
    public static void putBean(Class clazz) throws NulsException {
        loadBean(getBeanName(clazz), clazz, true);
        autowireFields();
    }

    public static void putBean(Class clazz, boolean proxy) throws NulsException {
        loadBean(getBeanName(clazz), clazz, proxy);
        autowireFields();
    }

    /**
     * 从上下文中删除一个类型的所有实例，请谨慎调用
     * Delete all instances of a type from the context, please call carefully.
     */
    public static void removeBean(Class clazz) {
        Set<String> nameSet = CLASS_NAME_SET_MAP.get(clazz);
        if (null == nameSet || nameSet.isEmpty()) {
            return;
        }
        for (String name : nameSet) {
            BEAN_OK_MAP.remove(name);
            BEAN_TEMP_MAP.remove(name);
            BEAN_TYPE_MAP.remove(name);
        }

    }

    /**
     * 检查实例的状态，是否已完成组装，即所有的属性都已自动赋值
     * Check the status of the instance, and whether the assembly has been completed, that is, all properties are automatically assigned.
     *
     * @param bean 对象实例
     */
    public static boolean checkBeanOk(Object bean) {
        return BEAN_OK_MAP.containsValue(bean);
    }

    /**
     * 获取一个类型的所有实例
     * Gets all instances of a type.
     *
     * @param beanClass 类型
     * @param <T>       泛型
     * @return 该类型的所有实例，all instances of the type;
     */
    public static <T> List<T> getBeanList(Class<T> beanClass) throws Exception {
        Set<String> nameSet = CLASS_NAME_SET_MAP.get(beanClass);
        if (null == nameSet || nameSet.isEmpty()) {
            return new ArrayList<>();
        }
        List<T> tlist = new ArrayList<>();
        for (String name : nameSet) {
            T value = (T) BEAN_OK_MAP.get(name);
            if (value == null) {
                value = (T) BEAN_TEMP_MAP.get(name);
            }
            if (null != value) {
                tlist.add(value);
            }
        }
        return tlist;
    }

    public static boolean isInitSuccess() {
        return success;
    }

    public static Collection<Object> getAllBeanList() {
        return BEAN_OK_MAP.values();
    }
}
