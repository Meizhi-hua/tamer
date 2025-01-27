public class Movie {
    private final int mNativeMovie;
    private Movie(int nativeMovie) {
        if (nativeMovie == 0) {
            throw new RuntimeException("native movie creation failed");
        }
        mNativeMovie = nativeMovie;
    }
    public native int width();
    public native int height();
    public native boolean isOpaque();
    public native int duration();
    public native boolean setTime(int relativeMilliseconds);    
    public native void draw(Canvas canvas, float x, float y, Paint paint);
    public void draw(Canvas canvas, float x, float y) {
        draw(canvas, x, y, null);
    }
    public static native Movie decodeStream(InputStream is);
    public static native Movie decodeByteArray(byte[] data, int offset,
                                               int length);
    public static Movie decodeFile(String pathName) {
        InputStream is;
        try {
            is = new FileInputStream(pathName);
        }
        catch (java.io.FileNotFoundException e) {
            return null;
        }
        return decodeTempStream(is);
    }
    private static Movie decodeTempStream(InputStream is) {
        Movie moov = null;
        try {
            moov = decodeStream(is);
            is.close();
        }
        catch (java.io.IOException e) {
        }
        return moov;
    }
}
