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

			MyLog.i("parserUrl ok  " + html.length());
			return parserImgs(url, html);
		} catch (Exception e) {
			MyLog.e(e);
		}

		return null;
	}

	public List<String> parserImgs(String url, String html) {
		try {
			SimpleNodeIterator iterator = parseHtml(html, new NodeClassFilter(ImageTag.class));
			// 获取域名
			String baseUrl = getDomain(url);
			List<String> urls = new ArrayList<String>();
			while (iterator.hasMoreNodes()) {
				ImageTag node = (ImageTag) iterator.nextNode();
				String imgUrl = node.getImageURL();
				if (!Util.isEmpty(imgUrl) && imgUrl.equals(node.extractImageLocn()) && !imgUrl.equals(".gif")) {
					urls.add(addDomain(url,baseUrl, imgUrl));
				}
			}
			MyLog.i("parserImgs  parserImgs ok  " + urls.size());
			return urls;
		} catch (Exception e) {
			MyLog.e(e);
		}

		return null;
	}
}
