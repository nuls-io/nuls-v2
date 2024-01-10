/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
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
package io.nuls.provider.model.dto;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.core.rpc.model.TypeDescriptor;

import java.util.List;

@ApiModel(name = "Contract Method Details")
public class ProgramMethod {

    @ApiModelProperty(description = "Method Name")
    private String name;
    @ApiModelProperty(description = "Method description")
    private String desc;
    @ApiModelProperty(description = "Method parameter list", type = @TypeDescriptor(value = List.class, collectionElement = ProgramMethodArg.class))
    private List<ProgramMethodArg> args;
    @ApiModelProperty(description = "return type")
    private String returnArg;
    @ApiModelProperty(description = "View Method（Call this method and the data will not be linked）")
    private boolean view;
    @ApiModelProperty(description = "Is it an event")
    private boolean event;
    @ApiModelProperty(description = "Is it an acceptable method for transferring main chain assets")
    private boolean payable;
    @ApiModelProperty(description = "Is it an acceptable method for transferring other assets")
    private boolean payableMultyAsset;
    @ApiModelProperty(description = "Is the method return valueJSONserialize")
    private boolean jsonSerializable;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public List<ProgramMethodArg> getArgs() {
        return args;
    }

    public void setArgs(List<ProgramMethodArg> args) {
        this.args = args;
    }

    public String getReturnArg() {
        return returnArg;
    }

    public void setReturnArg(String returnArg) {
        this.returnArg = returnArg;
    }

    public boolean isView() {
        return view;
    }

    public void setView(boolean view) {
        this.view = view;
    }

    public boolean isEvent() {
        return event;
    }

    public void setEvent(boolean event) {
        this.event = event;
    }

    public boolean isPayable() {
        return payable;
    }

    public void setPayable(boolean payable) {
        this.payable = payable;
    }

    public boolean isPayableMultyAsset() {
        return payableMultyAsset;
    }

    public void setPayableMultyAsset(boolean payableMultyAsset) {
        this.payableMultyAsset = payableMultyAsset;
    }

    public boolean isJsonSerializable() {
        return jsonSerializable;
    }

    public void setJsonSerializable(boolean jsonSerializable) {
        this.jsonSerializable = jsonSerializable;
    }
}
