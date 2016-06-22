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

	protected static SimpleNodeIterator parseIterator(String url, String html, NodeFilter filter, String encodeType) throws Exception {
		return Util.isEmpty(html) ? parseUrl(url, filter, encodeType) : parseHtml(html, filter);
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

	protected static Node parseNode(String url, String html, NodeFilter filter, String encodeType) {
		return Util.isEmpty(html) ? parseNodeByUrl(url, filter, encodeType) : parseNodeByHtml(html, filter);
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

	/**
	 * 获取子页面地址：如果地址以‘/’开头，则加上domain，否则加上当前页面地址
	 */
	protected static String addDomain(String currentUrl, String domain, String childUrl) {
		if (Util.isEmpty(childUrl))
			return null;
		if (childUrl.startsWith("http"))
			return childUrl;
		if (childUrl.startsWith("/") && !Util.isEmpty(domain)) {
			if (!Util.isEmpty(domain)) {
				childUrl = domain + childUrl;
			} else {
				if (currentUrl.endsWith("/"))
					childUrl = currentUrl + childUrl.substring(0);
				else
					childUrl = currentUrl + childUrl;
			}
		} else {
			if (currentUrl.endsWith(".html")) {
				childUrl = currentUrl.substring(0, currentUrl.lastIndexOf("/") + 1) + childUrl;
			} else if (currentUrl.endsWith("/"))
				childUrl = currentUrl + childUrl;
			else
				childUrl = currentUrl + "/" + childUrl;
		}
		return childUrl;
	}

	public static String toHtml(Node node) {
		return node == null ? null : node.toHtml();
	}

	public static String matcher(String html, String reg) {
		if (reg == null)
			return CONSTANT.EMPTY;
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
				return reg.equals(node.getText());
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
		if (reg == null)
			return CONSTANT.EMPTY;
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
		if (reg == null)
			return null;
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
		if (reg == null)
			return 0;
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
