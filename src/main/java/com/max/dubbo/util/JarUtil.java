package com.max.dubbo.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import com.max.dubbo.constant.Constant;
import com.max.dubbo.entity.Dependency;

/**
 * 
 * @author githubma
 * @date 2018年3月5日 下午6:54:13
 *
 */
@Component
public class JarUtil {

	static Logger logger = LoggerFactory.getLogger(JarUtil.class);

	public static final ConcurrentMap<String, byte[]> fileMap = new ConcurrentHashMap<String, byte[]>();

	public static JarFile saveJarFile(String jarFilePath) throws IOException {
		File fileDirectory = new File(Constant.JAR_FILE_PATH);
		if (!fileDirectory.exists()) {
			fileDirectory.mkdirs();
		}
		JarFile jarFile = null;
		if (jarFilePath.startsWith("http")) {
			String fileName = FilenameUtils.getName(jarFilePath);
			File file = new File(Constant.JAR_FILE_PATH + File.separator + fileName);
			if (!file.exists()) {
				byte[] result = null;
				if (fileMap.containsKey(jarFilePath)) {
					result = fileMap.get(jarFilePath);
				} else {
					HttpHeaders headers = new HttpHeaders();
					ResponseEntity<byte[]> response = RestTemplateUtil.restTemplate.exchange(jarFilePath,
							HttpMethod.GET, new HttpEntity<byte[]>(headers), byte[].class);
					result = response.getBody();
					fileMap.putIfAbsent(jarFilePath, result);
					file.createNewFile();
				}
				FileUtils.writeByteArrayToFile(file, result);
			}
			jarFile = new JarFile(file);// jar包无法直接读取，提示文件不存在
		} else {
			File file = new File(jarFilePath);
			jarFile = new JarFile(file);// jar包无法直接读取，提示文件不存在
		}
		return jarFile;
	}

	public static List<JarEntry> getAllJarEntryFromJarFile(String jarFilePath) throws IOException {
		JarFile jarFile = saveJarFile(jarFilePath);
		List<JarEntry> jarEntryList = new ArrayList<JarEntry>();
		Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
		while (jarEntryEnumeration.hasMoreElements()) {
			JarEntry entry = (JarEntry) jarEntryEnumeration.nextElement();
			jarEntryList.add(entry);
		}
		return jarEntryList;
	}

	public static List<String> getAllClassFromJarFile(String jarFilePath) throws IOException {
		List<String> classNameList = new ArrayList<String>();
		List<JarEntry> jarEntryList = new ArrayList<JarEntry>();
		jarEntryList = getAllJarEntryFromJarFile(jarFilePath);
		for (JarEntry entry : jarEntryList) {
			if (entry.getName().endsWith(".class")) {
				String className = entry.getName().replace('/', '.');
				className = className.substring(0, className.length() - 6);
				classNameList.add(className);
			}
		}
		return classNameList;
	}

	public static void copyJavaFileFromJarFile(String jarFilePath, List<String> serviceList) throws IOException {
		JarFile jarFile = saveJarFile(jarFilePath);
		File javaFileDirectory = new File(Constant.JAVA_FILE_PATH);
		if (!javaFileDirectory.exists()) {
			javaFileDirectory.mkdirs();
		}
		Enumeration<JarEntry> jarEntryEnumeration = jarFile.entries();
		while (jarEntryEnumeration.hasMoreElements()) {
			JarEntry entry = (JarEntry) jarEntryEnumeration.nextElement();
			if (entry.getName().endsWith(".java")) {
				String className = entry.getName().replace('/', '.');
				className = className.substring(0, className.length() - 5);
				if (serviceList.contains(className)) {
					InputStream inputStream = jarFile.getInputStream(entry);
					String filePath = Constant.JAVA_FILE_PATH + File.separator + entry.getName();
					File file = new File(filePath);
					File fileParent = file.getParentFile();
					if (!fileParent.exists()) {
						fileParent.mkdirs();
					}
					file.createNewFile();
					FileUtils.copyInputStreamToFile(inputStream, file);
				}
			}
		}
	}

