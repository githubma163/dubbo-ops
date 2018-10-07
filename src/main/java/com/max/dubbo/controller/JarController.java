package com.max.dubbo.controller;

import java.io.IOException;
import java.util.List;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.max.dubbo.entity.Dependency;
import com.max.dubbo.util.JarUtil;

/**
 * 
 * @author githubma
 * @date 2018年3月5日 下午6:51:43  
 *
 */
@RestController
public class JarController {

	@RequestMapping(value = "/getAllClassFromJarFile")
	public List<String> getAllClassFromJarFile(String jarFilePath) throws IOException {
		return JarUtil.getAllClassFromJarFile(jarFilePath);
	}

	@RequestMapping(value = "/getAllDependencyFromJarFile")
	public List<Dependency> getAllDependencyFromJarFile(String jarFilePath) throws IOException {
		return JarUtil.getAllDependencyFromJarFile(jarFilePath);
	}

}
