import schema
import inspect
from create_tables import get_create_table_sql

if __name__ == "__main__":
    for sql in get_create_table_sql():
        print(sql)
