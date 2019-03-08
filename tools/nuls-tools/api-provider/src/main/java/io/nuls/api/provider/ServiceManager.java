package io.nuls.api.provider;

import io.nuls.tools.core.ioc.ScanUtil;
import io.nuls.tools.parse.ConfigLoader;
import lombok.extern.slf4j.Slf4j;
import net.sf.cglib.proxy.Enhancer;

import java.io.IOException;
import java.util.*;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-06 14:34
 * @Description: 功能描述
 */
@Slf4j
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
            synchronized (ServiceManager.class){
                if(!inited){
                    init();
                }
            }
        }
        return (T) serviceImpls.get(serviceClass);
    }

    private static void init() {
        //1.初始化提供器类型
        Provider.ProviderType providerType;
        Integer defaultChainId;
        try {
            Properties prop = ConfigLoader.loadProperties("module.properties");
            providerType = Provider.ProviderType.valueOf(prop.getProperty("provider-type"));
            if(prop.getProperty("chain-id") == null){
                throw new RuntimeException("api provider init fail, must be set chain-id in module.properties");
            }
            defaultChainId = Integer.parseInt(prop.getProperty("chain-id"));
        } catch (IOException e) {
            throw new RuntimeException("api provider init fail, load module.properties fail");
        }
        //2.加载服务提供类实例
        List<Class> imps = ScanUtil.scan("io.nuls.api.provider");
        imps.forEach(cls->{
            Provider annotation = (Provider) cls.getAnnotation(Provider.class);
            if(annotation != null){
                Provider.ProviderType clsProviderType = annotation.value();
                log.info("{} provider type : {}",cls, providerType);
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
                            log.error("api provider init fail, service : {}",cls,e);
                            System.exit(0);
                        }
                    });
                }
            }

        });
    }

}
