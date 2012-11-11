package boston.Bus.Map.data;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;

public class TimeBounds {
	private final ImmutableMap<Integer, TimeSpan> bounds;
	
	private static final int MONDAY = 0x1;
	private static final int TUESDAY = 0x2;
	private static final int WEDNESDAY = 0x4;
	private static final int THURSDAY = 0x8;
	private static final int FRIDAY = 0x10;
	private static final int SATURDAY = 0x20;
	private static final int SUNDAY = 0x40;
	
	private TimeBounds(Builder builder) {
		this.bounds = ImmutableMap.copyOf(builder.bounds);
	}
	
	public static class Builder
	{
		private final Map<Integer, TimeSpan> bounds = Maps.newHashMap();
		
		public void add(int weekdaysBits, int start, int end) {
			bounds.put(weekdaysBits, new TimeSpan(start, end));
		}

		public TimeBounds build() {
			return new TimeBounds(this);
		}
	}
	
	private static class TimeSpan
	{
		public final int begin;
		public final int end;
		
		public TimeSpan(int begin, int end) {
			this.begin = begin;
			this.end = end;
		}
	}
	
	private static boolean doesRouteRunOnDayOfWeek(int dayOfWeek, int weekdayBits) {
		switch (dayOfWeek) {
		case Calendar.MONDAY:
			if ((weekdayBits & MONDAY) == 0) {
				return false;
			}
			break;
		case Calendar.TUESDAY:
		    if ((weekdayBits & TUESDAY) == 0) {
		        return false;
		    }
		    break;
		case Calendar.WEDNESDAY:
		    if ((weekdayBits & WEDNESDAY) == 0) {
		        return false;
		    }
		    break;
		case Calendar.THURSDAY:
		    if ((weekdayBits & THURSDAY) == 0) {
		        return false;
		    }
		    break;
		case Calendar.FRIDAY:
		    if ((weekdayBits & FRIDAY) == 0) {
		        return false;
		    }
		    break;
		case Calendar.SATURDAY:
		    if ((weekdayBits & SATURDAY) == 0) {
		        return false;
		    }
		    break;
		case Calendar.SUNDAY:
		    if ((weekdayBits & SUNDAY) == 0) {
		        return false;
		    }
		    break;
		default:
			throw new RuntimeException("Calendar.DAYOFWEEK returned unexpected value");
		}
		return true;
	}
	
	/**
	 * Checks if route is running at the given time
	 * @param calendar Some calendar, probably today's date, in 
	 * @return
	 */
	public boolean isRouteRunning(Calendar calendar) {
		if (bounds.size() == 0) {
			// this is a just in case measure
			return true;
		}
		
		int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
		
		int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
		int currentMinute = calendar.get(Calendar.MINUTE);
		int currentSecond = calendar.get(Calendar.SECOND);
		
		int secondsFromMidnight = currentSecond + 60*currentMinute + 60*60*currentHour;
		
		/**
		 * end can be greater than 24 hours, if the route ends at 1:30am for instance
		 * 
		 * check if secondsFromMidnight after start and before end for today's day of week
		 * also check for day of week = yesterday if end > 24 hours
		 */
		for	(Integer weekdayBitsObj : bounds.keySet()) {
			TimeSpan timeSpan = bounds.get(weekdayBitsObj);
			int weekdayBits = weekdayBitsObj;

			if (doesRouteRunOnDayOfWeek(dayOfWeek, weekdayBits)) {
				if (secondsFromMidnight >= timeSpan.begin && secondsFromMidnight < timeSpan.end) {
					return true;
				}
			}
			else if (timeSpan.end > 24*60*60) {
				int yesterday = calcYesterday(dayOfWeek);
				
				for (Integer yesterdayWeekdayBitsObj : bounds.keySet()) {
					int yesterdayWeekdayBits = yesterdayWeekdayBitsObj;
					if (doesRouteRunOnDayOfWeek(yesterday, yesterdayWeekdayBits)) {
						if (secondsFromMidnight < timeSpan.end - 24*60*60) {
							return true;
						}
					}
				}
			}
		}
		return false;
	}

	/**
	 * Like calendar.roll(Calendar.DAY_OF_WEEK, false), but without changing calendar object
	 * @param i
	 * @return
	 */
	private int calcYesterday(int today) {
		switch (today) {
		case Calendar.MONDAY:
			return Calendar.SUNDAY;
		case Calendar.TUESDAY:
			return Calendar.MONDAY;
		case Calendar.WEDNESDAY:
			return Calendar.TUESDAY;
		case Calendar.THURSDAY:
			return Calendar.WEDNESDAY;
		case Calendar.FRIDAY:
			return Calendar.THURSDAY;
		case Calendar.SATURDAY:
			return Calendar.FRIDAY;
		case Calendar.SUNDAY:
			return Calendar.SATURDAY;
			default:
				throw new RuntimeException("Unexpected day of week");
		}
	}

	public static boston.Bus.Map.data.TimeBounds.Builder builder() {
		return new boston.Bus.Map.data.TimeBounds.Builder();
	}
}
