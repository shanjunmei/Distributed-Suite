package com.lanhun.distributed.cache;


import com.lanhun.distributed.common.JsonMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.util.Pool;

public class RedisCacheManager implements CacheManager {

	private Logger logger = LogManager.getLogger(getClass());


	private Pool<Jedis> jedisPool;

	public void setJedisPool(Pool<Jedis> jedisPool) {
		this.jedisPool = jedisPool;
	}

	@Override
	public String cacheKeyPreProcess(String type) {
		type = "cache" + type;
		return type;
	}

	@Override
	public void set(String type, String value) {
		type = cacheKeyPreProcess(type);
		try {
			Jedis jedis = jedisPool.getResource();
			jedis.set(type, value);
			jedis.close();
		} catch (Exception e) {
			logger.info("set cache fail :{}", type);
		}
	}

	@Override
	public String get(String type) {
		type = cacheKeyPreProcess(type);
		try {
			Jedis jedis = jedisPool.getResource();
			String value = jedis.get(type);
			jedis.close();
			return value;
		} catch (Exception e) {
			logger.info("fetch cache fail :{}", type);
			return null;
		}

	}

	@Override
	public <T> T handleCollectionResult(String value, Class<?> collectionClass, Class<?> elementClass) {
		return JsonMapper.json2Collection(value, collectionClass, elementClass);
	}

	public <T> T handleObjectResult(String value, Class<T> cls) {
		return JsonMapper.fromJsonString(value, cls);
	}

	@Override
	public <T> T handleSimpleResult(String value, Class<T> cls) {
		return JsonMapper.convertVal(value, cls);
	}

	@Override
	public <T> T getObject(String type, Class<T> cls) {
		String value = get(type);
		return handleObjectResult(value, cls);
	}

	@Override
	public <T> T getSimpleType(String type, Class<T> cls) {
		String value = get(type);
		return handleSimpleResult(value, cls);
	}

	@Override
	public <T> T getCollection(String type, Class<?> collectionClass, Class<?> elementClasses) {
		String value = get(type);
		return handleCollectionResult(value, collectionClass, elementClasses);
	}

	public static void main(String[] args) {
		int test = JsonMapper.convertVal("1", Integer.class);
		System.out.println(test);
	}
}
