package io.nuls.core.basic;

import java.lang.reflect.InvocationTargetException;

public interface ModuleConfig {

    default VersionChangeInvoker getVersionChangeInvoker() throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        Class<?> aClass = Class.forName("io.nuls.core.basic.DefaultVersionChangeInvoker");
        return (VersionChangeInvoker) aClass.getDeclaredConstructor().newInstance();
    }

}
