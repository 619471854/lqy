package com.lqy.abook.tool;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.message.BasicNameValuePair;


/**
 * 用于封装post方法的参数
 */
public class PostParams {

	private List<BasicNameValuePair> list;

	public PostParams() {
		list = new ArrayList<BasicNameValuePair>();
	}

	public List<BasicNameValuePair> getList() {
		return list;
	}

	public void setList(List<BasicNameValuePair> list) {
		this.list = list;
	}

	public String getParams() {

		if (list == null || list.size() == 0)
			return CONSTANT.EMPTY;

		StringBuilder sb = new StringBuilder();
		for (BasicNameValuePair p : list) {
			sb.append("&" + p.getName() + "=" + p.getValue());
		}

		sb.deleteCharAt(0);
		return sb.toString();

	}

	public String toString() {
		return getParams();

	}

	/**
	 * 添加参数
	 */
	public void add(String key, String value) {
		if (!Util.isEmpty(value))
			list.add(new BasicNameValuePair(key, value));
	}

}
