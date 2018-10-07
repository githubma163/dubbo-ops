package com.max.dubbo.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.net.URLStreamHandler;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.dubbo.common.utils.CollectionUtils;
import com.max.dubbo.constant.Constant;

/**
 * 
 * @author githubma
 * @date 2018年4月9日 下午4:06:47
 *
 */
public class JavaCompilerUtil {

	static Logger logger = LoggerFactory.getLogger(JavaCompilerUtil.class);

	// 编译jar文件
	public static void compileJarClass(List<String> serviceList) throws IOException {
		// 当前编译器
		JavaCompiler javaCompiler = ToolProvider.getSystemJavaCompiler();
		// Java 标准文件管理器
		StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(null, null, null);
		File jarFileDirectory = new File(Constant.JAR_FILE_PATH);
		File jarSourceFileDirectory = new File(Constant.JAR_SOURCE_FILE_PATH);
		File javaFileDirectory = new File(Constant.JAVA_FILE_PATH);
		File javaClassFileDirectory = new File(Constant.JAVA_CLASS_FILE_PATH);
		if (!jarFileDirectory.exists()) {
			jarFileDirectory.mkdirs();
		}
		if (!jarSourceFileDirectory.exists()) {
			jarSourceFileDirectory.mkdirs();
		}
		if (!javaFileDirectory.exists()) {
			javaFileDirectory.mkdirs();
		}
		if (!javaClassFileDirectory.exists()) {
			javaClassFileDirectory.mkdirs();
		}
		String[] jarSourceFileExtensions = new String[1];
		jarSourceFileExtensions[0] = "jar";
		Collection<File> jarFileList = FileUtils.listFiles(jarFileDirectory, jarSourceFileExtensions, false);
		jarFileList = removeCompileExcludeJar(jarFileList);
		String classPathFile = "";
		for (File file : jarFileList) {
			classPathFile += Constant.JAR_FILE_PATH + File.separator + file.getName() + File.pathSeparator;
		}
		Collection<File> jarSourceFileList = FileUtils.listFiles(jarSourceFileDirectory, jarSourceFileExtensions,
				false);
		jarSourceFileList = removeCompileExcludeJar(jarSourceFileList);
		for (File file : jarSourceFileList) {
			JarUtil.copyJavaFileFromJarFile(file.getPath(), serviceList);
		}
		File directory = new File(Constant.JAVA_FILE_PATH);
		String[] extensions = new String[1];
		extensions[0] = "java";
		Collection<File> javaFileList = FileUtils.listFiles(directory, extensions, true);
		List<String> optionsList = new ArrayList<String>();
		// 编译文件的存放地方
		Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(javaFileList);
		optionsList.addAll(Arrays.asList("-cp", classPathFile, "-d", Constant.JAVA_CLASS_FILE_PATH, "-parameters"));
		// -cp编译时加载的jar文件 -d 指定编译文件目录 -parameters 生成方法参数名称
		DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
		CompilationTask compilationTask = javaCompiler.getTask(null, fileManager, diagnostics, optionsList, null,
				compilationUnits);
		// 运行编译任务
		compilationTask.call();
		List<Diagnostic<? extends JavaFileObject>> diagnosticsList = diagnostics.getDiagnostics();
		if (CollectionUtils.isNotEmpty(diagnosticsList)) {
			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosticsList) {
				logger.error("动态编译出错，错误编码:" + diagnostic.getCode() + ",错误信息:" + diagnostic.getMessage(null));
				if (diagnostic.getKind().equals(Diagnostic.Kind.ERROR)) {
					String message = diagnostic.getMessage(null);
					if (message.contains("不存在")) {
						String packageName = RegUtil.extractPackageName(message);
						JarDownloadUtil.downloadJarWithClassName(packageName);
					}
				}
			}
		}
		return;
	}

	@SuppressWarnings("rawtypes")
	public static Collection<File> removeCompileExcludeJar(Collection<File> jarFileList) {
		if (CollectionUtils.isEmpty(jarFileList)) {
			return jarFileList;
		}
		String compileExcludeJar = Constant.COMPILE_EXCLUDE_JAR;
		if (StringUtils.isBlank(compileExcludeJar)) {
			return jarFileList;
		}
		String[] compileExcludeJarArray = compileExcludeJar.split(",");
		for (Iterator iterator = jarFileList.iterator(); iterator.hasNext();) {
			File file = (File) iterator.next();
			String fileName = file.getName();
			for (String exclude : compileExcludeJarArray) {
				if (fileName.contains(exclude))
					iterator.remove();
			}
		}
		return jarFileList;
	}

	// 先加载源码编译后的class文件目录,会出现ClassNotFoudException，需要加载依赖的jar
	@Deprecated
	@SuppressWarnings({ "resource", "rawtypes" })
	public static String[] getClassParameter(String className, Integer hashCode) throws Exception {
		String[] parameterName = null;
		String path = Constant.JAVA_CLASS_FILE_PATH;
		File classpath = new File(path);
		URL[] urls = new URL[1];
		URLClassLoader loader = null;
		String repository = (new URL("file", null, classpath.getCanonicalPath() + File.separator)).toString();
		URLStreamHandler streamHandler = null;
		urls[0] = new URL(null, repository, streamHandler);
		loader = new URLClassLoader(urls);
		Class clazz = null;
		clazz = loader.loadClass(className);
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			System.out.println(method.toGenericString());
			System.out.println(method.hashCode());
			if (hashCode == method.hashCode()) {
				Parameter[] parameters = method.getParameters();
				parameterName = new String[parameters.length];
				int i = 0;
				for (Parameter parameter : parameters) {
					parameterName[i++] = parameter.getName();
				}
			}
		}
		return parameterName;
	}

	@SuppressWarnings("rawtypes")
	public static String[] getClassParamaterWithAllJar(URLClassLoader urlClassLoader, String className,
			String methodSignature) throws Exception {
		String[] parameterName = null;
		Class clazz = null;
		clazz = urlClassLoader.loadClass(className);
		Method[] methods = clazz.getMethods();
		for (Method method : methods) {
			if (methodSignature.equals(method.toString())) {// 只能使用toString比较，不能使用toGenericString比较，因为可能会出现异常，不能使用hashcode比较，hashcode比较，方法重名的话无法比较
				Parameter[] parameters = method.getParameters();
				parameterName = new String[parameters.length];
				int j = 0;
				for (Parameter parameter : parameters) {
					parameterName[j++] = parameter.getName();
				}
			}
		}
		return parameterName;
	}

	// 获取URLClassLoader
	public static URLClassLoader getURLClassLoader() throws MalformedURLException, IOException {
		File jarFileDirectory = new File(Constant.JAR_FILE_PATH);
		String[] jarSourceFileExtensions = new String[1];
		jarSourceFileExtensions[0] = "jar";
		Collection<File> jarFileList = FileUtils.listFiles(jarFileDirectory, jarSourceFileExtensions, false);
		URL[] fileUrlArray = new URL[jarFileList.size() + 1];
		URLClassLoader urlClassLoader = null;
		ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
		String path = Constant.JAVA_CLASS_FILE_PATH;
		File classpath = new File(path);
		String repository = (new URL("file", null, classpath.getCanonicalPath() + File.separator)).toString();
		URLStreamHandler streamHandler = null;
		URL sourceFileUrl = new URL(null, repository, streamHandler);
		fileUrlArray[0] = sourceFileUrl;// 先加载源码编译后的class文件目录，后加载其他jar，包含依赖的jar，URLClassLoader先放入的class路径先加载，后续再加载不会覆盖
		int i = 1;
		for (File file : jarFileList) {
			fileUrlArray[i++] = file.toURI().toURL();
		}
		urlClassLoader = new URLClassLoader(fileUrlArray, contextClassLoader);
		return urlClassLoader;
	}

}
