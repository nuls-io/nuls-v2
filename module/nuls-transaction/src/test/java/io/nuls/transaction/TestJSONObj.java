package io.nuls.transaction; /**
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

import io.nuls.core.parse.JSONUtils;

import java.util.List;

/**
 * @author: Charlie
 * @date: 2019/4/16
 */
public class TestJSONObj {
    public static void main(String[] args) throws Exception{
        List<AccountTemp> list = getAccountTempList();
        System.out.println(list.size());

    }

    public static List<AccountTemp> getAccountTempList() throws Exception{
        return JSONUtils.json2list("[\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMhZpsphpnPPd9zoH27BvMSCmvGP3WJ\",\n" +
                "                    \"alias\": \"kevin\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 10612,\n" +
                "                    \"totalOut\": 6800000,\n" +
                "                    \"totalIn\": 148353353749184,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 170453347049184,\n" +
                "                    \"totalBalance\": 148353346949184,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN1qrJpycgikzgz9CMGCWAbiXXS2qjd,PG\",\n" +
                "                         \"tNULSeBaMvpcTwtnEQVKBCxGvSSy8M9y69Nocb,KV\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMvjVJ4zxmjSmnpXyurV9rsfn98F9yr\",\n" +
                "                    \"alias\": \"ln\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 109,\n" +
                "                    \"totalOut\": 100000,\n" +
                "                    \"totalIn\": 109989799700000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 109989799600000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrV62RjpZKx7GseRwhEg982Cqpptuf\",\n" +
                "                    \"alias\": \"vivi\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 4818,\n" +
                "                    \"totalOut\": 200000,\n" +
                "                    \"totalIn\": 100902522174223,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 100902521974223,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn\",\n" +
                "                    \"alias\": \"niels\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 15175,\n" +
                "                    \"totalOut\": 200000,\n" +
                "                    \"totalIn\": 76982139638184,\n" +
                "                    \"consensusLock\": 70000000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 4126806574855,\n" +
                "                    \"totalBalance\": 76982139438184,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMsLENwW4s1WsofHGYmziu5amSxu7eG\",\n" +
                "                    \"alias\": \"pen\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 4525,\n" +
                "                    \"totalOut\": 300000,\n" +
                "                    \"totalIn\": 76834271722425,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 76834271422425,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaMvpcTwtnEQVKBCxGvSSy8M9y69Nocb,KV\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2\",\n" +
                "                    \"alias\": \"tag123\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1030,\n" +
                "                    \"totalOut\": 800000,\n" +
                "                    \"totalIn\": 60215813878223,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 60215812478223,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMuLbKskfSyCeZuqNo5SeLwF9nZFnGw\",\n" +
                "                    \"alias\": \"kevin02\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1033,\n" +
                "                    \"totalOut\": 200000,\n" +
                "                    \"totalIn\": 54231859520119,\n" +
                "                    \"consensusLock\": 22000000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 31990486657426,\n" +
                "                    \"totalBalance\": 54231859120119,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrtczgLEDwvxw4DRCtBVhqtoy2PLUC\",\n" +
                "                    \"alias\": \"lanjs\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 9,\n" +
                "                    \"totalOut\": 200000,\n" +
                "                    \"totalIn\": 49998899800000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 49998899600000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMhErrYpe3YiJGfHVc3ziW8iVUC2GkF\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 46000000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 46000000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMhp9ZYgXJhCT7iLaxx5JS7Xene6h8A\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2089,\n" +
                "                    \"totalOut\": 300000,\n" +
                "                    \"totalIn\": 30238018772084,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 30238018472084,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMhEVNrVyEH1fC94Vi7bZ1K3hTPirAm\",\n" +
                "                    \"alias\": \"siwei\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 4194,\n" +
                "                    \"totalOut\": 200000,\n" +
                "                    \"totalIn\": 25623071543132,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 25623071343132,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMkzTffKDZLy9v5SWbooFFhefwM4dff\",\n" +
                "                    \"alias\": \"jyc_1553704394623\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 3,\n" +
                "                    \"totalOut\": 100000,\n" +
                "                    \"totalIn\": 24999999900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 24999999800000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMgS1a9yNF2mVAY8qs4R7riTyd2J1Kp\",\n" +
                "                    \"alias\": \"nuls_gold\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 108,\n" +
                "                    \"totalOut\": 200000,\n" +
                "                    \"totalIn\": 24999899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 24999899700000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjiyLX3aPdq9oanVhfFb9hp114YEgX\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 18898,\n" +
                "                    \"totalOut\": 600000,\n" +
                "                    \"totalIn\": 24466823971530,\n" +
                "                    \"consensusLock\": 24254000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 180310734010,\n" +
                "                    \"totalBalance\": 24466823371530,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMqA829tXvyeqyCEPxPRfUzxVsdW6qV\",\n" +
                "                    \"alias\": \"mick_one\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 6143,\n" +
                "                    \"totalOut\": 200000,\n" +
                "                    \"totalIn\": 23995378940286,\n" +
                "                    \"consensusLock\": 22000000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 1272084591198,\n" +
                "                    \"totalBalance\": 23995378740286,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMu3nwXeMYmKhuNgPVtoeGryjBmUJ2H\",\n" +
                "                    \"alias\": \"sebytza05\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 5328,\n" +
                "                    \"totalOut\": 100000,\n" +
                "                    \"totalIn\": 23840839834288,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 23840839734288,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMv3w8aEmduJhUnqVv7jWcFQMetPkF8\",\n" +
                "                    \"alias\": \"xiaoxin\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2189,\n" +
                "                    \"totalOut\": 200000,\n" +
                "                    \"totalIn\": 23302688306433,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 23302688106433,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMqWUEG9LxyCqP1KsC9jLnegxkN87Lo\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 23000000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 23000000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMr8898iVf7qaF2GmZBnhwgKFU38doJ\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 23000000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 23000000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMt4tmvDe22egZS9XEHpX4bBX19djP5\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 23000000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 23000000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrmSXiZbevJQHGczhbDCMoLCNusvMS\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 23000000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 23000000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMhWkbJUbTwLw2HeMzhcMXP2D51Tuzx\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 23000000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 23000000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 72,\n" +
                "                    \"totalOut\": 16700000,\n" +
                "                    \"totalIn\": 22965505436925,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 22965488736925,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN8JQ35W3UifG1V8X9n4cYuob77wBB5,KongQiBi\",\n" +
                "                         \"tNULSeBaMzCeoukqhMvWYpCZAbKCN2fE2JVzfK,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMr86kzopdD2N7GvVuUNNBD6zWPxzfR\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 22001000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 22001000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjMGBiEWBjMX5j9PcSe8qtmhRPs8gp\",\n" +
                "                    \"alias\": \"jyc_1553702950931\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 20,\n" +
                "                    \"totalOut\": 400000,\n" +
                "                    \"totalIn\": 22000100300000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 22000099700000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrAdGEPqUHC7WEvRDnkpX7WcnwZLqu\",\n" +
                "                    \"alias\": \"xiaomai\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 111,\n" +
                "                    \"totalOut\": 100000,\n" +
                "                    \"totalIn\": 20998699700000,\n" +
                "                    \"consensusLock\": 2000000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 18998699600000,\n" +
                "                    \"totalBalance\": 20998699600000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 31,\n" +
                "                    \"totalOut\": 16700000,\n" +
                "                    \"totalIn\": 20000109725675,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 20000093025675,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN862uW2juuHsByzpZ9wpfH6ZaGthzB,KongQiBi\",\n" +
                "                         \"tNULSeBaNBawLTvbi4yokiXsFLkiNe8x8XH7Ac,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrZNEmsTVXDBey7Edvod6YNSJzRbfS\",\n" +
                "                    \"alias\": \"zhoulijun1\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2200,\n" +
                "                    \"totalOut\": 100000,\n" +
                "                    \"totalIn\": 10050508690822,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 10050508590822,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMfEKKL6p9UuRpZ8FxgsmJqPSPKaW67\",\n" +
                "                    \"alias\": \"zhoulijun\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 3,\n" +
                "                    \"totalOut\": 100000,\n" +
                "                    \"totalIn\": 9999899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 9999899800000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMn29hLvHc8ZsRZ25JfdSCq99VZkWFw\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 18969,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 3894587428843,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 2161019938980,\n" +
                "                    \"totalBalance\": 3894587428843,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjWuGgCGG5v7XriHBPvw2gdCFUrsiV\",\n" +
                "                    \"alias\": \"ned\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 19780,\n" +
                "                    \"totalOut\": 500000,\n" +
                "                    \"totalIn\": 2938121163249,\n" +
                "                    \"consensusLock\": 2000000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 880157170849,\n" +
                "                    \"totalBalance\": 2938120663249,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMnG8hGcyeygVeyaL5cXV38bQm1rw9M\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 36,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 2500029609850,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 2500029609850,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMigwBrvikwVwbhAgAxip8cTScwcaT8\",\n" +
                "                    \"alias\": \"charlie\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 3,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 2399909900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 2399909900000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMkKxoboxwejrCJyy16zLWwMqWkyqEu\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 4289,\n" +
                "                    \"totalOut\": 100000,\n" +
                "                    \"totalIn\": 2102721913072,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 2102721813072,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrucKmBUxXVSeyQhdCwGHQHSus7yWg\",\n" +
                "                    \"alias\": \"xiaomai2\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 2000899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 2000899900000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 4,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999999900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999999900000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMj8XfWDjyKHZ1ybC3ShR8qKGyVKRcb\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 20,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000007434975,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999996434975,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN5wu9iPmHmo7V8zQHMMFZyCdQVDuzP,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjcximfy1JEGzjxodNMjrjydWuiffr\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 20,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000007034425,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999996034425,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN7nPwpUiDuMUv8s3KP1qvmrCfNcqeh,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMgTcqskhNrE1ZSt3kZpdAv6B83npXE\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 20,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000007028400,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999996028400,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN8xftpyYR41ukVejwQx1EXv5LrkU1j,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMfMk3RGzotV3Dw788NFTP52ep7SMnJ\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 20,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000006992175,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999995992175,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN7JyE4GmncaTtP7gZmab5yZm4E8xYb,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMoixxbUovqmzPyJ2AwYFAX2evKbuy9\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005665175,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994665175,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaNAFyLfA9BUh37PYcJoTKmPxoodGG1N,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMvGmZSrFyQHptSL9yBCNSDfhWoxEHF\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005665175,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994665175,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN5zm2pVT1b5wxhyeLgWgNTQTgo6pPN,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMvaRhahBAYkZKQFhiSqcC67UiRzoSA\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005665175,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994665175,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaMvmYCoPN5sEvjm9u6uQaxKddd3PH1a,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMfXDQeT4MJZim1RusCJRPx5j9bMKQN\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005662325,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994662325,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaMxvoBpyCkXKVWjoXYXeVZsrxUGjFDV,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMqwycXLTWtjexSHHfa4jDTrVq9FMWE\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005662325,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994662325,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN1Y6guJY5dnr4PCbmqFTXNcTstVbhy,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMiKWTid5Gj3FoqBFP7WomUzgumVeKc\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005659475,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994659475,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN71frdeMUeAWZgkitD56xv1omQhRvU,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMk52mfhacRWkmB98PrwCVXuEzCdQuk\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005659475,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994659475,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaMzZomEiwH7pWdzJonekAfvEZFY4oae,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMuk5jx12ZXhaf5HLgcAr3WCwUhRGfT\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005657625,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994657625,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaMwgpXDnh7YZgi7Bi3pHvsX2GRfJgSL,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMmbiCH5soCFasXnG4TwqknyTzYBM3S\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005622925,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994622925,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN63UW3syqccMXY1cz4Utajr1pYLvDo,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMkzsRE6qc9RVoeY6gHq8k1xSMcdrc7\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005620075,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994620075,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaMy6yzt6Z3hqPcNTEMa7JoHAeogrH7i,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMsUBLVxwoaswjWvghJyoUJfbfB6dja\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005616175,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994616175,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaNBshfGi9eYvzQauMio7jvXK8havrzu,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMv8q3pWzS7bHpQWW8yypNGo8auRoPf\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005580675,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994580675,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaNBs7FmRVrf2925XvUZe6DJwMjYzJPo,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMtkzQ1tH8JWBGZDCmRHCmySevE4frM\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005580675,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994580675,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaNA2pJxdN8Ui5wuEnymFcF91NrxdL3v,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMhKaLzhQh1AhhecUqh15ZKw98peg29\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005578825,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994578825,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaMvtKgRAGsmEE3V91jSttxxsFtMNQ69,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMqjT3y9bGz4gBeJ7FJujmxBDTGdNp1\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005262775,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994262775,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN5CudjrrEMMskRF9JaLgzDpvZnN2Fq,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMpaiBiMHWfAeTzdXhnfJXPfwXwKikc\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005262775,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994262775,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN8mMezWfv8xwS8ywbE4kB13Bg1SHHy,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMi5yGkDbDgKGGX8TGxYdDttZ4KhpMv\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005262775,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994262775,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN3TUmG6ZDB6hv9yhe1zDALozziTQYb,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrL5netZkTo9FZb86xGSk47kq6TRBR\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005262450,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994262450,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaMy6gkMZQWFsQhaa4FQtsG2nSrnhFSg,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMqjttJV62GZ1iXVFDBudet3ey2aYSB\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005260925,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994260925,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN2KDRw8z4Muid4ayLjQSS3JA3g7dma,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMh4VafNqp5TJSmV5ogdZviq1nbXBSu\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005259925,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994259925,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaNBWoEgy5NvaPQyXGRqJt6esWZoWD4s,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMtgmrSYu98QwP1Mv8G5FwaMDkWSkuy\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005259925,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994259925,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaMx7jarG7Rntj6uQ6dipTTkrZUQLRxR,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMvQr8dVnk3f3DPvwCYX3ctTRtrTurD\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005259925,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994259925,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN5h2fjwyFRS8WJboYBSLtpRrymJLro,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMfCD8hK8inyEKDBZpuuBUjLdiKgwnG\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005259925,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994259925,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaNB49KvHVo3NPXYqjQBPRc7evKgVvnV,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjXxVzqB4T7zFoykRwfSZSD5ptAn4A\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005259025,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994259025,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN1jtT4pqHmr6wPYSyYnkAa842hKeqM,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMj7QaB8mYBBvkhaT3jCrXEMCEcRfb1\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005257075,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994257075,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN6umjKPhiefmXbbqMXB4BZByCpgmG1,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMobzkpUc1zYcT67wheRPLg7cmas5A6\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 27,\n" +
                "                    \"totalOut\": 11000000,\n" +
                "                    \"totalIn\": 1000005218675,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994218675,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN6nU1fpF9Uc8NUAqRSdi36jMkL3Rxk,KongQiBi\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm\",\n" +
                "                    \"alias\": \"json\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 5,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899899600000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899899600000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMvimye5NhHDYXrsNRM9TqHidMqXkNy\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 10000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 10000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMkqeHbTxwKqyquFcbewVTUDHPkF11o\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 43,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 4300000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 4300000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMk85772MgK5woFxCvkSPGqMSousBH1\",\n" +
                "                    \"alias\": \"gqmsousbh1\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 3,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1899900000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMnzjzdng5dsDzYNKrSvMArqAngUQtV\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 4,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1300000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1300000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMvJt9tJe2PGu2z5VKC5JxPUV8PAoJH\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1100000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1100000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjkMcLysX5umEwXFJozyKtKqrV9Txw\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMgBtNes7FbLwWh6AE6hHAyfqvUwz5F\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMsFW9CXobATEzqJQ66JLPZ6HiFeZEu\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMh3F59GqvPsCfhpAQE9GDo6VomosLs\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMu2dZ2RohVuGGdq7nSadM6CNfcVRpW\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMsXdFWw2qW4pUSoJaqEfXyLFBFL96T\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjqfofAzWwEmtwsZZFafuKriJPde74\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMqEqhxXxoiWYvDigVTrsgyypmrToGD\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMfPAn62sHGnKaWSzmsMG6a42ZrvNcx\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjAUdqqt5T4kV2VKyxzi7dAiHrwyK9\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMqauK818CPv59VbFZcGFPKzN9TDuWS\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMuESqHz2xM7NajDfDS4YTRV5P5GChE\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMtxT7P4UzwSVsNHVxRG8N7Mqasna2z\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMnnssWxNirXYQQumocERg5bYm82VgW\",\n" +
                "                    \"alias\": \"g5bym82vgw\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMvNoDDDtLVMmKAuHvqvxqX4JBRagyi\",\n" +
                "                    \"alias\": \"qx4jbragyi\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMiBCyH9RcfKiVTFJPYV3jcqw4fWLA8\",\n" +
                "                    \"alias\": \"jcqw4fwla8\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMfcqArw5963h2JBqB5jxBx4EgYGMZJ\",\n" +
                "                    \"alias\": \"bx4egygmzj\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMv2tKnwovvJKFEcY2Xv9SgcxsHxTQH\",\n" +
                "                    \"alias\": \"sgcxshxtqh\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjNJvs7AjBHpPBYpmLDYy5H7TKnMDM\",\n" +
                "                    \"alias\": \"y5h7tknmdm\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMioHwVHZdSqXFSprNfaWd49dT4A7Qb\",\n" +
                "                    \"alias\": \"d49dt4a7qb\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMhTsPdtspoHc6puvPTtb2axMT22FkX\",\n" +
                "                    \"alias\": \"2axmt22fkx\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMmWi4KWh3HBmaSuUnCqkpLKJRBEnhX\",\n" +
                "                    \"alias\": \"plkjrbenhx\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMoiTQm5N6c8X2DCJ6rVCS1cd3P4Si1\",\n" +
                "                    \"alias\": \"s1cd3p4si1\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrDkzVSFhf4YRKxcEg4zeaoqsuszVS\",\n" +
                "                    \"alias\": \"lanjinsheng\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMtCw6dRFia6cXeMQu9TD1Pe82z7snA\",\n" +
                "                    \"alias\": \"1pe82z7sna\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 4,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899800000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899800000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMfhTHVznRAvHTBsGRPnYSxZv5w7LUW\",\n" +
                "                    \"alias\": \"sxzv5w7luw\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 4,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899800000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899800000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMobwSnmSzF1EjByjwgoLp1ddByVamL\",\n" +
                "                    \"alias\": \"p1ddbyvaml\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 4,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899800000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899800000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               }\n" +
                "          ]", AccountTemp.class);
    }

}
