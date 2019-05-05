package io.nuls.chain.service;
/**
 *消息协议服务接口
 *Message protocol service interface
 *
 * @author lanjinsheng
 * @date 2018/12/04
 */
public interface MessageService {
    /**
     * 请求链发行资产
     * request Chain Issuing Assets
     * @return
     */
    boolean requestChainIssuingAssets();

    /**
     * 接收链发行资产
     *recieve Chain Issuing Assets
     * @return
     */
    boolean recChainIssuingAssets();
}
