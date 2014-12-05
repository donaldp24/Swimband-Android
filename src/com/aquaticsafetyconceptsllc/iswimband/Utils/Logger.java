package com.aquaticsafetyconceptsllc.iswimband.Utils;

public class Logger {
	public static final String TAG = "iSwimband";

	public static void log(String format, Object ...args) {
		// android.util.Log.w(TAG + ":" + "none_tag", String.format(format, args));
	}

	public static void l(String tag, String format, Object ...args) {
		android.util.Log.w(TAG + ":" + tag, String.format(format, args));
	}
	
	public static void logError(String tag, String format, Object ...args) {
		android.util.Log.e(TAG + ":" + tag, String.format(format, args));
	}
}
