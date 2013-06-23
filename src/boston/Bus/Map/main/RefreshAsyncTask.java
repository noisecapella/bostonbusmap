package boston.Bus.Map.main;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.xml.sax.SAXException;

import com.google.android.maps.GeoPoint;
import com.google.common.collect.ImmutableList;

import android.content.Context;
import android.content.OperationApplicationException;
import android.os.RemoteException;
import boston.Bus.Map.data.Location;
import boston.Bus.Map.data.Locations;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.Selection;
import boston.Bus.Map.data.UpdateArguments;
import boston.Bus.Map.ui.ProgressMessage;
import boston.Bus.Map.util.Constants;
import boston.Bus.Map.util.FeedException;
import boston.Bus.Map.util.LogUtil;
import boston.Bus.Map.util.StringUtil;

public class RefreshAsyncTask extends UpdateAsyncTask
{
	public RefreshAsyncTask(UpdateArguments arguments,
			boolean doShowUnpredictable, int maxOverlays,
			boolean drawCircle, boolean allRoutesBlue, boolean doInit,
			Selection selection, UpdateHandler handler) {
		super(arguments, doShowUnpredictable, maxOverlays, drawCircle, allRoutesBlue,
				doInit, selection, handler, null);
	}

	@Override
	protected boolean doUpdate() throws RemoteException, OperationApplicationException {
		try
		{
			Locations busLocations = arguments.getBusLocations();
			publish(new ProgressMessage(ProgressMessage.PROGRESS_SPINNER_ON, null, null));
			
			GeoPoint geoPoint = currentMapCenter;
			double centerLatitude = geoPoint.getLatitudeE6() * Constants.InvE6;
			double centerLongitude = geoPoint.getLongitudeE6() * Constants.InvE6;

			busLocations.refresh(arguments.getContext(), selection,
					centerLatitude, centerLongitude, this, arguments.getOverlayGroup().getRouteOverlay().isShowLine());
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
		catch (RuntimeException e)
		{
			if (e.getCause() instanceof FeedException)
			{
				publish(new ProgressMessage(ProgressMessage.TOAST, null, "The feed is reporting an error"));

				LogUtil.e(e);
				
				return false;
			}
			else
			{
				throw e;
			}
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
}
