package com.max.dubbo.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.CollectionUtils;

import com.max.dubbo.constant.Constant;
import com.max.dubbo.entity.Dependency;
import com.max.dubbo.entity.NexusDependecy;
import com.max.dubbo.entity.NexusDependecyList;
import com.max.dubbo.entity.NexusDependencyData;

/**
 * 
 * @author githubma
 * @date 2018年3月5日 下午6:54:18
 *
 */
public class NexusUtil {

    static Logger logger = LoggerFactory.getLogger(NexusUtil.class);

    public static Boolean checkNexusUrl() {
        if (StringUtils.isBlank(Constant.NEXUS_URL)) {
            logger.info("没有配置nexus.url，请在application.properties文件中添加配置");
            return false;
        }
        return true;
    }

    public static NexusDependecyList searchDependencyWithClassName(String className) {
        if (!checkNexusUrl()) {
            return new NexusDependecyList();
        }
        ResponseEntity<NexusDependecyList> nexusDependecyList = RestTemplateUtil.restTemplate.getForEntity(
                Constant.NEXUS_URL + "/nexus/service/local/lucene/search?cn=" + className, NexusDependecyList.class);
        NexusDependecyList nexusDependecyListBody = nexusDependecyList.getBody();
        return nexusDependecyListBody;
    }

    public static String getRepository(NexusDependecyList nexusDependecyList) {
        List<NexusDependecy> nexusDependecyListData = nexusDependecyList.getData();
        String snapshotOrRelease = "";
        if (nexusDependecyListData.get(0).getVersion().toLowerCase().contains("snapshot")) {
            snapshotOrRelease = nexusDependecyListData.get(0).getLatestSnapshotRepositoryId();
        } else {
            snapshotOrRelease = nexusDependecyListData.get(0).getLatestReleaseRepositoryId();
        }
        return snapshotOrRelease;
    }

    public static NexusDependencyData searchDepencyJarDownloadUrl(NexusDependecyList nexusDependecyList) {
        List<NexusDependecy> nexusDependecyListData = nexusDependecyList.getData();
        String snapshotOrRelease = getRepository(nexusDependecyList);
        ResponseEntity<NexusDependencyData> nexusDependencyData = RestTemplateUtil.restTemplate
                .getForEntity(Constant.NEXUS_URL + "/nexus/service/local/artifact/maven/resolve?r=" + snapshotOrRelease
                        + "&g=" + nexusDependecyListData.get(0).getGroupId() + "&a="
                        + nexusDependecyListData.get(0).getArtifactId() + "&v="
                        + nexusDependecyListData.get(0).getVersion(), NexusDependencyData.class);
        NexusDependencyData nexusDependencyDataBody = nexusDependencyData.getBody();
        return nexusDependencyDataBody;
    }

    public static String generateDependencyJarUrl(String className) {
        String url = "";
        if (!checkNexusUrl()) {
            return url;
        }
        NexusDependecyList nexusDependecyList = searchDependencyWithClassName(className);
        List<NexusDependecy> nexusDependecyListData = nexusDependecyList.getData();
        if (CollectionUtils.isEmpty(nexusDependecyListData)) {
            return url;
        }
        String snapshotOrRelease = getRepository(nexusDependecyList);
        NexusDependencyData nexusDependencyData = searchDepencyJarDownloadUrl(nexusDependecyList);
        url = Constant.NEXUS_URL + "/nexus/service/local/repositories/";
        url += snapshotOrRelease;
        url += "/content";
        url += nexusDependencyData.getData().getRepositoryPath();
        return url;
    }

    public static NexusDependecyList searchDependencyWithGav(String groupId, String artifactId, String version) {
        if (!checkNexusUrl()) {
            return new NexusDependecyList();
        }
        ResponseEntity<NexusDependecyList> nexusSearch = RestTemplateUtil.restTemplate.getForEntity(Constant.NEXUS_URL
                + "/nexus/service/local/lucene/search?g=" + groupId + "&a=" + artifactId + "&v=" + version,
                NexusDependecyList.class);
        NexusDependecyList nexusSearchBody = nexusSearch.getBody();
        if (CollectionUtils.isEmpty(nexusSearchBody.getData())) {
            nexusSearch = RestTemplateUtil.restTemplate.getForEntity(
                    Constant.NEXUS_URL + "/nexus/service/local/lucene/search?g=" + groupId + "&a=" + artifactId,
                    NexusDependecyList.class);
            nexusSearchBody = nexusSearch.getBody();
            if (!CollectionUtils.isEmpty(nexusSearchBody.getData())) {
                List<NexusDependecy> dataList = new ArrayList<NexusDependecy>();
                dataList.add(nexusSearchBody.getData().get(0));
                NexusDependecyList nexusDependecyList = new NexusDependecyList();
                nexusDependecyList.setData(dataList);
                return nexusDependecyList;
            }
        }
        return nexusSearchBody;
    }

