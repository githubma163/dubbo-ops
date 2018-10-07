package com.max.dubbo.entity;

/**
 * 
 * @author githubma
 * @date 2018年5月29日 下午3:26:44
 *
 */
public class LocalMethod {

	public String beanId;
	public String methodName;
	public String[] parameterType;
	public Object[] parameterValue;

	public String getBeanId() {
		return beanId;
	}

	public void setBeanId(String beanId) {
		this.beanId = beanId;
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

	public Object[] getParameterValue() {
		return parameterValue;
	}

	public void setParameterValue(Object[] parameterValue) {
		this.parameterValue = parameterValue;
	}

}
