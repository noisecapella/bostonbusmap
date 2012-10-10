package boston.Bus.Map.database;

import android.database.DatabaseUtils.InsertHelper;

public class Schema {
    public static final String dbName = "bostonBusMap";

    public static int toInteger(boolean b) {
        return b ? 1 : 0;
    }


    public static class DirectionsStops {
        public static final String table = "directionsStops"; 
        public static final String[] columns = new String[] {
            "dirTag", "tag"
        };

        public static final int dirTagIndex = 0;
        public static final String dirTagColumn = "dirTag";
        public static final int tagIndex = 1;
        public static final String tagColumn = "tag";

        public static final String createSql = "CREATE TABLE IF NOT EXISTS directionsStops (dirTag STRING, tag STRING)";
        public static void executeInsertHelper(InsertHelper helper, String dirTag, String tag) {
            helper.prepareForReplace();
            helper.bind(dirTagIndex, dirTag);
            helper.bind(tagIndex, tag);
            helper.execute();
        }
    }
    public static class Stops {
        public static final String table = "stops"; 
        public static final String[] columns = new String[] {
            "tag", "lat", "lon", "title"
        };

        public static final int tagIndex = 0;
        public static final String tagColumn = "tag";
        public static final int latIndex = 1;
        public static final String latColumn = "lat";
        public static final int lonIndex = 2;
        public static final String lonColumn = "lon";
        public static final int titleIndex = 3;
        public static final String titleColumn = "title";

        public static final String createSql = "CREATE TABLE IF NOT EXISTS stops (tag STRING PRIMARY KEY, lat FLOAT, lon FLOAT, title STRING)";
        public static void executeInsertHelper(InsertHelper helper, String tag, float lat, float lon, String title) {
            helper.prepareForReplace();
            helper.bind(tagIndex, tag);
            helper.bind(latIndex, lat);
            helper.bind(lonIndex, lon);
            helper.bind(titleIndex, title);
            helper.execute();
        }
    }
    public static class Routes {
        public static final String table = "routes"; 
        public static final String[] columns = new String[] {
            "route", "color", "oppositecolor", "pathblob", "routetitle"
        };

        public static final int routeIndex = 0;
        public static final String routeColumn = "route";
        public static final int colorIndex = 1;
        public static final String colorColumn = "color";
        public static final int oppositecolorIndex = 2;
        public static final String oppositecolorColumn = "oppositecolor";
        public static final int pathblobIndex = 3;
        public static final String pathblobColumn = "pathblob";
        public static final int routetitleIndex = 4;
        public static final String routetitleColumn = "routetitle";

        public static final String createSql = "CREATE TABLE IF NOT EXISTS routes (route STRING PRIMARY KEY, color INTEGER, oppositecolor INTEGER, pathblob BLOB, routetitle STRING)";
        public static void executeInsertHelper(InsertHelper helper, String route, int color, int oppositecolor, byte[] pathblob, String routetitle) {
            helper.prepareForReplace();
            helper.bind(routeIndex, route);
            helper.bind(colorIndex, color);
            helper.bind(oppositecolorIndex, oppositecolor);
            helper.bind(pathblobIndex, pathblob);
            helper.bind(routetitleIndex, routetitle);
            helper.execute();
        }
    }
    public static class Subway {
        public static final String table = "subway"; 
        public static final String[] columns = new String[] {
            "tag", "platformorder", "branch"
        };

        public static final int tagIndex = 0;
        public static final String tagColumn = "tag";
        public static final int platformorderIndex = 1;
        public static final String platformorderColumn = "platformorder";
        public static final int branchIndex = 2;
        public static final String branchColumn = "branch";

        public static final String createSql = "CREATE TABLE IF NOT EXISTS subway (tag STRING PRIMARY KEY, platformorder INTEGER, branch STRING)";
        public static void executeInsertHelper(InsertHelper helper, String tag, int platformorder, String branch) {
            helper.prepareForReplace();
            helper.bind(tagIndex, tag);
            helper.bind(platformorderIndex, platformorder);
            helper.bind(branchIndex, branch);
            helper.execute();
        }
    }
    public static class Favorites {
        public static final String table = "favorites"; 
        public static final String[] columns = new String[] {
            "tag"
        };

        public static final int tagIndex = 0;
        public static final String tagColumn = "tag";

        public static final String createSql = "CREATE TABLE IF NOT EXISTS favorites (tag STRING PRIMARY KEY)";
        public static void executeInsertHelper(InsertHelper helper, String tag) {
            helper.prepareForReplace();
            helper.bind(tagIndex, tag);
            helper.execute();
        }
    }
    public static class Directions {
        public static final String table = "directions"; 
        public static final String[] columns = new String[] {
            "dirTag", "dirNameKey", "dirTitleKey", "dirRouteKey", "useAsUI"
        };

        public static final int dirTagIndex = 0;
        public static final String dirTagColumn = "dirTag";
        public static final int dirNameKeyIndex = 1;
        public static final String dirNameKeyColumn = "dirNameKey";
        public static final int dirTitleKeyIndex = 2;
        public static final String dirTitleKeyColumn = "dirTitleKey";
        public static final int dirRouteKeyIndex = 3;
        public static final String dirRouteKeyColumn = "dirRouteKey";
        public static final int useAsUIIndex = 4;
        public static final String useAsUIColumn = "useAsUI";

        public static final String createSql = "CREATE TABLE IF NOT EXISTS directions (dirTag STRING PRIMARY KEY, dirNameKey STRING, dirTitleKey STRING, dirRouteKey STRING, useAsUI INTEGER)";
        public static void executeInsertHelper(InsertHelper helper, String dirTag, String dirNameKey, String dirTitleKey, String dirRouteKey, int useAsUI) {
            helper.prepareForReplace();
            helper.bind(dirTagIndex, dirTag);
            helper.bind(dirNameKeyIndex, dirNameKey);
            helper.bind(dirTitleKeyIndex, dirTitleKey);
            helper.bind(dirRouteKeyIndex, dirRouteKey);
            helper.bind(useAsUIIndex, useAsUI);
            helper.execute();
        }
    }
    public static class Stopmapping {
        public static final String table = "stopmapping"; 
        public static final String[] columns = new String[] {
            "route", "tag", "dirTag"
        };

        public static final int routeIndex = 0;
        public static final String routeColumn = "route";
        public static final int tagIndex = 1;
        public static final String tagColumn = "tag";
        public static final int dirTagIndex = 2;
        public static final String dirTagColumn = "dirTag";

        public static final String createSql = "CREATE TABLE IF NOT EXISTS stopmapping (route STRING, tag STRING, dirTag STRING, PRIMARY KEY (route, tag))";
        public static void executeInsertHelper(InsertHelper helper, String route, String tag, String dirTag) {
            helper.prepareForReplace();
            helper.bind(routeIndex, route);
            helper.bind(tagIndex, tag);
            helper.bind(dirTagIndex, dirTag);
            helper.execute();
        }
    }
}
