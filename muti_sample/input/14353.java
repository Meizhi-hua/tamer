public class TextRenderer extends GlyphListPipe {
    CompositePipe outpipe;
    public TextRenderer(CompositePipe pipe) {
        outpipe = pipe;
    }
    protected void drawGlyphList(SunGraphics2D sg2d, GlyphList gl) {
        int num = gl.getNumGlyphs();
        Region clipRegion = sg2d.getCompClip();
        int cx1 = clipRegion.getLoX();
        int cy1 = clipRegion.getLoY();
        int cx2 = clipRegion.getHiX();
        int cy2 = clipRegion.getHiY();
        Object ctx = null;
        try {
            int[] bounds = gl.getBounds();
            Rectangle r = new Rectangle(bounds[0], bounds[1],
                                        bounds[2] - bounds[0],
                                        bounds[3] - bounds[1]);
            Shape s = sg2d.untransformShape(r);
            ctx = outpipe.startSequence(sg2d, s, r, bounds);
            for (int i = 0; i < num; i++) {
                gl.setGlyphIndex(i);
                int metrics[] = gl.getMetrics();
                int gx1 = metrics[0];
                int gy1 = metrics[1];
                int w = metrics[2];
                int gx2 = gx1 + w;
                int gy2 = gy1 + metrics[3];
                int off = 0;
                if (gx1 < cx1) {
                    off = cx1 - gx1;
                    gx1 = cx1;
                }
                if (gy1 < cy1) {
                    off += (cy1 - gy1) * w;
                    gy1 = cy1;
                }
                if (gx2 > cx2) gx2 = cx2;
                if (gy2 > cy2) gy2 = cy2;
                if (gx2 > gx1 && gy2 > gy1 &&
                    outpipe.needTile(ctx, gx1, gy1, gx2 - gx1, gy2 - gy1))
                {
                    byte alpha[] = gl.getGrayBits();
                    outpipe.renderPathTile(ctx, alpha, off, w,
                                           gx1, gy1, gx2 - gx1, gy2 - gy1);
                } else {
                    outpipe.skipTile(ctx, gx1, gy1);
                }
            }
        } finally {
            if (ctx != null) {
                outpipe.endSequence(ctx);
            }
        }
    }
}
