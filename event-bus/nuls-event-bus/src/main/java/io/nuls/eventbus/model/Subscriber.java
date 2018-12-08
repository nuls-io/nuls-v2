package io.nuls.eventbus.model;

import io.nuls.rpc.model.ModuleE;
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
    private String url;

    public Subscriber(String url,String abbr,String moduleName,String domain){
        this.url = url;
        this.moduleAbbr = abbr;
        this.moduleName = moduleName;
        this.domain = domain;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o){ return true;}
        if (o == null || this.getClass() != o.getClass()) {return false;}

        Subscriber that = (Subscriber) o;

        return this.url.equals(that.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.url);
    }
}
