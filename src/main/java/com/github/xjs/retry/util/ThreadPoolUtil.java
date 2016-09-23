/**
 * 
 */
package com.github.xjs.retry.util;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author 605162215@qq.com
 *
 * 2016年8月6日 下午1:13:30
 */
public class ThreadPoolUtil {
	
	private final ExecutorService executor;

	private static ThreadPoolUtil instance = new ThreadPoolUtil();

	private ThreadPoolUtil() {
		this.executor = Executors.newFixedThreadPool(5);
	}

	public static ThreadPoolUtil getInstance() {
		return instance;
	}

	public static <T> Future<T> execute(final Callable<T> runnable) {
		return getInstance().executor.submit(runnable);
	}

	public static Future<?> execute(final Runnable runnable) {
		return getInstance().executor.submit(runnable);
	}
}
