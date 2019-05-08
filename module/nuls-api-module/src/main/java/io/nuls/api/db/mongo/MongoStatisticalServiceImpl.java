/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.api.db.mongo;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import io.nuls.api.db.StatisticalService;
import io.nuls.api.model.po.db.KeyValue;
import io.nuls.api.model.po.db.StatisticalInfo;
import io.nuls.api.utils.DocumentTransferTool;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.model.DoubleUtils;
import org.bson.Document;

import java.math.BigInteger;
import java.util.*;

import static com.mongodb.client.model.Filters.*;
import static io.nuls.api.constant.DBTableConstant.*;

/**
 * @author Niels
 */
@Component
public class MongoStatisticalServiceImpl implements StatisticalService {
    @Autowired
    private MongoDBService mongoDBService;

    public long getBestId(int chainId) {
        Document document = mongoDBService.findOne(STATISTICAL_TABLE + chainId, Filters.eq("_id", LAST_STATISTICAL_TIME));
        if (null == document) {
            return -1;
        }
        return document.getLong("value");
    }

    public void saveBestId(int chainId, long id) {
        Document document = new Document();
        document.put("_id", LAST_STATISTICAL_TIME);
        document.put("value", id);
        mongoDBService.insertOne(STATISTICAL_TABLE + chainId, document);
    }

    public void updateBestId(int chainId, long id) {
        Document document = new Document();
        document.put("_id", LAST_STATISTICAL_TIME);
        document.put("value", id);
        mongoDBService.updateOne(STATISTICAL_TABLE + chainId, Filters.eq("_id", LAST_STATISTICAL_TIME), document);
    }

    public void insert(int chainId, StatisticalInfo info) {
        Document document = DocumentTransferTool.toDocument(info, "time");
        mongoDBService.insertOne(STATISTICAL_TABLE + chainId, document);
    }

    public long calcTxCount(int chainId, long start, long end) {
        long count = this.mongoDBService.getCount(TX_TABLE + chainId, and(gte("createTime", start), lte("createTime", end)));
        return count;
    }

    /**
     * @param type 0:14天，1:周，2：月，3：年，4：全部
     * @return
     */
    public List getStatisticalList(int chainId, int type, String field) {
        List<KeyValue> list = new ArrayList<>();
        long startTime = getStartTime(type);
        List<Document> documentList = mongoDBService.query(STATISTICAL_TABLE + chainId, gte("_id", startTime), Sorts.ascending("_id"));
        if (documentList.size() < 32) {
            for (Document document : documentList) {
                KeyValue keyValue = new KeyValue();
                keyValue.setKey(document.get("month") + "/" + document.get("date"));
                if (ANNUALIZE_REWARD.equals(field)) {
                    keyValue.setValue(document.getDouble(field));
                } else if (CONSENSUS_LOCKED.equals(field)) {
                    keyValue.setValue(new BigInteger(document.getString(field)));
                } else {
                    keyValue.setValue(document.getLong(field));
                }
                list.add(keyValue);
            }
        } else {
            if (TX_COUNT.equals(field)) {
                summaryLong(list, documentList, field);
            } else if (ANNUALIZE_REWARD.equals(field)) {
                avgDouble(list, documentList, field);
            } else if (CONSENSUS_LOCKED.equals(field)) {
                avgBigInteger(list, documentList, field);
            } else {
                avgLong(list, documentList, field);
            }
        }
        return list;
    }

    private void summaryLong(List<KeyValue> list, List<Document> documentList, String field) {
        List<String> keyList = new ArrayList<>();
        Map<String, Long> map = new HashMap<>();

        for (Document document : documentList) {
            String key = document.get("year") + "/" + document.get("month");
            Long value = map.get(key);
            if (null == value) {
                value = 0L;
                keyList.add(key);
            }
            value += document.getLong(field);
            map.put(key, value);
        }
        for (String key : keyList) {
            KeyValue keyValue = new KeyValue();
            keyValue.setKey(key);
            keyValue.setValue(map.get(key));
            list.add(keyValue);
        }
    }

    private void avgBigInteger(List<KeyValue> list, List<Document> documentList, String field) {
        List<String> keyList = new ArrayList<>();
        Map<String, List<BigInteger>> map = new HashMap<>();
        for (Document document : documentList) {
            String key = document.get("year") + "/" + document.get("month");
            List<BigInteger> value = map.get(key);
            if (null == value) {
                value = new ArrayList<>();
                keyList.add(key);
                map.put(key, value);
            }
            value.add(new BigInteger(document.get(field).toString()));
        }
        for (String key : keyList) {
            KeyValue keyValue = new KeyValue();
            keyValue.setKey(key);
            BigInteger value = BigInteger.ZERO;
            List<BigInteger> valueList = map.get(key);
            for (BigInteger val : valueList) {
                value = value.add(val);
            }

            keyValue.setValue(value.divide(new BigInteger(valueList.size() + "")));
            list.add(keyValue);
        }

    }

    private void avgLong(List<KeyValue> list, List<Document> documentList, String field) {
        List<String> keyList = new ArrayList<>();
        Map<String, List<Long>> map = new HashMap<>();

        for (Document document : documentList) {
            String key = document.get("year") + "/" + document.get("month");
            List<Long> value = map.get(key);
            if (null == value) {
                value = new ArrayList<>();
                keyList.add(key);
                map.put(key, value);
            }
            value.add(Long.parseLong(document.get(field) + ""));
        }
        for (String key : keyList) {
            KeyValue keyValue = new KeyValue();
            keyValue.setKey(key);
            long value = 0;
            List<Long> valueList = map.get(key);
            for (long val : valueList) {
                value += val;
            }
            keyValue.setValue(value / valueList.size());
            list.add(keyValue);
        }
    }

    private void avgDouble(List<KeyValue> list, List<Document> documentList, String field) {
        List<String> keyList = new ArrayList<>();
        Map<String, List<Double>> map = new HashMap<>();

        for (Document document : documentList) {
            String key = document.get("year") + "/" + document.get("month");
            List<Double> value = map.get(key);
            if (null == value) {
                value = new ArrayList<>();
                keyList.add(key);
                map.put(key, value);
            }
            value.add(document.getDouble(field));
        }
        for (String key : keyList) {
            KeyValue keyValue = new KeyValue();
            keyValue.setKey(key);
            double value = 0;
            List<Double> valueList = map.get(key);
            for (double val : valueList) {
                value += val;
            }
            keyValue.setValue(DoubleUtils.div(value, valueList.size(), 2));
            list.add(keyValue);
        }
    }

    private long getStartTime(int type) {
        if (4 == type) {
            return 0;
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        switch (type) {
            case 0:
                calendar.add(Calendar.DATE, -14);
                break;
            case 1:
                calendar.add(Calendar.DATE, -7);
                break;
            case 2:
                calendar.add(Calendar.MONTH, -1);
                break;
            case 3:
                calendar.add(Calendar.YEAR, -1);
                break;
            default:
        }
        return calendar.getTime().getTime();
    }
}
