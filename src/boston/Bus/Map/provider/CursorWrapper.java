package boston.Bus.Map.provider;

import android.database.Cursor;

/**
 * Created by schneg on 12/30/14.
 */
public class CursorWrapper {
    private Cursor cursor;

    public CursorWrapper(Cursor cursor) {
        this.cursor = cursor;
    }

    public boolean moveToFirst() {
        return this.cursor.moveToFirst();
    }

    public boolean isAfterLast() {
        return this.cursor.isAfterLast();
    }


    public String getString(int columnIndex) {
        return this.cursor.getString(columnIndex);
    }

    public int getInt(int columnIndex) {
        return this.cursor.getInt(columnIndex);
    }

    public boolean moveToNext() {
        return this.cursor.moveToNext();
    }

    public void close() {
        this.cursor.close();
    }

    public int getCount() {
        return this.cursor.getCount();
    }

    public byte[] getBlob(int columnIndex) {
        return this.cursor.getBlob(columnIndex);
    }

    public boolean isNull(int columnIndex) {
        return cursor.isNull(columnIndex);
    }

    public float getFloat(int columnIndex) {
        return cursor.getFloat(columnIndex);
    }
}
