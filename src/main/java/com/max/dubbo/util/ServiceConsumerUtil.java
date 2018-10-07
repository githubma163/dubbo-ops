package com.max.dubbo.util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Maps;
import com.max.dubbo.entity.ServiceConsumer;

/**
 * 
 * @author githubma
 * @date 2018年4月3日 下午4:39:59
 *
 */
public class ServiceConsumerUtil {

	public static ServiceConsumer convert(String serviceConsumerInfo) throws MalformedURLException {
		ServiceConsumer serviceConsumer = new ServiceConsumer();
		serviceConsumer.setContent(serviceConsumerInfo);
		serviceConsumerInfo = serviceConsumerInfo.replaceFirst("consumer://", "http://");
		URL url = new URL(serviceConsumerInfo);
		serviceConsumer.setHost(url.getHost());
		String invokeInterface = url.getPath().replaceAll("/", "");
		serviceConsumer.setInvokeInterface(invokeInterface);
		String query = url.getQuery();
		String[] params = query.split("&");
		Map<String, String> paramMap = Maps.newHashMap();
		for (String param : params) {
			String[] tempParams = param.split("=");
			paramMap.put(tempParams[0], tempParams[1]);
		}
		serviceConsumer.setApplication(paramMap.get("application"));
		serviceConsumer.setCallInterface(paramMap.get("interface"));
		if (StringUtils.isBlank(paramMap.get("methods"))) {
			serviceConsumer.setMethods("");
		} else {
			serviceConsumer.setMethods(paramMap.get("methods"));
		}
		serviceConsumer.setPid(paramMap.get("pid"));
		serviceConsumer.setVersion(paramMap.get("version"));
		serviceConsumer.setDubbo(paramMap.get("dubbo"));
		serviceConsumer.setAuthority(serviceConsumer.getHost() + ":" + serviceConsumer.getPid());
		return serviceConsumer;
	}

}
