package io.nuls.crosschain.nuls.servive;

import io.nuls.core.basic.Result;

import java.util.Map;

/**
 * 提供给区块模块调用的接口
 * @author tag
 * @date 2019/4/25
 */
public interface BlockService {
    /**
     * 接收最新区块高度
     * @param params  参数
     * @return        消息处理结果
     * */
    Result newBlockHeight(Map<String,Object> params);
}
