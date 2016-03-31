package com.lqy.abook.entity;

public class ChapterEntity extends SerializableEntity {
	private int id;//章节列表的位置
	private String name;
	private String url;
	private LoadStatus loadStatus = LoadStatus.notLoaded;

	public boolean isVip() {
		return loadStatus == LoadStatus.vip;
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

	public LoadStatus getLoadStatus() {
		return loadStatus;
	}

	public void setLoadStatus(LoadStatus loadStatus) {
		this.loadStatus = loadStatus;
	}

}
