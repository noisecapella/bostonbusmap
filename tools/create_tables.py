import schema
import inspect

if __name__ == "__main__":
    obj = schema.getSchemaAsObject()
    for tableName in (each[0] for each in inspect.getmembers(obj)):
        if tableName[:2] != "__":
            table = getattr(obj, tableName)
            print table.create() + ";"
