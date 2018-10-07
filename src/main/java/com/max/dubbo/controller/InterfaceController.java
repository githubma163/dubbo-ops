package com.max.dubbo.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;
import java.util.Map;

import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.max.dubbo.constant.Constant;
import com.max.dubbo.entity.ServiceMethod;
import com.max.dubbo.entity.ServiceProvider;
import com.max.dubbo.job.RefreshServiceMethod;
import com.max.dubbo.util.CuratorUtil;
import com.max.dubbo.util.PomUtil;
import com.max.dubbo.util.ServiceProviderUtil;
import com.max.dubbo.util.ServiceUtil;

/**
 * 
 * @author githubma
 * @date 2018年3月5日 下午6:51:37
 *
 */
@RestController
public class InterfaceController {

	Logger logger = LoggerFactory.getLogger(InterfaceController.class);

	@RequestMapping(value = "/generateInterface")
	@ResponseBody
	public Object index(@RequestBody(required = false) List<String> dependencyJarFilePath,
			@RequestParam(required = false) String jarFilePath, @RequestParam(required = true) String className) {
		try {
			return ServiceUtil.generateService(dependencyJarFilePath, jarFilePath, className, "", "1.0");
		} catch (IllegalAccessException e) {
			logger.error("", e);
			return "非法访问错误";
		} catch (ClassNotFoundException e) {
			logger.error("", e);
			return "未找到该class";
		} catch (IOException e) {
			logger.error("", e);
			return "未找到该文件";
		} catch (HttpClientErrorException e) {
			logger.error("", e);
			return "未找到该文件";
		} catch (Exception e) {
			logger.error("", e);
			return "系统错误";
		}
	}

	@RequestMapping(value = "/selectAllMethod")
	@ResponseBody
	public Object selectAllMethod(@RequestParam(required = true) String uuid) {
		try {
			return refreshAllMethodFromNexus(uuid);// 找不到方法,自动强制刷新
		} catch (Exception e) {
			logger.error("", e);
			return "系统错误";
		}
	}

	@RequestMapping(value = "/refreshAllMethod")
	@ResponseBody
	public Object refreshAllMethod(@RequestBody(required = false) List<String> dependencyJarFilePath,
			@RequestParam(required = false) String jarFilePath, @RequestParam(required = true) String uuid) {
		try {
			ServiceProvider serviceProvider = ServiceProviderUtil.serviceProviderMap.get(uuid);
			List<ServiceMethod> serviceList = ServiceUtil.generateService(dependencyJarFilePath, jarFilePath,
					serviceProvider.getService(), serviceProvider.getGroup(), serviceProvider.getVersion());
			for (ServiceMethod service : serviceList) {
				service.setGroup(serviceProvider.getGroup());
			}
			ServiceProviderUtil.serviceMethodCache(uuid, serviceList);
			return serviceList;
		} catch (IllegalAccessException e) {
			logger.error("", e);
			return "非法访问错误";
		} catch (ClassNotFoundException e) {
			logger.error("", e);
			return "未找到该class";
		} catch (IOException e) {
			logger.error("", e);
			return "未找到该文件";
		} catch (HttpClientErrorException e) {
			logger.error("", e);
			return "未找到该文件";
		} catch (Exception e) {
			logger.error("", e);
			return "系统错误";
		}
	}

	@RequestMapping(value = "/refreshAllMethodFromNexus")
	@ResponseBody
	public Object refreshAllMethodFromNexus(String uuid) {
		try {
			ServiceProvider serviceProvider = ServiceProviderUtil.serviceProviderMap.get(uuid);
			return RefreshServiceMethod.refreshServiceMethod(serviceProvider.getService(), serviceProvider.getGroup(),
					serviceProvider.getVersion());
		} catch (Exception e) {
			logger.error("", e);
			return "系统错误";
		}
	}

	@RequestMapping(value = "/selectProvider")
	@ResponseBody
	public Object selectProvider(@RequestParam(required = true) String uuid,
			@RequestParam(required = true) String group, @CookieValue(name = "env") String env) {
		try {
			ServiceMethod service = ServiceUtil.serviceMap.get(uuid);
			String serviceName = service.getServiceName();
			List<String> resultList = Lists.newArrayList();
			String path = Constant.getDubboPath(env);
			String providerPath = path + "/" + serviceName + Constant.DEFAULT_PROVIDER_PATH;
			List<String> serviceProviders = CuratorUtil.getPathChildren(providerPath, env);
			if (CollectionUtils.isEmpty(serviceProviders)) {
				return resultList;
			}
			String serviceProviderUUID = ServiceProviderUtil.generateUUID(service.getServiceName(), service.getGroup(),
					service.getServiceVersion());
			Map<String, List<String>> serviceProviderMap = Maps.newHashMap();
			for (String serviceProviderInfo : serviceProviders) {
				ServiceProvider serviceProvider = new ServiceProvider();
				String decodeServiceProviderInfo = URLDecoder.decode(serviceProviderInfo, Charsets.UTF_8.toString());
				serviceProvider = ServiceProviderUtil.convert(decodeServiceProviderInfo);
				String tempUuid = serviceProvider.getUuid();
				if (serviceProviderMap.containsKey(tempUuid)) {
					if (!serviceProviderMap.get(tempUuid).contains(serviceProvider.getAuthority())) {
						serviceProviderMap.get(tempUuid).add(serviceProvider.getAuthority());
					}
				} else {
					List<String> serviceProviderHostList = Lists.newArrayList();
					serviceProviderHostList.add(serviceProvider.getAuthority());
					serviceProviderMap.put(tempUuid, serviceProviderHostList);
				}
				serviceProvider.setProviders(serviceProviderMap.get(tempUuid));
				ServiceProviderUtil.serviceProviderCache(serviceProvider);
			}

			return ServiceProviderUtil.serviceProviderMap.get(serviceProviderUUID).getProviders();
		} catch (Exception e) {
			logger.error("", e);
			return "系统错误";
		}
	}

	@RequestMapping(value = "/generateDubboConfig")
	@ResponseBody
	public String generateDubboConfig(@RequestParam(required = true) String uuid) {
		StringBuilder config = new StringBuilder();
		try {
			ServiceProvider serviceProvider = ServiceProviderUtil.serviceProviderMap.get(uuid);
			String pomXml = PomUtil.generatePom(serviceProvider.getService());
			config.append(pomXml);
		} catch (Exception e) {
			logger.error("生成dubbo配置文件异常", e);
		}
		return StringEscapeUtils.escapeHtml4(config.toString());
	}

}
