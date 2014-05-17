import schema
import inspect
import sqlite3

def create_tables(conn):
    ret = ""
    obj = schema.getSchemaAsObject()
    for tableName in (each[0] for each in inspect.getmembers(obj)):
        if tableName[:2] != "__":
            table = getattr(obj, tableName)
            conn.execute(table.create())
            
            for index in table.index():
                conn.execute(index)

