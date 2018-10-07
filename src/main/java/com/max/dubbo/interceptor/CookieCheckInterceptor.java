package com.max.dubbo.interceptor;

import java.util.List;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.max.dubbo.constant.Constant;
import com.max.dubbo.entity.Environment;

/**
 * 
 * @author githubma
 * @date 2018年4月3日 下午4:36:12
 *
 */
public class CookieCheckInterceptor implements HandlerInterceptor {

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
		Cookie[] cookies = request.getCookies();
		if (ArrayUtils.isEmpty(cookies)) {
			response.setStatus(302);
			return true;
		}
		String env = "";
		for (Cookie cookie : cookies) {
			if (cookie.getName().equals(Constant.ENV_COOKIE)) {
				env = cookie.getValue();
			}
		}
		if (StringUtils.isBlank(env)) {
			response.setStatus(302);
			return false;
		}
		List<Environment> environmentList = Constant.ENVIRONMENT_LIST;
		for (Environment environment : environmentList) {
			if (environment.getEnv().equals(env)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub
	}

}
