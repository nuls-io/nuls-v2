package io.nuls.base.api.provider;

import io.nuls.core.core.ioc.ScanUtil;
import io.nuls.core.log.Log;
import net.sf.cglib.proxy.Enhancer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 14:34
 * @Description: 功能描述
 */
public class ServiceManager {

    /**
     * 初始化标记
     */
    public static boolean inited = false;


    public static Map<Class,Object> serviceImpls = new HashMap<>();

    /**
     * 获取服务实现类
     * @param serviceClass
     * @param <T>
     * @return
     */
    public static <T> T get(Class<T> serviceClass){
        if(!inited){
            throw new RuntimeException("ServiceManager not ready, must call init before ");
        }
        return (T) serviceImpls.get(serviceClass);
    }

    public static void init(Integer defaultChainId, Provider.ProviderType providerType) {
        //1.初始化提供器类型
//        Provider.ProviderType providerType;
//        Integer defaultChainId;
//        try {
//            Properties prop = ConfigLoader.loadProperties("module.properties");
//            providerType = Provider.ProviderType.valueOf(prop.getProperty("provider-type"));
//            if(prop.getProperty("chain-id") == null){
//                throw new RuntimeException("api provider init fail, must be set chain-id in module.properties");
//            }
//            defaultChainId = Integer.parseInt(prop.getProperty("chain-id"));
//        } catch (IOException e) {
//            throw new RuntimeException("api provider init fail, load module.properties fail");
//        }
        //2.加载服务提供类实例
        List<Class> imps = ScanUtil.scan("io.nuls.base.api.provider");
        imps.forEach(cls->{
            Provider annotation = (Provider) cls.getAnnotation(Provider.class);
            if(annotation != null){
                Provider.ProviderType clsProviderType = annotation.value();
                if(providerType == clsProviderType){
                    Arrays.stream(cls.getInterfaces()).forEach(intf->{
                        try {
                            if(cls.getSuperclass() == null && !cls.getSuperclass().equals(BaseService.class)){
                                throw new RuntimeException("api provider init fail, Service must be extends BaseService");
                            }
                            ServiceProxy serviceProxy = new ServiceProxy(defaultChainId);
                            Enhancer enhancer = new Enhancer();
                            enhancer.setSuperclass(cls);
                            enhancer.setCallback(serviceProxy);
                            BaseService service = (BaseService)enhancer.create();
                            serviceImpls.put(intf,service);
                            service.setChainId(defaultChainId);
                        } catch (Exception e) {
                            Log.error("api provider init fail, service : {}",cls,e);
                            System.exit(0);
                        }
                    });
                }
            }

        });
        inited = true;
    }

}
