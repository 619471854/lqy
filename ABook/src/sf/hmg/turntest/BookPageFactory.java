package sf.hmg.turntest;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Vector;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Rect;

import com.lqy.abook.activity.ReadActivity;
import com.lqy.abook.entity.FontMode;
import com.lqy.abook.load.Cache;
import com.lqy.abook.tool.DisplayUtil;
import com.lqy.abook.tool.GlobalConfig;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

public class BookPageFactory {
	private ReadActivity activity;

	private TextType textType = TextType.DEFAULT;// 内容类型

	private File book_file = null;
	private MappedByteBuffer m_mbBuf = null;
	private int m_mbBufLen = 0;
	private int m_mbBufBegin = 0;
	private int m_mbBufEnd = 0;
	private String m_strCharsetName = "utf-8";
	private Bitmap m_book_bg = null;
	private int mWidth;
	private int mHeight;

	private Vector<String> m_lines = new Vector<String>();

	private int m_fontSize = 32;
	private int m_statusColor = 0xff9f8268;// 进度条等颜色
	private int m_backColor = 0xffe7cfad; // 背景颜色
	private int m_textColor = 0xff423829;
	private final int m_textSelectedColor = 0xaaaaaaaa;// 选中行颜色
	private int marginWidth = 20; // 左右与边缘的距离
	private int marginTop = 40; // 上下与边缘的距离
	private int marginBottom = 40; // 上下与边缘的距离
	private float lineSpacingMultiplier = 1.3f;// 行距1.3倍textsize

	private int mLineCount; // 每页可以显示的行数
	private int mVisibleHeight; // 绘制内容的宽
	private int mVisibleWidth; // 绘制内容的宽
	private boolean m_isfirstPage, m_islastPage;
	private boolean toLast = false;// 是否跳转到前一页，这时

	// private int m_nLineSpaceing = 5;

	private Paint mPaint;

	private int paragraphStartLine = -1; // 当前正在听书的段落起始行
	private int paragraphEndLine = -1; // 当前正在听书的段落结束行

	public BookPageFactory(ReadActivity activity, int w, int h) {
		this.activity = activity;
		marginWidth = DisplayUtil.dip2px(activity, 10); // 左右与边缘的距离
		marginTop = marginWidth * 2;
		marginBottom = DisplayUtil.dip2px(activity, 33);
		mWidth = w;
		mHeight = h;
		mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		mPaint.setTextAlign(Align.LEFT);
		mPaint.setTextSize(m_fontSize);
		mPaint.setColor(m_textColor);
		mVisibleWidth = mWidth - marginWidth * 2;
		mVisibleHeight = mHeight - marginTop - marginBottom;
		mLineCount = mVisibleHeight / (int) (lineSpacingMultiplier * m_fontSize); // 可显示的行数
	}

	private void reset() {
		m_lines.clear();
		mLineCount = mVisibleHeight / (int) (lineSpacingMultiplier * m_fontSize); // 可显示的行数
		m_mbBufBegin = 0;
		m_mbBufEnd = 0;
	}

	public boolean openBook(String strFilePath, TextType type, int readBegin) {
		this.textType = type;
		m_mbBuf = null;
		reset();
		if (textType == TextType.PATH) {
			try {
				book_file = new File(strFilePath);
				long lLen = book_file.length();
				m_mbBufLen = (int) lLen;
				if (!book_file.exists() || lLen < 2) {
					textType = TextType.ERROR;
				} else {
					m_mbBuf = new RandomAccessFile(book_file, "r").getChannel().map(FileChannel.MapMode.READ_ONLY, 0, lLen);
					if (toLast) {
						while (m_mbBufEnd < m_mbBufLen) {
							m_mbBufBegin = m_mbBufEnd;
							m_lines = pageDown();
						}
					} else {
						if (readBegin > 0 || readBegin < lLen) {
							m_mbBufBegin = readBegin;
							m_mbBufEnd = m_mbBufBegin;
						}
						m_lines = pageDown();
					}
					toLast = false;
					return true;
				}
			} catch (IOException e) {
				MyLog.e("openbook error " + e.toString());
				m_mbBuf = null;
				textType = TextType.ERROR;
			}
		}
		toLast = false;
		return false;
	}

