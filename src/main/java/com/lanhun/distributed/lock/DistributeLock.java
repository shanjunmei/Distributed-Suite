package com.lanhun.distributed.lock;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.lanhun.distributed.common.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

public class DistributeLock implements Lock {

	private Logger logger = LogManager.getLogger(getClass());

	private final ConcurrentHashMap<String, String> lockCache = new ConcurrentHashMap<>();

	private ThreadLocal<Map<String, String>> keyCache = new ThreadLocal<>();

	private Pool<Jedis> jedisPool;

	public void setJedisPool(Pool<Jedis> jedisPool) {
		this.jedisPool = jedisPool;
	}

	// @Override
	private String prefix() {
		return "_lock";
	}

	// @Override
	private String generateKey() {
		return DateUtils.format("yyyyMMddHHmmss",new Date())+"_"+UUID.randomUUID().toString().replace("-", "");
	}

	@Override
	public boolean lock(String type,String key) {
		return lock(type,key, 0);
	}

	@Override
	public boolean lock(String type,String typeKey, int timeout) {
		type = prefix() + ":" + type+":"+typeKey;

		if (timeout == 0) {// 没有设置超时时间的锁，先校验本地缓存
			synchronized (lockCache) {
				if (lockCache.containsKey(type)) {
					if (keyCache.get()!=null) {
						Map<String, String> localKeyCache = keyCache.get();
						if(lockCache.get(type).equals(localKeyCache.get(type))){
							String countStr = localKeyCache.get(type + "_count");
							int count = Integer.valueOf(countStr);
							count = count + 1;
							localKeyCache.put(type + "_count", count + "");
							return true;
						}else{
							//normal scene never occurred
							logger.warn("Last Lock Cache Have Not Clean");
							//return false;
						}
					}
				}
			}

		}

		String key = generateKey();

		Jedis jedis = jedisPool.getResource();

		long r = jedis.setnx(type, key);
		if (timeout != 0) {
			jedis.expire(type, timeout);
		}

		jedis.close();

		if (r > 0) {
			Map<String, String> cache = keyCache.get();
			if (cache == null) {
				cache = new HashMap<>();
				keyCache.set(cache);
			}
			cache.put(type, key);
			if (timeout == 0) {
				synchronized (lockCache) {
					lockCache.put(type, key);
				}
				
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean tryLock(String type,String key) {
		return tryLock(type,key, 0L);
	}

	@Override
	public boolean tryLock(String type,String key, long timeout) {
		if (timeout == 0) {
			while (!lock(type,key)) {
				try {
					TimeUnit.MILLISECONDS.sleep( 10);
				} catch (InterruptedException e) {
					logger.info(e);
				}
			}
			return true;
		}
		long t = System.currentTimeMillis();
		while ((System.currentTimeMillis() - t) < timeout) {
			try {
				if (!lock(type,key)) {
					TimeUnit.MILLISECONDS.sleep(10);
				} else {
					return true;
				}
			} catch (InterruptedException e) {
				logger.info(e);
			}
		}
		return false;
	}

	@Override
	public void unLock(String type,String typeKey) {
		type = prefix() + ":" + type+":"+typeKey;
		Map<String, String> cache = keyCache.get();
		if (cache != null) {
			String lockCacheKey = cache.get(type);
			if(lockCacheKey==null||lockCacheKey.trim().equals("")){
				logger.debug("Ignored Unlock Without Lock For Key:"+type);
				return;
			}
			String countStr=cache.get(type+"_count");
			int count=Integer.valueOf(countStr);
			count=count-1;
			cache.put(type+"_count", count+"");
			if(count>0){
				return;
			}
			cache.remove(type);
			cache.remove(type+"_count");
			synchronized (lockCache) {
				lockCache.remove(type, lockCacheKey);
			}
			
			Jedis jedis = jedisPool.getResource();
			String key = jedis.get(type);
			if (key != null && !"".equals(key)) {
				if (key.equals(lockCacheKey)) {
					jedis.del(type);
				}
			}
			jedis.close();
		} else {
			// 忽略未加锁 进行解锁
			logger.debug("Ignored Unlock Without Lock For Key:"+type);
		}

	}
}
