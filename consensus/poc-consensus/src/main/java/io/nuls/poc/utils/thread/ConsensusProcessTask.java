/*
 * *
 *  * MIT License
 *  *
 *  * Copyright (c) 2017-2018 nuls.io
 *  *
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  *
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  *
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */
package io.nuls.poc.utils.thread;

import io.nuls.poc.model.bo.Chain;
import io.nuls.poc.utils.thread.process.ConsensusProcess;

/**
 * 共识任务
 * Consensus task
 *
 * @author tag
 * 2018/11/15
 */
public class ConsensusProcessTask implements Runnable {

    private ConsensusProcess consensusProcess;
    private Chain chain;

    public ConsensusProcessTask(Chain chain,ConsensusProcess consensusProcess) {
        this.chain = chain;
        this.consensusProcess = consensusProcess;
    }

    @Override
    public void run() {
        try {
            consensusProcess.process(chain);
        } catch (Exception e) {
            Log.error(e);
        }
    }
}
