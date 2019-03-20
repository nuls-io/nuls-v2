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
public class RemoteTestParam {

    Class<? extends TestCaseIntf> caseCls;

    Object source;

    Object param;

}
