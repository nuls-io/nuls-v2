package io.nuls.transaction.db.h2;

import com.fasterxml.jackson.core.JsonProcessingException;
import io.nuls.base.constant.BaseConstant;
import io.nuls.base.data.Address;
import io.nuls.base.data.Page;
import io.nuls.h2.utils.MybatisDbHelper;
import io.nuls.tools.core.ioc.SpringLiteContext;
import io.nuls.tools.crypto.ECKey;
import io.nuls.tools.parse.JSONUtils;
import io.nuls.tools.parse.SerializeUtils;
import io.nuls.transaction.constant.TxConstant;
import io.nuls.transaction.db.h2.dao.TransactionH2Service;
import io.nuls.transaction.db.h2.dao.TransactionService;
import io.nuls.transaction.db.h2.dao.impl.BaseService;
import io.nuls.transaction.db.h2.dao.impl.TransactionH2ServiceImpl;
import io.nuls.transaction.db.h2.dao.impl.TransactionServiceImpl;
import io.nuls.transaction.model.po.TransactionPO;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.h2.util.StringUtils;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.*;

/**
 * @author: Charlie
 * @date: 2018/11/14
 */
public class H2Test {


    public static void before() throws Exception {
        String resource = "mybatis/mybatis-config.xml";
        SpringLiteContext.init("io.nuls");
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(Resources.getResourceAsReader(resource), "druid");
        MybatisDbHelper.setSqlSessionFactory(sqlSessionFactory);


    }

    public static void main(String[] args) throws Exception {
        before();
        testInsert();
//        testSelect();
//        long start = System.currentTimeMillis();
//        initTestTable();
//        System.out.println("花费时间：" + String.valueOf(System.currentTimeMillis() - start));
//
//        start = System.currentTimeMillis();
//        initTestData();
//        //insert();
//        //delete();
//        System.out.println("查询数据花费时间：" + String.valueOf(System.currentTimeMillis() - start));

      /*  for(int i=0;i<20;i++){
            long s = System.currentTimeMillis();
            select();
            System.out.println("查询数据花费时间：" + String.valueOf(System.currentTimeMillis() - s));
        }*/
        //createAddressSameTable();


       /* long s = System.currentTimeMillis();
        select("Y31vkMqXEViqpkZ29vysnDG2XPz9Eb822");
        System.out.println("查询数据花费时间：" + String.valueOf(System.currentTimeMillis() - s));

        s = System.currentTimeMillis();
        select("H5NeEKb4xmr1y6i3ihijceb4kEfWhb822");
        System.out.println("查询数据花费时间：" + String.valueOf(System.currentTimeMillis() - s));

        s = System.currentTimeMillis();
        select("R3yG2Eu8gogomitWoiwbLAriCNWreb822");
        System.out.println("查询数据花费时间：" + String.valueOf(System.currentTimeMillis() - s));

        s = System.currentTimeMillis();
        select("SWrPUjNakX1brqKSszdvUEvtjuQL6b822");
        System.out.println("查询数据花费时间：" + String.valueOf(System.currentTimeMillis() - s));

        s = System.currentTimeMillis();
        select("LD9P3K8GEvfWYmWfUN5BR3zUJu3x7b822");
        System.out.println("查询数据花费时间：" + String.valueOf(System.currentTimeMillis() - s));*/
    }

    private static void testSelect() {
        TransactionService service = new TransactionServiceImpl();
        service.createTxTablesIfNotExists(TxConstant.H2_TX_TABLE_NAME_PREFIX,
                TxConstant.H2_TX_TABLE_INDEX_NAME_PREFIX,
                TxConstant.H2_TX_TABLE_UNIQUE_NAME_PREFIX,
                TxConstant.H2_TX_TABLE_NUMBER);
        Page<TransactionPO> page = service.getTxs("5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu", null, null, null, null, null, null, 1, 10);
        System.out.println(page.getTotal());

    }

    private static void testInsert() {
        TransactionService service = SpringLiteContext.getBean(TransactionService.class);
        TransactionPO tx = createTxPo("5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu");
        service.saveTx(tx);
    }


