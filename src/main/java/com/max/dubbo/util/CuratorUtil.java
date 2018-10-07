package com.max.dubbo.util;

import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.max.dubbo.constant.Constant;
import com.max.dubbo.entity.Environment;

/**
 * 
 * @author githubma
 * @date 2018年3月5日 下午6:54:02
 *
 */
@Component
public class CuratorUtil {

	public static Map<String, CuratorFramework> clientMap;

	static {
		clientMap = Maps.newHashMap();
		List<Environment> environmentList = Constant.ENVIRONMENT_LIST;
		for (Environment environment : environmentList) {
			// 连接时间和重试次数
			RetryPolicy retryPolicy = new ExponentialBackoffRetry(5000, 3);
			CuratorFramework client = CuratorFrameworkFactory.newClient(environment.getAddress(), retryPolicy);
			client.start();
			clientMap.put(environment.getEnv(), client);
		}
	}

	public static CuratorFramework getCuratorFramework(String env) {
		return clientMap.get(env);
	}

	public static List<String> getPathChildren(String path, String env) throws Exception {
		List<String> pathChildren = CuratorUtil.getCuratorFramework(env).getChildren().forPath(path);
		return pathChildren;
	}

	public static List<String> getAllDubboServiceWithProvider(String env) throws Exception {
		List<String> dubboServiceWithProvider = new ArrayList<String>();
		String dubboPath = Constant.getDubboPath(env);
		List<String> pathChildren = getPathChildren(dubboPath, env);
		for (String path : pathChildren) {
			String providerPath = dubboPath + "/" + path + Constant.DEFAULT_PROVIDER_PATH;
			List<String> serviceProviders = CuratorUtil.getCuratorFramework(env).getChildren().forPath(providerPath);
			if (!CollectionUtils.isEmpty(serviceProviders)) {
				dubboServiceWithProvider.add(path);
			}
		}
		return dubboServiceWithProvider;
	}

	public static List<String> getDubboServiceProvider(String serviceName, String env) throws Exception {
		String dubboPath = Constant.getDubboPath(env);
		String providerPath = dubboPath + "/" + serviceName + Constant.DEFAULT_PROVIDER_PATH;
		List<String> decodeServiceProviders = Lists.newArrayList();
		List<String> serviceProviders = CuratorUtil.getCuratorFramework(env).getChildren().forPath(providerPath);
		for (String serviceProviderInfo : serviceProviders) {
			String decodeServiceProviderInfo = URLDecoder.decode(serviceProviderInfo, Charsets.UTF_8.toString());
			decodeServiceProviders.add(decodeServiceProviderInfo);
		}
		return decodeServiceProviders;
	}

}
