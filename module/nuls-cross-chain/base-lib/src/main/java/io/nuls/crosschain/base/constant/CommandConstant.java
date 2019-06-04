package io.nuls.crosschain.base.constant;

/**
 * 跨链模块命令常量管理类
 *
 * @author tag
 * 2019/04/08
 */
public interface CommandConstant {

    /**
     * 向链内节点获取跨链交易
     */
    String GET_CTX_MESSAGE = "getCtx";

    /**
     * 向其他链节点获取跨链交易
     */
    String GET_OTHER_CTX_MESSAGE = "getOtherCtx";

    /**
     * 发起链接收主网发送过来的验证跨链交易消息
     */
    String VERIFY_CTX_MESSAGE = "verifyCtx";

    /**
     * 发起链接收跨链交易在主链的处理结果
     */
    String CTX_STATE_MESSAGE = "recvCtxState";
    /**
     * 发起链接收到主网发送的获取链资产统计
     */
    String GET_CIRCULLAT_MESSAGE = "getCirculat";

    /**
     * 接收本链节点发来的跨链交易
     */
    String NEW_CTX_MESSAGE = "recvCtx";

    /**
     * 接收其他链节点发来的跨链交易
     */
    String NEW_OTHER_CTX_MESSAGE = "recvOtherCtx";

    /**
     * 接收链接收到主网发来的跨链交易验证结果
     */
    String CTX_VERIFY_RESULT_MESSAGE = "recvVerifyRs";

    /**
     * 接收链收到主网发送的获取跨链交易处理结果消息
     */
    String GET_CTX_STATE_MESSAGE = "getCtxState";

    /**
     * 接收链接收主网广播的跨链交易Hash
     */
    String BROAD_CTX_HASH_MESSAGE = "recvCtxHash";

    /**
     * 接收链向链内节点广播交易Hash和签名
     */
    String BROAD_CTX_SIGN_MESSAGE = "recvCtxSign";

    /**
     * 主网接收到友链发送的资产发行量统计消息
     * */
    String  CIRCULATION_MESSAGE = "recvCirculat";

    /**
     * 已注册跨链交易信息
     * */
    String  REGISTERED_CHAIN_MESSAGE = "recvRegChain";
}
