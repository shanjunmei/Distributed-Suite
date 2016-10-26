package com.lanhun.distributed.lock;

/**
 * @author vincent
 */
public interface Lock {

    //String generateKey();

    //String prefix();

    boolean lock(String type, String key);

    boolean lock(String type, String key, int timeout);

    boolean tryLock(String type, String key);

    boolean tryLock(String type, String key, long timeout);

    void unLock(String type, String key);

}
