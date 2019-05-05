package io.nuls.crosschain.nuls.servive.impl;

import io.nuls.crosschain.nuls.servive.MainNetService;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Service;

import java.util.Map;

/**
 * 主网跨链模块特有方法
 * @author tag
 * @date 2019/4/23
 */
@Service
public class MainNetServiceImpl implements MainNetService {
    @Override
    public Result registerCrossChain(Map<String, Object> params) {
        return null;
    }

    @Override
    public Result cancelCrossChain(Map<String, Object> params) {
        return null;
    }
}
