/*
 * MIT License
 * Copyright (c) 2017-2018 nuls.io
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package io.nuls.block.constant;

/**
 * 模块运行状态枚举
 * The module runs state enumeration.
 * @author captain
 * @date 18-11-28 下午5:58
 * @version 1.0
 */
public enum RunningStatusEnum {

    /**
     * 正在初始化
     * initializing
     */
    INITIALIZING,

    /**
     * 初始化完成
     * initialized
     */
    INITIALIZED,

    /**
     * 等待中
     * waiting
     */
    WAITING,

    /**
     * 同步区块中
     * synchronizing
     */
    SYNCHRONIZING,

    /**
     * 切换主链中
     * switching
     */
    SWITCHING,

    /**
     * 运行中
     * running
     */
    RUNNING,

    /**
     * 正在停止
     * stopping
     */
    STOPPING,

    /**
     * 运行出现异常
     * Running exception
     */
    EXCEPTION,

    /**
     * 维护孤儿链
     * Clean up the database
     */
    MAINTAIN_CHAINS,

    /**
     * 清理数据库中
     * Clean up the database
     */
    DATABASE_CLEANING,
    ;

    @Override
    public String toString() {
        return name();
    }
}
