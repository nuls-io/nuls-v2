/**
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
 */
package io.nuls.h2.filter.impl;

import io.nuls.h2.session.SessionManager;
import io.nuls.h2.transactional.annotation.DbSession;
import io.nuls.h2.transactional.annotation.PROPAGATION;
import io.nuls.tools.core.annotation.Interceptor;
import io.nuls.tools.core.inteceptor.base.BeanMethodInterceptor;
import io.nuls.tools.core.inteceptor.base.BeanMethodInterceptorChain;
import io.nuls.tools.data.StringUtils;
import org.apache.ibatis.session.SqlSession;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author zhouwei
 * @date 2017/10/13
 */
@Interceptor(DbSession.class)
public class TransactionalInterceptorImpl implements BeanMethodInterceptor {

    @Override
    public Object intercept(Annotation annotation, Object obj, Method method, Object[] args, BeanMethodInterceptorChain interceptorChain) throws Throwable {
        String lastId = SessionManager.getId();
        String id = lastId;
        if (id == null) {
            id = StringUtils.getNewUUID();
        }
        Object result;
        boolean isSessionBeginning = false;
        boolean isCommit = false;
        DbSession ann = (DbSession) annotation;
        if (ann.transactional() == PROPAGATION.REQUIRED && !SessionManager.getTxState(id)) {
            isCommit = true;
        } else if (ann.transactional() == PROPAGATION.INDEPENDENT) {
            id = StringUtils.getNewUUID();
            isCommit = true;
        }
        SqlSession session = SessionManager.getSession(id);
        if (session == null) {
            isSessionBeginning = true;
            session = SessionManager.openSession(false);
            SessionManager.setConnection(id, session);
            SessionManager.setId(id);
        } else {
            isSessionBeginning = false;
        }
        try {
            if (isCommit) {
                SessionManager.startTransaction(id);
            }
            result = interceptorChain.execute(annotation, obj, method, args);
            if (isCommit) {
                session.commit();
                SessionManager.endTransaction(id);
            }
        } catch (Exception e) {
            session.rollback();
            SessionManager.endTransaction(id);
            throw e;
        } finally {
            if (isSessionBeginning) {
                SessionManager.setConnection(id, null);
                SessionManager.setId(lastId);
                session.close();
            }
        }
        return result;
    }


    private boolean isFilterMethod(Method method) {
        return false;
    }

}
