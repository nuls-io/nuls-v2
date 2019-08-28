/*
 * MIT License
 * Copyright (c) 2017-2019 nuls.io
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

package io.nuls.api.model.po;

/**
 * @author Niels
 */
public class PocRound {

    private long index;

    private long startTime;

    private int memberCount;

    private long endTime;

    private int redCardCount;

    private int yellowCardCount;

    private int producedBlockCount;

    private long startHeight;

    private long endHeight;

    private double lostRate;

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }

    public int getMemberCount() {
        return memberCount;
    }

    public void setMemberCount(int memberCount) {
        this.memberCount = memberCount;
    }

    public long getEndTime() {
        return endTime;
    }

    public void setEndTime(long endTime) {
        this.endTime = endTime;
    }

    public int getRedCardCount() {
        return redCardCount;
    }

    public void setRedCardCount(int redCardCount) {
        this.redCardCount = redCardCount;
    }

    public int getYellowCardCount() {
        return yellowCardCount;
    }

    public void setYellowCardCount(int yellowCardCount) {
        this.yellowCardCount = yellowCardCount;
    }

    public int getProducedBlockCount() {
        return producedBlockCount;
    }

    public void setProducedBlockCount(int producedBlockCount) {
        this.producedBlockCount = producedBlockCount;
    }

    public long getStartHeight() {
        return startHeight;
    }

    public void setStartHeight(long startHeight) {
        this.startHeight = startHeight;
    }

    public long getEndHeight() {
        return endHeight;
    }

    public void setEndHeight(long endHeight) {
        this.endHeight = endHeight;
    }

    public double getLostRate() {
        return lostRate;
    }

    public void setLostRate(double lostRate) {
        this.lostRate = lostRate;
    }
}
