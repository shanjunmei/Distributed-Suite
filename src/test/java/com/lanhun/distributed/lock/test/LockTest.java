package com.lanhun.distributed.lock.test;

import com.lanhun.distributed.lock.Lock;
import com.lanhun.distributed.lock.DistributeLock;
import redis.clients.jedis.JedisPool;

public class LockTest {

    public static void main(String[] args) {
        String type = "test";
        String host = "127.0.0.1";
        int port = 6379;

        JedisPool jedisPool = new JedisPool(host, port);
        Lock lock = new DistributeLock();
        ((DistributeLock) lock).setJedisPool(jedisPool);
        lock.lock(type, "");


        //lock.lock(type+"12");

        //lock.unLock(type+"12");
        //lock.unLock(type);
        for (int i = 0; i < 100; i++) {
            t(type, lock);
        }
    }

    private static void t(String type, Lock lock) {
        boolean x = false;
        long t = System.currentTimeMillis();
        for (int i = 0; i < 10000; i++) {
            x = lock.lock(type, "");
            //System.out.println(x);
            lock.unLock(type, "");
        }
        t = System.currentTimeMillis() - t;
        System.out.println(t + " ms");
    }

}
