package io.nuls.test.cases;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 13:53
 * @Description: 功能描述
 */
@Data
@AllArgsConstructor
public class RemoteTestParam<T> {

    Class<? extends TestCaseIntf> caseCls;

    T source;

    Object param;

    boolean isEquals = true;

    public RemoteTestParam(Class<? extends TestCaseIntf> caseCls,T source,Object param){
        this(caseCls,source,param,true);
    }

}
