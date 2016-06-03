package sf.hmg.turntest;

import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.lqy.abook.activity.ReadActivity;
import com.lqy.abook.entity.ChapterEntity;
import com.lqy.abook.entity.FontMode;
import com.lqy.abook.load.FileUtil;
import com.lqy.abook.tool.CallBackListener;
import com.lqy.abook.tool.GlobalConfig;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.widget.MyAlertDialog;

public class TurnUtil implements GestureDetector.OnGestureListener {
	private PageWidget mPageWidget;
	private Bitmap bmp;
	private Canvas canvas;
	private BookPageFactory pagefactory;
	private GestureDetector mGestureDetector;

	private int sw;// 屏幕宽度的4分之1
	private int sh;
	private boolean isDrag = false;
	private boolean isForbid = false;// 翻页到底了不处理touch事件
	private ReadActivity activity;

	public TurnUtil(ReadActivity activity, PageWidget pageWidget, FontMode mode, int fontSize) {
		this.activity = activity;
		this.mPageWidget = pageWidget;

		sw = GlobalConfig.getScreenWidth();
		sh = GlobalConfig.getScreenHeight() + GlobalConfig.getStatusBarHeight();
		pagefactory = new BookPageFactory(activity, sw, sh);
		pagefactory.setFontMode(mode);
		pagefactory.setFontSize(fontSize);

		bmp = Bitmap.createBitmap(sw, sh, Bitmap.Config.RGB_565);
		canvas = new Canvas(bmp);
		// mPageWidget默认显示lastPageBitmap
		mPageWidget.setScreen(sw, sh);
		pagefactory.draw(canvas);
		mPageWidget.setCurrentBitmap(bmp);
		// 手势
		mGestureDetector = new GestureDetector(activity, TurnUtil.this);
		mGestureDetector.setIsLongpressEnabled(false); // 禁用长按监听
		mPageWidget.setOnTouchListener(new OnTouchListener() {
			public boolean onTouch(View v, MotionEvent e) {
				return onLayoyutTouch(e);
			}
		});

		this.sw = sw / 4;
		this.sh = sh / 4;
	}

	public void setFontMode(FontMode mode) {
		pagefactory.setFontMode(mode);
		pagefactory.draw(canvas);
		mPageWidget.setCurrentBitmap(bmp);
		mPageWidget.invalidate();
	}

	public void setLoading() {
		pagefactory.setLoading();
		pagefactory.draw(canvas);
		mPageWidget.setCurrentBitmap(bmp);
		mPageWidget.invalidate();
	}

	public boolean showChapterText(ChapterEntity chapter, long bookId, int readBegin) {
		boolean isSuccess = false;
		if (chapter == null)
			pagefactory.openBook(null, TextType.NOTDIR, 0);
		else if (chapter.isVip())
			pagefactory.openBook(null, TextType.VIP, 0);
		else {
			MyLog.i("showChapterText " + chapter.getName());
			String path = FileUtil.getBooksPath(bookId) + File.separator + FileUtil.getChapterName(chapter.getName());
			isSuccess = pagefactory.openBook(path, TextType.PATH, readBegin);
		}
		pagefactory.draw(canvas);
		mPageWidget.setCurrentBitmap(bmp);

		mPageWidget.invalidate();
		return isSuccess;
	}

	public void setFontSize(int fontSize) {
		pagefactory.setFontSize(fontSize);
		pagefactory.draw(canvas);
		mPageWidget.setCurrentBitmap(bmp);
		mPageWidget.invalidate();
	}

	private long time = 0;

	private boolean changePage(float x, float y) {
		MyLog.i("changePage x=" + x + " y=" + y);
		if (System.currentTimeMillis() - time < 500)
			return false;
		time = System.currentTimeMillis();
		mPageWidget.calcCornerXY(x, y);

		if (mPageWidget.DragToRight()) {
			try {
				pagefactory.prePage();
			} catch (IOException e) {
			}
			if (pagefactory.isfirstPage()) {
				return false;
			}
			mPageWidget.pageBmp();
			pagefactory.draw(canvas);
		} else {
			try {
				pagefactory.nextPage();
			} catch (IOException e) {
			}
			if (pagefactory.islastPage()) {
				return false;
			}
			mPageWidget.pageBmp();
			pagefactory.draw(canvas);
		}

		activity.updateTime();
		return true;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		MyLog.i("onDown");
		isDrag = false;
		isForbid = false;
		return true;
	}

	private final int FLING_MIN_VEL = 300; // 判断轻扫的最小速度

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
		if (Math.abs(velocityX) > FLING_MIN_VEL) {
			// MyLog.i("onFling " + e1.getX() + "  " + e2.getX() + " " +
			// velocityX);
			int mCornerX = velocityX > 0 ? sw : sw * 3;
			int mCornerY = velocityY > 0 ? sh : sh * 3;
			MyLog.i("onFling " + mCornerX + "  " + mCornerY);
			if (changePage(mCornerX, mCornerY)) {
				mPageWidget.abortAnimation();
				mPageWidget.startAnimation(mCornerX, mCornerY);
				MyLog.i("pageOver ok ");
			} else {
				MyLog.i("pageOver error ");
			}
			isDrag = false;
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		MyLog.i("onLongPress");
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
		// //MyLog.i("onScroll " + e1.getX() + "  " + e2.getX());
		isDrag = true;

		// if (isDrag) {
		// mPageWidget.draging(e2.getX(), e2.getY());
		// } else {
		// if (changePage(distanceX > 0 ? 0 : sw, distanceY > 0 ? 0 : sh)) {
		// MyLog.i("pageOver ok ");
		// mPageWidget.setTouch(e2.getX(), e2.getY());
		// mPageWidget.abortAnimation();
		// } else {
		// MyLog.i("pageOver error ");
		// isForbid = true;
		// return false;
		// }
		// }

		return false;
	}

	// layout触摸事件
	public boolean onLayoyutTouch(MotionEvent event) {
		boolean result = mGestureDetector.onTouchEvent(event);
		// 松开,拖拽结束
		// if (MotionEvent.ACTION_UP == event.getAction() && isDrag == true
		// && !isForbid) {
		// MyLog.i("drag over ");
		// mPageWidget.startAnimation(event.getX(), event.getY());
		// }
		// 转到手势监听
		return result;
	}

	@Override
	public void onShowPress(MotionEvent e) {

	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		if (e.getX() > sw && e.getX() < sw * 3) {
			if (e.getY() > sh & e.getY() < sh * 3) {// 点击中部弹出菜单
				// MyLog.i("点击中部弹出菜单");
				if (menuListener != null)
					menuListener.callBack();
			}
		} else {// 点击两恻 翻页
			if (changePage(e.getX(), e.getY())) {
				mPageWidget.abortAnimation();
				mPageWidget.startAnimation(e.getX(), e.getY());
				MyLog.i("pageOver ok ");
			} else {
				MyLog.i("pageOver error ");
			}
		}
		return false;
	}

	private CallBackListener menuListener;

	public void setMenuListener(CallBackListener menuListener) {
		this.menuListener = menuListener;
	}

	private void showImg() {
		Activity a = ReadActivity.getInstance();
		LinearLayout lay = new LinearLayout(a);
		ImageView v = new ImageView(a);
		v.setImageBitmap(bmp);
		lay.addView(v, new LinearLayout.LayoutParams(400, -2));
		v = new ImageView(a);
		v.setImageBitmap(bmp);
		lay.addView(v, new LinearLayout.LayoutParams(400, -2));
		new MyAlertDialog(a).setView(lay).show();
	}
}