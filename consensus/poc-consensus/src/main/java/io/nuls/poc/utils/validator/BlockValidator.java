package io.nuls.poc.utils.validator;

import io.nuls.base.data.Block;
import io.nuls.poc.model.bo.Chain;
import io.nuls.tools.core.annotation.Component;

/**
 * 区块验证工具类
 * Block Verification Tool Class
 *
 * @author tag
 * 2018/11/30
 * */
@Component
public class BlockValidator {
   /**
    * 区块头验证
    * Block verification
    *
    * @param isDownload  block status
    * @param chain       chain info
    * @param block       block info
    * */
   public void validate(boolean isDownload, Chain chain, Block block){

   }

   /**
    * 区块轮次验证
    * Block round validation
    *
    * @param isDownload  block status
    * @param chain       chain info
    * @param block       block info
    * */
   private void roundValidate(boolean isDownload, Chain chain, Block block){

   }

   /**
    * 区块惩罚交易验证
    * Block Penalty Trading Verification
    *
    * */
   private void punishValidate(){

   }

   /**
    * 区块CoinBase交易验证
    * Block CoinBase transaction verification
    *
    * */
   private void coinBaseValidate(){

   }
}
