package com.lqy.abook.tool;

import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.lqy.abook.R;
import com.lqy.abook.activity.ReadMenuActivity;
import com.lqy.abook.load.FileUtil;
import com.lqy.abook.widget.ArrayDialog;
import com.lqy.abook.widget.MyAlertDialog;

public class VoiceUtils {

	public static final String bookOver = "此书已完结";
	private boolean isOver = false;

	private boolean isRunning;
	private ReadMenuActivity activity;
	private SpeechSynthesizer mTts;
	private SharedPreferences sp;

	private String voicer = "xiaoyan";
	private int speed = CONSTANT.voice_speed;

	private String[] mCloudVoicersEntries;
	private String[] mCloudVoicersValue;
	private int selectedNum = 0;
	private boolean isLocated = false;// 本地听书还是在线听书

	public VoiceUtils(ReadMenuActivity activity) {
		this.activity = activity;

		sp = activity.getSharedPreferences(CONSTANT.SP_READ, 0);
		speed = sp.getInt("speed", speed);
		voicer = sp.getString("voicer", voicer);

		mCloudVoicersEntries = activity.getResources().getStringArray(R.array.voicer_cloud_entries);
		mCloudVoicersValue = activity.getResources().getStringArray(R.array.voicer_cloud_values);

		mTts = SpeechSynthesizer.createSynthesizer(activity, mTtsInitListener);
	}

