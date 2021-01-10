package com.schneeloch.bostonbusmap_library.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.util.Log;

public class LogUtil
{
	public static String TAG = "BostonBusMap";
	
	public static void e(Throwable e)
	{
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		Log.e(TAG, writer.toString());
	}

	public static void i(String msg) {
		Log.i(TAG, msg);
	}

	public static void w(String msg) {
		Log.w(TAG, msg);
	}
}
