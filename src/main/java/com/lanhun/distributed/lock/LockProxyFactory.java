package com.lanhun.distributed.lock;

import java.lang.reflect.Proxy;

/**
 * @author vincent
 */
public class LockProxyFactory {

    @SuppressWarnings("unchecked")
    public static <T> T createProxy(Lock lock, T target) {
        return (T) Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(),
                new DistributeLockProxy(target, lock));
    }

}
