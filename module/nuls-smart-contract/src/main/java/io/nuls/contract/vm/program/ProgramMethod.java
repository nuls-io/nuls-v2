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
package io.nuls.contract.vm.program;

import io.nuls.core.rpc.model.ApiModel;
import io.nuls.core.rpc.model.ApiModelProperty;
import io.nuls.core.rpc.model.TypeDescriptor;

import java.util.List;

@ApiModel(name = "合约方法详情")
public class ProgramMethod {

    @ApiModelProperty(description = "方法名称")
    private String name;
    @ApiModelProperty(description = "方法描述")
    private String desc;
    @ApiModelProperty(description = "方法参数列表", type = @TypeDescriptor(value = List.class, collectionElement = ProgramMethodArg.class))
    private List<ProgramMethodArg> args;
    @ApiModelProperty(description = "返回值类型")
    private String returnArg;
    @ApiModelProperty(description = "是否视图方法（调用此方法数据不上链）")
    private boolean view;
    @ApiModelProperty(description = "是否是事件")
    private boolean event;
    @ApiModelProperty(description = "是否是可接受主链资产转账的方法")
    private boolean payable;

    public ProgramMethod() {
    }

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProgramMethod that = (ProgramMethod) o;

        if (view != that.view) {
            return false;
        }
        if (event != that.event) {
            return false;
        }
        if (payable != that.payable) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (desc != null ? !desc.equals(that.desc) : that.desc != null) {
            return false;
        }
        if (args != null ? !args.equals(that.args) : that.args != null) {
            return false;
        }
        return returnArg != null ? returnArg.equals(that.returnArg) : that.returnArg == null;
    }

    public boolean equalsNrc20Method(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ProgramMethod that = (ProgramMethod) o;

        if (view != that.view) {
            return false;
        }
        if (event != that.event) {
            return false;
        }
        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (args != null) {
            if (that.args == null) {
                return false;
            }
            if (!isEqualNrc20Args(args, that.args)) {
                return false;
            }
        } else {
            if (that.args != null) {
                return false;
            }
        }
        return returnArg != null ? returnArg.equals(that.returnArg) : that.returnArg == null;
    }

    public String[] argsType2Array() {
        if (args != null && args.size() > 0) {
            int size = args.size();
            String[] result = new String[size];
            for (int i = 0; i < size; i++) {
                result[i] = args.get(i).getType();
            }
            return result;
        } else {
            return null;
        }
    }

    private boolean isEqualNrc20Args(List<ProgramMethodArg> a, List<ProgramMethodArg> b) {
        if (a.size() != b.size()) {
            return false;
        } else {
            /*
            // 参数类型、名称完全相同
            Map<String, ProgramMethodArg> mapA = a.stream().collect(Collectors.toMap(ProgramMethodArg::getName, Function.identity(), (key1, key2) -> key2, LinkedHashMap::new));
            Map<String, ProgramMethodArg> mapB = b.stream().collect(Collectors.toMap(ProgramMethodArg::getName, Function.identity(), (key1, key2) -> key2, LinkedHashMap::new));
            Set<Map.Entry<String, ProgramMethodArg>> entriesA = mapA.entrySet();
            String methodName;
            ProgramMethodArg methodArg;
            for(Map.Entry<String, ProgramMethodArg> entryA : entriesA) {
                methodName = entryA.getKey();
                if(!mapB.containsKey(methodName)) {
                    return false;
                }
                methodArg = entryA.getValue();
                if(!methodArg.equalsNrc20(mapB.get(methodName))) {
                    return false;
                }
            }
            */

            // 参数类型相同
            int size = a.size();
            ProgramMethodArg argA, argB;
            for (int i = 0; i < size; i++) {
                argA = a.get(i);
                argB = b.get(i);
                if (!argA.equalsNrc20(argB)) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (desc != null ? desc.hashCode() : 0);
        result = 31 * result + (args != null ? args.hashCode() : 0);
        result = 31 * result + (returnArg != null ? returnArg.hashCode() : 0);
        result = 31 * result + (view ? 1 : 0);
        result = 31 * result + (event ? 1 : 0);
        result = 31 * result + (payable ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ProgramMethod{" +
                "name=" + name +
                ", desc=" + desc +
                ", args=" + args +
                ", returnArg=" + returnArg +
                ", view=" + view +
                ", event=" + event +
                ", payable=" + payable +
                '}';
    }

}