	public void setLoading() {
		textType = TextType.LOADING;
	}

	protected byte[] readParagraphBack(int nFromPos) {
		int nEnd = nFromPos;
		int i;
		byte b0, b1;
		if (m_strCharsetName.equals("UTF-16LE")) {
			i = nEnd - 2;
			while (i > 0) {
				b0 = m_mbBuf.get(i);
				b1 = m_mbBuf.get(i + 1);
				if (b0 == 0x0a && b1 == 0x00 && i != nEnd - 2) {
					i += 2;
					break;
				}
				i--;
			}

		} else if (m_strCharsetName.equals("UTF-16BE")) {
			i = nEnd - 2;
			while (i > 0) {
				b0 = m_mbBuf.get(i);
				b1 = m_mbBuf.get(i + 1);
				if (b0 == 0x00 && b1 == 0x0a && i != nEnd - 2) {
					i += 2;
					break;
				}
				i--;
			}
		} else {
			i = nEnd - 1;
			while (i > 0) {
				b0 = m_mbBuf.get(i);
				if (b0 == 0x0a && i != nEnd - 1) {
					i++;
					break;
				}
				i--;
			}
		}
		if (i < 0)
			i = 0;
		int nParaSize = nEnd - i;
		int j;
		byte[] buf = new byte[nParaSize];
		for (j = 0; j < nParaSize; j++) {
			buf[j] = m_mbBuf.get(i + j);
		}
		return buf;
	}

	// 读取上一段落
	protected byte[] readParagraphForward(int nFromPos) {
		int nStart = nFromPos;
		int i = nStart;
		byte b0, b1;
		// 根据编码格式判断换行
		if (m_strCharsetName.equals("UTF-16LE")) {
			while (i < m_mbBufLen - 1) {
				b0 = m_mbBuf.get(i++);
				b1 = m_mbBuf.get(i++);
				if (b0 == 0x0a && b1 == 0x00) {
					break;
				}
			}
		} else if (m_strCharsetName.equals("UTF-16BE")) {
			while (i < m_mbBufLen - 1) {
				b0 = m_mbBuf.get(i++);
				b1 = m_mbBuf.get(i++);
				if (b0 == 0x00 && b1 == 0x0a) {
					break;
				}
			}
		} else {
			while (i < m_mbBufLen) {
				b0 = m_mbBuf.get(i++);
				if (b0 == 0x0a) {
					break;
				}
			}
		}
		int nParaSize = i - nStart;
		byte[] buf = new byte[nParaSize];
		for (i = 0; i < nParaSize; i++) {
			buf[i] = m_mbBuf.get(nFromPos + i);
		}
		return buf;
	}

