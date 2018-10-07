package com.max.dubbo.controller;

import java.net.URLDecoder;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.max.dubbo.constant.Constant;
import com.max.dubbo.entity.ServiceConsumer;
import com.max.dubbo.entity.ServiceProvider;
import com.max.dubbo.util.CuratorUtil;
import com.max.dubbo.util.ServiceConsumerUtil;
import com.max.dubbo.util.ServiceProviderUtil;

/**
 * 
 * @author githubma
 * @date 2018年3月5日 下午6:51:55
 *
 */
@RestController
public class ZookeeperController {

	@RequestMapping(value = "/zookeeper")
	public List<String> selectNodeInfo(String path, String decode, @CookieValue(name = "env") String env)
			throws Exception {
		List<String> pathChildren = CuratorUtil.getPathChildren(path, env);
        if (!StringUtils.isBlank(decode)) {
			List<String> decodePathChildren = Lists.newArrayList();
			for (String node : pathChildren) {
				decodePathChildren.add(URLDecoder.decode(node, Charsets.UTF_8.toString()));
			}
			return decodePathChildren;
		}
		return pathChildren;
	}

	@RequestMapping(value = "/provider/list")
	public List<ServiceProvider> listDubboProvider(String key, @CookieValue(name = "env") String env) throws Exception {
		List<ServiceProvider> resultList = Lists.newArrayList();
		Set<ServiceProvider> resultSet = Sets.newHashSet();
		String dubboPath = Constant.getDubboPath(env);
		List<String> services = CuratorUtil.getPathChildren(dubboPath, env);
		if (CollectionUtils.isEmpty(services)) {
			return resultList;
		}
		List<String> matchServices = Lists.newArrayList();
		for (String service : services) {
			if (service.toLowerCase().contains(key.toLowerCase())) {// 支持字母大小写模糊匹配
				matchServices.add(service);
			}
		}
		if (CollectionUtils.isEmpty(matchServices)) {
			return resultList;
		}
		for (String service : matchServices) {
			String providerPath = dubboPath + "/" + service + Constant.DEFAULT_PROVIDER_PATH;
			List<String> serviceProviders = CuratorUtil.getPathChildren(providerPath, env);
			if (CollectionUtils.isEmpty(serviceProviders)) {// 防止dubbo服务提供者为空
//				ServiceProvider serviceProvider = new ServiceProvider();
//				serviceProvider.setService(service);
//				resultList.add(serviceProvider);
				continue;
			}
			Map<String, List<String>> serviceProviderMap = Maps.newHashMap();
			for (String serviceProviderInfo : serviceProviders) {
				ServiceProvider serviceProvider = new ServiceProvider();
				String decodeServiceProviderInfo = URLDecoder.decode(serviceProviderInfo, Charsets.UTF_8.toString());
				serviceProvider = ServiceProviderUtil.convert(decodeServiceProviderInfo);
				String uuid = serviceProvider.getUuid();
				if (serviceProviderMap.containsKey(uuid)) {
					if (!serviceProviderMap.get(uuid).contains(serviceProvider.getAuthority())) {
						serviceProviderMap.get(uuid).add(serviceProvider.getAuthority());
					}
				} else {
					List<String> serviceProviderHostList = Lists.newArrayList();
					serviceProviderHostList.add(serviceProvider.getAuthority());
					serviceProviderMap.put(uuid, serviceProviderHostList);
				}
				serviceProvider.setProviders(serviceProviderMap.get(uuid));
				ServiceProviderUtil.serviceProviderCache(serviceProvider);
				resultSet.add(serviceProvider);
			}
		}
		resultList.addAll(resultSet);
		for (ServiceProvider serviceProvider : resultList) {
			if (!CollectionUtils.isEmpty(serviceProvider.getProviders())) {
				Collections.sort(serviceProvider.getProviders(), new ComparatorByProviderName());// 添加ip+port排序，方便查看
			}
		}
		return resultList;
	}

	class ComparatorByProviderName implements Comparator<String> {

		@Override
		public int compare(String o1, String o2) {
			if (o1.compareTo(o2) > 0) {
				return 1;
			}
			if (o1.compareTo(o2) < 0) {
				return -1;
			}
			return 0;
		}

	}

	@RequestMapping(value = "/consumer/list")
	public List<ServiceConsumer> listDubboConsumer(String service, @CookieValue(name = "env") String env)
			throws Exception {
		List<ServiceConsumer> resultList = Lists.newArrayList();
		Set<ServiceConsumer> resultSet = Sets.newHashSet();
		String dubboPath = Constant.getDubboPath(env);
		String providerPath = dubboPath + "/" + service + Constant.DEFAULT_CONSUMER_PATH;
		List<String> serviceConsumers = CuratorUtil.getPathChildren(providerPath, env);
		for (String serviceConsumerInfo : serviceConsumers) {
			ServiceConsumer serviceConsumer = new ServiceConsumer();
			String decodeServiceConsumerInfo = URLDecoder.decode(serviceConsumerInfo, Charsets.UTF_8.toString());
			serviceConsumer = ServiceConsumerUtil.convert(decodeServiceConsumerInfo);
			resultSet.add(serviceConsumer);
		}
		resultList.addAll(resultSet);
		Collections.sort(resultList, new CompratorByAuthority());
		return resultList;
	}

	static class CompratorByAuthority implements Comparator<ServiceConsumer> {

		@Override
		public int compare(ServiceConsumer o1, ServiceConsumer o2) {
			if (o1.getAuthority().compareTo(o2.getAuthority()) > 0) {
				return 1;
			}
			if (o1.getAuthority().compareTo(o2.getAuthority()) < 0) {
				return -1;
			}
			return 0;
		}
	}

	@RequestMapping(value = "/getAllDubboServiceWithProvider")
	public List<String> getAllDubboServiceWithProvider(@CookieValue(name = "env") String env) throws Exception {
		return CuratorUtil.getAllDubboServiceWithProvider(env);
	}

}
