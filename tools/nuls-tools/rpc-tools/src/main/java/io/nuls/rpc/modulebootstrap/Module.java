package io.nuls.rpc.modulebootstrap;

import lombok.Data;

/**
 * @Author: zhoulijun
 * @Time: 2019-02-27 17:50
 * @Description: Module 描述对象
 */
@Data
public class Module {

    /**
     * module name
     */
    String name;

    /**
     * modulel version
     */
    String version;

    public Module(){};

    public Module(String name,String version){
        this.name = name;
        this.version = version;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Module)) return false;

        Module module = (Module) o;

        if (name != null ? !name.equals(module.name) : module.name != null) return false;
        return version != null ? version.equals(module.version) : module.version == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }
}
