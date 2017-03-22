package com.lqy.abook.tool;

import java.util.ArrayList;
import java.util.List;

import com.lqy.abook.entity.BookAndChapters;
import com.lqy.abook.entity.BookAndChapters.SearchResult;
import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.parser.ParserBase;

public class ParserTest {

	public static String keyword = "诛仙";

	private ParserBase tool;

	public List<String> resultData = new ArrayList<String>();

	public ParserTest(ParserBase tool) {
		this.tool = tool;
	}

	public void test() {
		ArrayList<BookEntity> data = new ArrayList<BookEntity>();
		tool.parserSearch(data, keyword);

		if (data == null || data.size() == 0) {
			resultData.add(" parserSearch 失败:" + keyword);
			return;
		}
		BookEntity book = data.get(0);
		resultData.add(" parserSearch 成功:" + data.size() + " first=" + book.getName() + "  " + book.getAuthor());
		{
			BookEntity e = tool.parserSearchSite(book.getName(), book.getAuthor());
			if (e != null) {
				resultData.add(" parserSearchSite 成功:" + e.getDirectoryUrl());
			} else {
				resultData.add(" parserSearchSite 失败:" + book.getName() + "  " + book.getAuthor());
			}
		}
		{
			BookEntity copy = copy(book);
			tool.parserBookDetail(copy);
			if (!Util.isEmpty(copy.getTip())) {
				resultData.add(" parserBookDetail 成功:" + shortString(copy.getTip()));
			} else {
				resultData.add(" parserBookDetail 失败:" + (copy.getDetailUrl() == null ? copy.getDirectoryUrl() : copy.getDetailUrl()));
			}
		}

		// {
		// BookEntity copy = copy(book);
		// tool.updateBook(copy);
		// if (!Util.isEmpty(copy.getNewChapter())) {
		// resultData.add(" updateBook 成功:" + copy.getNewChapter());
		// } else {
		// resultData.add(" updateBook 失败:" + (copy.getDetailUrl() == null ?
		// copy.getDirectoryUrl() : copy.getDetailUrl()));
		// }
		// }

		{
			BookEntity copy = copy(book);
			List<ChapterEntity> chapters = tool.updateBookAndDict(copy);
			if (chapters != null && chapters.size() > 0) {
				resultData.add(" updateBookAndDict 成功:" + getChapterTip(chapters));
			} else {
				resultData.add(" updateBookAndDict 失败:" + copy.getDirectoryUrl());
			}
		}

		List<ChapterEntity> chapters = tool.parserBookDict(book.getDirectoryUrl());
		if (chapters != null && chapters.size() > 0) {
			resultData.add(" parserBookDict 成功:" + getChapterTip(chapters));

			{
				String chapter = chapters.get(0).getUrl();
				String txt = tool.getChapterDetail(chapter);

				if (!Util.isEmpty(txt)) {
					resultData.add(" getChapterDetail 成功:" + shortString(txt));
				} else {
					resultData.add(" getChapterDetail 失败 ：" + chapter);
				}
			}

		} else {
			resultData.add(" parserBookDict 失败:" + book.getDirectoryUrl());
		}
		if (Util.isEmpty(book.getDetailUrl())) {
			resultData.add(" parserBrowser DetailUrl is null");
		} else {
			BookAndChapters re = tool.parserBrowser(book.getDetailUrl(), null, null);
			if (re == null) {
				resultData.add(" parserBrowser DetailUrl 不匹配 ：" + book.getDetailUrl());
			} else if (re.getResult() == SearchResult.Failed) {
				resultData.add(" parserBrowser DetailUrl 失败:" + book.getDetailUrl());
			} else if (re.getResult() != SearchResult.Failed) {
				resultData.add(" parserBrowser DetailUrl 成功， 名字：" + (re.getBook() != null ? re.getBook().getName() : "") + " 章节数： "
						+ getChapterTip(re.getChapters()));
			}
		}
		if (Util.isEmpty(book.getDirectoryUrl())) {
			resultData.add(" parserBrowser getDirectoryUrl is null");
		} else {
			BookAndChapters re = tool.parserBrowser(book.getDirectoryUrl(), null, null);
			if (re == null) {
				resultData.add(" parserBrowser DirectoryUrl 不匹配 ：" + book.getDirectoryUrl());
			} else if (re.getResult() == SearchResult.Failed) {
				resultData.add(" parserBrowser DirectoryUrl 失败:" + book.getDirectoryUrl());
			} else if (re.getResult() != SearchResult.Failed) {
				resultData.add(" parserBrowser DirectoryUrl 成功， 名字：" + (re.getBook() != null ? re.getBook().getName() : "") + " 章节数： "
						+ getChapterTip(re.getChapters()));
			}
		}
	}

	private static BookEntity copy(BookEntity book) {
		BookEntity copy = new BookEntity();
		copy.setDetailUrl(book.getDetailUrl());
		copy.setDirectoryUrl(book.getDirectoryUrl());
		return copy;
	}

	private static String shortString(String msg) {
		if (Util.isEmpty(msg))
			return "";
		int l = msg.length();
		return l + "字：" + msg.substring(0, Math.min(l, 20)).replace("\n", "");
	}

	private static String getChapterTip(List<ChapterEntity> chapters) {
		if (chapters == null || chapters.size() == 0)
			return "";
		ChapterEntity e = chapters.get(0);
		return chapters.size() + " first=" + e.getName() + "  " + e.getUrl();
	}
}
