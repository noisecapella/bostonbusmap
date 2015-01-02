package boston.Bus.Map.provider;

import android.os.RemoteException;

import com.google.common.collect.ImmutableList;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import boston.Bus.Map.data.Direction;
import boston.Bus.Map.data.IntersectionLocation;
import boston.Bus.Map.data.RouteConfig;
import boston.Bus.Map.data.RouteTitles;
import boston.Bus.Map.data.StopLocation;
import boston.Bus.Map.data.TransitSourceTitles;
import boston.Bus.Map.transit.ITransitSystem;

/**
 * Created by schneg on 1/2/15.
 */
public interface IDatabaseAgent {
    void populateFavorites(CopyOnWriteArraySet<String> favorites);

    ImmutableList<String> getAllStopTagsAtLocation(String stopTag);

    void saveFavorite(Collection<String> allStopTagsAtLocation, boolean isFavorite) throws RemoteException;

    RouteConfig getRoute(String routeToUpdate,
                         ConcurrentMap<String, StopLocation> sharedStops,
                         ITransitSystem transitSystem) throws IOException;

    void refreshDirections(ConcurrentHashMap<String, Direction> directions);

    Collection<StopLocation> getClosestStopsAndFilterRoutes(double currentLat, double currentLon, ITransitSystem transitSystem,
                                                            ConcurrentMap<String, StopLocation> sharedStops, int limit, Set<String> routes);

    Collection<StopLocation> getClosestStops(double currentLat, double currentLon, ITransitSystem transitSystem,
                                             ConcurrentMap<String, StopLocation> sharedStops, int limit);

    StopLocation getStopByTagOrTitle(String tagQuery, String titleQuery, ITransitSystem transitSystem);

    void getStops(ImmutableList<String> stopTags,
                  ITransitSystem transitSystem, ConcurrentMap<String, StopLocation> outputMapping);

    ArrayList<String> getDirectionTagsForStop(String stopTag);

    List<String> getStopTagsForDirTag(String dirTag);

    void populateIntersections(
            ConcurrentMap<String, IntersectionLocation> intersections,
            ITransitSystem transitSystem, ConcurrentMap<String, StopLocation> sharedStops,
            float miles, boolean filterByDistance);

    boolean addIntersection(IntersectionLocation.Builder build, TransitSourceTitles routeTitles);

    RouteTitles getRouteTitles();

    void removeIntersection(String name);

    void editIntersectionName(
            String oldName, String newName);
}