	protected Vector<String> pageDown() {
		paragraphStartLine = -1;
		paragraphEndLine = -1;
		String strParagraph = "";
		Vector<String> lines = new Vector<String>();
		while (lines.size() < mLineCount && m_mbBufEnd < m_mbBufLen) {
			byte[] paraBuf = readParagraphForward(m_mbBufEnd); // 读取一个段落
			m_mbBufEnd += paraBuf.length;
			try {
				strParagraph = new String(paraBuf, m_strCharsetName);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String strReturn = "";
			if (strParagraph.indexOf("\r\n") != -1) {
				strReturn = "\r\n";
				strParagraph = strParagraph.replaceAll("\r\n", "");
			} else if (strParagraph.indexOf("\n") != -1) {
				strReturn = "\n";
				strParagraph = strParagraph.replaceAll("\n", "");
			}

			if (strParagraph.length() == 0) {
				lines.add(strParagraph);
			}
			while (strParagraph.length() > 0) {
				int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth, null);
				String line = strParagraph.substring(0, nSize);
				if (nSize >= strParagraph.length())
					line = line + "\n";// 用于判断段落
				lines.add(line);
				strParagraph = strParagraph.substring(nSize);
				if (lines.size() >= mLineCount) {
					break;
				}
			}
			if (strParagraph.length() != 0) {
				try {
					m_mbBufEnd -= (strParagraph + strReturn).getBytes(m_strCharsetName).length;
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return lines;
	}

	protected void pageUp() {
		if (m_mbBufBegin < 0)
			m_mbBufBegin = 0;
		Vector<String> lines = new Vector<String>();
		String strParagraph = "";
		while (lines.size() < mLineCount && m_mbBufBegin > 0) {
			Vector<String> paraLines = new Vector<String>();
			byte[] paraBuf = readParagraphBack(m_mbBufBegin);
			m_mbBufBegin -= paraBuf.length;
			try {
				strParagraph = new String(paraBuf, m_strCharsetName);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			strParagraph = strParagraph.replaceAll("\r\n", "");
			strParagraph = strParagraph.replaceAll("\n", "");

			if (strParagraph.length() == 0) {
				paraLines.add(strParagraph);
			}
			while (strParagraph.length() > 0) {
				int nSize = mPaint.breakText(strParagraph, true, mVisibleWidth, null);
				paraLines.add(strParagraph.substring(0, nSize));
				strParagraph = strParagraph.substring(nSize);
			}
			lines.addAll(0, paraLines);
		}
		while (lines.size() > mLineCount) {
			try {
				m_mbBufBegin += lines.get(0).getBytes(m_strCharsetName).length;
				lines.remove(0);
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		m_mbBufEnd = m_mbBufBegin;
		return;
	}

	protected void prePage() throws IOException {
		if (m_mbBuf == null) {
			if (textType != textType.NOTDIR && textType != textType.DEFAULT) {
				textType = textType.LOADING;

				if (Cache.toLastChapter()) {
					activity.changeChapter();
					toLast = true;
				}
			}
			return;
		}
		if (m_mbBufBegin <= 0) {
			m_mbBufBegin = 0;
			if (Cache.toLastChapter()) {
				toLast = true;
				textType = textType.LOADING;
				activity.changeChapter();
				m_isfirstPage = false;
			} else {
				m_isfirstPage = true;
			}

			return;
		} else
			m_isfirstPage = false;
		m_lines.clear();
		pageUp();
		m_lines = pageDown();
		if (ReadActivity.getInstance() != null)
			ReadActivity.getInstance().updateReadLoation(m_mbBufBegin);
	}

	public void nextPage() throws IOException {
		if (m_mbBuf == null) {
			if (textType != textType.NOTDIR && textType != textType.DEFAULT) {
				if (Cache.toNextChapter())
					activity.changeChapter();
				textType = textType.LOADING;
			}
			return;
		}
		if (m_mbBufEnd >= m_mbBufLen) {
			if (Cache.toNextChapter()) {
				textType = textType.LOADING;
				activity.changeChapter();
				m_islastPage = false;
			} else {
				m_islastPage = true;
			}
			return;
		} else
			m_islastPage = false;
		m_lines.clear();
		m_mbBufBegin = m_mbBufEnd;
		m_lines = pageDown();
		if (ReadActivity.getInstance() != null)
			ReadActivity.getInstance().updateReadLoation(m_mbBufBegin);
	}

	/**
	 * 获取当前行，不能返回空字符串
	 */
	public String getVoiceText() {
		if (m_mbBuf == null || textType != textType.PATH) {// 文件未找到
			if (textType == textType.NOTDIR) {
				return "获取目录失败，请换源下载";
			} else if (textType == textType.LOADING || textType == textType.DEFAULT) {
				return "加载中，请稍等";
			} else if (textType == textType.VIP) {
				return "此章是VIP章节，请换源下载";
			} else {
				return "获取章节失败";
			}
		}
		if (m_lines.size() == 0)
			m_lines = pageDown();

		// 获取一个段落
		StringBuilder sb = new StringBuilder();
		String line;
		paragraphStartLine = paragraphEndLine + 1;
		paragraphEndLine = paragraphStartLine;
		int start = paragraphStartLine;
		while (start < m_lines.size()) {
			line = m_lines.get(start);
			sb.append(line);
			start++;
			if (line.indexOf("\n") != -1) {
				break;
			}
		}
		paragraphEndLine = start - 1;

		if (sb.length() == 0)
			return null;
		return sb.toString();
	}

	public void draw(Canvas c) {
		if (m_book_bg == null)
			c.drawColor(m_backColor);
		else
			c.drawBitmap(m_book_bg, null, new Rect(0, 0, mWidth, mHeight), null);

		if (m_mbBuf == null || textType != textType.PATH) {// 文件未找到
			mPaint.setTextAlign(Align.CENTER);
			if (textType == textType.NOTDIR) {
				c.drawText("获取目录失败，请换源下载", mWidth / 2, mHeight / 2, mPaint);
			} else if (textType == textType.LOADING || textType == textType.DEFAULT) {
				c.drawText("加载中", mWidth / 2, mHeight / 2, mPaint);
			} else if (textType == textType.VIP) {
				c.drawText("此章是VIP章节，请换源下载", mWidth / 2, mHeight / 2, mPaint);
			} else {
				c.drawText("获取失败", mWidth / 2, mHeight / 2, mPaint);
			}
			MyLog.i("drawImg " + textType.toString());
			return;
		}
		mPaint.setTextAlign(Align.LEFT);
		if (m_lines.size() == 0)
			m_lines = pageDown();

		int lineHeight = (int) (lineSpacingMultiplier * m_fontSize);

		if (paragraphStartLine != -1 && paragraphEndLine != -1) {
			mPaint.setColor(m_textSelectedColor);
			int p = (int) (m_fontSize * (lineSpacingMultiplier - 0.7) / 2);// 0.7取的标准线处于字体的大概位置，实际上有更复杂的算法
			int top = marginTop + lineHeight * paragraphStartLine + p;
			int bottom = marginTop + lineHeight * paragraphEndLine + lineHeight + p;
			c.drawRect(0, top, mWidth, bottom, mPaint);
			mPaint.setColor(m_textColor);
		}
		mPaint.setColor(m_textColor);

		if (m_lines.size() > 0) {
			MyLog.i("drawImg " + m_lines.get(0));
			int y = marginTop;
			int size = m_lines.size();
			for (int i = 0; i < size; i++) {
				String strLine = m_lines.get(i);
				y += lineHeight;
				c.drawText(strLine, marginWidth, y, mPaint);
			}
		}

		float dp = GlobalConfig.getDensity();
		int progressY = marginTop + mVisibleHeight + (int) (dp * 5);
		mPaint.setColor(m_statusColor);
		mPaint.setStrokeWidth(dp / 2);
		c.drawLine(marginWidth, progressY, mWidth - marginWidth, progressY, mPaint);
		mPaint.setStrokeWidth(dp);
		float fPercent = (float) (m_mbBufEnd * 1.0 / m_mbBufLen);
		int stopX = (int) (Math.min(fPercent, 1) * (mWidth - 2 * marginWidth)) + marginWidth;
		progressY -= (int) (dp * 1.5);
		c.drawLine(marginWidth, progressY, stopX, progressY, mPaint);
	}

	/**
	 * 设置背景
	 */
	public void setBgBitmap(Bitmap BG) {
		m_book_bg = BG;
	}

	public void setFontMode(FontMode mode) {
		if (mode.type == FontMode.Type.custom) {
			m_backColor = mode.bgColor;
			m_book_bg = null;
		} else {
			m_book_bg = BitmapFactory.decodeResource(activity.getResources(), mode.bgResId);
			m_backColor = mode.bgColor;
			if (m_book_bg != null && mode.type != FontMode.Type.green) {
				// 获取全屏的平铺的背景图
				Bitmap full = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.RGB_565);
				Canvas c = new Canvas(full);
				int w = m_book_bg.getWidth();
				int h = m_book_bg.getHeight();
				for (int x = 0; x < mWidth;) {
					for (int y = 0; y < mHeight;) {
						if (y + h > mHeight || x + w > mWidth) {
							Rect s = new Rect(0, 0, mWidth - x, mHeight - y);
							Rect d = new Rect(x, y, mWidth, mHeight);
							c.drawBitmap(m_book_bg, s, d, null);
						} else {
							c.drawBitmap(m_book_bg, x, y, null);
						}
						y += h;
					}
					x += w;
				}
				m_book_bg = full;
			}
		}
		m_textColor = mode.textColor;
		mPaint.setColor(m_textColor);
	}

	public boolean isfirstPage() {
		return m_isfirstPage;
	}

	public boolean islastPage() {
		return m_islastPage;
	}

	public void setFontSize(int fontSize) {
		this.m_fontSize = DisplayUtil.dip2px(activity, fontSize);
		mPaint.setTextSize(m_fontSize);
		reset();
	}
}
