package io.nuls.eventbus.model;

import lombok.*;

import java.io.Serializable;
import java.util.Objects;

/**
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

    public Subscriber(String abbr,String callBackCmd) {
        this.moduleAbbr = abbr;
        this.callBackCmd = callBackCmd;
    }

    public Subscriber(String abbr,String moduleName,String domain) {
        this.moduleAbbr = abbr;
        this.moduleName = moduleName;
        this.domain = domain;
    }
    public Subscriber(String abbr,String moduleName,String domain,String callBackCmd){
        this.callBackCmd = callBackCmd;
        this.moduleAbbr = abbr;
        this.moduleName = moduleName;
        this.domain = domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){ return true;}
        if (o == null || this.getClass() != o.getClass()) {return false;}

        Subscriber that = (Subscriber) o;

        return this.moduleAbbr.equals(that.moduleAbbr);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.moduleAbbr);
    }
}
