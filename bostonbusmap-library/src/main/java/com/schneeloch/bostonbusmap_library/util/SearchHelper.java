package com.schneeloch.bostonbusmap_library.util;

import com.schneeloch.bostonbusmap_library.data.TransitSourceTitles;

/**
 * Created by schneg on 1/11/15.
 */
public class SearchHelper {

    public static String naiveSearch(String indexingQuery, String lowercaseQuery,
                                     TransitSourceTitles routeKeysToTitles)
    {
        if (routeKeysToTitles.hasRoute(indexingQuery))
        {
            return indexingQuery;
        }
        else
        {
            //try the titles
            for (String route : routeKeysToTitles.routeTags()) {
                String title = routeKeysToTitles.getTitle(route);
                String titleWithoutSpaces = title.toLowerCase().replaceAll(" ", "");
                if (titleWithoutSpaces.equals(lowercaseQuery)) {
                    return route;
                }
            }

            //no match
            return null;
        }
    }
}
