package com.lanhun.distributed.lock;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class DistributeLockMethodExecutor {

    /**
     * @param target
     * @param method
     * @param args
     * @param lock
     * @return
     * @throws Exception
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    public static Object lockMethodExecute(Object target, Method method, Object[] args, Lock lock)
            throws Exception, IllegalAccessException, InvocationTargetException {
        LockMethod uniqMethod = method.getAnnotation(LockMethod.class);
        String type = null;
        if (uniqMethod != null) {
            type = uniqMethod.value();
            int lockIndex = uniqMethod.lockIndex();
            if ("".equals(type)) {
                type = method.getReturnType().getName() + "." + method.getName();
            }
            if (lockIndex > -1) {
                type = type + ":" + args[lockIndex];
            }
            boolean hasLock = lock.lock(type, "");
            if (hasLock) {
                try {
                    Object ret = method.invoke(target, args);
                    return ret;
                } catch (Exception e) {
                    throw e;
                } finally {
                    lock.unLock(type, "");
                }
            } else {
                throw new RuntimeException("can't get the lock:" + type);

            }

        } else {
            return method.invoke(target, args);
        }
    }
}
