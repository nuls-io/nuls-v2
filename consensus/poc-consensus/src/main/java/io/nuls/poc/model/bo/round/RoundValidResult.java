package io.nuls.poc.model.bo.round;
/**
 * 轮次信息验证结果类
 * Information about rotation
 *
 * @author tag
 * 2019/3/27
 */
public class RoundValidResult {
    private MeetingRound round;
    private boolean validResult = false;

    public MeetingRound getRound() {
        return round;
    }

    public void setRound(MeetingRound round) {
        this.round = round;
    }

    public boolean isValidResult() {
        return validResult;
    }

    public void setValidResult(boolean validResult) {
        this.validResult = validResult;
    }
}
