import inspect
import struct
import binascii

# these must match route_type in routes.txt in GTFS
CommuterRailAgencyId = 2
SubwayAgencyId = 1
BusAgencyId = 3
# boat would be 4

exampleSql =     """
CREATE TABLE android_metadata (locale TEXT);
CREATE TABLE directions (dirTag STRING PRIMARY KEY, dirNameKey STRING, dirTitleKey STRING, dirRouteKey STRING, useAsUI INTEGER);
CREATE TABLE directionsStops(dirTag STRING, tag STRING);
CREATE TABLE favorites (tag STRING PRIMARY KEY);
CREATE TABLE routes (route STRING PRIMARY KEY, color INTEGER, oppositecolor INTEGER, pathblob BLOB, routetitle STRING);
CREATE TABLE stopmapping (route STRING, tag STRING, dirTag STRING, PRIMARY KEY (route, tag));
CREATE TABLE stops (tag STRING PRIMARY KEY, lat FLOAT, lon FLOAT, title STRING);
CREATE TABLE subway (tag STRING PRIMARY KEY, platformorder INTEGER, branch STRING);"""

def rawhex(b):
    if b > 0xff or b < 0:
        raise Exception("invalid value for byte: " + b)
    s = hex(b)[2:]
    if len(s) == 1:
        return "0" + s
    else:
        return s

class Box:
    def __init__(self, paths):
        self.bytes = "" # this is hex
        self.add_paths(paths)

    def add_paths(self, paths):
        self.add_int(len(paths))
        for path in paths:
            self.add_path(path)

    def add_path(self, path):
        self.add_int(len(path) * 2)
        for floats in path:
            for f in floats:
                self.add_float(f)
        
    def add_int(self, x):
        self.bytes += binascii.b2a_hex(struct.pack('>i', x))

    def add_float(self, f):
        self.bytes += binascii.b2a_hex(struct.pack('>f', f))

    def get_blob_string(self):
        return "X'" + self.bytes + "'"

def getIntFromBool(boolString):
    if str(boolString).lower() == "true":
        return 1
    else:
        return 0
            

schema = {"directions" : {"columns":[
            {"tag" : "dirTag", "type": "String"},
            {"tag": "dirNameKey", "type": "String"},
            {"tag": "dirTitleKey", "type": "String", "canbenull" : "true"},
            {"tag": "dirRouteKey", "type": "String"},
            {"tag": "useAsUI", "type": "int"}],
                          "primaryKeys" : ["dirTag"],
                          "indexes" : []},
          "directionsStops" : {"columns":[
            {"tag": "dirTag", "type": "String"},
            {"tag": "tag", "type": "String"}],
                               "primaryKeys" : [],
                               "indexes" : []},
          "favorites" : {"columns":[ #NOTE: this is a different database
            {"tag" : "tag", "type" : "String"}],
                         "primaryKeys" : ["tag"],
                         "indexes" : []},
          "routes" : {"columns":[
            {"tag": "route", "type" : "String"},
            {"tag": "color", "type": "int"},
            {"tag": "oppositecolor", "type": "int"},
            {"tag": "pathblob", "type": "byte[]"},
            {"tag": "listorder", "type" :"int"},
            {"tag": "agencyid", "type" : "int", 
             "values" : {CommuterRailAgencyId:"CommuterRail",
                         BusAgencyId : "Bus",
                         SubwayAgencyId : "Subway"}},
            {"tag": "routetitle", "type": "String"}],
                      "primaryKeys" : ["route"],
                          "indexes" : []},
          "stopmapping" : {"columns":[
              {"tag": "route", "type": "String"},
              {"tag": "tag", "type": "String"}],
                           "primaryKeys" : ["route", "tag"],
                           "indexes" : ["route", "tag"]},
          "stops" : {"columns":[
            {"tag": "tag", "type": "String"},
            {"tag": "lat", "type": "float"},
            {"tag": "lon", "type": "float"},
            {"tag": "title", "type": "String"}], 
                     "primaryKeys" : ["tag"],
                     "indexes" : []},
          "locations" : {"columns":[
            {"tag" : "lat", "type" : "float"},
            {"tag" : "lon", "type" : "float"},
            {"tag" : "name", "type" : "String"}],
                         "primaryKeys" : ["name"],
                         "indexes" : []},
          "bounds" : {"columns":[
            {"tag" : "route", "type" : "String"},
            {"tag" : "weekdays", "type" : "int"},
            {"tag" : "start", "type" : "int"},
            {"tag" : "stop", "type" : "int"}],
                      "primaryKeys" : [],
                      "indexes" : []},
          "predictions" : {"columns":[
              {"tag" : "stopid", "type" : "String"},
              {"tag" : "vehicleid", "type" : "String"},
              {"tag" : "route", "type" : "String"},
              {"tag" : "arrivalTimeInMillis", "type" : "long"},
              {"tag" : "affectedByLayover", "type" : "int"},
              {"tag" : "isDelayed", "type" : "int"},
              {"tag" : "lateness", "type" : "int"}],
                           "primaryKeys" : [],
                           "indexes" : ["stopid", "route"]},
          "vehicles" : {"columns":[
              {"tag" : "lat", "type" : "float"},
              {"tag" : "lon", "type" : "float"},
              {"tag" : "vehicleid", "type" : "String"},
              {"tag" : "route", "type" : "String"},
              {"tag" : "lastUpdateInMillis", "type" : "long"},
              {"tag" : "lastFeedUpdateInMillis", "type" : "long"},
              {"tag" : "dirTag", "type" : "String"}],
                        "primaryKeys" : ["vehicleid"],
                        "indexes" : ["route"]}
          }

