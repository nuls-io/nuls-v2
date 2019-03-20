package io.nuls.test.cases.account;

import io.nuls.test.cases.TestFailException;
import io.nuls.tools.core.annotation.Component;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 11:40
 * @Description: 功能描述
 */
@Component
@Slf4j
public class T1 extends BaseAccountCase<String, T1.T1Param> {

    @Override
    public String title() {
        return "";
    }

    @Override
    public String doTest(T1Param param, int depth) throws TestFailException {
        log.info("param:{}",param.name);
        return "success";
    }

    @Data
    public static class T1Param {
        String name;
    }

}
