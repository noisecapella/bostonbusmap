package boston.Bus.Map.ui;

public class ProgressMessage {
	public static final int PROGRESS_DIALOG_ON = 1;
	public static final int PROGRESS_SPINNER_ON = 2;
	public static final int PROGRESS_OFF = 3;
	public static final int TOAST = 4;
	public static final int SET_MAX = 5;
	
	public final int type;
	public final String title;
	public final String message;
	public final int max;
	
	public ProgressMessage(int type, String title, String message)
	{
		this.type = type;
		this.title = title;
		this.message = message;
		this.max = 0;
	}
	
	public ProgressMessage(int type, int max)
	{
		this.type = type;
		this.title = null;
		this.message = null;
		this.max = max;
	}
	
}
