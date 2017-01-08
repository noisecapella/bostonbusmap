import inspect
import struct
import binascii

# these must match route_type in routes.txt in GTFS
CommuterRailAgencyId = 2
SubwayAgencyId = 1
BusAgencyId = 3
# boat would be 4
HubwayAgencyId = 50

# TODO: change to SQLAlchemy, whose functionality this duplicates badly


def rawhex(b):
    if b > 0xff or b < 0:
        raise Exception("invalid value for byte: " + b)
    s = hex(b)[2:]
    if len(s) == 1:
        return "0" + s
    else:
        return s

class Box:
    def __init__(self, paths=None):
        self.bytes = "" # this is hex
        if paths is not None:
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
        self.bytes += binascii.b2a_hex(struct.pack('>i', x)).decode('utf-8')

    def add_float(self, f):
        self.bytes += binascii.b2a_hex(struct.pack('>f', f)).decode('utf-8')

    def get_blob_string(self):
        return "X'" + self.bytes + "'"

    def from_hex_string(self, hex_string):
        b = bytes.fromhex(hex_string)
        return self.from_bytes(b)

    def from_bytes(self, b):
        counter = 0
        def unpack_int():
            nonlocal counter
            ret = struct.unpack('>i', b[counter:counter+4])[0]
            counter += 4
            return ret
        def unpack_float():
            nonlocal counter
            ret = struct.unpack('>f', b[counter:counter+4])[0]
            counter += 4
            return ret

        length = unpack_int()

        paths = []
        for i in range(length):
            path_length = int(unpack_int() / 2)
            path = []
            for j in range(path_length):
                float1 = unpack_float()
                float2 = unpack_float()
                path.append((float1, float2))
            paths.append(path)
        if counter != len(b):
            raise Exception("counter was %d but length was %d" % (counter, len(b)))
        return paths
def getIntFromBool(boolString):
    if str(boolString).lower() == "true":
        return 1
    else:
        return 0
            

schema = {
    "bounds": {
        "columns": [
            {
                "tag": "route",
                "type": "String"
            },
            {
                "tag": "weekdays",
                "type": "int"
            },
            {
                "tag": "start",
                "type": "int"
            },
            {
                "tag": "stop",
                "type": "int"
            }
        ],
        "indexes": [],
        "primaryKeys": []
    },
    "directions": {
        "columns": [
            {
                "tag": "dirTag",
                "type": "String"
            },
            {
                "tag": "dirNameKey",
                "type": "String"
            },
            {
                "canbenull": "true",
                "tag": "dirTitleKey",
                "type": "String"
            },
            {
                "tag": "dirRouteKey",
                "type": "String"
            },
            {
                "tag": "useAsUI",
                "type": "int"
            }
        ],
        "indexes": [],
        "primaryKeys": [
            "dirTag"
        ]
    },
    "directionsStops": {
        "columns": [
            {
                "tag": "dirTag",
                "type": "String"
            },
            {
                "tag": "tag",
                "type": "String"
            }
        ],
        "indexes": [],
        "primaryKeys": []
    },
    "favorites": {
        "columns": [
            {
                "tag": "tag",
                "type": "String"
            }
        ],
        "indexes": [],
        "primaryKeys": [
            "tag"
        ]
    },
    "locations": {
        "columns": [
            {
                "tag": "lat",
                "type": "float"
            },
            {
                "tag": "lon",
                "type": "float"
            },
            {
                "tag": "name",
                "type": "String"
            }
        ],
        "indexes": [],
        "primaryKeys": [
            "name"
        ]
    },
    "routes": {
        "columns": [
            {
                "tag": "route",
                "type": "String"
            },
            {
                "tag": "color",
                "type": "int"
            },
            {
                "tag": "oppositecolor",
                "type": "int"
            },
            {
                "tag": "pathblob",
                "type": "byte[]"
            },
            {
                "tag": "listorder",
                "type": "int"
            },
            {
                "tag": "agencyid",
                "type": "int",
                "values": {
                    BusAgencyId: "Bus",
                    CommuterRailAgencyId: "CommuterRail",
                    HubwayAgencyId: "Hubway",
                    SubwayAgencyId: "Subway"
                }
            },
            {
                "tag": "routetitle",
                "type": "String"
            }
        ],
        "indexes": [],
        "primaryKeys": [
            "route"
        ]
    },
    "stopmapping": {
        "columns": [
            {
                "tag": "route",
                "type": "String"
            },
            {
                "tag": "tag",
                "type": "String"
            }
        ],
        "indexes": [
            "route",
            "tag"
        ],
        "primaryKeys": [
            "route",
            "tag"
        ]
    },
    "stops": {
        "columns": [
            {
                "tag": "tag",
                "type": "String"
            },
            {
                "tag": "lat",
                "type": "float"
            },
            {
                "tag": "lon",
                "type": "float"
            },
            {
                "tag": "title",
                "type": "String"
            },
            {
                "tag": "parent",
                "type": "String"
            }
        ],
        "indexes": [],
        "primaryKeys": [
            "tag"
        ]
    }
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
        ret = "INSERT INTO " + self.tablename + " VALUES ("
        ret += ", ".join(getattr(self, argument["tag"]).insert() for argument in self.arguments)
        ret += ")"
        return ret

class Column:
    value = None
    def __init__(self, column_name, type, canbenull, valid_values):
        self.column_name = column_name
        self.data_type = type
        self.canbenull = canbenull.lower() == "true"
        self.valid_values = valid_values

    def sqlForColumn(self, primaryKeys):
        type = "TEXT"
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
    for tableName, table in schema.items():
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

