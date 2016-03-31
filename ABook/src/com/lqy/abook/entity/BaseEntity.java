package com.lqy.abook.entity;

import com.google.gson.Gson;

public class BaseEntity {
	@Override
	public String toString() {
		return new Gson().toJson(this);
	}
}
