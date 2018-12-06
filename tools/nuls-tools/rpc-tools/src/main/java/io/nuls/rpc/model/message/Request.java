/*
 * MIT License
 *
 * Copyright (c) 2017-2018 nuls.io
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
package io.nuls.rpc.model.message;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;

/**
 * 请求调用远程方法
 * Request to invoke remote methods
 *
 * @author tangyi
 * @date 2018/11/15
 */
@ToString
@NoArgsConstructor
public class Request {
    /**
     * (Default: 0): This is a boolean value.
     * 0: The Micro server that made the request expects only a Response message, if it subscribed to the function then it may expect many Response messages.
     * 1: The Micro server that made the request expects exactly one Ack message and also a Response message, if it subscribed to the function then it may expect many Response messages.
     */
    @Getter
    @Setter
    private String requestAck;

    /**
     * This is an unsigned integer that specifies how many events do the target methods need to process before sending back another Response request.
     * The first Response is always sent as soon as possible.
     * For example, if the requested method is GetHeight and this parameter is set to 5 then the service will send back responses only after 5 blocks have been processed.
     * 0 means the method should send a Response only once; this is the default value.
     */
    @Getter
    @Setter
    private String subscriptionEventCounter;

    /**
     * This is an unsigned integer that specifies how many seconds do the target methods need to wait before sending back another Response request.
     * The first Response is always sent as soon as possible.
     * For example, if the requested method is GetHeight and this parameter is set to 5 then the service will send back responses only after 5 seconds have passed.
     * 0 means the method should send a Response only once; this is the default value.
     */
    @Getter
    @Setter
    private String subscriptionPeriod;

    /**
     * If the event defined in the target micro service returns a number, this is a string that represents the set of numbers that will trigger a Response. .
     * The string is a pair of signed decimal numbers, the first one is the lower bound, empty if not available and the second one is the higher bound.
     * If the pair starts or ends with "(" or ")" respectively then it means that the number is not included,
     * If the pair pair starts or ends with "[" or "]" respectively then it means that the number is included.
     * <p>
     * Example: Assume we only want to be notified only when the balance is equal or greater to 1000.
     * Then the getbalance request should be sent with "[1000, )" string as SubscriptionRange parameter.
     */
    @Getter
    @Setter
    private String subscriptionRange;

    /**
     * An unsigned integer which specifies the maximum number of objects that the method should return, a value of zero (the default) means no limit
     */
    @Getter
    @Setter
    private String responseMaxSize;

    /**
     * An map that holds all methods being requested with their respective parameters
     */
    @Getter
    @Setter
    private Map<String, Object> requestMethods;
}
