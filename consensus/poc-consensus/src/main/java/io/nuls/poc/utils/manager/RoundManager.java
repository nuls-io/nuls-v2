package io.nuls.poc.utils.manager;

import io.nuls.poc.model.bo.round.MeetingRound;

import java.util.ArrayList;
import java.util.List;

public class RoundManager {
    /**
     * 轮次列表
     * */
    private List<MeetingRound> roundList = new ArrayList<>();

    public void addRound(MeetingRound meetingRound) {
        roundList.add(meetingRound);
    }

    public MeetingRound getRoundByIndex(long roundIndex) {
        MeetingRound round = null;
        for (int i = roundList.size() - 1; i >= 0; i--) {
            round = roundList.get(i);
            if (round.getIndex() == roundIndex) {
                break;
            }
        }
        return round;
    }


}
