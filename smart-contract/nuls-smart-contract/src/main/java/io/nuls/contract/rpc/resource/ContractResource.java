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
package io.nuls.contract.rpc.resource;

import io.nuls.base.basic.AddressTool;
import io.nuls.contract.constant.ContractConstant;
import io.nuls.contract.constant.ContractErrorCode;
import io.nuls.contract.helper.ContractHelper;
import io.nuls.contract.service.ContractTxService;
import io.nuls.contract.util.ContractUtil;
import io.nuls.contract.util.MapUtil;
import io.nuls.contract.vm.program.ProgramMethod;
import io.nuls.rpc.cmd.BaseCmd;
import io.nuls.rpc.model.CmdAnnotation;
import io.nuls.rpc.model.Parameter;
import io.nuls.rpc.model.message.Response;
import io.nuls.tools.core.annotation.Autowired;
import io.nuls.tools.core.annotation.Component;
import io.nuls.tools.log.Log;
import io.nuls.tools.model.StringUtils;
import org.spongycastle.util.encoders.Hex;

import java.util.Map;

import static io.nuls.contract.constant.ContractCmdConstant.CREATE;
import static io.nuls.contract.constant.ContractErrorCode.ADDRESS_ERROR;

/**
 * @author: PierreLuo
 * @date: 2019-03-11
 */
@Component
public class ContractResource extends BaseCmd {

    @Autowired
    private ContractHelper contractHelper;
    @Autowired
    private ContractTxService contractTxService;

    @CmdAnnotation(cmd = CREATE, version = 1.0, description = "invoke contract")
    @Parameter(parameterName = "chainId", parameterType = "int")
    @Parameter(parameterName = "sender", parameterType = "String")
    @Parameter(parameterName = "password", parameterType = "String")
    @Parameter(parameterName = "gasLimit", parameterType = "long")
    @Parameter(parameterName = "price", parameterType = "long")
    @Parameter(parameterName = "contractCode", parameterType = "String")
    @Parameter(parameterName = "args", parameterType = "Object[]")
    @Parameter(parameterName = "remark", parameterType = "String")
    public Response create(Map<String,Object> params){
        try {
            Integer chainId = (Integer) params.get("chainId");
            String sender = (String) params.get("sender");
            String password = (String) params.get("password");
            Long gasLimit = Long.parseLong(params.get("gasLimit").toString()) ;
            Long price = Long.parseLong(params.get("price").toString()) ;
            String contractCode = (String) params.get("contractCode");
            Object[] args = (Object[]) params.get("args");
            String remark = (String) params.get("remark");

            if (gasLimit < 0 || price < 0) {
                return failed(ContractErrorCode.PARAMETER_ERROR);
            }

            if (!AddressTool.validAddress(chainId, sender)) {
                return failed(ADDRESS_ERROR);
            }

            if(StringUtils.isBlank(contractCode)) {
                return failed(ContractErrorCode.NULL_PARAMETER);
            }

            byte[] contractCodeBytes = Hex.decode(contractCode);

            ProgramMethod method = contractHelper.getMethodInfoByCode(chainId, ContractConstant.CONTRACT_CONSTRUCTOR, null, contractCodeBytes);
            String[][] convertArgs = null;
            if(method != null) {
                convertArgs = ContractUtil.twoDimensionalArray(args, method.argsType2Array());
            }

            Map<String, Object> resultMap = MapUtil.createHashMap(2);

            return success(resultMap);
        } catch (Exception e) {
            Log.error(e);
            return failed(e.getMessage());
        }
    }
}
