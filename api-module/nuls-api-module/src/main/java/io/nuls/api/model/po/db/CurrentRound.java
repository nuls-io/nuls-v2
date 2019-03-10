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

package io.nuls.api.model.po.db;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.apache.tools.ant.util.DateUtils;

import java.util.Date;
import java.util.List;

/**
 * @author Niels
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrentRound extends PocRound {

    public CurrentRound() {
        this.setIndex(-1);
    }

    private int packerOrder;

    private BlockHeaderInfo startBlockHeader;

    private List<PocRoundItem> itemList;

    public int getPackerOrder() {
        return packerOrder;
    }

    public void setPackerOrder(int packerOrder) {
        this.packerOrder = packerOrder;
    }

    public List<PocRoundItem> getItemList() {
        return itemList;
    }

    public void setItemList(List<PocRoundItem> itemList) {
        this.itemList = itemList;
    }

    public PocRound toPocRound() {
        PocRound round = new PocRound();
        round.setEndHeight(this.getEndHeight());
        round.setEndTime(this.getEndTime());
        round.setIndex(this.getIndex());
        round.setMemberCount(this.getMemberCount());
        round.setProducedBlockCount(this.getProducedBlockCount());
        round.setRedCardCount(this.getRedCardCount());
        round.setStartHeight(this.getStartHeight());
        round.setStartTime(this.getStartTime());
        round.setYellowCardCount(this.getYellowCardCount());
        round.setLostRate(this.getLostRate());
        return round;
    }


    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("index:" + this.getIndex());
        stringBuilder.append(", startHeight:");
        stringBuilder.append(this.getStartHeight());
        stringBuilder.append(", startTime:" + DateUtils.format(new Date(this.getStartTime()), "YYYY-mm-DD HH:MM:SS"));
        stringBuilder.append("\n");
        int index = 1;
        if (null != this.getItemList()) {
            for (PocRoundItem item : this.getItemList()) {
                stringBuilder.append(index++);
                stringBuilder.append(" , ");
                stringBuilder.append(item.getSeedAddress() == null ? item.getPackingAddress() : item.getSeedAddress());
                stringBuilder.append("\n");
            }
        }
        return stringBuilder.toString();
    }

    public BlockHeaderInfo getStartBlockHeader() {
        return startBlockHeader;
    }

    public void setStartBlockHeader(BlockHeaderInfo startBlockHeader) {
        this.startBlockHeader = startBlockHeader;
    }

    public void initByPocRound(PocRound round) {
        this.setEndHeight(round.getEndHeight());
        this.setEndTime(round.getEndTime());
        this.setProducedBlockCount(round.getProducedBlockCount());
        this.setRedCardCount(round.getRedCardCount());
        this.setYellowCardCount(round.getYellowCardCount());
        this.setStartTime(round.getStartTime());
        this.setMemberCount(round.getMemberCount());
        this.setStartHeight(round.getStartHeight());
        this.setIndex(round.getIndex());
        this.setLostRate(round.getLostRate());
    }
}
