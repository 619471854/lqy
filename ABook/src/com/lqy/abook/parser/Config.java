package com.lqy.abook.parser;

public class Config {

	public static final String tagReg = "<[^>]+>";// 去标签
	public static final String lineWrapReg = "<br\\s*/?>";// 换行
	public static final String lineWrapReg2 = "<p>";// 换行
	public static final String nbsp = "&nbsp;";// 替换空格
	public static final String amp = "&amp;";// 替换&
	public static final String blank = "　";// 全角空格

	public String searchUrl;
	public String searchFilter;
	public String detailUrlReg;
	public String coverReg;
	public String directoryUrlReg;
	public String nameReg;
	public String keyReg;// 搜索到的关键词
	public String authorReg;
	public String typeReg;
	public String wordsReg;
	public String tipsReg;
	public String tipsDetailReg;
	public String newChapterReg;
	public String updateTimeReg;
	public String completedReg;
	public String newChapterReg2;// 详情界面的
	public String updateTimeReg2;
	public String wordsReg2;

	private static Config _17K;

	public static Config get17KConfig() {
		if (_17K == null) {
			_17K = new Config();
			_17K.searchUrl = "http://search.17k.com/search.xhtml?c.st=0&c.q=";
			_17K.searchFilter = "div class=\"textlist\"";
			_17K.detailUrlReg = "textleft\">\\s*<a\\s*href=\"([^\"]+)\"";
			_17K.coverReg = "<img\\s*src=\"([^\"]+)\"\\s*/>";
			_17K.directoryUrlReg = "<a\\s*href=\"(\\S+)\"\\s*target=\"_blank\"\\s*>在线阅读</a>";
			_17K.nameReg = "<div\\s*class=\"textmiddle\">[^a]+<a[^>]+>(((?!</a>)[\\s\\S])+)</a>";
			_17K.keyReg = "color=\"red\"";// 搜索到的关键词
			_17K.authorReg = "class=\"ls\"[^<]+<a[^>]+>(.+)</a></span>\\s*<span>类别";
			_17K.typeReg = "<span>类别：<a[^>]+>(\\S+)</a></span>[\\s\\S]+字数";
			_17K.wordsReg = "字数：\\s*<code>(\\d+)</code>";
			_17K.tipsReg = "<li><strong>简介：</strong>\\s*<p>\\s*<a[^>]+>([\\s\\S]+)</a>\\s*</p>\\s*</li>[\\s\\S]+最近更新";
			_17K.tipsDetailReg = "<a[^>]+>(((?!</a>).)+)</a>";
			_17K.newChapterReg = "最近更新：</font>\\s*<a[^>]+>([^<]+)</a>";
			_17K.updateTimeReg = "<cite>([\\d-\\s:]+)</cite>";
			_17K.completedReg = "<font\\s*itemprop=\"updataStatus\">(此书已完成)</font>";
		}
		return _17K;
	}

	private static Config SM;

	public static Config getSMConfig() {
		if (SM == null) {
			SM = new Config();
			SM.searchUrl = "http://so.shenmaxiaoshuo.com/cse/search?s=1112742193063402114&q=";
			SM.searchFilter = "div class=\"result-item result-game-item\"";
			SM.detailUrlReg = null;// 通过directoryUrl查出
			SM.coverReg = "<img\\s*src=\"([^\"]+)\"";
			SM.directoryUrlReg = "<a\\s*cpos=\"img\"\\s*href=\"([^\"]+)\"";
			SM.nameReg = "<a\\s*cpos=\"title\"[^>]+>(((?!</a>)[\\s\\S])+)</a>";
			SM.keyReg = "<em>([^<]+)</em>";// 搜索到的关键词
			SM.authorReg = "作者：</span>\\s*<span>(((?!</span>)[\\s\\S])+)</span>";
			SM.typeReg = "<span\\s*class=\"result-game-item-info-tag-title\">([^<]+)</span>";
			SM.wordsReg = null;
			SM.tipsReg = "<p\\s*class=\"result-game-item-desc\">(((?!</p>)[\\s\\S])+)</p>";
			SM.tipsDetailReg = "<p\\s*class=\"intro\">([\\s\\S]+?)</p>";
			SM.newChapterReg = "<a\\s*cpos=\"newchapter\"[^>]+>([^<]+)</a>";
			SM.updateTimeReg = "<span\\s*class=\"result-game-item-info-tag-title\">([\\d-]+)</span>";
			SM.completedReg = "状态：(已完成)";
			SM.newChapterReg2 = "<a\\s*href=\"/ml-\\d+-\\d+/\">([\\s\\S]+?)</a>";
			SM.updateTimeReg2 = "最后更新：(\\d\\d\\d\\d-\\d\\d-\\d\\d)\\s*<div";
			SM.wordsReg2 = "字数：\\s*(\\d+)";
		}
		return SM;
	}

