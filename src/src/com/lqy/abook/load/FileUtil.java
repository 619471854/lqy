package com.lqy.abook.load;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

import android.os.Environment;

import com.lqy.abook.tool.CONSTANT;
import com.lqy.abook.tool.MatcherTool;
import com.lqy.abook.tool.MyLog;
import com.lqy.abook.tool.Util;

/**
 * 文件操作类
 */
public class FileUtil {

	private static final String APP_PATH = "/abook";// 路径
	private static final String ERROR_PATH = "/abook/error_log";// 日志
	private static final String IMAGE_PATH = "/abook/image";// 图片路径
	private static final String DB_PATH = "/abook/db";// 数据库
	private static final String BOOKS_PATH = "/abook/books";// books
	private static final String CACHE_PATH = "/abook/cache";// books
	private static final String BOOKNAME = "book_";// books
	public static final String BOOK_INDEX_NAME = "000000";// books

	private static String sdcard_path = null;// sd卡路径

	public static String getSdCardPath() {

		if (Util.isEmpty(sdcard_path)) {
			// 判断sd卡是否存在
			boolean sdCardExist = Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED);
			if (sdCardExist)
				sdcard_path = Environment.getExternalStorageDirectory().toString();
			else
				sdcard_path = "/";
		}
		return sdcard_path;

	}

	public static String getCachePath() {
		File file = new File(getSdCardPath() + CACHE_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.toString();
	}
	public static String getErrorPath() {
		File file = new File(getSdCardPath() + ERROR_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.toString();
	}

	public static String getImagePath() {
		File file = new File(getSdCardPath() + IMAGE_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.toString();
	}

	public static String getDBPath() {
		File file = new File(getSdCardPath() + DB_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.toString();
	}

	public static String getBooksPath(long bookId) {
		File file = new File(getSdCardPath() + BOOKS_PATH, getBookName(bookId));
		if (!file.exists()) {
			file.mkdirs();
		}
		return file.toString();
	}

	public static String getBookName(long bookId) {
		return BOOKNAME + bookId;
	}

	// public static String getChapterName(int chapterId) {
	// return String.format("ch_%05d", chapterId);
	// }

	public static String getChapterName(String chapterName) {
		return Util.isEmpty(chapterName) ? "ch_error" : chapterName.trim().hashCode() + CONSTANT.EMPTY;
	}

	/**
	 * 整行的文件，比如章节目录
	 */
	public static String readByBytes(String path) {

		FileInputStream fis = null;// 输入流
		ByteArrayOutputStream bos = null;

		String end = null;
		File file = new File(path);
		if (file.length() == 0) {
			end = CONSTANT.EMPTY;
		} else if (file.exists()) {
			try {
				fis = new FileInputStream(new File(path));
				bos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int length = -1;
				while ((length = fis.read(buffer)) != -1) {
					bos.write(buffer, 0, length);
				}
				end = new String(bos.toByteArray(), "UTF-8");

			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				try {
					fis.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
				try {
					bos.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}

		return end;
	}

	/**
	 * 文件读取,多行的文件，比如章节内容
	 */
	public static String readByLine(String path) {

		FileInputStream fis = null;// 输入流
		BufferedReader br = null;
		String end = null;

		File file = new File(path);
		if (file.length() == 0) {
			end = CONSTANT.EMPTY;
		} else if (file.exists()) {
			try {
				fis = new FileInputStream(file);
				br = new BufferedReader(new InputStreamReader(fis, "utf-8"));
				StringBuffer sb = new StringBuffer();
				String data = CONSTANT.EMPTY;
				while ((data = br.readLine()) != null) {
					sb.append(data + "\n");
				}
				end = sb.toString();
			} catch (Exception e) {
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (Exception e) {
					}
					fis = null;
				}
				if (br != null) {
					try {
						br.close();
					} catch (IOException e) {
					}
					br = null;
				}
			}
		}
		return end;
	}

	/**
	 * 文件写入
	 */

	public static boolean write(String str, String path, String name) {
		// 获取文件地址
		boolean end = true;
		FileOutputStream os = null;// 输出流
		try {

			// 创建文件夹及文件
			File file = new File(path);
			if (!file.exists())
				file.mkdirs();
			path += File.separator + name;
			file = new File(path);
			file.createNewFile();

			os = new FileOutputStream(file, false);
			os.write(str.getBytes());

		} catch (Exception e) {
			end = false;
		} finally {

			if (os != null) {
				try {
					os.close();
				} catch (Exception e) {
				}
			}
			os = null;
		}
		return end;
	}

	/**
	 * 保存文件
	 */
	public static String loadImageForUrl(String url, String path) {
		return loadImageForUrl(url, path, null);
	}

	/**
	 * 保存文件
	 */
	public static String loadImageForUrl(String url, String path, String fileName) {

		if (Util.isEmpty(url))
			return null;

		if (Util.isEmpty(fileName)) {// 用url的哈希码作为文件名
			fileName = url.hashCode() + CONSTANT.EMPTY;
		}
		try {
			// 创建文件夹
			File file = new File(path);
			if (!file.exists())
				file.mkdirs();

			// 先保存到tempFile里，下载结束后改名为file
			file = new File(path, fileName);
			File tempFile = new File(path, fileName + CONSTANT.TEMP);

			// 保存文件
			if (file.exists() && file.length() == 0)
				file.delete();

			if (tempFile.exists())
				tempFile.delete();

			// 带后缀名的地址的图片可以使用缓存图片，
			if (MatcherTool.hasExtendName(url) && file.exists() && file.length() != 0) {
				MyLog.i("已下载:" + url);
			} else {
				MyLog.i("开始下载:" + url);
				FileOutputStream fos = null;
				InputStream is = null;

				try {

					fos = new FileOutputStream(tempFile);
					is = new URL(url).openStream();

					byte buf[] = new byte[1024];
					int numread;
					while ((numread = is.read(buf)) != -1) {
						fos.write(buf, 0, numread);
					}
					fos.close();
					is.close();

					fos = null;
					is = null;

					tempFile.renameTo(file);

					MyLog.i("下载完成！:" + url);
				} catch (Exception e) {
					MyLog.e(e.toString() + "下载时出现异常！:" + file.toString());
					return null;

				} finally {

					if (fos != null) {
						try {
							fos.close();
						} catch (IOException e) {
						}
						fos = null;
					}
					if (is != null) {
						try {
							is.close();
						} catch (IOException e) {
						}
						is = null;
					}
				}

			}

			return file.toString();

		} catch (Exception ex) {
			return null;
		}
	}

	/**
	 * 删除文件
	 */
	public static void delFile(File file) {
		if (file == null || !file.exists()) {
		} else if (file.isFile()) {
			file.delete();
		} else if (file.isDirectory()) {
			File[] childFiles = file.listFiles();
			if (childFiles == null || childFiles.length == 0) {
				file.delete();
			} else {
				for (File f : file.listFiles())
					delFile(f);
			}
		}
		file.delete();
	}

}