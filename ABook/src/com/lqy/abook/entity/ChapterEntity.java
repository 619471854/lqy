package com.lqy.abook.entity;

public class ChapterEntity extends SerializableEntity {
	private int id;//章节列表的位置
	private String name;
	private String url;
	private LoadStatusEnum loadStatus = LoadStatusEnum.notLoaded;

	public boolean isVip() {
		return loadStatus == LoadStatusEnum.vip;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public LoadStatusEnum getLoadStatus() {
		return loadStatus;
	}

	public void setLoadStatus(LoadStatusEnum loadStatus) {
		this.loadStatus = loadStatus;
	}

}
