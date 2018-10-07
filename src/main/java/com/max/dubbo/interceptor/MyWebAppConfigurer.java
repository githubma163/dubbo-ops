package com.max.dubbo.interceptor;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

/**
 * 
 * @author githubma
 * @date 2018年4月3日 下午4:36:31
 *
 */
@Configuration
public class MyWebAppConfigurer extends WebMvcConfigurerAdapter {

	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new CookieCheckInterceptor()).addPathPatterns("/**").excludePathPatterns("/", "/env/**",
				"/monitor/**", "/thread/**", "/monitor.html", "/jstack.html");
		super.addInterceptors(registry);
	}

}
