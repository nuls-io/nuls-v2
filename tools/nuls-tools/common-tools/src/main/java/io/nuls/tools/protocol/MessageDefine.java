package io.nuls.tools.protocol;

import java.util.List;

public class MessageDefine {
    private String name;
    private String handlers;
    private List<MessageProcessor> processors;

    public List<MessageProcessor> getProcessors() {
        return processors;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setProcessors(List<MessageProcessor> processors) {
        this.processors = processors;
    }

    class MessageProcessor {
        private String handler;
        private String method;

        public String getHandler() {
            return handler;
        }

        public void setHandler(String handler) {
            this.handler = handler;
        }

        public String getMethod() {
            return method;
        }

        public void setMethod(String method) {
            this.method = method;
        }
    }

    public MessageDefine() {
    }

    public String getHandlers() {
        return handlers;
    }

    public void setHandlers(String handlers) {
        this.handlers = handlers;
    }
}
