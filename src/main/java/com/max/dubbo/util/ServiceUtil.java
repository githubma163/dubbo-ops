package com.max.dubbo.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.GenericArrayType;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import com.google.common.collect.Sets;
import com.max.dubbo.constant.Constant;
import com.max.dubbo.entity.ServiceMethod;

/**
 * 
 * @author githubma
 * @date 2018年3月5日 下午6:54:40
 *
 */
public class ServiceUtil {

	public static ConcurrentMap<String, ServiceMethod> httpServiceMap = new ConcurrentHashMap<String, ServiceMethod>();

	public static ConcurrentHashMap<String, ServiceMethod> serviceMap = new ConcurrentHashMap<String, ServiceMethod>();

	static Logger logger = LoggerFactory.getLogger(ServiceUtil.class);

	/**
	 * 
	 * @param filePath
	 * @param className
	 * @return
	 * @throws IllegalAccessException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static List<ServiceMethod> generateService(List<String> dependencyJarFilePath, String jarFilePath,
			String className, String group, String version)
			throws IllegalAccessException, ClassNotFoundException, IOException {
		String jarSourceFilePath = "";
		if (!StringUtils.isBlank(jarFilePath)) {
			jarFilePath.substring(0, jarFilePath.length() - 4);// 去除.jar
			jarSourceFilePath += "-sources.jar";
		}
		JarUtil.saveJarSourceFile(jarFilePath);// 下载java源码文件
		Class<?> clazz = null;
		URLClassLoader urlClassLoader = null;
		if (CollectionUtils.isEmpty(dependencyJarFilePath) && StringUtils.isBlank(jarFilePath)) {
			clazz = Class.forName(className);
		} else {
			if (CollectionUtils.isEmpty(dependencyJarFilePath)) {
				dependencyJarFilePath = new ArrayList<String>();
			}
			dependencyJarFilePath.add(jarFilePath);
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			URL[] fileUrlArray = new URL[dependencyJarFilePath.size()];
			int i = 0;
			for (String jarFile : dependencyJarFilePath) {
				JarUtil.saveJarFile(jarFile);
				String fileName = FilenameUtils.getName(jarFile);
				File file = null;
				if (jarFilePath.startsWith("http")) {
					file = new File(Constant.JAR_FILE_PATH + File.separator + fileName);
				} else {
					file = new File(jarFile);
				}
				fileUrlArray[i++] = file.toURI().toURL();
			}
			urlClassLoader = new URLClassLoader(fileUrlArray, contextClassLoader);
			clazz = urlClassLoader.loadClass(className);
		}
		List<ServiceMethod> serviceList = new ArrayList<ServiceMethod>();
		if (null == clazz || !clazz.isInterface()) {// 必须是接口类型
			return serviceList;
		}
		Method[] methods = clazz.getMethods();
		Set<String> methodUUIDSet = Sets.newHashSet();
		for (Method method : methods) {
			ServiceMethod serviceMethod = new ServiceMethod();
			serviceMethod.setServiceName(clazz.getName());
			serviceMethod.setServiceVersion(version);
			serviceMethod.setGroup(group);
			serviceMethod.setMethodName(method.getName());
			serviceMethod.setMethodSignature(method.toGenericString());// 如果范型class存在，会显示范型类型,范型class不存在，会显示异常信息，如<java.lang.TypeNotPresentException:
																		// Type
																		// AccountAuthTspDTO
																		// not present>
			serviceMethod.setMethodString(method.toString());// 不管范型class是否存在都不会显示范型类型
			// java中标识一个方法只需要方法名称和参数类型即可，和返回类型（包括返回类型范型）和异常类型无关，所以method.toString()可以唯一标识一个方法
			serviceMethod.setReturnType(method.getReturnType().getTypeName());// 不能使用method.getReturnType().getName(),byte[]
																				// 会显示成为[B
			String methodUUID = generateUUID(serviceMethod.getServiceName(), serviceMethod.getGroup(),
					serviceMethod.getServiceVersion(), serviceMethod.getMethodString());
			if (methodUUIDSet.contains(methodUUID)) {
				logger.error("methodUUID重复,service:" + serviceMethod.getMethodName() + ",method:"
						+ serviceMethod.getMethodName());
				for (int i = 0; i < 5; i++) {
					methodUUID = UUID.randomUUID().toString().replaceAll("-", "");
					if (!methodUUIDSet.contains(methodUUID)) {
						break;
					}
				}
				logger.error("methodUUID已经尝试生成5次");
			}
			serviceMethod.setUuid(methodUUID);
			serviceMethod.setJarFilePath(jarFilePath);
			serviceMethod.setJarSourceFilePath(jarSourceFilePath);
			serviceMethod.setDependencyJarFilePath(dependencyJarFilePath);
			String[] parameterType = new String[method.getParameterCount()];
			Object[] parameterValue = new Object[method.getParameterCount()];
			for (int i = 0, l = method.getParameterCount(); i < l; i++) {
				// String[] paramNames = AsmUtil.getMethodParameterNamesByAsm4(clazz, method);
				Parameter parameter = method.getParameters()[i];
				String paramType = parameter.getType().getName();// 参数类型
				Type type = parameter.getParameterizedType();
				if (parameter.getType().isArray()) {// 数组类型
					// String arrayType = parameter.getType().getComponentType().getName();//
					// 获取数组中包含对象的类型
					paramType = paramType.replace("[L", "");
					paramType = paramType.replace(";", "");
					parameterType[i] = paramType + "[]";
				} else {// 基本类型或系统对象
					parameterType[i] = parameter.getType().getName();
				}
				if (parameter.getType().isPrimitive()) {// 基本原始类型 int
					parameterValue[i] = getPrimitiveTypeValue(paramType);
				} else {
					Object paramValue = getReferenceTypeValue(paramType, urlClassLoader);
					if (parameter.getType().isArray()) {
						Object[] paramValueArray = new Object[1];
						paramValueArray[0] = paramValue;
						parameterValue[i] = paramValueArray;
					} else if (type instanceof ParameterizedType) {// 范型类型
						if (paramType.contains("Map")) {// map类型
							ParameterizedType parameterizedType = (ParameterizedType) type;
							Class mapKeyClazz = (Class) parameterizedType.getActualTypeArguments()[0];
							Class mapValueClazz = (Class) parameterizedType.getActualTypeArguments()[1];
							Object mapKeyClazzValue = getReferenceTypeValue(mapKeyClazz.getName(), urlClassLoader);
							Object mapValueClazzValue = getReferenceTypeValue(mapValueClazz.getName(), urlClassLoader);
							Map map = new HashMap();
							map.put(mapKeyClazzValue, mapValueClazzValue);
							parameterValue[i] = map;
						} else {// list类型，set类型
							ParameterizedType parameterizedType = (ParameterizedType) type;
							// Class genericClazz = (Class) parameterizedType.getActualTypeArguments()[0];
							Class genericClazz = getParameterizedType(parameterizedType);
							Object genericClazzValue = getReferenceTypeValue(genericClazz.getName(), urlClassLoader);
							Object[] paramValueArray = new Object[1];
							paramValueArray[0] = genericClazzValue;
							parameterValue[i] = paramValueArray;
						}
					} else {
						parameterValue[i] = paramValue;
					}
				}

			}
			serviceMethod.setParameterType(parameterType);
			serviceMethod.setParameterValue(parameterValue);
			generateServiceHttp(clazz, method, serviceMethod);
			serviceMap.put(serviceMethod.getUuid(), serviceMethod);
			serviceList.add(serviceMethod);
		}
		return serviceList;
	}

	// 获取范型的类型,之前的写法不支持多层嵌套，如java.util.List<java.util.List<double[]>>，参考dubbo自带的工具类处理ReflectUtils.getGenericClass
	// 会出现这种情形
	@SuppressWarnings("rawtypes")
	public static Class getParameterizedType(ParameterizedType parameterizedType) {
		Object genericClass = parameterizedType.getActualTypeArguments()[0];
		if (genericClass instanceof ParameterizedType) { // 处理多级泛型
			return (Class<?>) ((ParameterizedType) genericClass).getRawType();
		} else if (genericClass instanceof GenericArrayType) { // 处理数组泛型
			return (Class<?>) ((GenericArrayType) genericClass).getGenericComponentType();
		} else if (((Class) genericClass).isArray()) {
			// 在 JDK 7 以上的版本, Foo<int[]> 不再是 GenericArrayType
			return ((Class) genericClass).getComponentType();
		} else {
			return (Class<?>) genericClass;
		}
	}

	/**
	 * 生成http接口调用方式
	 * 
	 * @param methods
	 * @param service
	 */
	@SuppressWarnings("rawtypes")
	private static void generateServiceHttp(Class clazz, Method method, ServiceMethod service) {
		StringBuilder serviceHttpUrl = new StringBuilder();
		serviceHttpUrl.append("/api");
		// if (!StringUtils.isBlank(service.getGroup())) {
		// serviceHttpUrl.append("/" + service.getGroup());
		// }
		serviceHttpUrl.append("/" + method.getName());
		// serviceHttpUrl.append("/" + clazz.getSimpleName()).append("/" +
		// service.getServiceVersion())
		// .append("/" + method.getName());//
		// 类似与maven的gav（groupId+artifactId+version）,dubbo可以通过givm(group+interface+version+method)来区分
		String httpServiceKey = service.getUuid();
		serviceHttpUrl.append("/" + httpServiceKey);
		service.setServiceHttpUrl(serviceHttpUrl.toString());
		if (method.getParameterCount() == 0) {
			service.setServiceHttpUrlWithParam(serviceHttpUrl.toString());
			httpServiceMap.putIfAbsent(httpServiceKey, service);
			return;
		}
		StringBuilder serviceHttpUrlParam = new StringBuilder();
		serviceHttpUrlParam.append("?");// http接口url参数
		int serviceHttpUrlParamCount = 0;// http请求url参数个数
		int serviceHttpBodyCount = 0;// http请求body参数个数
		Object[] parameterValue = service.getParameterValue();
		for (int i = 0, l = method.getParameterCount(); i < l; i++) {
			Parameter parameter = method.getParameters()[i];
			Type type = parameter.getParameterizedType();
			if (parameter.getType().isPrimitive()) {// 基本原始类型 int
				serviceHttpUrlParam.append(parameter.getName() + "=" + parameterValue[i]);
				serviceHttpUrlParamCount++;
			} else {
				if (parameter.getType().isArray()) {
					serviceHttpBodyCount++;
				} else if (type instanceof ParameterizedType) {// 范性类型
					serviceHttpBodyCount++;
				} else {
					try {
						serviceHttpUrlParam.append(parameter.getName() + "=" + parameterValue[i]);
					} catch (Exception e) {
						e.printStackTrace();
					}

				}
			}
		}
		String serviceHttpUrlParamString = serviceHttpUrlParam.toString();
		String serviceHttpMsg = "";
		if (serviceHttpUrlParamCount > 0 && serviceHttpBodyCount > 0) {// url参数和body参数都有
			serviceHttpUrlParamString = "";
			serviceHttpMsg = "接口定义不规范，url参数和body参数不能都有";
			if (serviceHttpBodyCount > 1) {// body参数超过1个，则无法通过post方式
				serviceHttpMsg += ";body对象超过一个";
			}
		}
		if (serviceHttpBodyCount > 1) {// body参数超过1个，则无法通过post方式
			serviceHttpMsg = "body对象超过一个";
		}
		if (serviceHttpUrlParamString.endsWith("?")) {
			serviceHttpUrlParamString = serviceHttpUrlParamString.substring(0, serviceHttpUrlParamString.length() - 1);
		}
		service.setServiceHttpUrlWithParam(serviceHttpUrl + serviceHttpUrlParamString);
		if (serviceHttpUrlParamCount == 0 && serviceHttpUrlParamCount > 0) {
			service.setServiceHttpBody(parameterValue);
		}
		service.setServiceHttpMsg(serviceHttpMsg);
		httpServiceMap.putIfAbsent(httpServiceKey, service);
	}

