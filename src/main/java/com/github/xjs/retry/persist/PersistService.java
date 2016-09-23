/**
 * 
 */
package com.github.xjs.retry.persist;

import java.util.List;

/**
 * @author 605162215@qq.com
 *
 * 2016年8月6日 下午2:34:49
 */
public interface PersistService<T> {
	/*insert*/
	public void save(T t);
	/*update*/
	public void update(T t);
	/*delete*/
	public void delete(T t);
	/*update invisible*/
	public void deleteLogic(T t);
	/*select visible*/
	public List<T> getAll();
	/*select count(visible)*/
	public int size();
}
