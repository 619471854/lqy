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

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.htmlparser.Parser;
import org.htmlparser.lexer.Page;

import com.lqy.abook.parser.ParserUtil;

public class WebServer {

	public static String getDataByParser(String url, String encodeType) throws Exception {
		URLConnection conn = Parser.getConnectionManager().openConnection(url);
		conn.connect();
		return getConnectionResult(conn, encodeType);
	}

	public static String hcGetData(String url, String encodeType) throws Exception {
		try {
			if (Util.isEmpty(encodeType))
				encodeType = HTTP.UTF_8;

			HttpGet request = new HttpGet(url);
			HttpClient httpClient = new DefaultHttpClient();
			addHeader(httpClient.getParams(), request, url, encodeType);
			HttpResponse response = httpClient.execute(request);

			int code = response.getStatusLine().getStatusCode();
			if (200 == code) {
				return EntityUtils.toString(response.getEntity(), encodeType);
			} else {
				throw new Exception("没有网路  " + code);
			}
		} catch (OutOfMemoryError e) {
			throw new Exception("内存溢出");
		}
	}

	private static void addHeader(HttpParams params, HttpUriRequest request, String url, String encodeType) {
		params.setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONSTANT.CONNECTION_TIMEOUT);
		params.setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		params.setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, encodeType);

		request.addHeader("accept", "*/*");
		request.addHeader("connection", "Keep-Alive");
		request.addHeader("User-Agent", CONSTANT.CHROME_USER_AGENT);
		request.addHeader("Referer", url);
	}

	private static void addHeader(HttpURLConnection conn, String url, String cookie) {
		conn.setReadTimeout(CONSTANT.CONNECTION_TIMEOUT);
		conn.setConnectTimeout(CONSTANT.CONNECTION_TIMEOUT);
		conn.setRequestProperty("accept", "*/*");
		conn.setRequestProperty("connection", "Keep-Alive");
		conn.setRequestProperty("User-Agent", CONSTANT.CHROME_USER_AGENT_PC);
		conn.setRequestProperty("Referer", url);
		if (!Util.isEmpty(cookie))
			conn.setRequestProperty("Cookie", cookie);
		// conn.setRequestProperty("Content-Encoding", "gzip,deflate");
	}

	/**
	 * Get请求，获得返回数据,gzip可能会出错http://files.qidian.com/Author2/1445033/26694962.
	 * txt
	 */
	public static String getDataByUrlConnection(String url, String encodeType) throws Exception {
		return getDataOnCookie(url, null, encodeType);
	}

	public static String getDataOnCookie(String url, String cookie, String encodeType) throws Exception {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			addHeader(conn, url, cookie);

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
	public static String postData(String url, String param, String encodeType) throws Exception {
		PrintWriter out = null;
		try {
			// 打开和URL之间的连接
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setReadTimeout(CONSTANT.CONNECTION_TIMEOUT);
			conn.setConnectTimeout(CONSTANT.CONNECTION_TIMEOUT);
			// 设置通用的请求属性
			conn.setRequestMethod("POST");
			addHeader(conn, url, null);
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

	private static void printResponseHeader(URLConnection http) throws UnsupportedEncodingException {
		Map<String, String> header = getHttpResponseHeader(http);
		for (Map.Entry<String, String> entry : header.entrySet()) {
			String key = entry.getKey() != null ? entry.getKey() + ":" : "";
			MyLog.i(key + "==" + entry.getValue());
		}
	}

	private static Map<String, String> getHttpResponseHeader(URLConnection http) throws UnsupportedEncodingException {
		Map<String, String> header = new LinkedHashMap<String, String>();
		for (int i = 0;; i++) {
			String mine = http.getHeaderField(i);
			if (mine == null)
				break;
			header.put(http.getHeaderFieldKey(i), mine);
		}
		return header;
	}

	public static String getConnectionResult(URLConnection conn, String encodeType) throws Exception {
		// printResponseHeader(conn);
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
			is = new InflaterInputStream(conn.getInputStream(), new Inflater(true));
		} else {
			is = conn.getInputStream();
		}

		return convertToString(is, encodeType);
	}

	public static String convertToString(InputStream is, String encodeType) throws Exception {
		// ByteArrayOutputStream bos = new ByteArrayOutputStream();
		// byte[] buffer = new byte[1024];
		// int length = -1;
		// while ((length = is.read(buffer)) != -1) {
		// bos.write(buffer, 0, length);
		// }
		// return new String(bos.toByteArray(), encodeType);
		StringBuffer string = new StringBuffer();
		BufferedReader reader = null;

		if (Util.isEmpty(encodeType))
			reader = new BufferedReader(new InputStreamReader(is));
		else
			reader = new BufferedReader(new InputStreamReader(is, encodeType));
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

	/**
	 * post方法
	 */
	protected static String hcPostData(String url, PostParams params, String encodeType) throws Exception {
		HttpPost post = new HttpPost(url);
		if (params != null) {
			post.setEntity(new UrlEncodedFormEntity(params.getList(), HTTP.UTF_8));
		}
		HttpClient httpClient = new DefaultHttpClient();
		addHeader(httpClient.getParams(), post, url, encodeType);

		HttpResponse response = httpClient.execute(post);
		int code = response.getStatusLine().getStatusCode();
		if (200 == code) {
			String re = EntityUtils.toString(response.getEntity(), encodeType);
			if (Util.isEmpty(re)) {
				throw new Exception("hcPostData：没有数据");
			}
			return re;
		} else {
			throw new Exception("hcPostData：code=" + code);
		}
	}
}
