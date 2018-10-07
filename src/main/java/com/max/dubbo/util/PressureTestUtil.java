package com.max.dubbo.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.max.dubbo.entity.ServiceMethod;
import com.max.dubbo.entity.ThreadExecuteResult;

/**
 * 
 * @author githubma
 * @date 2018年5月18日 上午9:42:30
 *
 */
public class PressureTestUtil {

	static Logger logger = LoggerFactory.getLogger(PressureTestUtil.class);

	static Integer MAX_THREAD_COUNT = 100;// 最大线程数量
	static Integer MAX_THREAD_EXECUTE_COUNT = 1000;// 单个线程最大执行次数

	/**
	 * 
	 * @param threadCount
	 *            线程数量
	 * @param threadExecuteCount
	 *            每个线程执行次数
	 * @param uuid
	 * @param targetProvider
	 * @param parameterValue
	 * @param env
	 * @throws BrokenBarrierException
	 * @throws InterruptedException
	 */
	public static List<ThreadExecuteResult> pressureTest(Integer threadCount, Integer threadExecuteCount, String uuid,
			String targetProvider, Object[] parameterValue, String env)
			throws InterruptedException, BrokenBarrierException {
		if (threadCount < 0 || threadCount > MAX_THREAD_COUNT) {
			throw new IllegalArgumentException("线程数量不能超过10");
		}
		if (threadExecuteCount < 0 || threadExecuteCount > MAX_THREAD_EXECUTE_COUNT) {
			throw new IllegalArgumentException("单个线程执行次数不能超过1000");
		}
		ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(threadCount, threadCount, 10, TimeUnit.SECONDS,
				new ArrayBlockingQueue<>(MAX_THREAD_EXECUTE_COUNT));
		final List<ThreadExecuteResult> resultList = Collections.synchronizedList(new ArrayList<ThreadExecuteResult>());//  线程安全
		final CountDownLatch countDownLatch = new CountDownLatch(threadCount);
		for (int i = 0; i < threadCount; i++) {
			ThreadExecuteResult threadExecuteResult = new ThreadExecuteResult();
			threadPoolExecutor.execute(new SingleThreadExecute(threadExecuteCount, uuid, targetProvider, parameterValue,
					env, threadExecuteResult, countDownLatch, i, resultList));
		}
		countDownLatch.await();
		return resultList;

	}

	static class SingleThreadExecute implements Runnable {
		Integer threadExecuteCount;
		String uuid;
		String targetProvider;
		Object[] parameterValue;
		String env;
		ThreadExecuteResult threadExecuteResult;
		CountDownLatch countDownLatch;
		int index;
		List<ThreadExecuteResult> resultList;

		public SingleThreadExecute(Integer threadExecuteCount, String uuid, String targetProvider,
				Object[] parameterValue, String env, ThreadExecuteResult threadExecuteResult,
				CountDownLatch countDownLatch, int index, List<ThreadExecuteResult> resultList) {
			super();
			this.threadExecuteCount = threadExecuteCount;
			this.uuid = uuid;
			this.targetProvider = targetProvider;
			this.parameterValue = parameterValue;
			this.env = env;
			this.threadExecuteResult = threadExecuteResult;
			this.countDownLatch = countDownLatch;
			this.index = index;
			this.resultList = resultList;
		}

		public void run() {
			Integer count = 0;// 单个线程执行总次数
			Integer successCount = 0;// 单个线程执行成功次数
			Integer failCount = 0;// 单个线程执行失败次数
			Long maxExecuteTime = 0L;// 单个线程最大执行时间，单位毫秒
			Long minExecuteTime = 0L;// 单个线程最小执行时间，单位毫秒
			Long avgExecuteTime = 0L;// 单个线程平均执行时间
			Long totalExecuteTime = 0L;// 单个线程执行总时间
			Set<String> failMsg = new HashSet<String>();// 失败错误信息汇总
			for (int i = 0; i < threadExecuteCount; i++) {
				count++;
				long startTime = System.currentTimeMillis();
				try {
					ServiceMethod service = ServiceUtil.serviceMap.get(uuid);
					service.setParameterValue(parameterValue);
					if (!StringUtils.isBlank(targetProvider)) {
						service.setTargetProvider(targetProvider);
					}
					DubboUtil.genericServiceInvoke(service.getServiceName(), service.getServiceVersion(),
							service.getGroup(), service.getMethodName(), service.getParameterType(),
							service.getParameterValue(), service.getTargetProvider(), env);
				} catch (Exception e) {
					failCount++;
					failMsg.add(e.getMessage());
					logger.error("压测单个执行出错", e);
				}
				long endTime = System.currentTimeMillis();
				long executeTime = endTime - startTime;
				if (i == 0) {
					maxExecuteTime = executeTime;
					minExecuteTime = executeTime;
				} else {
					if (maxExecuteTime < executeTime) {
						maxExecuteTime = executeTime;
					}
					if (minExecuteTime > executeTime) {
						minExecuteTime = executeTime;
					}
				}
				totalExecuteTime += executeTime;
				successCount++;
			}
			avgExecuteTime = totalExecuteTime / threadExecuteCount;
			threadExecuteResult = new ThreadExecuteResult(count, successCount, failCount, maxExecuteTime,
					minExecuteTime, avgExecuteTime, totalExecuteTime, failMsg);
			resultList.add(threadExecuteResult);
			countDownLatch.countDown();
		}
	}

}
