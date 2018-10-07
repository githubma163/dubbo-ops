package com.max.dubbo.entity;

/**
 * 
 * @author githubma
 * @date 2018年7月12日 下午3:35:34
 *
 */
public class Monitor {

	private String ip;// ip地址

	private String port;// 端口号

	private String name;// 名称

	private String time;// 最后更新时间

	private Integer status;// 状态

	private String env;// 环境

	private String type;// zookeeper,dubbo

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTime() {
		return time;
	}

	public void setTime(String time) {
		this.time = time;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getEnv() {
		return env;
	}

	public void setEnv(String env) {
		this.env = env;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

}
