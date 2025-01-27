public abstract class ColorSpace implements Serializable {
    private static final long serialVersionUID = -409452704308689724L;
    public static final int TYPE_XYZ = 0;
    public static final int TYPE_Lab = 1;
    public static final int TYPE_Luv = 2;
    public static final int TYPE_YCbCr = 3;
    public static final int TYPE_Yxy = 4;
    public static final int TYPE_RGB = 5;
    public static final int TYPE_GRAY = 6;
    public static final int TYPE_HSV = 7;
    public static final int TYPE_HLS = 8;
    public static final int TYPE_CMYK = 9;
    public static final int TYPE_CMY = 11;
    public static final int TYPE_2CLR = 12;
    public static final int TYPE_3CLR = 13;
    public static final int TYPE_4CLR = 14;
    public static final int TYPE_5CLR = 15;
    public static final int TYPE_6CLR = 16;
    public static final int TYPE_7CLR = 17;
    public static final int TYPE_8CLR = 18;
    public static final int TYPE_9CLR = 19;
    public static final int TYPE_ACLR = 20;
    public static final int TYPE_BCLR = 21;
    public static final int TYPE_CCLR = 22;
    public static final int TYPE_DCLR = 23;
    public static final int TYPE_ECLR = 24;
    public static final int TYPE_FCLR = 25;
    public static final int CS_sRGB = 1000;
    public static final int CS_LINEAR_RGB = 1004;
    public static final int CS_CIEXYZ = 1001;
    public static final int CS_PYCC = 1002;
    public static final int CS_GRAY = 1003;
    private static ColorSpace cs_Gray = null;
    private static ColorSpace cs_PYCC = null;
    private static ColorSpace cs_CIEXYZ = null;
    private static ColorSpace cs_LRGB = null;
    private static ColorSpace cs_sRGB = null;
    private int type;
    private int numComponents;
    protected ColorSpace(int type, int numcomponents) {
        this.numComponents = numcomponents;
        this.type = type;
    }
    public String getName(int idx) {
        if (idx < 0 || idx > numComponents - 1) {
            throw new IllegalArgumentException(Messages.getString("awt.16A", idx)); 
        }
      return "Unnamed color component #" + idx; 
    }
    public abstract float[] toRGB(float[] colorvalue);
    public abstract float[] toCIEXYZ(float[] colorvalue);
    public abstract float[] fromRGB(float[] rgbvalue);
    public abstract float[] fromCIEXYZ(float[] colorvalue);
    public float getMinValue(int component) {
        if (component < 0 || component > numComponents - 1) {
            throw new IllegalArgumentException(Messages.getString("awt.16A", component)); 
        }
        return 0;
    }
    public float getMaxValue(int component) {
        if (component < 0 || component > numComponents - 1) {
            throw new IllegalArgumentException(Messages.getString("awt.16A", component)); 
        }
        return 1;
    }
    public boolean isCS_sRGB() {
        return (this == cs_sRGB);
    }
    public int getType() {
        return type;
    }
    public int getNumComponents() {
        return numComponents;
    }
    public static ColorSpace getInstance(int colorspace) {
        switch (colorspace) {
            case CS_sRGB:
                if (cs_sRGB == null) {
                    cs_sRGB = new ICC_ColorSpace(
                            new ICC_ProfileStub(CS_sRGB));
                    LUTColorConverter.sRGB_CS = cs_sRGB;
                }
                return cs_sRGB;
            case CS_CIEXYZ:
                if (cs_CIEXYZ == null) {
                    cs_CIEXYZ = new ICC_ColorSpace(
                            new ICC_ProfileStub(CS_CIEXYZ));
                }
                return cs_CIEXYZ;
            case CS_GRAY:
                if (cs_Gray == null) {
                    cs_Gray = new ICC_ColorSpace(
                            new ICC_ProfileStub(CS_GRAY));
                    LUTColorConverter.LINEAR_GRAY_CS = cs_Gray;
                }
                return cs_Gray;
            case CS_PYCC:
                if (cs_PYCC == null) {
                    cs_PYCC = new ICC_ColorSpace(
                            new ICC_ProfileStub(CS_PYCC));
                }
                return cs_PYCC;
            case CS_LINEAR_RGB:
                if (cs_LRGB == null) {
                    cs_LRGB = new ICC_ColorSpace(
                            new ICC_ProfileStub(CS_LINEAR_RGB));
                    LUTColorConverter.LINEAR_GRAY_CS = cs_Gray;
                }
                return cs_LRGB;
            default:
        }
        throw new IllegalArgumentException(Messages.getString("Not a predefined colorspace")); 
    }
}