package com.max.dubbo.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.max.dubbo.constant.Constant;
import com.max.dubbo.entity.Monitor;
import com.max.dubbo.entity.MonitorEnv;
import com.max.dubbo.job.GenerateSocketMonitorJobInMemeory;

/**
 * 
 * @author githubma
 * @date 2018年7月18日 下午1:46:30
 *
 */
@RestController
@RequestMapping(value = "/monitor")
public class MonitorController {

	Logger logger = LoggerFactory.getLogger(MonitorController.class);

	@Autowired
	GenerateSocketMonitorJobInMemeory generateSocketMonitorJobInMemeory;

	ConcurrentHashMap<String, List<MonitorEnv>> cache = new ConcurrentHashMap<String, List<MonitorEnv>>();

	String key = "monitor";

	@RequestMapping(value = "/list")
	public List<MonitorEnv> appList() {
//		logger.info("GenerateSocketMonitorJobInMemeory.hasChange:" + GenerateSocketMonitorJobInMemeory.hasChange);
//		logger.info("cache.size:" + cache.size());
//		if (!GenerateSocketMonitorJobInMemeory.hasChange && cache.get(key) != null) {
//			return cache.get(key);
//		}
		Map<String, Monitor> map = generateSocketMonitorJobInMemeory.map;
		List<MonitorEnv> monitorEnvVOList = Lists.newArrayList();
		Map<String, MonitorEnv> monitorEnvMap = Maps.newHashMap();
		for (Entry<String, Monitor> entry : map.entrySet()) {
			Monitor monitorVO = entry.getValue();
			if (monitorEnvMap.containsKey(monitorVO.getEnv())) {
				if (monitorVO.getType().equals(Constant.DUBBO)) {
					monitorEnvMap.get(monitorVO.getEnv()).getDubboList().add(monitorVO);
				}
				if (monitorVO.getType().equals(Constant.ZOOKEEPER)) {
					monitorEnvMap.get(monitorVO.getEnv()).getZookeeperList().add(monitorVO);
				}
			} else {
				List<Monitor> zookeeperList = Lists.newArrayList();
				List<Monitor> dubboList = Lists.newArrayList();
				MonitorEnv monitorEnvVO = new MonitorEnv();
				monitorEnvVO.setZookeeperList(zookeeperList);
				monitorEnvVO.setDubboList(dubboList);
				monitorEnvVO.setEnv(monitorVO.getEnv());
				monitorEnvVO.setName(Constant.getEnvName(monitorVO.getEnv()));
				if (monitorVO.getType().equals(Constant.DUBBO)) {
					dubboList.add(monitorVO);
				}
				if (monitorVO.getType().equals(Constant.ZOOKEEPER)) {
					zookeeperList.add(monitorVO);
				}
				monitorEnvMap.put(monitorVO.getEnv(), monitorEnvVO);
			}
		}
		for (Entry<String, MonitorEnv> entry : monitorEnvMap.entrySet()) {
			monitorEnvVOList.add(entry.getValue());
		}
		for (MonitorEnv monitorEnvVO : monitorEnvVOList) {
			int zookeeperSuccessCount = 0;
			int zookeeperFailCount = 0;
			int dubboSuccessCount = 0;
			int dubboFailCount = 0;
			List<Monitor> zookeeperList = monitorEnvVO.getZookeeperList();
			for (Monitor monitorVO : zookeeperList) {
				if (monitorVO.getStatus() == 0) {
					zookeeperFailCount++;
				}
				if (monitorVO.getStatus() == 1) {
					zookeeperSuccessCount++;
				}
			}
			List<Monitor> dubboList = monitorEnvVO.getDubboList();
			for (Monitor monitorVO : dubboList) {
				if (monitorVO.getStatus() == 0) {
					dubboFailCount++;
				}
				if (monitorVO.getStatus() == 1) {
					dubboSuccessCount++;
				}
			}
			monitorEnvVO.setDubboFailCount(dubboFailCount);
			monitorEnvVO.setDubboSuccessCount(dubboSuccessCount);
			monitorEnvVO.setZookeeperFailCount(zookeeperFailCount);
			monitorEnvVO.setZookeeperSuccessCount(zookeeperSuccessCount);
		}
		CompratorByStatus compratorByStatus = new CompratorByStatus();
		for (MonitorEnv monitorEnvVO : monitorEnvVOList) {
			Collections.sort(monitorEnvVO.getDubboList(), compratorByStatus);
			Collections.sort(monitorEnvVO.getZookeeperList(), compratorByStatus);
		}
		cache.put(key, monitorEnvVOList);
		return monitorEnvVOList;
	}

	static class CompratorByStatus implements Comparator<Monitor> {

		@Override
		public int compare(Monitor o1, Monitor o2) {
			if (o1.getStatus() > o2.getStatus()) {
				return 1;
			}
			if (o1.getStatus() < o2.getStatus()) {
				return -1;
			}
			if (o1.getName().compareTo(o2.getName()) > 0) {
				return 1;
			}
			if (o1.getName().compareTo(o2.getName()) < 0) {
				return -1;
			}
			return 0;
		}

	}

}
