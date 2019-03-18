/**
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
package io.nuls.contract.tx.base;


import com.alibaba.fastjson.JSONObject;
import io.nuls.base.data.Transaction;
import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.txdata.CallContractData;
import io.nuls.contract.model.txdata.ContractData;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author: PierreLuo
 * @date: 2018/12/4
 */
public class Base {
    static String[] contractAddressSeeds = {
            "NseKMBJ8Z7WgqfDkF5bGKNCUE6G7FMa8",
            "NseM7aGB6QUDo2RrKh9ogbrBC6c27aH6",
            "NseFuqwPHwrZsyBKnhfKQMzSrYsVhjfL",
            "NseBeLaoi4gFEffqsGzB1zVNU6jsjjhK",
            "NseMnV77Tii2nY1Qomuoji5bojgTJbrU",
            "NseBRYKU3vQvZY4XCiSVY5jyL6oVabFr",
            "NseQLXBFZaZotpNhtBQqDLJTtdsoqURd",
            "NsePwaJfYBuVvHxo7YmAfAhcjstoPCXY",
            "NseALLow7yVQyVboH9NreKdccWPGNriq",
            "NseDWcNqN4zpzvLH59o8RjQDReRi5NEH",
            "NseHMCgNK5rCKa3bLVGbVXyd3WnziNxX",
            "NseN7DjnscKKgKH8ckHhAzjHfVapgsz2",
            "NsePjWf23b2UrZnTQwDaSJtqo9AoLC7G",
            "NseL4MPxPCvyTFMo8Qj6bvBJ2NTCJbzN",
            "NseFEH2LC1c9sMzCAHXiuwJtExULZXPt",
            "NseL6yz3co4RzRwaK96P4eL2c7UwuMBE",
            "NseFt39WQKWmUFPQ5bQnGzp6Fzi3z8WD",
            "NseDx4t5u8aR3PoY3YqrxFPJ9bD9rW6F",
            "NseGzHCstPjHEiWGoUJMTse12ZEgPtv7",
            "NseEwuKf1rAouBngPaUFiY7xK6Rz9VQL"};

    static String[] senderSeeds = {
            "5MR_2CWWTDXc32s9Wd1guNQzPztFgkyVEsz",
            "5MR_2CbdqKcZktcxntG14VuQDy8YHhc6ZqW",
            "5MR_2Cj9tfgQpdeF7nDy5wyaGG6MZ35H3rA",
            "5MR_2CWKhFuoGVraaxL5FYY3RsQLjLDN7jw",
            "5MR_2CgwCFRoJ8KX37xNqjjR7ttYuJsg8rk",
            "5MR_2CjZkQsN7EnEPcaLgNrMrp6wpPGN6xo",
            "5MR_2CeG11nRqx7nGNeh8hTXADibqfSYeNu",
            "5MR_2CVCFWH7o8AmrTBPLkdg2dYH1UCUJiM",
            "5MR_2CfUsasd33vQV3HqGw6M3JwVsuVxJ7r",
            "5MR_2CVuGjQ3CYVkhFszxfSt6sodg1gDHYF"};

    static String[] methodNameSeeds = {
            "transfer",
            "approve",
            "increaseApproval",
            "decreaseApproval"
    };

    static String[][] methodArgsSeeds = {
            {"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG", "10000"},
            {"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG", "10000"},
            {"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG", "10000"},
            {"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG", "10000"}
    };

    protected List<Transaction> list = new ArrayList<>();

    protected void initData() {
        initBean();
        //initTx(30);
        initTxFromJson();
    }

