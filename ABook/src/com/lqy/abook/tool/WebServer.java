package com.lqy.abook.tool;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.params.CoreProtocolPNames;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class WebServer {

	/**
	 * Get请求，获得返回数据
	 */
	public static String getData(String url) throws Exception {
		try {
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setReadTimeout(CONSTANT.CONNECTION_TIMEOUT);
			conn.setConnectTimeout(CONSTANT.CONNECTION_TIMEOUT);
			conn.setRequestMethod("GET");
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestProperty("User-Agent", CONSTANT.CHROME_USER_AGENT);
			conn.setRequestProperty("Referer", url);

			// conn.setRequestProperty("Host", "sosu.qidian.com");
			// conn.setRequestProperty("X-Requested-With", "XMLHttpRequest");

			if (conn.getResponseCode() == 200) {
				return convertToString(conn.getInputStream());
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
	public static String postData(String url, String param) throws Exception {
		PrintWriter out = null;
		try {
			// 打开和URL之间的连接
			HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
			// 设置通用的请求属性
			conn.setRequestProperty("accept", "*/*");
			conn.setRequestProperty("connection", "Keep-Alive");
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setRequestProperty("charset", "utf-8");
			conn.setRequestProperty("User-Agent", CONSTANT.CHROME_USER_AGENT);
			conn.setUseCaches(false);
			// 发送POST请求必须设置如下两行
			conn.setDoOutput(true);
			conn.setDoInput(true);
			conn.setReadTimeout(CONSTANT.CONNECTION_TIMEOUT);
			conn.setConnectTimeout(CONSTANT.CONNECTION_TIMEOUT);

			if (param != null && !param.trim().equals("")) {
				// 获取URLConnection对象对应的输出流
				out = new PrintWriter(conn.getOutputStream());
				// 发送请求参数
				out.print(param);
				// flush输出流的缓冲
				out.flush();
			}
			return convertToString(conn.getInputStream());
		} finally {
			try {
				out.close();
			} catch (Exception e) {
			}
		}
	}

	public static String convertToString(InputStream is) throws Exception {
		StringBuffer string = new StringBuffer();
		BufferedReader reader = new BufferedReader(new InputStreamReader(is));
		String line;
		try {
			while ((line = reader.readLine()) != null) {
				string.append(line + "\n");
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

	private static HttpClient getHttpClient() {
		HttpClient httpClient = new DefaultHttpClient();
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONSTANT.CONNECTION_TIMEOUT);

		// 设置协议版本
		httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, "utf-8");

		return httpClient;
	}

	/**
	 * get方法
	 */
	public static String hcGetData(String url, String charset) throws Exception {
		try {
			if (Util.isEmpty(charset))
				charset = HTTP.UTF_8;

			HttpGet request = new HttpGet(url);
			request.addHeader("User-Agent", CONSTANT.CHROME_USER_AGENT);

			// 处理响应
			HttpResponse response = getHttpClient().execute(request);
			int code = response.getStatusLine().getStatusCode();
			if (200 == code) {
				return EntityUtils.toString(response.getEntity(), charset);
			} else {
				throw new Exception("没有网路  " + code);
			}
		} catch (OutOfMemoryError e) {
			throw new Exception("内存溢出");
		}
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
		httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, CONSTANT.CONNECTION_TIMEOUT);
		httpClient.getParams().setParameter(CoreProtocolPNames.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
		httpClient.getParams().setParameter(CoreProtocolPNames.HTTP_CONTENT_CHARSET, encodeType);

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
