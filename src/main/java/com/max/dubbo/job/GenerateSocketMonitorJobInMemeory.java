package com.max.dubbo.job;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.max.dubbo.constant.Constant;
import com.max.dubbo.entity.Environment;
import com.max.dubbo.entity.Monitor;
import com.max.dubbo.entity.ServiceProvider;
import com.max.dubbo.util.CuratorUtil;
import com.max.dubbo.util.NetWorkUtil;
import com.max.dubbo.util.ServiceProviderUtil;

/**
 * 
 * @author githubma
 * @date 2018年6月20日 下午2:22:49
 *
 */
@Component
public class GenerateSocketMonitorJobInMemeory {

	Logger logger = LoggerFactory.getLogger(GenerateSocketMonitorJobInMemeory.class);

	public Map<String, Monitor> map = new HashMap<String, Monitor>();

	public static volatile boolean hasChange = false;// 值是否有变化

	@Scheduled(cron = "0 * * * * *")
	public void monitor() {
		logger.info("monitor监控开始");
		hasChange = false;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");// 方便前端显示
		Date date = new Date();
		boolean monitorHasChange = false;
		for (Entry<String, Monitor> entry : map.entrySet()) {
			Monitor monitor = entry.getValue();
			boolean result = NetWorkUtil.telnet(monitor.getIp(), Integer.parseInt(monitor.getPort()));
			if (result == true) {
				if (monitor.getStatus() == 1) {
					continue;
				} else {
					monitorHasChange = true;
					monitor.setStatus(1);
					monitor.setTime(sdf.format(date));
					entry.setValue(monitor);
				}
			} else {
				if (monitor.getStatus() == 0) {
					continue;
				} else {
					monitorHasChange = true;
					monitor.setStatus(1);
					monitor.setTime(sdf.format(date));
					entry.setValue(monitor);
				}
			}
			if (monitorHasChange) {
				hasChange = true;
			} else {
				hasChange = false;
			}
		}
		logger.info("monitor监控结束");
	}

	@Scheduled(cron = "0 * * * * *")
	public void generate() {
		logger.info("monitor扫描开始");
		hasChange = false;
		List<Environment> environmentList = Constant.ENVIRONMENT_LIST;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd-hh:mm:ss");
		Date date = new Date();
		boolean appHasChange = false;
		for (Environment environment : environmentList) {
			String[] addressArray = environment.getAddress().split(",");
			for (String address : addressArray) {
				if (map.containsKey(address)) {
					continue;
				}
				appHasChange = true;
				String[] ipAndPort = address.split(":");
				Monitor monitor = new Monitor();
				monitor.setName("zookeeper-" + environment.getEnv());
				monitor.setEnv(environment.getEnv());
				monitor.setTime(sdf.format(date));
				monitor.setIp(ipAndPort[0]);
				monitor.setPort(ipAndPort[1]);
				monitor.setStatus(0);
				monitor.setType(Constant.ZOOKEEPER);
				map.put(address, monitor);
			}
		}
		try {
			for (Environment environment : environmentList) {
				logger.info("环境" + environment.getEnv());
				List<String> serviceNameList;
				serviceNameList = CuratorUtil.getAllDubboServiceWithProvider(environment.getEnv());
				logger.info("刷新服务总数:" + serviceNameList.size());
				for (String serviceName : serviceNameList) {
					List<String> serviceProviders = CuratorUtil.getDubboServiceProvider(serviceName,
							environment.getEnv());
					for (String content : serviceProviders) {
						ServiceProvider serviceProvider = ServiceProviderUtil.convert(content);
						if (map.containsKey(serviceProvider.getAuthority())) {
							continue;
						}
						appHasChange = true;
						String[] ipAndPort = serviceProvider.getAuthority().split(":");
						Monitor monitor = new Monitor();
						monitor.setName(serviceProvider.getApp());
						monitor.setEnv(environment.getEnv());
						monitor.setTime(sdf.format(date));
						monitor.setIp(ipAndPort[0]);
						monitor.setPort(ipAndPort[1]);
						monitor.setStatus(0);
						monitor.setType(Constant.DUBBO);
						map.put(serviceProvider.getAuthority(), monitor);
					}
				}
				if (appHasChange) {
					hasChange = true;
				} else {
					hasChange = false;
				}
			}
		} catch (Exception e) {
			logger.error("扫描服务异常", e);
		}
		logger.info("monitor扫描结束");
	}
}