    void initTxFromJson() {
        String json = "[{\"blockHeight\":1111,\"hash\":\"c31a0d2b-e32b-4e45-9dbc-721aaeee8f0c\",\"remark\":\"test multy thread contract\",\"time\":1550806416956,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseBeLaoi4gFEffqsGzB1zVNU6jsjjhK\",\"gasLimit\":10000000,\"methodName\":\"approve\",\"price\":25,\"sender\":\"NsdyTeuM3mH5BnAdCLHk3zBGMGUQCqmb\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"ac5b53d5-6ede-4f2d-b13b-fdb17d3f1e5d\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseQLXBFZaZotpNhtBQqDLJTtdsoqURd\",\"gasLimit\":10000000,\"methodName\":\"increaseApproval\",\"price\":25,\"sender\":\"NsdyWwhMkWUvJLw8WCEoTRDGp1dc33t9\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"2b8b6af5-13e2-4f56-9798-1bb41e5d7bbd\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseBeLaoi4gFEffqsGzB1zVNU6jsjjhK\",\"gasLimit\":10000000,\"methodName\":\"increaseApproval\",\"price\":25,\"sender\":\"Nse9fRpc8buRX1cJkd1ySe3ZB41qZUnH\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"052fc2c9-5834-4d79-b2d4-0a57afd450d0\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NsePjWf23b2UrZnTQwDaSJtqo9AoLC7G\",\"gasLimit\":10000000,\"methodName\":\"transfer\",\"price\":25,\"sender\":\"Nse4JnTguRYsFpwFuDZAsPJuUjXJ5E9n\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"b06ec161-012d-406b-be8a-89ca42832fd0\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseEwuKf1rAouBngPaUFiY7xK6Rz9VQL\",\"gasLimit\":10000000,\"methodName\":\"approve\",\"price\":25,\"sender\":\"Nse6Pmz3TnEyBmfBDE3mcfd5Z4DyppMG\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"553cb907-c213-4686-ada4-16a89bb801f4\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NsePjWf23b2UrZnTQwDaSJtqo9AoLC7G\",\"gasLimit\":10000000,\"methodName\":\"decreaseApproval\",\"price\":25,\"sender\":\"Nse9fRpc8buRX1cJkd1ySe3ZB41qZUnH\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"0f17ebbb-e49f-4903-80a2-ae06c2a0405e\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseHMCgNK5rCKa3bLVGbVXyd3WnziNxX\",\"gasLimit\":10000000,\"methodName\":\"approve\",\"price\":25,\"sender\":\"Nse4JnTguRYsFpwFuDZAsPJuUjXJ5E9n\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"875b2d98-c9e0-4496-9b05-f250473b8798\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseHMCgNK5rCKa3bLVGbVXyd3WnziNxX\",\"gasLimit\":10000000,\"methodName\":\"increaseApproval\",\"price\":25,\"sender\":\"Nse4JnTguRYsFpwFuDZAsPJuUjXJ5E9n\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"7398c558-c24b-463b-9cce-a8b1ee64a4e2\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseL4MPxPCvyTFMo8Qj6bvBJ2NTCJbzN\",\"gasLimit\":10000000,\"methodName\":\"approve\",\"price\":25,\"sender\":\"Nse6h7646A5bSbgXkbANN6oV57j1C6ae\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"318d8ec8-707b-46ff-a2ed-dbd4042899b1\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseM7aGB6QUDo2RrKh9ogbrBC6c27aH6\",\"gasLimit\":10000000,\"methodName\":\"transfer\",\"price\":25,\"sender\":\"NsdyTeuM3mH5BnAdCLHk3zBGMGUQCqmb\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"c5872515-37fc-46ea-b737-7e7289444243\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseFuqwPHwrZsyBKnhfKQMzSrYsVhjfL\",\"gasLimit\":10000000,\"methodName\":\"decreaseApproval\",\"price\":25,\"sender\":\"Nse9fRpc8buRX1cJkd1ySe3ZB41qZUnH\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"52165368-3da2-45f2-8648-ab59a689bd8b\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseMnV77Tii2nY1Qomuoji5bojgTJbrU\",\"gasLimit\":10000000,\"methodName\":\"transfer\",\"price\":25,\"sender\":\"NsdvBoGQ6Jstzm228KmzwzvLcqPh2ts9\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"99562241-4c1e-403b-88a8-178aafccddce\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NsePjWf23b2UrZnTQwDaSJtqo9AoLC7G\",\"gasLimit\":10000000,\"methodName\":\"approve\",\"price\":25,\"sender\":\"NsdyWwhMkWUvJLw8WCEoTRDGp1dc33t9\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"ee120481-8c2d-4223-be81-f870383f76a8\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseDx4t5u8aR3PoY3YqrxFPJ9bD9rW6F\",\"gasLimit\":10000000,\"methodName\":\"decreaseApproval\",\"price\":25,\"sender\":\"NsdvBoGQ6Jstzm228KmzwzvLcqPh2ts9\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"d9fa4d12-6bfe-4e9f-b8c2-036f64778876\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseDx4t5u8aR3PoY3YqrxFPJ9bD9rW6F\",\"gasLimit\":10000000,\"methodName\":\"increaseApproval\",\"price\":25,\"sender\":\"Nsdy51dGPybGbjGnvfSCk1ZdFQs2wPeK\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"900a8153-f50c-43eb-8f23-71c670660b6c\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseFuqwPHwrZsyBKnhfKQMzSrYsVhjfL\",\"gasLimit\":10000000,\"methodName\":\"transfer\",\"price\":25,\"sender\":\"NsdtmE55nzonFEyur7Pe9DDNjJqdcnLt\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"20c07b89-270b-49a1-9074-91e5e8e9b7ea\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseDx4t5u8aR3PoY3YqrxFPJ9bD9rW6F\",\"gasLimit\":10000000,\"methodName\":\"transfer\",\"price\":25,\"sender\":\"Nse4pEvMHgj2aotePjn2qKR8kiEe8juW\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"416916ce-e504-4919-9a79-39e0e69f72dd\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseFEH2LC1c9sMzCAHXiuwJtExULZXPt\",\"gasLimit\":10000000,\"methodName\":\"approve\",\"price\":25,\"sender\":\"NsdvhN4gE4UHd5GRaHcyoQPaeKW7ckU3\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"6c5b813f-0f6d-4a26-aef1-6ed304d02b3b\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseFuqwPHwrZsyBKnhfKQMzSrYsVhjfL\",\"gasLimit\":10000000,\"methodName\":\"transfer\",\"price\":25,\"sender\":\"Nsdy51dGPybGbjGnvfSCk1ZdFQs2wPeK\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"c259aa3b-07a3-4316-b256-e23f83148fc3\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseGzHCstPjHEiWGoUJMTse12ZEgPtv7\",\"gasLimit\":10000000,\"methodName\":\"decreaseApproval\",\"price\":25,\"sender\":\"Nse4NwMo3hwxxtpTkzaB9VGVfhvzPX1x\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"0be0114e-d20e-4e80-8a28-882fcf9f049a\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseKMBJ8Z7WgqfDkF5bGKNCUE6G7FMa8\",\"gasLimit\":10000000,\"methodName\":\"decreaseApproval\",\"price\":25,\"sender\":\"NsdvBoGQ6Jstzm228KmzwzvLcqPh2ts9\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"08c8a1df-dfbd-413a-abf1-3065e223b67e\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseDWcNqN4zpzvLH59o8RjQDReRi5NEH\",\"gasLimit\":10000000,\"methodName\":\"approve\",\"price\":25,\"sender\":\"NsdtmE55nzonFEyur7Pe9DDNjJqdcnLt\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"a8921283-2e2e-4072-8203-adb8383d6975\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseFEH2LC1c9sMzCAHXiuwJtExULZXPt\",\"gasLimit\":10000000,\"methodName\":\"approve\",\"price\":25,\"sender\":\"Nse6h7646A5bSbgXkbANN6oV57j1C6ae\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"df803540-c9df-4b43-946d-71e64016d65f\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NsePwaJfYBuVvHxo7YmAfAhcjstoPCXY\",\"gasLimit\":10000000,\"methodName\":\"increaseApproval\",\"price\":25,\"sender\":\"Nse1JZzHAY65rAwTFMxsHwum4kDdaxt8\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"21c6eccd-09f2-4b50-b2f1-5bf901eaafdb\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseL4MPxPCvyTFMo8Qj6bvBJ2NTCJbzN\",\"gasLimit\":10000000,\"methodName\":\"decreaseApproval\",\"price\":25,\"sender\":\"NsdwNp1piSRARnJoV9qLWZtqNULkUXSz\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"13deb4c1-179a-4998-a9e6-1652da0c4d48\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseM7aGB6QUDo2RrKh9ogbrBC6c27aH6\",\"gasLimit\":10000000,\"methodName\":\"decreaseApproval\",\"price\":25,\"sender\":\"Nse4JUbdj6vrbRGWCzBEoj2WYJvXazS7\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"6d8e35ab-4f42-4264-98fc-d4c50f6c575d\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseBRYKU3vQvZY4XCiSVY5jyL6oVabFr\",\"gasLimit\":10000000,\"methodName\":\"approve\",\"price\":25,\"sender\":\"Nse4JnTguRYsFpwFuDZAsPJuUjXJ5E9n\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"a1e8c62d-dfdd-46f6-9775-25d7e177dfcd\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseKMBJ8Z7WgqfDkF5bGKNCUE6G7FMa8\",\"gasLimit\":10000000,\"methodName\":\"decreaseApproval\",\"price\":25,\"sender\":\"Nsdzh7uPRoMXM4ErrVuQY1AryPyVfWj4\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"9a0a32fd-e5c4-4342-b063-9503a6a2d03e\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseHMCgNK5rCKa3bLVGbVXyd3WnziNxX\",\"gasLimit\":10000000,\"methodName\":\"approve\",\"price\":25,\"sender\":\"Nsdy51dGPybGbjGnvfSCk1ZdFQs2wPeK\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"a322d6ef-f098-4b04-8a94-acc85e96b225\",\"remark\":\"test multy thread contract\",\"time\":1550806416958,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseBRYKU3vQvZY4XCiSVY5jyL6oVabFr\",\"gasLimit\":10000000,\"methodName\":\"approve\",\"price\":25,\"sender\":\"Nse3hf99UwduTKM1qjpGNRxj8bV4XJoi\",\"value\":0},\"type\":101}]";
        list = JSONObject.parseArray(json, Transaction.class);
    }

