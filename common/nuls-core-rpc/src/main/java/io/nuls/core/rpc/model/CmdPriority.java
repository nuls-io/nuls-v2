package io.nuls.core.rpc.model;
/**
 * 消息优先级
 * Message Priority
 *
 * @author tag
 */
public enum CmdPriority {
    /**
     * 高优先级
     * */
    HIGH(10),
    /**
     * 默认优先级
     * */
    DEFAULT(5),
    /**
     * 低优先级
     * */
    LOWER(0);

    private int priority;
    CmdPriority(int priority){
        this.priority = priority;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }
}