	public static List<Dependency> getAllDependencyFromJarFile(String jarFilePath) throws IOException {
		JarFile jarFile = saveJarFile(jarFilePath);
		List<Dependency> dependencyList = new ArrayList<Dependency>();

		List<JarEntry> jarEntryList = new ArrayList<JarEntry>();
		jarEntryList = getAllJarEntryFromJarFile(jarFilePath);
		for (JarEntry entry : jarEntryList) {
			if (entry.getName().endsWith("pom.xml")) {
				String groupId = null;
				String artifactId = null;
				String version = null;
				boolean dependenciesStart = false;
				boolean exclusionsStart = false;
				boolean dependencyStart = false;
				boolean dependencyEnd = false;
				try (Scanner scanner = new Scanner(jarFile.getInputStream(entry), "US-ASCII")) {
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine();
						if (line.contains("<dependencies>")) {
							dependenciesStart = true;
						} else if (line.contains("<dependency>")) {
							dependencyStart = true;
						} else if (line.contains("</dependency>")) {
							dependencyEnd = true;
						} else if (line.contains("<exclusions>")) {
							exclusionsStart = true;
						} else if (line.contains("<groupId>") && dependenciesStart && dependencyStart
								&& !exclusionsStart) {
							groupId = line;
						} else if (line.contains("<artifactId>") && dependenciesStart && dependencyStart
								&& !exclusionsStart) {
							artifactId = line;
						} else if (line.contains("<version>") && dependenciesStart && dependencyStart
								&& !exclusionsStart) {
							version = line;
						} else if (line.contains("</exclusions>")) {
							exclusionsStart = false;
						} else if (line.contains("</dependencies>")) {
							break;
						}
						if (groupId != null && artifactId != null && dependencyEnd) {
							if (StringUtils.isBlank(version)) {
								version = "<version></version>";
							}
							Dependency dependency = new Dependency();
							dependency.setGroupId(getXmlContent(groupId));
							dependency.setArtifactId(getXmlContent(artifactId));
							dependency.setVersion(getXmlContent(version));
							dependencyList.add(dependency);
							groupId = null;
							artifactId = null;
							version = null;
							dependencyStart = false;
							dependencyEnd = false;
						}
					}
				}
			}
		}
		Boolean versionNotFound = false;
		for (Dependency dependency : dependencyList) {
			if (StringUtils.isBlank(dependency.getVersion()) || dependency.getVersion().contains("$")) {// 有的通过${project.verison},有的通过属性
				versionNotFound = true;
				break;
			}
		}
		if (versionNotFound) {
			Dependency parentDependency = getParentPomFromJarFile(jarFilePath);
			String parentPomUrl = NexusUtil.generatePomUrlWithGav(parentDependency.getGroupId(),
					parentDependency.getArtifactId(), parentDependency.getVersion());
			Map<String, String> parentPropertiyMap = getAllPropertiesFromPom(parentPomUrl);
			List<Dependency> parentDependencyList = getAllDependencyFromPom(parentPomUrl);
			if (null != parentDependency) {
				for (Dependency dependency : dependencyList) {
					if (StringUtils.isBlank(dependency.getVersion())) {
						for (Dependency pDependency : parentDependencyList) {
							if (dependency.getGroupId().equals(pDependency.getGroupId())
									&& dependency.getArtifactId().equals(pDependency.getArtifactId())) {
								dependency.setVersion(pDependency.getVersion());
							}
						}
					}
					if (dependency.getVersion().equals("${project.version}")) {
						dependency.setVersion(parentDependency.getVersion());
					}
					if (dependency.getVersion().contains("$")) {
						String key = dependency.getVersion().replaceAll("\\$", "");
						key = key.replaceAll("\\{", "");
						key = key.replaceAll("\\}", "");
						String value = parentPropertiyMap.get(key);
						dependency.setVersion(value);
					}
				}
			}

		}
		return dependencyList;
	}

	public static Dependency getParentPomFromJarFile(String jarFilePath) throws IOException {
		JarFile jarFile = saveJarFile(jarFilePath);
		Dependency dependency = new Dependency();
		List<JarEntry> jarEntryList = new ArrayList<JarEntry>();
		jarEntryList = getAllJarEntryFromJarFile(jarFilePath);
		for (JarEntry entry : jarEntryList) {
			if (entry.getName().endsWith("pom.xml")) {
				String groupId = null;
				String artifactId = null;
				String version = null;
				boolean parentStart = false;
				try (Scanner scanner = new Scanner(jarFile.getInputStream(entry), "US-ASCII")) {
					while (scanner.hasNextLine()) {
						String line = scanner.nextLine();
						if (line.contains("<parent>")) {
							parentStart = true;
						} else if (line.contains("<groupId>") && parentStart) {
							groupId = line;
						} else if (line.contains("<artifactId>") && parentStart) {
							artifactId = line;
						} else if (line.contains("<version>") && parentStart) {
							version = line;
						}
						if (groupId != null && artifactId != null && version != null && parentStart) {
							dependency.setGroupId(getXmlContent(groupId));
							dependency.setArtifactId(getXmlContent(artifactId));
							dependency.setVersion(getXmlContent(version));
							return dependency;
						}
					}
				}
			}
		}
		return dependency;
	}

	@SuppressWarnings("deprecation")
	public static List<Dependency> getAllDependencyFromPom(String pomFilePath) {
		List<Dependency> dependencyList = new ArrayList<Dependency>();
		if (StringUtils.isBlank(pomFilePath)) {// 有些第三方的pom是找不到的，URI is not absolute
			return dependencyList;
		}
		HttpHeaders headers = new HttpHeaders();
		ResponseEntity<String> response = RestTemplateUtil.restTemplate.exchange(pomFilePath, HttpMethod.GET,
				new HttpEntity<String>(headers), String.class);
		String content = response.getBody();
		InputStream inputStream = IOUtils.toInputStream(content);

		String groupId = null;
		String artifactId = null;
		String version = null;
		boolean dependenciesStart = false;
		boolean exclusionsStart = false;
		try (Scanner scanner = new Scanner(inputStream, "US-ASCII")) {
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.contains("<dependencies>")) {
					dependenciesStart = true;
				} else if (line.contains("<exclusions>")) {
					exclusionsStart = true;
				} else if (line.contains("<groupId>") && dependenciesStart && !exclusionsStart) {
					groupId = line;
				} else if (line.contains("<artifactId>") && dependenciesStart && !exclusionsStart) {
					artifactId = line;
				} else if (line.contains("<version>") && dependenciesStart && !exclusionsStart) {
					version = line;
				} else if (line.contains("</exclusions>")) {
					exclusionsStart = false;
				} else if (line.contains("</dependencies>")) {
					break;
				}
				if (groupId != null && artifactId != null && version != null) {
					Dependency dependency = new Dependency();
					dependency.setGroupId(getXmlContent(groupId));
					dependency.setArtifactId(getXmlContent(artifactId));
					dependency.setVersion(getXmlContent(version));
					dependencyList.add(dependency);
					groupId = null;
					artifactId = null;
					version = null;
				}
			}
		}
		return dependencyList;
	}

	@SuppressWarnings("deprecation")
	public static Map<String, String> getAllPropertiesFromPom(String pomFilePath) {
		Map<String, String> propertieMap = new HashMap<String, String>();
		if (StringUtils.isBlank(pomFilePath)) {// 有些第三方的pom是找不到的
			return propertieMap;
		}
		HttpHeaders headers = new HttpHeaders();
		ResponseEntity<String> response = RestTemplateUtil.restTemplate.exchange(pomFilePath, HttpMethod.GET,
				new HttpEntity<String>(headers), String.class);
		String content = response.getBody();
		InputStream inputStream = IOUtils.toInputStream(content);
		try (Scanner scanner = new Scanner(inputStream, "US-ASCII")) {
			Boolean propertiesstart = false;
			while (scanner.hasNextLine()) {
				String line = scanner.nextLine();
				if (line.contains("<properties>")) {
					propertiesstart = true;
				}
				if (line.contains("<!--")) {
					continue;
				}
				if (StringUtils.isBlank(line.trim())) {
					continue;
				}
				if (propertiesstart) {
					String key = getXmlLabel(line.trim());
					String value = getXmlContent(line);
					propertieMap.put(key, value);
				}
				if (line.contains("</properties>")) {
					break;
				}
			}
		}
		return propertieMap;
	}

	public static String getXmlContent(String xml) {
		String result = "";
		Pattern pattern = Pattern.compile(">([^</]+)</");// 正则表达式 commend by danielinbiti
		Matcher matcher = pattern.matcher(xml);//
		while (matcher.find()) {
			result = matcher.group(1);
		}
		return result;
	}

	public static String getXmlLabel(String xml) {
		int start = xml.indexOf("<");
		int end = xml.indexOf(">");
		String label = xml.substring(start + 1, end);
		return label;
	}

	public static String generateNexusPath(Dependency dependency) {
		String url = Constant.NEXUS_URL + "/nexus/service/local/repositories/Central/content/";
		url += dependency.getGroupId().replaceAll("\\.", "\\/");
		url += "/";
		url += dependency.getArtifactId();
		url += "/";
		url += dependency.getVersion();
		url += "/";
		url += dependency.getArtifactId() + "-" + dependency.getVersion() + ".jar";
		System.out.println(url);
		return url;
	}

	public static void saveJarSourceFile(String jarFilePath) throws IOException {
		if (StringUtils.isBlank(jarFilePath)) {
			return;
		}
		if (!jarFilePath.startsWith("http")) {
			return;
		}
		File fileDirectory = new File(Constant.JAR_SOURCE_FILE_PATH);
		if (!fileDirectory.exists()) {
			fileDirectory.mkdirs();
		}
		String jarSourceFilePath = jarFilePath.substring(0, jarFilePath.length() - 4);// 去除.jar
		try {
			jarSourceFilePath += "-sources.jar";
			String fileName = FilenameUtils.getName(jarSourceFilePath);
			File file = new File(Constant.JAR_SOURCE_FILE_PATH + File.separator + fileName);
			if (!file.exists()) {
				byte[] result = null;
				if (fileMap.containsKey(jarSourceFilePath)) {
					return;
				} else {
					HttpHeaders headers = new HttpHeaders();
					ResponseEntity<byte[]> response = RestTemplateUtil.restTemplate.exchange(jarSourceFilePath,
							HttpMethod.GET, new HttpEntity<byte[]>(headers), byte[].class);
					result = response.getBody();
					fileMap.putIfAbsent(jarSourceFilePath, result);
					file.createNewFile();
				}
				FileUtils.writeByteArrayToFile(file, result);
			}
		} catch (Exception e) {
			logger.error("下载源码异常,jarSourceFilePath:" + jarSourceFilePath, e);
		}
	}

}
