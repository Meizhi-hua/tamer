class LCMSImageLayout {
    public static int BYTES_SH(int x) {
        return x;
    }
    public static int EXTRA_SH(int x) {
        return x<<7;
    }
    public static int CHANNELS_SH(int x) {
        return x<<3;
    }
    public static final int SWAPFIRST   = 1<<14;
    public static final int DOSWAP      = 1<<10;
    public static final int PT_RGB_8 =
        CHANNELS_SH(3) | BYTES_SH(1);
    public static final int PT_GRAY_8 =
        CHANNELS_SH(1) | BYTES_SH(1);
    public static final int PT_GRAY_16 =
        CHANNELS_SH(1) | BYTES_SH(2);
    public static final int PT_RGBA_8 =
        EXTRA_SH(1) | CHANNELS_SH(3) | BYTES_SH(1);
    public static final int PT_ARGB_8 =
        EXTRA_SH(1) | CHANNELS_SH(3) | BYTES_SH(1) | SWAPFIRST;
    public static final int PT_BGR_8 =
        DOSWAP | CHANNELS_SH(3) | BYTES_SH(1);
    public static final int PT_ABGR_8 =
        DOSWAP | EXTRA_SH(1) | CHANNELS_SH(3) | BYTES_SH(1);
    public static final int PT_BGRA_8 = EXTRA_SH(1) | CHANNELS_SH(3) |
        BYTES_SH(1) | DOSWAP | SWAPFIRST;
    public static final int DT_BYTE     = 0;
    public static final int DT_SHORT    = 1;
    public static final int DT_INT      = 2;
    public static final int DT_DOUBLE   = 3;
    boolean isIntPacked = false;
    int pixelType;
    int dataType;
    int width;
    int height;
    int nextRowOffset;
    int offset;
    Object dataArray;
    private LCMSImageLayout(int np, int pixelType, int pixelSize) {
        this.pixelType = pixelType;
        width = np;
        height = 1;
        nextRowOffset = np*pixelSize;
        offset = 0;
    }
    private LCMSImageLayout(int width, int height, int pixelType,
                            int pixelSize) {
        this.pixelType = pixelType;
        this.width = width;
        this.height = height;
        nextRowOffset = width*pixelSize;
        offset = 0;
    }
    public LCMSImageLayout(byte[] data, int np, int pixelType, int pixelSize) {
        this(np, pixelType, pixelSize);
        dataType = DT_BYTE;
        dataArray = data;
    }
    public LCMSImageLayout(short[] data, int np, int pixelType, int pixelSize) {
        this(np, pixelType, pixelSize);
        dataType = DT_SHORT;
        dataArray = data;
    }
    public LCMSImageLayout(int[] data, int np, int pixelType, int pixelSize) {
        this(np, pixelType, pixelSize);
        dataType = DT_INT;
        dataArray = data;
    }
    public LCMSImageLayout(double[] data, int np, int pixelType, int pixelSize)
    {
        this(np, pixelType, pixelSize);
        dataType = DT_DOUBLE;
        dataArray = data;
    }
    public LCMSImageLayout(BufferedImage image) {
        ShortComponentRaster shortRaster;
        IntegerComponentRaster intRaster;
        ByteComponentRaster byteRaster;
        switch (image.getType()) {
            case BufferedImage.TYPE_INT_RGB:
                pixelType = PT_ARGB_8;
                isIntPacked = true;
                break;
            case BufferedImage.TYPE_INT_ARGB:
                pixelType = PT_ARGB_8;
                isIntPacked = true;
                break;
            case BufferedImage.TYPE_INT_BGR:
                pixelType = PT_ABGR_8;
                isIntPacked = true;
                break;
            case BufferedImage.TYPE_3BYTE_BGR:
                pixelType = PT_BGR_8;
                break;
            case BufferedImage.TYPE_4BYTE_ABGR:
                pixelType = PT_ABGR_8;
                break;
            case BufferedImage.TYPE_BYTE_GRAY:
                pixelType = PT_GRAY_8;
                break;
            case BufferedImage.TYPE_USHORT_GRAY:
                pixelType = PT_GRAY_16;
                break;
            default:
                throw new IllegalArgumentException(
                    "CMMImageLayout - bad image type passed to constructor");
        }
        width = image.getWidth();
        height = image.getHeight();
        switch (image.getType()) {
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_BGR:
                intRaster = (IntegerComponentRaster)image.getRaster();
                nextRowOffset = intRaster.getScanlineStride()*4;
                offset = intRaster.getDataOffset(0)*4;
                dataArray = intRaster.getDataStorage();
                dataType = DT_INT;
                break;
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_4BYTE_ABGR:
                byteRaster = (ByteComponentRaster)image.getRaster();
                nextRowOffset = byteRaster.getScanlineStride();
                offset = byteRaster.getDataOffset(0);
                dataArray = byteRaster.getDataStorage();
                dataType = DT_BYTE;
                break;
            case BufferedImage.TYPE_BYTE_GRAY:
                byteRaster = (ByteComponentRaster)image.getRaster();
                nextRowOffset = byteRaster.getScanlineStride();
                offset = byteRaster.getDataOffset(0);
                dataArray = byteRaster.getDataStorage();
                dataType = DT_BYTE;
                break;
            case BufferedImage.TYPE_USHORT_GRAY:
                shortRaster = (ShortComponentRaster)image.getRaster();
                nextRowOffset = shortRaster.getScanlineStride()*2;
                offset = shortRaster.getDataOffset(0) * 2;
                dataArray = shortRaster.getDataStorage();
                dataType = DT_SHORT;
                break;
        }
    }
    public static boolean isSupported(BufferedImage image) {
        switch (image.getType()) {
            case BufferedImage.TYPE_INT_RGB:
            case BufferedImage.TYPE_INT_ARGB:
            case BufferedImage.TYPE_INT_BGR:
            case BufferedImage.TYPE_3BYTE_BGR:
            case BufferedImage.TYPE_4BYTE_ABGR:
            case BufferedImage.TYPE_BYTE_GRAY:
            case BufferedImage.TYPE_USHORT_GRAY:
                return true;
        }
        return false;
    }
}
