package com.max.dubbo.entity;

import java.util.List;

/**
 * 
 * @author githubma
 * @date 2018年3月5日 下午6:53:44
 *
 */
public class ServiceMethod {

	private String serviceName;
	private String serviceVersion;
	private String group;
	private String methodName;
	private String[] parameterType;
	private String[] parameterName;
	private Object[] parameterValue;
	private String returnType;
	private String targetProvider = "ip:port";
	private String methodSignature;// 方法签名，
	private String methodString;// 方法签名对应的字符串，一般情况下与methodSignature一样，但是如果依赖的jar没有，则methodSignature会显示异常信息"<java.lang.TypeNotPresentException:
								// Type AccountAuthTspDTO not
								// present>，methodString会显示正常值
	private String serviceHttpUrl;// http 接口地址
	private String serviceHttpUrlWithParam;// http接口url带参数
	private Object[] serviceHttpBody;// http接口消息体参数
	private String serviceHttpMsg;
	private String uuid;// 生成规则，serviceName+group+serviceVersion+methodSignature一起计算md5
	private String jarFilePath;
	private String jarSourceFilePath;
	private List<String> dependencyJarFilePath;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getServiceVersion() {
		return serviceVersion;
	}

	public void setServiceVersion(String serviceVersion) {
		this.serviceVersion = serviceVersion;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String[] getParameterType() {
		return parameterType;
	}

	public void setParameterType(String[] parameterType) {
		this.parameterType = parameterType;
	}

	public String[] getParameterName() {
		return parameterName;
	}

	public void setParameterName(String[] parameterName) {
		this.parameterName = parameterName;
	}

	public Object[] getParameterValue() {
		return parameterValue;
	}

	public void setParameterValue(Object[] parameterValue) {
		this.parameterValue = parameterValue;
	}

	public String getReturnType() {
		return returnType;
	}

	public void setReturnType(String returnType) {
		this.returnType = returnType;
	}

	public String getTargetProvider() {
		return targetProvider;
	}

	public void setTargetProvider(String targetProvider) {
		this.targetProvider = targetProvider;
	}

	public String getMethodSignature() {
		return methodSignature;
	}

	public void setMethodSignature(String methodSignature) {
		this.methodSignature = methodSignature;
	}

	public String getMethodString() {
		return methodString;
	}

	public void setMethodString(String methodString) {
		this.methodString = methodString;
	}

	public String getServiceHttpUrl() {
		return serviceHttpUrl;
	}

	public void setServiceHttpUrl(String serviceHttpUrl) {
		this.serviceHttpUrl = serviceHttpUrl;
	}

	public String getServiceHttpUrlWithParam() {
		return serviceHttpUrlWithParam;
	}

	public void setServiceHttpUrlWithParam(String serviceHttpUrlWithParam) {
		this.serviceHttpUrlWithParam = serviceHttpUrlWithParam;
	}

	public Object[] getServiceHttpBody() {
		return serviceHttpBody;
	}

	public void setServiceHttpBody(Object[] serviceHttpBody) {
		this.serviceHttpBody = serviceHttpBody;
	}

	public String getServiceHttpMsg() {
		return serviceHttpMsg;
	}

	public void setServiceHttpMsg(String serviceHttpMsg) {
		this.serviceHttpMsg = serviceHttpMsg;
	}

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getJarFilePath() {
		return jarFilePath;
	}

	public void setJarFilePath(String jarFilePath) {
		this.jarFilePath = jarFilePath;
	}

	public String getJarSourceFilePath() {
		return jarSourceFilePath;
	}

	public void setJarSourceFilePath(String jarSourceFilePath) {
		this.jarSourceFilePath = jarSourceFilePath;
	}

	public List<String> getDependencyJarFilePath() {
		return dependencyJarFilePath;
	}

	public void setDependencyJarFilePath(List<String> dependencyJarFilePath) {
		this.dependencyJarFilePath = dependencyJarFilePath;
	}

}
