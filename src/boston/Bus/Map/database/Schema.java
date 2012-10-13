package boston.Bus.Map.database;

import android.database.DatabaseUtils.InsertHelper;

import java.util.Collection;

public class Schema {
    public static final String dbName = "bostonBusMap";

    public static int toInteger(boolean b) {
        return b ? 1 : 0;
    }


    public static class Directions {
        public static final String table = "directions"; 
        public static final String[] columns = new String[] {
            "dirTag", "dirNameKey", "dirTitleKey", "dirRouteKey", "useAsUI"
        };

        public static final int dirTagIndex = 1;
        public static final String dirTagColumn = "dirTag";
        public static final int dirNameKeyIndex = 2;
        public static final String dirNameKeyColumn = "dirNameKey";
        public static final int dirTitleKeyIndex = 3;
        public static final String dirTitleKeyColumn = "dirTitleKey";
        public static final int dirRouteKeyIndex = 4;
        public static final String dirRouteKeyColumn = "dirRouteKey";
        public static final int useAsUIIndex = 5;
        public static final String useAsUIColumn = "useAsUI";

        public static final String createSql = "CREATE TABLE IF NOT EXISTS directions (dirTag STRING PRIMARY KEY, dirNameKey STRING, dirTitleKey STRING, dirRouteKey STRING, useAsUI INTEGER)";
        public static class Bean {
            public final String dirTag;
            public final String dirNameKey;
            public final String dirTitleKey;
            public final String dirRouteKey;
            public final int useAsUI;
            public Bean(String dirTag, String dirNameKey, String dirTitleKey, String dirRouteKey, int useAsUI) {
                this.dirTag = dirTag;
                this.dirNameKey = dirNameKey;
                this.dirTitleKey = dirTitleKey;
                this.dirRouteKey = dirRouteKey;
                this.useAsUI = useAsUI;
            }
        }
        public static void executeInsertHelper(InsertHelper helper, Collection<Bean> beans) {
            for (Bean bean : beans) {
                executeInsertHelper(helper, bean.dirTag, bean.dirNameKey, bean.dirTitleKey, bean.dirRouteKey, bean.useAsUI);
            }
        }
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
    public static class DirectionsStops {
        public static final String table = "directionsStops"; 
        public static final String[] columns = new String[] {
            "dirTag", "tag"
        };

        public static final int dirTagIndex = 1;
        public static final String dirTagColumn = "dirTag";
        public static final int tagIndex = 2;
        public static final String tagColumn = "tag";

        public static final String createSql = "CREATE TABLE IF NOT EXISTS directionsStops (dirTag STRING, tag STRING)";
        public static class Bean {
            public final String dirTag;
            public final String tag;
            public Bean(String dirTag, String tag) {
                this.dirTag = dirTag;
                this.tag = tag;
            }
        }
        public static void executeInsertHelper(InsertHelper helper, Collection<Bean> beans) {
            for (Bean bean : beans) {
                executeInsertHelper(helper, bean.dirTag, bean.tag);
            }
        }
        public static void executeInsertHelper(InsertHelper helper, String dirTag, String tag) {
            helper.prepareForReplace();
            helper.bind(dirTagIndex, dirTag);
            helper.bind(tagIndex, tag);
            helper.execute();
        }
    }
    public static class Favorites {
        public static final String table = "favorites"; 
        public static final String[] columns = new String[] {
            "tag"
        };

        public static final int tagIndex = 1;
        public static final String tagColumn = "tag";

        public static final String createSql = "CREATE TABLE IF NOT EXISTS favorites (tag STRING PRIMARY KEY)";
        public static class Bean {
            public final String tag;
            public Bean(String tag) {
                this.tag = tag;
            }
        }
        public static void executeInsertHelper(InsertHelper helper, Collection<Bean> beans) {
            for (Bean bean : beans) {
                executeInsertHelper(helper, bean.tag);
            }
        }
        public static void executeInsertHelper(InsertHelper helper, String tag) {
            helper.prepareForReplace();
            helper.bind(tagIndex, tag);
            helper.execute();
        }
    }
    public static class Routes {
        public static final String table = "routes"; 
        public static final String[] columns = new String[] {
            "route", "color", "oppositecolor", "pathblob", "routetitle"
        };

        public static final int routeIndex = 1;
        public static final String routeColumn = "route";
        public static final int colorIndex = 2;
        public static final String colorColumn = "color";
        public static final int oppositecolorIndex = 3;
        public static final String oppositecolorColumn = "oppositecolor";
        public static final int pathblobIndex = 4;
        public static final String pathblobColumn = "pathblob";
        public static final int routetitleIndex = 5;
        public static final String routetitleColumn = "routetitle";

