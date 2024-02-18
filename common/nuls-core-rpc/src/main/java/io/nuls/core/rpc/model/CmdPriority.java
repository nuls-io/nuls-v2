package io.nuls.core.rpc.model;
/**
 * Message Priority
 * Message Priority
 *
 * @author tag
 */
public enum CmdPriority {
    /**
     * High priority
     * */
    HIGH(10),
    /**
     * Default Priority
     * */
    DEFAULT(5),
    /**
     * Low priority
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
