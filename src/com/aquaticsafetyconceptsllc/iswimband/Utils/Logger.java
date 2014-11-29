package com.aquaticsafetyconceptsllc.iswimband.Utils;

public class Logger {
	public static final String TAG = "iSwimband";
	
	public static void log(String format, Object ...args) {
		/*
		Thread current = Thread.currentThread();
		StackTraceElement[] stack = current.getStackTrace();
		String className = "";
		String fileName = "";
		int lineNumber = 0;
		String methodName = "";
		for(StackTraceElement element : stack)
		{
			if (!element.isNativeMethod()) {
				className = element.getClassName();
				fileName = element.getFileName();
				lineNumber = element.getLineNumber();
				methodName = element.getMethodName();
				//break;
			}
		}
		if (methodName.equalsIgnoreCase(""))
			android.util.Log.w(TAG, String.format(format, args));
		else
			android.util.Log.w(TAG, String.format("%s-%s-%d : %s", className, methodName, lineNumber, String.format(format, args)));
			*/
		android.util.Log.w(TAG, String.format(format, args));
	}
	
	public static void logError(String format, Object ...args) {
		android.util.Log.e(TAG, String.format(format, args));
	}
	
	public static void logDebug(String format, Object ...args) {
		android.util.Log.d(TAG, String.format(format, args));
	}
	
	public static void logInfo(String format, Object ...args) {
		android.util.Log.i(TAG, String.format(format, args));
	}
}