	private static Config _16K;

	public static Config get16KConfig() {
		if (_16K == null) {
			_16K = new Config();
			_16K.searchUrl = "http://www.16kxsw.com/modules/article/search.php";
			_16K.searchFilter = "tr";
			_16K.detailUrlReg = null;
			_16K.coverReg = "<img\\s*src=\"([^\"]+)\"\\s*alt";
			_16K.directoryUrlReg = "<td\\s*class=\"odd\"><a\\s*href=\"([^\"]+)\"";
			_16K.nameReg = "<td\\s*class=\"odd\"><a[^>]+>([^<]+)</a></td>";
			_16K.keyReg = null;// 搜索到的关键词
			_16K.authorReg = "<td\\s*class=\"odd\">([^<]+)</td>";
			_16K.typeReg = null;
			_16K.wordsReg = "<td\\s*class=\"even\">(\\d+)K</td>";
			_16K.tipsReg = null;
			_16K.tipsDetailReg = "<div\\s*class=\"introCon\">(((?!</div>)[\\s\\S])+)</div>";
			_16K.newChapterReg = "<td\\s*class=\"even\"><a[^>]+>([^<]+)</a></td>";
			_16K.updateTimeReg = "<td\\s*class=\"odd\"\\s*align=\"center\">(\\d\\d-\\d\\d-\\d\\d)</td>";
			_16K.completedReg = null;
			_16K.newChapterReg2 = "<strong>最新章节：</strong><a[^>]+>([^<]+)</a>";
			_16K.updateTimeReg2 = null;
			_16K.wordsReg2 = null;
		}
		return _16K;
	}

	private static Config baidu;

	public static Config getBaiduConfig() {
		if (baidu == null) {
			baidu = new Config();
			baidu.searchUrl = "http://dushu.baidu.com/searchresult?word=";
			baidu.searchFilter = "div class=\"item\"";
			baidu.detailUrlReg = null;
			baidu.coverReg = "<img\\s*class='cover-img\\s*lazy\'\\s*data-original=\"([^\"]+)\"";
			baidu.directoryUrlReg = null;
			baidu.nameReg = "<span\\s*class=\"header-title cut\">(((?!</div>)[\\s\\S])+)</span>\\s*</div>";
			baidu.keyReg = null;// 搜索到的关键词
			baidu.authorReg = "<span\\s*class=\"info-author\">(.+)</span>";
			baidu.typeReg = "<span\\s*class=\"info-cate cut\">(.+)</span>";
			baidu.wordsReg = null;
			baidu.tipsReg = "<div\\s*class=\"detail-intro cut\">\\s*<span>(((?!</span>)[\\s\\S])+)</span>\\s*</div>";
			baidu.tipsDetailReg = null;
			baidu.newChapterReg = null;
			baidu.updateTimeReg = null;
			baidu.completedReg = "<span\\s*class=\"info-status\">(.+)</span>";
		}
		return baidu;
	}

	private static Config qidian;

