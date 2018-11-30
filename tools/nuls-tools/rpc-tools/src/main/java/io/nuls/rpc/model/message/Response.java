package io.nuls.rpc.model.message;

import io.nuls.rpc.info.Constants;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

/**
 * 远程调用的方法的结果
 * Result of a remotely invoked method
 *
 * @author tangyi
 * @date 2018/11/15
 * @description
 */
@ToString
@NoArgsConstructor
public class Response {
    @Getter
    @Setter
    private String requestId;
    @Getter
    @Setter
    private String responseProcessingTime;
    @Getter
    @Setter
    private String responseStatus;
    @Getter
    @Setter
    private String responseComment;
    @Getter
    @Setter
    private String responseMaxSize;
    @Getter
    @Setter
    private Object responseData;

    public boolean isSuccess(){
        return responseStatus.equals(Constants.booleanString(true));
    }
}
