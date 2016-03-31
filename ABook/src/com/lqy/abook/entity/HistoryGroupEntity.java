package com.lqy.abook.entity;

import java.util.List;

public class HistoryGroupEntity extends BaseEntity{
	private String date;
	private List<FavoriteEntity> historyList;

	public HistoryGroupEntity(String date, List<FavoriteEntity> historyList) {
		this.date = date;
		this.historyList = historyList;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public List<FavoriteEntity> getHistoryList() {
		return historyList;
	}

	public void setHistoryList(List<FavoriteEntity> historyList) {
		this.historyList = historyList;
	}

	public int getListSize() {
		// TODO Auto-generated method stub
		return historyList == null ? 0 : historyList.size();
	}
}