    public static List<String> generateThirdDependencyJarUrl(String className) throws IOException {
        List<String> urlList = new ArrayList<String>();
        if (!checkNexusUrl()) {
            return urlList;
        }
        String url = generateDependencyJarUrl(className);
        if (StringUtils.isBlank(url)) {
            return urlList;
        }
        List<Dependency> dependencyList = JarUtil.getAllDependencyFromJarFile(url);
        for (Dependency dependency : dependencyList) {
            try {
                NexusDependecyList nexusSearch = searchDependencyWithGav(dependency.getGroupId(),
                        dependency.getArtifactId(), dependency.getVersion());
                List<NexusDependecy> nexusDependecyList = nexusSearch.getData();
                if (CollectionUtils.isEmpty(nexusDependecyList)) {
                    continue;
                }
                /*
                 * String snapshotOrRelease =
                 * nexusDependecyList.get(0).getLatestSnapshotRepositoryId(); if
                 * (StringUtils.isBlank(nexusDependecyList.get(0).getLatestSnapshotRepositoryId(
                 * ))) { snapshotOrRelease =
                 * nexusDependecyList.get(0).getLatestReleaseRepositoryId(); }
                 */
                String snapshotOrRelease = getRepository(nexusSearch);
                NexusDependencyData nexusDependencyData = searchDepencyJarDownloadUrl(nexusSearch);
                String tempUrl = Constant.NEXUS_URL + "/nexus/service/local/repositories/";
                tempUrl += snapshotOrRelease;
                tempUrl += "/content";
                tempUrl += nexusDependencyData.getData().getRepositoryPath();
                urlList.add(tempUrl);
            } catch (Exception e) {// 有些jar不是强制依赖的，可以捕获之后再试试
                logger.error("jar未找到", e);
            }
        }
        return urlList;
    }

    public static NexusDependecyList searchPomWithArtifactId(String artifactId) {
        if (!checkNexusUrl()) {
            return new NexusDependecyList();
        }
        ResponseEntity<NexusDependecyList> nexusDependecyList = RestTemplateUtil.restTemplate.getForEntity(
                Constant.NEXUS_URL + "/nexus/service/local/lucene/search?q=" + artifactId, NexusDependecyList.class);
        NexusDependecyList nexusDependecyListBody = nexusDependecyList.getBody();
        return nexusDependecyListBody;
    }

    public static NexusDependencyData searchPomDownloadUrl(NexusDependecyList nexusDependecyList) {
        List<NexusDependecy> nexusDependecyListData = nexusDependecyList.getData();
        String snapshotOrRelease = getRepository(nexusDependecyList);
        ResponseEntity<NexusDependencyData> nexusDependencyData = RestTemplateUtil.restTemplate
                .getForEntity(Constant.NEXUS_URL + "/nexus/service/local/artifact/maven/resolve?e=pom&r="
                        + snapshotOrRelease + "&g=" + nexusDependecyListData.get(0).getGroupId() + "&a="
                        + nexusDependecyListData.get(0).getArtifactId() + "&v="
                        + nexusDependecyListData.get(0).getVersion(), NexusDependencyData.class);
        NexusDependencyData nexusDependencyDataBody = nexusDependencyData.getBody();
        return nexusDependencyDataBody;
    }

    public static NexusDependecyList searchPomWithGav(String groupId, String artifactId, String version) {
        ResponseEntity<NexusDependecyList> nexusSearch = RestTemplateUtil.restTemplate.getForEntity(Constant.NEXUS_URL
                + "/nexus/service/local/lucene/search?g=" + groupId + "&a=" + artifactId + "&v=" + version,
                NexusDependecyList.class);
        NexusDependecyList nexusSearchBody = nexusSearch.getBody();
        if (CollectionUtils.isEmpty(nexusSearchBody.getData())) {
            nexusSearch = RestTemplateUtil.restTemplate.getForEntity(
                    Constant.NEXUS_URL + "/nexus/service/local/lucene/search?g=" + groupId + "&a=" + artifactId,
                    NexusDependecyList.class);
            nexusSearchBody = nexusSearch.getBody();
            if (!CollectionUtils.isEmpty(nexusSearchBody.getData())) {
                List<NexusDependecy> dataList = new ArrayList<NexusDependecy>();
                dataList.add(nexusSearchBody.getData().get(0));
                NexusDependecyList nexusDependecyList = new NexusDependecyList();
                nexusDependecyList.setData(dataList);
                return nexusDependecyList;
            }
        }
        return nexusSearchBody;
    }

    public static String generatePomUrl(String artifactId) {
        String url = "";
        if (!checkNexusUrl()) {
            return url;
        }
        NexusDependecyList nexusDependecyList = searchPomWithArtifactId(artifactId);
        List<NexusDependecy> nexusDependecyListData = nexusDependecyList.getData();
        if (CollectionUtils.isEmpty(nexusDependecyListData)) {
            return url;
        }
        String snapshotOrRelease = getRepository(nexusDependecyList);
        NexusDependencyData nexusDependencyData = searchPomDownloadUrl(nexusDependecyList);
        url = Constant.NEXUS_URL + "/nexus/service/local/repositories/";
        url += snapshotOrRelease;
        url += "/content";
        url += nexusDependencyData.getData().getRepositoryPath();
        return url;
    }

    public static String generatePomUrlWithGav(String groupId, String artifactId, String version) {
        String url = "";
        if (!checkNexusUrl()) {
            return url;
        }
        NexusDependecyList nexusDependecyList = searchPomWithGav(groupId, artifactId, version);
        List<NexusDependecy> nexusDependecyListData = nexusDependecyList.getData();
        if (CollectionUtils.isEmpty(nexusDependecyListData)) {
            return url;
        }
        String snapshotOrRelease = getRepository(nexusDependecyList);
        NexusDependencyData nexusDependencyData = searchPomDownloadUrl(nexusDependecyList);
        url = Constant.NEXUS_URL + "/nexus/service/local/repositories/";
        url += snapshotOrRelease;
        url += "/content";
        url += nexusDependencyData.getData().getRepositoryPath();
        return url;
    }

}
