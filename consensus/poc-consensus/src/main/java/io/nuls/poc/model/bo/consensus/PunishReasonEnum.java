/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.poc.model.bo.consensus;
import io.nuls.tools.parse.I18nUtils;

/**
 * 红牌惩罚原因枚举类
 * Enumeration of Reasons for Red Card Punishment
 *
 * @author tag
 * 2018/11/28
 */
public enum PunishReasonEnum {
    /*
    连续分叉
    Bifurcate block chain
    */
    BIFURCATION((byte) 1, "69980"),

    /*
    双花
    double spend
    */
    DOUBLE_SPEND((byte) 2, "69981"),

    /*
    连续x轮黄牌
    Continuous x round yellow card.
    */
    TOO_MUCH_YELLOW_PUNISH((byte) 3, "69982"),;
    private final byte code;
    private final String msgCode;

    private PunishReasonEnum(byte code, String msgCode) {
        this.code = code;
        this.msgCode = msgCode;
    }

    public String getMessage() {
        return I18nUtils.get(this.msgCode);
    }

    public byte getCode() {
        return code;
    }

    public static PunishReasonEnum getEnum(int code) {
        switch (code) {
            case 1:
                return PunishReasonEnum.BIFURCATION;
            case 2:
                return PunishReasonEnum.DOUBLE_SPEND;
            case 3:
                return PunishReasonEnum.TOO_MUCH_YELLOW_PUNISH;
            default:
                return PunishReasonEnum.TOO_MUCH_YELLOW_PUNISH;
        }

    }
}
