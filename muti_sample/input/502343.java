public class ReplicateScaleFilter extends ImageFilter {
    protected int srcWidth;
    protected int srcHeight;
    protected int destWidth;
    protected int destHeight;
    protected int[] srcrows;
    protected int[] srccols;
    protected Object outpixbuf;
    public ReplicateScaleFilter(int width, int height) {
        if (width == 0 || height == 0) {
            throw new IllegalArgumentException(Messages.getString("awt.234")); 
        }
        this.destWidth = width;
        this.destHeight = height;
    }
    @SuppressWarnings("unchecked")
    @Override
    public void setProperties(Hashtable<?, ?> props) {
        Hashtable<Object, Object> fprops;
        if (props == null) {
            fprops = new Hashtable<Object, Object>();
        } else {
            fprops = (Hashtable<Object, Object>)props.clone();
        }
        String propName = "Rescale Filters"; 
        String prop = "destWidth=" + destWidth + "; " + 
                "destHeight=" + destHeight; 
        Object o = fprops.get(propName);
        if (o != null) {
            if (o instanceof String) {
                prop = (String)o + "; " + prop; 
            } else {
                prop = o.toString() + "; " + prop; 
            }
        }
        fprops.put(propName, prop);
        consumer.setProperties(fprops);
    }
    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off,
            int scansize) {
        if (srccols == null) {
            initArrays();
        }
        int buff[];
        if (outpixbuf == null || !(outpixbuf instanceof int[])) {
            buff = new int[destWidth];
            outpixbuf = buff;
        } else {
            buff = (int[])outpixbuf;
        }
        int wa = (srcWidth - 1) >>> 1;
        int ha = (srcHeight - 1) >>> 1;
        int dstX = (x * destWidth + wa) / srcWidth;
        int dstY = (y * destHeight + ha) / srcHeight;
        int sx, sy, dx, dy;
        dy = dstY;
        while ((dy < destHeight) && ((sy = srcrows[dy]) < y + h)) {
            dx = dstX;
            int srcOff = off + (sy - y) * scansize;
            while ((dx < destWidth) && ((sx = srccols[dx]) < x + w)) {
                buff[dx] = pixels[srcOff + (sx - x)];
                dx++;
            }
            consumer.setPixels(dstX, dy, dx - dstX, 1, model, buff, dstX, destWidth);
            dy++;
        }
    }
    @Override
    public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off,
            int scansize) {
        if (srccols == null) {
            initArrays();
        }
        byte buff[];
        if (outpixbuf == null || !(outpixbuf instanceof byte[])) {
            buff = new byte[destWidth];
            outpixbuf = buff;
        } else {
            buff = (byte[])outpixbuf;
        }
        int wa = (srcWidth - 1) >>> 1;
        int ha = (srcHeight - 1) >>> 1;
        int dstX = (x * destWidth + wa) / srcWidth;
        int dstY = (y * destHeight + ha) / srcHeight;
        int sx, sy, dx, dy;
        dy = dstY;
        while ((dy < destHeight) && ((sy = srcrows[dy]) < y + h)) {
            dx = dstX;
            int srcOff = off + (sy - y) * scansize;
            while ((dx < destWidth) && ((sx = srccols[dx]) < x + w)) {
                buff[dx] = pixels[srcOff + (sx - x)];
                dx++;
            }
            consumer.setPixels(dstX, dy, dx - dstX, 1, model, buff, dstX, destWidth);
            dy++;
        }
    }
    @Override
    public void setDimensions(int w, int h) {
        srcWidth = w;
        srcHeight = h;
        if (destWidth < 0 && destHeight < 0) {
            destWidth = srcWidth;
            destHeight = srcHeight;
        } else if (destWidth < 0) {
            destWidth = destHeight * srcWidth / srcHeight;
        } else if (destHeight < 0) {
            destHeight = destWidth * srcHeight / srcWidth;
        }
        consumer.setDimensions(destWidth, destHeight);
    }
    private void initArrays() {
        if ((destWidth < 0) || (destHeight < 0)) {
            throw new IndexOutOfBoundsException();
        }
        srccols = new int[destWidth];
        int ca = srcWidth >>> 1;
        for (int i = 0; i < destWidth; i++) {
            srccols[i] = (i * srcWidth + ca) / destWidth;
        }
        srcrows = new int[destHeight];
        int ra = srcHeight >>> 1;
        for (int i = 0; i < destHeight; i++) {
            srcrows[i] = (i * srcHeight + ra) / destHeight;
        }
    }
}
