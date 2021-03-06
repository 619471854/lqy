package com.lqy.abook.entity;

import com.lqy.abook.tool.CONSTANT;

public class BookEntity extends SerializableEntity {

	private long id = CONSTANT._1;
	private String cover;
	private String name;//not null
	private String type;
	private String author;
	private int matchWords;
	private Site site = Site.getDefault();
	private String tip;
	private String detailUrl;
	private int words;
	private String updateTime;
	private String newChapter;
	private String directoryUrl;//not null
	private boolean isCompleted;
	private LoadStatus loadStatus = LoadStatus.notLoaded;
	private int currentChapterId;
	private int readBegin;
	private int unReadCount;
	private String ext;//额 外信息


	public void setCurrentChapterId(int currentChapterId) {
		this.currentChapterId = currentChapterId;
		this.readBegin=0;
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

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
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

	public LoadStatus getLoadStatus() {
		return loadStatus;
	}

	public void setLoadStatus(LoadStatus loadStatus) {
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

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

	// public boolean equals(BookEntity e) {
	// return (e != null && e.getAuthor().equals(author) &&
	// e.getName().equals(name) && e.getSite().ordinal() == site.ordinal());
	// }

}
