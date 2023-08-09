public class EnabledProvider extends ContentProvider {
    public boolean onCreate() {
        return false;
    }
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        return null;
    }
    public String getType(Uri uri) {
        return null;
    }
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