        public static final String createSql = "CREATE TABLE IF NOT EXISTS routes (route STRING PRIMARY KEY, color INTEGER, oppositecolor INTEGER, pathblob BLOB, routetitle STRING)";
        public static class Bean {
            public final String route;
            public final int color;
            public final int oppositecolor;
            public final byte[] pathblob;
            public final String routetitle;
            public Bean(String route, int color, int oppositecolor, byte[] pathblob, String routetitle) {
                this.route = route;
                this.color = color;
                this.oppositecolor = oppositecolor;
                this.pathblob = pathblob;
                this.routetitle = routetitle;
            }
        }
        public static void executeInsertHelper(InsertHelper helper, Collection<Bean> beans) {
            for (Bean bean : beans) {
                executeInsertHelper(helper, bean.route, bean.color, bean.oppositecolor, bean.pathblob, bean.routetitle);
            }
        }
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
    public static class Stopmapping {
        public static final String table = "stopmapping"; 
        public static final String[] columns = new String[] {
            "route", "tag", "dirTag"
        };

        public static final int routeIndex = 1;
        public static final String routeColumn = "route";
        public static final int tagIndex = 2;
        public static final String tagColumn = "tag";
        public static final int dirTagIndex = 3;
        public static final String dirTagColumn = "dirTag";

        public static final String createSql = "CREATE TABLE IF NOT EXISTS stopmapping (route STRING, tag STRING, dirTag STRING, PRIMARY KEY (route, tag))";
        public static class Bean {
            public final String route;
            public final String tag;
            public final String dirTag;
            public Bean(String route, String tag, String dirTag) {
                this.route = route;
                this.tag = tag;
                this.dirTag = dirTag;
            }
        }
        public static void executeInsertHelper(InsertHelper helper, Collection<Bean> beans) {
            for (Bean bean : beans) {
                executeInsertHelper(helper, bean.route, bean.tag, bean.dirTag);
            }
        }
        public static void executeInsertHelper(InsertHelper helper, String route, String tag, String dirTag) {
            helper.prepareForReplace();
            helper.bind(routeIndex, route);
            helper.bind(tagIndex, tag);
            helper.bind(dirTagIndex, dirTag);
            helper.execute();
        }
    }
    public static class Stops {
        public static final String table = "stops"; 
        public static final String[] columns = new String[] {
            "tag", "lat", "lon", "title"
        };

        public static final int tagIndex = 1;
        public static final String tagColumn = "tag";
        public static final int latIndex = 2;
        public static final String latColumn = "lat";
        public static final int lonIndex = 3;
        public static final String lonColumn = "lon";
        public static final int titleIndex = 4;
        public static final String titleColumn = "title";

        public static final String createSql = "CREATE TABLE IF NOT EXISTS stops (tag STRING PRIMARY KEY, lat FLOAT, lon FLOAT, title STRING)";
        public static class Bean {
            public final String tag;
            public final float lat;
            public final float lon;
            public final String title;
            public Bean(String tag, float lat, float lon, String title) {
                this.tag = tag;
                this.lat = lat;
                this.lon = lon;
                this.title = title;
            }
        }
        public static void executeInsertHelper(InsertHelper helper, Collection<Bean> beans) {
            for (Bean bean : beans) {
                executeInsertHelper(helper, bean.tag, bean.lat, bean.lon, bean.title);
            }
        }
        public static void executeInsertHelper(InsertHelper helper, String tag, float lat, float lon, String title) {
            helper.prepareForReplace();
            helper.bind(tagIndex, tag);
            helper.bind(latIndex, lat);
            helper.bind(lonIndex, lon);
            helper.bind(titleIndex, title);
            helper.execute();
        }
    }
    public static class Subway {
        public static final String table = "subway"; 
        public static final String[] columns = new String[] {
            "tag", "platformorder", "branch"
        };

        public static final int tagIndex = 1;
        public static final String tagColumn = "tag";
        public static final int platformorderIndex = 2;
        public static final String platformorderColumn = "platformorder";
        public static final int branchIndex = 3;
        public static final String branchColumn = "branch";

        public static final String createSql = "CREATE TABLE IF NOT EXISTS subway (tag STRING PRIMARY KEY, platformorder INTEGER, branch STRING)";
        public static class Bean {
            public final String tag;
            public final int platformorder;
            public final String branch;
            public Bean(String tag, int platformorder, String branch) {
                this.tag = tag;
                this.platformorder = platformorder;
                this.branch = branch;
            }
        }
        public static void executeInsertHelper(InsertHelper helper, Collection<Bean> beans) {
            for (Bean bean : beans) {
                executeInsertHelper(helper, bean.tag, bean.platformorder, bean.branch);
            }
        }
        public static void executeInsertHelper(InsertHelper helper, String tag, int platformorder, String branch) {
            helper.prepareForReplace();
            helper.bind(tagIndex, tag);
            helper.bind(platformorderIndex, platformorder);
            helper.bind(branchIndex, branch);
            helper.execute();
        }
    }
}
