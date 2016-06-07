package com.lqy.abook.parser.site;

import java.util.ArrayList;
import java.util.List;

import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;
import com.lqy.abook.tool.WebServer;

public class ParserPic extends ParserOther {

	public List<String> parserUrl(BookEntity book, String url) {
		try {
			String cookie = book.getExt() != null ? book.getExt().getCookie() : null;
			String type = book.getExt() != null ? book.getExt().getEncodeType() : null;
			String html = WebServer.getDataOnCookie(url, cookie, type);

			return parserImgs(html);
		} catch (Exception e) {
			MyLog.e(e);
		}

		return null;
	}

	public List<String> parserImgs(String html) {
		try {
			SimpleNodeIterator iterator = parseHtml(html, new NodeClassFilter(ImageTag.class));
			List<String> urls = new ArrayList<String>();
			while (iterator.hasMoreNodes()) {
				ImageTag node = (ImageTag) iterator.nextNode();
				if (!Util.isEmpty(node.getImageURL()) && node.getImageURL() == node.extractImageLocn()) {
					urls.add(node.getImageURL());
				}
			}
			MyLog.i("ParserOther  parserImgs ok  " + urls.size());
			return urls;
		} catch (Exception e) {
			MyLog.e(e);
		}

		return null;
	}
}
