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

package io.nuls.account.model.bo;

/**
 * Validate transaction result combination encapsulation Return results
 * @author: Charlie
 * @date: 2019-01-11
 */
public class VerifyTxResult {

    /** 1Verification passed,2Orphan Trading 3honeysuckle 4 Other abnormalities 5Repeated transactions(Confirmed)*/
    private int code;
    /** Verification return description*/
    private String desc;

    public static final int SUCCESS = 1;
    public static final int ORPHAN = 2;
    public static final int DOUBLE_SPENDING = 3;
    public static final int OTHER_EXCEPTION = 4;
    public static final int CONFIRMED = 5;

    public VerifyTxResult(int code, String desc) {
        this.code = code;
        this.desc = desc;
    }
    public VerifyTxResult(int code) {
        this.code = code;
    }

    public boolean success(){
        return this.code == SUCCESS;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getDesc() {
      /*  if(StringUtils.isBlank(this.desc)){
            switch (this.code){
                case SUCCESS:
                    return "Verification passed";
                case ORPHAN:
                    return "Orphan Trading";
                case DOUBLE_SPENDING:
                    return "Double Flower Trading";
                case OTHER_EXCEPTION:
                    return "Other verification failures";
            }
        }*/
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }
}
