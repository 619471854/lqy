package com.lqy.abook.tool;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NetChangeReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		MyLog.i("NetChangeReceiver");
		NetworkUtils.refreshNetState(context);
	}
}
