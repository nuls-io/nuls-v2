package io.nuls.core.rpc.protocol;

public class MessageDefine {
    private String name;
    private String handlers;
    private String protocolCmd;

    public String getName() {
        return name;
    }

    public String getProtocolCmd() {
        return protocolCmd;
    }

    public void setProtocolCmd(String protocolCmd) {
        this.protocolCmd = protocolCmd;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "MessageDefine{" +
                "name='" + name + '\'' +
                ", handlers='" + handlers + '\'' +
                ", protocolCmd='" + protocolCmd + '\'' +
                '}';
    }

    public String getHandlers() {
        return handlers;
    }

    public void setHandlers(String handlers) {
        this.handlers = handlers;
    }
}
