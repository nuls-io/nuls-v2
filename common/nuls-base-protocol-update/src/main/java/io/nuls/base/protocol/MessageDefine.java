package io.nuls.base.protocol;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        MessageDefine that = (MessageDefine) o;

        if (name != null ? !name.equals(that.name) : that.name != null) {
            return false;
        }
        if (handlers != null ? !handlers.equals(that.handlers) : that.handlers != null) {
            return false;
        }
        return protocolCmd != null ? protocolCmd.equals(that.protocolCmd) : that.protocolCmd == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (handlers != null ? handlers.hashCode() : 0);
        result = 31 * result + (protocolCmd != null ? protocolCmd.hashCode() : 0);
        return result;
    }
}