class Tables:
    pass
class Table:
    def __init__(self, tablename, primaryKeys, indexes):
        self.tablename = tablename
        self.primaryKeys = primaryKeys
        self.indexes = indexes
        self.arguments = []

    def index(self):
        if not self.indexes:
            return
        else:
            for index in self.indexes:
                yield "CREATE INDEX IF NOT EXISTS idx" + self.tablename + index + " ON " + self.tablename + " (" + index + ")";
        
    def create(self):
        createParams = ", ".join(getattr(self, column["tag"]).sqlForColumn(self.primaryKeys) for column in self.arguments)
        if len(self.primaryKeys) > 1:
            createParams += ", PRIMARY KEY (" + ", ".join(self.primaryKeys) + ")"
        return "CREATE TABLE IF NOT EXISTS " + self.tablename + " (" + createParams + ")"

    def insert(self):
        print "INSERT INTO " + self.tablename + " VALUES (",
        print ", ".join(getattr(self, argument["tag"]).insert() for argument in self.arguments),
        print ");"

class Column:
    value = None
    def __init__(self, column_name, type, canbenull, valid_values):
        self.column_name = column_name
        self.data_type = type
        self.canbenull = canbenull.lower() == "true"
        self.valid_values = valid_values

    def sqlForColumn(self, primaryKeys):
        type = "STRING"
        if self.data_type == "float":
            type = "FLOAT"
        elif self.data_type == "byte[]":
            type = "BLOB"
        elif self.data_type == "int":
            type = "INTEGER"
        s = self.column_name + " " + type

        if self.column_name in primaryKeys and len(primaryKeys) == 1:
            s += " PRIMARY KEY"
        return s
    def insert(self):
        value = self.value
        if value is None:
            if self.canbenull:
                value = "NULL"
            else:
                raise Exception("value is null but shouldn't be: " + repr(inspect.getmembers(self)))

        if self.valid_values and value not in self.valid_values:
            raise Exception("invalid value: " + str(value))
            
        if self.data_type == "String":
            value = "\"" + value.replace("\"", "\\\"") + "\""

        self.value = None

        return str(value)


def getSchemaAsObject():
    ret = Tables()
    for tableName, table in schema.iteritems():
        newTable = Table(tableName, table["primaryKeys"], table["indexes"])
        for column in table["columns"]:
            canbenull = "false"
            valid_values = None
            if "canbenull" in column:
                canbenull = column["canbenull"]
            if "values" in column:
                valid_values = column["values"]
            setattr(newTable, column["tag"], Column(column["tag"], column["type"], canbenull, valid_values))
            newTable.arguments += [column]
        setattr(ret, tableName, newTable)
    return ret

