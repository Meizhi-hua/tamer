public class JPEGImageDecoder extends ImageDecoder {
    private static ColorModel RGBcolormodel;
    private static ColorModel ARGBcolormodel;
    private static ColorModel Graycolormodel;
    private static final Class InputStreamClass = InputStream.class;
    private static native void initIDs(Class InputStreamClass);
    private ColorModel colormodel;
    static {
        java.security.AccessController.doPrivileged(
                  new sun.security.action.LoadLibraryAction("jpeg"));
        initIDs(InputStreamClass);
        RGBcolormodel = new DirectColorModel(24, 0xff0000, 0xff00, 0xff);
        ARGBcolormodel = ColorModel.getRGBdefault();
        byte g[] = new byte[256];
        for (int i = 0; i < 256; i++) {
            g[i] = (byte) i;
        }
        Graycolormodel = new IndexColorModel(8, 256, g, g, g);
    }
    private native void readImage(InputStream is, byte buf[])
        throws ImageFormatException, IOException;
    Hashtable props = new Hashtable();
    public JPEGImageDecoder(InputStreamImageSource src, InputStream is) {
        super(src, is);
    }
    private static void error(String s1) throws ImageFormatException {
        throw new ImageFormatException(s1);
    }
    public boolean sendHeaderInfo(int width, int height,
                                  boolean gray, boolean hasalpha,
                                  boolean multipass)
    {
        setDimensions(width, height);
        setProperties(props);
        if (gray) {
            colormodel = Graycolormodel;
        } else {
            if (hasalpha) {
                colormodel = ARGBcolormodel;
            } else {
                colormodel = RGBcolormodel;
            }
        }
        setColorModel(colormodel);
        int flags = hintflags;
        if (!multipass) {
            flags |= ImageConsumer.SINGLEPASS;
        }
        setHints(hintflags);
        headerComplete();
        return true;
    }
    public boolean sendPixels(int pixels[], int y) {
        int count = setPixels(0, y, pixels.length, 1, colormodel,
                              pixels, 0, pixels.length);
        if (count <= 0) {
            aborted = true;
        }
        return !aborted;
    }
    public boolean sendPixels(byte pixels[], int y) {
        int count = setPixels(0, y, pixels.length, 1, colormodel,
                              pixels, 0, pixels.length);
        if (count <= 0) {
            aborted = true;
        }
        return !aborted;
    }
    public void produceImage() throws IOException, ImageFormatException {
        try {
            readImage(input, new byte[1024]);
            if (!aborted) {
                imageComplete(ImageConsumer.STATICIMAGEDONE, true);
            }
        } catch (IOException e) {
            if (!aborted) {
                throw e;
            }
        } finally {
            close();
        }
    }
    private static final int hintflags =
        ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES |
        ImageConsumer.SINGLEFRAME;
}
