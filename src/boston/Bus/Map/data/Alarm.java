package boston.Bus.Map.data;

/**
 * Created by schneg on 1/19/14.
 */
public class Alarm {
	private final long alarmTime;
	private final long scheduledTime;
	private final String routeTitle;
	private final String stop;
	private final String directionTitle;
	private final int minutesBefore;

	public Alarm(long alarmTime, long scheduledTime, String routeTitle,
				 String stop, String directionTitle, int minutesBefore) {
		this.alarmTime = alarmTime;
		this.scheduledTime = scheduledTime;
		this.routeTitle = routeTitle;
		this.stop = stop;
		this.directionTitle = directionTitle;
		this.minutesBefore = minutesBefore;
	}

	public long getAlarmTime() {
		return alarmTime;
	}

	public long getScheduledTime() {
		return scheduledTime;
	}

	public String getRouteTitle() {
		return routeTitle;
	}

	public String getStop() {
		return stop;
	}

	public String getDirectionTitle() {
		return directionTitle;
	}

	public int getMinutesBefore() {
		return minutesBefore;
	}

	public static String makeHtml(String routeTitle, String stopTitle, String directionTitle, int minutesFromNow) {
		StringBuilder builder = new StringBuilder();
		builder.append("Route ").append(routeTitle);
		builder.append("<br />").append(stopTitle).append("<br />");
		builder.append(directionTitle).append("<br />");
		builder.append("Estimated ").append(minutesFromNow).append(" minutes");
		return builder.toString();
	}

	public Alarm withUpdatedTime(long alarmTime) {
		return new Alarm(alarmTime, scheduledTime, stop, routeTitle, directionTitle, minutesBefore);
	}
}
