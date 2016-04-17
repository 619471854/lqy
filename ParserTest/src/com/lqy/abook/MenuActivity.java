package com.lqy.abook;
import com.lqy.abook.tool.MyLog;

public class MenuActivity {

	public void sendErrorOnThread(String error) {
		MyLog.i(error);

	}

	public void sendMsgOnThread(int what) {
		MyLog.i("sendMsgOnThread " + what);
	}

	public void sendMsgOnThread(int what, Object obj) {
		MyLog.i("sendMsgOnThread obj " + what);
	}

}
