public class NullPipe
    implements PixelDrawPipe, PixelFillPipe, ShapeDrawPipe, TextPipe,
    DrawImagePipe
{
    public void drawLine(SunGraphics2D sg,
                         int x1, int y1, int x2, int y2) {
    }
    public void drawRect(SunGraphics2D sg,
                         int x, int y, int width, int height) {
    }
    public void fillRect(SunGraphics2D sg,
                         int x, int y, int width, int height) {
    }
    public void drawRoundRect(SunGraphics2D sg,
                              int x, int y, int width, int height,
                              int arcWidth, int arcHeight) {
    }
    public void fillRoundRect(SunGraphics2D sg,
                              int x, int y, int width, int height,
                              int arcWidth, int arcHeight) {
    }
    public void drawOval(SunGraphics2D sg,
                         int x, int y, int width, int height) {
    }
    public void fillOval(SunGraphics2D sg,
                         int x, int y, int width, int height) {
    }
    public void drawArc(SunGraphics2D sg,
                        int x, int y, int width, int height,
                        int startAngle, int arcAngle) {
    }
    public void fillArc(SunGraphics2D sg,
                        int x, int y, int width, int height,
                        int startAngle, int arcAngle) {
    }
    public void drawPolyline(SunGraphics2D sg,
                             int xPoints[], int yPoints[],
                             int nPoints) {
    }
    public void drawPolygon(SunGraphics2D sg,
                            int xPoints[], int yPoints[],
                            int nPoints) {
    }
    public void fillPolygon(SunGraphics2D sg,
                            int xPoints[], int yPoints[],
                            int nPoints) {
    }
    public void draw(SunGraphics2D sg, Shape s) {
    }
    public void fill(SunGraphics2D sg, Shape s) {
    }
    public void drawString(SunGraphics2D sg, String s, double x, double y) {
    }
    public void drawGlyphVector(SunGraphics2D sg, GlyphVector g,
                                float x, float y) {
    }
    public void drawChars(SunGraphics2D sg,
                                char data[], int offset, int length,
                                int x, int y) {
    }
    public boolean copyImage(SunGraphics2D sg, Image img,
                             int x, int y,
                             Color bgColor,
                             ImageObserver observer) {
        return false;
    }
    public boolean copyImage(SunGraphics2D sg, Image img,
                             int dx, int dy, int sx, int sy, int w, int h,
                             Color bgColor,
                             ImageObserver observer) {
        return false;
    }
    public boolean scaleImage(SunGraphics2D sg, Image img, int x, int y,
                              int w, int h,
                              Color bgColor,
                              ImageObserver observer) {
        return false;
    }
    public boolean scaleImage(SunGraphics2D sg, Image img,
                              int dx1, int dy1, int dx2, int dy2,
                              int sx1, int sy1, int sx2, int sy2,
                              Color bgColor,
                              ImageObserver observer) {
        return false;
    }
    public boolean transformImage(SunGraphics2D sg, Image img,
                                  AffineTransform atfm,
                                  ImageObserver observer) {
        return false;
    }
    public void transformImage(SunGraphics2D sg, BufferedImage img,
                               BufferedImageOp op, int x, int y) {
    }
}