	public static Config getQidianConfig() {
		if (qidian == null) {
			qidian = new Config();
			qidian.searchUrl = "http://se.qidian.com/?kw=";
			qidian.searchFilter = "li data-rid=";
			qidian.detailUrlReg = null;
			qidian.coverReg = "<img\\s*src=\"([^\"]+)\"";
			qidian.directoryUrlReg = null;
			qidian.nameReg = "<h4>\\s*<a[^>]+>(((?!</a>)[\\s\\S])+)</a>\\s*</h4>";
			qidian.keyReg = null;// 搜索到的关键词
			qidian.authorReg = "<a\\s*class=\"name\"[^>]+>([^<]+)</a>";
			qidian.typeReg = null;
			qidian.wordsReg = "<span>\\s*([\\.\\d]+)万</span>\\s*总字数\\s*</p>";
			qidian.tipsReg = ">([^<]+)</a>\\s*<em>\\s*\\|\\s*</em>\\s*<span>([^<]+)</span>\\s*</p>\\s*<p\\s*class=\"intro\">([^<]+)</p>";
			qidian.tipsDetailReg = null;
			qidian.newChapterReg = "最新更新([^<]+)</a>\\s*<em>[^<]+</em>\\s*<span>([^<]+)(更新)?</span>";
			qidian.newChapterReg2 = "<b>最近更新：</b>\\s*<a.*?>(.+?)</a>";
			qidian.updateTimeReg = null;
			qidian.updateTimeReg2 = "<em\\s*class=\"time\">([^<]+)(更新)?</em>";
			qidian.completedReg = "";
		}
		return qidian;
	}

	private static Config shuyue;

	public static Config getShuyueConfig() {
		if (shuyue == null) {
			shuyue = new Config();
			shuyue.searchUrl = "http://zhannei.baidu.com/cse/search?s=2855222735713672313&q=";
			shuyue.searchFilter = "div class=\"result-item result-game-item\"";
			shuyue.detailUrlReg = null;
			shuyue.coverReg = "<img\\s*src=\"([^\"]+)\"\\s*alt";
			shuyue.directoryUrlReg = null;
			shuyue.nameReg = "<a\\s*cpos=\"title\"\\s*href=\"([^\"]+)\"\\s*title=\"([^\"]+)\"[^>]+>[^<]*(<em>(.*)</em>)?[^<]*</a>";//
			shuyue.keyReg = null;// 搜索到的关键词
			shuyue.authorReg = "作者：</span>\\s*<span>\\s*(((?!</span>)[\\s\\S])+)</span>";
			shuyue.typeReg = "类型：</span>\\s*<span\\s*class=\"result-game-item-info-tag-title\">([^<]+)</span>";
			shuyue.wordsReg = null;
			shuyue.tipsReg = "<p\\s*class=\"result-game-item-desc\">(((?!</p>)[\\s\\S])+)</p>";
			shuyue.tipsDetailReg = null;
			shuyue.newChapterReg = "<a\\s*cpos=\"newchapter\"[^>]+>([^<]+)</a>";
			shuyue.updateTimeReg = "更新时间：</span>\\s*<span\\s*class=\"result-game-item-info-tag-title\">(\\d\\d\\d\\d-\\d\\d-\\d\\d)</span>";
			shuyue.completedReg = null;
		}
		return shuyue;
	}

	private static Config _00ks;

