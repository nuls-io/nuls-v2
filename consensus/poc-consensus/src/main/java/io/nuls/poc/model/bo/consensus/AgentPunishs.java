package io.nuls.poc.model.bo.consensus;

import io.nuls.poc.model.po.PunishLogPo;

import java.util.List;

public class AgentPunishs {
    private String address;
    private List<PunishLogPo> yellowPunishList;
    private List<PunishLogPo> redPunishList;
    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public List<PunishLogPo> getYellowPunishList() {
        return yellowPunishList;
    }

    public void setYellowPunishList(List<PunishLogPo> yellowPunishList) {
        this.yellowPunishList = yellowPunishList;
    }

    public List<PunishLogPo> getRedPunishList() {
        return redPunishList;
    }

    public void setRedPunishList(List<PunishLogPo> redPunishList) {
        this.redPunishList = redPunishList;
    }
}
