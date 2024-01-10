//package io.nuls.provider.utils;
//
//import io.nuls.core.basic.InitializingBean;
//import io.nuls.core.core.annotation.Autowired;
//import io.nuls.core.core.annotation.Component;
//import io.nuls.core.exception.NulsException;
//import io.nuls.core.model.StringUtils;
//import io.nuls.provider.api.config.Config;
//
//import java.io.*;
//import java.util.Set;
//
///**
// * @author zhoulijun
// * @description TODO
// * @date 2022/1/18 16:57
// * @COPYRIGHT www.xianma360.com
// */
//@Component
//public class BlackListUtils implements InitializingBean {
//
//    public Set<String> blackList;
//
//    @Autowired
//    Config config;
//
//    /**
//     * Are you not on the blacklist
//     * @param address
//     * @return There are returns in the blacklistfalse
//     */
//    public boolean isPass(String address){
//        return !blackList.contains(address);
//    }
//
//    @Override
//    public void afterPropertiesSet() throws NulsException {
//        if(StringUtils.isBlank(config.getBlackListPath())){
//            Log.error("No blacklist address configured");
//            System.exit(0);
//        }
//        try {
//            FileReader reader = new FileReader(new File(config.getBlackListPath()));
//            BufferedReader buff = new BufferedReader(reader);
//            String line = buff.readLine();
//            while(line != null){
//                blackList.add(line);
//            }
//            buff.close();
//            Log.info("Blacklist initialization completed, recorded in total{}Blacklisted addresses", blackList.size());
//        } catch (FileNotFoundException e) {
//            Log.error("Blacklist address error, file does not exist");
//            System.exit(0);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//}