    private static void delete() {
        Map<String, String> map = new HashMap<>();
        TransactionH2Service ts = new TransactionH2ServiceImpl();
        ts.deleteTx("LD9P3K8GEvfWYmWfUN5BR3zUJu3x7b822", "00bb56982981c1fce2eee1c21db9ab016676ecea5ae047b0f6fa87b15aceba8f1279");
        ts.deleteTx("LD9P3K8GEvfWYmWfUN5BR3zUJu3x7b822", "00847cebbea85690903e2d0a241d2f8b9ea1d088866f9e12271adc62ec9cce33d61c");
        ts.deleteTx("LD9P3K8GEvfWYmWfUN5BR3zUJu3x7b822", "004015dee1d84f73ba41526966369dc4799b699e0ddf39fe04a9c10965469be119e8");
        ts.deleteTx("LD9P3K8GEvfWYmWfUN5BR3zUJu3x7b822", "00dd8d579acd7a623baca24f2a536ecfc76ac2ce282f6023b9fe48e4eaba21a9b4f3");
        ts.deleteTx("LD9P3K8GEvfWYmWfUN5BR3zUJu3x7b822", "009ad54d7175cce9612039aaa14eb00f3630a3953c63948ada08c78fdf60b8e02b3f");
        ts.deleteTx("LD9P3K8GEvfWYmWfUN5BR3zUJu3x7b822", "001d8b2f97b1784a4a96cc3abf09990fca2d269371892fe61d2153d0d42bdb282dcd");
        ts.deleteTx("LD9P3K8GEvfWYmWfUN5BR3zUJu3x7b822", "004b8813936014c4f8d438e3f5c71c82487d98bc188642a473383a6e6e8de9cd9c24");
        ts.deleteTx("LD9P3K8GEvfWYmWfUN5BR3zUJu3x7b822", "005a58f7cc3999073f37698e834010614e42df3f2a30236198ae464e81f0483dbafd");
        ts.deleteTx("LD9P3K8GEvfWYmWfUN5BR3zUJu3x7b822", "00674b95d94e9f8f666a21b76a26f631ec4f37be35d2de918b8fd0778fc8a290c86f");
        ts.deleteTx("LD9P3K8GEvfWYmWfUN5BR3zUJu3x7b822", "00b604d95d1cabfedae438d9fa11caa629eba32a7d80af1f38a530268fa3cb1c8554");

    }

    private static void insert() {
        //saveTxsTables
        TransactionH2Service ts = new TransactionH2ServiceImpl();
        for (int i = 0; i < 1; i++) {
            List<TransactionPO> listPo = new ArrayList<>();
            for (int j = 0; j < 200000; j++) {
                listPo.add(createTxPo());
            }
            ts.saveTxsTables(listPo);
            System.out.println("OK-" + i);
        }
    }

