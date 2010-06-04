package boston.Bus.Map;

public class Prediction {
	private final int seconds;
	private final long epochTime;
	private final int vehicleId;
	private final TriState inBound;
	
	public Prediction(int seconds, long epochTime, int vehicleId,
			TriState inBound) {
		this.seconds = seconds;
		this.epochTime = epochTime;
		this.vehicleId = vehicleId;
		this.inBound = inBound;
	}

	@Override
	public String toString() {
		long timeLeft = (seconds * 1000) - (System.currentTimeMillis() - epochTime);
		int minutesLeft = (int)(timeLeft / 1000 / 60);
		
		if (minutesLeft < 0)
		{
			return "";
		}
		else if (minutesLeft == 0)
		{
			return "Bus " + vehicleId + " arriving now!";
		}
		else
		{
			return "Bus " + vehicleId + " arriving in " + minutesLeft + " min";
		}
	}
}
