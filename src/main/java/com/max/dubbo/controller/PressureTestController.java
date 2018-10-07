package com.max.dubbo.controller;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.locks.ReentrantLock;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.max.dubbo.constant.Constant;
import com.max.dubbo.entity.ServiceMethod;
import com.max.dubbo.util.PressureTestUtil;
import com.max.dubbo.util.ServiceUtil;

/**
 * 
 * @author githubma
 * @date 2018年5月18日 上午11:21:03
 *
 */
@RestController
public class PressureTestController {

	Map<String, String> pressureMap = new HashMap<String, String>();

	String serviceMthod = "serviceMthod";

	ReentrantLock pressureLock = new ReentrantLock();

	@RequestMapping(value = "/pressureTest", method = RequestMethod.POST)
	@ResponseBody
	public Object pressureTest(Integer threadCount, Integer threadExecuteCount, String uuid, String targetProvider,
			@RequestBody Object[] parameterValue, @CookieValue(name = "env") String env)
			throws InterruptedException, BrokenBarrierException {
		ServiceMethod service = ServiceUtil.serviceMap.get(uuid);
		if (null == service) {
			throw new RuntimeException("服务不存在");
		}
		if (Constant.PRESSURE_LOCK_SWITCH.equals(Constant.SWITCH_OFF)) {
			throw new RuntimeException("压测功能关闭");
		}
		if (pressureMap.containsKey(serviceMthod)) {
			throw new RuntimeException("当前有其他服务正在压测，请稍后尝试");
		}
		pressureLock.tryLock();
		try {
			pressureMap.put(serviceMthod, service.getMethodString());
			service.setParameterValue(parameterValue);
			return PressureTestUtil.pressureTest(threadCount, threadExecuteCount, uuid, targetProvider, parameterValue,
					env);
		} finally {
			pressureMap.remove(serviceMthod);
			pressureLock.unlock();
		}
	}

	@RequestMapping(value = "/pressureTest/enable")
	@ResponseBody
	public Object pressureTestEnable() {
		Constant.PRESSURE_LOCK_SWITCH = Constant.SWITCH_ON;
		return true;
	}

	@RequestMapping(value = "/pressureTest/disable")
	@ResponseBody
	public Object pressureTestDisable() {
		Constant.PRESSURE_LOCK_SWITCH = Constant.SWITCH_OFF;
		return true;
	}

}
