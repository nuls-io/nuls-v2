package io.nuls.core.rpc.modulebootstrap;


import io.nuls.core.rpc.model.ModuleE;

/**
 * @Author: zhoulijun
 * @Time: 2019-02-27 17:50
 * @Description: Module 描述对象
 */

public class Module {

    /**
     * module name
     */
    String name;

    /**
     * modulel version
     */
    String version;

    public Module() {
    }

    public Module(String name, String version) {
        this.name = name;
        this.version = version;
    }

    public static Module build(ModuleE moduleE){
        return new Module(moduleE.getName(),RpcModule.ROLE);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Module)) {
            return false;
        }

        Module module = (Module) o;

        if (name != null ? !name.equals(module.name) : module.name != null) {
            return false;
        }
        return version != null ? version.equals(module.version) : module.version == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (version != null ? version.hashCode() : 0);
        return result;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    @Override
    public String toString() {
        return new StringBuilder("{")
                .append("\"name\":\"")
                .append(name).append('\"')
                .append(",\"version\":\"")
                .append(version).append('\"')
                .append('}').toString();
    }
}