	@SuppressWarnings("rawtypes")
	public static Object getReferenceTypeValue(String paramType, ClassLoader classLoader)
			throws IllegalAccessException, ClassNotFoundException {// 获取引用类型值,classLoader确保第三方jar中依赖的classloader和要生成的className是一个
		Object paramValue = null;
		Class clazz = null;
		if (null == classLoader) {
			clazz = Class.forName(paramType);
		} else {
			clazz = Class.forName(paramType, true, classLoader);
		}
		// instanceof 针对实例,是子-->父
		// isAssignableFrom针对class对象,父-->子
		// asSubclass转换成子类，java.lang.ClassCastException: class java.lang.String
		if (Number.class.isAssignableFrom(clazz)) {// 初始化没有init方法的class类，如java.lang.Intetger()
			paramValue = 0;
			return paramValue;
		}
		if (paramType.equals("java.lang.Character")) {
			paramValue = 'c';
			return paramValue;
		}
		if (paramType.equals("java.lang.Boolean")) {
			paramValue = false;
			return paramValue;
		}
		if (paramType.equals("java.util.Date")) {
			paramValue = System.currentTimeMillis();
			return paramValue;
		}
		if (paramType.equals("java.lang.Object")) {// Map<String,Object> 中value无法序列化
			return null;
		}
		try {
			paramValue = clazz.newInstance();
			return paramValue;
		} catch (InstantiationException e) {// InstantiationException e //com.sun.jdi.InvocationException occurred
			return null;
		}
	}

	@SuppressWarnings("rawtypes")
	public static Object getReferenceTypeValueWithoutInitMethod(Class clazz) {
		if (Map.class.isAssignableFrom(clazz)) {
			return new HashMap();
		}
		if (List.class.isAssignableFrom(clazz)) {
			return new ArrayList();
		}
		if (Set.class.isAssignableFrom(clazz)) {
			return new HashSet();
		}
		return null;
	}

	public static Object getPrimitiveTypeValue(String paramType) {// 获取基本类型值
		if (paramType.equals("char")) {
			return 'c';
		}
		if (paramType.equals("boolean")) {
			return false;
		}
		return 0;
	}

	public static String generateUUID(String service, String group, String version, String methodString) {
        if (StringUtils.isBlank(service)) {
			throw new RuntimeException("接口名称不能为空");
		}
		if (StringUtils.isBlank(group)) {
			group = "";
		}
		if (StringUtils.isBlank(version)) {
			version = "";
		}
		if (StringUtils.isBlank(methodString)) {
			throw new RuntimeException("方法签名不能为空");
		}
		String key = service + group + version + methodString;
		String uuid = DigestUtils.md5Hex(key);
		return uuid;
	}

}
