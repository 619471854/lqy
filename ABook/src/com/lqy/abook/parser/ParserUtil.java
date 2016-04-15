package com.lqy.abook.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;

public abstract class ParserUtil {
	protected String encodeType;

	protected SimpleNodeIterator getParserResult(String url, String reg) throws Exception {
		return parseUrl(url, createEqualFilter(reg), encodeType);
	}

	protected static SimpleNodeIterator parseHtml(String html, NodeFilter filter) throws Exception {
		Parser parser = new Parser(html);
		return parser.parse(filter).elements();
	}

	protected static SimpleNodeIterator parseUrl(String url, NodeFilter filter, String encodeType) throws Exception {
		Parser parser = new Parser(url);
		parser.setEncoding(encodeType);
		return parser.parse(filter).elements();
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
}
