package io.nuls.tools.protocol;

import java.util.List;

public class MessageConfig {
    private String name;
    private List<ListItem> handlers;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
