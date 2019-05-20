package io.nuls.core.rpc.protocol;

import java.util.ArrayList;
import java.util.List;

public class MessageDefine {
    private String name;
    private String handlers;
    private String protocolCmd;
    private List<MessageProcessor> processors;

    public List<MessageProcessor> getProcessors() {
        if (processors == null) {
            processors = new ArrayList<>();
            String[] strings = handlers.split(",");
            for (String string : strings) {
                String[] split = string.split("#");
                MessageProcessor processor = new MessageProcessor();
                processor.setHandler(split[0]);
                processor.setMethod(split[1]);
                processors.add(processor);
            }
        }
        return processors;
    }

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

    public void setProcessors(List<MessageProcessor> processors) {
        this.processors = processors;
    }

    @Override
    public String toString() {
        return "MessageDefine{" +
                "name='" + name + '\'' +
                ", handlers='" + handlers + '\'' +
                ", protocolCmd='" + protocolCmd + '\'' +
                ", processors=" + processors +
                '}';
    }

    public String getHandlers() {
        return handlers;
    }

    public void setHandlers(String handlers) {
        this.handlers = handlers;
    }
}
