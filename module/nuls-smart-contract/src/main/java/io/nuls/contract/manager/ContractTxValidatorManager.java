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
package io.nuls.contract.manager;

import io.nuls.contract.model.tx.CallContractTransaction;
import io.nuls.contract.model.tx.CreateContractTransaction;
import io.nuls.contract.model.tx.DeleteContractTransaction;
import io.nuls.contract.validator.CallContractTxValidator;
import io.nuls.contract.validator.CreateContractTxValidator;
import io.nuls.contract.validator.DeleteContractTxValidator;
import io.nuls.core.basic.Result;
import io.nuls.core.core.annotation.Autowired;
import io.nuls.core.core.annotation.Component;
import io.nuls.core.exception.NulsException;

/**
 * @author: PierreLuo
 * @date: 2019-03-11
 */
@Component
public class ContractTxValidatorManager {

    @Autowired
    private CreateContractTxValidator createContractTxValidator;
    @Autowired
    private CallContractTxValidator callContractTxValidator;
    @Autowired
    private DeleteContractTxValidator deleteContractTxValidator;

    public Result createValidator(int chainId, CreateContractTransaction tx) throws NulsException {
        return createContractTxValidator.validate(chainId, tx);
    }

    public Result callValidator(int chainId, CallContractTransaction tx) throws NulsException {
        return callContractTxValidator.validate(chainId, tx);
    }

    public Result deleteValidator(int chainId, DeleteContractTransaction tx) throws NulsException {
        return deleteContractTxValidator.validate(chainId, tx);
    }
}
