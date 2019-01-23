/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.tools.storage;

import io.nuls.db.model.Entry;
import io.nuls.db.service.BatchOperation;
import io.nuls.db.service.RocksDBService;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import static io.nuls.db.service.RocksDBService.batchPut;
import static io.nuls.db.service.RocksDBService.createTable;
import static io.nuls.db.service.RocksDBService.delete;
import static io.nuls.db.service.RocksDBService.deleteKeys;
import static io.nuls.db.service.RocksDBService.destroyTable;
import static io.nuls.db.service.RocksDBService.entryList;
import static io.nuls.db.service.RocksDBService.get;
import static io.nuls.db.service.RocksDBService.keyList;
import static io.nuls.db.service.RocksDBService.listTable;
import static io.nuls.db.service.RocksDBService.multiGet;
import static io.nuls.db.service.RocksDBService.multiGetValueList;
import static io.nuls.db.service.RocksDBService.put;
import static io.nuls.db.service.RocksDBService.valueList;
import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Created by qinyf on 2018/10/10.
 */
public class RocksDBTest {

    private static String table;
    private static String key;

    @Test
    public void test() throws Exception {
        table = "test-table";
        key = "test-key";
        initTest();
        //createTableTest();
        //existTableTest();
        //destroyTableTest();
        //listTableTest();
        //putTest();
        //getTest();
        //deleteTest();
        //multiGetTest();
        //multiGetValueListTest();
        //keyListTest();
        //valueListTest();
        //entryListTest();
        //batchPutTest();
        //deleteKeysTest();
        //executeBatchTest();
    }

    @Ignore
    @Test
    public void initTest() throws Exception {
        String dataPath = "../../data";
        long start = System.currentTimeMillis();
        RocksDBService.init(dataPath);
        long end = System.currentTimeMillis();
        System.out.println("数据库连接初始化测试耗时：" + (end - start) + "ms");
    }

