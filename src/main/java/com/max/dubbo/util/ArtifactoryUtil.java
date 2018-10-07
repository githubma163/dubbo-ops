package com.max.dubbo.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import com.google.common.collect.Lists;
import com.max.dubbo.constant.Constant;
import com.max.dubbo.entity.ArtifactoryClassEntity;
import com.max.dubbo.entity.ArtifactoryClassRequest;
import com.max.dubbo.entity.ArtifactoryClassResult;
import com.max.dubbo.entity.ArtifactoryGavcEntity;
import com.max.dubbo.entity.ArtifactoryGavcRequest;
import com.max.dubbo.entity.ArtifactoryGavcResult;
import com.max.dubbo.entity.Dependency;

/**
 * 
 * @author githubma
 * @date 2018年3月16日 上午10:16:13
 *
 */
public class ArtifactoryUtil {

	static Logger logger = LoggerFactory.getLogger(ArtifactoryUtil.class);

	public static Boolean checkArtifactoryUrl() {
		if (StringUtils.isBlank(Constant.ARTIFACTORY_URL)) {
			logger.info("没有配置artifactory.url，请在application.properties文件中添加配置");
			return false;
		}
		return true;
	}

	/**
	 * 按类名搜索jar包
	 * 
	 * @param className
	 * @return
	 */
	public static ArtifactoryClassEntity searchDependencyWithClassName(String className) {
		if (!checkArtifactoryUrl()) {
			return null;
		}
		String[] classNameSplitArray = className.split("\\.");
		String shortClassName = classNameSplitArray[classNameSplitArray.length - 1];
		String replaceClassName = className.replaceAll("\\.", "\\/");
		replaceClassName += ".class";
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ArtifactoryClassRequest artifactoryClassRequest = new ArtifactoryClassRequest();
		artifactoryClassRequest.setName(shortClassName);
		HttpEntity<ArtifactoryClassRequest> httpEntity = new HttpEntity<ArtifactoryClassRequest>(
				artifactoryClassRequest, headers);
		ResponseEntity<ArtifactoryClassResult> nexusDependecyList = RestTemplateUtil.restTemplate.exchange(
				Constant.ARTIFACTORY_URL + "/artifactory/ui/artifactsearch/class", HttpMethod.POST, httpEntity,
				ArtifactoryClassResult.class);
		ArtifactoryClassResult artifactoryClassBody = nexusDependecyList.getBody();
		List<ArtifactoryClassEntity> results = artifactoryClassBody.getResults();
		List<ArtifactoryClassEntity> stashResults = Lists.newArrayList();
		for (ArtifactoryClassEntity artifactoryClassEntity : results) {
			if (replaceClassName.equals(artifactoryClassEntity.getName())) {
				stashResults.add(artifactoryClassEntity);
			}
		}
		if (stashResults.size() == 0) {
			return null;
		}
		if (stashResults.size() == 1) {
			return stashResults.get(0);
		}
		Collections.sort(stashResults, new ArtifactoryClassEntityComparator());
		return stashResults.get(0);
	}

	/**
	 * 按gav搜索jar包
	 * 
	 * @param className
	 * @return
	 */
	public static ArtifactoryGavcEntity searchDependencyWithGav(String groupId, String artifactId, String version) {
		if (!checkArtifactoryUrl()) {
			return null;
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		ArtifactoryGavcRequest artifactoryGavcRequest = new ArtifactoryGavcRequest();
		artifactoryGavcRequest.setGroupID(groupId);
		artifactoryGavcRequest.setArtifactID(artifactId);
		artifactoryGavcRequest.setVersion(version);
		HttpEntity<ArtifactoryGavcRequest> httpEntity = new HttpEntity<ArtifactoryGavcRequest>(artifactoryGavcRequest,
				headers);
		ResponseEntity<ArtifactoryGavcResult> nexusDependecyList = RestTemplateUtil.restTemplate.exchange(
				Constant.ARTIFACTORY_URL + "/artifactory/ui/artifactsearch/gavc", HttpMethod.POST, httpEntity,
				ArtifactoryGavcResult.class);
		ArtifactoryGavcResult artifactoryGavcResult = nexusDependecyList.getBody();
		List<ArtifactoryGavcEntity> results = artifactoryGavcResult.getResults();
		List<ArtifactoryGavcEntity> stashResults = Lists.newArrayList();
		for (ArtifactoryGavcEntity artifactoryGavcEntity : results) {
			if (null == artifactoryGavcEntity.getClassifier()
					&& artifactoryGavcEntity.getDownloadLink().endsWith(".jar")) {
				stashResults.add(artifactoryGavcEntity);
			}
		}
		if (stashResults.size() == 0) {
			return null;
		}
		if (stashResults.size() == 1) {
			return stashResults.get(0);
		}
		Collections.sort(stashResults, new ArtifactoryGavcEntityComparator());
		return stashResults.get(0);
	}

	static class ArtifactoryClassEntityComparator implements Comparator<ArtifactoryClassEntity> {

		@Override
		public int compare(ArtifactoryClassEntity o1, ArtifactoryClassEntity o2) {
			if (o1.getModifiedDate() > o2.getModifiedDate()) {
				return -1;
			}
			if (o1.getModifiedDate() < o2.getModifiedDate()) {
				return 1;
			}
			return 0;
		}
	}

	static class ArtifactoryGavcEntityComparator implements Comparator<ArtifactoryGavcEntity> {

		@Override
		public int compare(ArtifactoryGavcEntity o1, ArtifactoryGavcEntity o2) {
			if (o1.getModifiedDate() > o2.getModifiedDate()) {
				return -1;
			}
			if (o1.getModifiedDate() < o2.getModifiedDate()) {
				return 1;
			}
			return 0;
		}
	}

	/**
	 * 根据类名获取jar文件的地址
	 * 
	 * @param className
	 * @return
	 */
	public static String getJarFilePath(String className) {
		ArtifactoryClassEntity artifactoryClassEntity = searchDependencyWithClassName(className);
		if (null != artifactoryClassEntity) {
			return artifactoryClassEntity.getDownloadLink();
		}
		return "";
	}

	/**
	 * 
	 * @param className
	 * @return
	 * @throws IOException
	 */
	public static List<String> getDependencyJarPath(String className) throws IOException {
		List<String> urlList = new ArrayList<String>();
		ArtifactoryClassEntity artifactoryClassEntity = searchDependencyWithClassName(className);
		if (null == artifactoryClassEntity) {
			return urlList;
		}
		List<Dependency> dependencyList = JarUtil.getAllDependencyFromJarFile(artifactoryClassEntity.getDownloadLink());
		for (Dependency dependency : dependencyList) {
			try {
				ArtifactoryGavcEntity artifactoryGavcEntity = searchDependencyWithGav(dependency.getGroupId(),
						dependency.getArtifactId(), dependency.getVersion());
				if (null != artifactoryGavcEntity) {
					urlList.add(artifactoryGavcEntity.getDownloadLink());
				}
			} catch (Exception e) {
				logger.error("查询依赖jar包出错", e);
			}
		}
		return urlList;
	}

}
