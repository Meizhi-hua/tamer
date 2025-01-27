public abstract class SpanShapeRenderer implements ShapeDrawPipe {
    final static RenderingEngine RenderEngine = RenderingEngine.getInstance();
    public static class Composite extends SpanShapeRenderer {
        CompositePipe comppipe;
        public Composite(CompositePipe pipe) {
            comppipe = pipe;
        }
        public Object startSequence(SunGraphics2D sg, Shape s,
                                    Rectangle devR, int[] bbox) {
            return comppipe.startSequence(sg, s, devR, bbox);
        }
        public void renderBox(Object ctx, int x, int y, int w, int h) {
            comppipe.renderPathTile(ctx, null, 0, w, x, y, w, h);
        }
        public void endSequence(Object ctx) {
            comppipe.endSequence(ctx);
        }
    }
    public static class Simple extends SpanShapeRenderer
        implements  LoopBasedPipe
    {
        public Object startSequence(SunGraphics2D sg, Shape s,
                                    Rectangle devR, int[] bbox) {
            return sg;
        }
        public void renderBox(Object ctx, int x, int y, int w, int h) {
            SunGraphics2D sg2d = (SunGraphics2D) ctx;
            SurfaceData sd = sg2d.getSurfaceData();
            sg2d.loops.fillRectLoop.FillRect(sg2d, sd, x, y, w, h);
        }
        public void endSequence(Object ctx) {
        }
    }
    public void draw(SunGraphics2D sg, Shape s) {
        if (sg.stroke instanceof BasicStroke) {
            ShapeSpanIterator sr = LoopPipe.getStrokeSpans(sg, s);
            try {
                renderSpans(sg, sg.getCompClip(), s, sr);
            } finally {
                sr.dispose();
            }
        } else {
            fill(sg, sg.stroke.createStrokedShape(s));
        }
    }
    public static final int NON_RECTILINEAR_TRANSFORM_MASK =
        (AffineTransform.TYPE_GENERAL_TRANSFORM |
         AffineTransform.TYPE_GENERAL_ROTATION);
    public void fill(SunGraphics2D sg, Shape s) {
        if (s instanceof Rectangle2D &&
            (sg.transform.getType() & NON_RECTILINEAR_TRANSFORM_MASK) == 0)
        {
            renderRect(sg, (Rectangle2D) s);
            return;
        }
        Region clipRegion = sg.getCompClip();
        ShapeSpanIterator sr = LoopPipe.getFillSSI(sg);
        try {
            sr.setOutputArea(clipRegion);
            sr.appendPath(s.getPathIterator(sg.transform));
            renderSpans(sg, clipRegion, s, sr);
        } finally {
            sr.dispose();
        }
    }
    public abstract Object startSequence(SunGraphics2D sg, Shape s,
                                         Rectangle devR, int[] bbox);
    public abstract void renderBox(Object ctx, int x, int y, int w, int h);
    public abstract void endSequence(Object ctx);
    public void renderRect(SunGraphics2D sg, Rectangle2D r) {
        double corners[] = {
            r.getX(), r.getY(), r.getWidth(), r.getHeight(),
        };
        corners[2] += corners[0];
        corners[3] += corners[1];
        if (corners[2] <= corners[0] || corners[3] <= corners[1]) {
            return;
        }
        sg.transform.transform(corners, 0, corners, 0, 2);
        if (corners[2] < corners[0]) {
            double t = corners[2];
            corners[2] = corners[0];
            corners[0] = t;
        }
        if (corners[3] < corners[1]) {
            double t = corners[3];
            corners[3] = corners[1];
            corners[1] = t;
        }
        int abox[] = {
            (int) corners[0],
            (int) corners[1],
            (int) corners[2],
            (int) corners[3],
        };
        Rectangle devR = new Rectangle(abox[0], abox[1],
                                       abox[2] - abox[0],
                                       abox[3] - abox[1]);
        Region clipRegion = sg.getCompClip();
        clipRegion.clipBoxToBounds(abox);
        if (abox[0] >= abox[2] || abox[1] >= abox[3]) {
            return;
        }
        Object context = startSequence(sg, r, devR, abox);
        if (clipRegion.isRectangular()) {
            renderBox(context, abox[0], abox[1],
                      abox[2] - abox[0],
                      abox[3] - abox[1]);
        } else {
            SpanIterator sr = clipRegion.getSpanIterator(abox);
            while (sr.nextSpan(abox)) {
                renderBox(context, abox[0], abox[1],
                              abox[2] - abox[0],
                              abox[3] - abox[1]);
            }
        }
        endSequence(context);
    }
    public void renderSpans(SunGraphics2D sg, Region clipRegion, Shape s,
                            ShapeSpanIterator sr)
    {
        Object context = null;
        int abox[] = new int[4];
        try {
            sr.getPathBox(abox);
            Rectangle devR = new Rectangle(abox[0], abox[1],
                                           abox[2] - abox[0],
                                           abox[3] - abox[1]);
            clipRegion.clipBoxToBounds(abox);
            if (abox[0] >= abox[2] || abox[1] >= abox[3]) {
                return;
            }
            sr.intersectClipBox(abox[0], abox[1], abox[2], abox[3]);
            context = startSequence(sg, s, devR, abox);
            spanClipLoop(context, sr, clipRegion, abox);
        } finally {
            if (context != null) {
                endSequence(context);
            }
        }
    }
    public void spanClipLoop(Object ctx, SpanIterator sr,
                             Region r, int[] abox) {
        if (!r.isRectangular()) {
            sr = r.filter(sr);
        }
        while (sr.nextSpan(abox)) {
            int x = abox[0];
            int y = abox[1];
            renderBox(ctx, x, y, abox[2] - x, abox[3] - y);
        }
    }
}