    /**
     * 判断数据表是否存在
     */
    @Ignore
    @Test
    public void existTableTest() {
        String tableName = table;//account chain
        boolean result = false;
        try {
            result = RocksDBService.existTable(tableName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(result);
        Assert.assertEquals(true, result);
    }

    /**
     * 创建数据表
     */
    @Ignore
    @Test
    public void createTableTest() {
        String tableName = table;//account chain
        boolean result = false;
        try {
            result = RocksDBService.createTable(tableName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(result);
        Assert.assertEquals(true, result);
    }

    /**
     * 删除数据表
     */
    @Ignore
    @Test
    public void destroyTableTest() {
        String tableName = "user";
        boolean result = false;
        try {
            result = RocksDBService.destroyTable(tableName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertEquals(true, result);
    }

    /**
     * 查询所有表名
     */
    @Ignore
    @Test
    public void listTableTest() {
        String testTable = "testListTable";
        try {
            createTable(testTable);
            String[] tables = listTable();
            boolean exist = false;
            for (String table : tables) {
                if (table.equals(testTable)) {
                    exist = true;
                    //break;
                }
                System.out.println("table: " + table);
            }
            Assert.assertTrue("create - list tables failed.", exist);
            put(testTable, key.getBytes(UTF_8), "testListTable".getBytes(UTF_8));
            String getValue = new String(get(testTable, key.getBytes(UTF_8)), UTF_8);
            Assert.assertEquals("testListTable", getValue);
            destroyTable(testTable);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Ignore
    @Test
    public void putTest() {
        String value = "testvalue";
        try {
            put(table, key.getBytes(UTF_8), value.getBytes(UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        String getValue = new String(get(table, key.getBytes(UTF_8)), UTF_8);
        Assert.assertEquals(value, getValue);
    }

    @Ignore
    @Test
    public void getTest() {
        String value = "testvalue";
        byte[] getByte = get(table, key.getBytes(UTF_8));
        if (getByte != null) {
            String getValue = new String(getByte, UTF_8);
            Assert.assertEquals(value, getValue);
        }
    }

    @Ignore
    @Test
    public void deleteTest() {
        try {
            delete(table, key.getBytes(UTF_8));
        } catch (Exception e) {
            e.printStackTrace();
        }
        Assert.assertNull(get(table, key.getBytes(UTF_8)));
    }

    @Ignore
    @Test
    public void multiGetTest() {
        //String value = "testvalue";
        //String getValue = new String(get(table, key.getBytes(UTF_8)), UTF_8);
        try {
            put(table, "key1".getBytes(), "value1".getBytes());
            put(table, "key2".getBytes(), "value2".getBytes());
            put(table, "key3".getBytes(), "value3".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<byte[]> keyBytes = new ArrayList<>();
        Map<String, String> result = new HashMap<>();
        keyBytes.add("key1".getBytes());
        keyBytes.add("key2".getBytes());
        keyBytes.add("key3".getBytes());
        //keyBytes size不能大于65536，否则查询结果为空
        Map<byte[], byte[]> map = multiGet(table, keyBytes);
        for (Map.Entry<byte[], byte[]> entry : map.entrySet()) {
            result.put(new String(entry.getKey()), new String(entry.getValue()));
            System.out.println(new String(entry.getKey()) + "==" + new String(entry.getValue()));
        }
    }

    @Ignore
    @Test
    public void multiGetValueListTest() {
        try {
            put(table, "key1".getBytes(), "value11".getBytes());
            put(table, "key2".getBytes(), "value22".getBytes());
            put(table, "key3".getBytes(), "value33".getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        }
        List<byte[]> keyBytes = new ArrayList<>();
        Map<String, String> result = new HashMap<>();
        keyBytes.add("key1".getBytes());
        keyBytes.add("key2".getBytes());
        keyBytes.add("key3".getBytes());
        List<byte[]> list = multiGetValueList(table, keyBytes);
        for (byte[] value : list) {
            System.out.println(new String(value));
        }
    }

    @Ignore
    @Test
    public void keyListTest() {
        long start = System.currentTimeMillis();
        List<byte[]> list = keyList(table);
        long end = System.currentTimeMillis();
        if (list != null) {
            System.out.println(list.size() + "查询测试耗时：" + (end - start) + "ms");
            for (byte[] value : list) {
                System.out.println(new String(value));
            }
        }
    }

    @Ignore
    @Test
    public void valueListTest() {
        List<byte[]> list = valueList(table);
        for (byte[] value : list) {
            System.out.println(new String(value));
        }
    }

    @Ignore
    @Test
    public void entryListTest() {
        List<Entry<byte[], byte[]>> list = entryList(table);
        for (Entry<byte[], byte[]> entry : list) {
            System.out.println(new String(entry.getKey()) + "===" + new String(entry.getValue()));
        }
    }

    @Ignore
    @Test
    public void batchPutTest() {
        List<byte[]> list = new ArrayList<>();
        Map<byte[], byte[]> insertMap = new HashMap<>();
        Map<byte[], byte[]> updateMap = new HashMap<>();
        for (int i = 0; i < 65536; i++) {
            list.add(randomstr().getBytes());
            insertMap.put(list.get(i), ("rocksDB批量新增测试-" + i + "-" + System.currentTimeMillis()).getBytes());
            updateMap.put(list.get(i), ("rocksDB批量修改测试-" + i + "-" + System.currentTimeMillis()).getBytes());
        }
        //批量添加测试
        {
            long start = System.currentTimeMillis();
            try {
                batchPut(table, insertMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
            System.out.println(list.size() + "次批量添加测试耗时：" + (end - start) + "ms");
            //System.out.println("last insert data======" + new String(get(table,list.get(list.size() - 1))));
        }
        //批量修改测试
        {
            long start = System.currentTimeMillis();
            try {
                batchPut(table, updateMap);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
            System.out.println(list.size() + "次批量修改测试耗时：" + (end - start) + "ms");
        }
//        //批量查询测试
//        {
//            long start = System.currentTimeMillis();
//            Map<byte[], byte[]> map = multiGet(table, list);
//            long end = System.currentTimeMillis();
//            System.out.println(map.size() + "次批量查询测试耗时：" + (end - start) + "ms");
//        }
        //批量删除测试
        {
            long start = System.currentTimeMillis();
            try {
                deleteKeys(table, list);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
            System.out.println(list.size() + "次批量删除测试耗时：" + (end - start) + "ms");
        }
    }

    @Ignore
    @Test
    public void deleteKeysTest() {
        List<byte[]> list = keyList(table);
        if (list != null) {
            long start = System.currentTimeMillis();
            try {
                deleteKeys(table, list);
            } catch (Exception e) {
                e.printStackTrace();
            }
            long end = System.currentTimeMillis();
            System.out.println(list.size() + "次批量删除测试耗时：" + (end - start) + "ms");
            list = keyList(table);
            Assert.assertEquals(0, list.size());
        }
    }

    @Ignore
    @Test
    public void executeBatchTest() {
        //批量操作测试
        {
            try {
                BatchOperation batch = RocksDBService.createWriteBatch(table);
                batch.put("key111".getBytes(), "value111".getBytes());
                batch.put("key222".getBytes(), "value222".getBytes());
                batch.put("key333".getBytes(), "value333".getBytes());
                batch.put("key444".getBytes(), "value444".getBytes());
                batch.put("key222".getBytes(), "value22222".getBytes());
                batch.delete("key444".getBytes());
                batch.executeBatch();
                System.out.println("query data======" + new String(get(table, "key222".getBytes())));
                System.out.println("query deleted data======" + get(table, "key444".getBytes()));

                RocksDBService.createTable("account");
                BatchOperation batch2 = RocksDBService.createWriteBatch("account");
                batch2.put("key111".getBytes(), "value111".getBytes());
                batch2.executeBatch();
                System.out.println("query data======" + new String(get("account", "key111".getBytes())));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 随机生成不重复的key
     *
     * @return
     */
    private static String randomstr() {
        Random ran1 = new Random();
        char buf[] = new char[255];
        int r1 = ran1.nextInt(100);
        for (int i = 0; i < 255; ++i) {
            buf[i] = (char) ('A' + (ran1.nextInt(100) % 26));
        }
        return new String(buf);
    }
}
