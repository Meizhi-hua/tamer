public class BMPSubsamplingTest {
    private static final int TYPE_INT_GRB = 0x100;
    private static final int TYPE_INT_GBR = 0x101;
    private static final int TYPE_INT_RBG = 0x102;
    private static final int TYPE_INT_BRG = 0x103;
    private static final int TYPE_3BYTE_RGB = 0x104;
    private static final int TYPE_3BYTE_GRB = 0x105;
    private static final int TYPE_USHORT_555_GRB = 0x106;
    private static final int TYPE_USHORT_555_BGR = 0x107;
    private static final int TYPE_USHORT_565_BGR = 0x108;
    private static final int TYPE_4BPP_INDEXED = 0x109;
    private String format = "BMP";
    private int[] img_types = new int[] {
        BufferedImage.TYPE_INT_RGB,
        BufferedImage.TYPE_INT_BGR,
        TYPE_INT_GRB,
        TYPE_INT_GBR,
        TYPE_INT_RBG,
        TYPE_INT_BRG,
        BufferedImage.TYPE_USHORT_555_RGB,
        BufferedImage.TYPE_USHORT_565_RGB,
        TYPE_USHORT_555_GRB,
        TYPE_USHORT_555_BGR,
        TYPE_USHORT_565_BGR,
        BufferedImage.TYPE_3BYTE_BGR,
        TYPE_3BYTE_RGB,
        TYPE_3BYTE_GRB,
        BufferedImage.TYPE_BYTE_INDEXED,
        TYPE_4BPP_INDEXED
    };
    Color[] colors = new Color[] { Color.red, Color.green, Color.blue };
    private final int srcXSubsampling = 3;
    private final int srcYSubsampling = 3;
    int dx = 300;
    int h = 300;
    int w = dx * colors.length + srcXSubsampling;
    public BMPSubsamplingTest() throws IOException {
        ImageWriter writer =
            ImageIO.getImageWritersByFormatName(format).next();
        ImageWriteParam wparam = writer.getDefaultWriteParam();
        wparam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        String[] types = wparam.getCompressionTypes();
        for (int t = 0; t < img_types.length; t++) {
            int img_type = img_types[t];
            System.out.println("Test for " + getImageTypeName(img_type));
            BufferedImage image = getTestImage(img_type);
            ImageTypeSpecifier specifier = new ImageTypeSpecifier(image);
            if (!writer.getOriginatingProvider().canEncodeImage(specifier)) {
                System.out.println("Writer does not support encoding this buffered image type.");
                continue;
            }
            for(int i=0; i<types.length; i++) {
                if ("BI_JPEG".equals(types[i])) {
                    continue;
                }
                if (canEncodeImage(types[i], specifier, img_type)) {
                    System.out.println("compression type: " + types[i] +
                        " Supported for " + getImageTypeName(img_type));
                } else {
                    System.out.println("compression type: " + types[i] +
                        " NOT Supported for " + getImageTypeName(img_type));
                    continue;
                }
                ImageWriteParam imageWriteParam = getImageWriteParam(writer, types[i]);
                imageWriteParam.setSourceSubsampling(srcXSubsampling,
                                                     srcYSubsampling,
                                                     0, 0);
                File outputFile = new File("subsampling_test_" +
                    getImageTypeName(img_type) + "__" +
                    types[i] + ".bmp");
                ImageOutputStream ios =
                    ImageIO.createImageOutputStream(outputFile);
                writer.setOutput(ios);
                IIOImage iioImg = new IIOImage(image, null, null);
                writer.write(null, iioImg, imageWriteParam);
                ios.flush();
                ios.close();
                BufferedImage outputImage = ImageIO.read(outputFile);
                checkTestImage(outputImage);
            }
        }
    }
    private ImageWriteParam getImageWriteParam(ImageWriter writer, String value) {
        ImageWriteParam imageWriteParam = writer.getDefaultWriteParam();
        imageWriteParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        imageWriteParam.setCompressionType(value);
        return imageWriteParam;
    }
    private boolean canEncodeImage(String compression,
                                   ImageTypeSpecifier imgType, int rawType)
    {
        int biType = imgType.getBufferedImageType();
        if ((!compression.equals("BI_BITFIELDS")) &&
            ((rawType == BufferedImage.TYPE_USHORT_565_RGB) ||
             (rawType == TYPE_USHORT_565_BGR)))
        {
            return false;
        }
        int bpp = imgType.getColorModel().getPixelSize();
        if (compression.equals("BI_RLE4") && bpp != 4) {
            return false;
        }
        if (compression.equals("BI_RLE8") && bpp != 8) {
            return false;
        }
        if (compression.equals("BI_PNG") &&
            ((rawType == TYPE_USHORT_555_GRB) ||
             (rawType == TYPE_USHORT_555_BGR)))
        {
            return false;
        }
        return true;
    }
    private String getImageTypeName(int t) {
        switch(t) {
          case BufferedImage.TYPE_INT_RGB:
              return "TYPE_INT_RGB";
          case BufferedImage.TYPE_INT_BGR:
              return "TYPE_INT_BGR";
          case TYPE_INT_GRB:
              return "TYPE_INT_GRB";
          case TYPE_INT_GBR:
              return "TYPE_INT_GBR";
          case TYPE_INT_RBG:
              return "TYPE_INT_RBG";
          case TYPE_INT_BRG:
              return "TYPE_INT_BRG";
          case BufferedImage.TYPE_USHORT_555_RGB:
              return "TYPE_USHORT_555_RGB";
          case BufferedImage.TYPE_USHORT_565_RGB:
              return "TYPE_USHORT_565_RGB";
          case TYPE_USHORT_555_GRB:
              return "TYPE_USHORT_555_GRB";
          case TYPE_USHORT_555_BGR:
              return "TYPE_USHORT_555_BGR";
          case TYPE_USHORT_565_BGR:
              return "TYPE_USHORT_565_BGR";
          case BufferedImage.TYPE_3BYTE_BGR:
              return "TYPE_3BYTE_BGR";
          case TYPE_3BYTE_RGB:
              return "TYPE_3BYTE_RGB";
          case TYPE_3BYTE_GRB:
              return "TYPE_3BYTE_GRB";
          case BufferedImage.TYPE_BYTE_INDEXED:
              return "TYPE_BYTE_INDEXED";
          case TYPE_4BPP_INDEXED:
              return "TYPE_BYTE_INDEXED (4bpp)";
          default:
              throw new IllegalArgumentException("Unknown image type: " + t);
        }
    }
    private BufferedImage getTestImage(int type) {
        BufferedImage dst = null;
        ColorModel colorModel = null;
        WritableRaster raster = null;
        switch(type) {
          case TYPE_INT_GRB:
              colorModel = new DirectColorModel(24,
                                                0x0000ff00,
                                                0x00ff0000,
                                                0x000000ff);
              break;
          case TYPE_INT_GBR:
              colorModel = new DirectColorModel(24,
                                                0x000000ff,
                                                0x00ff0000,
                                                0x0000ff00);
              break;
          case TYPE_INT_RBG:
              colorModel = new DirectColorModel(24,
                                                0x00ff0000,
                                                0x000000ff,
                                                0x0000ff00);
              break;
          case TYPE_INT_BRG:
              colorModel = new DirectColorModel(24,
                                                0x0000ff00,
                                                0x000000ff,
                                                0x00ff0000);
                break;
          case TYPE_3BYTE_RGB:
              dst = create3ByteImage(new int[] {8, 8, 8},
                                     new int[] {0, 1, 2});
              break;
          case TYPE_3BYTE_GRB:
              dst = create3ByteImage(new int[] {8, 8, 8},
                                     new int[] {1, 0, 2});
              break;
          case TYPE_USHORT_555_GRB:
              colorModel = new DirectColorModel(16,
                                                0x03E0,
                                                0x7C00,
                                                0x001F);
              break;
          case TYPE_USHORT_555_BGR:
              colorModel = new DirectColorModel(16,
                                                0x001F,
                                                0x03E0,
                                                0x7C00);
              break;
          case TYPE_USHORT_565_BGR:
              colorModel = new DirectColorModel(16,
                                                0x001F,
                                                0x07E0,
                                                0xf800);
              break;
          case TYPE_4BPP_INDEXED:
              dst = createIndexImage(4);
              break;
          default:
              dst = new BufferedImage(w, h, type);
        }
        if (dst == null) {
            raster = colorModel.createCompatibleWritableRaster(w, h);
            dst = new BufferedImage(colorModel, raster, false, null);
        }
        Graphics2D g = dst.createGraphics();
        for (int i = 0; i < colors.length; i++) {
            g.setColor(colors[i]);
            g.fillRect(i * dx, 0, dx, h);
        }
        g.dispose();
        return dst;
    }
    private BufferedImage createIndexImage(int bpp) {
        int psize = (1 << bpp);
        byte[] r = new byte[psize];
        byte[] g = new byte[psize];
        byte[] b = new byte[psize];
        for (int i = 0; i < colors.length; i++) {
            r[i] = (byte)(0xff & colors[i].getRed());
            g[i] = (byte)(0xff & colors[i].getGreen());
            b[i] = (byte)(0xff & colors[i].getBlue());
        }
        IndexColorModel icm = new IndexColorModel(bpp, psize, r, g, b);
        return new BufferedImage(w, h, BufferedImage.TYPE_BYTE_INDEXED, icm);
    }
    private BufferedImage create3ByteImage(int[] nBits, int[] bOffs) {
        ColorSpace cs = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorModel colorModel =
            new ComponentColorModel(cs, nBits,
                                    false, false,
                                    Transparency.OPAQUE,
                                    DataBuffer.TYPE_BYTE);
        WritableRaster raster =
            Raster.createInterleavedRaster(DataBuffer.TYPE_BYTE,
                                           w, h,
                                           w*3, 3,
                                           bOffs, null);
        return new BufferedImage(colorModel, raster, false, null);
    }
    private void checkTestImage(BufferedImage dst) {
        int x = dx / (2 * srcXSubsampling);
        int y = h / (2 * srcYSubsampling);
        System.out.println("Check result: width=" + dst.getWidth() +
            ", height=" + dst.getHeight());
        for (int i = 0; i < colors.length; i++) {
            System.out.println("\tcheck at: " + x + ", " + y);
            int srcRgb = colors[i].getRGB();
            int dstRgb = dst.getRGB(x, y);
            if (srcRgb != dstRgb) {
                throw new RuntimeException("Test failed due to wrong dst color " +
                    Integer.toHexString(dstRgb) + " at " + x + "," + y +
                    "instead of " + Integer.toHexString(srcRgb));
            }
            x += dx / srcXSubsampling;
        }
    }
    public static void main(String args[]) throws IOException {
        BMPSubsamplingTest test = new BMPSubsamplingTest();
    }
}
