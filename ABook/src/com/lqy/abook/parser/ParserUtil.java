package com.lqy.abook.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.util.SimpleNodeIterator;

import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.WebServer;

public abstract class ParserUtil {
	protected String encodeType;

	protected SimpleNodeIterator getParserResult(String url, final String reg) throws Exception {
		NodeFilter filter = new NodeFilter() {
			public boolean accept(Node node) {
				return node.getText().equals(reg);
			}
		};
		return getParserResult(url, filter, encodeType);
	}

	protected SimpleNodeIterator getParserResult2(String url, final String reg) throws Exception {
		NodeFilter filter = new NodeFilter() {
			public boolean accept(Node node) {
				return node.getText().startsWith(reg);
			}
		};
		return getParserResult(url, filter, encodeType);
	}

	protected SimpleNodeIterator getParserResult3(String url, final String reg) throws Exception {
		String html = WebServer.hcGetData(url, encodeType);
		//html = matcher(html, "(<body>[\\s\\S]+</body>)");
		NodeFilter filter = new NodeFilter() {
			public boolean accept(Node node) {
				return node.getText().startsWith(reg);
			}
		};
		Parser parser = new Parser(html);
		return parser.parse(filter).elements();
	}

	protected static SimpleNodeIterator getParserResult(String url, NodeFilter filter, String encodeType) throws Exception {
		// 生成一个解析器对象，用网页的 url 作为参数
		Parser parser = new Parser(url);
		// 设置网页的编码
		parser.setEncoding(encodeType);
		return parser.parse(filter).elements();
	}

	protected static String matcher(String html, String reg) {
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
