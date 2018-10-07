package com.max.dubbo.job;

import java.net.URLClassLoader;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.max.dubbo.constant.Constant;
import com.max.dubbo.entity.Environment;
import com.max.dubbo.entity.NexusDependecyList;
import com.max.dubbo.entity.ServiceMethod;
import com.max.dubbo.entity.ServiceProvider;
import com.max.dubbo.util.ArtifactoryUtil;
import com.max.dubbo.util.CuratorUtil;
import com.max.dubbo.util.DubboUtil;
import com.max.dubbo.util.JarUtil;
import com.max.dubbo.util.JavaCompilerUtil;
import com.max.dubbo.util.NexusUtil;
import com.max.dubbo.util.ReflectionUtil;
import com.max.dubbo.util.ServiceProviderUtil;
import com.max.dubbo.util.ServiceUtil;

/**
 * 
 * @author githubma
 * @date 2018年3月5日 下午6:54:52
 *
 */
@Component
public class RefreshServiceMethod {

	static Logger logger = LoggerFactory.getLogger(RefreshServiceMethod.class);

	public static Map<String, String> compileMap = new HashMap<String, String>();

	public static String compileSwitch = "compileSwitch";

	// @Scheduled(cron = "0 0/10 * * * ?")
	public void refreshAllService() throws Exception {
		long start = System.currentTimeMillis();
		logger.info("刷新服务开始");
		List<Environment> environmentList = Constant.ENVIRONMENT_LIST;
		for (Environment environment : environmentList) {
			logger.info("环境" + environment.getEnv());
			List<String> serviceNameList = CuratorUtil.getAllDubboServiceWithProvider(environment.getEnv());
			logger.info("刷新服务总数:" + serviceNameList.size());
			refreshAllServiceProvider(serviceNameList, environment.getEnv());
		}
		refreshAllServiceMethod();
		initAllService();
		compileAllServiceMethod();
		// initInvokeAllServiceMethod();// 本地测试使用
		long end = System.currentTimeMillis();
		logger.info("刷新服务结束,花费" + (end - start) + "ms");
	}

	// 动态编译java代码
	private static void compileAllServiceMethod() {
		logger.info("动态编译java开始");
		if (Constant.DYNAMIC_COMPILE_SWITCH.equals(Constant.SWITCH_OFF)) {
			logger.info("编译功能关闭");
			return;
		}
		URLClassLoader uRLClassLoader = null;
		try {
			List<String> serviceList = Lists.newArrayList();
			for (Entry<String, ServiceMethod> serviceEntry : ServiceUtil.serviceMap.entrySet()) {
				serviceList.add(serviceEntry.getValue().getServiceName());
			}
			JavaCompilerUtil.compileJarClass(serviceList);
			uRLClassLoader = JavaCompilerUtil.getURLClassLoader();
		} catch (Exception e) {
			logger.error("动态编译java代码出错", e);
		}
		if (null != ServiceUtil.serviceMap) {
			for (Entry<String, ServiceMethod> serviceEntry : ServiceUtil.serviceMap.entrySet()) {
				try {

					String[] parameterName = JavaCompilerUtil.getClassParamaterWithAllJar(uRLClassLoader,
							serviceEntry.getValue().getServiceName(), serviceEntry.getValue().getMethodString());
					if (null != parameterName) {
						serviceEntry.getValue().setParameterName(parameterName);
						logger.info("动态编译java代码成功,service:" + serviceEntry.getValue().getServiceName());
					}
				} catch (Throwable e) {
					logger.error("动态编译java代码出错", e);
					continue;
				}
			}
		}
		logger.info("动态编译java结束");
	}

