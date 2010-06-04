package boston.Bus.Map;

public class TriState {
	private boolean isSet;
	private boolean value;
	
	public void set(boolean value)
	{
		isSet = true;
		this.value = value;
	}
	
	public boolean isSet()
	{
		return isSet;
	}
	
	public boolean getValue()
	{
		return value;
	}
}
