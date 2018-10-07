package com.max.dubbo.util;

import java.lang.reflect.AnnotatedType;
import java.net.URLClassLoader;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;

/**
 * 
 * @author githubma
 * @date 2018年6月5日 下午5:13:14
 *
 */
public class ReflectionUtil {

	static Logger logger = LoggerFactory.getLogger(ReflectionUtil.class);

	// 获取接口的父类，暂时只往上一层
	@SuppressWarnings("rawtypes")
	public static List<String> getSuperClassName(URLClassLoader uRLClassLoader, String className) {
		List<String> list = Lists.newArrayList();
		Class clazz;
		try {
			clazz = uRLClassLoader.loadClass(className);
			if (!clazz.isInterface()) {
				return list;
			}
			AnnotatedType[] annotatedTypeArray = clazz.getAnnotatedInterfaces();
			for (AnnotatedType annotatedType : annotatedTypeArray) {
				String typeName = annotatedType.getType().getTypeName();
				if (typeName.contains(">")) {// 解析范型
					typeName = typeName.replaceAll(">", "");
				}
				if (typeName.contains("<")) {
					typeName = typeName.replaceAll("<", ",");
				}
				String[] typeNameArray = typeName.split(",");
				for (String type : typeNameArray) {
					list.add(type);
				}
			}
		} catch (Exception e) {
			logger.error("加载class异常" + className, e);
		}
		return list;
	}

}
