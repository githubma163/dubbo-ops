package com.max.dubbo.controller;

import java.io.File;
import java.io.IOException;

import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.max.dubbo.constant.Constant;
import com.max.dubbo.util.ServiceProviderUtil;
import com.max.dubbo.util.ServiceUtil;

/**
 * 
 * @author githubma
 * @date 2018年5月16日 下午4:13:13
 *
 */
@RestController
public class CacheController {

	Logger logger = LoggerFactory.getLogger(CacheController.class);

	@RequestMapping(value = "/cleanCache")
	public Boolean cleanCache() {
		ServiceUtil.httpServiceMap.clear();
		ServiceUtil.serviceMap.clear();
		ServiceProviderUtil.serviceMethodMap.clear();
		ServiceProviderUtil.serviceProviderMap.clear();
		return true;
	}

	@RequestMapping(value = "/cleanDirectory")
	public Boolean cleanDirectory() {
		File jarFileDirectory = new File(Constant.JAR_FILE_PATH);
		File jarSourceFileDirectory = new File(Constant.JAR_SOURCE_FILE_PATH);
		File jarClassFileDirectory = new File(Constant.JAVA_CLASS_FILE_PATH);
		File javaSourceFileDirectory = new File(Constant.JAVA_FILE_PATH);
		try {
			FileUtils.deleteDirectory(jarFileDirectory);
			FileUtils.deleteDirectory(jarSourceFileDirectory);
			FileUtils.deleteDirectory(jarClassFileDirectory);
			FileUtils.deleteDirectory(javaSourceFileDirectory);
		} catch (IOException e) {
			logger.error("文件夹删除异常", e);
		}
		return true;
	}

}
