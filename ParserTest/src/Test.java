import java.util.ArrayList;

import com.lqy.abook.entity.BookEntity;
import com.lqy.abook.parser.site.ParserSM;
import com.lqy.abook.tool.MyLog;

public class Test {
	public static void main(String[] args) {
		 ArrayList<BookEntity> data= new ArrayList<BookEntity>();
		 new ParserSM().parserSearch(data, "善良的蜜蜂 修罗武神");
		// BookEntity detail=new BookEntity();
		// detail.setDetailUrl("http://www.shenmaxiaoshuo.com/xx-28855/");
		// new ParserSM().parserBookDict(detail);
		// List<ChapterEntity> data = new
		// ParserSM().parserBookDict("http://www.shenmaxiaoshuo.com/ml-21318/");
		// String data = new ParserSM()
		// .getChapterDetail("http://www.shenmaxiaoshuo.com/ml-21318-125304187/");
		try {
//			String url = "http://www.16kxsw.com/";
//
//			MyLog.e(	WebServer.getDataByUrlConnection(url, "gbk"));

//			Parser.getConnectionManager().setCookieProcessingEnabled(true);
//			Parser parser = new Parser(url);
//			parser = new Parser(url);
			// SimpleNodeIterator iterator = new Parser(url).parse(
			// new NodeClassFilter(BodyTag.class)).elements();
			// if (iterator.hasMoreNodes()) {
			// String html = iterator.nextNode().toHtml();
			// MyLog.e(html);
			// } else {
			// MyLog.e("empty");
			// }
			// MyLog.i(WebServer.getDataByParser("http://www.mayaname.com/viewthread.php?tid=699783&extra=page%3D1",
			// "gbk"));
		} catch (Exception e) {
			MyLog.e(e);
		}
	}
}
