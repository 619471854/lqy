package com.lqy.abook.parser;

public class Config {

	public static final String tagReg = "<[^>]+>";// 去标签
	public static final String lineWrapReg = "<br\\s*/?>";// 换行
	public static final String nbsp = "&nbsp;";// 替换空格

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
			_17K.tipsDetailReg = "<font\\s*itemprop=\"description\">(((?!</font>).)+)</font>";
			_17K.newChapterReg = "最近更新：</font>\\s*<a[^>]+>([^<]+)</a>";
			_17K.updateTimeReg = "<cite>([\\d-\\s:]+)</cite>";
			_17K.completedReg = "<font\\s*itemprop=\"updataStatus\">(此书已完成)</font>";
			_17K.newChapterReg2 = "<font\\s*itemprop=\"headline\">([^<]+)</font>";
			_17K.wordsReg2 = "<em\\s*itemprop=\"wordCount\">(\\d+)</em>";
			_17K.updateTimeReg2 = "<span\\s*class=\"time\">更新：(\\d\\d\\d\\d-\\d\\d-\\d\\d)更新</span>";
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
			SM.tipsDetailReg = "<div\\s*class=\"introtxt\">\\s*<strong>[^<]+</strong>(((?!</div>)[\\s\\S])+)</div>";
			SM.newChapterReg = "<a\\s*cpos=\"newchapter\"[^>]+>([^<]+)</a>";
			SM.updateTimeReg = "<span\\s*class=\"result-game-item-info-tag-title\">([\\d-]+)</span>";
			SM.completedReg = "<span\\s*class=\"zt1\">已完成</span>";
			SM.newChapterReg2 = "<span\\s*class=\"l02\"\\s*style=\"overflow:hidden;\"><a[^>]+>([^<]+)</a></span>";
			SM.updateTimeReg2 = "<span\\s*class=\"l04\">([\\d-]+)</span>";
			SM.wordsReg2 = "字数：\\s*<i>(\\d+)</i>";
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

	private static Config _520;

	public static Config get18KConfig() {
		if (_520 == null) {
			_520 = new Config();
			_520.searchUrl = "http://520xs.co/modules/article/search.php?searchkey=";
			_520.searchFilter = null;
			_520.detailUrlReg = null;
			_520.coverReg = null;
			_520.directoryUrlReg = null;
			_520.nameReg = null;
			_520.keyReg = null;// 搜索到的关键词
			_520.authorReg = null;
			_520.typeReg = null;
			_520.wordsReg = null;
			_520.tipsReg = null;
			_520.tipsDetailReg = null;
			_520.newChapterReg = null;
			_520.updateTimeReg = null;
			_520.completedReg = null;
		}
		return _520;
	}
}
