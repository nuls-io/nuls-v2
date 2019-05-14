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

        StringBuilder builder = new StringBuilder(" [\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrV62RjpZKx7GseRwhEg982Cqpptuf\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 100902521974223,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 100902521974223,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMhErrYpe3YiJGfHVc3ziW8iVUC2GkF\",\n" +
                "                    \"alias\": \"7234cn\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 13450,\n" +
                "                    \"totalOut\": 23000100400000,\n" +
                "                    \"totalIn\": 115006921060044,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 92006820660044,\n" +
                "                    \"totalReward\": 3007921060044,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMsLENwW4s1WsofHGYmziu5amSxu7eG\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 76834271422425,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 76834271422425,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMoNnKitV28JeuUdBaPSR6n1xHfKLj2\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 3,\n" +
                "                    \"totalOut\": 1000000000000000,\n" +
                "                    \"totalIn\": 1060215812478223,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 60215812478223,\n" +
                "                    \"totalReward\": 1000000000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMsw6TMHimLdSe7y16acW54H5tyto5i\",\n" +
                "                    \"alias\": \"luo\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 11492,\n" +
                "                    \"totalOut\": 146000100500000,\n" +
                "                    \"totalIn\": 204019818823089,\n" +
                "                    \"consensusLock\": 22000000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 16368475020,\n" +
                "                    \"totalBalance\": 58019718323089,\n" +
                "                    \"totalReward\": 2019618823089,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMuLbKskfSyCeZuqNo5SeLwF9nZFnGw\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 54231859120119,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 54231859120119,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrtczgLEDwvxw4DRCtBVhqtoy2PLUC\",\n" +
                "                    \"alias\": \"pen\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1031,\n" +
                "                    \"totalOut\": 100300000,\n" +
                "                    \"totalIn\": 50171194274534,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 50171093974534,\n" +
                "                    \"totalReward\": 172294674534,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMhp9ZYgXJhCT7iLaxx5JS7Xene6h8A\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 30238018472084,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 30238018472084,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMkCHbYcyMfacjdR4TZEJqasWhEQKZS\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1039,\n" +
                "                    \"totalOut\": 200000,\n" +
                "                    \"totalIn\": 30212068453466,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 30212068253466,\n" +
                "                    \"totalReward\": 212068453466,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMqA829tXvyeqyCEPxPRfUzxVsdW6qV\",\n" +
                "                    \"alias\": \"mick_one\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 14452,\n" +
                "                    \"totalOut\": 3200600900000,\n" +
                "                    \"totalIn\": 32096159618192,\n" +
                "                    \"consensusLock\": 22000000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 6787842689011,\n" +
                "                    \"totalBalance\": 28895558518192,\n" +
                "                    \"totalReward\": 2508081177906,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMmoVEkJg3XYidMP2f7ZtreUyEdeStv\",\n" +
                "                    \"alias\": \"linkworld\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 14427,\n" +
                "                    \"totalOut\": 100300000,\n" +
                "                    \"totalIn\": 27733957380229,\n" +
                "                    \"consensusLock\": 24000800000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 1068944574151,\n" +
                "                    \"totalBalance\": 27733857080229,\n" +
                "                    \"totalReward\": 2733957380229,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMtEe7wFW1BHmN7gsbGVu5BsKQ4ST5P\",\n" +
                "                    \"alias\": \"wuyan01\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 8259,\n" +
                "                    \"totalOut\": 100300000,\n" +
                "                    \"totalIn\": 27516887074148,\n" +
                "                    \"consensusLock\": 2000000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 23797259800000,\n" +
                "                    \"totalBalance\": 27516786774148,\n" +
                "                    \"totalReward\": 1719527074148,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjiyLX3aPdq9oanVhfFb9hp114YEgX\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 14336,\n" +
                "                    \"totalOut\": 2100002100000,\n" +
                "                    \"totalIn\": 28369243623948,\n" +
                "                    \"consensusLock\": 26200000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 54874403684,\n" +
                "                    \"totalBalance\": 26269241523948,\n" +
                "                    \"totalReward\": 581720252418,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMhWkbJUbTwLw2HeMzhcMXP2D51Tuzx\",\n" +
                "                    \"alias\": \"nuls\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 13391,\n" +
                "                    \"totalOut\": 100300000,\n" +
                "                    \"totalIn\": 25458967352398,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 25458867052398,\n" +
                "                    \"totalReward\": 2458967352398,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMkzTffKDZLy9v5SWbooFFhefwM4dff\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 24999999800000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 24999999800000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMgS1a9yNF2mVAY8qs4R7riTyd2J1Kp\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 24999899700000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 24999899700000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMo9vSC1925J7Xa3QRXuipm7v26nre2\",\n" +
                "                    \"alias\": \"bightbc\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 5870,\n" +
                "                    \"totalOut\": 100300000,\n" +
                "                    \"totalIn\": 24006249940078,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 24006149640078,\n" +
                "                    \"totalReward\": 1006249940078,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMu3nwXeMYmKhuNgPVtoeGryjBmUJ2H\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 23840839734288,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 23840839734288,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMv3w8aEmduJhUnqVv7jWcFQMetPkF8\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 23302688106433,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 23302688106433,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMhEVNrVyEH1fC94Vi7bZ1K3hTPirAm\",\n" +
                "                    \"alias\": \"siwei\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 10828,\n" +
                "                    \"totalOut\": 4001878460000,\n" +
                "                    \"totalIn\": 27281409683880,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 23250828954906,\n" +
                "                    \"totalBalance\": 23279531223880,\n" +
                "                    \"totalReward\": 1658338340748,\n" +
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
                "                    \"totalReward\": 0,\n" +
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
                "                    \"totalReward\": 0,\n" +
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
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMr8898iVf7qaF2GmZBnhwgKFU38doJ\",\n" +
                "                    \"alias\": \"nulsfans\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 5,\n" +
                "                    \"totalOut\": 100400000,\n" +
                "                    \"totalIn\": 23000000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 22999899600000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMiBpwM8p91Vi8JtjbhLfzt6q3Mvk5N\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 8961,\n" +
                "                    \"totalOut\": 700000,\n" +
                "                    \"totalIn\": 22226257638754,\n" +
                "                    \"consensusLock\": 22152800000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 72397339472,\n" +
                "                    \"totalBalance\": 22226256938754,\n" +
                "                    \"totalReward\": 1228257638754,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMibSVuLgtDXfki42TPxALYgmuHM7sE\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1042,\n" +
                "                    \"totalOut\": 300000,\n" +
                "                    \"totalIn\": 22182366467522,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 22182366167522,\n" +
                "                    \"totalReward\": 182366467522,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMhHgddLhR2EJecMbct42MSFyjwPaPe\",\n" +
                "                    \"alias\": \"mick_three\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 11486,\n" +
                "                    \"totalOut\": 1898600400000,\n" +
                "                    \"totalIn\": 24007540754961,\n" +
                "                    \"consensusLock\": 22000000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 1897840868990,\n" +
                "                    \"totalBalance\": 22108940354961,\n" +
                "                    \"totalReward\": 2007040754961,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMr86kzopdD2N7GvVuUNNBD6zWPxzfR\",\n" +
                "                    \"alias\": \"mick_two\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 14176,\n" +
                "                    \"totalOut\": 2494400600000,\n" +
                "                    \"totalIn\": 24602949914216,\n" +
                "                    \"consensusLock\": 22000000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 2492873544324,\n" +
                "                    \"totalBalance\": 22108549314216,\n" +
                "                    \"totalReward\": 2601949914216,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjMGBiEWBjMX5j9PcSe8qtmhRPs8gp\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 22000099700000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 22000099700000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMsiAVo7zTvL42RPVMVDxKtAU1Pmj9T\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 14691,\n" +
                "                    \"totalOut\": 2002502800000,\n" +
                "                    \"totalIn\": 23816890826765,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 20118426636817,\n" +
                "                    \"totalBalance\": 21814388026765,\n" +
                "                    \"totalReward\": 1816080826765,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrbMRiFAUeeAt6swb4xVBNyi81YL24\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 3,\n" +
                "                    \"totalOut\": 1000000000000000,\n" +
                "                    \"totalIn\": 1020000093025675,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 20000093025675,\n" +
                "                    \"totalReward\": 1000000000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMnrs6JKrCy6TQdzYJZkMZJDng7QAsD\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 5,\n" +
                "                    \"totalOut\": 1004100000200000,\n" +
                "                    \"totalIn\": 1022965488736925,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 18865488536925,\n" +
                "                    \"totalReward\": 1000000000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMnt9hCjUynizuMo1YbnR1RjHp6CRBJ\",\n" +
                "                    \"alias\": \"kevin\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 853,\n" +
                "                    \"totalOut\": 187501100900000,\n" +
                "                    \"totalIn\": 200314477315407,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 32721019633342,\n" +
                "                    \"totalBalance\": 12813376415407,\n" +
                "                    \"totalReward\": 314477315407,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrZNEmsTVXDBey7Edvod6YNSJzRbfS\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 10050508590822,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 10050508590822,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMfEKKL6p9UuRpZ8FxgsmJqPSPKaW67\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 9999899800000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 9999899800000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMmSjQL6Hv98eCdT1STWVVecqLGP8eD\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 5000000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 5000000000000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMn29hLvHc8ZsRZ25JfdSCq99VZkWFw\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 3894587428843,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 3894587428843,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMnG8hGcyeygVeyaL5cXV38bQm1rw9M\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 15,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 2500031209850,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 2500031209850,\n" +
                "                    \"totalReward\": 1600000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMoF7p3QHGn8mGrkABeCSkQKp8Ei7r9\",\n" +
                "                    \"alias\": \"siwei2\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 9761,\n" +
                "                    \"totalOut\": 100200000,\n" +
                "                    \"totalIn\": 2472098890588,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 2471998690588,\n" +
                "                    \"totalReward\": 471210010588,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMhX8EbrY85fXkLv59e1y6ae39FGJjo\",\n" +
                "                    \"alias\": \"mai01c\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 4650,\n" +
                "                    \"totalOut\": 100200000,\n" +
                "                    \"totalIn\": 2427923137030,\n" +
                "                    \"consensusLock\": 2175000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 169972841346,\n" +
                "                    \"totalBalance\": 2427822937030,\n" +
                "                    \"totalReward\": 252523137030,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMpj4zHqRi21rr4tFzRVY2UfeDCmZJX\",\n" +
                "                    \"alias\": \"niels1\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 6099,\n" +
                "                    \"totalOut\": 100200000,\n" +
                "                    \"totalIn\": 2418645046903,\n" +
                "                    \"consensusLock\": 2000000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 5439211241,\n" +
                "                    \"totalBalance\": 2418544846903,\n" +
                "                    \"totalReward\": 418445046903,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMigwBrvikwVwbhAgAxip8cTScwcaT8\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 2399909900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 2399909900000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMqCFqskBKYBZz9YpVDdYSgk75c5TVN\",\n" +
                "                    \"alias\": \"fj_ly_lc_20190422\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 11848,\n" +
                "                    \"totalOut\": 100200000,\n" +
                "                    \"totalIn\": 2377859599248,\n" +
                "                    \"consensusLock\": 2000000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 151391619112,\n" +
                "                    \"totalBalance\": 2377759399248,\n" +
                "                    \"totalReward\": 376859599248,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjWuGgCGG5v7XriHBPvw2gdCFUrsiV\",\n" +
                "                    \"alias\": \"ned_0\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 14538,\n" +
                "                    \"totalOut\": 3321800900000,\n" +
                "                    \"totalIn\": 5592581394550,\n" +
                "                    \"consensusLock\": 2000000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 207830206935,\n" +
                "                    \"totalBalance\": 2270780494550,\n" +
                "                    \"totalReward\": 2554460731301,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMiFC9vf6qHKdRMceAJZps4QYCrbdPt\",\n" +
                "                    \"alias\": \"zhoulijun\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1127,\n" +
                "                    \"totalOut\": 100200000,\n" +
                "                    \"totalIn\": 2162658349387,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 2162558149387,\n" +
                "                    \"totalReward\": 62658349387,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMkKxoboxwejrCJyy16zLWwMqWkyqEu\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 2102721813072,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 2102721813072,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMtavxnG9LJHVJWBivo6johVXpgf2c8\",\n" +
                "                    \"alias\": \"siwei3\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 340,\n" +
                "                    \"totalOut\": 100200000,\n" +
                "                    \"totalIn\": 2010233129402,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 2010132929402,\n" +
                "                    \"totalReward\": 9344249402,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMr4pNM47akScnFUJgQTmNBhYqw6shk\",\n" +
                "                    \"alias\": \"ned_1\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 18,\n" +
                "                    \"totalOut\": 100200000,\n" +
                "                    \"totalIn\": 2001021452227,\n" +
                "                    \"consensusLock\": 2000000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 921252227,\n" +
                "                    \"totalBalance\": 2000921252227,\n" +
                "                    \"totalReward\": 21452227,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMmTNYqywL5ZSHbyAQ662uE3wibrgD1\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 3,\n" +
                "                    \"totalOut\": 1000000000000000,\n" +
                "                    \"totalIn\": 1000999999900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999999900000,\n" +
                "                    \"totalReward\": 1000000000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMj8XfWDjyKHZ1ybC3ShR8qKGyVKRcb\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999996434975,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999996434975,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjcximfy1JEGzjxodNMjrjydWuiffr\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999996034425,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999996034425,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMgTcqskhNrE1ZSt3kZpdAv6B83npXE\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999996028400,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999996028400,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMfMk3RGzotV3Dw788NFTP52ep7SMnJ\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999995992175,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999995992175,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMvaRhahBAYkZKQFhiSqcC67UiRzoSA\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994665175,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994665175,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMvGmZSrFyQHptSL9yBCNSDfhWoxEHF\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994665175,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994665175,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMoixxbUovqmzPyJ2AwYFAX2evKbuy9\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994665175,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994665175,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMfXDQeT4MJZim1RusCJRPx5j9bMKQN\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994662325,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994662325,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMqwycXLTWtjexSHHfa4jDTrVq9FMWE\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994662325,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994662325,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMk52mfhacRWkmB98PrwCVXuEzCdQuk\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994659475,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994659475,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMiKWTid5Gj3FoqBFP7WomUzgumVeKc\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994659475,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994659475,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMuk5jx12ZXhaf5HLgcAr3WCwUhRGfT\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994657625,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994657625,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMmbiCH5soCFasXnG4TwqknyTzYBM3S\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994622925,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994622925,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMkzsRE6qc9RVoeY6gHq8k1xSMcdrc7\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994620075,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994620075,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMsUBLVxwoaswjWvghJyoUJfbfB6dja\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994616175,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994616175,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMv8q3pWzS7bHpQWW8yypNGo8auRoPf\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994580675,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994580675,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMtkzQ1tH8JWBGZDCmRHCmySevE4frM\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994580675,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994580675,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMhKaLzhQh1AhhecUqh15ZKw98peg29\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994578825,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994578825,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMqjT3y9bGz4gBeJ7FJujmxBDTGdNp1\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994262775,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994262775,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMpaiBiMHWfAeTzdXhnfJXPfwXwKikc\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994262775,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994262775,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMi5yGkDbDgKGGX8TGxYdDttZ4KhpMv\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994262775,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994262775,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrL5netZkTo9FZb86xGSk47kq6TRBR\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994262450,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994262450,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMqjttJV62GZ1iXVFDBudet3ey2aYSB\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994260925,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994260925,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMh4VafNqp5TJSmV5ogdZviq1nbXBSu\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994259925,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994259925,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMtgmrSYu98QwP1Mv8G5FwaMDkWSkuy\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994259925,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994259925,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMvQr8dVnk3f3DPvwCYX3ctTRtrTurD\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994259925,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994259925,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMfCD8hK8inyEKDBZpuuBUjLdiKgwnG\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994259925,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994259925,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjXxVzqB4T7zFoykRwfSZSD5ptAn4A\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994259025,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994259025,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMj7QaB8mYBBvkhaT3jCrXEMCEcRfb1\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994257075,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994257075,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMobzkpUc1zYcT67wheRPLg7cmas5A6\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 999994218675,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999994218675,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMoodYW7AqyJrgYdWiJ6nfwfVHHHyXm\",\n" +
                "                    \"alias\": \"tony\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 6,\n" +
                "                    \"totalOut\": 1000000101300000,\n" +
                "                    \"totalIn\": 1000899899733100,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899798433100,\n" +
                "                    \"totalReward\": 1000000000133100,\n" +
                "                    \"tokens\": [\n" +
                "                         \"tNULSeBaN2dGRWTYNhw8kgCt5mufyG7qP4GwHk,BTC\"\n" +
                "                    ],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjctbk5cdBWj9qTbedrmWFTQGh6eTc\",\n" +
                "                    \"alias\": \"fj_ly_lc_20190416\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2936,\n" +
                "                    \"totalOut\": 2001110400000,\n" +
                "                    \"totalIn\": 2028993717443,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 27883317443,\n" +
                "                    \"totalReward\": 26691717443,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMkrt4z9FYEkkR9D6choPVvQr94oYZp\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 20,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 10953300000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 10953300000,\n" +
                "                    \"totalReward\": 10953300000,\n" +
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
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMkqeHbTxwKqyquFcbewVTUDHPkF11o\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 34,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 7600000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 7600000000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMgDEcAUhPSdF3D3C6mT54HPUt81cQ4\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 2000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 2000000000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMqywZjfSrKNQKBfuQtVxAHBQ8rB2Zn\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 110,\n" +
                "                    \"totalOut\": 10076979339438184,\n" +
                "                    \"totalIn\": 10076981239438184,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1900000000,\n" +
                "                    \"totalReward\": 1000000000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMk85772MgK5woFxCvkSPGqMSousBH1\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1899900000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMnzjzdng5dsDzYNKrSvMArqAngUQtV\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1300000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1300000000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMvJt9tJe2PGu2z5VKC5JxPUV8PAoJH\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 1100000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 1100000000,\n" +
                "                    \"totalReward\": 0,\n" +
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
                "                    \"totalReward\": 0,\n" +
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
                "                    \"totalReward\": 0,\n" +
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
                "                    \"totalReward\": 0,\n" +
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
                "                    \"totalReward\": 0,\n" +
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
                "                    \"totalReward\": 0,\n" +
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
                "                    \"totalReward\": 0,\n" +
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
                "                    \"totalReward\": 0,\n" +
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
                "                    \"totalReward\": 0,\n" +
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
                "                    \"totalReward\": 0,\n" +
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
                "                    \"totalReward\": 0,\n" +
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
                "                    \"totalReward\": 0,\n" +
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
                "                    \"totalReward\": 0,\n" +
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
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMsR529Ddi15gzhVNvbhTpDtB8RC7AR\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 65999000100000,\n" +
                "                    \"totalIn\": 66000000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 999900000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMvNoDDDtLVMmKAuHvqvxqX4JBRagyi\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMnnssWxNirXYQQumocERg5bYm82VgW\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMfcqArw5963h2JBqB5jxBx4EgYGMZJ\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrDkzVSFhf4YRKxcEg4zeaoqsuszVS\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMiBCyH9RcfKiVTFJPYV3jcqw4fWLA8\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMoiTQm5N6c8X2DCJ6rVCS1cd3P4Si1\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMjNJvs7AjBHpPBYpmLDYy5H7TKnMDM\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMmWi4KWh3HBmaSuUnCqkpLKJRBEnhX\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMhTsPdtspoHc6puvPTtb2axMT22FkX\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMv2tKnwovvJKFEcY2Xv9SgcxsHxTQH\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMioHwVHZdSqXFSprNfaWd49dT4A7Qb\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899900000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899900000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMobwSnmSzF1EjByjwgoLp1ddByVamL\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899800000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899800000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMfhTHVznRAvHTBsGRPnYSxZv5w7LUW\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899800000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899800000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMtCw6dRFia6cXeMQu9TD1Pe82z7snA\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 899800000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 899800000,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrXEpdgTSH8e3Qr3T7CVS3udhXNKsV\",\n" +
                "                    \"alias\": \"xiaomai01\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1628,\n" +
                "                    \"totalOut\": 2048100300000,\n" +
                "                    \"totalIn\": 2048837058652,\n" +
                "                    \"consensusLock\": 2000000000000,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 48736858652,\n" +
                "                    \"totalBalance\": 736758652,\n" +
                "                    \"totalReward\": 46837058652,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrucKmBUxXVSeyQhdCwGHQHSus7yWg\",\n" +
                "                    \"alias\": \"mai01\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 148,\n" +
                "                    \"totalOut\": 2002100300000,\n" +
                "                    \"totalIn\": 2002493794275,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 393494275,\n" +
                "                    \"totalReward\": 1593894275,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMsL7zscrSKZEe1ERXiSozmJ9p78kpa\",\n" +
                "                    \"alias\": \"mai001\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 658,\n" +
                "                    \"totalOut\": 2175500300000,\n" +
                "                    \"totalIn\": 2175545703293,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 2175445503293,\n" +
                "                    \"totalBalance\": 45403293,\n" +
                "                    \"totalReward\": 26400364031,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMfVT3QamH4pu885UHW22YTktTGUY1g\",\n" +
                "                    \"alias\": \"maimai\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 1967,\n" +
                "                    \"totalOut\": 2149245639262,\n" +
                "                    \"totalIn\": 2149255539262,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 2149155339262,\n" +
                "                    \"totalBalance\": 9900000,\n" +
                "                    \"totalReward\": 80055539262,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMoGr2RkLZPfJeS5dFzZeNj1oXmaYNe\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 26,\n" +
                "                    \"totalOut\": 0,\n" +
                "                    \"totalIn\": 2700000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 2700000,\n" +
                "                    \"totalReward\": 2700000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMoCL42ihvwRcm4D1mfvityBhjV8fk6\",\n" +
                "                    \"alias\": \"wuyan\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 4190,\n" +
                "                    \"totalOut\": 25797460500000,\n" +
                "                    \"totalIn\": 25797463080127,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 2580127,\n" +
                "                    \"totalBalance\": 2580127,\n" +
                "                    \"totalReward\": 797463080127,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrAdGEPqUHC7WEvRDnkpX7WcnwZLqu\",\n" +
                "                    \"alias\": \"xiaomai\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 629,\n" +
                "                    \"totalOut\": 23067300600000,\n" +
                "                    \"totalIn\": 23067301378358,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 200878358,\n" +
                "                    \"totalBalance\": 778358,\n" +
                "                    \"totalReward\": 20601778358,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMu38g1vnJsSZUCwTDU9GsE5TVNUtpD\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 1000000000000000,\n" +
                "                    \"totalIn\": 1000000000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 0,\n" +
                "                    \"totalReward\": 1000000000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMp9wC9PcWEcfesY7YmWrPfeQzkN1xL\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 1000000000000000,\n" +
                "                    \"totalIn\": 1000000000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 0,\n" +
                "                    \"totalReward\": 1000000000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMvEtDfvZuukDf2mVyfGo3DdiN8KLRG\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 1000000000000000,\n" +
                "                    \"totalIn\": 1000000000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 0,\n" +
                "                    \"totalReward\": 1000000000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMshNPEnuqiDhMdSA4iNs6LMgjY6tcL\",\n" +
                "                    \"alias\": null,\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 2,\n" +
                "                    \"totalOut\": 1000000000000000,\n" +
                "                    \"totalIn\": 1000000000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 0,\n" +
                "                    \"totalBalance\": 0,\n" +
                "                    \"totalReward\": 1000000000000000,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               },\n" +
                "               {\n" +
                "                    \"address\": \"tNULSeBaMrA8YjubRdjGTAVFcmuynCdw4K2UsY\",\n" +
                "                    \"alias\": \"mick_four\",\n" +
                "                    \"type\": 1,\n" +
                "                    \"txCount\": 4,\n" +
                "                    \"totalOut\": 1200000000000,\n" +
                "                    \"totalIn\": 1200000000000,\n" +
                "                    \"consensusLock\": 0,\n" +
                "                    \"timeLock\": 0,\n" +
                "                    \"balance\": 1199899900000,\n" +
                "                    \"totalBalance\": 0,\n" +
                "                    \"totalReward\": 0,\n" +
                "                    \"tokens\": [],\n" +
                "                    \"news\": false\n" +
                "               }\n" +
                "          ]");

      return JSONUtils.json2list(builder.toString(), AccountTemp.class);
    }

}
