package boston.Bus.Map.data;

import java.io.IOException;
import java.util.List;

import com.google.common.collect.Lists;

import boston.Bus.Map.provider.DatabaseContentProvider;
import boston.Bus.Map.util.Box;

public class TripInfo {
	private final String tripId;
	private final String routeId;
	private final int secondsOffset;
	private final int[] arrivalSeconds;
	private final int[] sequences;
	private final String[] stopIds;
	
	public TripInfo(String tripId, String routeId, int secondsOffset,
			byte[] arrivalsBlob, byte[] stopListBlob) throws IOException {
		this.tripId = tripId;
		this.routeId = routeId;
		this.secondsOffset = secondsOffset;
		
		Box arrivals = new Box(arrivalsBlob, DatabaseContentProvider.CURRENT_DB_VERSION);
		short arrivalsLen = arrivals.readShort();
		
		Box stopList = new Box(stopListBlob, DatabaseContentProvider.CURRENT_DB_VERSION);
		short stopListLen = stopList.readShort();
		
		stopIds = new String[stopListLen];
		sequences = new int[stopListLen];
		arrivalSeconds = new int[stopListLen];
		for (int i = 0; i < arrivalsLen; i++) {
			String stopId = stopList.readStringUnique();
			byte sequence = stopList.readByte();
			short arrivalMinutes = arrivals.readShort();
			
			stopIds[i] = stopId;
			sequences[i] = sequence;
			int arrivalSecond = arrivalMinutes * 60;
			arrivalSeconds[i] = arrivalSecond;
		}
	}
}
