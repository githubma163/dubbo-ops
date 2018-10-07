package com.max.dubbo.constant;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.max.dubbo.entity.Environment;

/**
 * 
 * @author githubma
 * @date 2018年3月5日 下午6:52:02
 *
 */
@Component
public class Constant {

	static Logger logger = LoggerFactory.getLogger(Constant.class);

	public static String DEFAULT_PATH = "/dubbo";

	public static String DEFAULT_PROVIDER_PATH = "/providers";

	public static String DEFAULT_CONSUMER_PATH = "/consumers";

	public static List<Environment> ENVIRONMENT_LIST;

	public static String ENV_COOKIE = "env";

	public static String NEXUS_URL;

	public static String ARTIFACTORY_URL;

	public static String JAR_FILE_PATH;// 存放私服中下载jar文件

	public static String JAR_SOURCE_FILE_PATH;// 存放jar源码文件

	public static String JAVA_FILE_PATH;// 存放jar中的java文件

	public static String JAVA_CLASS_FILE_PATH;// 存放jar中的java文件编译后的class文件

	public static String PRESSURE_LOCK_SWITCH;// 压测开关

	public static String DYNAMIC_COMPILE_SWITCH;// 动态编译开关

	public static String SWITCH_ON = "on";

	public static String SWITCH_OFF = "off";

	public static String COMPILE_EXCLUDE_JAR;// 动态编译时不需要编辑的jar,像lombok这种,多个用逗号隔开

	public static final String DUBBO = "dubbo";

	public static final String ZOOKEEPER = "zookeeper";

	public static String SCAN_PACKAGE;

	public static String EXCLUDE_PACKAGE;

	static {
		Properties properties = new Properties();
		InputStream inputStream = Constant.class.getClassLoader().getResourceAsStream("application.properties");
		try {
			properties.load(inputStream);
		} catch (IOException e) {
			logger.error("配置文件解析错误", e);
		}
		ENVIRONMENT_LIST = JSON.parseArray(properties.getProperty("env"), Environment.class);
	}

	@Value("${nexus.url}")
	public void setNEXUS_URL(String nEXUS_URL) {
		NEXUS_URL = nEXUS_URL;
	}

	@Value("${artifactory.url}")
	public void setARTIFACTORY_URL(String aRTIFACTORY_URL) {
		ARTIFACTORY_URL = aRTIFACTORY_URL;
	}

	@Value("${jar.path}")
	public void setJAR_FILE_PATH(String jAR_FILE_PATH) {
		JAR_FILE_PATH = jAR_FILE_PATH;
	}

	@Value("${jar.source.path}")
	public void setJAR_SOURCE_FILE_PATH(String jAR_SOURCE_FILE_PATH) {
		JAR_SOURCE_FILE_PATH = jAR_SOURCE_FILE_PATH;
	}

	@Value("${java.source.path}")
	public void setJAVA_FILE_PATH(String jAVA_FILE_PATH) {
		JAVA_FILE_PATH = jAVA_FILE_PATH;
	}

	@Value("${java.class.class}")
	public void setJAVA_CLASS_FILE_PATH(String jAVA_CLASS_FILE_PATH) {
		JAVA_CLASS_FILE_PATH = jAVA_CLASS_FILE_PATH;
	}

	@Value("${pressure.lock.switch}")
	public void setPRESSURE_LOCK_SWITCH(String pRESSURE_LOCK_SWITCH) {
		PRESSURE_LOCK_SWITCH = pRESSURE_LOCK_SWITCH;
	}

	@Value("${dynamic.compile.switch}")
	public void setDYNAMIC_COMPILE_SWITCH(String dYNAMIC_COMPILE_SWITCH) {
		DYNAMIC_COMPILE_SWITCH = dYNAMIC_COMPILE_SWITCH;
	}

	@Value("${compile.exclude.jar}")
	public void setCOMPILE_EXCLUDE_JAR(String cOMPILE_EXCLUDE_JAR) {
		COMPILE_EXCLUDE_JAR = cOMPILE_EXCLUDE_JAR;
	}

	public static String getZookeeperUrl(String env) {
		List<Environment> environmentList = ENVIRONMENT_LIST;
		for (Environment environment : environmentList) {
			if (environment.getEnv().equals(env)) {
				return environment.getAddress();
			}
		}
		throw new RuntimeException("请选择环境");
	}

	public static String getEnvName(String env) {
		List<Environment> environmentList = ENVIRONMENT_LIST;
		for (Environment environment : environmentList) {
			if (environment.getEnv().equals(env)) {
				return environment.getName();
			}
		}
		throw new RuntimeException("请选择环境");
	}

	public static String getDubboPath(String env) {
		List<Environment> environmentList = ENVIRONMENT_LIST;
		for (Environment environment : environmentList) {
			if (environment.getEnv().equals(env)) {
				if (StringUtils.isNotBlank(environment.getPath())) {
					return environment.getPath();
				} else {
					return DEFAULT_PATH;
				}
			}
		}
		throw new RuntimeException("请选择环境");
	}

	public static String getDubboRegistryGroup(String env) {
		List<Environment> environmentList = ENVIRONMENT_LIST;
		for (Environment environment : environmentList) {
			if (environment.getEnv().equals(env)) {
				if (StringUtils.isNotBlank(environment.getRegistryGroup())) {
					return environment.getRegistryGroup();
				} else {
					return "";
				}
			}
		}
		throw new RuntimeException("请选择环境");
	}

	@Value("${scan.package}")
	public void setSCAN_PACKAGE(String sCAN_PACKAGE) {
		SCAN_PACKAGE = sCAN_PACKAGE;
	}

	@Value("${exclude.package}")
	public void setEXCLUDE_PACKAGE(String eXCLUDE_PACKAGE) {
		EXCLUDE_PACKAGE = eXCLUDE_PACKAGE;
	}

}
