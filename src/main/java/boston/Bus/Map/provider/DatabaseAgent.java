package boston.Bus.Map.provider;

import android.app.SearchManager;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.SQLException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.util.Log;

import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArraySet;

import com.schneeloch.bostonbusmap_library.data.Direction;
import com.schneeloch.bostonbusmap_library.data.Favorite;
import com.schneeloch.bostonbusmap_library.data.IntersectionLocation;
import com.schneeloch.bostonbusmap_library.data.Path;
import com.schneeloch.bostonbusmap_library.data.RouteConfig;
import com.schneeloch.bostonbusmap_library.data.RouteTitles;
import com.schneeloch.bostonbusmap_library.data.StopLocation;
import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;
import com.schneeloch.bostonbusmap_library.database.Schema;
import com.schneeloch.bostonbusmap_library.math.Geometry;
import com.schneeloch.bostonbusmap_library.provider.IDatabaseAgent;
import com.schneeloch.bostonbusmap_library.transit.ITransitSystem;
import com.schneeloch.bostonbusmap_library.transit.TransitSource;
import com.schneeloch.bostonbusmap_library.transit.TransitSystem;

import com.schneeloch.bostonbusmap_library.util.Box;
import com.schneeloch.bostonbusmap_library.util.Constants;
import com.schneeloch.bostonbusmap_library.util.LogUtil;
import com.schneeloch.bostonbusmap_library.util.StringUtil;


public class DatabaseAgent implements IDatabaseAgent {
    private final ContentResolver resolver;

    public DatabaseAgent(ContentResolver resolver) {
        this.resolver = resolver;
    }

