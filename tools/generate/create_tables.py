import schema
import inspect
import sqlite3

def get_create_table_sql():
    obj = schema.getSchemaAsObject()
    for tableName in (each[0] for each in inspect.getmembers(obj)):
        if tableName[:2] != "__":
            table = getattr(obj, tableName)
            yield table.create()
            
            for index in table.index():
                yield index
    

def create_tables(conn):
    for sql in get_create_table_sql():
        conn.execute(sql)