    private static void select(String address) {
        TransactionH2Service ts = new TransactionH2ServiceImpl();
        String addr = StringUtils.isNullOrEmpty(address) ? ranAddress() : address;
//        Page<TransactionPO> page =  ts.getTxs(addr, null, null, 1540138501L, System.currentTimeMillis(), 1,15);
        Page<TransactionPO> page = ts.getTxs(addr, 1, 1, 1, null, null, null, 1, 15);
        try {
            System.out.println(JSONUtils.obj2json(page));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
    }


    public static void initTestData() {
        TransactionH2Service ts = new TransactionH2ServiceImpl();
        for (int i = 0; i < 1; i++) {
            List<TransactionPO> listPo = new ArrayList<>();
            for (int j = 0; j < 10000; j++) {
                listPo.add(createTxPo());
            }
            System.out.println(ts.saveTxs(listPo));
        }
    }

    //模拟TransactionPo的数据
    private static TransactionPO createTxPo() {
        Random rand = new Random();
        int amount = rand.nextInt(5050 - 50 + 1) + 50;
        int time = rand.nextInt(1542514842 - 1541001600 + 1) + 1541001600;
        int stateAndType = rand.nextInt(3);
        int type = rand.nextInt(10) + 1;
        int assetChainId = rand.nextInt(10) + 1;
        int assetId = rand.nextInt(30) + 1;
        TransactionPO txPo = new TransactionPO();

        txPo.setAddress(ranAddress()); //随机表随机地址
//        txPo.setAddress(ranSingleAddress());//表_0中随机地址
        txPo.setHash(getTestHash());

        txPo.setAmount(new BigInteger(amount + ""));
        txPo.setState(stateAndType);
        txPo.setType(type);
        txPo.setTime((long) time);
        txPo.setAssetChainId(assetChainId);
        txPo.setAssetId(assetId);
        return txPo;
    }

    //模拟TransactionPo的数据
    private static TransactionPO createTxPo(String address) {
        Random rand = new Random();
        int amount = rand.nextInt(5050 - 50 + 1) + 50;
        int time = rand.nextInt(1542514842 - 1541001600 + 1) + 1541001600;
        int stateAndType = rand.nextInt(3);
        int type = rand.nextInt(10) + 1;
        int assetChainId = rand.nextInt(10) + 1;
        int assetId = rand.nextInt(30) + 1;
        TransactionPO txPo = new TransactionPO();

        txPo.setAddress(address); //随机表随机地址
//        txPo.setAddress(ranSingleAddress());//表_0中随机地址
        txPo.setHash(getTestHash());

        txPo.setAmount(new BigInteger(amount + ""));
        txPo.setState(stateAndType);
        txPo.setType(type);
        txPo.setTime((long) time);
        txPo.setAssetChainId(assetChainId);
        txPo.setAssetId(assetId);
        return txPo;
    }

    //模拟随机生成交易hash，长度68
    private static String getTestHash() {
        char[] words = {'a', 'b', 'c', 'd', 'e', 'f', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9'};
        //0020b61313f59fbf8b0639b556690d9d6b8f2c89ce1755e6939080ddf13101b452aa
        Random rand = new Random();
        String hash = "00";
        int max = words.length;
        for (int i = 0; i < 66; i++) {
            int index = rand.nextInt(max);
            hash += words[index];
        }
        return hash;
    }

    private static String getAddr() {
        Address address = new Address((short) 8888, BaseConstant.DEFAULT_ADDRESS_TYPE, SerializeUtils.sha256hash160(new ECKey().getPubKey()));
        return address.getBase58();
    }

    //生成,随机地址
    private static void createAddress() {
        System.out.println("{");
        for (int i = 0; i < 100; i++) {
            String addr = getAddr();
            System.out.println("\"" + addr + "\"" + (i == 99 ? "" : ","));
//            System.out.println(addr.hashCode());
//            System.out.println((addr.hashCode() & Integer.MAX_VALUE) % TxConstant.H2_TX_TABLE_NUMBER);

        }
        System.out.println("}");
    }

    //生成,会存入同一张表的地址
    private static void createAddressSameTable() {

        int count = 0;
        System.out.println("{");
        while (count < 40) {
            String addr = getAddr();
            if ((addr.hashCode() & Integer.MAX_VALUE) % TxConstant.H2_TX_TABLE_NUMBER == 0) {
                System.out.println("\"" + addr + "\"" + (count == 39 ? "" : ","));
                count++;
            }
            //System.out.println((addr.hashCode() & Integer.MAX_VALUE) % TxConstant.H2_TX_TABLE_NUMBER);

        }
        System.out.println("}");
    }

    //多表
    private static String ranAddress() {
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
                "TW8UECAWaFhHVpZqQM9Js1CQiFt9nb822",
                "QAHPtq2JWiq5kXGefpeEirCtUjcpCb822",
                "HfLjaQ9i1XmPXiEQyk2XpBj6fvQWmb822",
                "KqGXKbiwrPCJMynrMoTd3j4P5pybjb822",
                "VkChU1XwpTiiPyFtjTJBQFvv9Ud65b822",
                "Tv8sV38QvJgJ5zS5zA2QC1ZuSt1NDb822",
                "XPsyyojJFeD218SA3L49hntHgTfLUb822",
                "JsHSpZdzcKcnspb7wEZvY6QhHZtkEb822",
                "PAng4g6a7KGSL3XNqgRYREHH6cGvNb822",
                "ReT5jGXq51Cy4tFta2Th9bRUThPLgb822",
                "MBXQ8iAdvMkqXk1BtHaZ5RdgNAF7Hb822",
                "QPNXzmaTLcNqjvrywCH7jcCDdGRGEb822",
                "NxDfTav2HdeyDtsjjxD1A3hLswybzb822",
                "RWUDf6zZV3oBoJ4FqqxoER7EZbTzdb822",
                "MXPwFbovGUUYxYg5HgBZdyThHXdvrb822",
                "Sr7Z9pgfTTdYjXeRrH7165SHvC9Khb822",
                "YCTY6W2bXFDnCYCXkydvsswtQGjJZb822",
                "XEc4hqyisiBYrhJYh9F3Y5vyGqyyTb822",
                "MUMqy8UVSunwsn9dcjxK1TYh5EEQSb822",
                "M1sxeDLWJwaZFA1G6k2Wf4ZmtCzCeb822",
                "WjzhKmptFdMoti6sfcNHpryKusELtb822",
                "JfLWVKhSjxYevHQuXWJLSfJXWev2Db822",
                "WZegGGpZQQekKTAyhCppwUMLN42X1b822",
                "QQjwxEZ48uyEUvRe3kL86NRsecskub822",
                "QHCAJJF21nyfed7JXGyZrJkdEcjHub822",
                "QYBMLVDY1FF3UFisU62qkLNCUqSWWb822",
                "VreHT5SWiU5DGCGLzfjut7w4wGZ9qb822",
                "T778iYnCS5reXENBM1dvmT2ZqtXGyb822",
                "QSYVaovN7GmGxdKVVz6jQ9Vv6xAoBb822",
                "QZTjCz5vwosv68knLNZ6b8ftqWr1Rb822",
                "LBkFeyRq3ikcq95Q6bUBBp7CtszB6b822",
                "JrhXbwHhs9nrXPZ8b75RARmocwe9Mb822",
                "X8ZRwenXompq7ku2U7KVsacfp4Bs2b822",
                "M5guVPpHKi6HZR4bsQjDr9HqZsUHLb822",
                "TKHc4mgaL56Xm5HTE93uofbhAQrzmb822",
                "Thuf5e7dB3Rsws93ZUogh5QsCpfZ3b822",
                "M7je5VTv3CQL7VNykkQPdRAkm6Zmob822",
                "Qh57eEyit6nxRLR1W1vzeHHBGV6swb822",
                "H9GRkupDXGzBLJeaUeJzpz4WGbi4gb822",
                "PxcrE7SB1kna2MnscTs4xC43YtvcWb822",
                "WMcDiWhcKoY9P1kVZ1XrMD6dcvoNBb822",
                "YQ8ghAh1XNuCSA86vdpwhistfFqSZb822",
                "WqrKeN5rPaNFYEVt3GAxy1XdtZFoSb822",
                "TrEraGPCRtGUByLefntNp24jMjkS6b822",
                "GujL8hRUhkM2HMsRCHRRYHTmWVfmwb822",
                "MnNh6JhqgxpFRdr89TiAWk3FdNScXb822",
                "L2uuZ7rAJcWZRESCwf95Asz5q9Hezb822",
                "Ko7Q5v2szeUkpHrfVyXFnYLLs3UT9b822",
                "RktgTo4bjWM2bqvPF4muvfxqBAuQ3b822",
                "Uu3vMyYEn3y452nUtpsE5wLoJpQEQb822",
                "NUp7guA54Cdg3hzKZGCSuJvm2iUh7b822",
                "YGfnmDVXBqo1xR6M5rsFm1sEZb4Ndb822",
                "HTVfcCvLscY6fnCnbi4VhBU88dsiMb822",
                "JxLpDafP35xdqzsnkdLG5Zf8mANpsb822",
                "M2Uzng3SZus7Fwqy6vvzT5PSdgvNrb822",
                "V5MPbfYtubiLtYpQS7dtmA9PUsb7bb822",
                "MtAx4iCog9xwGFUKPJMSjk388EBcKb822",
                "K74aQozZGsQmWzFye5GRNUA4ACf2kb822",
                "TB5K1byzPSKUmW1sfyUQGhfa8KPf3b822",
                "PnUxokA4yKnfABeZRVPSnqQahTrkwb822",
                "Vc5i1NpNg6NdLeJWn1nSbrKa5aNXkb822",
                "XLCfizWX3qmQywqF69MYo1hHTxrLxb822",
                "SgZJgZEn13GEP1vaNUhhMxY8f61dyb822",
                "LVHxbGng64DjKVdVm1tuP8f6Tuqnfb822",
                "HyZTdr4At4xSoCS4F1gwzkKgkShLPb822",
                "GoJ6gHAmriuVxrzkjccAqL1cmgguWb822",
                "QkDkYxjFZY4K4Hxt4jMX3Nkt9d526b822",
                "SzyqEMSPeGyiNccBgvbVrtHMjXUUTb822",
                "S3xe62ZWQAFfac4s44gTgFCQFfLaRb822",
                "M46GTFF9Ks5J4L7u3x2WbyP5sAGoXb822",
                "TVX4v4iQMJDmAsUpbJVkhYhSr4DJDb822",
                "Nyr6DbmzfgGF6sv2DTtkxv1GJm8Jtb822",
                "H3e3PNeZWxiHnpKqbxSTeSeacDd8ob822",
                "XwN5xSK7GuGdoXGho6hLkrmKxUYFYb822",
                "Vk2p2ErnndfbvGR8myM8esYutHsQvb822",
                "VhSBg6xMZvpr9uQUk14D4DRxvrsZ5b822",
                "RdRbaoL5ENvYVWed6vdUy4L3bDuqdb822",
                "TBwsoA2JhqevWxZ6SqqHRXNgPRuzCb822",
                "LWECENS2VxZduwkyj2Tx4iSXi4fPib822",
                "Y5rmdXheL37ukR75aHeZu9WTFNCbwb822",
                "L25iJKaYLdjygajfU9LjJcDAvhykUb822",
                "RidUSKSjQtGpQziNCGJNxXfzHEGhHb822",
                "H9rPrUbuQiFuc46wv5H4gFrXgVctqb822",
                "UsicHUJydKoAJD7iLixHuxn8a9ggHb822",
                "Vq4hJkw8bFiZGjYZaNWtrUvSa7aNUb822",
                "Qt98Zm2Ssykp6vqDQ4vwMMh2CkVJab822",
                "HSvQ9ZUiBMzVmZ28uJDk6RZZtAajUb822",
                "QjbnV8isRgkubgj1LXB9Dr2gjsy5ab822",
                "MFA23EyhrQdzaZQCPKofzq1Mrn3Tob822",
                "PkDoERiK6wbKH2wsB17bq2QANqYZWb822",
                "JemAdnnwNEgV5P2HaC2ewYF7rhhE1b822",
                "KWqptmdYCaPUkvFX7hA6xgdSmYUGsb822",
                "RpG8HtNrScZ9rvCw6cL9c9z5PisX4b822",
                "LvXSCWphKjpuSJ2hcgUJ8jfucHCt3b822",
                "L2d95e1geRaWTQgNhS5GCtQokrnYzb822",
                "YLCgt9YU4K6cdmk81qMQNZtrX5ZdLb822",
                "Nd5jst19pc5JXnx35WabFEf3f353nb822",
                "NmmPyovYwKXZdkMupe39fMmL9ep8ob822",
                "PF5tsY8aDqx92HqbgpPBH72JqFAFyb822",
                "NUybH9iv3nwHKB1ptfSKr5XcUAYYab822",
                "WM49cchwjE1kk16zkks4BZHQjR7seb822"
        };
        Random rand = new Random();
        int index = rand.nextInt(140);
        return address[index];
    }

    //单表
    private static String ranSingleAddress() {
        //40个种子地址，随机生成交易记录；
        String[] address = {
                "",
        };
        Random rand = new Random();
        int index = rand.nextInt(3);
        return address[index];
    }

    public static void initTestTable() {
        TransactionH2Service ts = new TransactionH2ServiceImpl();
        //ts.createTable("transaction", "transaction_index",128);
        ts.createTxTablesIfNotExists(TxConstant.H2_TX_TABLE_NAME_PREFIX,
                TxConstant.H2_TX_TABLE_INDEX_NAME_PREFIX,
                TxConstant.H2_TX_TABLE_UNIQUE_NAME_PREFIX,
                TxConstant.H2_TX_TABLE_NUMBER);
    }
}
