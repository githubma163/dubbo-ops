package com.max.dubbo.util;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;

import com.max.dubbo.entity.NexusDependecy;
import com.max.dubbo.entity.NexusDependecyList;

/**
 * 
 * @author githubma
 * @date 2018年6月12日 下午2:26:04
 *
 */
public class PomUtil {

	public static String generatePom(String className) {
		StringBuilder sb = new StringBuilder();
		NexusDependecyList nexusDependecyList = NexusUtil.searchDependencyWithClassName(className);
		if (null != nexusDependecyList && !CollectionUtils.isEmpty(nexusDependecyList.getData())) {
			List<NexusDependecy> data = nexusDependecyList.getData();
			for (NexusDependecy nexusDependecy : data) {
				String pomXml = generatePomXml(nexusDependecy.getGroupId(), nexusDependecy.getArtifactId(),
						nexusDependecy.getVersion());
				sb.append(pomXml);
			}
		} else {
			// artifactory的暂时不支持
		}
		return sb.toString();
	}

	public static String generatePomXml(String groupId, String artifactId, String version) {
		StringBuilder sb = new StringBuilder();
		sb.append("<dependency>");
		sb.append("\n");
		if (StringUtils.isNotBlank(groupId)) {
			sb.append("<groupId>" + groupId + "</groupId>");
			sb.append("\n");
		}
		if (StringUtils.isNotBlank(artifactId)) {
			sb.append("<artifactId>" + artifactId + "</artifactId>");
			sb.append("\n");
		}
		if (StringUtils.isNotBlank(version)) {
			sb.append("<version>" + version + "</version>");
			sb.append("\n");
		}
		sb.append("</dependency>");
		sb.append("\n");
		sb.append("\n");
		return sb.toString();
	}

}
