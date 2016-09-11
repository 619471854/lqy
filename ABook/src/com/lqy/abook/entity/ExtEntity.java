package com.lqy.abook.entity;

import com.google.gson.Gson;
import com.lqy.abook.tool.Util;

public class ExtEntity extends SerializableEntity {

	private String cookie;
	private String encodeType;
	private boolean isPicLoadOver;
	private String firstUrl;//第一个章节的地址，用于删除之前的章节
	private String lastestUrl;

	public String getFirstUrl() {
		return firstUrl;
	}

	public void setFirstUrl(String firstUrl) {
		this.firstUrl = firstUrl;
	}

	public String getLastestUrl() {
		return lastestUrl;
	}

	public void setLastestUrl(String lastestUrl) {
		this.lastestUrl = lastestUrl;
	}

	public boolean isPicLoadOver() {
		return isPicLoadOver;
	}

	public void setPicLoadOver(boolean isPicLoadOver) {
		this.isPicLoadOver = isPicLoadOver;
	}

	public static ExtEntity valueOf(String json) {
		if (Util.isEmpty(json))
			return null;
		try {
			return new Gson().fromJson(json, ExtEntity.class);
		} catch (Exception e) {
			return null;
		}
	}

	public ExtEntity(String cookie, String encodeType) {
		this.cookie = cookie;
		this.encodeType = encodeType;
	}

	public ExtEntity() {
	}
	public String getCookie() {
		return cookie;
	}

	public void setCookie(String cookie) {
		this.cookie = cookie;
	}

	public String getEncodeType() {
		return encodeType;
	}

	public void setEncodeType(String encodeType) {
		this.encodeType = encodeType;
	}

}
