package com.max.dubbo.entity;

import java.util.Set;

/**
 * 
 * @author githubma
 * @date 2018年5月18日 上午10:16:51
 *
 */
public class ThreadExecuteResult {

	private Integer count;// 单个线程执行总次数
	private Integer successCount;// 单个线程执行成功次数
	private Integer failCount;// 单个线程执行失败次数
	private Long maxExecuteTime;// 单个线程最大执行时间，单位毫秒
	private Long minExecuteTime;// 单个线程最小执行时间，单位毫秒
	private Long avgExecuteTime;// 单个线程平均执行时间
	private Long totalExecuteTime;// 单个线程执行总时间

	private Set<String> failMsg;// 失败错误信息汇总

	public ThreadExecuteResult() {
		super();
	}

	public ThreadExecuteResult(Integer count, Integer successCount, Integer failCount, Long maxExecuteTime,
			Long minExecuteTime, Long avgExecuteTime, Long totalExecuteTime, Set<String> failMsg) {
		super();
		this.count = count;
		this.successCount = successCount;
		this.failCount = failCount;
		this.maxExecuteTime = maxExecuteTime;
		this.minExecuteTime = minExecuteTime;
		this.avgExecuteTime = avgExecuteTime;
		this.totalExecuteTime = totalExecuteTime;
		this.failMsg = failMsg;
	}

	public Integer getCount() {
		return count;
	}

	public void setCount(Integer count) {
		this.count = count;
	}

	public Integer getSuccessCount() {
		return successCount;
	}

	public void setSuccessCount(Integer successCount) {
		this.successCount = successCount;
	}

	public Integer getFailCount() {
		return failCount;
	}

	public void setFailCount(Integer failCount) {
		this.failCount = failCount;
	}

	public Long getMaxExecuteTime() {
		return maxExecuteTime;
	}

	public void setMaxExecuteTime(Long maxExecuteTime) {
		this.maxExecuteTime = maxExecuteTime;
	}

	public Long getMinExecuteTime() {
		return minExecuteTime;
	}

	public void setMinExecuteTime(Long minExecuteTime) {
		this.minExecuteTime = minExecuteTime;
	}

	public Long getAvgExecuteTime() {
		return avgExecuteTime;
	}

	public void setAvgExecuteTime(Long avgExecuteTime) {
		this.avgExecuteTime = avgExecuteTime;
	}

	public Long getTotalExecuteTime() {
		return totalExecuteTime;
	}

	public void setTotalExecuteTime(Long totalExecuteTime) {
		this.totalExecuteTime = totalExecuteTime;
	}

	public Set<String> getFailMsg() {
		return failMsg;
	}

	public void setFailMsg(Set<String> failMsg) {
		this.failMsg = failMsg;
	}

}