    void initTx(int txCount) {
        Random random = new Random();
        for (int i = 0; i < txCount; i++) {
            int methodIndex = random.nextInt(4);
            list.add(makeTx(senderSeeds[random.nextInt(20)],
                    contractAddressSeeds[random.nextInt(20)],
                    0L,
                    methodNameSeeds[methodIndex],
                    methodArgsSeeds[methodIndex]));
        }

        String jsonString = JSONObject.toJSONString(list);
        System.out.println(jsonString);
        //List<Transaction> list1 = JSONObject.parseArray(jsonString, Transaction.class);
        //System.out.println(JSONObject.toJSONString(list1));
    }

    //public static void main(String[] args) {
    //    Base base = new Base();
    //    base.initTx(3);
    //
    //    String aaa = "[{\"blockHeight\":1111,\"hash\":\"fbc89d40-1736-466b-bae4-4611577fb3c9\",\"remark\":\"test multy thread contract\",\"time\":1550804367732,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseEwuKf1rAouBngPaUFiY7xK6Rz9VQL\",\"gasLimit\":10000000,\"methodName\":\"approve\",\"price\":25,\"sender\":\"Nse6Pmz3TnEyBmfBDE3mcfd5Z4DyppMG\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"54bcba01-e796-466b-a3d5-757a686d917b\",\"remark\":\"test multy thread contract\",\"time\":1550804367734,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseMnV77Tii2nY1Qomuoji5bojgTJbrU\",\"gasLimit\":10000000,\"methodName\":\"transfer\",\"price\":25,\"sender\":\"Nse5RVMvgr7iUGzTmZSdmoMidoP7ztvC\",\"value\":0},\"type\":101},{\"blockHeight\":1111,\"hash\":\"384e0a25-d420-42f7-a8e8-e877bcd8dc93\",\"remark\":\"test multy thread contract\",\"time\":1550804367734,\"txData\":{\"args\":[[\"Nse3Uaj7Lesh6VNBVJ62bZRRZRpZ4DAG\"],[\"10000\"]],\"argsCount\":2,\"contractAddress\":\"NseBeLaoi4gFEffqsGzB1zVNU6jsjjhK\",\"gasLimit\":10000000,\"methodName\":\"decreaseApproval\",\"price\":25,\"sender\":\"Nse9fRpc8buRX1cJkd1ySe3ZB41qZUnH\",\"value\":0},\"type\":101}]";
    //    System.out.println(aaa);
    //    List<Transaction> list1 = JSONObject.parseArray(aaa, Transaction.class);
    //    System.out.println(JSONObject.toJSONString(list1));
    //}


