package com.lqy.abook.parser;

import java.net.URI;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public abstract class ParserUtil {
	protected String encodeType;

	protected SimpleNodeIterator getParserResult(String url, String reg) throws Exception {
		return parseUrl(url, createEqualFilter(reg), encodeType);
	}

	protected static SimpleNodeIterator parseHtml(String html, NodeFilter filter) throws Exception {
		Parser parser = Parser.createParser(html, null);
		return parser.parse(filter).elements();
	}

	protected static SimpleNodeIterator parseUrl(String url, NodeFilter filter, String encodeType) throws Exception {
		Parser parser = new Parser(url);
		parser.setEncoding(encodeType);
		return parser.parse(filter).elements();
	}

	// 获取域名
	protected static String getDomain(String url) {
		String baseUrl = null;
		try {
			String path = new URI(url).getPath();
			int index = url.indexOf(path);
			if ("/".equals(path) || index < 1)
				baseUrl = url;
			else
				baseUrl = url.substring(0, index);
		} catch (Exception e2) {
		}
		if (Util.isEmpty(baseUrl)) {
			baseUrl = url.replace("http://", CONSTANT.EMPTY).replace("https://", CONSTANT.EMPTY);
			int index = baseUrl.indexOf("/");
			if (index == -1) {
				baseUrl = CONSTANT.EMPTY;
			} else {
				baseUrl = url.substring(0, url.length() - (baseUrl.length() - index));
			}
		}
		MyLog.i("getDomain= " + baseUrl);
		return baseUrl;
	}

	protected static String addDomain(String baseUrl, String url) {
		if (!Util.isEmpty(baseUrl) && !Util.isEmpty(url) && !url.startsWith("http")) {
			if (url.startsWith("/")) {
				url = baseUrl + url;
			} else {
				url = baseUrl + "/" + url;
			}
		}
		return url;
	}

	protected static Node parseNodeByUrl(String url, NodeFilter filter, String encodeType) {
		try {
			SimpleNodeIterator iterator = parseUrl(url, filter, encodeType);
			if (iterator.hasMoreNodes()) {
				return iterator.nextNode();
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	protected static Node parseNodeByHtml(String html, NodeFilter filter) {
		try {
			SimpleNodeIterator iterator = parseHtml(html, filter);
			if (iterator.hasMoreNodes()) {
				return iterator.nextNode();
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	public static String toHtml(Node node) {
		return node == null ? null : node.toHtml();
	}

	public static String matcher(String html, String reg) {
		try {
			Pattern p = Pattern.compile(reg);
			Matcher m = p.matcher(html);
			if (m.find()) {
				String result = m.group(1);
				// MyLog.i(result);
				return result;
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return CONSTANT.EMPTY;
	}

	protected NodeFilter createEqualFilter(final String reg) {
		return new NodeFilter() {
			public boolean accept(Node node) {
				return node.getText().equals(reg);
			}
		};
	}

	protected NodeFilter createStartFilter(final String reg) {
		return new NodeFilter() {
			public boolean accept(Node node) {
				return node.getText().startsWith(reg);
			}
		};
	}

	protected static String matcher(String html, String reg, int count) {
		try {
			Pattern p = Pattern.compile(reg);
			Matcher m = p.matcher(html);
			if (m.find()) {
				return m.group(count);
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return CONSTANT.EMPTY;
	}

	protected static Matcher getMatcher(String html, String reg) {
		try {
			Pattern p = Pattern.compile(reg);
			Matcher m = p.matcher(html);
			if (m.find()) {
				return m;
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return null;
	}

	protected static int getMatcherCount(String html, String reg) {
		int count = 0;
		try {
			Pattern p = Pattern.compile(reg);
			Matcher m = p.matcher(html);
			while (m.find()) {
				count += m.group(1).trim().length();
			}
		} catch (Exception e) {
			MyLog.e(e);
		}
		return count;
	}

}
