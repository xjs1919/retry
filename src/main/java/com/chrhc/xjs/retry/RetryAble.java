/**
 * 
 */
package com.chrhc.xjs.retry;

/**
 * @author 605162215@qq.com
 *
 * 2016年8月6日 下午2:18:28
 */
public interface RetryAble {

	public boolean retryAble()throws Exception;
	
}
