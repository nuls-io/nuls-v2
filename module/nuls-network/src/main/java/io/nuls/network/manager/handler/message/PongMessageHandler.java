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

package io.nuls.network.manager.handler.message;

import io.nuls.network.manager.handler.base.BaseMessageHandler;
import io.nuls.network.model.NetworkEventResult;
import io.nuls.network.model.Node;
import io.nuls.network.model.message.base.BaseMessage;

/**
 * get time message handler
 *
 * @author lan
 * @date 2018/10/15
 */
public class PongMessageHandler extends BaseMessageHandler {

    private static PongMessageHandler instance = new PongMessageHandler();

    private PongMessageHandler() {

    }

    public static PongMessageHandler getInstance() {
        return instance;
    }

    /**
     *
     * @param message  message
     * @param node    peer info
     * @return
     */
    @Override
    public NetworkEventResult recieve(BaseMessage message, Node node) {
//        PongMessage pongMessage = (PongMessage) message;
//        LoggerUtil.logger(node.getNodeGroup().getChainId()).debug("PongMessageHandler Recieve:magicNum={}, node={},randCode={}", pongMessage.getHeader().getMagicNumber(), node.getId(), pongMessage.getMsgBody().getRandomCode());
        return NetworkEventResult.getResultSuccess();
    }
}
