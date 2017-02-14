package com.lqy.abook.entity;

import com.lqy.abook.tool.CONSTANT;

public class BookEntity extends SerializableEntity {

	private long id = CONSTANT._1;
	private String cover;
	private String name;// not null
	private String type;
	private String author;
	private int matchWords;
	private SiteEnum site = SiteEnum.getDefault();
	private String tip;
	private String detailUrl;
	private int words;
	private String updateTime;
	private String newChapter;
	private String directoryUrl;// not null
	private boolean isCompleted;
	private LoadStatusEnum loadStatus = LoadStatusEnum.notLoaded;
	private int currentChapterId;
	private int readBegin;
	private int unReadCount;
	private ExtEntity ext;// 额 外信息

	public boolean isPicLoadOver() {
		return ext != null && ext.isPicLoadOver();
	}

	public void setPicLoadOver(boolean isPicLoadOver) {
		if (ext == null)
			ext = new ExtEntity();
		ext.setPicLoadOver(isPicLoadOver);
	}

	public String getFirstUrl() {
		return ext == null ? null : ext.getFirstUrl();
	}

	public void setFirstUrl(String firstUrl) {
		if (ext == null)
			ext = new ExtEntity();
		ext.setFirstUrl(firstUrl);
	}

	public String getLastestUrl() {
		return ext == null ? null : ext.getLastestUrl();
	}

	public void setLastestUrl(String lastestUrl) {
		if (ext == null)
			ext = new ExtEntity();
		ext.setLastestUrl(lastestUrl);
	}

	/**
	 * 是否支持更新
	 */
	public boolean supportUpdated() {
		return site != null && site.supportUpdated();
	}

	public void setCurrentChapterId(int currentChapterId) {
		this.currentChapterId = currentChapterId;
		this.readBegin = 0;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getCover() {
		return cover;
	}

	public void setCover(String cover) {
		this.cover = cover;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getAuthor() {
		return author;
	}

	public void setAuthor(String author) {
		this.author = author;
	}

	public int getMatchWords() {
		return matchWords;
	}

	public void setMatchWords(int matchWords) {
		this.matchWords = matchWords;
	}

	public SiteEnum getSite() {
		return site;
	}

	public void setSite(SiteEnum site) {
		this.site = site;
	}

	public String getTip() {
		return tip;
	}

	public void setTip(String tip) {
		this.tip = tip;
	}

	public String getDetailUrl() {
		return detailUrl;
	}

	public void setDetailUrl(String detailUrl) {
		this.detailUrl = detailUrl;
	}

	public int getWords() {
		return words;
	}

	public void setWords(int words) {
		this.words = words;
	}

	public String getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}

	public String getNewChapter() {
		return newChapter;
	}

	public void setNewChapter(String newChapter) {
		this.newChapter = newChapter;
	}

	public String getDirectoryUrl() {
		return directoryUrl;
	}

	public void setDirectoryUrl(String directoryUrl) {
		this.directoryUrl = directoryUrl;
	}

	public boolean isCompleted() {
		return isCompleted;
	}

	public void setCompleted(boolean isCompleted) {
		this.isCompleted = isCompleted;
	}

	public LoadStatusEnum getLoadStatus() {
		return loadStatus;
	}

	public void setLoadStatus(LoadStatusEnum loadStatus) {
		this.loadStatus = loadStatus;
	}

	public int getCurrentChapterId() {
		return currentChapterId;
	}

	public int getUnReadCount() {
		return unReadCount;
	}

	public void setUnReadCount(int unReadCount) {
		this.unReadCount = unReadCount;
	}

	public int getReadBegin() {
		return readBegin;
	}

	public void setReadBegin(int readBegin) {
		this.readBegin = readBegin;
	}

	public ExtEntity getExt() {
		return ext;
	}

	public void setExt(ExtEntity ext) {
		this.ext = ext;
	}

	// public boolean equals(BookEntity e) {
	// return (e != null && e.getAuthor().equals(author) &&
	// e.getName().equals(name) && e.getSite().ordinal() == site.ordinal());
	// }

}
