package com.max.dubbo.controller;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.max.dubbo.entity.ServiceMethod;
import com.max.dubbo.util.DubboUtil;
import com.max.dubbo.util.ServiceUtil;

/**
 * 
 * @author githubma
 * @date 2018年3月5日 下午6:51:49
 *
 */
@RestController
public class MethodController {

	@RequestMapping(value = "/", method = RequestMethod.POST)
	@ResponseBody
	public Object index(@RequestBody ServiceMethod service, @CookieValue(name = "env") String env) {
		return DubboUtil.genericServiceInvoke(service.getServiceName(), service.getServiceVersion(), service.getGroup(),
				service.getMethodName(), service.getParameterType(), service.getParameterValue(),
				service.getTargetProvider(), env);
	}

	@RequestMapping(value = "/methodInfo", method = RequestMethod.POST)
	@ResponseBody
	public Object methodInfo(String uuid) {
		ServiceMethod service = ServiceUtil.serviceMap.get(uuid);
		return service;
	}

	@RequestMapping(value = "/invokeMethod", method = RequestMethod.POST)
	@ResponseBody
	public Object invokeMethod(String uuid, String targetProvider, @RequestBody Object[] parameterValue,
			@CookieValue(name = "env") String env) {
		ServiceMethod service = ServiceUtil.serviceMap.get(uuid);
		service.setParameterValue(parameterValue);
		if (!StringUtils.isBlank(targetProvider)) {
			service.setTargetProvider(targetProvider);
		}
		return DubboUtil.genericServiceInvoke(service.getServiceName(), service.getServiceVersion(), service.getGroup(),
				service.getMethodName(), service.getParameterType(), service.getParameterValue(),
				service.getTargetProvider(), env);
	}

	@SuppressWarnings("rawtypes")
	@RequestMapping(value = "/api/{methodName}/{key}", method = RequestMethod.GET)
	@ResponseBody
	public Object apiGet(@PathVariable("key") String key, HttpServletRequest request,
			@CookieValue(name = "env") String env) {
		ServiceMethod service = ServiceUtil.httpServiceMap.get(key);
		service.getParameterType();
		Object[] parameterValue = new Object[request.getParameterMap().size()];
		if (request.getParameterMap().size() > 0) {
			Enumeration params = request.getParameterNames();
			int i = 0;
			while (params.hasMoreElements()) {
				String name = (String) params.nextElement();
				String value = request.getParameter(name);
				parameterValue[i++] = value;// 参数类型转换
			}
		}
		if (request.getParameterMap().size() > 0) {
			service.setParameterValue(parameterValue);
		}
		return DubboUtil.genericServiceInvoke(service.getServiceName(), service.getServiceVersion(), service.getGroup(),
				service.getMethodName(), service.getParameterType(), service.getParameterValue(), "", env);
	}

	@RequestMapping(value = "/api/{methodName}/{key}", method = RequestMethod.POST)
	@ResponseBody
	public Object apiPost(@PathVariable("key") String key, @RequestBody Object[] parameterValue,
			@CookieValue(name = "env") String env) {
		ServiceMethod service = ServiceUtil.httpServiceMap.get(key);
		service.setParameterValue(parameterValue);
		return DubboUtil.genericServiceInvoke(service.getServiceName(), service.getServiceVersion(), service.getGroup(),
				service.getMethodName(), service.getParameterType(), service.getParameterValue(), "", env);
	}

}
