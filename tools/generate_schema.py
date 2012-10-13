indent = "    "
indent2 = indent * 2
indent3 = indent * 3
indent4 = indent * 4
import schema
import inspect

def capitalizeFirst(s):
    return s[0].upper() + s[1:]

def writeTable(table):
    tableName = table.name
    print indent + "public static class %s {" % (capitalizeFirst(tableName),)
    print indent2 + "public static final String table = \"%s\"; "%(tableName,)
    print indent2 + "public static final String[] columns = new String[] {"
    columns = table.arguments
    quotedColumns = ", ".join(("\"" + each["tag"] + "\"") for each in columns)
    print indent3 + quotedColumns

    print indent2 + "};"

    print
    for i in xrange(len(columns)):
        column = columns[i]
        print indent2 + "public static final int " + column["tag"] + "Index = " + str(i + 1) + ";"
        print indent2 + "public static final String " + column["tag"] + "Column = \"" + column["tag"] + "\";"
    print

    print indent2 + "public static final String createSql = \"" + table.create() + "\";"

    params = ", ".join((column["type"] + " " + column["tag"]) for column in columns)

    print indent2 + "public static class Bean {"
    for column in columns:
        print indent3 + "public final " + column["type"] + " " + column["tag"] + ";"
    print indent3 + "public Bean(" + params + ") {"
    for column in columns:
        print indent4 + "this." + column["tag"] + " = " + column["tag"] + ";"
    print indent3 + "}"
    print indent2 + "}"

    beanParams = ", ".join(("bean." + column["tag"]) for column in columns)

    print indent2 + "public static void executeInsertHelper(InsertHelper helper, Collection<Bean> beans) {"
    print indent3 + "for (Bean bean : beans) {"
    print indent4 + "executeInsertHelper(helper, " + beanParams + ");"
    print indent3 + "}"
    print indent2 + "}"
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

import java.util.Collection;

public class Schema {
    public static final String dbName = "bostonBusMap";

    public static int toInteger(boolean b) {
        return b ? 1 : 0;
    }

"""

    schemaObj = schema.getSchemaAsObject()
    for tableName in (each[0] for each in inspect.getmembers(schemaObj)):
        if tableName[:2] != "__":
            table = getattr(schemaObj, tableName)

            writeTable(table)

    print """}"""

if __name__ == "__main__":
    main()
