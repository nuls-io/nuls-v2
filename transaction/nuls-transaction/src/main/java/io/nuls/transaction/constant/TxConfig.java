package io.nuls.transaction.constant;

import io.nuls.tools.core.annotation.Configuration;
import io.nuls.transaction.model.bo.config.ConfigBean;
import lombok.Data;

/**
 * Transaction module setting
 * @author: Charlie
 * @date: 2019/03/14
 */
@Configuration
@Data
public class TxConfig {
    /** 当前链默认配置*/
    private ConfigBean chainConfig;
    /** 模块数据根目录*/
    private String dbRootPath;
    /** 模块code*/
    private String moduleCode;
    /** 主链链ID*/
    private int mainChainId;
    /** 主链主资产ID*/
    private int mainAssetId;
    /** 语言*/
    private String language;
    /** 编码*/
    private String encoding;
    /** 本地计算nonce值的hash缓存有效时间 30秒*/
    private int hashTtl;
    /** 跨链交易打包确认后需要达到的最低阈值高度才生效*/
    private long ctxEffectThreshold;
    /** 跨链验证通过率百分比, 跨链通过率 */
    private String crossVerifyResultPassRat;
    /** 链内通过率 */
    private String chainNodesResultPassRate;
    /** 友链链内最近N个出块者阈值*/
    private int recentPackagerThreshold;
    /** 未确认交易过期毫秒数-30分钟 */
    private long unconfirmedTxExpireMs;
    /** h2数据库交易记录表分表数量*/
    private int h2TxTableNumber;
}
