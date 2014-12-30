package boston.Bus.Map.provider;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;

/**
 * Created by schneg on 12/30/14.
 */
public class ResolverWrapper {
    private ContentResolver resolver;

    public ResolverWrapper(ContentResolver resolver) {
        this.resolver = resolver;
    }

    public CursorWrapper query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return new CursorWrapper(resolver.query(uri, projection, selection, selectionArgs, sortOrder));
    }

    public int bulkInsert(Uri uri, ContentValues[] valueses) {
        return this.resolver.bulkInsert(uri, valueses);
    }

    public int delete(Uri uri, String where, String[] selectionArgs) {
        return resolver.delete(uri, where, selectionArgs);
    }

    public int update(Uri uri, ContentValues contentValues, String where, String[] selectionArgs) {
        return resolver.update(uri, contentValues, where, selectionArgs);
    }

    public Uri insert(Uri uri, ContentValues values) {
        return resolver.insert(uri, values);
    }
}
