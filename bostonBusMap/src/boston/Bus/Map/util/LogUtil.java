package boston.Bus.Map.util;

import java.io.PrintWriter;
import java.io.StringWriter;

import android.util.Log;

public class LogUtil
{
	public static void e(Throwable e)
	{
		StringWriter writer = new StringWriter();
		e.printStackTrace(new PrintWriter(writer));
		Log.e("BostonBusMap", writer.toString());
	}
}
