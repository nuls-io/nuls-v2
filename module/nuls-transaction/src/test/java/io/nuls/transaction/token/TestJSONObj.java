package io.nuls.transaction.token; /**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import io.nuls.core.parse.JSONUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author: Charlie
 * @date: 2019/4/16
 */
public class TestJSONObj {

    public static List<AccountData> getAccountTempList() throws Exception{
        return JSONUtils.json2list("", AccountData.class);
    }

    public List<AccountData> readStream() {
        List<AccountData> accountDataList = new ArrayList<>();
        try {
            //方式一：将文件放入Transaction模块test的resources中,json格式 只保留list部分
            InputStream inputStream = getClass().getClassLoader().getResource("alpha2.json").openStream();
            //方式二：定义文件目录
            //InputStream inputStream = new FileInputStream("E:/IdeaProjects/nuls_2.0/module/nuls-transaction/src/test/resources/alpha2.json");
            JsonReader reader = new JsonReader(new InputStreamReader(inputStream, "UTF-8"));
            Gson gson = new GsonBuilder().create();
            reader.beginArray();
            while (reader.hasNext()) {
                AccountData accountData = gson.fromJson(reader, AccountData.class);
                accountDataList.add(accountData);
            }
            reader.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return accountDataList;
    }


    public static void main(String[] args) throws Exception{
        TestJSONObj testJSONObj = new TestJSONObj();
        List<AccountData> accountDataList = testJSONObj.readStream();
        System.out.println(JSONUtils.obj2PrettyJson(accountDataList));
    }

}
