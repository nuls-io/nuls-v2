package io.nuls.transaction.init;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.base.constant.BaseConstant;
import io.nuls.base.data.Address;
import io.nuls.base.data.Page;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.parse.SerializeUtils;
import io.nuls.transaction.constant.TransactionConstant;
import io.nuls.transaction.db.h2.dao.TransactionService;
import io.nuls.transaction.db.h2.dao.impl.BaseService;
import io.nuls.transaction.db.h2.dao.impl.TransactionServiceImpl;
import io.nuls.transaction.model.po.TransactionPo;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
public class H2Test {

    public static void main(String[] args) throws Exception{
        //System.out.println(("QYAnNNfGKwUGJwPQHCpUa1WU8Qzhzb822".hashCode() & Integer.MAX_VALUE)%TransactionConstant.H2_TX_TABLE_NUMBER);
        before();
        long start = System.currentTimeMillis();
        initTestTable();
        System.out.println("花费时间：" + String.valueOf(System.currentTimeMillis() - start));
        start = System.currentTimeMillis();
        select();
        System.out.println("查询数据花费时间：" + String.valueOf(System.currentTimeMillis() - start));
    }

    private static void select(){
        String address = "QYAnNNfGKwUGJwPQHCpUa1WU8Qzhzb822";
        TransactionService ts = new TransactionServiceImpl();
        Page<TransactionPo> page =  ts.getTxs(address, null, null, null, null, 1,30);
        try {
            System.out.println(JSONUtils.obj2json(page));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }

    public static void before() throws Exception{
        String resource = "mybatis/mybatis-config.xml";
        InputStream in = Resources.getResourceAsStream(resource);
        BaseService.sqlSessionFactory = new SqlSessionFactoryBuilder().build(in);
    }

    public static void initTestTable(){
        TransactionService ts = new TransactionServiceImpl();
        //ts.createTable("transaction", "transaction_index",128);
        ts.createTxTables(TransactionConstant.H2_TX_TABLE_NAME_PREFIX,
                TransactionConstant.H2_TX_TABLE_INDEX_NAME_PREFIX,
                TransactionConstant.H2_TX_TABLE_NUMBER);
    }

    public static void initTestData(){
        TransactionService ts = new TransactionServiceImpl();
        List<TransactionPo> listPo = new ArrayList<>();
        for (int i=0;i<50;i++) {
            ts.saveTx(createTxPo());
        }
    }

    //模拟TransactionPo的数据
    private static TransactionPo createTxPo(){
        Random rand = new Random();
        int amount = rand.nextInt(5050 - 50 + 1) + 50;
        int time = rand.nextInt(1542514842 - 1541001600 + 1) + 1541001600;
        int stateAndType = rand.nextInt(3);
        int type = rand.nextInt(10) + 1;
        TransactionPo txPo = new TransactionPo();

        txPo.setAddress(ranAddress());
        txPo.setHash(getTestHash());
        txPo.setAmount((long)amount);
        txPo.setState(stateAndType);
        txPo.setType(type);
        txPo.setTime((long)time);
        try {
            System.out.println(JSONUtils.obj2json(txPo));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return txPo;
    }

    //模拟随机生成交易hash，长度68
    private static String getTestHash(){
        char[] words = {'a','b','c','d','e','f','0','1','2','3','4','5','6','7','8','9'};
        //0020b61313f59fbf8b0639b556690d9d6b8f2c89ce1755e6939080ddf13101b452aa
        Random rand = new Random();
        String hash = "00";
        int max = words.length;
        for(int i = 0; i<66;i++){
            int index = rand.nextInt(max);
            hash += words[index];
        }
        return hash;
    }

    private static String getAddr(){
        Address address = new Address((short) 8888, BaseConstant.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(new ECKey().getPubKey()));
        return address.getBase58();
    }

    private static void createAddress(){
        System.out.println("{");
        for (int i=0;i<40;i++){
            String addr = getAddr();
             System.out.println("\"" + addr + "\""  + (i==39  ? "" : ",") );
            System.out.println(addr.hashCode());
            System.out.println((addr.hashCode() & Integer.MAX_VALUE) % TransactionConstant.H2_TX_TABLE_NUMBER);

        }
        System.out.println("}");
    }

    private static String ranAddress(){
        //40个种子地址，随机生成交易记录；
        String[] address = {
                "USEkp7H2s1x2khUgbswMHe9HKmStRb822",
                "WYiRSZ4jzw9qXDTU8LS3rXZ3zvpJgb822",
                "JWULnLixrRFbWRuw6m3pfR46Lu2HDb822",
                "SWrPUjNakX1brqKSszdvUEvtjuQL6b822",
                "MdAczW427c2iH5CiXfWX2XXGoW9WKb822",
                "L498GRnC57xPzwT5bd3nooJfMSUTyb822",
                "R3yG2Eu8gogomitWoiwbLAriCNWreb822",
                "QYAnNNfGKwUGJwPQHCpUa1WU8Qzhzb822",
                "Y31vkMqXEViqpkZ29vysnDG2XPz9Eb822",
                "JiKbULf4dbTrmBt4eSuMjNdyrwgm9b822",
                "RP4ro5aVegE5h46aPNPk9vfs9r1CZb822",
                "PvM9g5otKi25Dnhj2Fx5etCPM7AwRb822",
                "YNz2v4FfR3bLjDaWJ8axiuSALnt9Nb822",
                "Sg1aRdgLRZjVQvzUWSqTZPD5o7kh6b822",
                "H5NeEKb4xmr1y6i3ihijceb4kEfWhb822",
                "WYXTVo3VWoy4Uk65Qxia4HSmZw8oyb822",
                "TEPg6CWSiAD3o3fjDQY1nS9w1adHSb822",
                "Q7Hsk84gLu1BMiWLSpy8KJt29Jj4Hb822",
                "KqL8cUqsKD3koABGJGkaU1D7GhZtyb822",
                "HMTLfNRJkzDhHKaLKKBq3t6Py8auRb822",
                "MtVBFCxHSQw4ZMyiASTHF3xDjxRM1b822",
                "K2FWQJGSFqVuhUKob9V8uybaqbqBrb822",
                "PNBXMWSmFLgm3zCsbwHeNeooxs4z1b822",
                "Ur9sA82vMFqnxhmx725JtLFXu62aVb822",
                "HWXmEyVfoNt3NESF27JH8VZd5nTKnb822",
                "NuhXpTwhPwxtFavkPh8ACdyA37dGeb822",
                "HCRfFFC1zpUumMXr7Y3JRDdGZuZvab822",
                "WF1RmGoT66A5DT1Y7xac9Nh3WBH8Db822",
                "XUfuA9aUmSKJviLLrKpTVwE4LRGTrb822",
                "TtPvc2Lfs2GUCuDDwNHj4j1tcjTAWb822",
                "KbK6eW4XKG6Ut6ARGNwq2YcKZBhsdb822",
                "YL2f5QvRN25LWzoKvPLHy1LGvb6r1b822",
                "MY9bbTCiHCTVrozkFUExpJcHWXeLgb822",
                "WHwHR4AXoKY3obGSPe4m2GZFjRocrb822",
                "N1kLLZPRrxz4xBdEwNbZ3i3Q7PA4ob822",
                "QkuAyrGs8egQPHgLQC6DJn3LMW6hEb822",
                "Tcadx1kCujsb6vuaehcsumz52iCnWb822",
                "JQk81s8bRHMwrRXNp6xvAvWwTLHVMb822",
                "PjFM3o7Si1mtYzQFWTREGen7XFa1Db822",
                "TW8UECAWaFhHVpZqQM9Js1CQiFt9nb822"
        };
        Random rand = new Random();
        int index = rand.nextInt(40);
        return address[index];
    }
}
