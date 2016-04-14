package com.lqy.abook.entity;

import com.lqy.abook.tool.CONSTANT;

public class FavoriteEntity extends BaseEntity {
	private int id = CONSTANT._1;
	private String title;
	private String url;

	public FavoriteEntity(String title, String url) {
		this.title = title;
		this.url = url;

	}

	public FavoriteEntity() {

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
