package com.max.dubbo.util;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang3.StringUtils;

import com.alibaba.dubbo.config.ApplicationConfig;
import com.alibaba.dubbo.config.ReferenceConfig;
import com.alibaba.dubbo.config.RegistryConfig;
import com.alibaba.dubbo.config.utils.ReferenceConfigCache;
import com.alibaba.dubbo.rpc.service.GenericService;
import com.alibaba.fastjson.JSONObject;
import com.max.dubbo.constant.Constant;
import com.max.dubbo.entity.Environment;

/**
 * 
 * @author githubma
 * @date 2018年3月5日 下午6:54:08
 *
 */
public class DubboUtil {

	public static ConcurrentHashMap<String, GenericService> genericServiceMap = new ConcurrentHashMap<String, GenericService>();

	public static GenericService createGenericService(String serviceName, String serviceVersion, String group,
			String targetProvider, String env) {
		List<Environment> environmentList = Constant.ENVIRONMENT_LIST;
		String zookeeperUrl = "";
		for (Environment environment : environmentList) {
			if (environment.getEnv().equals(env)) {
				zookeeperUrl = environment.getAddress();
			}
		}
		if (StringUtils.isBlank(zookeeperUrl)) {
			throw new RuntimeException("无法识别的环境");
		}
		String key = generateKey(serviceName, serviceVersion, group, targetProvider, env);
		if (genericServiceMap.containsKey(key)) {
			return genericServiceMap.get(key);
		}
		// 普通编码配置方式
		ApplicationConfig application = new ApplicationConfig();
		application.setName("dubbo-ops");
		// 连接注册中心配置
		RegistryConfig registry = new RegistryConfig();
		registry.setProtocol("zookeeper");
		registry.setAddress(zookeeperUrl);
		// registry.setTimeout(5000);
		String registryGroup = "";
		registryGroup = Constant.getDubboRegistryGroup(env);
		if (StringUtils.isNotBlank(registryGroup)) {
			registry.setGroup(registryGroup);
		}
		ReferenceConfig<GenericService> reference = new ReferenceConfig<GenericService>();
		if (!StringUtils.isBlank(group)) {
			reference.setGroup(group);
		}
		if (RegUtil.checkSocket(targetProvider)) {
			reference.setUrl("dubbo://" + targetProvider + "/" + serviceName);
		}
		reference.setApplication(application);
		reference.setRegistry(registry);
		reference.setInterface(serviceName);
		reference.setVersion(serviceVersion);
		reference.setGeneric(true); // 声明为泛化接口
		ReferenceConfigCache cache = ReferenceConfigCache.getCache(env);// 每个环境的泛化服务自己缓存
		cache.destroy(reference);// 指定provider的服务要去除，不然会缓存
		GenericService genericService = cache.get(reference);
		if (null == genericService) {// 防止有些dubbo服务在工程启动后在启动
			cache.destroy(reference);
			genericService = cache.get(reference);
		}
		genericServiceMap.putIfAbsent(key, genericService);
		return genericService;
	}

	private static String generateKey(String serviceName, String serviceVersion, String group, String targetProvider,
			String dev) {
		String key = serviceName + serviceVersion;
		if (!StringUtils.isBlank(group)) {
			key += group;
		}
		if (!StringUtils.isBlank(targetProvider)) {
			key += targetProvider;
		}
		key += dev;
		return key;
	}

	public static Object genericServiceInvoke(String serviceName, String serviceVersion, String group,
			String methodName, String[] parameterType, Object[] parameterValue, String targetProvider, String dev) {
		GenericService genericService = createGenericService(serviceName, serviceVersion, group, targetProvider, dev);
		// 基本类型以及Date,List,Map等不需要转换，直接调用，不支持hessian协议，com.caucho.hessian.io.HessianServiceException:
		// The service has no method named:
		Object result = null;
		try {
			result = genericService.$invoke(methodName, parameterType, parameterValue);
		} catch (Throwable t) {
			// 出错重试一次
			String key = generateKey(serviceName, serviceVersion, group, targetProvider, dev);
			genericServiceMap.remove(key);
			// result = genericService.$invoke(methodName, parameterType, parameterValue);
			throw t;
		}
		if (null == result) {
			return "{}";
		}
		return JSONObject.toJSON(result);
	}

}
