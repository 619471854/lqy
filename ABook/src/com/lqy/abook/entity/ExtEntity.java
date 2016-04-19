package com.lqy.abook.entity;

import com.google.gson.Gson;
import com.lqy.abook.tool.Util;

public class ExtEntity extends SerializableEntity {

	private String cookie;
	private String encodeType;

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
