package com.max.dubbo.entity;

/**
 * 
 * @author githubma
 * @date 2018年4月3日 下午4:35:30
 *
 */
public class Environment {

	private String env;
	private String name;
	private String address;
	private String path;
	private String registryGroup;

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getRegistryGroup() {
		return registryGroup;
	}

	public void setRegistryGroup(String registryGroup) {
		this.registryGroup = registryGroup;
	}

}
