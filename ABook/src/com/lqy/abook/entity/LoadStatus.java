package com.lqy.abook.entity;

public enum LoadStatus {
	// if book 获取目录错误：error；下载章节中：loading；下载全部或章节完成：completed，有更新：hasnew,其它：notLoaded,没有vip
	// if chapter......没有hasnew
	notLoaded, loading, completed, failed, vip,hasnew;

	public static LoadStatus valueOf(int index) {
		try {
			return LoadStatus.values()[index];
		} catch (Exception e) {
			return LoadStatus.notLoaded;
		}
	}
}
