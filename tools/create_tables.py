import schema
import inspect

if __name__ == "__main__":
    obj = schema.getSchemaAsObject()
    for tableName in (each[0] for each in inspect.getmembers(obj)):
        if tableName[:2] != "__":
            table = getattr(obj, tableName)
            print table.create() + ";"
            
            for index in table.index():
                print index + ";"


"""
-			db.execSQL("CREATE INDEX IF NOT EXISTS " + stopsRoutesMapIndexRoute + " ON " + Schema.Stopmapping.table + " (" + Schema.Stopmapping.routeColumn + ")");
-			db.execSQL("CREATE INDEX IF NOT EXISTS " + stopsRoutesMapIndexTag + " ON " + Schema.Stopmapping.table + " (" + Schema.Stopmapping.tagColumn + ")");
-			db.execSQL("CREATE INDEX IF NOT EXISTS " + directionsStopsMapIndexStop + " ON " + Schema.DirectionsStops.table + " (" + Schema.DirectionsStops.tagColumn + ")");
-			db.execSQL("CREATE INDEX IF NOT EXISTS " + directionsStopsMapIndexDirTag + " ON " + Schema.DirectionsStops.table + " (" + Schema.DirectionsStops.dirTagColumn + ")");
"""
