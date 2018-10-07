package com.max.dubbo.entity;

import java.util.List;

/**
 * 
 * @author githubma
 * @date 2018年7月12日 下午3:46:47
 *
 */
public class MonitorEnv {

	private String env;

	private String name;

	private List<Monitor> zookeeperList;

	private List<Monitor> dubboList;

	private int zookeeperSuccessCount;

	private int zookeeperFailCount;

	private int dubboSuccessCount;

	private int dubboFailCount;

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

	public List<Monitor> getZookeeperList() {
		return zookeeperList;
	}

	public void setZookeeperList(List<Monitor> zookeeperList) {
		this.zookeeperList = zookeeperList;
	}

	public List<Monitor> getDubboList() {
		return dubboList;
	}

	public void setDubboList(List<Monitor> dubboList) {
		this.dubboList = dubboList;
	}

	public int getZookeeperSuccessCount() {
		return zookeeperSuccessCount;
	}

	public void setZookeeperSuccessCount(int zookeeperSuccessCount) {
		this.zookeeperSuccessCount = zookeeperSuccessCount;
	}

	public int getZookeeperFailCount() {
		return zookeeperFailCount;
	}

	public void setZookeeperFailCount(int zookeeperFailCount) {
		this.zookeeperFailCount = zookeeperFailCount;
	}

	public int getDubboSuccessCount() {
		return dubboSuccessCount;
	}

	public void setDubboSuccessCount(int dubboSuccessCount) {
		this.dubboSuccessCount = dubboSuccessCount;
	}

	public int getDubboFailCount() {
		return dubboFailCount;
	}

	public void setDubboFailCount(int dubboFailCount) {
		this.dubboFailCount = dubboFailCount;
	}

}
