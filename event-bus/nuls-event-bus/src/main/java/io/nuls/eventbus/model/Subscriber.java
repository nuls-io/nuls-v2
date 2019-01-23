package io.nuls.eventbus.model;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
 * It represents subscriber in Event Bus
 * Any module/role in Nuls Blockchain can be a subscriber
 * Each subscriber is uniquely identified based on module/role code
 * @author naveen
 */
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Subscriber implements Serializable {

    private static final long serialVersionUID = -4204832123982044991L;

    @Getter
    @Setter
    private String moduleAbbr;

    @Getter
    @Setter
    private String moduleName;

    @Getter
    @Setter
    private String domain;

    @Getter
    @Setter
    private long subscribeTime;

    @Getter
    @Setter
    private String callBackCmd;

    /**
     * @param abbr module/role code
     * @param callBackCmd event is sent to this command, actual subscriber needs to implement to call this command on every event
     */
    public Subscriber(String abbr,String callBackCmd) {
        this.moduleAbbr = abbr;
        this.callBackCmd = callBackCmd;
    }

    /**
     * @param abbr
     * @param moduleName
     * @param domain
     */
    public Subscriber(String abbr,String moduleName,String domain) {
        this.moduleAbbr = abbr;
        this.moduleName = moduleName;
        this.domain = domain;
    }

    /**
     * @param abbr
     * @param moduleName
     * @param domain
     * @param callBackCmd
     */
    public Subscriber(String abbr,String moduleName,String domain,String callBackCmd){
        this.callBackCmd = callBackCmd;
        this.moduleAbbr = abbr;
        this.moduleName = moduleName;
        this.domain = domain;
    }

    /**
     *
     * @param o
     * @return
     */
    @Override
    public boolean equals(Object o) {
        if (this == o){ return true;}
        if (o == null || this.getClass() != o.getClass()) {return false;}

        Subscriber that = (Subscriber) o;

        return this.moduleAbbr.equals(that.moduleAbbr);
    }

    /**
     *
     * @return
     */
    @Override
    public int hashCode() {
        return Objects.hash(this.moduleAbbr);
    }
}
