package boston.Bus.Map.data;

import java.util.Date;
import java.util.HashMap;

import boston.Bus.Map.main.AlertInfo;
import boston.Bus.Map.main.MoreInfo;

import android.content.Context;
import android.text.Html;
import android.text.Spanned;

public class Alert
{
	private final Date date;
	private final String title;
	private final String description;
	private final String delay;
	
	public Alert(Date date, String title, String description, String delay)
	{
		this.date = date;
		this.title = title;
		this.description = description;
		this.delay = delay;
	}

	public Date getDate() {
		return date;
	}

	public String getTitle() {
		return title;
	}

	public String getDescription() {
		return description;
	}

	public String getDelay() {
		return delay;
	}

	public HashMap<String, Spanned> makeSnippetMap(Context context)
	{
		HashMap<String, Spanned> map = new HashMap<String, Spanned>();
		
		String ret = makeSnippet(context);
		
		map.put(AlertInfo.textKey, Html.fromHtml(ret));
		
		return map;
	}

	private String makeSnippet(Context context)
	{
		
	}
	
}
