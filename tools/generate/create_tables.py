import schema
import inspect
import sqlite3
from twisted.internet.defer import inlineCallbacks, returnValue
from twisted.web.client import getPage
from twisted.internet import reactor

def create_tables(conn):
    ret = ""
    obj = schema.getSchemaAsObject()
    for tableName in (each[0] for each in inspect.getmembers(obj)):
        if tableName[:2] != "__":
            table = getattr(obj, tableName)
            conn.execute(table.create())
            
            for index in table.index():
                conn.execute(index)

"""
-			db.execSQL("CREATE INDEX IF NOT EXISTS " + stopsRoutesMapIndexRoute + " ON " + Schema.Stopmapping.table + " (" + Schema.Stopmapping.routeColumn + ")");
-			db.execSQL("CREATE INDEX IF NOT EXISTS " + stopsRoutesMapIndexTag + " ON " + Schema.Stopmapping.table + " (" + Schema.Stopmapping.tagColumn + ")");
-			db.execSQL("CREATE INDEX IF NOT EXISTS " + directionsStopsMapIndexStop + " ON " + Schema.DirectionsStops.table + " (" + Schema.DirectionsStops.tagColumn + ")");
-			db.execSQL("CREATE INDEX IF NOT EXISTS " + directionsStopsMapIndexDirTag + " ON " + Schema.DirectionsStops.table + " (" + Schema.DirectionsStops.dirTagColumn + ")");
"""