    void initBean() {

    }

    private Transaction makeTx(String sender, String contractAddress, long value, String methodName, Object[] args) {
        return newInstance(sender, contractAddress, value, methodName, args);
    }

    public static Transaction newInstance(String sender, String contractAddress, long value, String methodName, Object[] args) {
        //int type = 101;
        //long time = System.currentTimeMillis();
        //CallContractData txData = new CallContractData();
        //txData.setSender(sender);
        //txData.setContractAddress(contractAddress);
        //txData.setValue(value);
        //txData.setGasLimit(10000000L);
        //txData.setPrice(25L);
        //txData.setMethodName(methodName);
        //String[][] args2 = ContractUtil.twoDimensionalArray(args);
        //txData.setArgsCount((byte) args.length);
        //txData.setArgs(args2);
        //String remark = "test multy thread contract";
        CallContractTransaction tx = new CallContractTransaction();
        return tx;
    }

    private ContractData makeCallContractData(String sender, String contractAddress) {
        CallContractData txData = new CallContractData();
        //txData.setSender(sender);
        //txData.setGasLimit(10000000L);
        //txData.setPrice(25L);
        //txData.setValue(0L);
        //txData.setMethodName("single");
        //txData.setMethodDesc(EMPTY);
        //txData.setArgsCount((byte) 0);
        //txData.setArgs(null);
        //txData.setContractAddress(contractAddress);
        return txData;
    }

}
