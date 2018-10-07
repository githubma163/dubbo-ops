package com.max.dubbo.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.max.dubbo.entity.NexusDependecyList;

/**
 * 
 * @author githubma
 * @date 2018年5月28日 下午1:34:10
 *
 */
public class JarDownloadUtil {

	static Logger logger = LoggerFactory.getLogger(JarDownloadUtil.class);

	public static void downloadJarWithClassName(String className) {
		if (StringUtils.isBlank(className)) {
			return;
		}
		String jarFilePath = "";
		List<String> dependencyJarFilePath = null;
		NexusDependecyList nexusDependecyList = NexusUtil.searchDependencyWithClassName(className);
		try {
			if (CollectionUtils.isEmpty(nexusDependecyList.getData())) {
				logger.info("nexus私服未找到依赖的jar,className:" + className);
				jarFilePath = ArtifactoryUtil.getJarFilePath(className);
				dependencyJarFilePath = ArtifactoryUtil.getDependencyJarPath(className);
				if (StringUtils.isBlank(jarFilePath)) {
					logger.info("Artifactory私服未找到依赖的jar,className:" + className);
				}
				if (CollectionUtils.isEmpty(dependencyJarFilePath)) {
					logger.info("Artifactory私服未找到依赖的jar,className:" + className);
				}
			} else {
				try {
					dependencyJarFilePath = NexusUtil.generateThirdDependencyJarUrl(className);
					jarFilePath = NexusUtil.generateDependencyJarUrl(className);
				} catch (Exception e) {
					logger.error("nexus私服中依赖jar查找错误，尝试从Artifactory中试试", e);
					jarFilePath = ArtifactoryUtil.getJarFilePath(className);
					dependencyJarFilePath = ArtifactoryUtil.getDependencyJarPath(className);
				}
			}
			JarUtil.saveJarFile(jarFilePath);
			JarUtil.saveJarSourceFile(jarFilePath);// 下载jar
			for (String path : dependencyJarFilePath) {
				JarUtil.saveJarFile(path);
			}
		} catch (Exception e) {
			logger.error("下载jar出错", e);
		}
	}

}
