indent = "    "
indent2 = indent * 2
indent3 = indent * 3
indent4 = indent * 4
from generate import schema
import inspect

def capitalizeFirst(s):
    return s[0].upper() + s[1:]

def writeTable(table):
    tableName = table.tablename
    print(indent + "public static class %s {" % (capitalizeFirst(tableName),))
    print(indent2 + "public static final String table = \"%s\"; "%(tableName,))
    print(indent2 + "public static final String[] columns = new String[] {")
    columns = table.arguments
    quotedColumns = ", ".join(("\"" + each["tag"] + "\"") for each in columns)
    print(indent3 + quotedColumns)

    print(indent2 + "};")

    print()
    for i in range(len(columns)):
        column = columns[i]
        print(indent2 + "public static final int " + column["tag"] + "Index = " + str(i + 1) + ";")
        print(indent2 + "public static final String " + column["tag"] + "Column = \"" + column["tag"] + "\";")
        print(indent2 + "public static final String " + column["tag"] + "ColumnOnTable = \"" + tableName + "." + column["tag"] + "\";")

        if "values" in column:
            print()
            print(indent2 + "public enum SourceId {")
            first = True
            for value, valueName in column["values"].items():
                if first:
                    first = False
                else:
                    print(",")
                print(indent3 + str(valueName) + "(" + str(value) + ")",)
            print(";")
            print(indent3 + "private final int value;")
            print(indent3 + "SourceId(int value) {")
            print(indent3 + "    this.value = value;")
            print(indent3 + "}")
            print(indent3 + "public int getValue() {")
            print(indent3 + "    return value;")
            print(indent3 + "}")
            print(indent3 + "public static SourceId fromValue(int value) {")
            print(indent3 + "    ")
            for value, valueName in column["values"].items():
                print(indent3 + "    if (value == " + str(value) + ") {")
                print(indent3 + "        return " + str(valueName) + ";")
                print(indent3 + "    }")
            print(indent3 + "    throw new RuntimeException(\"Unknown value \" + value);")
            print(indent3 + "}")
            print(indent2 + "}")
            print()
    print()

    print(indent2 + "public static final String dropSql = \"DROP TABLE IF EXISTS " + table.tablename + "\";")
    print(indent2 + "public static final String createSql = \"" + table.create() + "\";")

    params = ", ".join((column["type"] + " " + column["tag"]) for column in columns)

    print(indent2 + "public static class Bean {")
    for column in columns:
        print(indent3 + "public final " + column["type"] + " " + column["tag"] + ";")
    print(indent3 + "public Bean(" + params + ") {")
    for column in columns:
        print(indent4 + "this." + column["tag"] + " = " + column["tag"] + ";")
    print(indent3 + "}")
    print(indent2 + "}")

    beanParams = ", ".join(("bean." + column["tag"]) for column in columns)

    print(indent2 + "public static void executeInsertHelper(InsertHelper helper, Collection<Bean> beans) {")
    print(indent3 + "for (Bean bean : beans) {")
    print(indent4 + "executeInsertHelper(helper, " + beanParams + ");")
    print(indent3 + "}")
    print(indent2 + "}")
    print(indent2 + "public static void executeInsertHelper(InsertHelper helper, " + params + ") {")

    print(indent3 + "helper.prepareForReplace();")
    for column in columns:
        print(indent3 + "helper.bind(" + column["tag"] + "Index, " + column["tag"] + ");")
    print(indent3 + "helper.execute();")

    print(indent2 + "}")

    print(indent + "}")


def main():
    print("""package com.schneeloch.bostonbusmap_library.database;

import android.database.DatabaseUtils.InsertHelper;

import java.util.Collection;

public class Schema {
    public static final String dbName = "transitData";
    public static final String oldDb = "bostonBusMap";

    private static final int INT_TRUE = 1;
    private static final int INT_FALSE = 0;

    public static int toInteger(boolean b) {
        return b ? INT_TRUE : INT_FALSE;
    }

    public static boolean fromInteger(int i) {
        return i == INT_TRUE;
    }

""")

    schemaObj = schema.getSchemaAsObject()
    for tableName in (each[0] for each in inspect.getmembers(schemaObj)):
        if tableName[:2] != "__":
            table = getattr(schemaObj, tableName)

            writeTable(table)

    print("""}""")

if __name__ == "__main__":
    main()
