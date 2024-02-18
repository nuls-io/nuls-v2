/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.core.core.ioc;

import io.nuls.core.basic.InitializingBean;
import io.nuls.core.constant.BaseConstant;
import io.nuls.core.core.annotation.*;
import io.nuls.core.core.config.ConfigSetting;
import io.nuls.core.core.config.ConfigurationLoader;
import io.nuls.core.core.inteceptor.DefaultMethodInterceptor;
import io.nuls.core.core.inteceptor.base.BeanMethodInterceptor;
import io.nuls.core.core.inteceptor.base.BeanMethodInterceptorManager;
import io.nuls.core.exception.NulsException;
import io.nuls.core.exception.NulsRuntimeException;
import io.nuls.core.log.Log;
import io.nuls.core.model.StringUtils;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simplified versionROCFramework, referencespring-frameworkImplement simple dependency injection and dynamic proxy implementation for object-oriented programming using the
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

    /**
     * Load using default interceptorsrocenvironment
     * Load the roc environment with the default interceptor.
     *
     * @param packName Scanned root path,The root package of the scan.
     */
    public static void init(final String... packName) {
        init(new DefaultMethodInterceptor(), packName);
    }

    /**
     * Load based on the passed in parametersrocenvironment
     * Load the roc environment based on the incoming parameters.
     *
     * @param packName    Scanned root path,The root package of the scan.
     * @param interceptor method interceptors,Method interceptor
     */
    public static void init(final String packName, MethodInterceptor interceptor) {
        init(interceptor, packName);
    }

    public static void init(MethodInterceptor interceptor, String... packName) {
        if (packName.length == 0) {
            throw new IllegalArgumentException("spring lite init package can't be null");
        }
        SpringLiteContext.interceptor = interceptor;
        Log.info("spring lite scan package : " + Arrays.toString(packName));
        Set<Class> classes = new HashSet<>(ScanUtil.scan("io.nuls.core.core.config"));
        Arrays.stream(packName).forEach(pack -> classes.addAll(ScanUtil.scan(pack)));
        classes.stream()
                //adoptOrderAnnotation control class loading order
                .sorted((b1, b2) -> getOrderByClass(b1) > getOrderByClass(b2) ? 1 : -1)
                .forEach(SpringLiteContext::checkBeanClass);
        configurationInjectToBean();
        autowireFields();
        callAfterPropertiesSet();
    }

    /**
     * Inject configuration items intobeanin
     * inject config to bean
     */
    private static void configurationInjectToBean() {
        ConfigurationLoader configLoader = getBean(ConfigurationLoader.class);
        //Load configuration items
        configLoader.load();
        Map<String, ConfigurationLoader.ConfigItem> values = new HashMap<>();
        BEAN_TEMP_MAP.forEach((key1, bean) -> {
            Class<?> cls = BEAN_TYPE_MAP.get(key1);
            Configuration configuration = cls.getAnnotation(Configuration.class);
            if (configuration != null) {
                String domain = configuration.domain();
                //String mappingDomain = BaseConstant.ROLE_MAPPING.getOrDefault(domain, domain);
                Set<Field> fields = getFieldSet(cls);
                fields.forEach(field -> {
                    Value annValue = field.getAnnotation(Value.class);
                    String key = field.getName();
                    if (annValue != null) {
                        key = annValue.value();
                    }
                    Persist persist = field.getAnnotation(Persist.class);
                    boolean readPersist = persist != null;
                    ConfigurationLoader.ConfigItem configItem;
                    if (readPersist) {
                        configItem = configLoader.getConfigItemForPersist(domain, key);
                    } else {
                        configItem = configLoader.getConfigItem(domain, key);
                    }
                    if ("nuls-cores".equalsIgnoreCase(domain)) {
                        configItem = configLoader.getConfigItemForCore(key);
                    }
                    if (configItem == null) {
                        Log.warn("config item :{} not setting", key);
                    } else {
                        ConfigSetting.set(bean, field, configItem.getValue());
                        values.put(cls.getSimpleName() + "." + field.getName(), configItem);
                    }
                });
            } else {
                Set<Field> fields = getFieldSet(cls);
                fields.forEach(field -> {
                    Value annValue = field.getAnnotation(Value.class);
                    if (annValue != null) {
                        String key = annValue.value();
                        //inspectkeyWhen specifieddomainDoes the configuration item list appear multiple times
                        if (configLoader.getConfigData().entrySet().stream().filter(entry -> !entry.getKey().equals(ConfigurationLoader.GLOBAL_DOMAIN) && entry.getValue().containsKey(key)).count() > 1) {
                            throw new IllegalArgumentException("io.nuls.tools.core.annotation.Value " + key + " config item Find more ");
                        }
                        ConfigurationLoader.ConfigItem configItem = configLoader.getConfigItem(key);
                        if (configItem == null) {
                            Log.warn("not found config item : " + key + " to " + cls);
                            try {
                                field.setAccessible(true);
                                values.put(cls.getSimpleName() + "." + field.getName(), new ConfigurationLoader.ConfigItem("DEFAULT", String.valueOf(field.get(bean))));
                                field.setAccessible(false);
                            } catch (IllegalAccessException e) {
                                Log.error(e.getMessage());
                            }
                        } else {
                            ConfigSetting.set(bean, field, configItem.getValue());
                            values.put(cls.getSimpleName() + "." + field.getName(), configItem);
                        }
                    }
                });
            }
        });
        Optional<String> maxItem = values.keySet().stream().max((d1, d2) -> d1.length() > d2.length() ? 1 : -1);
        int maxKeyLength = maxItem.isPresent() ? maxItem.get().length() : 0;
        Log.info("Configuration information:");
        values.forEach((key, value) -> {
            Log.info("{} : {} ==> {}", key + " ".repeat(Math.max(0, maxKeyLength - key.length())), value.getValue(), value.getConfigFile());
        });
    }

    /**
     * Automatically assign values to properties in an object
     * Automatically assign values to attributes in an object.
     */
    private static void autowireFields() {
        Set<String> keySet = new HashSet<>(BEAN_TEMP_MAP.keySet());
        for (String key : keySet) {
            try {
                Object bean = BEAN_TEMP_MAP.get(key);
                injectionBeanFields(bean, BEAN_TYPE_MAP.get(key));
                BEAN_OK_MAP.put(key, bean);
                BEAN_TEMP_MAP.remove(key);
                //call afterPropertiesSet Code migration to callAfterPropertiesSetMethod execution
            } catch (Exception e) {
                Log.error("spring lite autowire fields failed! ", e);
                System.exit(0);
            }
        }
    }

    /**
     * Yes, it has been implementedInitializingBeanObject of interface, callingafterPropertiesSetmethod
     */
    private static void callAfterPropertiesSet() {
        BEAN_OK_MAP.entrySet().stream()
//                .sorted((e1, e2) ->
//                        getOrderByClass(e1.getValue().getClass()) > getOrderByClass(e2.getValue().getClass()) ? 1 : -1)
                .sorted(Comparator.comparing(d -> getOrderByClass(d.getValue().getClass())))
                .forEach(entry -> {
                    Object bean = entry.getValue();
                    if (bean instanceof InitializingBean) {
                        try {
                            ((InitializingBean) bean).afterPropertiesSet();
                        } catch (Exception e) {
                            Log.error("spring lite callAfterPropertiesSet fail :  " + bean.getClass(), e);
                            System.exit(0);
                        }
                    }
                });
    }

    /**
     * Marked all objectsAutowiredAnnotated field injection dependency
     * All of the objects tagged with Autowired annotation are injected with dependencies.
     *
     * @param obj     beanobject
     * @param objType object type
     * @throws Exception
     */
    private static void injectionBeanFields(Object obj, Class objType) throws Exception {
        Set<Field> fieldSet = getFieldSet(objType);
        for (Field field : fieldSet) {
            injectionBeanField(obj, field);
        }
    }

    /**
     * Get all fields of an object
     * Gets all the fields of an object.
     *
     * @param objType object type
     */
    private static Set<Field> getFieldSet(Class objType) {
        Set<Field> set = new HashSet<>();
        Field[] fields = objType.getDeclaredFields();
        Collections.addAll(set, fields);
        if (!objType.getSuperclass().equals(Object.class)) {
            set.addAll(getFieldSet(objType.getSuperclass()));
        }
        return set;
    }

    /**
     * Check a certain property of an object, if the object is markedAutowiredAnnotate, remove the corresponding dependency, and assign the dependency to the property of the object
     * Check an attribute of an object, and if the object is marked with Autowired annotations,
     * it is dependent and will depend on the attribute that is assigned to the object.
     *  @param obj   beanobject
     * @param field A property of an object
     */
    private static void injectionBeanField(Object obj, Field field) throws Exception {
        Annotation[] anns = field.getDeclaredAnnotations();
        if (anns == null || anns.length == 0) {
            return;
        }
        Annotation autowired = getFromArray(anns, Autowired.class);
        if (null == autowired) {
            return;
        }
        String name = ((Autowired) autowired).value();
        if (name.trim().length() == 0) {
            Set<String> nameSet = CLASS_NAME_SET_MAP.get(field.getType());
            if (nameSet == null || nameSet.isEmpty()) {
                throw new Exception("Can't find the model,class : " + obj.getClass() + " field:" + field.getName());
            } else if (nameSet.size() == 1) {
                name = nameSet.iterator().next();
            } else {
                name = field.getName();
            }
        }
        Object value = getBean(name);
        if (null == value) {
            throw new Exception("Can't find the model named:" + name);
        }
        field.setAccessible(true);
        field.set(obj, value);
        field.setAccessible(false);
    }

    /**
     * Obtain by namebean
     * get model by model name
     *
     * @param name Object name,Bean Name
     */
    private static Object getBean(String name) {
        Object value = BEAN_OK_MAP.get(name);
        if (null == value) {
            value = BEAN_TEMP_MAP.get(name);
        }
        return value;
    }

    /**
     * Obtain the name of the instance of the object type based on the object type string
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
     * Check a type if it has been annotated with the annotations we care about, such as：Service/Component/Interceptor,Load this object and place it inbeanIn Manager
     * Check a type, if this is commented on the type annotation, we care about, such as: (Service/Component/Interceptor), is to load the object, and in the model manager
     *
     * @param clazz class type
     */
    private static void checkBeanClass(Class clazz) {
        Annotation[] anns = clazz.getDeclaredAnnotations();
        if (anns == null || anns.length == 0) {
            return;
        }
        String beanName = null;
        boolean aopProxy = false;
        Annotation ann = getFromArray(anns, Service.class);

        if (null != ann) {
            beanName = ((Service) ann).value();
            aopProxy = true;
        }
        if (null == ann) {
            ann = getFromArray(anns, Component.class);
            if (null != ann) {
                beanName = ((Component) ann).value();
            }
        }
        if (null == ann) {
            ann = getFromArray(anns, Controller.class);
            if (null != ann) {
                beanName = ((Controller) ann).value();
            }
        }

        if (null == ann) {
            ann = getFromArray(anns, Configuration.class);
            if (null != ann) {
                aopProxy = true;
            }
//            if (null != ann) {
//                beanName = ((Configuration) ann).value();
//            }
        }

        if (ann != null) {
            if (beanName == null || beanName.trim().length() == 0) {
                beanName = getBeanName(clazz);
            }
            try {
                loadBean(beanName, clazz, aopProxy);
            } catch (NulsException e) {
                Log.error("spring lite load bean fail :  " + clazz, e);
                System.exit(0);
                return;
            }
        }
        Annotation interceptorAnn = getFromArray(anns, Interceptor.class);
        if (null != interceptorAnn) {
            BeanMethodInterceptor interceptor;
            try {
                Constructor constructor = clazz.getDeclaredConstructor();
                interceptor = (BeanMethodInterceptor) constructor.newInstance();
            } catch (Exception e) {
                Log.error("spring lite instance bean fail :  " + clazz, e);
                System.exit(0);
                return;
            }
            BeanMethodInterceptorManager.addBeanMethodInterceptor(((Interceptor) interceptorAnn).value(), interceptor);
        }
    }

    /**
     * Obtain the name of an instance of this type based on the object type
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
     * Get an instance of the specified annotation type from an array
     * Gets an instance of the specified annotation type from the array.
     *
     * @param anns  Annotation instance array,Annotated instance array
     * @param clazz Target annotation type,Target annotation type
     * @return Annotation
     */
    public static Annotation getFromArray(Annotation[] anns, Class clazz) {
        for (Annotation ann : anns) {
            if (ann.annotationType().equals(clazz)) {
                return ann;
            }
        }
        return null;
    }

    /**
     * Initialize an instance of this type, determine whether to use dynamic proxies for instantiation based on parameters, and add the instantiated object to the object pool
     * Instantiate an instance of this type by instantiating the instantiated object
     * into the object pool by determining whether the dynamic proxy is used.
     *
     * @param beanName Object Name
     * @param clazz    object type
     * @param proxy    Do you want to return the object of the dynamic proxy
     */
    private static void loadBean(String beanName, Class clazz, boolean proxy) throws NulsException {
        if (BEAN_OK_MAP.containsKey(beanName)) {
            Log.error("model name repetition (" + beanName + "):" + clazz.getName());
        }
        if (BEAN_TEMP_MAP.containsKey(beanName)) {
            Log.error("model name repetition (" + beanName + "):" + clazz.getName());
        }
        Object bean;
        if (proxy) {
            bean = createProxy(clazz, interceptor);
        } else {
            try {
                bean = clazz.getDeclaredConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
                Log.error(e.getMessage(), e);
                throw new NulsException(e);
            }
        }
        BEAN_TEMP_MAP.put(beanName, bean);
        BEAN_TYPE_MAP.put(beanName, clazz);
        addClassNameMap(clazz, beanName);
    }

    /**
     * Creating instances of objects using dynamic proxies
     * Create an instance of the object using a dynamic proxy.
     */
    private static Object createProxy(Class clazz, MethodInterceptor interceptor) {
        Enhancer enhancer = new Enhancer();
        enhancer.setSuperclass(clazz);
        enhancer.setCallback(interceptor);
        return enhancer.create();
    }

    /**
     * The relationship between cache type and instance name
     * Cache the relationship between the cache type and the instance name.
     *
     * @param clazz    object type
     * @param beanName Object instance name
     */
    private static void addClassNameMap(Class clazz, String beanName) {
        Set<String> nameSet = CLASS_NAME_SET_MAP.computeIfAbsent(clazz, k -> new HashSet<>());
        nameSet.add(beanName);
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
     * returnbeanThe loading order weight value of
     * The default weight value is1
     *
     * @param clazz
     * @return
     */
    private static int getOrderByClass(Class clazz) {
        List<Annotation> anns = getAnnotationForBean(clazz);
        if (anns.isEmpty()) {
            return Order.DEFALUT_ORDER;
        }
        Optional<Annotation> ann = anns.stream().filter(a -> a.annotationType().equals(Order.class)).findFirst();
        if (ann.isEmpty()) {
            return Order.DEFALUT_ORDER;
        }
        return ((Order) ann.get()).value();
    }

    private static List<Annotation> getAnnotationForBean(Class clazz) {
        Annotation[] anns = clazz.getAnnotations();
        return Arrays.asList(anns);
    }

    /**
     * Retrieve objects from the object pool based on their type
     * Gets the object in the object pool according to the type.
     *
     * @param beanClass object type
     * @param <T>       generic paradigm
     * @return Target object
     */
    public static <T> T getBean(Class<T> beanClass) {
        Set<String> nameSet = CLASS_NAME_SET_MAP.get(beanClass);
        if (null == nameSet || nameSet.isEmpty()) {
            throw new NulsRuntimeException(new Error("can not found " + beanClass + " in beans"));
        }
        if (nameSet.size() > 1) {
            throw new NulsRuntimeException(new Error("find multiple implementations of class " + beanClass + ", try to call getBean with the specifiedBeanName"));
        }
        T value;
        String beanName = nameSet.iterator().next();
        value = (T) BEAN_OK_MAP.get(beanName);
        if (null == value) {
            value = (T) BEAN_TEMP_MAP.get(beanName);
        }
        return value;
    }

    /**
     * Add a managed object to the context that is instantiated using dynamic proxies based on the type passed in
     * A managed object is added to the context, which is instantiated using a dynamic proxy based on the incoming type.
     *
     * @param clazz object type
     */
    public static void putBean(Class clazz) throws NulsException {
        loadBean(getBeanName(clazz), clazz, true);
        autowireFields();
    }

    public static void putBean(Class clazz, boolean proxy) throws NulsException {
        loadBean(getBeanName(clazz), clazz, proxy);
        autowireFields();
    }

    public static void putBean(String beanName, Object bean) {
        BEAN_TEMP_MAP.put(beanName, bean);
        BEAN_TYPE_MAP.put(beanName, bean.getClass());
        addClassNameMap(bean.getClass(), beanName);
    }


    /**
     * Delete all instances of a type from the context, please be cautious when calling
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
     * Check the status of the instance to see if assembly has been completed, i.e. all attributes have been automatically assigned values
     * Check the status of the instance, and whether the assembly has been completed, that is, all properties are automatically assigned.
     *
     * @param bean Object instance
     */
    public static boolean checkBeanOk(Object bean) {
        return BEAN_OK_MAP.containsValue(bean);
    }

    /**
     * Get all instances of a type
     * Gets all instances of a type.
     *
     * @param beanClass type
     * @param <T>       generic paradigm
     * @return All instances of this type,all instances of the type;
     */
    public static <T> List<T> getBeanList(Class<T> beanClass) {
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

    public static Collection<Object> getAllBeanList() {
        return BEAN_OK_MAP.values();
    }

    /**
     * Retrieve objects from the object pool based on their type
     * Gets the object in the object pool according to the type.
     *
     * @param beanClass object type
     * @param <T>       generic paradigm
     * @return Target object
     */
    public static <T> T getBean(Class<T> beanClass, String specifiedBeanName) {
        Set<String> nameSet = CLASS_NAME_SET_MAP.get(beanClass);
        if (null == nameSet || nameSet.isEmpty() || StringUtils.isBlank(specifiedBeanName)) {
            throw new NulsRuntimeException(new Error("can not found " + beanClass + " in beans"));
        }
        String beanName = null;
        for (String e : nameSet) {
            if (e.equals(specifiedBeanName)) {
                beanName = specifiedBeanName;
            }
        }
        if (beanName == null) {
            throw new NulsRuntimeException(new Error("can't find a instance of the specified class " + beanClass + " with gived name " + specifiedBeanName));
        }
        T value = (T) BEAN_OK_MAP.get(beanName);
        if (null == value) {
            value = (T) BEAN_TEMP_MAP.get(beanName);
        }
        return value;
    }
}
