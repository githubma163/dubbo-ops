package com.max.dubbo.util;

import java.util.concurrent.TimeUnit;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * 
 * @author githubma
 * @date 2018年4月3日 下午4:39:50
 *
 */
@Component
public class RestTemplateUtil {

	static RestTemplate restTemplate;

	static {
		// 长连接保持1小时
		PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager(1,
				TimeUnit.DAYS);
		// 总连接数
		pollingConnectionManager.setMaxTotal(100);
		// 同路由的并发数
		pollingConnectionManager.setDefaultMaxPerRoute(100);
		HttpClientBuilder httpClientBuilder = HttpClients.custom();
		httpClientBuilder.setConnectionManager(pollingConnectionManager);
		// 重试次数，默认是3次，没有开启
		httpClientBuilder.setRetryHandler(new DefaultHttpRequestRetryHandler(2, true));
		// 保持长连接配置，需要在头添加Keep-Alive
		httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
		HttpClient httpClient = httpClientBuilder.build();
		HttpComponentsClientHttpRequestFactory clientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory(
				httpClient);
		restTemplate = new RestTemplate(clientHttpRequestFactory);
	}

}
