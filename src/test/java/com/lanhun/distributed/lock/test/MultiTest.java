package com.lanhun.distributed.lock.test;

import com.lanhun.distributed.lock.Lock;
import com.lanhun.distributed.lock.DistributeLock;
import redis.clients.jedis.JedisPool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MultiTest {

    static int j = 0;
    static String type = "test";
    static String host = "127.0.0.1";
    static int port = 6379;
    static JedisPool jedisPool = new JedisPool(host, port);
    static Lock lock = new DistributeLock();

    public static void main(String[] args) {

        ((DistributeLock) lock).setJedisPool(jedisPool);

        Worker worker = new Worker();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        for (int i = 0; i < 10; i++) {
            executor.execute(worker);
        }
    }

    static class Worker implements Runnable {

        @Override
        public void run() {
            for (int i = 0; i < 10; i++) {
                if (lock.lock(type, "")) {
                    j++;
                    System.out.println(j);

                }
                lock.unLock(type, "");
            }
        }

    }
}
