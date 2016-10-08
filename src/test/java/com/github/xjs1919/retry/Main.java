/**
 * 
 */
package com.github.xjs1919.retry;

import java.util.Random;

import com.github.xjs1919.retry.RetryService.OnRetryListener;

/**
 * @author 605162215@qq.com
 *
 * 2016年8月6日 下午1:05:30
 */
public class Main {
	public static class Business implements RetryAble{
		
		@Override
		public boolean retry(String param) throws Exception{
			try{
				System.out.println("[Business]do business,param:"+param);
				Thread.sleep(1000);
				int rnd = new Random().nextInt(100);
				System.out.println("[Business]rnd:"+rnd);
				if(rnd > 80){
					System.out.println("[Business]do business end");
					return true;
				}else{
					throw new Exception();
				}
			}catch(Exception e){
				System.out.println("[Business]do business exception!");
				throw e;
			}
		}
	}
	public static void main(String[] args) {
		//创建service
		RetryService service = new RetryService(new int[]{1,3,5}, new RetryPersistService());
		//启动service
		service.start(new OnRetryListener(){
			//每次做重试完了以后都会回调
			@Override
			public void onRetryArrived(RetryTask retryTask) {
				System.out.println("[main]onDelayedArrived:"+retryTask);
			}
			//最终失败了的时候会回调，只一次
			@Override
			public void onRetryFailed(RetryTask retryTask){
				System.out.println("[main]onRetryFailed");
			}
			//最终成功了的时候会回调，只一次
			@Override
	        public void onRetrySuccessed(RetryTask retryTask){
	        	System.out.println("[main]onRetrySuccessed");
	        }
		});
		//做业务逻辑处理
		String param = "测试字符串";
		Business business = new Business();
		try{
			business.retry(param);
		}catch(Exception e){
			System.out.println("[main]business ecxception, try redo");
			//失败重试
			service.add(business, param);
		}
	}
}
