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
package io.nuls.poc.rpc.cmd;

import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.log.Log;
import io.nuls.core.rpc.cmd.BaseCmd;
import io.nuls.core.rpc.model.*;
import io.nuls.core.rpc.model.message.Response;
import io.nuls.poc.model.dto.RandomSeedDTO;
import io.nuls.poc.storage.RandomSeedsStorageService;
import io.nuls.poc.utils.RandomSeedCaculator;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author: PierreLuo
 * @date: 2019-07-17
 */
@Component
public class RandomCmd extends BaseCmd {
    @Autowired
    private RandomSeedsStorageService randomSeedService;

    @CmdAnnotation(cmd = "cs_random_seed_count", version = 1.0, description = "根据高度和原始种子个数生成一个随机种子并返回")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "最大高度"),
        @Parameter(parameterName = "count", requestType = @TypeDescriptor(value = int.class), parameterDes = "原始种子个数"),
        @Parameter(parameterName = "algorithm", parameterDes = "算法标识：SHA3...")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = RandomSeedDTO.class))
    public Response getRandomSeedByCount(Map<String,Object> params){
        try{
            Integer chainId = (Integer) params.get("chainId");
            Long height = Long.parseLong(params.get("height").toString());
            Integer count = (Integer) params.get("count");
            String algorithm = (String) params.get("algorithm");
            List<byte[]> list = randomSeedService.getSeeds(chainId, height, count);
            if (list.size() != count) {
                return failed("not enough raw random seed");
            }
            byte[] seed = RandomSeedCaculator.clac(list, algorithm);
            if (null == seed) {
                return failed("empty random seed");
            }
            RandomSeedDTO dto = new RandomSeedDTO();
            dto.setCount(count);
            dto.setAlgorithm(algorithm);
            BigInteger value = new BigInteger(seed);
            dto.setSeed(value.toString());
            return success(dto);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "cs_random_seed_height", version = 1.0, description = "根据高度区间生成一个随机种子并返回")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "startHeight", requestType = @TypeDescriptor(value = long.class), parameterDes = "起始高度"),
        @Parameter(parameterName = "endHeight", requestType = @TypeDescriptor(value = long.class), parameterDes = "截止高度"),
        @Parameter(parameterName = "algorithm", parameterDes = "算法标识：SHA3...")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = RandomSeedDTO.class))
    public Response getRandomSeedByHeight(Map<String,Object> params){
        try{
            Integer chainId = (Integer) params.get("chainId");
            Long startHeight = Long.parseLong(params.get("startHeight").toString());
            Long endHeight = Long.parseLong(params.get("endHeight").toString());
            String algorithm = (String) params.get("algorithm");
            List<byte[]> list = randomSeedService.getSeeds(chainId, startHeight, endHeight);
            int count = list.size();
            if (list.isEmpty()) {
                return failed("empty random seed");
            }
            byte[] seed = RandomSeedCaculator.clac(list, algorithm);
            if (null == seed) {
                return failed("empty random seed");
            }
            RandomSeedDTO dto = new RandomSeedDTO();
            dto.setCount(count);
            dto.setAlgorithm(algorithm);
            BigInteger value = new BigInteger(seed);
            dto.setSeed(value.toString());
            return success(dto);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }
    @CmdAnnotation(cmd = "cs_random_raw_seeds_count", version = 1.0, description = "根据高度查找原始种子列表并返回")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "height", requestType = @TypeDescriptor(value = long.class), parameterDes = "起始高度"),
        @Parameter(parameterName = "count", requestType = @TypeDescriptor(value = int.class), parameterDes = "截止高度")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = List.class, collectionElement = String.class))
    public Response getRandomRawSeedsByCount(Map<String,Object> params){
        try{
            Integer chainId = (Integer) params.get("chainId");
            Long height = Long.parseLong(params.get("height").toString());
            Integer count = (Integer) params.get("count");
            List<byte[]> list = randomSeedService.getSeeds(chainId, height, count);
            if (list.size() != count) {
                return failed("not enough raw random seed");
            }
            List<String> seeds = new ArrayList<>();
            for (byte[] value : list) {
                seeds.add(new BigInteger(value).toString());
            }
            return success(seeds);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }

    @CmdAnnotation(cmd = "cs_random_raw_seeds_height", version = 1.0, description = "根据高度区间查询原始种子列表并返回")
    @Parameters(value = {
        @Parameter(parameterName = "chainId", requestType = @TypeDescriptor(value = int.class), parameterDes = "链id"),
        @Parameter(parameterName = "startHeight", requestType = @TypeDescriptor(value = long.class), parameterDes = "起始高度"),
        @Parameter(parameterName = "endHeight", requestType = @TypeDescriptor(value = long.class), parameterDes = "截止高度")
    })
    @ResponseData(name = "返回值", responseType = @TypeDescriptor(value = List.class, collectionElement = String.class))
    public Response getRandomRawSeedsByHeight(Map<String,Object> params){
        try{
            Integer chainId = (Integer) params.get("chainId");
            Long startHeight = Long.parseLong(params.get("startHeight").toString());
            Long endHeight = Long.parseLong(params.get("endHeight").toString());
            List<byte[]> list = randomSeedService.getSeeds(chainId, startHeight, endHeight);
            if (list.isEmpty()) {
                return failed("empty random seed");
            }
            List<String> seeds = new ArrayList<>();
            for (byte[] value : list) {
                seeds.add(new BigInteger(value).toString());
            }
            return success(seeds);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }
}
