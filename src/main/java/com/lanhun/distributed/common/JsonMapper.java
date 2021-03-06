/**
 * Copyright &copy; 2012-2014 <a href="https://github.com/thinkgem/jeesite">JeeSite</a> All rights reserved.
 */
package com.lanhun.distributed.common;

import java.io.IOException;
import java.lang.reflect.Type;

import java.util.List;

import java.util.TimeZone;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;


import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.util.JSONPObject;



/**
 * 简单封装Jackson，实现JSON String<->Java Object的Mapper. 封装不同的输出风格,
 * 使用不同的builder函数创建实例.
 * 
 * @author ThinkGem
 * @version 2013-11-15
 */
public class JsonMapper extends ObjectMapper {

	private static final long serialVersionUID = 1L;



	private static JsonMapper mapper;

	public JsonMapper() {
		this(Include.NON_EMPTY);
	}

	public JsonMapper(Include include) {
		// 设置输出时包含属性的风格
		if (include != null) {
			this.setSerializationInclusion(include);
		}
		// 允许单引号、允许不带引号的字段名称
		this.enableSimple();
		// 设置输入时忽略在JSON字符串中存在但Java对象实际没有的属性
		this.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
		// 空值处理为空串
		this.getSerializerProvider().setNullValueSerializer(new JsonSerializer<Object>() {
			@Override
			public void serialize(Object value, JsonGenerator jgen, SerializerProvider provider)
					throws IOException, JsonProcessingException {
				jgen.writeString("");
			}
		});
		// 进行HTML解码。
		this.registerModule(new SimpleModule().addSerializer(String.class, new JsonSerializer<String>() {
			@Override
			public void serialize(String value, JsonGenerator jgen, SerializerProvider provider)
					throws IOException, JsonProcessingException {
				jgen.writeString(StringEscapeUtils.unescapeHtml4(value));
			}
		}));
		// 设置时区
		this.setTimeZone(TimeZone.getDefault());// getTimeZone("GMT+8:00")
	}

	/**
	 * 创建只输出非Null且非Empty(如List.isEmpty)的属性到Json字符串的Mapper,建议在外部接口中使用.
	 */

	public static JsonMapper getInstance() {
		if (mapper == null) {
			mapper = new JsonMapper().enableSimple();
		}
		return mapper;
	}

	/**
	 * 创建只输出初始值被改变的属性到Json字符串的Mapper, 最节约的存储方式，建议在内部接口中使用。
	 */
	public static JsonMapper nonDefaultMapper() {
		if (mapper == null) {
			mapper = new JsonMapper(Include.NON_DEFAULT);
		}
		return mapper;
	}

	/**
	 * Object可以是POJO，也可以是Collection或数组。 如果对象为Null, 返回"null". 如果集合为空集合, 返回"[]".
	 */
	public String toJson(Object object) {
		try {
			return this.writeValueAsString(object);
		} catch (IOException e) {

			return null;
		}
	}

	/**
	 * 反序列化POJO或简单Collection如List<String>.
	 * 
	 * 如果JSON字符串为Null或"null"字符串, 返回Null. 如果JSON字符串为"[]", 返回空集合.
	 * 
	 * 如需反序列化复杂Collection如List<MyBean>, 请使用fromJson(String,JavaType)
	 * 
	 * @see #fromJson(String, JavaType)
	 */
	public <T> T fromJson(String jsonString, Class<T> clazz) {
		if (StringUtils.isEmpty(jsonString)) {
			return null;
		}
		try {
			return this.readValue(jsonString, clazz);
		} catch (IOException e) {

			return null;
		}
	}

	/**
	 * 反序列化复杂Collection如List<Bean>, 先使用函數createCollectionType构造类型,然后调用本函数.
	 * 
	 * @see #createCollectionType(Class, Class...)
	 */
	@SuppressWarnings("unchecked")
	public <T> T fromJson(String jsonString, JavaType javaType) {
		if (StringUtils.isEmpty(jsonString)) {
			return null;
		}
		try {
			return (T) this.readValue(jsonString, javaType);
		} catch (IOException e) {

			return null;
		}
	}

