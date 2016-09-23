/**
 * 
 */
package com.github.xjs.retry;

import java.util.List;
import java.util.concurrent.DelayQueue;

import com.github.xjs.retry.persist.PersistService;
import com.github.xjs.retry.util.ThreadPoolUtil;

/**
 * @author 605162215@qq.com
 *
 * 2016年8月6日 下午1:05:20
 */
public class RetryService {
	
	private static final int[] DEFAULT_INTERVALS = new int[]{1*60, 5*60, 10*60, 30*60, 60*60, 2*3600, 5*3600, 10*600, 15*3600, 24*3600};
	
	private DelayQueue<RetryTask> delayQueue = new DelayQueue<RetryTask>();
	
	private boolean start = false;
	
	private int[] intervals;
	
	private OnRetryListener retryListener;
	private PersistService<RetryTask> persistService;
	
	public RetryService(){
		this(DEFAULT_INTERVALS);
	}
	
	public RetryService(int[] intervals){
		this(intervals, null);
	}
	
	public RetryService(int[] intervals, PersistService<RetryTask> persistService){
		this.intervals = intervals;
		this.persistService = persistService;
	}
	
	public static interface OnRetryListener{
        public void onRetryArrived(RetryTask retryTask);
        public void onRetryFailed(RetryTask retryTask);
        public void onRetrySuccessed(RetryTask retryTask);
    }
	
	/**
     * 应用启动以后，由Controller调用启动，只调用一次
     * */
    public void start(final OnRetryListener listener){
        if(start){
            return;
        }
        System.out.println("[RetryService] start....");
        start = true;
        retryListener = listener;
        new Thread(new Runnable(){
            public void run(){
            	//找到数据库中还需要重试的那些任务
            	ThreadPoolUtil.execute(new Runnable(){
					@Override
					public void run() {
						System.out.println("[RetryService]find task need to run");
						if(persistService == null){
							return;
						}
						List<RetryTask> oldTasks = persistService.getAll();
						System.out.println("[RetryService]find task need to run:"+oldTasks);
						if(oldTasks == null || oldTasks.size() <= 0){
							return;
						}
						for(RetryTask task : oldTasks){
							//不需要写数据库了
							delayQueue.put(task);
						}
					}
            	});
            	//等待从重试任务
                try{
                    while(true){
                    	//出队
                    	RetryTask task = delayQueue.take();
                    	ThreadPoolUtil.execute(new Runnable(){
                    		public void run(){
                    			retry(task);
                    		}
                    	});
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public void retry(RetryTask task){
    	if(task == null){
    		return;
    	}
		System.out.println("[retry]do retry:"+task);
		boolean success = false;
		try{
			Class<? extends RetryAble> taskClass = task.getTask();
			RetryAble retryAble = taskClass.newInstance();
			success = retryAble.retry();
		}catch(Exception e){
			e.printStackTrace();
		}
		if(!success){
			RetryTask next = getNextTask(task);
			if(next != null){
				System.out.println("[retry]will retry next:"+next);
				add(next, false);
				if(retryListener != null){
		        	retryListener.onRetryArrived(task);
		        }
			}else{
				System.out.println("[retry]sorry, i have tried all my best,abondon:"+task);
				remove(task, true);
				if(retryListener != null){
					retryListener.onRetryFailed(task);
				}
			}
		}else{
			System.out.println("[retry]success!"+task);
			remove(task, false);
			if(retryListener != null){
				retryListener.onRetrySuccessed(task);
			}
		}
	}
    
    public void add(RetryAble task){
    	if(task == null){
    		return;
    	}
    	add(task.getClass());
    }
    
    public void add(Class<? extends RetryAble> taskClass){
    	RetryTask retryTask = new RetryTask(taskClass, intervals[0]);
    	add(retryTask, true);
    }
    
    private void add(final RetryTask retryTask, boolean first){
        ThreadPoolUtil.execute(new Runnable(){
            public void run(){
                // 入队
                delayQueue.put(retryTask);
                // 做持久化
                if(persistService != null){
                	if(first){
                		persistService.save(retryTask);
                	}else{
                		persistService.update(retryTask);
                	}
                }
                System.out.println("[retry]delayQueue.size："+delayQueue.size()+",persistService.size:"+persistService.size());
            }
        });
    }
  
    public void remove(final RetryTask target, final boolean logical){
        ThreadPoolUtil.execute(new Runnable() {
            public void run() {
                if (target == null) {
                    return;
                }
                // 出队
                delayQueue.remove(target);
                // 从持久化删除
                if(persistService != null){
                	if(logical){
                		persistService.deleteLogic(target);
                	}else{
                		persistService.delete(target);
                	}
                }
                System.out.println("[retry]delayQueue.size："+delayQueue.size()+",persistService.size:"+persistService.size()+",getAll():"+persistService.getAll());
            }
        });
    }
    
    public RetryTask getNextTask(RetryTask task){
    	int index = task.getIndex();
    	if(index >= intervals.length-1){
    		return null;
    	}
    	int nextInterval = intervals[index + 1];
        return task.update(nextInterval);
    }
    
}
