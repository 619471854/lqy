package com.lqy.abook.tool;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

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
import org.json.JSONObject;

import com.lqy.abook.entity.ResultEntity;

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
	public static ResultEntity hcGetData(String url, String charset) {
		ResultEntity result = null;
		try {
			HttpGet request = new HttpGet(url);
			// 处理响应
			HttpResponse response = getHttpClient().execute(request);
			int code = response.getStatusLine().getStatusCode();
			if (200 == code) {
				if (Util.isEmpty(charset))
					charset = HTTP.UTF_8;
				String value = EntityUtils.toString(response.getEntity(), charset);
				result = ResultEntity.success(value);
			} else if (NetworkUtils.isNetConnected(null)) {
				result = ResultEntity.error("服务器错误  " + code);
			} else {
				result = ResultEntity.error("没有网路  " + code);
			}

		} catch (OutOfMemoryError e) {
			result = ResultEntity.error("内存溢出");
		} catch (Exception e) {// ConnectTimeoutException:找不到服务器//HttpHostConnectException：没有网络
			if (NetworkUtils.isNetConnected(null)) {
				result = ResultEntity.error("服务器错误  " + e.toString());
			} else {
				result = ResultEntity.error("没有网路  " + e.toString());
			}
		}
		MyLog.i(url + "----------------------------------" + result.toString());
		return result;
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
