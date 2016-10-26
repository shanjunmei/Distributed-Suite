package com.lanhun.distributed.lock;

import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * @author vincent
 */
public class DistributeLockInterceptor implements MethodInterceptor {

    private Lock lock;

    public void setLock(Lock lock) {
        this.lock = lock;
    }

    @Override
    public Object intercept(Object target, Method method, Object[] args, MethodProxy proxy) throws Throwable {

        return DistributeLockMethodExecutor.lockMethodExecute(target, method, args, lock);
    }

}