	public static Config get00ksConfig() {
		if (_00ks == null) {
			_00ks = new Config();
			_00ks.searchUrl = "http://so.00ksw.com/cse/search?click=1&entry=1&s=10977942222484467615&nsid=&q=";
			_00ks.searchFilter = "div class=\"result-item result-game-item\"";
			_00ks.detailUrlReg = null;
			_00ks.coverReg = "<img\\s*src=\"([^\"]+)\"\\s*alt";
			_00ks.directoryUrlReg = null;
			_00ks.nameReg = "<a\\s*cpos=\"title\"\\s*href=\"([^\"]+)\"\\s*title=\"([^\"]+)\"[^>]+>[^<]*(<em>(.*)</em>)?[^<]*</a>";//
			_00ks.keyReg = null;// 搜索到的关键词
			_00ks.authorReg = "作者：</span>\\s*<span>\\s*(((?!</span>)[\\s\\S])+)</span>";
			_00ks.typeReg = "类型：</span>\\s*<span\\s*class=\"result-game-item-info-tag-title\">([^<]+)</span>";
			_00ks.wordsReg = null;
			_00ks.tipsReg = "<p\\s*class=\"result-game-item-desc\">(((?!</p>)[\\s\\S])+)</p>";
			_00ks.tipsDetailReg = null;
			_00ks.newChapterReg = "<a\\s*cpos=\"newchapter\"[^>]+>([^<]+)</a>";
			_00ks.updateTimeReg = "更新时间：</span>\\s*<span\\s*class=\"result-game-item-info-tag-title\">(\\d\\d\\d\\d-\\d\\d-\\d\\d)</span>";
			_00ks.completedReg = null;
		}
		return _00ks;
	}

	private static Config dsb;

	public static Config getdDsbConfig() {
		if (dsb == null) {
			dsb = new Config();
			dsb.searchUrl = "http://so.dashubao.co/cse/search?click=1&entry=1&s=17492266668774909517&nsid=&q=";
			dsb.searchFilter = "div class=\"result-item result-game-item\"";
			dsb.detailUrlReg = null;
			dsb.coverReg = "<img\\s*src=\"([^\"]+)\"\\s*alt";// 列表和详情界面都用的这个
			dsb.directoryUrlReg = null;
			dsb.nameReg = "<a\\s*cpos=\"title\"\\s*href=\"([^\"]+)\"\\s*title=\"([^\"]+)\"[^>]+>[^<]*(<em>(.*)</em>)?[^<]*</a>";//
			dsb.keyReg = null;// 搜索到的关键词
			dsb.authorReg = "作者：</span>\\s*<span>\\s*(((?!</span>)[\\s\\S])+)</span>";
			dsb.typeReg = "类型：</span>\\s*<span\\s*class=\"result-game-item-info-tag-title\">([^<]+)</span>";
			dsb.wordsReg2 = "<span>字数：(\\d+)</span>";
			dsb.tipsReg = "<p\\s*class=\"result-game-item-desc\">(((?!</p>)[\\s\\S])+)</p>";
			dsb.tipsDetailReg = "<div\\s*class=\"txtbox\">(((?!</div>)[\\s\\S])+)</div>";
			dsb.newChapterReg = null;
			dsb.newChapterReg2 = "<div\\s*class=\"lastzj\">[^<]+<a\\s*[^>]+>([^<]+)</a>";
			dsb.updateTimeReg = "更新时间：</span>\\s*<span\\s*class=\"result-game-item-info-tag-title\">(\\d\\d\\d\\d-\\d\\d-\\d\\d)</span>";
			dsb.completedReg = null;
		}
		return dsb;
	}

	private static Config biquge;

