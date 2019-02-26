package io.nuls.base.data.protocol;

import java.util.List;

public class MessageConfig {
    private String name;
    private String refer;
    private List<ListItem> handlers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRefer() {
        return refer;
    }

    public void setRefer(String refer) {
        this.refer = refer;
    }

    public List<ListItem> getHandlers() {
        return handlers;
    }

    public void setHandlers(List<ListItem> handlers) {
        this.handlers = handlers;
    }

    public MessageConfig() {
    }
}
