package io.nuls.contract.util;

import com.alibaba.fastjson.JSONObject;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.model.bo.ContractResult;
import io.nuls.core.rockdb.service.RocksDBService;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.nio.charset.Charset;

import static java.nio.charset.StandardCharsets.UTF_8;

public class ContractDBUtilTest {

    private static void systemConfig() throws Exception {
        System.setProperty("protostuff.runtime.allow_null_array_element", "true");
        System.setProperty(ContractConstant.SYS_FILE_ENCODING, UTF_8.name());
        Field charset = Charset.class.getDeclaredField("defaultCharset");
        charset.setAccessible(true);
        charset.set(null, UTF_8);
    }

    private String area;

    @Before
    public void setUp() throws Exception {
        //systemConfig();
        area = "model";
        RocksDBService.init("./data/contract");
        RocksDBService.createTable(area);
    }

    @Test
    public void newInstance() {
        String json = "{\"acceptDirectTransfer\":false,\"balance\":0,\"contractAddress\":\"OTACBl9m97GO964IHLFSyjPj9EtnTd4=\",\"error\":false,\"events\":[\"{\\\"contractAddress\\\":\\\"5MR_3PyDN5hzZVEw8Jv21TNFZ5P5fNuvETj\\\",\\\"blockNumber\\\":29,\\\"event\\\":\\\"TransferEvent\\\",\\\"payload\\\":{\\\"from\\\":null,\\\"to\\\":\\\"5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo\\\",\\\"value\\\":\\\"1000000000000\\\"}}\"],\"gasUsed\":15794,\"hash\":\"0020342527f7320dedcb47c1efd7a86d0573d0669cd77077aa25879f74e79ff74106\",\"mergedTransferList\":[],\"nonce\":1,\"nrc20\":true,\"price\":25,\"remark\":\"create\",\"revert\":false,\"sender\":\"OTAB7k8OT0MWMF0uePRSkJQlgkRNnA8=\",\"stateRoot\":\"nIeL/Gtr7oZPMGlI6UdeM5q12F8XSKmn5yYCofXfHbQ=\",\"success\":true,\"terminated\":false,\"tokenDecimals\":2,\"tokenName\":\"KQB\",\"tokenSymbol\":\"KongQiBi\",\"tokenTotalSupply\":1000000000000,\"transfers\":[],\"txTime\":1553099238363,\"value\":0}";
        ContractResult result = JSONObject.parseObject(json, ContractResult.class);
        System.out.println(result.toString());
    }

    @Test
    public void putModel() {

    }

    @Test
    public void getModel() {
    }

    @Test
    public void getModel1() {
    }
}