	public void refreshAllServiceProvider(List<String> serviceNameList, String env) throws Exception {
		for (String serviceName : serviceNameList) {
			logger.info("刷新服务提供者:" + serviceName);
			List<String> serviceProviders = CuratorUtil.getDubboServiceProvider(serviceName, env);
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
			}
		}
	}

	public void refreshAllServiceMethod() {
		if (null != ServiceProviderUtil.serviceProviderMap) {
			String jarFilePath = "";
			List<String> dependencyJarFilePath = null;
			for (Entry<String, ServiceProvider> serviceProviderEntry : ServiceProviderUtil.serviceProviderMap
					.entrySet()) {
				ServiceProvider serviceProvider = serviceProviderEntry.getValue();
				String service = serviceProvider.getService();
				logger.info("刷新服务方法:" + service);
				NexusDependecyList nexusDependecyList = NexusUtil.searchDependencyWithClassName(service);
				try {
					if (CollectionUtils.isEmpty(nexusDependecyList.getData())) {
						logger.info("nexus私服未找到依赖的jar,service:" + service);
						jarFilePath = ArtifactoryUtil.getJarFilePath(service);
						dependencyJarFilePath = ArtifactoryUtil.getDependencyJarPath(service);
						if (StringUtils.isBlank(jarFilePath)) {
							logger.info("Artifactory私服未找到依赖的jar,service:" + service);
						}
						if (CollectionUtils.isEmpty(dependencyJarFilePath)) {
							logger.info("Artifactory私服未找到依赖的jar,service:" + service);
						}
					} else {
						try {
							dependencyJarFilePath = NexusUtil.generateThirdDependencyJarUrl(service);
							jarFilePath = NexusUtil.generateDependencyJarUrl(service);
						} catch (Exception e) {// b-realease在nexus中删除了
							logger.error("nexus私服中依赖jar查找错误，尝试从Artifactory中试试", e);
							jarFilePath = ArtifactoryUtil.getJarFilePath(service);
							dependencyJarFilePath = ArtifactoryUtil.getDependencyJarPath(service);
						}
					}
					List<ServiceMethod> serviceList = ServiceUtil.generateService(dependencyJarFilePath, jarFilePath,
							service, serviceProvider.getGroup(), serviceProvider.getVersion());
					String uuid = ServiceProviderUtil.generateUUID(service, serviceList.get(0).getGroup(),
							serviceList.get(0).getServiceVersion());
					ServiceProviderUtil.serviceMethodCache(uuid, serviceList);
				} catch (ClassNotFoundException e) {
					logger.error("刷新服务方法出错,class未找到", e);
					continue;
				} catch (NoClassDefFoundError e) {
					logger.error("刷新服务方法出错,依赖的class未找到", e);
					try {
						jarFilePath = NexusUtil.generateDependencyJarUrl(service);
						if (StringUtils.isBlank(jarFilePath)) {
							jarFilePath = ArtifactoryUtil.getJarFilePath(service);
						}
						if (StringUtils.isNotBlank(jarFilePath)) {
							JarUtil.saveJarFile(jarFilePath);// 解析的jar中版本中可能没有对应的class，尝试再次加载，等待下次定时任务刷新
							List<ServiceMethod> serviceList = ServiceUtil.generateService(dependencyJarFilePath,
									jarFilePath, service, serviceProvider.getGroup(), serviceProvider.getVersion());
							String uuid = ServiceProviderUtil.generateUUID(service, serviceList.get(0).getGroup(),
									serviceList.get(0).getServiceVersion());
							ServiceProviderUtil.serviceMethodCache(uuid, serviceList);
						}
					} catch (Throwable throwable) {
						logger.error("加载依赖的class异常", throwable);
					}
					continue;
				} catch (Throwable e) {
					logger.error("刷新服务方法出错", e);
					continue;
				}
			}
		}
	}

	public void initAllService() {
		if (null != ServiceUtil.serviceMap) {
			List<Environment> environmentList = Constant.ENVIRONMENT_LIST;
			for (Environment environment : environmentList) {
				for (Entry<String, ServiceMethod> serviceEntry : ServiceUtil.serviceMap.entrySet()) {
					ServiceMethod service = serviceEntry.getValue();
					String serviceProviderUuid = ServiceProviderUtil.generateUUID(service.getServiceName(),
							service.getGroup(), service.getServiceVersion());
					ServiceProvider serviceProvider = ServiceProviderUtil.serviceProviderMap.get(serviceProviderUuid);
					if (null == serviceProvider) {
						logger.info("服务提供者地址列表为空,service：" + service.getServiceName() + ",group:" + service.getGroup()
								+ ",version:" + service.getServiceVersion());
						break;
					}
					for (String serviceProviderHost : serviceProvider.getProviders()) {
						try {
							DubboUtil.createGenericService(service.getServiceName(), service.getServiceVersion(),
									service.getGroup(), serviceProviderHost, environment.getEnv());
						} catch (Exception e) {
							e.printStackTrace();
							logger.info("初始化服务出错,service：" + service.getServiceName() + ",group:" + service.getGroup()
									+ ",version:" + service.getServiceVersion() + ",serviceProvider:"
									+ serviceProvider);
						}
						logger.info("初始化服务成功,service：" + service.getServiceName() + ",group:" + service.getGroup()
								+ ",version:" + service.getServiceVersion() + ",serviceProvider:" + serviceProvider);
					}
				}
			}
		}
	}

	// 初始化调用方法会出错,参数使用的都是默认参数,用于本地测试
	public void initInvokeAllServiceMethod() {
		if (null != ServiceUtil.serviceMap) {
			List<Environment> environmentList = Constant.ENVIRONMENT_LIST;
			for (Environment environment : environmentList) {
				for (Entry<String, ServiceMethod> serviceEntry : ServiceUtil.serviceMap.entrySet()) {
					ServiceMethod service = serviceEntry.getValue();
					String serviceProviderUuid = ServiceProviderUtil.generateUUID(service.getServiceName(),
							service.getGroup(), service.getServiceVersion());
					List<String> serviceProviderList = ServiceProviderUtil.serviceProviderMap.get(serviceProviderUuid)
							.getProviders();
					if (CollectionUtils.isEmpty(serviceProviderList)) {
						logger.info("服务提供者地址列表为空,service：" + service.getServiceName() + ",group:" + service.getGroup()
								+ ",version:" + service.getServiceVersion());
						break;
					}
					for (String serviceProvider : serviceProviderList) {
						try {
							DubboUtil.genericServiceInvoke(service.getServiceName(), service.getServiceVersion(),
									service.getGroup(), service.getMethodName(), service.getParameterType(),
									service.getParameterValue(), serviceProvider, environment.getEnv());
						} catch (Exception e) {
							logger.error("initInvokeAllServiceMethod", e);
							logger.error("初始化调用出错,service：" + service.getServiceName() + ",group:" + service.getGroup()
									+ ",version:" + service.getServiceVersion() + ",method:" + service.getMethodString()
									+ ",serviceProvider:" + serviceProvider);
						}
						logger.info("初始化调用成功,service：" + service.getServiceName() + ",group:" + service.getGroup()
								+ ",version:" + service.getServiceVersion() + ",method:" + service.getMethodString()
								+ ",serviceProvider:" + serviceProvider);
					}
				}
			}
		}
	}

	public static List<ServiceMethod> refreshServiceMethod(String service, String group, String version) {
		logger.info("刷新服务方法开始");
		String jarFilePath = "";
		List<String> dependencyJarFilePath = null;
		try {
			logger.info("刷新服务方法,service:" + service);
			NexusDependecyList nexusDependecyList = NexusUtil.searchDependencyWithClassName(service);
			if (CollectionUtils.isEmpty(nexusDependecyList.getData())) {
				logger.info("nexus私服未找到依赖的jar,service:" + service);
				jarFilePath = ArtifactoryUtil.getJarFilePath(service);
				dependencyJarFilePath = ArtifactoryUtil.getDependencyJarPath(service);
				if (StringUtils.isBlank(jarFilePath)) {
					logger.info("Artifactory私服未找到依赖的jar,service:" + service);
				}
				if (CollectionUtils.isEmpty(dependencyJarFilePath)) {
					logger.info("Artifactory私服未找到依赖的jar,service:" + service);
				}
			} else {
				try {
					dependencyJarFilePath = NexusUtil.generateThirdDependencyJarUrl(service);
					jarFilePath = NexusUtil.generateDependencyJarUrl(service);
				} catch (Exception e) {// b-realease在nexus中删除了
					logger.error("nexus私服中依赖jar查找错误，尝试从Artifactory中试试", e);
					jarFilePath = ArtifactoryUtil.getJarFilePath(service);
					dependencyJarFilePath = ArtifactoryUtil.getDependencyJarPath(service);
				}
			}
			List<ServiceMethod> serviceMethodList = ServiceUtil.generateService(dependencyJarFilePath, jarFilePath,
					service, group, version);
			String uuid = ServiceProviderUtil.generateUUID(service, serviceMethodList.get(0).getGroup(),
					serviceMethodList.get(0).getServiceVersion());
			ServiceProviderUtil.serviceMethodCache(uuid, serviceMethodList);
			compileServiceMethod(service, uuid);
			return serviceMethodList;
		} catch (ClassNotFoundException e) {
			logger.error("刷新服务方法出错,class未找到", e);
		} catch (NoClassDefFoundError e) {
			logger.error("刷新服务方法出错,依赖的class未找到", e);
			String clazz = e.getMessage().replaceAll("\\/", "\\.");
			try {
				jarFilePath = NexusUtil.generateDependencyJarUrl(clazz);
				if (StringUtils.isBlank(jarFilePath)) {
					jarFilePath = ArtifactoryUtil.getJarFilePath(clazz);
				}
				if (StringUtils.isNotBlank(jarFilePath)) {
					JarUtil.saveJarFile(jarFilePath);// 解析的jar中版本中可能没有对应的class，尝试再次加载，等待下次定时任务刷新
					List<ServiceMethod> serviceList = ServiceUtil.generateService(dependencyJarFilePath, jarFilePath,
							service, group, version);
					String uuid = ServiceProviderUtil.generateUUID(service, serviceList.get(0).getGroup(),
							serviceList.get(0).getServiceVersion());
					ServiceProviderUtil.serviceMethodCache(uuid, serviceList);
					compileServiceMethod(service, uuid);
					return serviceList;
				}
			} catch (Throwable throwable) {
				logger.error("加载依赖的class异常", throwable);
			}
		} catch (Throwable e) {
			logger.error("刷新服务方法出错", e);
		}
		logger.info("刷新服务方法结束");
		return null;
	}

	// 动态编译java代码
	public static void compileServiceMethod(String service, String uuid) {
		logger.info("动态编译java开始,service:" + service);
		if (Constant.DYNAMIC_COMPILE_SWITCH.equals(Constant.SWITCH_OFF)) {
			logger.info("编译功能关闭");
			return;
		}
		URLClassLoader uRLClassLoader = null;
		try {
			List<String> serviceList = Lists.newArrayList();
			serviceList.add(service);
			JavaCompilerUtil.compileJarClass(serviceList);
			uRLClassLoader = JavaCompilerUtil.getURLClassLoader();
			List<String> parentClassList = Lists.newArrayList();
			parentClassList = ReflectionUtil.getSuperClassName(uRLClassLoader, service);
			if (CollectionUtils.isNotEmpty(parentClassList)) {
				JavaCompilerUtil.compileJarClass(parentClassList);
				uRLClassLoader = JavaCompilerUtil.getURLClassLoader();
			}
		} catch (Exception e) {
			logger.error("动态编译java代码出错", e);
		}
		List<ServiceMethod> serviceMethodList = ServiceProviderUtil.serviceMethodMap.get(uuid);
		if (CollectionUtils.isNotEmpty(serviceMethodList)) {
			for (ServiceMethod serviceMethod : serviceMethodList) {
				try {
					String[] parameterName = JavaCompilerUtil.getClassParamaterWithAllJar(uRLClassLoader,
							serviceMethod.getServiceName(), serviceMethod.getMethodString());
					if (null != parameterName) {
						serviceMethod.setParameterName(parameterName);
					}
				} catch (Throwable e) {
					logger.error("动态编译java代码出错", e);
					continue;
				}
			}
		}
		logger.info("动态编译java结束");
	}

}