	/**
	 * 構造泛型的Collection Type如: ArrayList<MyBean>,
	 * 则调用constructCollectionType(ArrayList.class,MyBean.class) HashMap
	 * <String,MyBean>, 则调用(HashMap.class,String.class, MyBean.class)
	 */
	public JavaType createCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
		return this.getTypeFactory().constructParametricType(collectionClass, elementClasses);
	}

	/**
	 * 
	 * @Title: json2Collection
	 * @Description: json反序列化为集合，支持泛型
	 * @param json
	 * @param collectionClass
	 *            ArrayList.class
	 * @param elementClasses
	 *            A.class
	 * @param    
	 *            设定文件
	 * @return T    返回类型 List<A>
	 * @throws
	 *
     *
     *
	 * 
	 * 			List<A>
	 *             dataList=json2Collection(json,ArrayList.class,A.class);
	 */
	public static <T> T json2Collection(String json, Class<?> collectionClass, Class<?>... elementClasses) {
		JavaType javaType = getCollectionType(collectionClass, elementClasses);
		return fromJsonString(json, javaType);
	}

	/**
	 * 當JSON裡只含有Bean的部分屬性時，更新一個已存在Bean，只覆蓋該部分的屬性.
	 */
	@SuppressWarnings("unchecked")
	public <T> T update(String jsonString, T object) {
		try {
			return (T) this.readerForUpdating(object).readValue(jsonString);
		} catch (JsonProcessingException e) {

		} catch (IOException e) {

		}
		return null;
	}

	/**
	 * 輸出JSONP格式數據.
	 */
	public String toJsonP(String functionName, Object object) {
		return toJson(new JSONPObject(functionName, object));
	}

	/**
	 * 設定是否使用Enum的toString函數來讀寫Enum, 為False時時使用Enum的name()函數來讀寫Enum, 默認為False.
	 * 注意本函數一定要在Mapper創建後, 所有的讀寫動作之前調用.
	 */
	public JsonMapper enableEnumUseToString() {
		this.enable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
		this.enable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
		return this;
	}



	/**
	 * 允许单引号 允许不带引号的字段名称
	 */
	public JsonMapper enableSimple() {
		this.configure(Feature.ALLOW_SINGLE_QUOTES, true);
		this.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
		return this;
	}

	/**
	 * 取出Mapper做进一步的设置或使用其他序列化API.
	 */
	public ObjectMapper getMapper() {
		return this;
	}

	/**
	 * 对象转换为JSON字符串
	 * 
	 * @param object
	 * @return
	 */
	public static String toJsonString(Object object) {
		return JsonMapper.getInstance().toJson(object);
	}

	/**
	 * JSON字符串转换为对象
	 * 
	 * @param jsonString
	 * @param clazz
	 * @return
	 */
	public static <T> T fromJsonString(String jsonString, Class<T> clazz) {
		return JsonMapper.getInstance().fromJson(jsonString, clazz);
	}

	/**
	 * JSON字符串转换为对象
	 * 
	 * @param src
	 * @param type
	 * @return
	 */
	public static <T> T fromJson(String src, Type type) {
		JavaType javaType = JsonMapper.getInstance().constructType(type);

		try {
			return JsonMapper.getInstance().readValue(src, javaType);
		} catch (Exception e) {

			return null;
		}
	}
	
	/**
	 * JSON字符串转换为对象
	 * 
	 * @param src
	 * @param type
	 * @return
	 */
	public static <T> T fromJson(byte[] src, Type type) {
		JavaType javaType = JsonMapper.getInstance().constructType(type);
		try {
			return JsonMapper.getInstance().readValue(src, javaType);
		} catch (Exception e) {


			return null;
		}
	}

	/**
	 * 
	 * @Title: convertVal @Description: 类型转换 @param @param src @param @param
	 * dest @param @return    设定文件 @return T    返回类型 @throws
	 */
	public static <T> T convertVal(Object src, Class<T> dest) {
		return JsonMapper.getInstance().convertValue(src, dest);

	}

	/**
	 * 
	 * @Title: fromJsonString @Description: 指定javaType反序列化 @param @param
	 * jsonString @param @param clazz @return T    返回类型 @throws
	 */
	public static <T> T fromJsonString(String jsonString, JavaType clazz) {
		return JsonMapper.getInstance().fromJson(jsonString, clazz);
	}

	/**
	 * 
	 * @Title: getCollectionType @Description: 获得泛型类型 @param
	 * collectionClass @param elementClasses @return JavaType    返回类型
	 * 
	 * 构造泛型的Collection Type如: ArrayList<A>,
	 * 则调用constructCollectionType(ArrayList.class,A.class) HashMap<String,A>,
	 * 则调用(HashMap.class,String.class, A.class)
	 * 
	 * @throws
	 */
	public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
		return JsonMapper.getInstance().getTypeFactory().constructParametricType(collectionClass, elementClasses);
	}

}
