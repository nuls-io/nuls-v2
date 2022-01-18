package io.nuls.provider.utils;

import io.nuls.core.basic.InitializingBean;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;
import io.nuls.core.model.StringUtils;
import io.nuls.provider.api.config.Config;

import java.io.*;
import java.util.Set;

/**
 * @author zhoulijun
 * @description TODO
 * @date 2022/1/18 16:57
 * @COPYRIGHT www.xianma360.com
 */
@Component
public class BlockListUtils implements InitializingBean {

    public Set<String> blockList;

    @Autowired
    Config config;

    /**
     * 是否不在黑名单中
     * @param address
     * @return 黑名单中存在返回false
     */
    public boolean isPass(String address){
        return !blockList.contains(address);
    }
    
    @Override
    public void afterPropertiesSet() throws NulsException {
        if(StringUtils.isBlank(config.getBlockListPath())){
            Log.error("未配置黑名单地址");
            System.exit(0);
        }
        try {
            FileReader reader = new FileReader(new File(config.getBlockListPath()));
            BufferedReader buff = new BufferedReader(reader);
            String line = buff.readLine();
            while(line != null){
                blockList.add(line);
            }
            buff.close();
            Log.info("初始化黑名单完成，共记录{}个黑名单地址",blockList.size());
        } catch (FileNotFoundException e) {
            Log.error("黑名单地址错误，文件不存在");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
