package io.nuls.h2.utils;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.ibatis.datasource.unpooled.UnpooledDataSourceFactory;

public class DruidDataSourceBuilder extends UnpooledDataSourceFactory {
    public DruidDataSourceBuilder() {
        this.dataSource = new DruidDataSource();
    }
}
