public class Paint9Painter extends CachedPainter {
    public enum PaintType {
        CENTER,
        TILE,
        PAINT9_STRETCH,
        PAINT9_TILE
    };
    private static final Insets EMPTY_INSETS = new Insets(0, 0, 0, 0);
    public static final int PAINT_TOP_LEFT = 1;
    public static final int PAINT_TOP = 2;
    public static final int PAINT_TOP_RIGHT = 4;
    public static final int PAINT_LEFT = 8;
    public static final int PAINT_CENTER = 16;
    public static final int PAINT_RIGHT = 32;
    public static final int PAINT_BOTTOM_RIGHT = 64;
    public static final int PAINT_BOTTOM = 128;
    public static final int PAINT_BOTTOM_LEFT = 256;
    public static final int PAINT_ALL = 512;
    public static boolean validImage(Image image) {
        return (image != null && image.getWidth(null) > 0 &&
                image.getHeight(null) > 0);
    }
    public Paint9Painter(int cacheCount) {
        super(cacheCount);
    }
    public void paint(Component c, Graphics g, int x,
                      int y, int w, int h, Image source, Insets sInsets,
                      Insets dInsets,
                      PaintType type, int mask) {
        if (source == null) {
            return;
        }
        super.paint(c, g, x, y, w, h, source, sInsets, dInsets, type, mask);
    }
    protected void paintToImage(Component c, Image destImage, Graphics g,
                                int w, int h, Object[] args) {
        int argIndex = 0;
        while (argIndex < args.length) {
            Image image = (Image)args[argIndex++];
            Insets sInsets = (Insets)args[argIndex++];
            Insets dInsets = (Insets)args[argIndex++];
            PaintType type = (PaintType)args[argIndex++];
            int mask = (Integer)args[argIndex++];
            paint9(g, 0, 0, w, h, image, sInsets, dInsets, type, mask);
        }
    }
    protected void paint9(Graphics g, int x, int y, int w, int h,
                          Image image, Insets sInsets,
                          Insets dInsets, PaintType type, int componentMask) {
        if (!validImage(image)) {
            return;
        }
        if (sInsets == null) {
            sInsets = EMPTY_INSETS;
        }
        if (dInsets == null) {
            dInsets = EMPTY_INSETS;
        }
        int iw = image.getWidth(null);
        int ih = image.getHeight(null);
        if (type == PaintType.CENTER) {
            g.drawImage(image, x + (w - iw) / 2,
                        y + (h - ih) / 2, null);
        }
        else if (type == PaintType.TILE) {
            int lastIY = 0;
            for (int yCounter = y, maxY = y + h; yCounter < maxY;
                     yCounter += (ih - lastIY), lastIY = 0) {
                int lastIX = 0;
                for (int xCounter = x, maxX = x + w; xCounter < maxX;
                             xCounter += (iw - lastIX), lastIX = 0) {
                    int dx2 = Math.min(maxX, xCounter + iw - lastIX);
                    int dy2 = Math.min(maxY, yCounter + ih - lastIY);
                    g.drawImage(image, xCounter, yCounter, dx2, dy2,
                                lastIX, lastIY, lastIX + dx2 - xCounter,
                                lastIY + dy2 - yCounter, null);
                }
            }
        }
        else {
            int st = sInsets.top;
            int sl = sInsets.left;
            int sb = sInsets.bottom;
            int sr = sInsets.right;
            int dt = dInsets.top;
            int dl = dInsets.left;
            int db = dInsets.bottom;
            int dr = dInsets.right;
            if (st + sb > ih) {
                db = dt = sb = st = Math.max(0, ih / 2);
            }
            if (sl + sr > iw) {
                dl = dr = sl = sr = Math.max(0, iw / 2);
            }
            if (dt + db > h) {
                dt = db = Math.max(0, h / 2 - 1);
            }
            if (dl + dr > w) {
                dl = dr = Math.max(0, w / 2 - 1);
            }
            boolean stretch = (type == PaintType.PAINT9_STRETCH);
            if ((componentMask & PAINT_ALL) != 0) {
                componentMask = (PAINT_ALL - 1) & ~componentMask;
            }
            if ((componentMask & PAINT_LEFT) != 0) {
                drawChunk(image, g, stretch, x, y + dt, x + dl, y + h - db,
                          0, st, sl, ih - sb, false);
            }
            if ((componentMask & PAINT_TOP_LEFT) != 0) {
                drawImage(image, g, x, y, x + dl, y + dt,
                          0, 0, sl, st);
            }
            if ((componentMask & PAINT_TOP) != 0) {
                drawChunk(image, g, stretch, x + dl, y, x + w - dr, y + dt,
                          sl, 0, iw - sr, st, true);
            }
            if ((componentMask & PAINT_TOP_RIGHT) != 0) {
                drawImage(image, g, x + w - dr, y, x + w, y + dt,
                            iw - sr, 0, iw, st);
            }
            if ((componentMask & PAINT_RIGHT) != 0) {
                drawChunk(image, g, stretch,
                          x + w - dr, y + dt, x + w, y + h - db,
                          iw - sr, st, iw, ih - sb, false);
            }
            if ((componentMask & PAINT_BOTTOM_RIGHT) != 0) {
                drawImage(image, g, x + w - dr, y + h - db, x + w, y + h,
                            iw - sr, ih - sb, iw, ih);
            }
            if ((componentMask & PAINT_BOTTOM) != 0) {
                drawChunk(image, g, stretch,
                          x + dl, y + h - db, x + w - dr, y + h,
                          sl, ih - sb, iw - sr, ih, true);
            }
            if ((componentMask & PAINT_BOTTOM_LEFT) != 0) {
                drawImage(image, g, x, y + h - db, x + dl, y + h,
                          0, ih - sb, sl, ih);
            }
            if ((componentMask & PAINT_CENTER) != 0) {
                drawImage(image, g, x + dl, y + dt, x + w - dr, y + h - db,
                          sl, st, iw - sr, ih - sb);
            }
        }
    }
    private void drawImage(Image image, Graphics g,
                           int dx1, int dy1, int dx2, int dy2, int sx1,
                           int sy1, int sx2, int sy2) {
        if (dx2 - dx1 <= 0 || dy2 - dy1 <= 0 || sx2 - sx1 <= 0 ||
                              sy2 - sy1 <= 0) {
            return;
        }
        g.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
    }
    private void drawChunk(Image image, Graphics g, boolean stretch,
                           int dx1, int dy1, int dx2, int dy2, int sx1,
                           int sy1, int sx2, int sy2,
                           boolean xDirection) {
        if (dx2 - dx1 <= 0 || dy2 - dy1 <= 0 || sx2 - sx1 <= 0 ||
                              sy2 - sy1 <= 0) {
            return;
        }
        if (stretch) {
            g.drawImage(image, dx1, dy1, dx2, dy2, sx1, sy1, sx2, sy2, null);
        }
        else {
            int xSize = sx2 - sx1;
            int ySize = sy2 - sy1;
            int deltaX;
            int deltaY;
            if (xDirection) {
                deltaX = xSize;
                deltaY = 0;
            }
            else {
                deltaX = 0;
                deltaY = ySize;
            }
            while (dx1 < dx2 && dy1 < dy2) {
                int newDX2 = Math.min(dx2, dx1 + xSize);
                int newDY2 = Math.min(dy2, dy1 + ySize);
                g.drawImage(image, dx1, dy1, newDX2, newDY2,
                            sx1, sy1, sx1 + newDX2 - dx1,
                            sy1 + newDY2 - dy1, null);
                dx1 += deltaX;
                dy1 += deltaY;
            }
        }
    }
    protected Image createImage(Component c, int w, int h,
                                GraphicsConfiguration config,
                                Object[] args) {
        if (config == null) {
            return new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        }
        return config.createCompatibleImage(w, h, Transparency.TRANSLUCENT);
    }
}
