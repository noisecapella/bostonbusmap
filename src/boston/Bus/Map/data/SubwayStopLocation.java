package boston.Bus.Map.data;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import android.graphics.drawable.Drawable;

public class SubwayStopLocation extends StopLocation {

	
	/**
	 * The order of this stop compared to other stops. Optional, used only for subways
	 */
	private int platformOrder;
	
	/**
	 * What branch this subway is on. Optional, only used for subways
	 */
	private String branch;
	
	protected SubwayStopLocation(SubwayBuilder builder)
	{
		super(builder);
		
		this.platformOrder = builder.platformOrder;
		this.branch = builder.branch;
	}
	
	public static class SubwayBuilder extends Builder {
		private final int platformOrder;
		private final String branch;

		public SubwayBuilder(float latitudeAsDegrees,
				float longitudeAsDegrees, String tag,
				String title, int platformOrder, String branch) {
			super(latitudeAsDegrees, longitudeAsDegrees, tag, title);
			
			this.platformOrder = platformOrder;
			this.branch = branch;
		}
		
		@Override
		public SubwayStopLocation build() {
			return new SubwayStopLocation(this);
		}
	}

	public int getPlatformOrder() {
		return platformOrder;
	}

	public String getBranch() {
		return branch;
	}
}
