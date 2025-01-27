public class PixelToParallelogramConverter extends PixelToShapeConverter
    implements ShapeDrawPipe
{
    ParallelogramPipe outrenderer;
    double minPenSize;
    double normPosition;
    double normRoundingBias;
    boolean adjustfill;
    public PixelToParallelogramConverter(ShapeDrawPipe shapepipe,
                                         ParallelogramPipe pgrampipe,
                                         double minPenSize,
                                         double normPosition,
                                         boolean adjustfill)
    {
        super(shapepipe);
        outrenderer = pgrampipe;
        this.minPenSize = minPenSize;
        this.normPosition = normPosition;
        this.normRoundingBias = 0.5 - normPosition;
        this.adjustfill = adjustfill;
    }
    public void drawLine(SunGraphics2D sg2d,
                         int x1, int y1, int x2, int y2)
    {
        if (!drawGeneralLine(sg2d, x1, y1, x2, y2)) {
            super.drawLine(sg2d, x1, y1, x2, y2);
        }
    }
    public void drawRect(SunGraphics2D sg2d,
                         int x, int y, int w, int h)
    {
        if (w >= 0 && h >= 0) {
            if (sg2d.strokeState < SunGraphics2D.STROKE_CUSTOM) {
                BasicStroke bs = ((BasicStroke) sg2d.stroke);
                if (w > 0 && h > 0) {
                    if (bs.getLineJoin() == BasicStroke.JOIN_MITER &&
                        bs.getDashArray() == null)
                    {
                        double lw = bs.getLineWidth();
                        drawRectangle(sg2d, x, y, w, h, lw);
                        return;
                    }
                } else {
                    drawLine(sg2d, x, y, x+w, y+h);
                    return;
                }
            }
            super.drawRect(sg2d, x, y, w, h);
        }
    }
    public void fillRect(SunGraphics2D sg2d,
                         int x, int y, int w, int h)
    {
        if (w > 0 && h > 0) {
            fillRectangle(sg2d, x, y, w, h);
        }
    }
    public void draw(SunGraphics2D sg2d, Shape s) {
        if (sg2d.strokeState < SunGraphics2D.STROKE_CUSTOM) {
            BasicStroke bs = ((BasicStroke) sg2d.stroke);
            if (s instanceof Rectangle2D) {
                if (bs.getLineJoin() == BasicStroke.JOIN_MITER &&
                    bs.getDashArray() == null)
                {
                    Rectangle2D r2d = (Rectangle2D) s;
                    double w = r2d.getWidth();
                    double h = r2d.getHeight();
                    double x = r2d.getX();
                    double y = r2d.getY();
                    if (w >= 0 && h >= 0) {
                        double lw = bs.getLineWidth();
                        drawRectangle(sg2d, x, y, w, h, lw);
                    }
                    return;
                }
            } else if (s instanceof Line2D) {
                Line2D l2d = (Line2D) s;
                if (drawGeneralLine(sg2d,
                                    l2d.getX1(), l2d.getY1(),
                                    l2d.getX2(), l2d.getY2()))
                {
                    return;
                }
            }
        }
        outpipe.draw(sg2d, s);
    }
    public void fill(SunGraphics2D sg2d, Shape s) {
        if (s instanceof Rectangle2D) {
            Rectangle2D r2d = (Rectangle2D) s;
            double w = r2d.getWidth();
            double h = r2d.getHeight();
            if (w > 0 && h > 0) {
                double x = r2d.getX();
                double y = r2d.getY();
                fillRectangle(sg2d, x, y, w, h);
            }
            return;
        }
        outpipe.fill(sg2d, s);
    }
    static double len(double x, double y) {
        return ((x == 0) ? Math.abs(y)
                : ((y == 0) ? Math.abs(x)
                   : Math.sqrt(x * x + y * y)));
    }
    double normalize(double v) {
        return Math.floor(v + normRoundingBias) + normPosition;
    }
    public boolean drawGeneralLine(SunGraphics2D sg2d,
                                   double ux1, double uy1,
                                   double ux2, double uy2)
    {
        if (sg2d.strokeState == SunGraphics2D.STROKE_CUSTOM ||
            sg2d.strokeState == SunGraphics2D.STROKE_THINDASHED)
        {
            return false;
        }
        BasicStroke bs = (BasicStroke) sg2d.stroke;
        int cap = bs.getEndCap();
        if (cap == BasicStroke.CAP_ROUND || bs.getDashArray() != null) {
            return false;
        }
        double lw = bs.getLineWidth();
        double dx = ux2 - ux1;
        double dy = uy2 - uy1;
        double x1, y1, x2, y2;
        switch (sg2d.transformState) {
        case SunGraphics2D.TRANSFORM_GENERIC:
        case SunGraphics2D.TRANSFORM_TRANSLATESCALE:
            {
                double coords[] = {ux1, uy1, ux2, uy2};
                sg2d.transform.transform(coords, 0, coords, 0, 2);
                x1 = coords[0];
                y1 = coords[1];
                x2 = coords[2];
                y2 = coords[3];
            }
            break;
        case SunGraphics2D.TRANSFORM_ANY_TRANSLATE:
        case SunGraphics2D.TRANSFORM_INT_TRANSLATE:
            {
                double tx = sg2d.transform.getTranslateX();
                double ty = sg2d.transform.getTranslateY();
                x1 = ux1 + tx;
                y1 = uy1 + ty;
                x2 = ux2 + tx;
                y2 = uy2 + ty;
            }
            break;
        case SunGraphics2D.TRANSFORM_ISIDENT:
            x1 = ux1;
            y1 = uy1;
            x2 = ux2;
            y2 = uy2;
            break;
        default:
            throw new InternalError("unknown TRANSFORM state...");
        }
        if (sg2d.strokeHint != SunHints.INTVAL_STROKE_PURE) {
            if (sg2d.strokeState == SunGraphics2D.STROKE_THIN &&
                outrenderer instanceof PixelDrawPipe)
            {
                int ix1 = (int) Math.floor(x1 - sg2d.transX);
                int iy1 = (int) Math.floor(y1 - sg2d.transY);
                int ix2 = (int) Math.floor(x2 - sg2d.transX);
                int iy2 = (int) Math.floor(y2 - sg2d.transY);
                ((PixelDrawPipe)outrenderer).drawLine(sg2d, ix1, iy1, ix2, iy2);
                return true;
            }
            x1 = normalize(x1);
            y1 = normalize(y1);
            x2 = normalize(x2);
            y2 = normalize(y2);
        }
        if (sg2d.transformState >= SunGraphics2D.TRANSFORM_TRANSLATESCALE) {
            double len = len(dx, dy);
            if (len == 0) {
                dx = len = 1;
            }
            double unitvector[] = {dy/len, -dx/len};
            sg2d.transform.deltaTransform(unitvector, 0, unitvector, 0, 1);
            lw *= len(unitvector[0], unitvector[1]);
        }
        lw = Math.max(lw, minPenSize);
        dx = x2 - x1;
        dy = y2 - y1;
        double len = len(dx, dy);
        double udx, udy;
        if (len == 0) {
            if (cap == BasicStroke.CAP_BUTT) {
                return true;
            }
            udx = lw;
            udy = 0;
        } else {
            udx = lw * dx / len;
            udy = lw * dy / len;
        }
        double px = x1 + udy / 2.0;
        double py = y1 - udx / 2.0;
        if (cap == BasicStroke.CAP_SQUARE) {
            px -= udx / 2.0;
            py -= udy / 2.0;
            dx += udx;
            dy += udy;
        }
        outrenderer.fillParallelogram(sg2d, ux1, uy1, ux2, uy2,
                                      px, py, -udy, udx, dx, dy);
        return true;
    }
    public void fillRectangle(SunGraphics2D sg2d,
                              double rx, double ry,
                              double rw, double rh)
    {
        double px, py;
        double dx1, dy1, dx2, dy2;
        AffineTransform txform = sg2d.transform;
        dx1 = txform.getScaleX();
        dy1 = txform.getShearY();
        dx2 = txform.getShearX();
        dy2 = txform.getScaleY();
        px = rx * dx1 + ry * dx2 + txform.getTranslateX();
        py = rx * dy1 + ry * dy2 + txform.getTranslateY();
        dx1 *= rw;
        dy1 *= rw;
        dx2 *= rh;
        dy2 *= rh;
        if (adjustfill &&
            sg2d.strokeState < SunGraphics2D.STROKE_CUSTOM &&
            sg2d.strokeHint != SunHints.INTVAL_STROKE_PURE)
        {
            double newx = normalize(px);
            double newy = normalize(py);
            dx1 = normalize(px + dx1) - newx;
            dy1 = normalize(py + dy1) - newy;
            dx2 = normalize(px + dx2) - newx;
            dy2 = normalize(py + dy2) - newy;
            px = newx;
            py = newy;
        }
        outrenderer.fillParallelogram(sg2d, rx, ry, rx+rw, ry+rh,
                                      px, py, dx1, dy1, dx2, dy2);
    }
    public void drawRectangle(SunGraphics2D sg2d,
                              double rx, double ry,
                              double rw, double rh,
                              double lw)
    {
        double px, py;
        double dx1, dy1, dx2, dy2;
        double lw1, lw2;
        AffineTransform txform = sg2d.transform;
        dx1 = txform.getScaleX();
        dy1 = txform.getShearY();
        dx2 = txform.getShearX();
        dy2 = txform.getScaleY();
        px = rx * dx1 + ry * dx2 + txform.getTranslateX();
        py = rx * dy1 + ry * dy2 + txform.getTranslateY();
        lw1 = len(dx1, dy1) * lw;
        lw2 = len(dx2, dy2) * lw;
        dx1 *= rw;
        dy1 *= rw;
        dx2 *= rh;
        dy2 *= rh;
        if (sg2d.strokeState < SunGraphics2D.STROKE_CUSTOM &&
            sg2d.strokeHint != SunHints.INTVAL_STROKE_PURE)
        {
            double newx = normalize(px);
            double newy = normalize(py);
            dx1 = normalize(px + dx1) - newx;
            dy1 = normalize(py + dy1) - newy;
            dx2 = normalize(px + dx2) - newx;
            dy2 = normalize(py + dy2) - newy;
            px = newx;
            py = newy;
        }
        lw1 = Math.max(lw1, minPenSize);
        lw2 = Math.max(lw2, minPenSize);
        double len1 = len(dx1, dy1);
        double len2 = len(dx2, dy2);
        if (lw1 >= len1 || lw2 >= len2) {
            fillOuterParallelogram(sg2d,
                                   rx, ry, rx+rw, ry+rh,
                                   px, py, dx1, dy1, dx2, dy2,
                                   len1, len2, lw1, lw2);
        } else {
            outrenderer.drawParallelogram(sg2d,
                                          rx, ry, rx+rw, ry+rh,
                                          px, py, dx1, dy1, dx2, dy2,
                                          lw1 / len1, lw2 / len2);
        }
    }
    public void fillOuterParallelogram(SunGraphics2D sg2d,
                                       double ux1, double uy1,
                                       double ux2, double uy2,
                                       double px, double py,
                                       double dx1, double dy1,
                                       double dx2, double dy2,
                                       double len1, double len2,
                                       double lw1, double lw2)
    {
        double udx1 = dx1 / len1;
        double udy1 = dy1 / len1;
        double udx2 = dx2 / len2;
        double udy2 = dy2 / len2;
        if (len1 == 0) {
            if (len2 == 0) {
                udx2 = 0;
                udy2 = 1;
            }
            udx1 = udy2;
            udy1 = -udx2;
        } else if (len2 == 0) {
            udx2 = udy1;
            udy2 = -udx1;
        }
        udx1 *= lw1;
        udy1 *= lw1;
        udx2 *= lw2;
        udy2 *= lw2;
        px -= (udx1 + udx2) / 2;
        py -= (udy1 + udy2) / 2;
        dx1 += udx1;
        dy1 += udy1;
        dx2 += udx2;
        dy2 += udy2;
        outrenderer.fillParallelogram(sg2d, ux1, uy1, ux2, uy2,
                                      px, py, dx1, dy1, dx2, dy2);
    }
}
