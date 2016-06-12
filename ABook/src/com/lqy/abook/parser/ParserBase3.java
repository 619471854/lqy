package com.lqy.abook.parser;

import java.util.ArrayList;
import java.util.List;

import org.htmlparser.Node;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.LoadStatus;
import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public abstract class ParserBase3 extends ParserBase2 {
	@Override
	public boolean updateBook(BookEntity book) {
		return false;
	}


	@Override
	public boolean parserBookDetail(BookEntity book) {
		Node node = parseNodeByUrl(book.getDirectoryUrl(), createEqualFilter("div id=\"intro\""), "gbk");
		String html = toHtml(node);
		if (html != null) {
			book.setTip(html.replaceAll(Config.tagReg, CONSTANT.EMPTY).replaceAll("\\s", CONSTANT.EMPTY));
			return true;
		}
		return false;
	}

	@Override
	public List<ChapterEntity> parserBookDict(String url) {
		try {
			List<ChapterEntity> chapters = new ArrayList<ChapterEntity>();
			SimpleNodeIterator iterator = parseUrl(url, createEqualFilter("div id=\"list\""), "gbk");
			MyLog.i("ParserShuyue parserBookDict getParserResult ok");
			if (iterator.hasMoreNodes()) {
				String html = iterator.nextNode().toHtml();
				return parserBookDictByHtml(url, html);
			}
			return chapters;
		} catch (Exception e) {
			MyLog.e(e);
			return null;
		}
	}

	public static List<ChapterEntity> parserBookDictByHtml(String urlRoot, String h) {
		try {
			List<ChapterEntity> chapters = new ArrayList<ChapterEntity>();
			SimpleNodeIterator iterator = parseHtml(h, new NodeClassFilter(LinkTag.class));

			ChapterEntity e;
			while (iterator.hasMoreNodes()) {
				LinkTag node = (LinkTag) iterator.nextNode();
				e = new ChapterEntity();

				e.setName(node.getLinkText() == null ? CONSTANT.EMPTY : node.getLinkText().trim());
				e.setUrl(urlRoot + node.getLink());
				e.setId(chapters.size());
				if (!Util.isEmpty(e.getName()))
					chapters.add(e);
			}
			return chapters;
		} catch (Exception e) {
			MyLog.e(e);
			return null;
		}
	}

}
