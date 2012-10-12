exampleSql =     """
CREATE TABLE android_metadata (locale TEXT);
CREATE TABLE directions (dirTag STRING PRIMARY KEY, dirNameKey STRING, dirTitleKey STRING, dirRouteKey STRING, useAsUI INTEGER);
CREATE TABLE directionsStops(dirTag STRING, tag STRING);
CREATE TABLE favorites (tag STRING PRIMARY KEY);
CREATE TABLE routes (route STRING PRIMARY KEY, color INTEGER, oppositecolor INTEGER, pathblob BLOB, routetitle STRING);
CREATE TABLE stopmapping (route STRING, tag STRING, dirTag STRING, PRIMARY KEY (route, tag));
CREATE TABLE stops (tag STRING PRIMARY KEY, lat FLOAT, lon FLOAT, title STRING);
CREATE TABLE subway (tag STRING PRIMARY KEY, platformorder INTEGER, branch STRING);"""


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

class Tables:
    pass
class Table:
    def __init__(self, name, primaryKeys):
        self.name = name
        self.primaryKeys = primaryKeys
        self.arguments = []

    def insert(self):
        print "INSERT INTO " + self.name + " VALUES (",
        print ", ".join(getattr(self, argument["tag"]).insert() for argument in self.arguments)

class Column:
    value = None
    def __init__(self, name, type):
        self.name = name
        self.type = type
        self.canbenull = False
    def sqlForColumn(self, primaryKeys):
        type = "STRING"
        if self.type == "float":
            type = "FLOAT"
        elif self.type == "byte[]":
            type = "BLOB"
        elif self.type == "int":
            type = "INTEGER"
        s = self.name + " " + type

        if self.name in primaryKeys and len(primaryKeys) == 1:
            s += " PRIMARY KEY"
        return s
    def insert(self):
        value = self.value
        if not value:
            if self.canbenull:
                value = "NULL"
            else:
                raise "WRONG"
        if self.type == "STRING":
            value = "\"" + self.value.replace("\"", "\\\"") + "\""

        self.value = None
        return value


def getSchemaAsObject():
    ret = Tables()
    for tableName, table in schema.iteritems():
        newTable = Table(tableName, table["primaryKeys"])
        for column in table["columns"]:
            setattr(newTable, column["tag"], Column(column["tag"], column["type"]))
            newTable.arguments += [column]
        setattr(ret, tableName, newTable)
    return ret

