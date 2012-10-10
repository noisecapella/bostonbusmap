indent = "    "
indent2 = indent * 2
indent3 = indent * 3
exampleSql =     """
CREATE TABLE android_metadata (locale TEXT);
CREATE TABLE directions (dirTag STRING PRIMARY KEY, dirNameKey STRING, dirTitleKey STRING, dirRouteKey STRING, useAsUI INTEGER);
CREATE TABLE directionsStops(dirTag STRING, tag STRING);
CREATE TABLE favorites (tag STRING PRIMARY KEY);
CREATE TABLE routes (route STRING PRIMARY KEY, color INTEGER, oppositecolor INTEGER, pathblob BLOB, routetitle STRING);
CREATE TABLE stopmapping (route STRING, tag STRING, dirTag STRING, PRIMARY KEY (route, tag));
CREATE TABLE stops (tag STRING PRIMARY KEY, lat FLOAT, lon FLOAT, title STRING);
CREATE TABLE subway (tag STRING PRIMARY KEY, platformorder INTEGER, branch STRING);"""

def capitalizeFirst(s):
    return s[0].upper() + s[1:]

def sqlForColumn(tableInfo, column):
    type = "STRING"
    if column["type"] == "float":
        type = "FLOAT"
    elif column["type"] == "byte[]":
        type = "BLOB"
    elif column["type"] == "int":
        type = "INTEGER"
    s = column["tag"] + " " + type

    primaryKeys = tableInfo["primaryKeys"]
    if column["tag"] in primaryKeys and len(primaryKeys) == 1:
        s += " PRIMARY KEY"
    return s

def writeTable(tableName, tableInfo):
    columns = tableInfo["columns"]
    print indent + "public static class %s {" % (capitalizeFirst(tableName),)
    print indent2 + "public static final String table = \"%s\"; "%(tableName,)
    print indent2 + "public static final String[] columns = new String[] {"
    quotedColumns = ", ".join(("\"" + each["tag"] + "\"") for each in columns)
    print indent3 + quotedColumns

    print indent2 + "};"

    print
    for i in xrange(len(columns)):
        column = columns[i]
        print indent2 + "public static final int " + column["tag"] + "Index = " + str(i) + ";"
        print indent2 + "public static final String " + column["tag"] + "Column = \"" + column["tag"] + "\";"
    print

    createParams = ", ".join(sqlForColumn(tableInfo, column) for column in columns)
    primaryKeys = tableInfo["primaryKeys"]
    if len(primaryKeys) > 1:
        createParams += ", PRIMARY KEY (" + ", ".join(primaryKeys) + ")"
    print indent2 + "public static final String createSql = \"CREATE TABLE IF NOT EXISTS " + tableName + " (" + createParams + ")\";"

    params = ", ".join((column["type"] + " " + column["tag"]) for column in columns)

    print indent2 + "public static void executeInsertHelper(InsertHelper helper, " + params + ") {"

    print indent3 + "helper.prepareForReplace();"
    for column in columns:
        print indent3 + "helper.bind(" + column["tag"] + "Index, " + column["tag"] + ");"
    print indent3 + "helper.execute();"

    print indent2 + "}"

    print indent + "}"


def main():
    print """package boston.Bus.Map.database;

import android.database.DatabaseUtils.InsertHelper;

public class Schema {
    public static final String dbName = "bostonBusMap";

    public static int toInteger(boolean b) {
        return b ? 1 : 0;
    }

"""

    schema = {"directions" : {"columns":[
            {"tag" : "dirTag", "type": "String"},
            {"tag": "dirNameKey", "type": "String"},
            {"tag": "dirTitleKey", "type": "String"},
            {"tag": "dirRouteKey", "type": "String"},
            {"tag": "useAsUI", "type": "int"}], "primaryKeys" : ["dirTag"]},
              "directionsStops" : {"columns":[
            {"tag": "dirTag", "type": "String"},
            {"tag": "tag", "type": "String"}], "primaryKeys" : []},
              "favorites" : {"columns":[
            {"tag" : "tag", "type" : "String"}], "primaryKeys" : ["tag"]},
              "routes" : {"columns":[
            {"tag": "route", "type" : "String"},
            {"tag": "color", "type": "int"},
            {"tag": "oppositecolor", "type": "int"},
            {"tag": "pathblob", "type": "byte[]"},
            {"tag": "routetitle", "type": "String"}], "primaryKeys" : ["route"]},
              "stopmapping" : {"columns":[
            {"tag": "route", "type": "String"},
            {"tag": "tag", "type": "String"},
            {"tag": "dirTag", "type": "String"}], "primaryKeys" : ["route", "tag"]},
              "stops" : {"columns":[
            {"tag": "tag", "type": "String"},
            {"tag": "lat", "type": "float"},
            {"tag": "lon", "type": "float"},
            {"tag": "title", "type": "String"}], "primaryKeys" : ["tag"]},
              "subway" : {"columns":[
            {"tag": "tag", "type": "String"},
            {"tag": "platformorder", "type": "int"},
            {"tag": "branch", "type": "String"}], "primaryKeys" : ["tag"]}}

    for table in schema.iteritems():
        tableName, columns = table

        writeTable(tableName, columns)

    print """}"""

if __name__ == "__main__":
    main()
