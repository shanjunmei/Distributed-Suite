package com.lanhun.distributed.cache;

/**
 * 缓存管理
 * 
 * @author Administrator
 *
 */
public interface CacheManager {

	String cacheKeyPreProcess(String type);

	void set(String type, String value);

	String get(String type);

	<T> T handleCollectionResult(String value, Class<?> collectionClass, Class<?> elementClass);

	<T> T handleSimpleResult(String value, Class<T> cls);

	<T> T getObject(String type, Class<T> cls);

	<T> T getSimpleType(String type, Class<T> cls);

	<T> T getCollection(String type, Class<?> collectionClass, Class<?> elementClasses);

}
