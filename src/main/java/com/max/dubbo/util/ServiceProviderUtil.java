package com.max.dubbo.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.lang3.StringUtils;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.max.dubbo.entity.ServiceMethod;
import com.max.dubbo.entity.ServiceProvider;

/**
 * 
 * @author githubma
 * @date 2018年3月5日 下午6:54:31
 *
 */
public class ServiceProviderUtil {

	public static ConcurrentHashMap<String, ServiceProvider> serviceProviderMap = new ConcurrentHashMap<String, ServiceProvider>();

	public static ConcurrentHashMap<String, List<ServiceMethod>> serviceMethodMap = new ConcurrentHashMap<String, List<ServiceMethod>>();

	public static ServiceProvider convert(String serviceProviderInfo) throws MalformedURLException {
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ServiceProvider serviceProvider = new ServiceProvider();
		serviceProvider.setContent(serviceProviderInfo);
		serviceProviderInfo = serviceProviderInfo.replaceFirst("dubbo://", "http://");
		serviceProviderInfo = serviceProviderInfo.replaceFirst("hessian://", "http://");
		serviceProviderInfo = serviceProviderInfo.replaceFirst("rest://", "http://");
		URL url = new URL(serviceProviderInfo);
		serviceProvider.setAuthority(url.getAuthority());
		serviceProvider.setHost(url.getHost());
		// String service = url.getPath().replaceAll("/", "");//不是取这个值
		// serviceProvider.setService(service);
		String query = url.getQuery();
		String[] params = query.split("&");
		Map<String, String> paramMap = Maps.newHashMap();
		for (String param : params) {
			String[] tempParams = param.split("=");
			paramMap.put(tempParams[0], tempParams[1]);
		}
		String service = paramMap.get("interface");
		serviceProvider.setService(service);
		String app = paramMap.get("application");// 使用application，这是dubbo.xml中配置的，不要使用serverAppName，这是META-INF下配置的
		if (StringUtils.isBlank(app)) {
			serviceProvider.setApp("");
		} else {
			serviceProvider.setApp(app);
		}
		String group = paramMap.get("group");
		if (!StringUtils.isBlank(group)) {
			serviceProvider.setGroup(paramMap.get("group"));
		}
		String version = paramMap.get("version");
		if (!StringUtils.isBlank(version)) {
			serviceProvider.setVersion(paramMap.get("version"));
		}
		String timestamp = paramMap.get("timestamp");
		if (!StringUtils.isBlank(timestamp)) {
			Long timestampValue = Long.parseLong(timestamp);
			Date date = new Date(timestampValue);
			serviceProvider.setTimestamp(simpleDateFormat.format(date));
		}
		serviceProvider.setUuid(generateUUID(service, group, version));
		return serviceProvider;
	}

	public static String generateUUID(String service, String group, String version) {
		if (StringUtils.isBlank(service)) {
			throw new RuntimeException("接口名称不能为空");
		}
		if (StringUtils.isBlank(group)) {
			group = "";
		}
		if (StringUtils.isBlank(version)) {
			version = "";
		}
		String key = service + group + version;
		String uuid = DigestUtils.md5Hex(key);
		return uuid;
	}

	public static void serviceProviderCache(ServiceProvider serviceProvider) {
		serviceProviderMap.put(serviceProvider.getUuid(), serviceProvider);
	}

	public static void serviceProviderHostCache(ServiceProvider serviceProvider) {
		if (CollectionUtils.isEmpty(serviceProvider.getProviders())) {
			List<String> serviceProviderList = Lists.newArrayList();
			serviceProviderList.add(serviceProvider.getAuthority());
			serviceProvider.setProviders(serviceProviderList);
		} else {
			List<String> serviceProviderList = serviceProvider.getProviders();
			serviceProviderList.add(serviceProvider.getAuthority());
			serviceProvider.setProviders(serviceProviderList);
		}
	}

	public static void serviceMethodCache(String uuid, List<ServiceMethod> serviceMethodList) {
		serviceMethodMap.put(uuid, serviceMethodList);
	}

}
