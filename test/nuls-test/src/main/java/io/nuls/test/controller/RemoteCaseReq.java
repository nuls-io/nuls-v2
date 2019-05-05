package io.nuls.test.controller;

import io.nuls.test.cases.TestCaseIntf;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-20 11:09
 * @Description: 功能描述
 */
@Data
public class RemoteCaseReq {

    Class<? extends TestCaseIntf> caseClass;

    String param;

}
