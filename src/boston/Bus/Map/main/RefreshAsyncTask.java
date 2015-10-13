package boston.Bus.Map.main;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.maps.GeoPoint;
import com.google.common.collect.ImmutableList;

import android.content.OperationApplicationException;
import android.os.RemoteException;
import com.schneeloch.bostonbusmap_library.data.Location;
import com.schneeloch.bostonbusmap_library.data.Locations;
import com.schneeloch.bostonbusmap_library.data.Selection;
import boston.Bus.Map.data.UpdateArguments;
import boston.Bus.Map.ui.ProgressMessage;
import com.schneeloch.bostonbusmap_library.util.Constants;
import com.schneeloch.bostonbusmap_library.util.FeedException;
import com.schneeloch.bostonbusmap_library.util.LogUtil;

public class RefreshAsyncTask extends UpdateAsyncTask
{
	public RefreshAsyncTask(UpdateArguments arguments,
			boolean doShowUnpredictable, int maxOverlays,
			Selection selection, UpdateHandler handler) {
		super(arguments, doShowUnpredictable, maxOverlays,
				selection, handler, null);
	}

	@Override
	protected boolean doUpdate() throws RemoteException, OperationApplicationException {
		try
		{
			Locations busLocations = arguments.getBusLocations();
			publish(new ProgressMessage(ProgressMessage.PROGRESS_SPINNER_ON, null, null));
			
			LatLng geoPoint = currentMapCenter;

			busLocations.refresh(arguments.getDatabaseAgent(), selection,
					geoPoint.latitude, geoPoint.longitude, arguments.getOverlayGroup().isShowLine());
			return true;
		}
		catch (IOException e)
		{
			//this probably means that there is no Internet available, or there's something wrong with the feed
			publish(new ProgressMessage(ProgressMessage.TOAST, null, "Feed is inaccessible; try again later"));

			LogUtil.e(e);
			
			return false;
		} catch (SAXException e) {
			publish(new ProgressMessage(ProgressMessage.TOAST, null,
					"XML parsing exception; cannot update. Maybe there was a hiccup in the feed?"));

			LogUtil.e(e);
			
			return false;
		} catch (NumberFormatException e) {
			publish(new ProgressMessage(ProgressMessage.TOAST, null, "XML number parsing exception; cannot update. Maybe there was a hiccup in the feed?"));

			LogUtil.e(e);
			
			return false;
		} catch (ParserConfigurationException e) {
			publish(new ProgressMessage(ProgressMessage.TOAST, null, "XML parser configuration exception; cannot update"));

			LogUtil.e(e);
			
			return false;
		} catch (FactoryConfigurationError e) {
			publish(new ProgressMessage(ProgressMessage.TOAST, null, "XML parser factory configuration exception; cannot update"));

			LogUtil.e(e);
			
			return false;
		}
        catch (FeedException e) {
            publish(new ProgressMessage(ProgressMessage.TOAST, null, e.getMessage()));

            LogUtil.e(e);

            return false;
        }
        catch (AssertionError e)
		{
			Throwable cause = e.getCause();
			if (cause != null)
			{
				if (cause instanceof SocketTimeoutException)
				{
					publish(new ProgressMessage(ProgressMessage.TOAST, null, "Connection timed out"));

					LogUtil.e(e);
					
					return false;
				}
				else if (cause instanceof SocketException)
				{
					publish(new ProgressMessage(ProgressMessage.TOAST, null, "Connection error occurred"));

					LogUtil.e(e);
					
					return false;
				}
				else
				{
					publish(new ProgressMessage(ProgressMessage.TOAST, null, "Unknown exception occurred"));

					LogUtil.e(e);
					
					return false;
				}
			}
			else
			{
				publish(new ProgressMessage(ProgressMessage.TOAST, null, "Unknown exception occurred"));

				LogUtil.e(e);
				
				return false;
			}
		}
		finally
		{
			//we should always set the icon to invisible afterwards just in case
			publish(new ProgressMessage(ProgressMessage.PROGRESS_OFF, null, null));
		}
	}

	@Override
	protected boolean areUpdatesSilenced() {
		return false;
	}

	@Override
	protected void postExecute(ImmutableList<Location> locationsNearCenter) {
		if (locationsNearCenter != null && locationsNearCenter.size() == 0)
		{
			//no data? oh well
			//sometimes the feed provides an empty XML message; completely valid but without any vehicle elements
			publish(new ProgressMessage(ProgressMessage.TOAST, null, "Finished update, no data provided"));

			//an error probably occurred; keep buses where they were before, and don't overwrite message in textbox
			return;
		}
		
		super.postExecute(locationsNearCenter);
	}

    @Override
    protected boolean forceNewMarker() {
        return true;
    }
}