	public void setVoicer() {
		if (isInstalled() && isLocated) {
			SpeechUtility.getUtility().openEngineSettings(SpeechConstant.ENG_TTS);
		} else {
			// new
			// AlertDialog.Builder(activity).setTitle("在线合成发音人选项").setSingleChoiceItems(mCloudVoicersEntries,
			// // 单选框有几项,各是什么名字
			// selectedNum, // 默认的选项
			// new DialogInterface.OnClickListener() { // 点击单选框后的处理
			// public void onClick(DialogInterface dialog, int which) { //
			// 点击了哪一项
			// voicer = mCloudVoicersValue[which];
			// selectedNum = which;
			// // dialog.dismiss();
			// sp.edit().putString("voicer", voicer).commit();
			// }
			// }).show();
			new ArrayDialog(activity).setTitle("在线合成发音人选项").setItems(mCloudVoicersEntries, selectedNum, new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					voicer = mCloudVoicersValue[which];
					selectedNum = which;
					// dialog.dismiss();
					sp.edit().putString("voicer", voicer).commit();
				}
			}).show();
		}
	}

	private boolean isInstalled() {
		return SpeechUtility.getUtility().checkServiceInstalled();
	}

	public void onlongClick(final CallBackListener cb) {
		if (isInstalled()) {
			new MyAlertDialog(activity).setItems(new String[] { "使用讯飞语记听书", "在线听书" }, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					if (mTts != null && mTts.isSpeaking())
						mTts.stopSpeaking();
					if (which == 0) {
						startVoice(cb, null, true);
					} else {
						startVoiceOnlineToast(cb, null);
					}
				}
			}).show();
		} else {
			startVoice(cb);
		}
	}

	public void setVoiceSpeed(int speed) {
		if (speed >= 0 && speed <= 100)
			this.speed = speed;
		sp.edit().putInt("speed", speed).commit();
	}

	public void startVoiceAfterGetChapter() { // 初始化合成对象
		if (isRunning) {
			startVoice(null);
		}
	}

	public void startVoice(final CallBackListener cb) { // 初始化合成对象
		if (mTts != null && mTts.isSpeaking())
			mTts.stopSpeaking();
		if (isInstalled()) {
			startVoice(cb, null, true);
		} else {
			install(cb);
		}
	}

	public void startVoiceOnlineToast(final CallBackListener cb, final String text) {
		Util.dialog(activity, "确定要在线听书吗，这可能会消耗您的流量，建议在wifi下使用", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				startVoice(cb, text, false);
			}
		});
	}

	public void startVoice(CallBackListener cb, String text, boolean isLocated) {
		this.isLocated = isLocated;
		if (text == null) {
			text = activity.getVoiceText();
		}
		isRunning = true;
		if (Util.isEmpty(text)) {
			startVoice(cb, "加载中，请稍等", isLocated);
			return;
		}
		if (bookOver.equals(text))
			isOver = true;
		else
			isOver = false;
		if (mTts == null) {
			mTts = SpeechSynthesizer.createSynthesizer(activity, mTtsInitListener);
		}
		// 清空参数
		mTts.setParameter(SpeechConstant.PARAMS, null);
		if (isLocated) {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
			// 设置本地合成发音人 voicer为空，默认通过语记界面指定发音人。
			mTts.setParameter(SpeechConstant.VOICE_NAME, "");
		} else {
			mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
			// 设置在线合成发音人
			mTts.setParameter(SpeechConstant.VOICE_NAME, voicer);
		}
		// 设置合成语速
		mTts.setParameter(SpeechConstant.SPEED, speed + CONSTANT.EMPTY);

		// 设置播放器音频流类型
		mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");
		// 设置播放合成音频打断音乐播放，默认为true
		mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

		// 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
		// 注：AUDIO_FORMAT参数语记需要更新版本才能生效
		mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");
		mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, FileUtil.getCachePath() + "/tts.wav");

		int code = mTts.startSpeaking(text, mTtsListener);

		if (code == ErrorCode.SUCCESS) {
			if (cb != null)
				cb.callBack();
		} else {
			showTip("语音合成失败,错误码: " + code);
		}

	}

	private void install(final CallBackListener cb) {
		new MyAlertDialog(activity).setTitle("系统提示").setMessage("检测到您未安装讯飞语记！\n是否前往下载讯飞语记？").setPositiveButton("确定", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String url = SpeechUtility.getUtility().getComponentUrl();
				Uri uri = Uri.parse(url);
				Intent it = new Intent(Intent.ACTION_VIEW, uri);
				activity.startActivity(it);
			}
		}).setNeutralButton("在线听书", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				startVoiceOnlineToast(cb, null);
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
			}
		}).show();
	}

	public void stopVoice() {
		isRunning = false;
		if (mTts != null)
			mTts.stopSpeaking();
	}

	public void pauseVoice() {
		isRunning = false;
		if (mTts != null)
			mTts.pauseSpeaking();
	}

	public void resumeVoice() {
		isRunning = true;
		if (mTts != null)
			mTts.resumeSpeaking();
	}

	/**
	 * 初始化监听。
	 */
	private InitListener mTtsInitListener = new InitListener() {
		@Override
		public void onInit(int code) {
			if (code != ErrorCode.SUCCESS) {
				showTip("初始化失败,错误码：" + code);
			} else {
				// 初始化成功，之后可以调用startSpeaking方法
				// 注：有的开发者在onCreate方法中创建完合成对象之后马上就调用startSpeaking进行合成，
				// 正确的做法是将onCreate中的startSpeaking调用移至这里
			}
		}
	};

	private void showTip(String str) {
		Util.toast(activity, str);
	}

	/**
	 * 合成回调监听。
	 */
	private SynthesizerListener mTtsListener = new SynthesizerListener() {

		@Override
		public void onSpeakBegin() {
			// showTip("开始播放");
		}

		@Override
		public void onSpeakPaused() {
			// showTip("暂停播放");
		}

		@Override
		public void onSpeakResumed() {
			// showTip("继续播放");
		}

		@Override
		public void onBufferProgress(int percent, int beginPos, int endPos, String info) {

		}

		@Override
		public void onSpeakProgress(int percent, int beginPos, int endPos) {
			// 播放进度
		}

		@Override
		public void onCompleted(SpeechError error) {
			if (error == null) {
				// showTip("播放完成");
				if (!isOver)
					startVoice(null, null, isLocated && isInstalled());
			} else if (error != null) {
				showTip(error.getPlainDescription(true));
			}
		}

		@Override
		public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
			// 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
			// 若使用本地能力，会话id为null
			// if (SpeechEvent.EVENT_SESSION_ID == eventType) {
			// String sid = obj.getString(SpeechEvent.KEY_EVENT_SESSION_ID);
			// Log.d(TAG, "session id =" + sid);
			// }
		}
	};

	public void onDestroy() {
		mTts.stopSpeaking();
		// 退出时释放连接
		mTts.destroy();
	}
}
