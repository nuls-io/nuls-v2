/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.contract.storage.impl;


import io.nuls.contract.constant.ContractDBConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.model.po.ContractAddressInfoPo;
import io.nuls.contract.storage.ContractAddressStorageService;
import io.nuls.contract.util.ContractDBUtil;
import io.nuls.contract.util.ContractUtil;
import io.nuls.db.model.Entry;
import io.nuls.db.service.RocksDBService;
import io.nuls.tools.basic.Result;
import io.nuls.tools.core.annotation.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @desription:
 * @author: PierreLuo
 * @date: 2018/5/24
 */
@Component
public class ContractAddressStorageServiceImpl implements ContractAddressStorageService {

    @Override
    public Result saveContractAddress(int chainId, byte[] contractAddressBytes, ContractAddressInfoPo info) {
        if (contractAddressBytes == null || info == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        boolean result = ContractDBUtil.putModel(ContractDBConstant.DB_NAME_CONTRACT_ADDRESS + chainId, contractAddressBytes, info);
        if (result) {
            return ContractUtil.getSuccess();
        } else {
            return ContractUtil.getFailed();
        }
    }

    @Override
    public Result<ContractAddressInfoPo> getContractAddressInfo(int chainId, byte[] contractAddressBytes) {
        if (contractAddressBytes == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        ContractAddressInfoPo infoPo = ContractDBUtil.getModel(ContractDBConstant.DB_NAME_CONTRACT_ADDRESS + chainId, contractAddressBytes, ContractAddressInfoPo.class);
        if(infoPo != null) {
            infoPo.setContractAddress(contractAddressBytes);
        }
        return ContractUtil.getSuccess().setData(infoPo);
    }

    @Override
    public Result deleteContractAddress(int chainId, byte[] contractAddressBytes) throws Exception {
        if (contractAddressBytes == null) {
            return Result.getFailed(ContractErrorCode.NULL_PARAMETER);
        }
        boolean result = RocksDBService.delete(ContractDBConstant.DB_NAME_CONTRACT_ADDRESS + chainId, contractAddressBytes);
        if (result) {
            return ContractUtil.getSuccess();
        } else {
            return ContractUtil.getFailed();
        }
    }

    @Override
    public boolean isExistContractAddress(int chainId, byte[] contractAddressBytes) {
        if (contractAddressBytes == null) {
            return false;
        }
        byte[] contract = RocksDBService.get(ContractDBConstant.DB_NAME_CONTRACT_ADDRESS + chainId, contractAddressBytes);
        if(contract == null) {
            return false;
        }
        return true;
    }

    @Override
    public Result<List<ContractAddressInfoPo>> getContractInfoList(int chainId, byte[] creater) {
        List<Entry<byte[], byte[]>> list = RocksDBService.entryList(ContractDBConstant.DB_NAME_CONTRACT_ADDRESS + chainId);
        if(list == null || list.size() ==0) {
            return Result.getFailed(ContractErrorCode.DATA_NOT_FOUND);
        }
        List<ContractAddressInfoPo> resultList = new ArrayList<>();
        ContractAddressInfoPo po;
        for(Entry<byte[], byte[]> entry : list) {
            po = ContractDBUtil.getModel(entry.getValue(), ContractAddressInfoPo.class);
            if(Arrays.equals(creater, po.getSender())) {
                po.setContractAddress(entry.getKey());
                resultList.add(po);
            }
        }
        Result<List<ContractAddressInfoPo>> result = ContractUtil.getSuccess();
        result.setData(resultList);
        return result;
    }

    @Override
    public Result<List<ContractAddressInfoPo>> getAllContractInfoList(int chainId) {
        List<Entry<byte[], byte[]>> list = RocksDBService.entryList(ContractDBConstant.DB_NAME_CONTRACT_ADDRESS + chainId);
        if(list == null || list.size() ==0) {
            return Result.getFailed(ContractErrorCode.DATA_NOT_FOUND);
        }
        List<ContractAddressInfoPo> resultList = new ArrayList<>();
        ContractAddressInfoPo po;
        for(Entry<byte[], byte[]> entry : list) {
            po = ContractDBUtil.getModel(entry.getValue(), ContractAddressInfoPo.class);
            po.setContractAddress(entry.getKey());
            resultList.add(po);
        }
        Result<List<ContractAddressInfoPo>> result = ContractUtil.getSuccess();
        result.setData(resultList);
        return result;
    }

    @Override
    public Result<List<ContractAddressInfoPo>> getAllNrc20ContractInfoList(int chainId) {
        Result<List<ContractAddressInfoPo>> allContractInfoListResult = getAllContractInfoList(chainId);
        if(allContractInfoListResult.isFailed()) {
            return allContractInfoListResult;
        }
        List<ContractAddressInfoPo> resultList = new ArrayList<>();
        List<ContractAddressInfoPo> contractAddressInfoPoList = allContractInfoListResult.getData();
        for(ContractAddressInfoPo po : contractAddressInfoPoList) {
            if(po.isNrc20()) {
                resultList.add(po);
            }
        }
        return ContractUtil.getSuccess().setData(resultList);
    }
}
