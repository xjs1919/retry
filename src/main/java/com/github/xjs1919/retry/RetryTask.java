/**
 * 
 */
package com.github.xjs1919.retry;

import java.util.UUID;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * @author 605162215@qq.com
 *
 * 2016年8月6日 下午1:06:05
 */
public class RetryTask implements Delayed{

    private Class<? extends RetryAble> task;
    private String param;
    private int index;
    private int interval;
    private long startTime;
    private String uuid;

    private RetryTask(){
    }
    
    public RetryTask(Class<? extends RetryAble> task, String param, int interval){
        this(UUID.randomUUID().toString().replace("-", ""), 0, interval, task, param);
    }
    
    public RetryTask(String uuid,  int index,  int interval, Class<? extends RetryAble> task,String param){
    	this.uuid = uuid;
    	this.index = index;
        this.interval = interval;
        this.task = task;
        this.startTime = System.currentTimeMillis() + interval*1000L;
        this.param = param;
    }

    @Override
    public int compareTo(Delayed other) {
        if (other == this){
            return 0;
        }
        if(other instanceof RetryTask){
        	RetryTask otherTask = (RetryTask)other;
            long otherStartTime = otherTask.getStartTime();
            return (int)(this.startTime - otherStartTime);
        }
        return 0;
    }

    @Override
    public long getDelay(TimeUnit unit) {
        return unit.convert(startTime - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
    }
    
    public RetryTask update(int interval){
    	 RetryTask task = new RetryTask();
    	 task.uuid = this.uuid;
    	 task.task = this.task;
    	 task.param = this.param;
    	 task.index = this.index+1;
    	 task.interval = interval;
         task.startTime = System.currentTimeMillis() + interval*1000L;
         return task;
    }

	public Class<? extends RetryAble> getTask() {
		return task;
	}

	public int getInterval() {
		return interval;
	}

	public long getStartTime() {
		return startTime;
	}

	public String getUuid() {
		return uuid;
	}

	public int getIndex() {
		return index;
	}
	
	public String getParam(){
		return param;
	}

	@Override
	public String toString() {
		return "RetryTask [task=" + task + ", param=" + param + ", index=" + index + ", interval=" + interval
				+ ", startTime=" + startTime + ", uuid=" + uuid + "]";
	}
}
