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
package io.nuls.contract.helper;

import io.nuls.base.data.Transaction;
import io.nuls.contract.model.bo.ContractResult;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

import static io.nuls.contract.util.ContractUtil.collectAddress;


/**
 * @author: PierreLuo
 * @date: 2019/1/11
 */
@Getter
@Setter
public class ContractConflictChecker {

    public final ReentrantLock lock = new ReentrantLock();

    public static ContractConflictChecker newInstance() {
        return new ContractConflictChecker();
    }

    private Set<String>[] contractSetArray;

    public boolean checkConflict(Transaction tx, ContractResult contractResult, Set<String> commitSet) {
        lock.lock();
        try {
            boolean isConflict = false;
            Set<String> collectAddress = collectAddress(contractResult);
            for (String address : collectAddress) {
                if (containAddress(address, commitSet)) {
                    isConflict = true;
                    break;
                }
            }
            if (!isConflict) {
                if (contractResult.isSuccess()) {
                    commitSet.addAll(collectAddress);
                }
            }

            return isConflict;
        } finally {
            lock.unlock();
        }

    }

    private boolean containAddress(String address, Set<String> commitSet) {
        for (Set<String> set : contractSetArray) {
            // 排除掉自己线程执行的智能合约，因为自己线程执行的合约是排队顺序执行，不会冲突
            if (set == commitSet) {
                continue;
            }
            if (set.contains(address)) {
                return true;
            }
        }
        return false;
    }

}
