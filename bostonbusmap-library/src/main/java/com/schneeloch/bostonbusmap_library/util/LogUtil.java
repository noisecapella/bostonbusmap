package com.schneeloch.bostonbusmap_library.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.util.Log;

public class LogUtil
{
	private static final String TAG = "BostonBusMap";

	public static boolean print = false;

	public static void e(Throwable e)
	{
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		if (print) {
			System.out.println("Error: " + writer.toString());
		} else {
			Log.e(TAG, writer.toString());
		}
	}

	public static void i(String msg) {
		if (print) {
			System.out.println("Info: " + msg);
		} else {
			Log.i(TAG, msg);
		}
	}

	public static void w(String msg) {
		if (print) {
			System.out.println("Warning: " + msg);
		} else {
			Log.w(TAG, msg);
		}
	}
}
