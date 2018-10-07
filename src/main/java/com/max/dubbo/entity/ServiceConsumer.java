package com.max.dubbo.entity;

/**
 * 
 * @author githubma
 * @date 2018年3月13日 下午2:10:10
 *
 */
public class ServiceConsumer {

	private String host;

	private String application;

	private String callInterface;// 被调用的dubbo服务接口

	private String invokeInterface;// 调用dubbo服务的接口

	private String pid;

	private String methods;

	private String content;

	private String version;

	private String dubbo;

	private String authority;

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getApplication() {
		return application;
	}

	public void setApplication(String application) {
		this.application = application;
	}

	public String getCallInterface() {
		return callInterface;
	}

	public void setCallInterface(String callInterface) {
		this.callInterface = callInterface;
	}

	public String getInvokeInterface() {
		return invokeInterface;
	}

	public void setInvokeInterface(String invokeInterface) {
		this.invokeInterface = invokeInterface;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getMethods() {
		return methods;
	}

	public void setMethods(String methods) {
		this.methods = methods;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDubbo() {
		return dubbo;
	}

	public void setDubbo(String dubbo) {
		this.dubbo = dubbo;
	}

	public String getAuthority() {
		return authority;
	}

	public void setAuthority(String authority) {
		this.authority = authority;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((authority == null) ? 0 : authority.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ServiceConsumer other = (ServiceConsumer) obj;
		if (authority == null) {
			if (other.authority != null)
				return false;
		} else if (!authority.equals(other.authority))
			return false;
		return true;
	}

}