	public static Config getBiqugeConfig() {
		if (biquge == null) {
			biquge = new Config();
			biquge.searchUrl = "http://zhannei.baidu.com/cse/search?s=287293036948159515&q=";
			biquge.searchFilter = "div class=\"result-item result-game-item\"";
			biquge.detailUrlReg = null;
			biquge.coverReg = "<img\\s*src=\"([^\"]+)\"\\s*alt";// 列表和详情界面都用的这个
			biquge.directoryUrlReg = null;
			biquge.nameReg = "<a\\s*cpos=\"title\"\\s*href=\"([^\"]+)\"\\s*title=\"([^\"]+)\"[^>]+>[^<]*(<em>(.*)</em>)?[^<]*</a>";//
			biquge.keyReg = null;// 搜索到的关键词
			biquge.authorReg = "作者：</span>\\s*<span>\\s*(((?!</span>)[\\s\\S])+)</span>";
			biquge.typeReg = "类型：</span>\\s*<span\\s*class=\"result-game-item-info-tag-title\">([^<]+)</span>";
			biquge.wordsReg2 = "<span>字数：(\\d+)</span>";
			biquge.tipsReg = "<p\\s*class=\"result-game-item-desc\">(((?!</p>)[\\s\\S])+)</p>";
			biquge.tipsDetailReg = "<div\\s*class=\"txtbox\">(((?!</div>)[\\s\\S])+)</div>";
			biquge.newChapterReg = "<a\\s*cpos=\"newchapter\"[^>]+>([^<]+)</a>";
			biquge.newChapterReg2 = "<div\\s*class=\"lastzj\">[^<]+<a\\s*[^>]+>([^<]+)</a>";
			biquge.updateTimeReg = "更新时间：</span>\\s*<span\\s*class=\"result-game-item-info-tag-title\">(\\d\\d\\d\\d-\\d\\d-\\d\\d)</span>";
			biquge.completedReg = null;
		}
		return biquge;
	}

	private static Config _6mao;

	public static Config get6MaoConfig() {
		if (_6mao == null) {
			_6mao = new Config();
			_6mao.searchUrl = "http://zhannei.baidu.com/cse/search?s=3242083170117637887&q=";
			_6mao.searchFilter = "div class=\"result-item result-game-item\"";
			_6mao.detailUrlReg = null;
			_6mao.coverReg = "<img\\s*src=\"([^\"]+)\"\\s*alt";// 列表和详情界面都用的这个
			_6mao.directoryUrlReg = null;
			_6mao.nameReg = "<a\\s*cpos=\"title\"\\s*href=\"([^\"]+)\"\\s*title=\"([^\"]+)\"[^>]+>[^<]*(<em>(.*)</em>)?[^<]*</a>";//
			_6mao.keyReg = null;// 搜索到的关键词
			_6mao.authorReg = "作者：</span>\\s*<span>\\s*(((?!</span>)[\\s\\S])+)</span>";
			_6mao.typeReg = null;
			_6mao.wordsReg = "字数：\\s*</span>\\s*<span[^>]+>\\s*(\\d+)";
			_6mao.wordsReg2 = "<span>字数：(\\d+)</span>";
			_6mao.tipsReg = "<p\\s*class=\"result-game-item-desc\">(((?!</p>)[\\s\\S])+)</p>";
			_6mao.tipsDetailReg = "<div\\s*class=\"txtbox\">(((?!</div>)[\\s\\S])+)</div>";
			_6mao.newChapterReg = null;
			_6mao.newChapterReg2 = "<div\\s*class=\"lastzj\">[^<]+<a\\s*[^>]+>([^<]+)</a>";
			_6mao.updateTimeReg = "更新时间：</span>\\s*<span\\s*class=\"result-game-item-info-tag-title\">(\\d\\d\\d\\d-\\d\\d-\\d\\d)</span>";
			_6mao.completedReg = null;
		}
		return _6mao;
	}

	private static Config _520;

	public static Config get18KConfig() {
		if (baidu == null) {
			baidu = new Config();
			baidu.searchUrl = "http://520xs.co/modules/article/search.php?searchkey=";
			baidu.searchFilter = null;
			baidu.detailUrlReg = null;
			baidu.coverReg = null;
			baidu.directoryUrlReg = null;
			baidu.nameReg = null;
			baidu.keyReg = null;// 搜索到的关键词
			baidu.authorReg = null;
			baidu.typeReg = null;
			baidu.wordsReg = null;
			baidu.tipsReg = null;
			baidu.tipsDetailReg = null;
			baidu.newChapterReg = null;
			baidu.updateTimeReg = null;
			baidu.completedReg = null;
		}
		return baidu;
	}
}
