package com.lqy.abook.parser;

import java.util.List;

import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.Site;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;

public abstract class ParserBase extends ParserUtil {
	protected static final int searchMaxSizeSite = 5;// 每个网站最多显示5本书
	protected static final int MaxMatch = Integer.MAX_VALUE;// 最大的匹配值
	protected Config config;
	protected Site site;

	// 搜索小说

	public boolean parserSearch(List<BookEntity> books, String key) {
		try {
			key = key.replaceAll("'", "").replaceAll("\\s", " ");// 去除单引号和多余的空格
			SimpleNodeIterator iterator = getParserResult(config.searchUrl + key, config.searchFilter);
			MyLog.i("Search ok,parsering");
			int count = 0;
			while (iterator.hasMoreNodes() && (count++ < searchMaxSizeSite || books.size() < searchMaxSizeSite)) {
				String html = iterator.nextNode().toHtml();
				boolean success = processSearchNode(books, html, key.split(" "));
				if (!success)
					break;// 如果未匹配，后面的就不要了
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// 搜索小说所在的所在的site
	public boolean parserSearchSite(List<BookEntity> books, String name, String author) {
		try {
			SimpleNodeIterator iterator = getParserResult(config.searchUrl + name + author, config.searchFilter);
			MyLog.i("SearchSite ok,parsering");
			while (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				// 完全匹配到了1个就可以了
				if (processSearchSiteNode(books, html, name, author))
					break;
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	// 解析 搜索小说
	protected abstract boolean processSearchNode(List<BookEntity> books, String html, String[] searchKey) throws Exception;

	// 更新小说，如果false则未更新或更新失败
	public abstract boolean updateBook(BookEntity book);

	// 更新小说，如果false则未更新或更新失败
	public abstract List<ChapterEntity> updateBookAndDict(BookEntity book);

	// 根据小说主页地址获取 小说信息
	public abstract boolean parserBookDetail(BookEntity detail);

	// 根据小说目录地址获取 目录
	public abstract List<ChapterEntity> parserBookDict(String url);

	// 获取章节详情
	public abstract String getChapterDetail(String url);

	// 解析 搜索小说所在的所在的site
	private boolean processSearchSiteNode(List<BookEntity> books, String html, String name, String author) throws Exception {
		// MyLog.i(html);
		BookEntity book = new BookEntity();
		book.setSite(site);
		String nameHtml = matcher(html, config.nameReg);
		book.setName(nameHtml.replaceAll(config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));
		String authorHtml = matcher(html, config.authorReg);
		book.setAuthor(authorHtml.replaceAll(config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		// 如果有作者，那么必须完全匹配
		if (!name.equals(book.getName()) || !author.equals(book.getAuthor())) {
			return false;// 继续找第二本
		}
		book.setCover(matcher(html, config.coverReg));
		book.setDetailUrl(matcher(html, config.detailUrlReg));
		book.setType(matcher(html, config.typeReg));
		book.setTip(matcher(html, config.tipsReg).replaceAll(config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));

		book.setNewChapter(matcher(html, config.newChapterReg));
		book.setUpdateTime(matcher(html, config.updateTimeReg));
		book.setDirectoryUrl(matcher(html, config.directoryUrlReg));

		books.add(book);
		return true;// 已找到
	}

	/**
	 * 通过url与html解析小说目录
	 */
	public abstract ParserResult parserBrowser(String url, String html);

}
