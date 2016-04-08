package com.lqy.abook.tool;

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
