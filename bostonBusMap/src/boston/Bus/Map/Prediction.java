package boston.Bus.Map;

public class Prediction implements Comparable<Prediction>
{
	private final int minutes;
	private final long epochTime;
	private final int vehicleId;
	private final String directionToShow;
	
	public Prediction(int minutes, long epochTime, int vehicleId,
			String directionToShow) {
		this.minutes = minutes;
		this.epochTime = epochTime;
		this.vehicleId = vehicleId;
		this.directionToShow = directionToShow;
	}

	@Override
	public String toString() {
		if (minutes < 0)
		{
			return "";
		}
		else
		{
			String ret = "Bus " + vehicleId + " " + directionToShow;

			if (minutes == 0)
			{
				return ret + " arriving now!";
			}
			else
			{
				return ret + " arriving in " + minutes + " min";
			}
		}			
	}

	@Override
	public int compareTo(Prediction another) {
		return new Integer(minutes).compareTo(another.minutes);
		
	}
}
