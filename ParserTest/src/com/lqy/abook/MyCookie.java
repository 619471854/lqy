package com.lqy.abook;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.util.List;

public class MyCookie {

	static CookieManager manager = new CookieManager();

	public static void storecoo(URI uri, String strcoo) {

		// 创建一个默认的 CookieManager

		// 将规则改掉，接受所有的 Cookie
		manager.setCookiePolicy(CookiePolicy.ACCEPT_ALL);

		// 保存这个定制的 CookieManager
		CookieHandler.setDefault(manager);

		// 接受 HTTP 请求的时候，得到和保存新的 Cookie
		HttpCookie cookie = new HttpCookie("Cookie: ", strcoo);
		// cookie.setMaxAge(60000);//没这个也行。
		manager.getCookieStore().add(uri, cookie);
	}

	public static HttpCookie getcookies() {

		HttpCookie res = null;
		// 使用 Cookie 的时候：
		// 取出 CookieStore
		CookieStore store = manager.getCookieStore();

		// 得到所有的 URI
		List<URI> uris = store.getURIs();
		for (URI ur : uris) {
			// 筛选需要的 URI
			// 得到属于这个 URI 的所有 Cookie
			List<HttpCookie> cookies = store.get(ur);
			for (HttpCookie coo : cookies) {
				res = coo;
			}
		}
		return res;
	}
}
