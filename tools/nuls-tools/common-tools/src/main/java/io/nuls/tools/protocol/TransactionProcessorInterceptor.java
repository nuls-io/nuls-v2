/**
 * MIT License
 * <p>
 * Copyright (c) 2017-2018 nuls.io
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package io.nuls.tools.protocol;

import io.nuls.tools.core.annotation.Interceptor;
import io.nuls.tools.core.inteceptor.base.BeanMethodInterceptor;
import io.nuls.tools.core.inteceptor.base.BeanMethodInterceptorChain;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * @author zhouwei
 * @date 2017/10/13
 */
@Interceptor(TransactionProcessor.class)
public class TransactionProcessorInterceptor implements BeanMethodInterceptor<TransactionProcessor> {

    @Override
    public Object intercept(TransactionProcessor annotation, Object object, Method method, Object[] params, BeanMethodInterceptorChain interceptorChain) throws Throwable {
        Map map = (Map) params[0];
        int chainId = (Integer) map.get("chainId");
        ProtocolGroup context = ProtocolGroupManager.getProtocol(chainId);
        short version = context.getVersion();
        Protocol protocol = context.getProtocolsMap().get(version);
        boolean validate = ProtocolValidator.transactionValidate(annotation.txType(), object.getClass().getSuperclass(), protocol, method.getName(), annotation.methodType());
        if (!validate) {
            throw new RuntimeException("The transaction or transaction processor is not available in the current version!");
        }
        return interceptorChain.execute(annotation, object, method, params);
    }
}
