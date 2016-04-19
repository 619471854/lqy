package com.lqy.abook.tool;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MatcherTool {

	/**
	 * 去除html标签
	 */
	public static String ClearHtmlTag(String html) {

		if (Util.isEmpty(html))
			return CONSTANT.EMPTY;
		// 先排除空格和回车
		html = html.replaceAll("<br\\s*/>", "\n");
		html = html.replaceAll("&nbsp;", " ");
		// 去除html标签
		String htmlStr = html;
		String textStr = CONSTANT.EMPTY;
		Pattern p_script;
		Matcher m_script;
		Pattern p_style;
		Matcher m_style;
		Pattern p_html;
		Matcher m_html;

		String regEx_script = "<[//s]*?script[^>]*?>[//s//S]*?<[//s]*?///[//s]*?script[//s]*?>";
		String regEx_style = "<[//s]*?style[^>]*?>[//s//S]*?<[//s]*?///[//s]*?style[//s]*?>";
		String regEx_html = "<[^>]+>";

		p_script = Pattern.compile(regEx_script, Pattern.CASE_INSENSITIVE);
		m_script = p_script.matcher(htmlStr);
		htmlStr = m_script.replaceAll(CONSTANT.EMPTY);

		p_style = Pattern.compile(regEx_style, Pattern.CASE_INSENSITIVE);
		m_style = p_style.matcher(htmlStr);
		htmlStr = m_style.replaceAll(CONSTANT.EMPTY);

		p_html = Pattern.compile(regEx_html, Pattern.CASE_INSENSITIVE);
		m_html = p_html.matcher(htmlStr);
		htmlStr = m_html.replaceAll(CONSTANT.EMPTY);

		textStr = htmlStr.replaceAll("&nbsp;", CONSTANT.EMPTY);

		return textStr;

	}

	/**
	 * 文件url地址是否有后缀名
	 */
	public static boolean hasExtendName(String url) {
		Pattern p = Pattern.compile("[^/\\?\\s\\.].+\\.[^\\.\\s]{1,5}$");
		Matcher m = p.matcher(url);
		return m.find();
	}
}
