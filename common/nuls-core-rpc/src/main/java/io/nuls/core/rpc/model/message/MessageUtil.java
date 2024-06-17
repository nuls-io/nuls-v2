/*
 * MIT License
 *
 * Copyright (c) 2017-2019 nuls.io
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */
package io.nuls.core.rpc.model.message;

import io.nuls.core.rpc.netty.channel.manager.ConnectManager;
import io.nuls.core.rpc.info.Constants;
import io.nuls.core.rpc.util.NulsDateUtils;
import io.nuls.core.constant.ErrorCode;
import io.nuls.core.model.DateUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Message tool class, used to construct commonly used basic message bodies
 * Message Tool Class for Constructing Commonly Used Basic Message Body
 *
 * @author tangyi
 * @date 2018/12/4
 */
public class MessageUtil {

    /**
     * defaultMessageobject
     * Default Message object
     *
     * @param messageType Message type
     * @return Message
     */
    public static Message basicMessage(MessageType messageType) {
        Message message = new Message();
        message.setMessageID(Constants.nextSequence());
        message.setMessageType(messageType.name());
        message.setTimestamp(String.valueOf(System.currentTimeMillis() ));
        message.setTimeZone(DateUtils.TIME_ZONE_STRING);
        return message;
    }


    /**
     * Default handshake object
     * Default NegotiateConnection object
     *
     * @return NegotiateConnection
     */
    public static NegotiateConnection defaultNegotiateConnection() {
        NegotiateConnection negotiateConnection = new NegotiateConnection();
        negotiateConnection.setAbbreviation(ConnectManager.LOCAL.getAbbreviation());
        negotiateConnection.setProtocolVersion("0.1");
        negotiateConnection.setCompressionAlgorithm("zlib");
        negotiateConnection.setCompressionRate("0");
        return negotiateConnection;
    }

    /**
     * Construct defaultRequestobject
     * Constructing a default Request object
     *
     * @return Request
     */
    public static Request defaultRequest() {
        Request request = new Request();
        request.setRequestAck("0");
        request.setSubscriptionEventCounter("0");
        request.setSubscriptionPeriod("0");
        request.setSubscriptionRange("0");
        request.setResponseMaxSize("0");
        request.setRequestMethods(new HashMap<>(1));
        return request;
    }


    /**
     * Construct based on parametersRequestObject, then sendRequest
     * Construct the Request object according to the parameters, and then send the Request
     *
     * @param cmd                      Cmd of remote method
     * @param params                   Parameters of remote method
     * @param ack                      Need an Ack?
     * @param subscriptionPeriod       Remote method call frequency（second）,Frequency of remote method (Second)
     * @param subscriptionEventCounter Remote method call frequency（Change frequency）,Frequency of remote method (Change count)
     * @return Request
     */
    public static Request newRequest(String cmd, Map params, String ack, String subscriptionPeriod, String subscriptionEventCounter) {
        Request request = defaultRequest();
        request.setRequestAck(ack);
        request.setSubscriptionPeriod(subscriptionPeriod);
        request.setSubscriptionEventCounter(subscriptionEventCounter);
        request.getRequestMethods().put(cmd, params);
        return request;
    }


    /**
     * Construct aResponseobject
     * Constructing a new Response object
     *
     * @param requestId Message ID of request
     * @param status    1 = success, 0 = failed
     * @param comment   User defined string
     * @return Response
     */
    public static Response newResponse(String requestId, int status, String comment) {
        Response response = new Response();
        response.setRequestID(requestId);
        response.setResponseStatus(status);
        response.setResponseComment(comment);
        response.setResponseMaxSize(Constants.ZERO);
        return response;
    }

    /**
     * Construct a successful executionResponseobject
     * Constructing a new Response object
     *
     * @param requestId Message ID of request
     * @return Response
     */
    public static Response newSuccessResponse(String requestId) {
        return newResponse(requestId, Response.SUCCESS, Response.SUCCESS_MSG);
    }

    /**
     * Construct a successful executionResponseobject
     * Constructing a new Response object
     *
     * @param requestId Message ID of request
     * @return Response
     */
    public static Response newSuccessResponse(String requestId,String msg) {
        return newResponse(requestId, Response.SUCCESS, msg);
    }

    /**
     * Construct a failed executionResponseobject
     * Constructing a new Response object
     *
     * @param requestId Message ID of request
     * @return Response
     */
    public static Response newFailResponse(String requestId,String msg) {
        return newResponse(requestId, Response.FAIL, msg);
    }

    /**
     * Construct a failed executionResponseobject
     * Constructing a new Response object
     *
     * @param requestId Message ID of request
     * @param errorCode error object
     * @return Response
     */
    public static Response newFailResponse(String requestId, ErrorCode errorCode){
        Objects.requireNonNull(errorCode,"errorCode can't be null");
        Response response = newFailResponse(requestId,errorCode.getMsg());
        response.setResponseErrorCode(errorCode.getCode());
        return response;
    }



}
