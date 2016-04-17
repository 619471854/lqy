package com.lqy.abook.tool;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.Inflater;
import java.util.zip.InflaterInputStream;

import org.htmlparser.Parser;
import org.htmlparser.http.ConnectionManager;
import org.htmlparser.http.Cookie;
import org.htmlparser.lexer.Page;

import com.lqy.abook.parser.ParserUtil;

public class WebServer {

	/**
	 * get方法
	 */

	public static String getDataByParser(String url, String encodeType)
			throws Exception {
		ConnectionManager c = Parser.getConnectionManager();
		//c.setCookieProcessingEnabled(true);
		URLConnection conn = c.openConnection(url);
		conn.connect();
		return getConnectionResult(conn, encodeType);
	}

	public static String hcGetData(String url, String encodeType)
			throws Exception {
		return getDataByUrlConnection(url, encodeType);
	}

	private static void addHeader(HttpURLConnection conn, String url) {
		conn.setReadTimeout(CONSTANT.CONNECTION_TIMEOUT);
		conn.setConnectTimeout(CONSTANT.CONNECTION_TIMEOUT);
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("connection", "Keep-Alive");conn.setRequestProperty("Referer", url);
		conn.setRequestProperty("Referer", url);
		
		conn.setRequestProperty(
				"User-Agent",
				"Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.97 Safari/537.36");
		conn.setRequestProperty(
				"Cookie",
				"cdb_cookietime=2592000; cdb_auth=%2BqkCkRVFBnRnBJKSedmBxL4g4EhXTIHtJHZ3MXFvYrxItjr6kjYq7lv6MfNMIADw5w; cdb_visitedfid=11; cdb_oldtopics=D1087230D699783D; cdb_fid11=1460810158; cdb_sid=z7m67E ");

		// conn.setReadTimeout(CONSTANT.CONNECTION_TIMEOUT);
		// conn.setConnectTimeout(CONSTANT.CONNECTION_TIMEOUT);
		// conn.setRequestProperty("accept",
		// "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		// conn.setRequestProperty("connection", "Keep-Alive");
		// conn.setRequestProperty("Accept-Language", "zh-CN,zh;q=0.8");
		// conn.setRequestProperty("Cache-Control", "max-age=0");
		// conn.setRequestProperty("Upgrade-Insecure-Requests", "1");
		// conn.setRequestProperty("User-Agent",
		// "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.97 Safari/537.36");
		// conn.setRequestProperty("Referer",
		// "http://www.mayaname.com/forumdisplay.php?fid=11");
		// conn.setRequestProperty("Host", "www.mayaname.com");
		// conn.setRequestProperty("Accept-Encoding", "gzip, deflate, sdch");
		// conn.setRequestProperty("Referer",
		// "http://www.mayaname.com/forumdisplay.php?fid=11&page=1");

	}

	/**
	 * Get请求，获得返回数据,gzip可能会出错http://files.qidian.com/Author2/1445033/26694962.
	 * txt
	 */
	public static String getDataByUrlConnection(String url, String encodeType)
			throws Exception {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(url)
					.openConnection();
			conn.setRequestMethod("GET");
			addHeader(conn, url);

			conn.connect();
			if (conn.getResponseCode() == 200) {
				return getConnectionResult(conn, encodeType);
			} else {
				throw new Exception("没有网路  " + conn.getResponseCode());
			}
		} catch (OutOfMemoryError e) {
			throw new Exception("内存溢出");
		}
	}

	/**
	 * 向指定 URL 发送POST方法的请求请求参数应该是 name1=value1&name2=value2 的形式。
	 */
	public static String postData(String url, String param, String encodeType)
			throws Exception {
		PrintWriter out = null;
		try {
			// 打开和URL之间的连接
			HttpURLConnection conn = (HttpURLConnection) new URL(url)
					.openConnection();
			conn.setReadTimeout(CONSTANT.CONNECTION_TIMEOUT);
			conn.setConnectTimeout(CONSTANT.CONNECTION_TIMEOUT);
			// 设置通用的请求属性
			conn.setRequestMethod("POST");
			addHeader(conn, url);
			conn.setUseCaches(false);
			conn.setDoOutput(true);
			conn.setDoInput(true);

			if (param != null && !param.trim().equals("")) {
				// 获取URLConnection对象对应的输出流
				out = new PrintWriter(conn.getOutputStream());
				// 发送请求参数
				out.print(param);
				// flush输出流的缓冲
				out.flush();
			}
			// printResponseHeader(conn);
			conn.connect();
			return getConnectionResult(conn, encodeType);
		} finally {
			try {
				out.close();
			} catch (Exception e) {
			}
		}
	}

	private static void printResponseHeader(URLConnection http)
			throws UnsupportedEncodingException {
		Map<String, String> header = getHttpResponseHeader(http);
		for (Map.Entry<String, String> entry : header.entrySet()) {
			String key = entry.getKey() != null ? entry.getKey() + ":" : "";
			MyLog.i(key + "==" + entry.getValue());
		}
	}

	private static Map<String, String> getHttpResponseHeader(URLConnection http)
			throws UnsupportedEncodingException {
		Map<String, String> header = new LinkedHashMap<String, String>();
		for (int i = 0;; i++) {
			String mine = http.getHeaderField(i);
			if (mine == null)
				break;
			header.put(http.getHeaderFieldKey(i), mine);
		}
		return header;
	}

	public static String getConnectionResult(URLConnection conn,
			String encodeType) throws Exception {
		printResponseHeader(conn);
		// 获取编码格式
		String charset = conn.getHeaderField("Content-Type");
		charset = ParserUtil.matcher(charset, "charset=['\"]?([^'\";]+)['\"]?");
		encodeType = Page.findCharset(charset, encodeType);

		InputStream is;
		String encode = conn.getContentEncoding();
		if (Util.isEmpty(encode)) {
			is = conn.getInputStream();
		} else if (encode.toLowerCase().contains("gzip")) {
			is = new GZIPInputStream(conn.getInputStream());
		} else if (encode.toLowerCase().contains("deflate")) {
			is = new InflaterInputStream(conn.getInputStream(), new Inflater(
					true));
		} else {
			is = conn.getInputStream();
		}

		return convertToString(is, encodeType);
	}

	public static String convertToString(InputStream is, String encodeType)
			throws Exception {
		// ByteArrayOutputStream bos = new ByteArrayOutputStream();
		// byte[] buffer = new byte[1024];
		// int length = -1;
		// while ((length = is.read(buffer)) != -1) {
		// bos.write(buffer, 0, length);
		// }
		// return new String(bos.toByteArray(), encodeType);
		StringBuffer string = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is,
				encodeType));
		try {
			String line = reader.readLine();
			while (line != null) {
				string.append(line + "\n");
				line = reader.readLine();
			}
		} finally {
			try {
				is.close();
			} catch (Exception e) {
			}
			try {
				reader.close();
			} catch (Exception e) {
			}
		}
		return string.toString();
	}

}
