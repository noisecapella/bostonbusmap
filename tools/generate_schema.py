indent = "    "
indent2 = indent * 2
indent3 = indent * 3

def capitalizeFirst(s):
    return s[0].upper() + s[1:]

def writeTable(tableName, columns):
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

    params = ", ".join((each["type"] + " " + each["tag"]) for each in columns)

    print indent2 + "public static void bindInsertHelper(InsertHelper helper, " + params + ") {"

    print indent3 + "helper.prepareForReplace();"
    for each in columns:
        print indent3 + "helper.bind(" + each["tag"] + "Index, " + each["tag"] + ");"
    print indent3 + "helper.execute();"

    print indent2 + "}"
    print indent + "}"


def main():
    print """public class Schema {
"""
    """
CREATE TABLE android_metadata (locale TEXT);
CREATE TABLE directions (dirTag STRING PRIMARY KEY, dirNameKey STRING, dirTitleKey STRING, dirRouteKey STRING, useAsUI INTEGER);
CREATE TABLE directionsStops(dirTag STRING, tag STRING);
CREATE TABLE favorites (tag STRING PRIMARY KEY);
CREATE TABLE routes (route STRING PRIMARY KEY, color INTEGER, oppositecolor INTEGER, pathblob BLOB, routetitle STRING);
CREATE TABLE stopmapping (route STRING, tag STRING, dirTag STRING, PRIMARY KEY (route, tag));
CREATE TABLE stops (tag STRING PRIMARY KEY, lat FLOAT, lon FLOAT, title STRING);
CREATE TABLE subway (tag STRING PRIMARY KEY, platformorder INTEGER, branch STRING);"""

    schema = {"directions" : [
            {"tag" : "dirTag", "type": "String"},
            {"tag": "dirNameKey", "type": "String"},
            {"tag": "dirTitleKey", "type": "String"},
            {"tag": "dirRouteKey", "type": "String"},
            {"tag": "useAsUI", "type": "int"}],
              "directionsStops" : [
            {"tag": "dirTag", "type": "String"},
            {"tag": "tag", "type": "String"}],
              "favorites" : [
            {"tag" : "tag", "type" : "String"}],
              "routes" : [
            {"tag": "route", "type" : "String"},
            {"tag": "color", "type": "int"},
            {"tag": "oppositecolor", "type": "int"},
            {"tag": "pathblob", "type": "byte[]"},
            {"tag": "routetitle", "type": "String"}],
              "stopmapping" : [
            {"tag": "route", "type": "String"},
            {"tag": "tag", "type": "String"},
            {"tag": "dirTag", "type": "String"}],
              "stops" : [
            {"tag": "tag", "type": "String"},
            {"tag": "lat", "type": "float"},
            {"tag": "lon", "type": "float"},
            {"tag": "title", "type": "String"}],
              "subway" : [
            {"tag": "tag", "type": "String"},
            {"tag": "platformorder", "type": "int"},
            {"tag": "branch", "type": "String"}]}

    for table in schema.iteritems():
        tableName, columns = table

        writeTable(tableName, columns)

    print """}"""

if __name__ == "__main__":
    main()
