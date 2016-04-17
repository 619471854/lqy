package com.lqy.abook.parser;

import java.util.List;

import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.Site;

public abstract class ParserBase extends ParserUtil {
	protected static final int searchMaxSizeSite = 5;// 每个网站最多显示5本书
	protected static final int MaxMatch = Integer.MAX_VALUE;// 最大的匹配值
	protected Site site;

	// 搜索小说
	public abstract boolean parserSearch(List<BookEntity> books, String key);

	// 搜索拥有此小说的网站
	public abstract BookEntity parserSearchSite(String name, String author);

	// 更新小说，如果false则未更新或更新失败
	public abstract boolean updateBook(BookEntity book);

	// 更新小说，如果null则未更新或更新失败
	public abstract List<ChapterEntity> updateBookAndDict(BookEntity book);

	// 根据小说主页地址获取 小说信息
	public abstract boolean parserBookDetail(BookEntity detail);

	// 根据小说目录地址获取 目录
	public abstract List<ChapterEntity> parserBookDict(String url);

	// 获取章节详情
	public abstract String getChapterDetail(String url);

	// 通过url与html解析小说目录
	public abstract BookAndChapters parserBrowser(String url, String html);

}
