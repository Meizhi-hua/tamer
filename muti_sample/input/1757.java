public class ColConvDCMTest extends ColConvTest {
    final static int [][] imgTypes = {
        {BufferedImage.TYPE_INT_ARGB, 8, 8, 8, 0, 0},
        {BufferedImage.TYPE_INT_ARGB, 8, 8, 8, 1, 3},
        {BufferedImage.TYPE_INT_RGB, 8, 8, 8, 0, 0},
        {BufferedImage.TYPE_INT_RGB, 8, 8, 8, 1, 3},
        {BufferedImage.TYPE_INT_BGR, 8, 8, 8, 0, 0},
        {BufferedImage.TYPE_INT_BGR, 8, 8, 8, 1, 3},
        {BufferedImage.TYPE_USHORT_555_RGB, 5, 5, 5, 0, 1},
        {BufferedImage.TYPE_USHORT_555_RGB, 5, 5, 5, 1, 4},
        {BufferedImage.TYPE_USHORT_565_RGB, 5, 6, 5, 0, 2},
        {BufferedImage.TYPE_USHORT_565_RGB, 5, 6, 5, 1, 5}
    };
    final static int [] cSpaces = {
        ColorSpace.CS_sRGB,
        ColorSpace.CS_LINEAR_RGB,
    };
    final static double ACCURACY = 2.5;
    final static String [] gldImgNames = {
        "SRGB.png", "SRGB555.png", "SRGB565.png", "LRGB.png", "LRGB555.png",
        "LRGB565.png"
    };
    static BufferedImage [] gldImages = null;
    static boolean testImage(int type, int rBits, int gBits, int bBits,
                              int cs, BufferedImage gldImage,
                              double accuracy)
    {
        BufferedImage src = ImageFactory.createDCMImage(type, cs);
        BufferedImage dst = ImageFactory.createDstImage(
            BufferedImage.TYPE_INT_RGB);
        ColorConvertOp op = new ColorConvertOp(null);
        op.filter(src, dst);
        ImageComparator cmp = new ImageComparator(accuracy, rBits, gBits,
                                                  bBits);
        boolean result = cmp.compare(gldImage, dst);
        if (!result) {
            System.err.println(cmp.getStat());
        }
        return result;
    }
     static boolean testSubImage(int x0, int y0, int dx, int dy, int type,
                                 int rBits, int gBits, int bBits,
                                 int cs, BufferedImage gldImage,
                                 double accuracy)
     {
        BufferedImage src = ImageFactory.createDCMImage(type, cs);
        BufferedImage subSrc = src.getSubimage(x0, y0, dx, dy);
        BufferedImage dst = ImageFactory.createDstImage(
            BufferedImage.TYPE_INT_RGB);
        BufferedImage subDst = dst.getSubimage(x0, y0, dx, dy);
        ColorConvertOp op = new ColorConvertOp(null);
        op.filter(subSrc, subDst);
        ImageComparator cmp = new ImageComparator(accuracy, rBits, gBits,
                                                  bBits);
        boolean result = cmp.compare(subDst, gldImage, x0, y0, dx, dy);
        if (!result) {
            System.err.println(cmp.getStat());
        }
        return result;
     }
     synchronized public static void initGoldenImages() {
        if (gldImages == null) {
            gldImages = new BufferedImage[gldImgNames.length];
            for (int i = 0; i < gldImgNames.length; i++) {
                try {
                    File gldFile = new File(System.getProperty("test.src", "."),
                                            gldImgNames[i]);
                    gldImages[i] = ImageIO.read(gldFile);
                } catch (IOException e) {
                    throw new RuntimeException("Cannot initialize golden " +
                                               "image: " + gldImgNames[i]);
                }
            }
        }
     }
     public void init() {
        initGoldenImages();
     }
     public void runTest() {
        for (int i = 0; i < imgTypes.length; i++) {
            BufferedImage gldImage = gldImages[imgTypes[i][5]];
            if (!testImage(imgTypes[i][0], imgTypes[i][1], imgTypes[i][2],
                           imgTypes[i][3], cSpaces[imgTypes[i][4]], gldImage,
                           ACCURACY))
            {
                throw new RuntimeException(
                    "Invalid result of the ColorConvertOp for " +
                    "ColorSpace:" + getCSName(cSpaces[imgTypes[i][4]]) +
                    " Image type:" +
                    getImageTypeName(imgTypes[i][0]) + ". Golden image:" +
                    gldImgNames[imgTypes[i][5]]);
            }
            if (!testSubImage(SI_X, SI_Y, SI_W, SI_H, imgTypes[i][0],
                              imgTypes[i][1], imgTypes[i][2], imgTypes[i][3],
                              cSpaces[imgTypes[i][4]], gldImage, ACCURACY))
            {
                throw new RuntimeException(
                    "Invalid result of the ColorConvertOp for " +
                     "ColorSpace:" + getCSName(cSpaces[imgTypes[i][4]]) +
                     " Image type:" +
                     getImageTypeName(imgTypes[i][0]) + ". Golden image:" +
                     gldImgNames[imgTypes[i][5]]);
            }
        }
     }
     public static void main(String [] args) throws Exception {
         ColConvDCMTest test = new ColConvDCMTest();
         test.init();
         test.run();
     }
}
