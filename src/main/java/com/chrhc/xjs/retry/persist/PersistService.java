/**
 * 
 */
package com.chrhc.xjs.retry.persist;

import java.util.List;

/**
 * @author 605162215@qq.com
 *
 * 2016年8月6日 下午2:34:49
 */
public interface PersistService<T> {
	
	public void save(T t);
	
	public void delete(T t);
	
	public List<T> getAll();
	
	public int size();
}
