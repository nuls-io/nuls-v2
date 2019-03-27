package io.nuls.api.provider.consensus.facade;

import io.nuls.api.provider.BaseReq;
import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-03-26 15:54
 * @Description: 功能描述
 */
@Data
public class GetAgentListReq extends BaseReq {

    private int pageNumber,pageSize;

    private String keyWord;

}