    /**
     * Fill the given HashSet with all stop tags that are favorites
     * @param favorites
     */
    @Override
    public void populateFavorites(CopyOnWriteArraySet<String> favorites)
    {
        Cursor cursor = null;
        try
        {
            cursor = resolver.query(FavoritesContentProvider.FAVORITES_URI, new String[]{Schema.Favorites.tagColumn},
                    null, null, null);

            cursor.moveToFirst();
            List<String> toWrite = Lists.newArrayList();
            while (cursor.isAfterLast() == false)
            {
                String favoriteStopKey = cursor.getString(0);

                toWrite.add(favoriteStopKey);

                cursor.moveToNext();
            }
            favorites.addAll(toWrite);
        }
        finally
        {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    @Override
    public ImmutableList<String> getAllStopTagsAtLocation(String stopTag)
    {
        Cursor cursor = null;
        try
        {
            cursor = resolver.query(DatabaseContentProvider.STOPS_STOPS_URI,
                    new String[]{"s2." + Schema.Stops.tagColumn}, "s1." + Schema.Stops.tagColumn + " = ? AND s1." + Schema.Stops.latColumn + " = s2." + Schema.Stops.latColumn +
                            " AND s1." + Schema.Stops.lonColumn + " = s2." + Schema.Stops.lonColumn + "", new String[]{stopTag}, null);

            ImmutableList.Builder<String> ret = ImmutableList.builder();
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false)
            {
                String tag = cursor.getString(0);
                ret.add(tag);

                cursor.moveToNext();
            }

            return ret.build();
        }
        finally
        {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private void storeFavorite(Collection<String> stopTags) throws RemoteException
    {
        if (stopTags == null || stopTags.size() == 0)
        {
            return;
        }

        List<ContentValues> allValues = Lists.newArrayList();
        for (String tag : stopTags)
        {
            ContentValues values = new ContentValues();
            values.put(Schema.Favorites.tagColumn, tag);
            allValues.add(values);
        }

        resolver.bulkInsert(FavoritesContentProvider.FAVORITES_URI, allValues.toArray(new ContentValues[0]));
    }


    @Override
    public void saveFavorite(Collection<String> allStopTagsAtLocation, Favorite isFavorite) throws RemoteException {
        if (isFavorite == Favorite.IsFavorite)
        {
            storeFavorite(allStopTagsAtLocation);
        }
        else
        {
            //delete all stops at location
            resolver.delete(FavoritesContentProvider.FAVORITES_URI, Schema.Favorites.tagColumn + " IN (" + StringUtil.quotedJoin(allStopTagsAtLocation) + ")", null);
        }
    }

    @Override
    public RouteConfig getRoute(String routeToUpdate,
                                ConcurrentMap<String, StopLocation> sharedStops,
                                ITransitSystem transitSystem) throws IOException {

        //get the route-specific information, like the path outline and the color
        RouteConfig.Builder routeConfigBuilder = null;
        {
            Cursor cursor = null;
            try
            {
                String[] projectionIn = new String[]{Schema.Routes.colorColumn,
                        Schema.Routes.oppositecolorColumn, Schema.Routes.pathblobColumn,
                        Schema.Routes.routetitleColumn, Schema.Routes.listorderColumn,
                        Schema.Routes.agencyidColumn,
                        Schema.Bounds.weekdaysColumn, Schema.Bounds.startColumn,
                        Schema.Bounds.stopColumn};
                cursor = resolver.query(DatabaseContentProvider.ROUTES_AND_BOUNDS_URI, projectionIn,
                        Schema.Routes.routeColumnOnTable + "=?",
                        new String[]{routeToUpdate}, null);
                if (cursor.getCount() == 0)
                {
                    return null;
                }

                cursor.moveToFirst();

                while (cursor.isAfterLast() == false) {
                    if (routeConfigBuilder == null) {
                        TransitSource source = transitSystem.getTransitSource(routeToUpdate);

                        int color = cursor.getInt(0);
                        int oppositeColor = cursor.getInt(1);
                        byte[] pathsBlob = cursor.getBlob(2);
                        String routeTitle = cursor.getString(3);
                        int listorder = cursor.getInt(4);
                        int transitSourceId = cursor.getInt(5);

                        Schema.Routes.SourceId sourceId = Schema.Routes.SourceId.fromValue(transitSourceId);

                        Box pathsBlobBox = new Box(pathsBlob);

                        routeConfigBuilder = new RouteConfig.Builder(routeToUpdate, routeTitle,
                                color, oppositeColor, source, listorder, sourceId, pathsBlobBox);
                    }
                    if (!cursor.isNull(6)) {
                        int weekdays = cursor.getInt(6);
                        int start = cursor.getInt(7);
                        int stop = cursor.getInt(8);
                        routeConfigBuilder.addTimeBound(weekdays, start, stop);
                    }
                    cursor.moveToNext();
                }
            }
            finally
            {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        {
            // Aiming to save memory for often repeated strings
            Map<String, String> parentStrings = Maps.newHashMap();

            // get all stops, joining in stops again to get every route for every stop
            String[] projectionIn = new String[] {
                    Schema.Stops.tagColumnOnTable, Schema.Stops.latColumn, Schema.Stops.lonColumn,
                    Schema.Stops.titleColumn, Schema.Stops.parentColumn, "sm2." + Schema.Stopmapping.routeColumn,
            };
            String select = "sm1." + Schema.Stopmapping.routeColumn + "=?";
            String[] selectArray = new String[]{routeToUpdate};

            Cursor cursor = null;
            try
            {
                cursor = resolver.query(DatabaseContentProvider.STOPS_LOOKUP_URI, projectionIn, select, selectArray, null);

                cursor.moveToFirst();
                while (cursor.isAfterLast() == false)
                {
                    String stopTag = cursor.getString(0);
                    String route = cursor.getString(5);

                    //we need to ensure this stop is in the sharedstops and the route
                    StopLocation stop = sharedStops.get(stopTag);
                    if (stop != null)
                    {
                        //make sure it exists in the route too
                        if (routeConfigBuilder.containsStop(stopTag) == false)
                        {
                            routeConfigBuilder.addStop(stopTag, stop);
                        }
                        stop.addRoute(route);
                    }
                    else
                    {
                        stop = routeConfigBuilder.getStop(stopTag);

                        if (stop == null)
                        {
                            float latitude = cursor.getFloat(1);
                            float longitude = cursor.getFloat(2);
                            String stopTitle = cursor.getString(3);
                            String parentFromCursor = cursor.getString(4);

                            Optional<String> parent;
                            if (StringUtil.isEmpty(parentFromCursor)) {
                                parent = Optional.absent();
                            }
                            else if (parentStrings.containsKey(parentFromCursor)) {
                                parent = Optional.of(parentStrings.get(parentFromCursor));
                            }
                            else {
                                parentStrings.put(parentFromCursor, parentFromCursor);
                                parent = Optional.of(parentFromCursor);
                            }

                            stop = transitSystem.createStop(latitude, longitude, stopTag, stopTitle, route, parent);

                            routeConfigBuilder.addStop(stopTag, stop);
                        }

                        sharedStops.put(stopTag, stop);
                    }
                    cursor.moveToNext();
                }
            }
            finally
            {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }

        return routeConfigBuilder.build();
    }


    /**
     * Populate directions from the database
     *
     * NOTE: these data structures are assumed to be synchronized
     */
    @Override
    public void refreshDirections(ConcurrentHashMap<String, Direction> directions) {
        Cursor cursor = null;
        try
        {
            cursor = resolver.query(DatabaseContentProvider.DIRECTIONS_URI, new String[]{Schema.Directions.dirTagColumn, Schema.Directions.dirNameKeyColumn, Schema.Directions.dirTitleKeyColumn, Schema.Directions.dirRouteKeyColumn, Schema.Directions.useAsUIColumn},
                    null, null, null);
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false)
            {
                String dirTag = cursor.getString(0);
                String dirName = cursor.getString(1);
                String dirTitle = cursor.getString(2);
                String dirRoute = cursor.getString(3);
                boolean dirUseAsUI = Schema.fromInteger(cursor.getInt(4));

                Direction direction = new Direction(dirName, dirTitle, dirRoute, dirUseAsUI);
                directions.put(dirTag, direction);

                cursor.moveToNext();
            }
        }
        finally
        {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static Cursor getCursorForSearch(ContentResolver resolver, String search) {
        String[] columns = new String[] {BaseColumns._ID, SearchManager.SUGGEST_COLUMN_TEXT_1, SearchManager.SUGGEST_COLUMN_QUERY, SearchManager.SUGGEST_COLUMN_TEXT_2};
        MatrixCursor ret = new MatrixCursor(columns);

        addSearchRoutes(resolver, search, ret);
        addSearchStops(resolver, search, ret);


        return ret;
    }

    private static void addSearchRoutes(ContentResolver resolver, String search, MatrixCursor ret)
    {
        if (search == null)
        {
            return;
        }

        Cursor cursor = null;
        try
        {
            cursor = resolver.query(DatabaseContentProvider.ROUTES_URI, new String[]{Schema.Routes.routetitleColumn, Schema.Routes.routeColumn}, Schema.Routes.routetitleColumn + " LIKE ?",
                    new String[]{"%" + search + "%"}, Schema.Routes.routetitleColumn);
            if (cursor.moveToFirst() == false)
            {
                return;
            }

            while (!cursor.isAfterLast())
            {
                String routeTitle = cursor.getString(0);
                String routeKey = cursor.getString(1);

                ret.addRow(new Object[]{ret.getCount(), routeTitle, "route " + routeKey, "Route"});

                cursor.moveToNext();
            }
        }
        finally
        {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static void addSearchStops(ContentResolver resolver, String search, MatrixCursor ret)
    {
        if (search == null)
        {
            return;
        }

        String thisStopTitleKey = Schema.Stops.titleColumnOnTable;
        String[] projectionIn = new String[] {thisStopTitleKey, Schema.Stops.tagColumnOnTable, "r1." + Schema.Routes.routetitleColumn};
        String select = thisStopTitleKey + " LIKE ?";
        String[] selectArray = new String[]{"%" + search + "%"};

        Cursor cursor = null;
        try
        {
            cursor = resolver.query(DatabaseContentProvider.STOPS_LOOKUP_2_URI,
                    projectionIn, select, selectArray, null);

            if (cursor.moveToFirst() == false)
            {
                return;
            }

            int count = 0;
            String prevStopTag = null;
            String prevStopTitle = null;
            StringBuilder routes = new StringBuilder();
            int routeCount = 0;
            while (!cursor.isAfterLast())
            {
                String stopTitle = cursor.getString(0);
                String stopTag = cursor.getString(1);
                String routeTitle = cursor.getString(2);

                if (prevStopTag == null)
                {
                    // do nothing, first row
                    prevStopTag = stopTag;
                    prevStopTitle = stopTitle;
                    routeCount++;
                    routes.append(routeTitle);
                }
                else if (!prevStopTag.equals(stopTag))
                {
                    // change in row. write out this row
                    String routeString = routeCount == 0 ? "Stop"
                            : routeCount == 1 ? ("Stop on route " + routes.toString())
                            : ("Stop on routes " + routes);
                    ret.addRow(new Object[]{count, prevStopTitle, "stop " + prevStopTag, routeString});
                    prevStopTag = stopTag;
                    prevStopTitle = stopTitle;
                    routeCount = 1;
                    routes.setLength(0);
                    routes.append(routeTitle);
                }
                else
                {
                    // just add a new route
                    routes.append(", ");
                    routes.append(routeTitle);
                    routeCount++;
                }


                cursor.moveToNext();
                count++;
            }

            if (prevStopTag != null)
            {
                // at least one row
                String routeString = routeCount == 0 ? "Stop"
                        : routeCount == 1 ? ("Stop on route " + routes.toString())
                        : ("Stop on routes " + routes);
                ret.addRow(new Object[]{count, prevStopTitle, "stop " + prevStopTag, routeString});
            }
        }
        finally
        {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private static Uri appendUris(Uri uri, int... x) {
        for (int id : x) {
            uri = ContentUris.withAppendedId(uri, id);
        }
        return uri;
    }

    @Override
    public Collection<StopLocation> getClosestStopsAndFilterRoutes(double currentLat, double currentLon, ITransitSystem transitSystem,
                                                                   ConcurrentMap<String, StopLocation> sharedStops, int limit, Set<String> routes) {
        return getClosestStops(currentLat, currentLon, transitSystem,
                sharedStops, limit, routes, true);
    }
    @Override
    public Collection<StopLocation> getClosestStops(double currentLat, double currentLon, ITransitSystem transitSystem,
                                                    ConcurrentMap<String, StopLocation> sharedStops, int limit) {
        Set<String> emptySet = Collections.emptySet();
        return getClosestStops(currentLat, currentLon, transitSystem,
                sharedStops, limit, emptySet, false);

    }
    private Collection<StopLocation> getClosestStops(double currentLat, double currentLon, ITransitSystem transitSystem,
                                                            ConcurrentMap<String, StopLocation> sharedStops, int limit, Set<String> routes,
                                                            boolean filterRoutes)
    {
        // what we should scale longitude by for 1 unit longitude to roughly equal 1 unit latitude

        int currentLatAsInt = (int)(currentLat * Constants.E6);
        int currentLonAsInt = (int)(currentLon * Constants.E6);
        Uri uri;
        String[] projectionIn = new String[] {Schema.Stops.tagColumnOnTable, DatabaseContentProvider.distanceKey};
        if (filterRoutes == false) {
            uri = DatabaseContentProvider.STOPS_WITH_DISTANCE_URI;
        }
        else
        {
            uri = DatabaseContentProvider.STOPS_AND_ROUTES_WITH_DISTANCE_URI;
        }
        uri = appendUris(uri, currentLatAsInt, currentLonAsInt, limit);

        Cursor cursor = null;
        try
        {
            String select;
            if (filterRoutes == false) {
                select = null;
            }
            else
            {
                StringBuilder selectBuilder = new StringBuilder();
                selectBuilder.append(Schema.Routes.routeColumn).append(" IN (").append(StringUtil.quotedJoin(routes)).append(")");
                select = selectBuilder.toString();
            }
            cursor = resolver.query(uri, projectionIn, select, null, DatabaseContentProvider.distanceKey);
            if (cursor.moveToFirst() == false)
            {
                return Collections.emptyList();
            }

            ImmutableList.Builder<String> stopTagsBuilder = ImmutableList.builder();
            List<String> stopTagsInAll = Lists.newArrayList();
            while (!cursor.isAfterLast())
            {
                String id = cursor.getString(0);
                if (sharedStops.containsKey(id) == false) {
                    stopTagsBuilder.add(id);
                }
                stopTagsInAll.add(id);

                cursor.moveToNext();
            }
            ImmutableList<String> stopTags = stopTagsBuilder.build();
            getStops(stopTags, transitSystem, sharedStops);

            ImmutableList.Builder<StopLocation> builder = ImmutableList.builder();
            for (String stopTag : stopTagsInAll)
            {
                StopLocation stop = sharedStops.get(stopTag);
                if (stop != null) {
                    builder.add(stop);
                }
            }

            return builder.build();
        }
        finally
        {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public StopLocation getStopByTagOrTitle(String tagQuery, String titleQuery, ITransitSystem transitSystem)
    {
        //TODO: we should have a factory somewhere to abstract details away regarding subway vs bus

        //get stop with name stopTag, joining with the subway table
        String[] projectionIn = new String[] {Schema.Stops.tagColumnOnTable, Schema.Stops.latColumn, Schema.Stops.lonColumn,
                Schema.Stops.titleColumn, Schema.Stops.parentColumn, Schema.Stopmapping.routeColumnOnTable};

        //if size == 1, where clause is tag = ?. if size > 1, where clause is "IN (tag1, tag2, tag3...)"
        StringBuilder select;
        String[] selectArray;

        select = new StringBuilder(
            Schema.Stops.tagColumnOnTable + "=? OR " +
            Schema.Stops.tagColumnOnTable + "=? OR " +
            Schema.Stops.titleColumnOnTable + "=? OR " +
            Schema.Stops.titleColumnOnTable + "=?"
        );
        selectArray = new String[]{tagQuery, titleQuery, tagQuery, titleQuery};

        Cursor stopCursor = null;
        try
        {
            // for string interning
            Map<String, String> parentStrings = Maps.newHashMap();
            stopCursor = resolver.query(DatabaseContentProvider.STOPS_LOOKUP_3_URI, projectionIn, select.toString(), selectArray, null);

            stopCursor.moveToFirst();

            if (stopCursor.isAfterLast() == false)
            {
                String stopTag = stopCursor.getString(0);

                String route = stopCursor.getString(5);

                float lat = stopCursor.getFloat(1);
                float lon = stopCursor.getFloat(2);
                String title = stopCursor.getString(3);
                String parentFromCursor = stopCursor.getString(4);

                Optional<String> parent;
                if (parentFromCursor == null || parentFromCursor.length() == 0) {
                    parent = Optional.absent();
                }
                else if (parentStrings.containsKey(parentFromCursor)) {
                    parent = Optional.of(parentStrings.get(parentFromCursor));
                }
                else {
                    parentStrings.put(parentFromCursor, parentFromCursor);
                    parent = Optional.of(parentFromCursor);
                }

                return transitSystem.createStop(lat, lon, stopTag, title, route, parent);
            }
            else
            {
                return null;
            }
        }
        finally
        {
            if (stopCursor != null) {
                stopCursor.close();
            }
        }
    }

    /**
     * Read stops from the database and return a mapping of the stop tag to the stop object
     * @param transitSystem
     * @return
     */
    @Override
    public void getStops(ImmutableList<String> stopTags,
                         ITransitSystem transitSystem, ConcurrentMap<String, StopLocation> outputMapping) {
        if (stopTags == null || stopTags.size() == 0)
        {
            return;
        }

        //TODO: we should have a factory somewhere to abstract details away regarding subway vs bus

        //get stop with name stopTag, joining with the subway table
        String[] projectionIn = new String[] {Schema.Stops.tagColumnOnTable, Schema.Stops.latColumn, Schema.Stops.lonColumn,
                Schema.Stops.titleColumn, Schema.Stops.parentColumn, Schema.Stopmapping.routeColumnOnTable};

        //if size == 1, where clause is tag = ?. if size > 1, where clause is "IN (tag1, tag2, tag3...)"
        StringBuilder select;
        String[] selectArray;
        if (stopTags.size() == 1)
        {
            String stopTag = stopTags.get(0);

            select = new StringBuilder(Schema.Stops.tagColumnOnTable + "=?");
            selectArray = new String[]{stopTag};

            //Log.v("BostonBusMap", SQLiteQueryBuilder.buildQueryString(false, tables, projectionIn, verboseStops + "." + stopTagKey + "=\"" + stopTagKey + "\"",
            //		null, null, null, null));
        }
        else
        {
            select = new StringBuilder(Schema.Stops.tagColumnOnTable + " IN (");

            for (int i = 0; i < stopTags.size(); i++)
            {
                String stopTag = stopTags.get(i);
                select.append('\'').append(stopTag);
                if (i != stopTags.size() - 1)
                {
                    select.append("', ");
                }
                else
                {
                    select.append("')");
                }
            }
            selectArray = null;

            //Log.v("BostonBusMap", select.toString());
        }

        Cursor stopCursor = null;
        try
        {
            Map<String, String> parentStrings = Maps.newHashMap();
            stopCursor = resolver.query(DatabaseContentProvider.STOPS_LOOKUP_3_URI, projectionIn, select.toString(), selectArray, null);

            stopCursor.moveToFirst();

            //iterate through the stops in the database and create new ones if necessary
            //stops will be repeated if they are on multiple routes. If so, just skip to the bottom and add the route and dirTag
            while (stopCursor.isAfterLast() == false)
            {
                String stopTag = stopCursor.getString(0);

                String route = stopCursor.getString(5);

                StopLocation stop = outputMapping.get(stopTag);
                if (stop == null)
                {
                    float lat = stopCursor.getFloat(1);
                    float lon = stopCursor.getFloat(2);
                    String title = stopCursor.getString(3);
                    String parentFromCursor = stopCursor.getString(4);

                    Optional<String> parent;
                    if (parentFromCursor == null || parentFromCursor.length() == 0) {
                        parent = Optional.absent();
                    }
                    else if (parentStrings.containsKey(parentFromCursor)) {
                        parent = Optional.of(parentStrings.get(parentFromCursor));
                    }
                    else {
                        parentStrings.put(parentFromCursor, parentFromCursor);
                        parent = Optional.of(parentFromCursor);
                    }

                    stop = transitSystem.createStop(lat, lon, stopTag, title, route, parent);
                    outputMapping.putIfAbsent(stopTag, stop);
                }
                else
                {
                    stop.addRoute(route);
                }

                stopCursor.moveToNext();
            }
        }
        finally
        {
            if (stopCursor != null) {
                stopCursor.close();
            }
        }
    }

    @Override
    public ArrayList<String> getDirectionTagsForStop(String stopTag) {
        ArrayList<String> ret = Lists.newArrayList();
        Cursor cursor = null;
        try
        {
            cursor = resolver.query(DatabaseContentProvider.DIRECTIONS_STOPS_URI, new String[] {Schema.DirectionsStops.dirTagColumn},
                    Schema.DirectionsStops.tagColumn + " = ?", new String[] {stopTag}, null);

            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                String dirTag = cursor.getString(0);
                ret.add(dirTag);
                cursor.moveToNext();
            }
            return ret;
        }
        finally
        {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public List<String> getStopTagsForDirTag(String dirTag) {
        ArrayList<String> ret = Lists.newArrayList();
        Cursor cursor = null;
        try
        {
            cursor = resolver.query(DatabaseContentProvider.DIRECTIONS_STOPS_URI, new String[] {Schema.DirectionsStops.tagColumn},
                    Schema.DirectionsStops.dirTagColumn + " = ?", new String[] {dirTag}, null);

            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                String stopTag = cursor.getString(0);
                ret.add(stopTag);
                cursor.moveToNext();
            }
            return ret;
        }
        finally
        {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    @Override
    public void populateIntersections(
            ConcurrentMap<String, IntersectionLocation> intersections,
            ITransitSystem transitSystem, ConcurrentMap<String, StopLocation> sharedStops,
            float miles, boolean filterByDistance) {

        Map<String, IntersectionLocation.Builder> ret = Maps.newHashMap();

        String[] projectionIn = new String[]{Schema.Locations.nameColumn,
                Schema.Locations.latColumn, Schema.Locations.lonColumn};
        Cursor cursor = resolver.query(FavoritesContentProvider.LOCATIONS_URI, projectionIn, null, null, null);
        try
        {
            cursor.moveToFirst();
            while (cursor.isAfterLast() == false) {
                String title = cursor.getString(0);

                float lat = cursor.getFloat(1);
                float lon = cursor.getFloat(2);
                IntersectionLocation.Builder builder =
                        new IntersectionLocation.Builder(title, lat, lon);
                ret.put(title, builder);

                cursor.moveToNext();
            }
        }
        finally
        {
            if (cursor != null) {
                cursor.close();
            }
        }

        for (String key : ret.keySet()) {
            // get 35 closest stops for each intersection point, and
            // get all routes mentioned in that list
            // then eliminate those which are farther than a mile
            IntersectionLocation.Builder builder = ret.get(key);

            int limit = 35;

            Collection<StopLocation> stops = getClosestStops(
                    builder.getLatitudeAsDegrees(), builder.getLongitudeAsDegrees(),
                    transitSystem, sharedStops, limit);
            Set<String> routes = Sets.newHashSet();
            for (StopLocation stop : stops) {
                float lat = (float) (builder.getLatitudeAsDegrees() * Geometry.degreesToRadians);
                float lon = (float) (builder.getLongitudeAsDegrees() * Geometry.degreesToRadians);
                if (filterByDistance) {
                    double distance = stop.distanceFromInMiles(lat, lon);
                    if (distance < miles) {
                        routes.addAll(stop.getRoutes());
                    }
                }
                else
                {
                    routes.addAll(stop.getRoutes());
                }
            }

            for (String route : routes) {
                builder.addRoute(route);
            }

            intersections.put(key, builder.build(transitSystem.getRouteKeysToTitles()));
        }

    }

    @Override
    public void replaceStops(Collection<StopLocation> stops) {
        if (stops.size() == 0) {
            return;
        }

        List<String> stopIds = Lists.newArrayList();
        for (StopLocation stop : stops) {
            stopIds.add(stop.getStopTag());
        }

        // delete any existing stops
        String[] selectArray;

        String[] questionMarks = new String[stops.size()];
        for (int i = 0; i < stops.size(); i++) {
            questionMarks[i] = "?";
        }
        selectArray = stopIds.toArray(new String[0]);

        String inClause = " IN (" + Joiner.on(',').join(questionMarks) + ")";

        resolver.delete(DatabaseContentProvider.STOPS_URI,
                Schema.Stops.tagColumnOnTable + inClause,
                selectArray);

        resolver.delete(DatabaseContentProvider.STOPS_ROUTES_URI,
                Schema.Stopmapping.tagColumnOnTable + inClause,
                selectArray);
        resolver.delete(DatabaseContentProvider.DIRECTIONS_STOPS_URI,
                Schema.DirectionsStops.tagColumnOnTable + inClause,
                selectArray);

        List<ContentValues> stopsToInsert = Lists.newArrayList();
        List<ContentValues> stopRoutesToInsert = Lists.newArrayList();

        for (StopLocation stop : stops) {
            ContentValues stopValues = new ContentValues();
            stopValues.put(Schema.Stops.tagColumn, stop.getStopTag());
            stopValues.put(Schema.Stops.titleColumn, stop.getTitle());
            stopValues.put(Schema.Stops.latColumn, stop.getLatitudeAsDegrees());
            stopValues.put(Schema.Stops.lonColumn, stop.getLongitudeAsDegrees());
            stopsToInsert.add(stopValues);

            for (String route : stop.getRoutes()) {
                ContentValues stopRouteValues = new ContentValues();
                stopRouteValues.put(Schema.Stopmapping.routeColumn, route);
                stopRouteValues.put(Schema.Stopmapping.tagColumn, stop.getStopTag());
                stopRoutesToInsert.add(stopRouteValues);
            }

            // TODO: directions. This was originally written for Hubway which doesn't use this
            // table
        }

        resolver.bulkInsert(DatabaseContentProvider.STOPS_URI, stopsToInsert.toArray(new ContentValues[0]));
        resolver.bulkInsert(DatabaseContentProvider.STOPS_ROUTES_URI, stopRoutesToInsert.toArray(new ContentValues[0]));
    }

    /**
     *
     * @param build
     * @return true for success, false for failure
     */
    @Override
    public boolean addIntersection(IntersectionLocation.Builder build, TransitSourceTitles routeTitles) {
        // temporary throwaway location. We still need to attach nearby routes to it,
        // that gets done in populateIntersections
        IntersectionLocation location = build.build(routeTitles);
        ContentValues values = new ContentValues();
        values.put(Schema.Locations.nameColumn, location.getName());
        values.put(Schema.Locations.latColumn, location.getLatitudeAsDegrees());
        values.put(Schema.Locations.lonColumn, location.getLongitudeAsDegrees());
        try
        {
            resolver.insert(FavoritesContentProvider.LOCATIONS_URI, values);
            return true;
        }
        catch (SQLException e) {
            LogUtil.e(e);
            return false;
        }
    }

    @Override
    public RouteTitles getRouteTitles() {

        Cursor cursor = resolver.query(DatabaseContentProvider.ROUTES_URI, new String[]{
                        Schema.Routes.routeColumn, Schema.Routes.routetitleColumn, Schema.Routes.agencyidColumn},
                null, null, Schema.Routes.listorderColumn);
        try
        {
            cursor.moveToFirst();

            ImmutableBiMap.Builder<String, String> builder = ImmutableBiMap.builder();
            ImmutableMap.Builder<String, Schema.Routes.SourceId> agencyIdMap = ImmutableMap.builder();
            while (cursor.isAfterLast() == false) {
                String route = cursor.getString(0);
                String routetitle = cursor.getString(1);
                int agencyid = cursor.getInt(2);

                agencyIdMap.put(route, Schema.Routes.SourceId.fromValue(agencyid));
                builder.put(route, routetitle);
                cursor.moveToNext();
            }

            return new RouteTitles(builder.build(), agencyIdMap.build());
        }
        finally
        {
            if (cursor != null) {
                cursor.close();
            }
        }
    }


    @Override
    public void removeIntersection(String name) {
        int result = resolver.delete(FavoritesContentProvider.LOCATIONS_URI, Schema.Locations.nameColumn + "= ?", new String[]{name});
        if (result == 0) {
            Log.e("BostonBusMap", "Failed to delete intersection " + name);
        }
    }

    @Override
    public void editIntersectionName(
            String oldName, String newName) {
        if (oldName.equals(newName))
        {
            return;
        }

        ContentValues values = new ContentValues();
        values.put(Schema.Locations.nameColumn, newName);
        int result = resolver.update(FavoritesContentProvider.LOCATIONS_URI, values, Schema.Locations.nameColumn + "= ?", new String[]{oldName});
        if (result == 0) {
            Log.e("BostonBusMap", "Failed to update intersection");
        }
    }

}
